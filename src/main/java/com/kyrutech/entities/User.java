package com.kyrutech.entities;

import javax.persistence.*;

/**
 * Created by kdrudy on 9/26/16.
 */
@Entity
@Table(name = "users")
public class User {

    @GeneratedValue
    @Id
    int id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String password;

    @Column
    boolean admin;

    public User() {
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        admin = false;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
