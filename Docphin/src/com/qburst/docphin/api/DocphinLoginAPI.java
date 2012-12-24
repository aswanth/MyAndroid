package com.qburst.docphin.api;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.qburst.docphin.Webservice.DocphinWebservice;
import com.qburst.docphin.apilisteners.DocphinLoginAPIListener;
import com.qburst.docphin.app.DocphinApiConstants;
import com.qburst.docphin.app.DocphinConstants;
import com.qburst.docphin.utilities.DocphinUtilities;

public class DocphinLoginAPI extends DocphinBaseAPI{
	
	private DocphinLoginAPIListener _listener;
	private String username, password;
	private DocphinWebservice _webservice;
	
	public DocphinLoginAPI(DocphinLoginAPIListener listener, String username,
			String password) {
		// TODO Auto-generated constructor stub
		super();
		this._listener = listener;
		setCredentials(username, password);

	}
	
	public void login() {
		_webservice = new DocphinWebservice(getRequestBody(), this);
		_webservice.execute();
	}
	
	public void setCredentials(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	@Override
	public Element getXMLDocContent() {
		// TODO Auto-generated method stub
		Element MobileAuthenticate = XMLRequestDoc.createElement("MobileAuthenticate");
		MobileAuthenticate.setAttribute("xmlns", DocphinApiConstants.DOCPHIN_URL);
		
		Element username = XMLRequestDoc.createElement("username");
		Element password = XMLRequestDoc.createElement("password");
		
		username.appendChild(XMLRequestDoc.createTextNode(this.username));
		password.appendChild(XMLRequestDoc.createTextNode(this.password));
		
		MobileAuthenticate.appendChild(username);
		MobileAuthenticate.appendChild(password);
		
		return MobileAuthenticate;
	}
	@Override
	public String getSoapURL() {
		// TODO Auto-generated method stub
		return super.getSoapURL() + "MobileAuthenticate";
	}
	@Override
	public String parseXMLData(String xml) {

		XMLResponseDoc = DocphinUtilities.getXMLObj(xml);
		NodeList nodes = XMLResponseDoc
				.getElementsByTagName("MobileAuthenticateResult");
		String authKey = (nodes.item(0)).getTextContent();
		

		return authKey;
	}
	
	@Override
	public void WebserviceFinishedWithResponse(String response, int statusCode) {
		// TODO Auto-generated method stub
		super.WebserviceFinishedWithResponse(response, statusCode);
		Log.i("STATUSCODE", "" + statusCode);
		Log.i("AUTHKEY", parseXMLData(response));
		
		if(parseXMLData(response).equals(DocphinConstants.loginFailedResponse)){
			_listener.userLoginFailed(DocphinConstants.invalidCredentials);}
		else{
			_listener.userLoggedInSuccessfully(parseXMLData(response));
		}
			
		
	}
	
	public void WebserviceFailedWithError(String error, int statusCode) {
		_listener.userLoginFailed(error);
	}

}
