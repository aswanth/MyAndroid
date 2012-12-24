package com.qburst.docphin.datamodels;

public class DocphinCalenderModel {
String EventDateTime,EventDate,EventTime,Title;
//String date, time,event,place;
int StatusID,MessageID;

public String getEventDateTime() {
	return EventDateTime;
}
public void setEventDateTime(String eventDateTime) {
	EventDateTime = eventDateTime;
}
public int getMessageID() {
	return MessageID;
}
public void setMessageID(int messageID) {
	MessageID = messageID;
}
public String getEventDate() {
	return EventDate;
}
public void setEventDate(String eventDate) {
	EventDate = eventDate;
}
public String getEventTime() {
	return EventTime;
}
public void setEventTime(String eventTime) {
	EventTime = eventTime;
}
public String getTitle() {
	return Title;
}
public void setTitle(String title) {
	Title = title;
}
public int getStatusID() {
	return StatusID;
}
public void setStatusID(int statusID) {
	StatusID = statusID;
}
//public String getDate() {
//	return date;
//}
//public void setDate(String date) {
//	this.date = date;
//}
//public String getTime() {
//	return time;
//}
//public void setTime(String time) {
//	this.time = time;
//}
//public String getEvent() {
//	return event;
//}
//public void setEvent(String event) {
//	this.event = event;
//}
//public String getPlace() {
//	return place;
//}
//public void setPlace(String place) {
//	this.place = place;
//}

}