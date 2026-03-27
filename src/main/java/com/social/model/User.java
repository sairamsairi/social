package com.social.model;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private String id;
    private String name;
    private List<String> notifications; // Thread-safe handling
    
    // Jackson requires a no-args constructor
    public User() {
        this.notifications = new CopyOnWriteArrayList<>();
    }
    
    public User(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.notifications = new CopyOnWriteArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public List<String> getNotifications() { return notifications; }
    public void setNotifications(List<String> notifications) { this.notifications = new CopyOnWriteArrayList<>(notifications); }
    
    public void addNotification(String message) {
        this.notifications.add(message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
