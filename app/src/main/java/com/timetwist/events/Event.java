package com.timetwist.events;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Event {
    private String id;
    private String name;
    private String username;
    private String description;
    private @ServerTimestamp Date dateTime;
    private int people;
    private String contacts;

    public Event() {
    }

    public Event(String id, String name, String username, String description, Date dateTime, int people, String contacts) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.description = description;
        this.dateTime = dateTime;
        this.people = people;
        this.contacts = contacts;
    }

    public String getId() {
        return id;
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

    public int getPeople() {
        return people;
    }

    public String getContacts() {
        return contacts;
    }
}
