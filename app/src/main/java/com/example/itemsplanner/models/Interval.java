package com.example.itemsplanner.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        DateFormat dateFormat = new SimpleDateFormat("yyyy MM dd");
        try {
            Date fromFormated = dateFormat.parse(from);
            Date tillFormated = dateFormat.parse(till);
            String fromStringFormated = buildOutputDate(fromFormated);
            String tillStringFormated = buildOutputDate(tillFormated);
            if(from.equals(till)) {
                return "Data: " + fromStringFormated;
            }

            return "Interval: "
                    + fromStringFormated + " - "
                    + tillStringFormated;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String buildOutputDate(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return day + " " + month + " " + year;
    }
}