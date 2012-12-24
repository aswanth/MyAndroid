package com.qburst.docphin.responseparsers;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.qburst.docphin.datamodels.DocphinCalenderModel;
import com.qburst.docphin.datamodels.DocphinMessageModel;
import com.qburst.docphin.utilities.DocphinUtilities;

public class DocphinEventsParser {
	
	String xml;
	Document document;
	public DocphinEventsParser(String xml) {
		// TODO Auto-generated constructor stub
		this.xml = xml;
		document = DocphinUtilities.getXMLObj(xml);
	}
	
	public ArrayList<DocphinCalenderModel> getMessageList() {

		Node msgNode;
		NodeList msgNodeChildren;
		DocphinCalenderModel msgModel;
		NodeList messageNodes = document.getElementsByTagName("SimpleEvent");
		ArrayList<DocphinCalenderModel> messageList = new ArrayList<DocphinCalenderModel>();
		for (int i = 0; i < messageNodes.getLength(); i++) {
			msgNode = messageNodes.item(i);
			msgNodeChildren = msgNode.getChildNodes();
			msgModel = new DocphinCalenderModel();
			for (int j = 0; j < msgNodeChildren.getLength(); j++) {
				Node node = msgNodeChildren.item(j);
				if (node.getNodeName().equals("EventDateTime")) {
					msgModel.setEventDateTime((node.getTextContent()));
				} else if (node.getNodeName().equals("MessageID")) {
					msgModel.setMessageID(Integer.parseInt(node
							.getTextContent()));
				} else if (node.getNodeName().equals("EventDate")) {
					msgModel.setEventDate(node.getTextContent());
				} else if (node.getNodeName().equals("EventTime")) {
					msgModel.setEventTime(node.getTextContent());
				} else if (node.getNodeName().equals("Title")) {
					msgModel.setTitle(node.getTextContent());
				}  else {

				}
			}
			messageList.add(msgModel);
		}
		return messageList;
	}
	


}
