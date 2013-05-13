/*
 * Package Name : com.ingogo.android.app
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : A set of API specific constants that will be used through by the web service classes and their callback implementations.
 */

package com.ingogo.android.app;

public interface IGApiConstants {
	public static final String kErrorMsgKey = "Error";
	public static final String kExceptionKey = "EXCEPTION";

	// Request Time out param
	public static final int kRequestTimeOutInMills = 50000;

	// Api ID's
	public static final int kLoginWebServiceId = 0;
	public static final int kJobsWebServiceId = 1;
	public static final int kSupportWebServiceId = 2;
	public static final int kAcceptJobWebServiceId = 3;
	public static final int kCancelJobWebServiceId = 4;
	public static final int kAvailableWebServiceId = 5;
	public static final int kBusyWebServiceId = 6;
	public static final int kCollectedJobWebServiceId = 7;
	public static final int kCompletedJobWebServiceId = 8;
	public static final int kSendMessageWebServiceId = 9;
	public static final int kIncomingMessageWebServiceId = 10;
	public static final int kUpdateCurrentPositionWebServiceId = 11;
	public static final int kForgotPasswordWebServiceId = 12;
	public static final int kResetPasswordWebServiceId = 13;
	public static final int kLogoutWebServiceId = 14;
	public static final int kNoShowWebServiceId = 15;
	public static final int kPaymentWebServiceId = 16;
	public static final int kVersionWebServiceId = 17;
	public static final int kCreditDetailsWebServiceId = 18;
	public static final int kFindCurrentDriverStateWebServiceId = 19;
	public static final int kCompleteOfflineWebServiceId = 20;
	public static final int kAccountInfoWebServiceId = 21;
	public static final int kUpdateAccountWebServiceId = 22;
	public static final int kTimeToPickUpWebServiceId = 23;
	public static final int kCancellationReasonWebServiceId = 24;
	public static final int kVerifyPwdWebServiceId = 25;

	// API Prefixes
	// public static final String kIngogoBaseURL =
	// "http://www.ingogo.mobi:8080/presentation/service/"; //live server
	// public static final String kIngogoBaseURL =
	// "http://ingogoint01.idigfree.com:8081/presentation/service/";
	// //Integration server
	// public static final String kIngogoBaseURL =
	// "http://ingogosys01.idigfree.com:8083/presentation/service/"; //System
	// Test server
	// public static final String kIngogoBaseURL =
	// "http://ingogouat01.idigfree.com:8085/presentation/service/";//UAT
	// public static final String kIngogoBaseURL =
	// "https://www.ingogo.mobi/presentation/service/";//secure server

	// Local Servers
	// public static final String kIngogoBaseURL =
	// "http://10.1.3.65:9090/presentation/service/";
	// public static final String kIngogoBaseURL =
	// "http://10.1.3.65:8200/presentation/service/";
	// public static final String kIngogoBaseURL =
	// "http://50.18.104.74:8016/presentation/service/"; //local server
	// public static final String kIngogoBaseURL =
	// "http://10.1.3.65:8200/presentation/service/";
	// public static final String kIngogoBaseURL =
	// "http://10.1.3.114:8100/presentation/service/"; //local server
	// public static final String kIngogoBaseURL =
	// "http://10.1.3.75:8180/presentation/service/"; //local server
	// public static final String kIngogoBaseURL =
	// "http://10.1.3.75:8080/presentation/service/"; //local server
	// public static final String kIngogoBaseURL =
	// "http://10.1.3.114:8080/presentation/service/"; //local server
	// public static final String kIngogoBaseURL =
	// "http://10.1.3.114:8080/presentation/service/"; //local server
	// public static final String kIngogoBaseURL =
   //     public static final String kIngogoBaseURL = "http://50.18.104.74:8075/presentation/service/"; //local serve
  //public static final String kIngogoBaseURL = "http://50.18.104.74:8075/presentation/service/"; //local server
        
