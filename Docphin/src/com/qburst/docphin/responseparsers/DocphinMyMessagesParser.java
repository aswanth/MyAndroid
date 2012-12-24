package com.qburst.docphin.responseparsers;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.qburst.docphin.datamodels.DocphinMessageModel;
import com.qburst.docphin.utilities.DocphinUtilities;

public class DocphinMyMessagesParser {
	
	String xml;
	Document document;
	public DocphinMyMessagesParser(String xml) {
		// TODO Auto-generated constructor stub
		this.xml = xml;
		document = DocphinUtilities.getXMLObj(xml);
	}
	
	public ArrayList<DocphinMessageModel> getMessageList() {

		Node msgNode;
		NodeList msgNodeChildren;
		DocphinMessageModel msgModel;
		NodeList messageNodes = document.getElementsByTagName("SimpleMessage");
		ArrayList<DocphinMessageModel> messageList = new ArrayList<DocphinMessageModel>();
		for (int i = 0; i < messageNodes.getLength(); i++) {
			msgNode = messageNodes.item(i);
			msgNodeChildren = msgNode.getChildNodes();
			msgModel = new DocphinMessageModel();
			for (int j = 0; j < msgNodeChildren.getLength(); j++) {
				Node node = msgNodeChildren.item(j);
				if (node.getNodeName().equals("HasAttachments")) {
					msgModel.setHasAttachments(Boolean.parseBoolean(node
							.getTextContent()));
				} else if (node.getNodeName().equals("isFavorite")) {
					msgModel.setFavorite(Boolean.parseBoolean(node
							.getTextContent()));
				} else if (node.getNodeName().equals("MessageUserStatus")) {
					msgModel.setMessageUserStatus(node.getTextContent());
				} else if (node.getNodeName().equals("MessageDate")) {
					msgModel.setMessageDate(node.getTextContent());
				} else if (node.getNodeName().equals("Sender")) {
					msgModel.setSender(node.getTextContent());
				} else if (node.getNodeName().equals("Title")) {
					msgModel.setTitle(node.getTextContent());
				} else if (node.getNodeName().equals("Type")) {
					msgModel.setType(node.getTextContent());
				} else if (node.getNodeName().equals("Status")) {
					msgModel.setStatus(node.getTextContent());
				} else if (node.getNodeName().equals("MessageSnippet")) {
					msgModel.setMessageSnippet(node.getTextContent());
				} else if (node.getNodeName().equals("MessageID")) {
					msgModel.setMessageID(Integer.parseInt(node
							.getTextContent()));
				} else if (node.getNodeName().equals("TypeID")) {
					msgModel.setTypeID(Integer.parseInt(node.getTextContent()));
				} else if (node.getNodeName().equals("StatusID")) {
					msgModel.setStatusID(Integer.parseInt(node.getTextContent()));
				} else if (node.getNodeName().equals("MessageUserStatusID")) {
					msgModel.setMessageUserStatusID(Integer.parseInt(node
							.getTextContent()));
				} else {

				}
			}
			messageList.add(msgModel);
		}
		return messageList;
	}
	
}
