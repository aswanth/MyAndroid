/*
  Package Name : com.ingogo.android.app
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : A set of constants that will be used through out the application.
 */

package com.ingogo.android.app;

public interface IGConstants {
	public static final String GPSService = "com.ingogo.android.utilities.IGGPSService";

	public static final String kDriverNameKey = "driverName";
	public static final String kUsernameKey = "mobileNumber";
	public static final String kPasswordKey = "password";
	public static final String kRemainderKey = "remainderInterval";
	public static final String kLicenseNumberKey = "licenseNumber";

	public static final String kAccessToken = "personId";
	public static final String kbroadcastPositionInterval = "broadcastPositionInterval";
	public static final String kPollJobsInterval = "pollBookingsInterval";
	public static final String kStaleTime = "staleTime";
	public static final String kPinRetriesMaxLimit = "pinRetries";
	public static final String kConnectionTimeOut = "webServiceTimeout";
	
	public static final String kDataKey = "data";
	public static final String kPreviousJobsKey = "previousJobs";
	public static final String kJobId = "bookingId";
	public static final String kUpdatedVersion = "Updated_Version";
	public static final String kUpdatedTime = "Updated_Time";
	public static final String kPreviousJobMessageKey = "previousJobMessage";
	public static final String kPreviousUserId = "previousUserId";
	// Job specific API Constants
	public static final String kpickupFrom = "pickupFrom";
	public static final String kDropOffTo = "dropOffAt";
	public static final String kTimeStamp = "booked";
	public static final String kPlateNumber = "plateNumber";
	public static final String kBookingTime = "bookingTime";
	public static final String kMessage = "message";
	public static final String kAuthErrorCount = "authenticationErrorCount";

	public static final String kStreetName = "streetName";
	// public static final String kStreetNo = "streetNo";
	// public static final String kBuildingName = "buildingName";
	
	public static final String kBookingTypeHail = "HAIL";
	public static final String kBookingTypeBooking = "BOOKING";

	public static final String kAddressLine1 = "addressLine1";
	public static final String kAddressLine2 = "addressLine2";
	public static final String kAddressLine3 = "addressLine3";
	public static final String kBuildingName = "buildingName";
    public static final String kNickName = "nickname";
	public static final String kSuburb = "suburb";
	public static final String kUnitNumber = "unitNumber";
	public static final String kState = "state";
	public static final String kStreetNumber = "streetNumber";

	public static final String kExtraPay = "bidExtra";
	public static final String ktimeWithin = "bidInterval";
	public static final String kPassengerName = "passengerName";
	public static final String kDriverIsAvailable = "driverIsAvailable";

	// Shared preference Constants
	public static final String kSharedPreference = "IngogoPreference";
	public static final String kLoggedIn = "LoggedIn";
	public static final String kSavingsPercentage = "SavingsPercentage";

	// Job Details Api Constants
	public static final int kJobOpen = 1;
	public static final int kJobAccepted = 2;
	public static final int kJobCompleted = 3;
	public static final int kJobCancelled = 4;
	public static final int kJobCollected = 5;
	public static final int kJobPassengerNoShow = 6;

	// Driver status Constants
	public static final int kAvailable = 1;
	public static final int kBusy = -1;

	// Job Ignore Constant
	public static final int kJobIgnored = 0;
	public static final String kStatus = "status";

	// Message constants
	public static final String kIncomingMessageKey = "messages";
	public static final String kMessageStatus = "messagestatus";
	public static final int kMessageSuccess = 1;
	public static final int kMessageFailed = -1;
	public static final int kIncomingMessagePollingInterval = 10000;
	public static final int kAvailableJobsPollingInterval = 10000;
	public static final String kUpdatePositionKey = "currentpositionstatus";
	// Login Constants
	public static final String defaultUserId = "0421333333";
	public static final String defaultPassword = "password";
	public static final String driverOffline = "offline";

	// Forgot Password Constants
	public static final String kPassphrase = "key";
	public static final String kUserId = "mobile";
	public static final String kLaunchType = "type";
	public static final String kForgotPasswordType = "forgotPassword";

	// ChatHistory limit constants
	public static final int kChatHistoryLimit = 25;

	// Job completed constants
	public static final String kBooking = "booking";
	public static final String kPickUpFrom = "pickupFrom";
	public static final String kDropOffAt = "dropOffAt";
	public static final String kBalance = "balance";
	public static final String kMobileNumber = "passengerMobileNumber";
	public static final String kName = "passengerName";
	public static final String kBookingFee = "bookingFee";
	public static final String kBidExtra = "bidExtra";
	public static final String kApplyBid = "applyBid";
	public static final String kFareEntered = "fareEntered";
	public static final String kPaymentDue = "paymentDue";
	public static final String kPaymentDetails = "paymentDetails";
	public static final String kBookingType = "bookingType";
	public static final String kFareDetails = "faredetails";

	public static final String kDriverAvailability = "driverStatus";
	public static final String kDriverStatus = "driverCurrentstatus";
	public static final String kDriverAvailable = "available";
	public static final String kDriverUnAvailable = "unavailable";
	public static final int NotificationID = 222222111;
	public static final String zeroBalance = "00.00";
	public static final String priceFormat = "0.00";


