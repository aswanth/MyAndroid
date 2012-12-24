package com.qburst.docphin.api;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.qburst.docphin.RequestModels.DocphinBaseRequestModel;
import com.qburst.docphin.RequestModels.DocphinMessagesRequestModel;
import com.qburst.docphin.Webservice.DocphinWebservice;
import com.qburst.docphin.apilisteners.DocphinEventsAPIListener;
import com.qburst.docphin.apilisteners.DocphinMyMessageAPIListener;
import com.qburst.docphin.app.DocphinApiConstants;
import com.qburst.docphin.responseparsers.DocphinEventsParser;
import com.qburst.docphin.responseparsers.DocphinMyMessagesParser;

public class DocphinEventsAPI extends DocphinBaseAPI{

	DocphinWebservice _webservice;
	DocphinEventsAPIListener _listener;
	
	public DocphinEventsAPI(DocphinEventsAPIListener listener) {
		// TODO Auto-generated constructor stub
		super();
		this._listener = listener;
		
	}

	public void getEvents() {
		_webservice = new DocphinWebservice(getRequestBody(), this);
		_webservice.execute();
	}
	
	@Override
	public Element getXMLDocContent() {
		// TODO Auto-generated method stub
		Element GetEvents = XMLRequestDoc.createElement("GetEvents");
		GetEvents.setAttribute("xmlns", DocphinApiConstants.DOCPHIN_URL);		
		
		DocphinBaseRequestModel xmlModel = new DocphinBaseRequestModel(XMLRequestDoc, GetEvents);
		
		return xmlModel.getRootElement();
	}
	@Override
	public String getSoapURL() {
		// TODO Auto-generated method stub
		return super.getSoapURL() + "GetEvents";
	}

		
	@Override
	public void WebserviceFinishedWithResponse(String response, int statucode) {
		// TODO Auto-generated method stub
		super.WebserviceFinishedWithResponse(response, statucode);
		Log.i("EventsResponse", response);
		DocphinEventsParser msgParser = new DocphinEventsParser(response);
		_listener.eventsFetchComplete(msgParser.getMessageList());
		
	}
	
}
