package com.example.paper.recommend;

public class RecommendFile {
    private String mTitle;
    private String mValue;

    public RecommendFile(String title,String value) {
        this.mTitle = title;
        this.mValue = value;
    }


    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmValue() {
        return mValue;
    }

    public void setmValue(String mValue) {
        this.mValue = mValue;
    }
}