	public static final String kJobInProgress = "jobInProgress";
	public static final String kTrue = "true";

	// Base activity constants
	public static final String kJobsActivityName = ".activities.IGJobsActivity";
	public static final String kAccountInfoActivityName = ".activities.IGAccountInfoActivity";
	public static final String kHelpActivityName = ".activities.IGHelpActivity";
	public static final String kPaymentActivityName = ".activities.IGPaymentActivity";
	public static final String kJobDetailsActivityName = ".activities.IGJobDetailsActivity";
	public static final String kCompleteJobActivityName = ".activities.IGCompleteJobActivity";
	public static final String kPaymentHistoryActivityName = ".activities.payments.IGPaymentHistorySummaryActivity";
	public static final String kPaymentDailyHistoryActivityName = ".activities.payments.IGPaymentDailySummaryActivity";
	public static final String kPaymentDetailsActivityName = ".activities.payments.IGPaymentHistoryDetailActivity";
	public static final String kFindPassengerActivityName = ".activities.payments.IGFindPassengerActivity";
	public static final String kSwipeCalculatorActivityName = ".activities.payments.IGSwipeCalculatorActivity";
	public static final String kSwipeCardActivityName = ".activities.payments.IGSwipeCardActivity";
	public static final String kSwipePracticeActivityName = ".activities.payments.IGSwipePracticeActivity";
	public static final String kPaymentsSwipeActivityName = ".activities.payments.IGPaymentsSwipeActivity";
	public static final String kAddressDetailsActivityName = ".activities.IGAddressDetailsActivity";
	public static final String kPrinterConfigActivityName = ".activities.IGPrinterConfigActivity";
	public static final String kPrintReferralActivityName = ".activities.IGPrintReferralActivity";
	public static final String kDriversMapActivityName = ".activities.IGDriversMapActivity";
	public static final String kCashReceiptActivityName=".activities.IGCashReceiptActivity";
	public static final String kAccountInfoRecordedOrNonRecordedActivityName = ".activities.IGAccountInfoRecordedOrNonRecordedActivity";
	public static final String kAccountInfoRecordedListActivityName = ".activities.IGAccountInfoRecordedListActivity";
	public static final String kSplashActivityName = ".activities.IGSplashActivity";
	public static final String kSignInActivityName = ".activities.IGSignupActivity";

	public static final String kJob = "Job";
	public static final String kNull = "null";
	public static final String kInformationMessages = "informationMessages";
	public static final String CodeHeader = "Code: ";
	public static final String kCode = "code";
	public static final String ContentHeader = "Content: ";
	public static final String kInfo = "Info";

	public static final String kUpgrade = "Upgrade";
	public static final String kDownloadUpdate = "Downloading updates";
	public static final String kCheckForUpdate = "Checking for updates";
	public static final String kPaymentFailurePage = "paymentfailure";

	public static final String OKMessage = "OK";
	public static final String CancelMessage = "Cancel";
	public static final String ConfirmMessage = "Confirm";

	// Time to dismiss network error alert and redirect to login page

	public static int logoutDelay = 300000;

	// Cancellation Reason constants
	public static final String kReasonKey = "reasons";

	// Menu constants
	public static final int GROUP_ZERO = 0;
	public static final int ORDER_NONE = 0;

	// Payment Calculator
	public static final String kPaymentOffline = "Payment completed";

	// Payment History Detail Constants
	public static final String kBookingId = "bookingid";
	// Payment History Summaries Constants
	public static final String kPaymentHistorySummaries = "summaries";
	public static final String kPaymentAmount = "amount";
	public static final String kPaymentId = "paymentId";
	public static final String kPaymentStatus = "status";
	public static final String kPaymentDate = "settled";

	// Payment history extras
	public static final String kPaymentHistoryItem = "paymentitem";
	public static final String kPending = "Pending";
	public static final String kPaymentStatusSettled = "SETTLED";
	public static final String kPaymentStatusPaid = "PAID";

	// payment confirmation constants
	public static final String kReceiptKey = "receipt";
	public static final String kPaymentSuccess = "success";
	
	// payment offline constant
	public static final String kReceiptInformationKey = "receiptInformation";
	public static final String kTotalPaidKey = "totalPaid";

	public static final String kPinRetryMessage = "Invalid PIN. Try again..";
	public static final String kCardNumber = "CardToken";
	public static final String kPinNumber = "pinnumber";
	public static final String kErrorMessage = "errormessage";
	public static final String kErrorCode = "errorcode";
	public static final String kInvalidPinRetry = "invalidtry";
	public static final String kCreditCardCount = "creditCardCount";
	public static final String kPaymentMethod = "paymentMethod";
	public static final String kbaseFee = "baseFee";
	public static final String kServiceFee = "serviceFee";
	public static final String kTripCharge = "tripCharge";
	public static final String kTotalDueAmount = "amountIncAllCharges";

