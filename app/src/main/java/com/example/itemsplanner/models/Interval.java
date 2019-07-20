package com.example.itemsplanner.models;

import java.util.Date;

public class Interval{
    private String from;
    private String till;

    public Interval(Date from, Date till) {
        this.from = from.toString();
        this.till = till.toString();
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