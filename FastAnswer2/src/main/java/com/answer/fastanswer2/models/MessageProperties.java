package com.answer.fastanswer2.models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageProperties {
    private SimpleIntegerProperty id;
    private SimpleStringProperty message;
    private SimpleStringProperty date;
    private SimpleStringProperty name;

    public MessageProperties(int id, String message, LocalDateTime date, String name) {
        this.id = new SimpleIntegerProperty(id);
        this.message = new SimpleStringProperty(message);
        this.date = new SimpleStringProperty(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        this.name = new SimpleStringProperty(name);
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public SimpleStringProperty messageProperty() {
        return message;
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }
}
