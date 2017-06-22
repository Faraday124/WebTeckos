package engineer.thesis.websocket.app.models;

import java.util.Comparator;

public class Message {


    private String number;
    private String body;
    private String date;
    private String direction;

    public Message() {
    }

    public Message(String number, String body, String date, String direction) {
        this.number = number;
        this.body = body;
        this.date = date;
        this.direction = direction;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public static class MessageComparator implements Comparator<Message> {
        @Override
        public int compare(Message m1, Message m2) {
            return m1.getDate().compareTo(m2.getDate());
        }
    }
}

