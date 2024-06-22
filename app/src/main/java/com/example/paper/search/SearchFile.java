package com.example.paper.search;

public class SearchFile {
    private String mTitle;
    private String mValue;
    public SearchFile(String title,String value ){
        this.mTitle = title;
        this.mValue = value;
    }

    public String getmValue() {
        return mValue;
    }

    public void setmValue(String mValue) {
        this.mValue = mValue;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }
}