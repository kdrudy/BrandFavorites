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

    @OneToOne
    BrandImage image;

    public Brand() {

    }

    public Brand(String name, BrandImage image) {
        this.name = name;
        this.image = image;
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

    public BrandImage getImage() {
        return image;
    }

    public void setImage(BrandImage image) {
        this.image = image;
    }
}
