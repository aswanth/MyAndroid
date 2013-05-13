package com.ingogo.android.webservices.beans.response;

import java.util.List;

import com.ingogo.android.model.IGCommissionDetails;
import com.ingogo.android.model.IGSampleCalculation;

/**
 * 
 * @author suslov
 *
 */
public class IGReferralInformationResponseBean extends IGBaseResponseBean {
	private float referralCommissionPercentage;
    private int referralCommissionAppliesFor;
    private List< IGCommissionDetails > commissionDetails;
    private List< IGSampleCalculation > sampleCalculations;
    private long referralDate;
    private long referralValidUntilDate;
    private int signupPeriod;
    private float referralPercentage;
    private int referralPercentageAppliesFor;

    public List< IGCommissionDetails > getCommissionDetails() {
		return commissionDetails;
	}
	public void setCommissionDetails(List< IGCommissionDetails > commissionDetails) {
		this.commissionDetails = commissionDetails;
	}

	public long getReferralDate() {
		return referralDate;
	}
	public void setReferralDate(long referralDate) {
		this.referralDate = referralDate;
	}

	public List< IGSampleCalculation > getSampleCalculations() {
		return sampleCalculations;
	}
	public void setSampleCalculations(List< IGSampleCalculation > sampleCalculations) {
		this.sampleCalculations = sampleCalculations;
	}
	public float getReferralCommissionPercentage() {
		return referralCommissionPercentage;
	}
	public void setReferralCommissionPercentage(float referralCommissionPercentage) {
		this.referralCommissionPercentage = referralCommissionPercentage;
	}
	public int getReferralCommissionAppliesFor() {
		return referralCommissionAppliesFor;
	}
	public void setReferralCommissionAppliesFor(int referralCommissionAppliesFor) {
		this.referralCommissionAppliesFor = referralCommissionAppliesFor;
	}
	public int getSignupPeriod() {
		return signupPeriod;
	}
	public void setSignupPeriod(int signupPeriod) {
		this.signupPeriod = signupPeriod;
	}
	public long getReferralValidUntilDate() {
		return referralValidUntilDate;
	}
	public void setReferralValidUntilDate(long referralValidUntilDate) {
		this.referralValidUntilDate = referralValidUntilDate;
	}
	public int getReferralPercentageAppliesFor() {
		return referralPercentageAppliesFor;
	}
	public void setReferralPercentageAppliesFor(int referralPercentageAppliesFor) {
		this.referralPercentageAppliesFor = referralPercentageAppliesFor;
	}
	public float getReferralPercentage() {
		return referralPercentage;
	}
	public void setReferralPercentage(float referralPercentage) {
		this.referralPercentage = referralPercentage;
	}

}
