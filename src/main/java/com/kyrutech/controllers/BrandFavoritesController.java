package com.kyrutech.controllers;

import com.kyrutech.entities.Brand;
import com.kyrutech.entities.BrandImage;
import com.kyrutech.entities.User;
import com.kyrutech.services.BrandImageRepository;
import com.kyrutech.services.BrandRepository;
import com.kyrutech.services.UserRepository;
import com.kyrutech.utilities.PasswordStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
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
    BrandImageRepository brandImages;

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(HttpSession session, Model model) {
        //TODO Get two random brands
        long brandCount = brands.count();

        int firstBrand = (int) (Math.random()*brandCount);
        int secondBrand = (int) (Math.random()*brandCount);
        while(firstBrand == secondBrand) {
            secondBrand = (int) (Math.random()*brandCount);
        }

        ArrayList<Brand> brandList = (ArrayList<Brand>) brands.findAll();

        Brand first = brandList.get(firstBrand);
        Brand second = brandList.get(secondBrand);

        model.addAttribute("first", first);
        model.addAttribute("firstImage", first.getImage());
        model.addAttribute("second", second);
        model.addAttribute("secondImage", second.getImage());

        return "home";
    }

    @RequestMapping(path = "/vote", method = RequestMethod.POST)
    public String vote(Integer id, HttpServletResponse response) {
        Brand brand = brands.findOne(id);
        brand.setVoteCount(brand.getVoteCount() + 1);
        brands.save(brand);

        return "redirect:/";
    }

    @RequestMapping(path = "/unknown", method = RequestMethod.POST)
    public String unknown(Integer id, HttpServletResponse response) {
        Brand brand = brands.findOne(id);
        brand.setUnknownCount(brand.getUnknownCount() + 1);
        brands.save(brand);

        return "redirect:/";
    }

    @RequestMapping(path = "/rankings", method = RequestMethod.GET)
    public String rankings(HttpSession session, Model model) {

        model.addAttribute("brands", brands.findAll(new Sort(Sort.Direction.DESC, "voteCount")));

        return "rankings";
    }

    @RequestMapping(path = "/upload", method = RequestMethod.GET)
    public String uploadBrand() {
        return "upload";
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String upload(MultipartFile file, String name, HttpServletResponse response) throws IOException {
        File dir = new File("public/files");
        dir.mkdirs();
        File f = File.createTempFile("file", file.getOriginalFilename(), dir);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(file.getBytes());

        BrandImage bi = new BrandImage(f.getName(), file.getOriginalFilename());
        brandImages.save(bi);

        Brand brand = new Brand(name, bi);
        brands.save(brand);

        return "upload";
    }

    @RequestMapping(path = "/admin")
    public String admin(HttpSession session, Model model) {
        String userName = (String) session.getAttribute("userName");
        User user = users.findFirstByName(userName);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("brands", brands.findAll(new Sort(Sort.Direction.DESC, "voteCount")));
        }

        return "admin";
    }

    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public String delete(Integer id, HttpSession session, Model model) {
        String userName = (String) session.getAttribute("userName");
        User user = users.findFirstByName(userName);
        if (user != null && user.isAdmin()) {
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
    }
}
