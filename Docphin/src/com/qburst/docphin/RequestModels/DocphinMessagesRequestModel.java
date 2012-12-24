package com.qburst.docphin.RequestModels;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DocphinMessagesRequestModel extends DocphinBaseRequestModel
{
    private Element pageLen, pageNum, statusID, typeID, keywords, datefrom,
            dateto, isfavorite;

    public DocphinMessagesRequestModel(Document doc, Element root)
    {

        super(doc, root);
    }

    public void prepareElements()
    {
        super.prepareElements();
        pageLen = document.createElement("pageLen");
        pageNum = document.createElement("pageNum");
        statusID = document.createElement("statusID");
        typeID = document.createElement("typeID");
        keywords = document.createElement("keywords");
        datefrom = document.createElement("datefrom");
        dateto = document.createElement("dateto");
        isfavorite = document.createElement("isfavorite");
    }

    public void setElementData()
    {
        super.setElementData();
        pageLen.appendChild(document.createTextNode("" + 20));
        pageNum.appendChild(document.createTextNode("" + (1)));
        statusID.appendChild(document.createTextNode("" + (-1)));
        typeID.appendChild(document.createTextNode("" + (-1)));
        keywords.appendChild(document.createTextNode(""));
        datefrom.appendChild(document.createTextNode("1970-01-01T00:00:00"));
        dateto.appendChild(document.createTextNode("2100-01-01T00:00:00"));
        isfavorite.appendChild(document.createTextNode("" + (-1)));
    }

    public void prepareRootElement()
    {
        super.prepareRootElement();
        rootElement.appendChild(pageLen);
        rootElement.appendChild(pageNum);
        rootElement.appendChild(statusID);
        rootElement.appendChild(typeID);
        rootElement.appendChild(keywords);
        rootElement.appendChild(datefrom);
        rootElement.appendChild(dateto);
        rootElement.appendChild(isfavorite);
    }   

}