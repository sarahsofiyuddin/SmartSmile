package com.example.smartsmile;

public class Child {
    private String name;
    private int age;

    public Child() {}  // Required for Firebase

    public Child(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}