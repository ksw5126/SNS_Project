package com.example.sns_project;

public class WriteInfo {

    private String title;
    private String contents;
    private String publisher;

    public WriteInfo(String name, String contents, String publisher) {
        this.title = name;
        this.contents = contents;
        this.publisher = publisher;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
