package com.ingogo.android.webservices.beans.response;

import java.io.Serializable;
import java.util.ArrayList;

import com.ingogo.android.model.IGEarningTargetModel;
import com.ingogo.android.model.IGJobTargetModel;

public class IGTargetProgressResponseBean extends IGBaseResponseBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3057501071051687655L;
	
	ArrayList<IGJobTargetModel> jobsTargets = new ArrayList<IGJobTargetModel>();
	ArrayList<IGEarningTargetModel> earningsTarget = new ArrayList<IGEarningTargetModel>();
	int jobsCompletedThisMonth;
	float paymentsThisMonth;
	String currentCommissionPercentage;
	int paymentsDisplayPercentile;
	int jobsDisplayPercentile;
	
	
	public ArrayList<IGJobTargetModel> getJobsTargets() {
		return jobsTargets;
	}
	public void setJobsTargets(ArrayList<IGJobTargetModel> jobsTargets) {
		this.jobsTargets = jobsTargets;
	}
	public ArrayList<IGEarningTargetModel> getEarningsTarget() {
		return earningsTarget;
	}
	public void setEarningsTarget(ArrayList<IGEarningTargetModel> earningsTarget) {
		this.earningsTarget = earningsTarget;
	}
	public int getJobsCompletedThisMonth() {
		return jobsCompletedThisMonth;
	}
	public void setJobsCompletedThisMonth(int jobsCompletedThisMonth) {
		this.jobsCompletedThisMonth = jobsCompletedThisMonth;
	}
	public String getCurrentCommissionPercentage() {
		return currentCommissionPercentage;
	}
	public void setCurrentCommissionPercentage(String currentCommissionPercentage) {
		this.currentCommissionPercentage = currentCommissionPercentage;
	}
	public int getPaymentsDisplayPercentile() {
		return paymentsDisplayPercentile;
	}
	public void setPaymentsDisplayPercentile(int paymentsDisplayPercentile) {
		this.paymentsDisplayPercentile = paymentsDisplayPercentile;
	}
	public int getJobsDisplayPercentile() {
		return jobsDisplayPercentile;
	}
	public void setJobsDisplayPercentile(int jobsDisplayPercentile) {
		this.jobsDisplayPercentile = jobsDisplayPercentile;
	}
	public float getPaymentsThisMonth() {
		return paymentsThisMonth;
	}
	public void setPaymentsThisMonth(float paymentsThisMonth) {
		this.paymentsThisMonth = paymentsThisMonth;
	}
	
}
