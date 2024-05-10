package com.timetwist.events;

import java.util.Date;

public class Event {
    private final String name;
    private final String username;
    private final String description;
    private final Date dateTime;

    public Event() {
        this.name = "";
        this.username = "";
        this.description = "";
        this.dateTime = new Date();
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