        //public static final String kIngogoBaseURL = "http://50.18.104.74:8085/presentation/service/"; //local serve
      // public static final String kIngogoBaseURL = "http://50.18.104.74:8020/presentation/service/"; //local server
	// "http://10.1.3.150:8080/presentation/service/"; //local server
	// public static final String kIngogoBaseURL =
	// "http://10.1.3.150:8088/presentation/service/"; //local server
	// public static final String kIngogoBaseURL =
	// "http://10.1.3.114:8010/presentation/service/"; //local server
	// public static final String kIngogoBaseURL =
	// "http://10.1.3.130:8080/presentation/service/"; //local server
	// public static final String kIngogoBaseURL =
	// "http://10.1.3.39:8080/presentation/service/"; //local server
	// public static final String kIngogoBaseURL =
	// "http://50.18.104.74:8085/presentation/service/";
	// public static final String kIngogoBaseURL =
	// "http://50.18.104.74:8030/presentation/service/";
	// public static final String kIngogoBaseURL =
	// "http://50.18.104.74:8075/presentation/service/"; //local server
	//public static final String kIngogoBaseURL = "http://50.18.104.74:8075/presentation/service/"; // local
																									// serve
	 public static final String kIngogoBaseURL =
	 "http://50.18.104.74:8025/presentation/service/"; //local serve
	// API PostFixes

	public static final String kLoginApiURL = "authentication/driverLogin/";
	public static final String kJobsApiURL = "bookings/available/";
	public static final String kSupportApiURL = "drivers/helpSupport/";
	public static final String kAcceptJobApiURL = "bookings/accept/";
	public static final String kCancelJobApiURL = "bookings/driverCancels/";
	public static final String kAvailableApiURL = "drivers/available/";
	public static final String kBusyApiURL = "drivers/unavailable/";
	public static final String kCollectedJobApiURL = "bookings/collect/";
	public static final String kCreditDetailsApiURL = "bookings/getAvailableCredit/";
	public static final String kCompletedJobApiURL = "bookings/complete/";
	public static final String kSendMessageApiURL = "bookings/driverSendMessage/";
	public static final String kIncomingMessageApiURL = "bookings/driverIncomingMessage/";
	public static final String kUpdateCurrentPositionApiURL = "drivers/currentPosition/";
	public static final String kForgotPasswordApiURL = "authentication/driverForgotPassword/";
	public static final String kResetPasswordApiURL = "authentication/resetDriverAccount/";
	public static final String kLogoutApiURL = "authentication/logout/";
	public static final String kNoShowApiURL = "bookings/noShow/";
	public static final String KPaymentApiURL = "bookings/recordPaymentDetails/";
	public static final String kVersionApiURL = "applications/upgradeAvailable/";
	public static final String kFindCurrentDriverStateApiURL = "bookings/findCurrentDriverState/";
	public static final String kCompleteOfflineApiURL = "bookings/completeOffline/";
        public static final String kAccountInfoApiURL = "accountInfo/driverAccountInfo";
        public static final String kUpdateAccountApiURL = "accountInfo/driverUpdateAccountInfo";
	public static final String kTimeToPickUpApiURL = "bookings/timeToPickup";
	public static final String kCancellationReasonApiURL = "bookings/cancellationReasons";
	public static final String kverifyPwdApiURL = "authentication/passwordVerification";
	public static final String kPaymentSummaryApiUrl = "payments/rollingSummary";
	public static final String kPaymentDetailApiUrl = "payments/paymentDetail";
	public static final String kPaymentSettledDailySummaryApiUrl = "payments/paymentSummary";
	public static final String kPaymentPendingDailySummaryApiUrl = "payments/unsettledSummary";
	public static final String kDriverLocalityApiUrl = "drivers/findLocality";
	public static final String kJobDetailsApiUrl = "bookings/jobDetails";
	public static final String kTakePaymentUrl = "drivers/canTakePayment";
	public static final String kSwipeInitialiseApiUrl = "bookings/swipeInitialise";
	public static final String kFindAccount = "passengers/find";
	public static final String kFindAccountUrl = "passengers/find";
	public static final String kCreateBookingForPaymentUrl = "bookings/createBookingForTakingPayment";
	public static final String kProcessPaymentUrl = "bookings/processPayment";
	public static final String kSendReceiptUrl = "bookings/sendReceipt";
	public static final String kInitialiseUnknownPassengerPaymentUrl = "bookings/initialiseUnknownPassengerPayment";
	public static final String kRetrieveContactInfoUrl = "drivers/retrieveContactInfo";
	public static final String kMaintainContactInfoUrl = "drivers/maintainContactInfo";
	public static final String kPrintReceiptUrl = "bookings/printReceipt";
	public static final String kPrinterConfigUrl = "drivers/initialisePrinterConfig";
	public static final String kReprintLastReceiptUrl = "bookings/reprintLastReceipt";
	public static final String kMapInfoUrl = "bookings/mapInfo";
	public static final String kTargetProgressUrl = "drivers/targetProgress";
	public static final String kReferralInformationUrl = "drivers/referralInformation";
	public static final String kReconnectAttemptedUrl = "drivers/reconnectAttempted";
	public static final String kRegisterIssueUrl = "drivers/registerIssue";
	public static final String kIssueReasonsUrl = "drivers/issueReasons";
	public static final String kProcessPaymentForUnknownPassengerUrl = "bookings/processPaymentForUnknownPassenger";
	public static final String kGetPaidAtUrl = "drivers/getPaidAt/";
        public static final String kLoadAndGoAccountsUrl = "accountInfo/viewLoadAndGoAccounts";
	public static final String kSendDiagnostics = "drivers/sendDiagnostics";

