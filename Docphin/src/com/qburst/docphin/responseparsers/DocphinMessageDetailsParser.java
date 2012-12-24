package com.qburst.docphin.responseparsers;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.qburst.docphin.datamodels.DocphinAttachments;
import com.qburst.docphin.datamodels.DocphinMessageDetails;
import com.qburst.docphin.utilities.DocphinUtilities;

public class DocphinMessageDetailsParser {

	String xml;
	Document document;

	public DocphinMessageDetailsParser(String xml) {
		// TODO Auto-generated constructor stub
		this.xml = xml;
		document = DocphinUtilities.getXMLObj(xml);
	}

	public ArrayList<DocphinMessageDetails> getMessageDetails() {

		Node msgNode;
		NodeList msgNodeChildren;
		DocphinMessageDetails msgModel;
		NodeList messageNodes = document
				.getElementsByTagName("GetMessageDetailResult");
		ArrayList<DocphinMessageDetails> messageDetails = new ArrayList<DocphinMessageDetails>();
		for (int i = 0; i < messageNodes.getLength(); i++) {
			msgNode = messageNodes.item(i);
			msgNodeChildren = msgNode.getChildNodes();
			msgModel = new DocphinMessageDetails();
			for (int j = 0; j < msgNodeChildren.getLength(); j++) {
				Node node = msgNodeChildren.item(j);
				if (node.getNodeName().equals("hasAttachments")) {
					msgModel.setHasAttachments(Boolean.parseBoolean(node
							.getTextContent()));
				} else if (node.getNodeName().equals("isFavorite")) {
					msgModel.setFavorite(Boolean.parseBoolean(node
							.getTextContent()));
				} else if (node.getNodeName().equals("messageUserStatus")) {
					msgModel.setMessageUserStatus(node.getTextContent());
				} else if (node.getNodeName().equals("messageDate")) {
					msgModel.setMessageDate(node.getTextContent());
				} else if (node.getNodeName().equals("sender")) {
					msgModel.setSender(node.getTextContent());
				} else if (node.getNodeName().equals("title")) {
					msgModel.setTitle(node.getTextContent());
				} else if (node.getNodeName().equals("type")) {
					msgModel.setType(node.getTextContent());
				} else if (node.getNodeName().equals("status")) {
					msgModel.setStatus(node.getTextContent());
				} else if (node.getNodeName().equals("messageSnippet")) {
					msgModel.setMessageSnippet(node.getTextContent());
				} else if (node.getNodeName().equals("messageID")) {
					msgModel.setMessageId(Integer.parseInt(node
							.getTextContent()));
				} else if (node.getNodeName().equals("typeID")) {
					msgModel.setTypeId(Integer.parseInt(node.getTextContent()));
				} else if (node.getNodeName().equals("statusID")) {
					msgModel.setStatusId(Integer.parseInt(node.getTextContent()));
				} else if (node.getNodeName().equals("messageUserStatusID")) {
					msgModel.setMessageUserStatusID(Integer.parseInt(node
							.getTextContent()));
				} else if (node.getNodeName().equals("messageText")) {
					msgModel.setMessageText(node.getTextContent());
				} else if (node.getNodeName().equals("attachments")) {
					msgModel.setAttachments(getAttachments(node));
				} else {

				}
			}
			messageDetails.add(msgModel);
		}
		return messageDetails;
	}


	public ArrayList<DocphinAttachments> getAttachments(Node node) {

		Node attchNode;
		NodeList attchNodeChildren;
		DocphinAttachments attchModel;
		NodeList messageNodes = document
				.getElementsByTagName("SimpleAttachment");
		ArrayList<DocphinAttachments> attachments = new ArrayList<DocphinAttachments>();
		for (int i = 0; i < messageNodes.getLength(); i++) {
			attchNode = messageNodes.item(i);
			attchNodeChildren = attchNode.getChildNodes();
			attchModel = new DocphinAttachments();
			for (int j = 0; j < attchNodeChildren.getLength(); j++) {
				Node attachmentNode = attchNodeChildren.item(j);
				if (attachmentNode.getNodeName().equals("attachmentID")) {
					attchModel.setAttachmentID(node.getTextContent());
				} else if (attachmentNode.getNodeName().equals("fileName")) {
					attchModel.setFileName(node.getTextContent());
				} else if (attachmentNode.getNodeName().equals("fileType")) {
					attchModel.setFileType(node.getTextContent());
				} else if (attachmentNode.getNodeName().equals("fileTypeImage")) {
					attchModel.setFileTypeImage(node.getTextContent());
				}
				else {

				}
			}
			attachments.add(attchModel);
		}
		return attachments;
	}

}
