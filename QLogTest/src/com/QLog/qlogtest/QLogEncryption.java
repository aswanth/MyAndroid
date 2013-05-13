package com.QLog.qlogtest;

import java.util.Arrays;

import android.util.Log;

public class QLogEncryption {
	
	static String encrypt(String ciphertext){
		
		 byte[] cipherBytes = ciphertext.getBytes(); 
		
//		byte[] x= Arrays.copyOfRange(ciphertext, 0, 1);
		for(int i = 0; i < cipherBytes.length; i++){
			Log.i("element", "" + i +"-" + cipherBytes[i]);
			cipherBytes[i] = (byte) (cipherBytes[i] + 10);
		}
		
		return new String(cipherBytes);
	}
	static String decrypt(String ciphertext){
		
		 byte[] cipherBytes = ciphertext.getBytes(); 
		
//		byte[] x= Arrays.copyOfRange(ciphertext, 0, 1);
		for(int i = 0; i < cipherBytes.length; i++){
			Log.i("element", "" + i +"-" + cipherBytes[i]);
			cipherBytes[i] = (byte) (cipherBytes[i] - 10);
		}
		
		return new String(cipherBytes);
	}

}
