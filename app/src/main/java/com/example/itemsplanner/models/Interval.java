package com.example.itemsplanner.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Interval{
    private String from;
    private String till;

    public Interval(Date from, Date till) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
        this.from = dateFormat.format(from);
        this.till = dateFormat.format(till);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from.toString();
    }

    public String getTill() {
        return till;
    }

    public void setTill(Date till) {
        this.till = till.toString();
    }

    @Override
    public String toString() {
        return "Interval{" +
                "from=" + from +
                ", till=" + till +
                '}';
    }
}