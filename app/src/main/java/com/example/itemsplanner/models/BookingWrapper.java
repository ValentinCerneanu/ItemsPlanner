package com.example.itemsplanner.models;

public class BookingWrapper {
    private Booking booking;
    private Interval interval;

    public BookingWrapper(Booking booking, Interval interval) {
        this.booking = booking;
        this.interval = interval;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    @Override
    public String toString() {
        return booking.toString() + "\n" + interval.toString();
    }
}