	public static final String kPinErrorMessage = "Invalid payment code";
	public static final String kPinMaxRetryMessage = "Invalid payment code, attempts exceeded";
	public static final String kCardErrorMessage = "Card error";
	public static final String kConnectionLostMessage = "Connection lost";
	public static final String kCreditLimitExceededMessage = "Credit limit exceeded";
	public static final String kCardExpiredMessage = "Card expired";
	public static final String kUnspecifiedFailureMessage = "UNSPECIFIED_FAILURE";
	public static final String kPassengerNotCreatedConfirmationCode = "Passenger has not created a 'Confirmation Code'";
	public static final String kPickupRefutedErrorMessage = "Pickup Refuted - can not take payment.";
	
	// Payment daily history extras
	public static final String kPaymentDailyHistoryItem = "bookingId";
	public static final String kCompletedOffline = "COMPLETED_OFFLINE";
	public static final String kCarriedForward = "*carried forward";

	public static final String kDetails = "details";
	public static final String kJobDetails = "jobDetails";
	
	//Calculator constants
	
	public static final String kPassengerID = "passengerid";
	public static final String kMinimumDue = "minimumDue";

	public static final String kMaximumDue = "maximumDue";
	public static final String kConfirmationValue = "confirmValue";
	public static final String kCreditBalance = "creditBalance";
	public static final String kCreditPercentage = "CreditPercentage";
	
	public static final String kMinTotalDueValue = "minTotalDueValue";

	public static final String kMaxTotalDueValue = "maxTotalDueValue";
	public static final String kconfirmationValue = "confirmationValue";
	public static final String kcreditPercentage = "creditPercentage";

	public interface paymentErrorTypes {
		public static final int kPinError = 0;
		public static final int kCardError = 1;
		public static final int kConnectionLost = 2;
		public static final int kCreditLimitExceeded = 3;
		public static final int kCardExpiry = 4;
		public static final int kUnspecifiedFailure = 5;
		public static final int kPickupRefuted = 5;
	}
	public interface badSwipeErrorTypes {
		public static final int SwipeError_Success = 0;
		public static final int SwipeError_Parity = 1;
		public static final int SwipeError_Lrc = 2;
		public static final int SwipeError_NoPeaks = 3;
		public static final int SwipeError_NotEnoughBits = 4;
		public static final int SwipeError_Malloc = 5;
		public static final int SwipeError_IndeterminableTrackType = 6;
		public static final int SwipeError_TooManyChars = 7;
		public static final int SwipeError_List = 8;
	} 

	// File Name

	public static final String kMobileNumberFile = "mobileNumber";
	public static final String kLicenseNumberFile = "licenseNumber";
	public static final String kSupportedPrefixes = "supportedPrefixes";
	public static final String kMaskedPrefixes = "maskedPrefixes";
	public static final String kChatFailures = ":failures";

	public static final String kErrorHeading = "errorheading";
	
	//printReceipt
	
	public static final String ABN = "27 152 473 482";
	public static final String kDeviceName = "deviceName";
	public static final String kDevicePin = "devicePin";
	public static final String KPrintDeviceName = "printDeviceName";
	
	//Map
	public static final String kMapInfo = "mapInfo";
	public static final String kPickupLat = "pickupLat";
	public static final String kPickupLong = "pickupLong";
	public static final String kPickupAddress = "pickupAddress";
	public static final String kPassengerNum = "passengerNum";

	public static final String kTargetProgress = "targetProgress";
	
	public static final String kPassengerStatus = "passengerStatus";
	public static final String kPassengerStatusNew = "NEW";
	public static final String kPassengerStatusVip = "VIP";
	public static final String kPassengerStatusBiz = "BIZ";
	
	public static final String kLocalitySydney = "sydney";
	public static final String kLocalityMelbourne = "melbourne";
	public static final String kLocalityTvm = "tvm";
	public static final String kLocalityAdelaide = "adelaide";
	public static final String kLocalityBrisbane = "brisbane";
	public static final String kLocalityCanbera = "canberra";
	public static final String kLocalityNewCastle = "newcastle";
	public static final String kLocalityPerth = "perth";
	public static final String kLocalityHobart = "hobart";
	
	public static final String KSUBURBLIST = "suburb_list_";

	public static final String isBackButtonEnabled = "backbutton";

	public static final String disableBackButton = "disablebackbutton";
	
	public static final String KSuburbName = "suburb";
	public static final String kAnalyticsNetworkUnavailable = "Network Unavailable";
	public static final String kAnalyticsWebserviceFailureEventName = "Webservice Failure Response";
	public static final String kAnalyticsFailedWithDetails = "Failed with details - ";
	public static final String kAnalyticsWebserviceSuccessEventName = "Webservice Success Response";
	public static final String kAnalyticsWebserviceTimeout = "Webservice Timeout";
	public static final String kAnalyticsGPSDropout = "GPS DROP OUT";
	public static final String kAnalyticsWebserviceReqCall = "Webservice Request Call";
	public static final String kAnalyticsPageView = "Page View";
	
	public static final String enableFlurryLog = "captureDiagnostics";

}