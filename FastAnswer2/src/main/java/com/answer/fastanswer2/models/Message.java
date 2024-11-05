package com.answer.fastanswer2.models;

import java.time.LocalDateTime;

public class Message {

    private int id;
    private String name;
    private String message;
    private LocalDateTime createdAt;

    public Message(int id, String message, LocalDateTime date, String name) {
        this.id = id;
        this.message = message;
        this.createdAt = date;
        this.name = name;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {

        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


    public LocalDateTime getCreatedAt() {

        return createdAt;
    }

    @Override
    public String toString() {
        return id + "," + message + "," + createdAt + "," + name ;
    }
}