	// HTTP Status Code Constants
	public static final String kHttpStatusKey = "HttpStatus";
	public static final int kHttpStatusOK = 200;
	public static final int kHttpStatusForbidden = 401;
	public static final int kHttpStatusBadRequest = 400;

	public static final String kStatusOK = "OK";
	public static final String kStatusError = "FAILED";
	public static final String kStatusSystemError = "SYSTEM_ERROR";
	public static final String kStatusKey = "responseCode";
	public static final String kAuthHeaderKey = "Authorization";

	// JSON Request Constants
	public static final String kJSONUsernameKey = "mobileNumber";
	public static final String kJSONPasswordKey = "password";
	public static final String kJSONBookingIdKey = "bookingId";
	public static final String kJSONMessageKey = "content";
	public static final String kJSONLongitudeKey = "longitude";
	public static final String kJSONLatitudeKey = "latitude";
	public static final String kJSONPassphraseKey = "passphrase";
	public static final String kJSONTotalFareKey = "totalFare";
	public static final String kJSONBookingCreditKey = "bookingCredit";
	public static final String kJSONBidCreditKey = "bidCredit";
	public static final String kJSONFareKey = "fare";
	public static final String kJSONBidAmountKey = "bidAmount";
	public static final String kJSONLicenceNumberKey = "licenseNumber";
	public static final String kJSONAccountNameKey = "accountName";
	public static final String kJSONAccountNumberKey = "accountNumber";
	public static final String kJSONBSBKey = "bsb";
	public static final String kJSONAccountInfoKey = "bankAccountInformation";
	public static final String kJSONCompanyNameKey = "companyName";
	public static final String kJSONABNKey = "abn";
	public static final String kJSONTimeToPickUpKey = "timeToPickup";
	public static final String kJSONMessageTypeKey = "messageType";
	public static final String kDirectText = "direct";
	public static final String kDropOffOnRouteText = "dropoffOnRoute";
	public static final String kCancellationReasonKey = "cancellationReason";
	public static final String kDevice = "device";
	public static final String kAndroidDevice = "android";
	public static final String kCardId = "cardId";
	public static final String kTokenKey = "token";
	public static final String kConfCode = "confCode";
	public static final String kBidInterval = "bidInterval";
	public static final String kBidExtra = "bidExtra";
	public static final String kBooking = "booking";
	public static final String kErrorRecoveryKey = "errorRecovery";
	public static final String kPaymentMethod = "paymentMethod";
	public static final String kJSONBankAccountIDKey = "bankAccountId";

