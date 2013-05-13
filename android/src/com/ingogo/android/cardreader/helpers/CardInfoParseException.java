package com.ingogo.android.cardreader.helpers;

import com.ingogo.android.utilities.IGUtility;

public class CardInfoParseException extends Exception {

    private static final long serialVersionUID = 4973013545996924262L;

    public CardInfoParseException() {
    	super();
		  IGUtility.logExceptionInQLogger(this);

    }

    public CardInfoParseException(String aMessage) {
    	super(aMessage);
		  IGUtility.logExceptionInQLogger(this);

    }
}
