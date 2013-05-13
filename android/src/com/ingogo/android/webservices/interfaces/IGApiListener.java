package com.ingogo.android.webservices.interfaces;

import java.util.Map;

public interface IGApiListener {
    public void onResponseReceived(Map<String, Object> response);
    public void onFailedToGetResponse(Map<String, Object> errorResponse);
    
}
