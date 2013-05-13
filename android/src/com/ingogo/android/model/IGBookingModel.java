package com.ingogo.android.model;

import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IGBookingModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3172867588901511888L;

	private String bookingId;
	private String bookingStatus;
	private String booked;
	private String bookingFee;
	private String bidExtra;
	private String bidInterval;
	private String licensePlate;
	private IGAddress pickupFrom;
	private IGAddress dropOffAt;
	private String passengerName;
	private String passengerMobileNumber;
	private String totalDue;
	private IGNetworkProfileModel networkDispatch;
	private String bookingType;
    private boolean hasRegisteredCard;

	public String getPassengerName() {
		return passengerName;
	}

	public void setPassengerName(String passengerName) {
		this.passengerName = passengerName;
	}

	public String getPassengerMobileNumber() {
		return passengerMobileNumber;
	}

	public void setPassengerMobileNumber(String passengerMobileNumber) {
		this.passengerMobileNumber = passengerMobileNumber;
	}

	public String getBookingId() {
		return bookingId;
	}

	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}

	public String getBookingStatus() {
		return bookingStatus;
	}

	public void setBookingStatus(String bookingStatus) {
		this.bookingStatus = bookingStatus;
	}

	public String getBooked() {
		return booked;
	}

	public void setBooked(String booked) {
		this.booked = booked;
	}

	public String getBookingFee() {
		return bookingFee;
	}

	public void setBookingFee(String bookingFee) {
		this.bookingFee = bookingFee;
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

	public String getLicensePlate() {
		return licensePlate;
	}

	public void setDriverLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getTotalDue() {
		return totalDue;
	}

	public void setTotalDue(String totalDue) {
		this.totalDue = totalDue;
	}

	public IGNetworkProfileModel getNetworkDispatch() {
		return networkDispatch;
	}

	public String getBookingType() {
		return bookingType;
	}

	public void setBookingType(String bookingType) {
		this.bookingType = bookingType;
	}

	public void setNetworkDispatch(IGNetworkProfileModel networkDispatch) {
		this.networkDispatch = networkDispatch;
	}

	public boolean isHasRegisteredCard() {
		return hasRegisteredCard;
	}

	public void setHasRegisteredCard(boolean hasRegisteredCard) {
		this.hasRegisteredCard = hasRegisteredCard;
	}

	public String toJsonString() {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(this);
	}

	public IGBookingModel toJsonModel(String details) {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(details, this.getClass());
	}

}
