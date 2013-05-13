/**
 * Package Name : com.ingogo.android.model
 * Author 		: Ingogo
 * Copyright 	: Ingogo @ 2010-2011
 * Description 	: IGJob is used as data model to represent a job. 
 */

package com.ingogo.android.model;

import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.text.format.Time;

import com.ingogo.android.app.IGConstants;

public class IGJob implements Serializable {

	/**
	 * IGJob is passed between intents as serialized data.
	 */
	private static final long serialVersionUID = 55673452591254450L;
	private String _id;
	private String _pickupFrom;
	private String _dropOffTo;
	private String _shortAddress;
	private String _passengerName;
	private int _timeWithin;
	private String _bookingTime;
	private String _passengermobileNumber;

	private float _extraOffer;
	private long _timeStamp;
	private int _status = IGConstants.kJobOpen;
	private Boolean _isNewJob = true;
	private String _passengerStatus;

	public IGJob() {
		_pickupFrom = "";
		_dropOffTo = "";
		_passengerName = "";
		_shortAddress = "";
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}

	public String getPickupFrom() {
		return _pickupFrom;
	}

	public void setPickupFrom(String pickupFrom) {
		this._pickupFrom = pickupFrom;
	}

	public String getDropOffTo() {
		return _dropOffTo;
	}

	public String getshortAddress() {
		return _shortAddress;
	}

	public void setDropOffTo(String dropOffTo) {
		this._dropOffTo = dropOffTo;
	}

	public void setshortAddress(String shortAddress) {
		this._shortAddress = shortAddress;
	}

	public String getPassengerName() {
		return _passengerName;
	}

	public void setPassengerName(String passengerName) {
		this._passengerName = passengerName;
	}

	public int getTimeWithin() {
		return _timeWithin;
	}

	public void setTimeWithin(int timeWithin) {
		this._timeWithin = timeWithin;
	}

	public void setBookingTime(String bookingTime) {
		this._bookingTime = bookingTime;
	}

	public String getBookingTime() {
		return this._bookingTime;
	}

	public String getCollectTime() {
		String collectTime = null;
		return collectTime;
	}

	public float getExtraOffer() {
		return _extraOffer;
	}

	public void setExtraOffer(float extraOffer) {
		this._extraOffer = extraOffer;
	}

	public int getStatus() {
		return _status;
	}

	public void setStatus(int status) {
		this._status = status;
	}

	/**
	 * @return Current job description
	 */
	public String getJobDescription() {
		String jobDescription;

		jobDescription = this._shortAddress;

		return jobDescription;
	}

	public String timeStampToTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("h:mmaa");
		Calendar calendar = Calendar.getInstance();
		long timestamp = _timeStamp;
		Date date = new Date(timestamp);
		long newTime = date.getTime();
		Time time = new Time();
		time.set(newTime);
		time.minute += this._timeWithin;
		time.normalize(false);
		time.toMillis(false);
		time.format(time.toString());
		date.setTime(time.toMillis(false));
		calendar.setTime(date);
		return (formatter.format(calendar.getTime()));
	}

	/**
	 * Overridden to search a job with same job id.
	 */
	@Override
	public boolean equals(Object o) {

		if (o.getClass().equals(IGJob.class)) {
			IGJob job = (IGJob) o;
			if (job.getId().compareTo(getId()) == 0)
				return true;
			else
				return false;
		}

		return false;
	}

	public Boolean isNewJob() {
		return _isNewJob;
	}

	public void setNewJob(Boolean _isNewJob) {
		this._isNewJob = _isNewJob;
	}

	public void setTimeStamp(long timeStamp) {
		this._timeStamp = timeStamp;
	}

	public long getTimeStamp() {
		return _timeStamp;
	}

	public String getPassengermobileNumber() {
		return _passengermobileNumber;
	}

	public void setPassengermobileNumber(String _passengermobileNumber) {
		this._passengermobileNumber = _passengermobileNumber;
	}

	public String getPassengerStatus() {
		return _passengerStatus;
	}

	public void setPassengerStatus(String _passengerStatus) {
		this._passengerStatus = _passengerStatus;
	}

}
