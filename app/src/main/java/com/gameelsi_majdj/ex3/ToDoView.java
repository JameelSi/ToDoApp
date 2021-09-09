package com.gameelsi_majdj.ex3;

public class ToDoView {
    // title of the ToDo
    private String title;
    // description of the ToDo
    private String description;
    // date and time of the ToDo
    private String dateTime;
   //id in database
    private int id;

    public ToDoView(String title, String description, String dateTime,int id) {
        this.title = title;
        this.description = description;
        this.dateTime=dateTime;
        this.id = id;
    }
    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDateTime(){return dateTime;}
    public int getId() { return id; }
}
