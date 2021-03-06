package com.kyrutech.controllers;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.kyrutech.entities.Brand;
import com.kyrutech.entities.User;
import com.kyrutech.services.BrandRepository;
import com.kyrutech.services.UserRepository;
import com.kyrutech.utilities.PasswordStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by kdrudy on 9/26/16.
 */
@Controller
public class BrandFavoritesController {

    @Autowired
    UserRepository users;

    @Autowired
    BrandRepository brands;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    String bucket;


    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(HttpSession session, Model model) {

        Integer oldFirstId = (Integer) model.asMap().get("oldFirst");
        Integer oldSecondId = (Integer) model.asMap().get("oldSecond");

        if(oldFirstId == null) {
            oldFirstId = -1;
        }

        if(oldSecondId == null) {
            oldSecondId = -1;
        }

        long brandCount = brands.count();

        ArrayList<Brand> brandList = (ArrayList<Brand>) brands.findAll();

        //Get the first brand
        int firstBrand = (int) (Math.random()*brandCount);
        Brand first = brandList.get(firstBrand);

        while(first.getId() == oldFirstId || first.getId() == oldSecondId) {
            firstBrand = (int) (Math.random()*brandCount);
            first = brandList.get(firstBrand);
        }

        //Get the second brand
        int secondBrand = (int) (Math.random()*brandCount);
        Brand second = brandList.get(secondBrand);

        while(second.getId() == oldFirstId || second.getId() == oldSecondId || firstBrand == secondBrand) {
            secondBrand = (int) (Math.random()*brandCount);
            second = brandList.get(secondBrand);
        }

//        System.out.printf("%d:%d:%d:%d", oldFirstId, oldSecondId, firstBrand, secondBrand);
//        System.out.println();

        model.addAttribute("first", first);
        model.addAttribute("second", second);

        return "home";
    }

    @RequestMapping(path = "/vote", method = RequestMethod.POST)
    public String vote(Integer id, Integer loserId, HttpServletResponse response, RedirectAttributes attributes) {
        Brand winner = brands.findOne(id);
        Brand loser = brands.findOne(loserId);
        winner.setVoteCount(winner.getVoteCount() + 1);

        int wElo = winner.getEloRating();
        int lElo = loser.getEloRating();
        double wTRating = Math.pow(10, wElo/400);
        double lTRating = Math.pow(10, lElo/400);

        double wExpected = wTRating / (wTRating + lTRating);
        double lExpected = lTRating / (wTRating + lTRating);

        wElo = (int) (wElo + (32 * (1d-wExpected)));
        lElo = (int) (lElo + (32 * (0d-lExpected)));

        winner.setEloRating(wElo);
        loser.setEloRating(lElo);

        brands.save(winner);
        brands.save(loser);

        attributes.addFlashAttribute("oldFirst", winner.getId());
        attributes.addFlashAttribute("oldSecond", loser.getId());

        return "redirect:/";
    }

    @RequestMapping(path = "/unknown", method = RequestMethod.POST)
    public String unknown(Integer id, HttpServletResponse response, RedirectAttributes attributes) {
        Brand brand = brands.findOne(id);
        brand.setUnknownCount(brand.getUnknownCount() + 1);
        brands.save(brand);

        attributes.addFlashAttribute("oldFirst", brand.getId());

        return "redirect:/";
    }

    @RequestMapping(path = "/rankings", method = RequestMethod.GET)
    public String rankings(HttpSession session, Model model) {

        model.addAttribute("brands", brands.findAll(new Sort(Sort.Direction.DESC, "eloRating")));

        return "rankings";
    }

    @RequestMapping(path = "/upload", method = RequestMethod.GET)
    public String uploadBrand() {
        return "upload";
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String upload(String name, MultipartFile file, HttpServletResponse response) throws IOException {
        TransferManager tm = new TransferManager(amazonS3);
        tm.upload(bucket, file.getOriginalFilename(), file.getInputStream(), new ObjectMetadata());

        Brand brand = new Brand(name, file.getOriginalFilename());
        brands.save(brand);

        return "upload";
    }

    @RequestMapping(path = "/admin")
    public String admin(String order, HttpSession session, Model model) {
        String userName = (String) session.getAttribute("userName");
        User user = users.findFirstByName(userName);
        if (user != null) {
            model.addAttribute("user", user);
            if(order!=null && order.equalsIgnoreCase("elorating")) {
                model.addAttribute("brands", brands.findAll(new Sort(Sort.Direction.DESC, "eloRating")));
            } else if(order != null && order.equalsIgnoreCase("unknown")) {
                model.addAttribute("brands", brands.findAll(new Sort(Sort.Direction.DESC, "unknownCount")));
            } else {
                model.addAttribute("brands", brands.findAll(new Sort(Sort.Direction.DESC, "voteCount")));
            }
        }

        return "admin";
    }

    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public String delete(Integer id, HttpSession session, Model model) {
        String userName = (String) session.getAttribute("userName");
        User user = users.findFirstByName(userName);
        if (user != null && user.isAdmin()) {
            Brand brand = brands.findOne(id);
            amazonS3.deleteObject(bucket, brand.getImageLink());
            brands.delete(id);
        }
        return "redirect:/admin";
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(HttpSession session, String userName, String password) throws Exception {
        User user = users.findFirstByName(userName);
        if (!PasswordStorage.verifyPassword(password, user.getPassword())) {
            throw new Exception("Incorrect password");
        }
        session.setAttribute("userName", userName);
        return "redirect:/admin";
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin";
    }

    @PostConstruct
    public void init() throws PasswordStorage.CannotPerformOperationException {
        if(users.count() == 0) {
            User user = new User();
            user.setName("admin");
            user.setPassword(PasswordStorage.createHash("adm1n"));
            user.setAdmin(true);
            users.save(user);
        }

        if(brands.count() == 0) {
            brands.save(new Brand("Pepsi", "pepsi.png"));
            brands.save(new Brand("kyrutech", "kyrutech.jpg"));
        }
    }
}
