package com.qburst.docphin.RequestModels;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.qburst.docphin.app.DocphinApp;

public class DocphinBaseRequestModel {
	private Element mobileGUID;
    public Document document;
    public Element rootElement;
    
	public DocphinBaseRequestModel(Document document, Element rootElement) {
		this.document = document;
		this.rootElement = rootElement;
		prepareElements();
        setElementData();
        prepareRootElement();
		
	}
	 protected void prepareElements()
	    {

	        mobileGUID = document.createElement("mobileGUID");

	    }

	    protected void setElementData()
	    {
	        mobileGUID.appendChild(document
	                .createTextNode(DocphinApp.getSharedApplication().getSessionToken()));

	    }

	    protected void prepareRootElement()
	    {
	        rootElement.appendChild(mobileGUID);

	    }
	    public Element getRootElement()
	    {
	        return rootElement;
	    }

		
}
