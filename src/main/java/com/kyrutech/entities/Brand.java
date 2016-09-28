package com.kyrutech.entities;

import javax.persistence.*;

/**
 * Created by kdrudy on 9/26/16.
 */
@Entity
public class Brand {

    @GeneratedValue
    @Id
    int id;

    @Column(nullable = false)
    String name;

    @Column
    int voteCount;

    @Column
    int unknownCount;

    @Column
    String imageLink;

    @Column
    int eloRating;

    public Brand() {

    }

    public Brand(String name, String imageLink) {
        this.name = name;
        this.imageLink = imageLink;
        this.eloRating = 1000;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public int getUnknownCount() {
        return unknownCount;
    }

    public void setUnknownCount(int unknownCount) {
        this.unknownCount = unknownCount;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public int getEloRating() {
        return eloRating;
    }

    public void setEloRating(int eloRating) {
        this.eloRating = eloRating;
    }
}
