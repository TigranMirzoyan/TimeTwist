package com.timetwist.events;

import java.util.Date;

public class Event {
    private String name;
    private String username;
    private String description;
    private Date dateTime;

    public Event() {
    }

    public Event(String name, String username, String description, Date dateTime) {
        this.name = name;
        this.username = username;
        this.description = description;
        this.dateTime = dateTime;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getDescription() {
        return description;
    }

    public Date getDateTime() {
        return dateTime;
    }
}
