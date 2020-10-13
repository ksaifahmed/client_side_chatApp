package com.example.talks;

public class ContactClass
{
    private String message;
    private String name;
    private String uid;
    private String from;

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    private String seen;
    private Long time;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public ContactClass(String message, String name, String uid, String from, String seen, Long time) {
        this.message = message;
        this.name = name;
        this.uid = uid;
        this.from = from;
        this.seen = seen;
        this.time = time;
    }

    public ContactClass() {

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
