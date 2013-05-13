package com.ingogo.android.webservices;

import java.util.HashMap;
import java.util.Map;

import com.ingogo.android.app.IGConstants;

/**
 * Created by IntelliJ IDEA. User: mahadevan Date: 27/5/11 Time: 5:39 PM To
 * change this template use File | Settings | File Templates.
 */
public class IGDummyResponse {

	public static Map<String, Object> getDummyLogin() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(IGConstants.kAccessToken, "124525334878");
		result.put(IGConstants.kbroadcastPositionInterval, "100000"); // in
																		// seconds?
		return result;
	}
}
