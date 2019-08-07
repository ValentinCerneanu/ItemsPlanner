package com.godmother.itemsplanner.models;

public class BookingWrapper {
    private Booking booking;
    private Interval interval;
    private String phoneNumber;

    public BookingWrapper(Booking booking, Interval interval) {
        this.booking = booking;
        this.interval = interval;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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
        if(phoneNumber == null)
            return booking.toString() + "\n" + interval.toString();
        return booking.toString() + "\n" + phoneNumber + "\n" + interval.toString();
    }
}
