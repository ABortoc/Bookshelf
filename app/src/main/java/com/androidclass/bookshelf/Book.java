package com.androidclass.bookshelf;

import android.graphics.drawable.Drawable;

public class Book {

    private String mAuthor;
    private String mTitle;
    private String mDate;
    private int mRank;
    private Drawable mDrawable;
    private String mDescription;

    public Book() {
    }

    public Book(String mAuthor, String mTitle, String mDate , int mRank,
                Drawable mDrawable, String mDescription) {
        this.mAuthor = mAuthor;
        this.mTitle = mTitle;
        this.mDate = mDate;
        this.mRank = mRank;
        this.mDrawable = mDrawable;
        this.mDescription = mDescription;
    }

    public Book(String mAuthor, String mTitle, String mDate) {
        this.mAuthor = mAuthor;
        this.mTitle = mTitle;
        this.mDate = mDate;
    }

    public Book(String mAuthor, String mTitle, String mDate, Drawable mDrawable) {
        this.mAuthor = mAuthor;
        this.mTitle = mTitle;
        this.mDate = mDate;
        this.mDrawable = mDrawable;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmDate() {
        return mDate;
    }

    public int getmRank() {
        return mRank;
    }

    public Drawable getmDrawable() {
        return mDrawable;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public void setmRank(int mRank) {
        this.mRank = mRank;
    }

    public void setmDrawable(Drawable mDrawable) {
        this.mDrawable = mDrawable;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

}
