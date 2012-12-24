package com.qburst.docphin.app;

import org.w3c.dom.Element;

public class DocphinApiConstants
{
    public static final String kNetworkExceptionKey = "NETWORK_EXCEPTION";
    public static final String kTimeOutExceptionKey = "TIMEOUT_EXCEPTION";
    /*
     * ErrorMessages
     */

    public static final String kNetworkErrorMessage =
            "No Internet connection detected. Please try again later.";
    public static final String kNetworkTimeoutErrorMessage =
            "Connection timed out. Please try again later.";
    public static final String kInternalServerErrorMessage =
            "Internal Server Error. Please try again later.";
    public static final String kAuthenticationErrorMessage =
            "Authentication Error. Please try again later.";
    public static final String kApiErrorMessage =
            "Web Service Error. Please try again later.";
    public static final String kBadRequestErrorMessage =
            "API Request Error. Please try again later.";

    // Network Constants

    public static final String kNetworkErrorExceptionKey = "101";
    public static final String kTimeOutErrorExceptionKey = "102";
    public static final String kInternalServerErrorExceptionKey = "103";
    public static final String kAuthenticationErrorExceptionKey = "104";
    public static final String kBadRequestErrorExceptionKey = "105";
    public static final int kNetworkErrorExceptionResponseKey = 101;
    public static final int kTimeOutErrorExceptionResponseKey = 102;
    public static final int kInternalServerExceptionResponseKey = 103;
    public static final int kAuthenticationExceptionResponseKey = 104;

    public static final String kSuccessMsgKey = "data";
    public static final String kNetworkError = "No Network";
    public static final String kApiError = "API Error";
    public static final String kApiUnauthMsgKey = "UnAuthorized";
    public static final String kApiFailedMsgKey = "Failure";
    public static final String kResponseBeanKey = "responseBean";
    public static final String kErrorMsgKey = "errorMessages";
    public static final String KResponseMessagesKey = "responseMessages";

    // HTTP Status Code Constants
    public static final String kHttpStatusKey = "HttpStatus";
    public static final int kHttpStatusOK = 200;
    public static final int kHttpStatusCreated = 201;
    public static final String kAuthHeaderKey = "Authorization";

    // Request Time out param

    public static final int kRequestTimeOutInMills = 20000;

    // Base Web Service
    public static final String kApiSuccessMsgKey = "API_SUCCESS";
    public static final String kApiFailMsgKey = "API_FAIL";
    public static final String kTimeoutError = "Server_Error";
    public static final String PHOTO_PATH_KEY = "photo[photo]";

    public static final String BASE_URL =
            "http://test.docphin.com/services/ws_connectmobile.asmx";
    public static final String DOCPHIN_URL = "https://www.docphin.com/";
    public static final String DOCPHIN_MYDIRECTORY_URL = "GetConnectTeam";
    
    //XML Constants

    public static final String kXMLattribute1 = "xmlns:xsi";
    public static final String kXMLattribute2 = "xmlns:xsd";
    public static final String kXMLattribute3 = "xmlns:soap";
    
    public static final String kXMLattribute1Value = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String kXMLattribute2Value = "http://www.w3.org/2001/XMLSchema";
    public static final String kXMLattribute3Value = "http://schemas.xmlsoap.org/soap/envelope/";
    
    public static final String XMLRoot = "soap:Envelope";
    public static final String XMLBody = "soap:Body";
    
    public static final String MY_MESSAGES_TAG = "GetUserMessages";
}
