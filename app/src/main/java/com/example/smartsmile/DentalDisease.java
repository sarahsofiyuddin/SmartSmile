package com.example.smartsmile;

import java.util.List;

public class DentalDisease {
    private String name;
    private List<Integer> imageList; // Store image resource IDs

    public DentalDisease(String name, List<Integer> imageList) {
        this.name = name;
        this.imageList = imageList;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getImageList() {
        return imageList;
    }
}