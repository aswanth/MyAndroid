package com.qburst.docphin.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class DocphinUtilities {


    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connec =
                (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connec.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected() == true) {
            return true;
        }
        return false;
    }

    public static String convertStreamToString(InputStream is)
            throws IOException
    {
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    public static String encodeImageUrl(String url)
    {
        int pos = url.lastIndexOf('/') + 1;
        String temp = url.substring(pos);
        int pos1 = 0;
        pos1 = temp.lastIndexOf('?') + 1;
        try {
            if (pos1 != 0) {
                url =
                        url.substring(0, pos)
                                + URLEncoder.encode(
                                        url.substring(pos, pos + pos1 - 1),
                                        "UTF-8");
            } else {
                url =
                        url.substring(0, pos)
                                + URLEncoder
                                        .encode(url.substring(pos), "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(e.getClass().getName() + ": encodeImageUrl", e.getMessage());
        }

        return url;
    }

    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];

            for (;;) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1) {
                    break;
                }
                os.write(bytes, 0, count);
            }

        } catch (Exception ex) {
        }

    }
    

    public static void showToast(String message, Context context)
    {
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

    public static float calculateDistance(float lat1, float lng1, float lat2,
            float lng2)
    {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
                        * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        int meterConversion = 1609;

        return new Float(dist * meterConversion).floatValue();
    }

    public static float roundOff(float value, int place)
    {
        float p = (float) Math.pow(10, place);
        value = value * p;
        float tmp = Math.round(value);
        return tmp / p;
    }

    public static boolean checkNullCase(String data)
    {
        if (data == null || data.trim().equals("")) {
            return false;
        }

        return true;
    }
    
    public static Document getXMLObj(String xml){
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
 
            DocumentBuilder db = dbf.newDocumentBuilder();
 
            InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is); 
 
            } catch (ParserConfigurationException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (SAXException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (IOException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            }
                // return DOM
            return doc;
    }
    
    public static String getXMLString(Document doc) {
    	
    	String xmlString;
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			Properties outFormat = new Properties();
			outFormat.setProperty(OutputKeys.INDENT, "yes");
			outFormat.setProperty(OutputKeys.METHOD, "xml");
			outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			outFormat.setProperty(OutputKeys.VERSION, "1.0");
			outFormat.setProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperties(outFormat);
			DOMSource domSource = new DOMSource(doc.getDocumentElement());
			OutputStream output = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(output);
			transformer.transform(domSource, result);
			xmlString = output.toString();
		}
    	catch (Exception e) {
			// TODO: handle exception
    		return null;
		}
        return xmlString;
	}

}
