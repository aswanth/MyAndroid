package com.ingogo.android.model;

import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IGPassengerInformationModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -841817299380171286L;
	private String initial;
	private String surname;
	private String passengerId;
	
	public String getPassengerId() {
		return passengerId;
	}
	public void setPassengerId(String passengerId) {
		this.passengerId = passengerId;
	}
	public String getInitial() {
		return initial;
	};
	public void setInitial(String initial) {
		this.initial = initial;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	
	public String toJsonString() {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(this);
	}

	public IGPassengerInformationModel toJsonModel(String details) {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(details, this.getClass());
	}

}
