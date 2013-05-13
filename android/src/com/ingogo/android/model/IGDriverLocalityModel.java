package com.ingogo.android.model;

import java.io.Serializable;

public class IGDriverLocalityModel implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4656512197692681169L;
	private String _localityPrefix;

	
	public IGDriverLocalityModel(){
		_localityPrefix = "";
	}

	public String getLocalityPrefixList() {
		return _localityPrefix;
	}

	public void setLocalityPrefixList(String _localityPrefixList) {
		this._localityPrefix = _localityPrefixList;
	}

}
