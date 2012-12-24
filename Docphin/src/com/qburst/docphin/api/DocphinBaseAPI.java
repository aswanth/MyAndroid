package com.qburst.docphin.api;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.util.Log;

import com.qburst.docphin.Webservice.DocphinWebserviceListener;
import com.qburst.docphin.app.DocphinApiConstants;
import com.qburst.docphin.utilities.DocphinUtilities;

public class DocphinBaseAPI implements DocphinWebserviceListener {
	
	Document XMLRequestDoc, XMLResponseDoc;
	
	public DocphinBaseAPI() {
		// TODO Auto-generated constructor stub
		initXMLDocs();
	}

	public void WebserviceFinishedWithResponse(String response, int statusCode) {
		// TODO Auto-generated method stub
		Log.i("API Response", response);
	}

	public void WebserviceFailedWithError(String error, int statusCode) {
		// TODO Auto-generated method stub
		Log.i("API Error", error);
	}

	public String getRequestBody() {
		String requsetBody;		
		Document doc = getXMLDoc();
		requsetBody = DocphinUtilities.getXMLString(doc);
		
		return requsetBody;
	}
	
	public void initXMLDocs() {
		try{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
			XMLRequestDoc = documentBuilder.newDocument();
			XMLResponseDoc = documentBuilder.newDocument();
		}
		catch (Exception e) {
			// TODO: handle exception
			XMLRequestDoc = null;
			XMLResponseDoc = null;
		}
	}

	public Document getXMLDoc() {
		  
		try{
		
		Element rootElement = XMLRequestDoc.createElement(DocphinApiConstants.XMLRoot);
		rootElement.setAttribute(DocphinApiConstants.kXMLattribute1, DocphinApiConstants.kXMLattribute1Value);
		rootElement.setAttribute(DocphinApiConstants.kXMLattribute2, DocphinApiConstants.kXMLattribute2Value);
		rootElement.setAttribute(DocphinApiConstants.kXMLattribute3,DocphinApiConstants.kXMLattribute3Value);
		Element body = XMLRequestDoc.createElement(DocphinApiConstants.XMLBody);
		body.appendChild(getXMLDocContent());
		rootElement.appendChild(body);
		XMLRequestDoc.appendChild(rootElement);
		}
		catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		return XMLRequestDoc;
	}
	
	public Element getXMLDocContent() {
		return null;
	}
	public String getSoapURL() {
		return DocphinApiConstants.DOCPHIN_URL;
	}
	
	public String parseXMLData(String xml) {		
		return null;
	}

}
