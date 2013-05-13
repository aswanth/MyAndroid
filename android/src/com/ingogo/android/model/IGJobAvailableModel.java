package com.ingogo.android.model;

import java.io.Serializable;

public class IGJobAvailableModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5450077656730737632L;

	private String bookingId;
	private long booked;
	private IGAddress pickupFrom;
	private IGAddress dropOffAt;
	private String bidExtra;
	private String bidInterval;
	private String passengerName;
	private String passengerStatus;
	private String bookingStatus;
	private String passengerMobileNumber;
	private boolean hasRegisteredCard;
	private String passengerId;
	private String bookingType;

	public String getBookingId() {
		return bookingId;
	}

	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}

	public long getBooked() {
		return booked;
	}

	public void setBooked(long booked) {
		this.booked = booked;
	}

	public IGAddress getPickupFrom() {
		return pickupFrom;
	}

	public void setPickupFrom(IGAddress pickupFrom) {
		this.pickupFrom = pickupFrom;
	}

	public IGAddress getDropOffAt() {
		return dropOffAt;
	}

	public void setDropOffAt(IGAddress dropOffAt) {
		this.dropOffAt = dropOffAt;
	}

	public String getBidExtra() {
		return bidExtra;
	}

	public void setBidExtra(String bidExtra) {
		this.bidExtra = bidExtra;
	}

	public String getBidInterval() {
		return bidInterval;
	}

	public void setBidInterval(String bidInterval) {
		this.bidInterval = bidInterval;
	}

	public String getPassengerName() {
		return passengerName;
	}

	public void setPassengerName(String passengerName) {
		this.passengerName = passengerName;
	}

	public String getPassengerStatus() {
		return passengerStatus;
	}

	public void setPassengerStatus(String passengerStatus) {
		this.passengerStatus = passengerStatus;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getBookingStatus() {
		return bookingStatus;
	}

	public void setBookingStatus(String bookingStatus) {
		this.bookingStatus = bookingStatus;
	}

	public String getPassengerMobileNumber() {
		return passengerMobileNumber;
	}

	public void setPassengerMobileNumber(String passengerMobileNumber) {
		this.passengerMobileNumber = passengerMobileNumber;
	}

	public boolean isHasRegisteredCard() {
		return hasRegisteredCard;
	}

	public void setHasRegisteredCard(boolean hasRegisteredCard) {
		this.hasRegisteredCard = hasRegisteredCard;
	}

	public String getPassengerId() {
		return passengerId;
	}

	public void setPassengerId(String passengerId) {
		this.passengerId = passengerId;
	}

	public String getBookingType() {
		return bookingType;
	}

	public void setBookingType(String bookingType) {
		this.bookingType = bookingType;
	}

}
