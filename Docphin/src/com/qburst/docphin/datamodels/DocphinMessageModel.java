package com.qburst.docphin.datamodels;

public class DocphinMessageModel {
	
	String MessageDate, Sender, Title, Type, Status, MessageSnippet, MessageUserStatus;
	int MessageID, TypeID, StatusID, MessageUserStatusID;
	boolean HasAttachments, isFavorite;
	
	public String getMessageDate() {
		return MessageDate;
	}
	public void setMessageDate(String messageDate) {
		MessageDate = messageDate;
	}
	public String getSender() {
		return Sender;
	}
	public void setSender(String sender) {
		Sender = sender;
	}
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public String getStatus() {
		return Status;
	}
	public void setStatus(String status) {
		Status = status;
	}
	public String getMessageSnippet() {
		return MessageSnippet;
	}
	public void setMessageSnippet(String messageSnippet) {
		MessageSnippet = messageSnippet;
	}
	public String getMessageUserStatus() {
		return MessageUserStatus;
	}
	public void setMessageUserStatus(String messageUserStatus) {
		MessageUserStatus = messageUserStatus;
	}
	public int getMessageID() {
		return MessageID;
	}
	public void setMessageID(int messageID) {
		MessageID = messageID;
	}
	public int getTypeID() {
		return TypeID;
	}
	public void setTypeID(int typeID) {
		TypeID = typeID;
	}
	public int getStatusID() {
		return StatusID;
	}
	public void setStatusID(int statusID) {
		StatusID = statusID;
	}
	public int getMessageUserStatusID() {
		return MessageUserStatusID;
	}
	public void setMessageUserStatusID(int messageUserStatusID) {
		MessageUserStatusID = messageUserStatusID;
	}
	public boolean isHasAttachments() {
		return HasAttachments;
	}
	public void setHasAttachments(boolean hasAttachments) {
		HasAttachments = hasAttachments;
	}
	public boolean isFavorite() {
		return isFavorite;
	}
	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}
}
