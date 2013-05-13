/**
 * Package Name : com.ingogo.android.model
 * Author 		: Ingogo
 * Copyright 	: Ingogo @ 2010-2011
 * Description 	: Used to generate jobs from api response.  
 */
package com.ingogo.android.model;

import java.util.ArrayList;

import android.util.Log;

import com.ingogo.android.app.IGConstants;
import com.ingogo.android.poll.IGChatService;

public class IGJobListModel {

	private ArrayList<IGJobAvailableModel> _jobList;
	private static String latestJobID;

	public String getLatestJobID() {
		return latestJobID;
	}

	public static void setLatestJobID(String jobID) {
		latestJobID = jobID;
	}

	public IGJobListModel(ArrayList<IGJobAvailableModel> jobListModel) {
		_jobList = jobListModel;
	}

	/**
	 * Generate and return job objects from API response.
	 * 
	 * @return available jobs for the driver
	 */
	public ArrayList<IGJob> getAvailablejobs() {

		ArrayList<IGJob> availableJobs = new ArrayList<IGJob>();

		for (int i = 0; i < _jobList.size(); i++) {
			IGJobAvailableModel model = _jobList.get(i);
			IGJob job = new IGJob();

			job.setId(model.getBookingId());
			job.setPassengerName(model.getPassengerName());
			job.setTimeStamp(model.getBooked());
			job.setPickupFrom(getFullAddressFromObject(model.getPickupFrom()));
			job.setshortAddress(getShortAddress(model.getPickupFrom()));

			if (model.getDropOffAt() != null) {
				String dropOffAddr = model.getDropOffAt().getSuburb()
						.toUpperCase();
				if (dropOffAddr == null) {
					dropOffAddr = "";
				}
				job.setDropOffTo(dropOffAddr);
			}

			String extraPayString = model.getBidExtra();
			try {
				job.setExtraOffer(Float.parseFloat(extraPayString));
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
				job.setExtraOffer(0.0f);
			}

			if (model.getBidInterval() != null) {
				job.setTimeWithin(Integer.parseInt(model.getBidInterval()));
			}
			if (model.getPassengerStatus() != null) {
				job.setPassengerStatus(model.getPassengerStatus());
			}
			availableJobs.add(job);
		}

		return availableJobs;
	}

	private String getFullAddressFromObject(IGAddress pickupFrom) {
		String addr = "";

		String buildingName = pickupFrom.getBuildingName();
		if (buildingName == null) {
			buildingName = "";
		} else {
			addr += "" + buildingName + ", ";
		}

		String unitNumber = pickupFrom.getUnitNumber();
		if (unitNumber == null) {
			unitNumber = "";
		} else {
			if (buildingName.equals("")) {
				addr += "" + unitNumber;

			} else {
				addr += "" + unitNumber;
			}
		}

		String streetNumber = pickupFrom.getStreetNumber();
		if (streetNumber == null) {
			streetNumber = "";
		} else {
			if (buildingName.equals("") && unitNumber.equals("")) {
				addr += "" + streetNumber;
			} else if ((!buildingName.equals("")) && unitNumber.equals("")) {
				addr += "" + streetNumber;
			} else {
				addr += "/" + streetNumber;
			}
		}

		String addressLine1 = pickupFrom.getAddressLine1();
		if (addressLine1 == null) {
			addressLine1 = "";
		} else if (buildingName.equals("") && unitNumber.equals("")
				&& streetNumber.equals("")) {
			addr += "" + addressLine1;
		} else {
			addr += " " + addressLine1;
		}

		String addressLine2 = pickupFrom.getAddressLine2();
		if (addressLine2 == null) {
			addressLine2 = "";
		} else {
			if (addressLine1.equals("")) {
				addr += " " + addressLine2;
			} else if (buildingName.equals("") && unitNumber.equals("")
					&& streetNumber.equals("") && addressLine1.equals("")) {
				addr += "" + addressLine2;
			} else {
				addr += ", " + addressLine2;
			}
		}

		String addressLine3 = pickupFrom.getAddressLine3();
		if (addressLine3 == null) {
			addressLine3 = "";
		}
		if (addressLine2.equals("")) {
			addr += " " + addressLine3;
		} else if (buildingName.equals("") && unitNumber.equals("")
				&& streetNumber.equals("") && addressLine1.equals("")
				&& addressLine2.equals("")) {
			addr += "" + addressLine3;
		} else {
			addr += ", " + addressLine3;
		}

		String suburb = pickupFrom.getSuburb();
		if (suburb == null) {
			suburb = "";
		} else {
			if (addressLine1.equals("") && addressLine2.equals("")
					&& addressLine3.equalsIgnoreCase("")
					&& unitNumber.equals("") && streetNumber.equals("")) {
				addr += "" + suburb;
			} else if (addressLine1.equals("") || addressLine2.equals("")
					|| addressLine3.equals("") || unitNumber.equals("")
					|| streetNumber.equals("")) {
				addr += ", " + suburb;
			} else {
				addr += ", " + suburb;
			}
		}
		return addr;
	}

