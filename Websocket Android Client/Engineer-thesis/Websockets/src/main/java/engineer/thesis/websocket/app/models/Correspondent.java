package engineer.thesis.websocket.app.models;

import java.util.Comparator;

public class Correspondent {


    String name;
    String address;
    String date;

    public Correspondent(String name, String address, String date) {
        this.name = name;
        this.address = address;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public static class CorrespondentComparator implements Comparator<Correspondent> {
        @Override
        public int compare(Correspondent c1, Correspondent c2) {
            return c1.getDate().compareTo(c2.getDate());
        }
    }


}
