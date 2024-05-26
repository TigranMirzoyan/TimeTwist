package com.timetwist.events;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Event {
    private String id;
    private String name;
    private String username;
    private String description;
    private String email;
    private String status;
    private @ServerTimestamp Date dateTime;
    private int maxPeople;
    private int joinedPeople;

    public Event() {
    }


    public Event(String id, String name, String username,
                 String description, Date dateTime, int maxPeople,
                 String email, String status, int joinedPeople) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.description = description;
        this.dateTime = dateTime;
        this.maxPeople = maxPeople;
        this.email = email;
        this.status = status;
        this.joinedPeople = joinedPeople;
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

    public int getMaxPeople() {
        return maxPeople;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public int getJoinedPeople() {
        return joinedPeople;
    }
}
