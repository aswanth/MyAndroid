package com.ingogo.android.webservices.beans.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

import com.ingogo.android.model.IGAddress;
import com.ingogo.android.model.IGMessageModel;

public class IGMapInfoResponseBean extends IGBaseResponseBean implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8128830259411034877L;
	
	private ArrayList< IGMessageModel  > messages = new ArrayList< IGMessageModel >();
    private String bookingStatus;
    private String name;
    private String passengerMobileNo;
    private Boolean callOnApproach;
    private IGAddress pickUpFrom;
    private BigDecimal pickupLongitude;
    private BigDecimal pickupLatitude;
    
	public ArrayList<IGMessageModel> getMessages() {
		return messages;
	}
	public void setMessages(ArrayList<IGMessageModel> messages) {
		this.messages = messages;
	}
	public String getBookingStatus() {
		return bookingStatus;
	}
	public void setBookingStatus(String bookingStatus) {
		this.bookingStatus = bookingStatus;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassengerMobileNo() {
		return passengerMobileNo;
	}
	public void setPassengerMobileNo(String passengerMobileNo) {
		this.passengerMobileNo = passengerMobileNo;
	}
	public Boolean getCallOnApproach() {
		return callOnApproach;
	}
	public void setCallOnApproach(Boolean callOnApproach) {
		this.callOnApproach = callOnApproach;
	}
	public IGAddress getPickUpFrom() {
		return pickUpFrom;
	}
	public void setPickUpFrom(IGAddress pickUpFrom) {
		this.pickUpFrom = pickUpFrom;
	}
	public BigDecimal getPickupLongitude() {
		return pickupLongitude;
	}
	public void setPickupLongitude(BigDecimal pickupLongitude) {
		this.pickupLongitude = pickupLongitude;
	}
	public BigDecimal getPickupLatitude() {
		return pickupLatitude;
	}
	public void setPickupLatitude(BigDecimal pickupLatitude) {
		this.pickupLatitude = pickupLatitude;
	}

    
}
