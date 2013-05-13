package com.QLog.qlogtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import android.content.Context;
import android.util.Log;

public class QLogFileManager {
	private File qlogDir;
	Context context = null;
	private String fileTag = "QLog.";
	
//	private String _encryptionKey = "AAAAB3NzaC1yc2EAAAADAQABAAABAQDaFtA91fluCPpVUOYA80aQiXEaBDIDetAvJ+NcK3PKGxvVZHbH8QM2sNGn/4h4gsjhqflt4MBAtesYYSqr/qeXlXOr2dJdgoK5EuGv6gAEemnJU8HWUZYSHXA6b5AlMRfd36kKdYYw9EB7e/UCZdcNqCXiUmspPK8WBCGPlESw1lulo1j2MzEIBRUSHliFKBCwRwRHLL8Mv3NRQkPa732W/oXpAUK2SwDhlXNMEYf2r6xYaEiKuOaAhGcfmKppVppIDJeneG791d+9F9NpcxBDwynymxnY4LotS+j0VlAGrJcXKUNZ+S+PlouY0kx9qHarCtEcYHt7TqSPhLybQlwB";

//	private String _encryptionKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2hbQPdX5bgj6VVDmAPNG" +
//"kIlxGgQyA3rQLyfjXCtzyhsb1WR2x/EDNrDRp/+IeILI4an5beDAQLXrGGEqq/6n" +
//"l5Vzq9nSXYKCuRLhr+oABHppyVPB1lGWEh1wOm+QJTEX3d+pCnWGMPRAe3v1AmXX" +
//"Dagl4lJrKTyvFgQhj5REsNZbpaNY9jMxCAUVEh5YhSgQsEcERyy/DL9zUUJD2u99" +
//"lv6F6QFCtksA4ZVzTBGH9q+sWGhIirjmgIRnH5iqaVaaSAyXp3hu/dXfvRfTaXMQ" +
//"Q8Mp8psZ2OC6LUvo9FZQBqyXFylDWfkvj5aLmNJMfah2qwrRHGB7e06kj4S8m0Jc" +
//"AQIDAQAB";
	
	private String _encryptionKey = "00:da:16:d0:3d:d5:f9:6e:08:fa:55:50:e6:00:f3:" +
    "46:90:89:71:1a:04:32:03:7a:d0:2f:27:e3:5c:2b:" +
    "73:ca:1b:1b:d5:64:76:c7:f1:03:36:b0:d1:a7:ff:" +
    "88:78:82:c8:e1:a9:f9:6d:e0:c0:40:b5:eb:18:61:" +
    "2a:ab:fe:a7:97:95:73:ab:d9:d2:5d:82:82:b9:12:" +
    "e1:af:ea:00:04:7a:69:c9:53:c1:d6:51:96:12:1d:" +
    "70:3a:6f:90:25:31:17:dd:df:a9:0a:75:86:30:f4:" +
    "40:7b:7b:f5:02:65:d7:0d:a8:25:e2:52:6b:29:3c:" +
    "af:16:04:21:8f:94:44:b0:d6:5b:a5:a3:58:f6:33:" +
    "31:08:05:15:12:1e:58:85:28:10:b0:47:04:47:2c:" +
    "bf:0c:bf:73:51:42:43:da:ef:7d:96:fe:85:e9:01:" +
    "42:b6:4b:00:e1:95:73:4c:11:87:f6:af:ac:58:68:" +
    "48:8a:b8:e6:80:84:67:1f:98:aa:69:56:9a:48:0c:" +
    "97:a7:78:6e:fd:d5:df:bd:17:d3:69:73:10:43:c3:" +
    "29:f2:9b:19:d8:e0:ba:2d:4b:e8:f4:56:50:06:ac:" +
    "97:17:29:43:59:f9:2f:8f:96:8b:98:d2:4c:7d:a8:" +
    "76:ab:0a:d1:1c:60:7b:7b:4e:a4:8f:84:bc:9b:42:" +
    "5c:01";
	
