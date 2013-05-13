package com.ingogo.android.cardreader.helpers;

import java.io.Serializable;

public class CardInfo implements Serializable {

    private static final long serialVersionUID = -1419046693843353445L;
    private static final String TRACK1_FIELD_SEPARATOR = "^";
    private static final String TRACK1_NAME_SEPARATOR = "/";
    private static final String TRACK2_FIELD_SEPARATOR = "=";

    public static enum TrackNum {
	TRACK_1, TRACK_2
    }

    private String _name;
    private String _pan;
    private int _expirationYear;
    private int _expirationMonth;
    private String _rawData;
    private TrackNum _trackNum;

    public CardInfo(String aRawData) throws CardInfoParseException {
	_rawData = aRawData;
	parse();
    }

    public String getAccountName() {
	return _name;
    }

    public String getAccountNumber() {
	return _pan;
    }

    public int getExpirationYear() {
	return _expirationYear;
    }

    public int getExpirationMonth() {
	return _expirationMonth;
    }

    public String getRawData() {
	return _rawData;
    }

    public void setRawData(String aRawData) {
	_rawData = aRawData;
    }

    public TrackNum getTrackNum() {
	return _trackNum;
    }

    /* LUHN checksum */
    public boolean validateAccountNumber() throws CardInfoParseException {
	int sum = 0;
	boolean alt = false;

	int[] pan = new int[_pan.length()];
	for (int i = 0; i < _pan.length(); ++i) {
	    if (!Character.isDigit(_pan.charAt(i))) {
		throw new CardInfoParseException(String.format("PAN contains non-digits (%s).", _pan));
	    }
	    pan[i] = Integer.parseInt(new String(new char[] { _pan.charAt(i) }));
	}

	for (int i = pan.length - 1; i >= 0; --i) {
	    int temp = pan[i];
	    if (alt) {
		temp *= 2;
		if (temp > 9) {
		    temp -= 9;
		}
	    }
	    sum += temp;
	    alt = !alt;
	}
	return sum % 10 == 0;
    }

    public void parse() throws CardInfoParseException {
	if (_rawData.startsWith(";")) {
	    _trackNum = TrackNum.TRACK_2;
	    _name = "";
	    int separatorIndex = _rawData.indexOf(TRACK2_FIELD_SEPARATOR);
	    if (separatorIndex == -1) {
		throw new CardInfoParseException(String.format("Field separator not found. Card string: %s", _rawData));
	    }
	    _pan = _rawData.substring(1, separatorIndex);
	    _expirationYear = Integer.parseInt(_rawData.substring(separatorIndex + 1, separatorIndex + 3));
	    _expirationMonth = Integer.parseInt(_rawData.substring(separatorIndex + 3, separatorIndex + 5));
	    if (!validateAccountNumber()) {
		throw new CardInfoParseException(String.format("PAN validation failed. Card string: %s", _rawData));
	    }
	} else if (_rawData.startsWith("%")) {
	    _trackNum = TrackNum.TRACK_1;
	    int separatorIndex = _rawData.indexOf(TRACK1_FIELD_SEPARATOR);
	    if (separatorIndex == -1) {
		throw new CardInfoParseException(String.format("Field separator not found. Card string: %s", _rawData));
	    }
	    _pan = _rawData.substring(2, separatorIndex);

	    int firstSeparatorIndex = separatorIndex;
	    separatorIndex = _rawData.indexOf(TRACK1_FIELD_SEPARATOR, separatorIndex + 1);
	    if (separatorIndex == -1) {
		throw new CardInfoParseException(String.format("Second field separator not found. Card string: %s", _rawData));
	    }
	    _name = _rawData.substring(firstSeparatorIndex + 1, separatorIndex);
	    _name = _name.trim();
	    _name = _name.replace(TRACK1_NAME_SEPARATOR, " ");
	    _expirationYear = Integer.parseInt(_rawData.substring(separatorIndex + 1, separatorIndex + 3));
	    _expirationMonth = Integer.parseInt(_rawData.substring(separatorIndex + 3, separatorIndex + 5));
	    if (!validateAccountNumber()) {
		throw new CardInfoParseException(String.format("PAN validation failed. Card string: %s", _rawData));
	    }
	} else {
	    throw new CardInfoParseException(String.format("No start sentinel found. Card string: %s", _rawData));
	}
    }
}