	private String getShortAddress(IGAddress igAddress) {
		String addr = "";
		int streetNumberLength = 0;
		String reverseMaskedString = "";
		String maskedStreetNumber = "";
		String streetNumber = igAddress.getStreetNumber();

		int i = 0;
		if (streetNumber != null) {
			streetNumberLength = streetNumber.length();
			i = streetNumberLength - 1;
		} else {
			streetNumberLength = 0;
		}
		if (streetNumberLength != 0) {
			if (streetNumberLength == 1) {
				if (Character.isDigit(streetNumber.charAt(0))) {
					maskedStreetNumber = "*";
				} else {
					maskedStreetNumber += streetNumber.charAt(0);
				}
			} else {
				while (i >= 1) {

					if (Character.isDigit(streetNumber.charAt(i))) {
						try {
							if (Character.isDigit(streetNumber.charAt(i - 1))) {
								reverseMaskedString += "*";
							} else if (Character.isDigit(streetNumber
									.charAt(i + 1))) {
								reverseMaskedString += streetNumber.charAt(i);
							} else {
								reverseMaskedString += "*";
							}
						} catch (StringIndexOutOfBoundsException e) {
							reverseMaskedString += "*";

						}

					} else {
						reverseMaskedString += streetNumber.charAt(i);
					}
					i--;
				}
				reverseMaskedString += streetNumber.charAt(0);
				Log.i("reverse masked number", "" + reverseMaskedString);
				maskedStreetNumber = new StringBuffer(reverseMaskedString)
						.reverse().toString();
			}
		}

		if (streetNumber == null) {
			maskedStreetNumber = "";
		} else {
			addr += maskedStreetNumber;
		}
		String addressLine1 = igAddress.getAddressLine1();
		if (addressLine1 == null) {
			addressLine1 = "";
		} else {
			if (addressLine1.equalsIgnoreCase("")) {
				addr += addressLine1;
			} else {
				addr += " " + addressLine1;
			}
		}

		String suburb = igAddress.getSuburb();
		if (suburb == null) {
			suburb = "";
		} else {
			if (addressLine1.equals("")) {
				addr += "" + suburb;
			} else {
				addr += ", " + suburb;
			}
		}
		return addr;
	}

	/**
	 * Given jobs are filtered and active jobs are returned.
	 * 
	 * @param jobs
	 * @return Jobs which are not cancelled or ignored or completed
	 */
	public static ArrayList<IGJob> getActiveJobs(ArrayList<IGJob> jobs) {

		ArrayList<IGJob> activeJobs = new ArrayList<IGJob>();

		int count = jobs.size();
		for (int i = 0; i < count; i++) {
			IGJob job = jobs.get(i);
			if (job.getStatus() != IGConstants.kJobCancelled
					&& job.getStatus() != IGConstants.kJobIgnored
					&& job.getStatus() != IGConstants.kJobCompleted) {
				activeJobs.add(job);
			} else {
				IGChatService.delete_chatHistory(Integer.parseInt(job.getId()));
			}
		}
		return activeJobs;
	}

	/**
	 * Check whether the latest job arrived is still available.
	 * 
	 * @param newJobs
	 * @return
	 */
	public static Boolean isLatestJobAvailable(ArrayList<IGJob> newJobs) {
		if (newJobs!=null) {
			for (IGJob igJob : newJobs) {
				if (igJob.getId().equals(latestJobID)) {
					return true;
				}
			}
		}

		return false;
	}

	public static Boolean syncJobs(ArrayList<IGJob> newJobs,
			ArrayList<IGJob> jobs) {

		Boolean newJobFound = false;
		int count = newJobs.size();
		for (int i = 0; i < count; i++) {

			IGJob newJob = newJobs.get(i);

			if (jobs.contains(newJob)) {

				IGJob job = jobs.get(jobs.indexOf(newJob));
				newJob.setStatus(job.getStatus());
				newJob.setNewJob(job.isNewJob());
			} else {
				newJob.setNewJob(true);
				newJobFound = true;
				setLatestJobID(newJob.getId());
				IGChatService.delete_chatHistory(Integer.parseInt(newJob
						.getId()));

			}

		}

		return newJobFound;
	}
}
