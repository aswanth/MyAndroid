package com.ingogo.android.cardreader.helpers;

public class LinePrintHelper {

	private static final char BOLD_DELIMITER = '~';
	private static final char SPACE_CHARACTER = ' ';

	private static final char ESCAPE = 0x1B;
	private static final char SET_DOUBLE_WIDTH_CODE = 0x21;
	private static final char NORMAL_WIDTH = 0x00;
	private static final char BOLD_WIDTH = 0x10;
	private static final char SPACE = 0x20;
	private static Boolean boldEnabled = false;
	
	public static void reset(){
		boldEnabled = false;
	}
	
	
	public static String getPrinterFeed(String data){
		
		StringBuffer stringBuffer = new StringBuffer();
		
		int length = data.length();
		
		for(int i=0;i<length;i++){
		
			char character = data.charAt(i);
			if(isDelimiter(character))
				toggleBoldMode(stringBuffer);
			else if(isSpace(character)) 
				stringBuffer.append(SPACE);
			else 
				stringBuffer.append(character);
			
			
		}	
		
		return stringBuffer.toString();
		
	}
	
	private static Boolean isSpace(char character) {
		if(character==SPACE_CHARACTER){
			return true;
		}
		return false;
	}
	private static Boolean isDelimiter(char character){
		
		if(character==BOLD_DELIMITER){
			return true;
		}
		return false;
		
	}
	
	private static void toggleBoldMode(StringBuffer stringBuffer){
		setBoldMode(!isBoldModeActive());
		appendBoldModeCommands(stringBuffer);			
	}
	
	private static void appendBoldModeCommands(StringBuffer stringBuffer) {
		stringBuffer.append(boldModeCommands());
	}

	private static Boolean isBoldModeActive(){
		return boldEnabled;
	}
	
	private static void setBoldMode(Boolean mode){
		boldEnabled = mode;
	}
	
	private static String boldModeCommands(){
		
		char commands[] = new char[3];
		commands[0] = ESCAPE;
		commands[1] = SET_DOUBLE_WIDTH_CODE;

		if(isBoldModeActive())
			commands[2] = BOLD_WIDTH;
		else
			commands[2] = NORMAL_WIDTH;
		
		return new String(commands);
	}
}
