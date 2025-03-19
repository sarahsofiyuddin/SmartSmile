package com.example.smartsmile;

public class DentalDisease {
    private String name;
    private int imageResId;
    private String fragmentTag;

    public DentalDisease(String name, int imageResId, String fragmentTag) {
        this.name = name;
        this.imageResId = imageResId;
        this.fragmentTag = fragmentTag;
    }

    public String getName() {
        return name; }
    public int getImageResId() {
        return imageResId; }
    public String getFragmentTag() {
        return fragmentTag; }
}
