package com.qburst.docphin.datamodels;

import java.util.ArrayList;

public class DocphinMessageDetails {

	private boolean hasAttachments, isFavorite;
	private int messageId, typeId, statusId, messageUserStatusID;
	private String messageDate, sender, title, status, type, messageSnippet, messageUserStatus, messageText;

	private ArrayList<DocphinAttachments> attachments;
	

	public ArrayList<DocphinAttachments> getAttachments() {
		return attachments;
	}

	public void setAttachments(ArrayList<DocphinAttachments> attachments) {
		this.attachments = attachments;
	}

	public boolean isHasAttachments() {
		return hasAttachments;
	}

	public void setHasAttachments(boolean hasAttachments) {
		this.hasAttachments = hasAttachments;
	}

	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}

	public String getMessageDate() {
		return messageDate;
	}

	public void setMessageDate(String messageDate) {
		this.messageDate = messageDate;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getMessageUserStatusID() {
		return messageUserStatusID;
	}

	public void setMessageUserStatusID(int messageUserStatusID) {
		this.messageUserStatusID = messageUserStatusID;
	}

	public String getMessageSnippet() {
		return messageSnippet;
	}

	public void setMessageSnippet(String messageSnippet) {
		this.messageSnippet = messageSnippet;
	}

	public String getMessageUserStatus() {
		return messageUserStatus;
	}

	public void setMessageUserStatus(String messageUserStatus) {
		this.messageUserStatus = messageUserStatus;
	}

	public boolean isFavorite() {
		return isFavorite;
	}

	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
}