	// JSON Response Constants
	public static final String kAcceptKey = "accepted";
	public static final String kCollectKey = "collected";
	public static final String kCompleteKey = "completed";
	public static final String kPaymentDue = "paymentDue";
	public static final String kConfirmKey = "confirmed";

	public static final String kBookingSummary = "bookingSummary";
	public static final String kBookingSummaries = "bookingSummaries";

	public static final String kResponseMessages = "responseMessages";
	public static final String kErrorMessages = "errorMessages";
	// Passenger cancelled booking
	public static final String kPassengerCancelledJob = "cancelled";
	public static final String kBookingStatusKey = "bookingStatus";
	public static final String kBookingTypeKey = "bookingType";
	public static final String kHasCardRegisteredKey = "hasRegisteredCard";
	public static final String kPassengerNotConfirmedJob = "refuted";
	public static final String kPassengerDispatched = "dispatched";
	// Version API
	public static final String kVersionNumber = "versionNumber";
	public static final String kAppVersion = "appVersion";
	public static final String kAppUrl = "appUrl";
	public static final String kRetryAfter = "retryAfter";

	// Support specific API Constants
	public static final String kSupport = "driverSupportDetails";

	public static final String kEmailPrompt = "driverEmailShortName";
	public static final String kEmailInfo = "driverEmailDetail";

	public static final String kSmsPrompt = "driverSmsShortName";
	public static final String kSmsInfo = "driverSmsDetail";

	public static final String kPhonePrompt = "driverPhoneShortName";
	public static final String kPhoneInfo = "driverPhoneDetail";
	public static final String kStaleState = "Stale";
	public static final String kHasChanged = "hasChanged";

	/*----------------------------------------------------------------------------
	 * JSON CONSTANTS
	 * ---------------------------------------------------------------------------
	 */

	// Network Constants

	public static final String kNetworkErrorExceptionKey = "101";
	public static final String kTimeOutErrorExceptionKey = "102";
	public static final String kInternalServerErrorExceptionKey = "103";
	public static final int kNetworkErrorExceptionResponseKey = 101;
	public static final int kTimeOutErrorExceptionResponseKey = 102;
	public static final int kInternalServerExceptionResponseKey = 103;
	public static final String kAuthenticationErrorExceptioney = "104";
	public static final String kSuccessMsgKey = "OK";
	public static final String kNetworkError = "Failed";
	public static final String kApiError = "API Error";
	public static final String kApiSuccessfulMsgKey = "success";
	public static final String kApiFailedMsgKey = "failed";
	public static final String kResponseBeanKey = "responseBean";
	public static final String KResponseMessagesKey = "responseMessages";

	public static final String KPassengerNameKey = "passengerName";
	public static final String KPassengerMobileNumberKey = "passengerMobileNumber";

	public static final String kNetworkScriptResultError = "error:";
	public static final String kNetworkScriptResultConfirmed = "confirmed:";

	public static final String kNetworkErrorMessage = "No Internet connection detected. Please try again later.";
	public static final String kNetworkTimeoutErrorMessage = "You've lost ingogo. Please try again later.";
	public static final String kInternalServerErrorMessage = "Internal Server Error. Please try again later.";
	public static final String kAuthenticationErrorMessage = "Authentication Error. Please try again later.";

	// Payment
	public static final String kCardDetails = "cardDetails";
	public static final String kCreditPercentage = "creditPercentage";
	public static final String kDefault = "isDefault";

	public static final String kminTotalDueValue = "minTotalDueValue";
	public static final String kconfirmationValue = "confirmationValue";
	public static final String kmaxTotalDueValue = "maxTotalDueValue";

	// Payment Daily History
	public static final String kPaymentStatus = "Settled";

	// Payment failure
	public static final String kIsSwipeFailure = "swipeFailure";

}