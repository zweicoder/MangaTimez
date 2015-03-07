package com.pewpewpew.user.mangatimez;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by User on 24/2/15.
 */
@ParseClassName("Manga")
public class Manga extends ParseObject {
    private static final String MANGA_NAME = "name";
    private static final String MANGA_CHAPTER = "latestChapter";
    private static final String MANGA_READABLE_NAME = "readableName";
    public Manga(){}

    //Getters
    public String getMangaName() {
        return getString(MANGA_NAME);
    }
    public int getMangaChapter() {
        return getInt(MANGA_CHAPTER);
    }
    public String getReadableName(){return getString(MANGA_READABLE_NAME);}
    // Setters (to save new manga object when found)
    public void setMangaName(String mangaName) {
        put(MANGA_NAME, mangaName);
    }

    public void setMangaChapter(int mangaChapter) {
        put(MANGA_CHAPTER, mangaChapter);
    }

    public void setReadableName(String name){put(MANGA_READABLE_NAME, name);}


}
