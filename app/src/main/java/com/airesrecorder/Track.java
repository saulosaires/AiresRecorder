package com.airesrecorder;

/**
 * Created by saulo on 22/11/2015.
 */
public class Track {

    private String name;
    private String duration;
    private String size;
    private String date;
    private String path;

    public Track(String name, String duration, String size,String date, String path) {
        this.name = name;
        this.duration = duration;
        this.size = size;
        this.date=date;
        this.path=path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