	public QLogFileManager() {

		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			qlogDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					QLog.app.getPackageName());
		} 

		if (!qlogDir.exists()) {
			qlogDir.mkdirs();
		}
	}

	//Write the content in the file, with the given filename
	public void writeToFile(String fileName, String content) {

		File f = new File(qlogDir, fileName);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f, true);
			fos.write(content.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public int getLogFileCount() {
		return (qlogDir.list()).length;
	}
	
	public String getFilePath(int pos) {
		
		File f = new File(qlogDir, fileTag + pos);
		
		return f.getAbsolutePath();
	}

	//Retrieve the content in the file, with the given filename
	public String readFromFile(String fileName) {

		String responseFromCache = null;
		File f = new File(qlogDir, fileName);
		FileInputStream fis;

		try {
			fis = new FileInputStream(f);
			InputStreamReader inputStreamReader = new InputStreamReader(fis);
		    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		    StringBuilder sb = new StringBuilder();
		    String line;
		    while ((line = bufferedReader.readLine()) != null) {
		        sb.append(line);
		    }
		    responseFromCache = sb.toString();
		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return responseFromCache;
	}
	
	public void saveLog(String content) {
		
		arrangeLogFiles();
		String encryptedText = QLogEncryption.encrypt(content);
		
		Log.i("Encrypted text-", encryptedText);
		
		Log.i("Decrypted text-", QLogEncryption.decrypt(encryptedText));
		
		writeToFile(fileTag + 1, encryptedText);
		
//		String logFromFile = readFromFile(fileTag + 1);
		
//		Log.i("Decrypted text from file-", QLogEncryption.decrypt(logFromFile));
		
	}
	
	public void arrangeLogFiles() {
		File currentFile = new File(qlogDir, fileTag + 1);
//		currentFile.length() >= 1048576
		if(currentFile.exists() && currentFile.length() >= 1048576){
			File temp;
			for(int i = 10; i > 0; i--){
				temp = new File(qlogDir, fileTag + i);
				if(temp.exists()){
					if(i == 10){
						temp.delete();
					}else {
						int y = i+1;
						temp.renameTo(new File(qlogDir, fileTag + y));
						temp.delete();
					}
				}
			}
		}
	}
	
	
	
	//To delete a file corresponding to the given name
	public void deleteFile(String filename) {
		
        File DeletedFile = new File(qlogDir , filename);
		DeletedFile.delete();
	}
	
	public void clearAllLogs() {
		if (qlogDir.exists()) {
            String[] children = qlogDir.list();
            for (String s : children) {
                    deleteDir(new File(qlogDir, s));
            }
        }
	}
	
	//To delete a given file/directory
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
    
	public byte[] encryptRSA(String in) {
		
//		QLogEncryption.encrypt(in.getBytes());
		return in.getBytes();
		
//		try {
//			byte[] encodedKey = Base64.decode(_encryptionKey, Base64.DEFAULT);//_encryptionKey.getBytes();
//			byte[] encodedKey = _encryptionKey.getBytes();
//			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
//					encodedKey);
//			PemReader pemReader = new PemReader(new FileReader("file.pem"));
//			Object obj = pemReader.readObject();
//			byte[] modulusBase64 = _encryptionKey.getBytes("UTF-8");//Base64.decode(_encryptionKey);//new String(encodedKey);
//			String exponentBase64 = "65537";
//			RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(1, modulusBase64), new BigInteger(1, exponentBase64.getBytes("UTF-8")));
//			RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(modulusBase64), new BigInteger(exponentBase64.getBytes()));

			
//			ASN1InputStream insi = new ASN1InputStream(modulusBase64);
//			DERObject obj =  insi.readObject();
//            RSAPublicKeyStructure keyStruct = RSAPublicKeyStructure.getInstance(insi.readObject());
//            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(keyStruct.getModulus(), keyStruct.getPublicExponent());
			
			
//			KeyFactory kf = KeyFactory.getInstance("RSA");
//			RSAPublicKey pkPublic = (RSAPublicKey)kf.generatePublic(publicKeySpec);
//			Log.i("pblicExponent", "format-" + pkPublic.getFormat() + ", Algorithm-" + pkPublic.getAlgorithm() + ", encoding-" + pkPublic.getEncoded().toString() + ", getModulus-" + pkPublic.getModulus() + ", getPublicExponent-" + pkPublic.getPublicExponent());
//			// Encrypt
//			Cipher pkCipher = Cipher.getInstance("RSA");
//			Cipher pkCipher = Cipher.getInstance("RSA");
//			pkCipher.init(Cipher.ENCRYPT_MODE, pkPublic);
//			Log.i("LogMessage-", in);
//			return pkCipher.doFinal(in.getBytes("UTF-8"));
//		} catch (Exception e) {
//			Log.i("Encryption-exception", e.toString());
//			return null;
//		}
	}

}
