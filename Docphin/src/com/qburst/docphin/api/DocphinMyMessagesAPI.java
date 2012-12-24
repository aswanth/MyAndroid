package com.qburst.docphin.api;

import org.w3c.dom.Element;

import android.util.Log;

import com.qburst.docphin.RequestModels.DocphinMessagesRequestModel;
import com.qburst.docphin.Webservice.DocphinWebservice;
import com.qburst.docphin.apilisteners.DocphinMyMessageAPIListener;
import com.qburst.docphin.app.DocphinApiConstants;
import com.qburst.docphin.responseparsers.DocphinMyMessagesParser;

public class DocphinMyMessagesAPI extends DocphinBaseAPI{

	DocphinWebservice _webservice;
	DocphinMyMessageAPIListener _listener;
	public DocphinMyMessagesAPI(DocphinMyMessageAPIListener listener) {
		// TODO Auto-generated constructor stub
		super();
		this._listener = listener;
		
	}
	
	public void getMyMessages() {
		_webservice = new DocphinWebservice(getRequestBody(), this);
		_webservice.execute();
	}
	
	@Override
	public Element getXMLDocContent() {
		// TODO Auto-generated method stub
		Element GetUserMessages = XMLRequestDoc.createElement(DocphinApiConstants.MY_MESSAGES_TAG);
		GetUserMessages.setAttribute("xmlns", DocphinApiConstants.DOCPHIN_URL);		
		
		DocphinMessagesRequestModel xmlModel = new DocphinMessagesRequestModel(XMLRequestDoc, GetUserMessages);
		
		return xmlModel.getRootElement();
	}
	@Override
	public String getSoapURL() {
		// TODO Auto-generated method stub
		return super.getSoapURL() + DocphinApiConstants.MY_MESSAGES_TAG;
	}

		
	@Override
	public void WebserviceFinishedWithResponse(String response, int statucode) {
		// TODO Auto-generated method stub
		super.WebserviceFinishedWithResponse(response, statucode);
		Log.i("MyMessagesResponse", response);
		DocphinMyMessagesParser msgParser = new DocphinMyMessagesParser(response);
		_listener.myMessagesFetchComplete(msgParser.getMessageList());
		
	}
	
}
