package com.ingogo.android.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.os.Bundle;
import android.test.InstrumentationTestRunner;
import android.util.Log;

public class CustomTestRunner extends InstrumentationTestRunner {

	private Writer mWriter;
    private XmlSerializer mTestSuiteSerializer;
    private long mTestStarted;
    private static final String JUNIT_TEST_FILE = "TEST_REPORT";
    Bundle failedTestReasons = new Bundle();
    Bundle failedTestClasses = new Bundle();
    ArrayList<String> failedTests = new ArrayList<String>();
    
    
    @Override
    public void onStart() {
        try {
        	
        	Log.i("CustomTestRunner", "OnStart");
            startJUnitOutput(new FileWriter(new File(getContext().getExternalFilesDir("RobotiumTestReport"), getFileName())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.onStart();
    }

    void startJUnitOutput(Writer writer) {
        try {
            mWriter = writer;
            mTestSuiteSerializer = newSerializer(mWriter);
            mTestSuiteSerializer.startDocument(null, null);
            mTestSuiteSerializer.startTag(null, "html");
            mTestSuiteSerializer.startTag(null, "head");
            mTestSuiteSerializer.startTag(null, "Title");
            mTestSuiteSerializer.text("Ingogo test report");
            mTestSuiteSerializer.endTag(null, "Title");
            mTestSuiteSerializer.endTag(null, "head");
            mTestSuiteSerializer.startTag(null, "body");
            
            mTestSuiteSerializer.startTag(null, "h1");
			mTestSuiteSerializer.text("Ingogo test report");
			mTestSuiteSerializer.endTag(null, "h1");
			
			mTestSuiteSerializer.startTag(null, "p");
			mTestSuiteSerializer.text("Device : " + getDevice());
			mTestSuiteSerializer.endTag(null, "p");
			
			mTestSuiteSerializer.startTag(null, "p");
			mTestSuiteSerializer.text("Time : " + getTime());
			mTestSuiteSerializer.endTag(null, "p");
			
            mTestSuiteSerializer.startTag(null, "table");
            mTestSuiteSerializer.attribute(null, "border", "1");
            
            mTestSuiteSerializer.startTag(null, "tr");
            mTestSuiteSerializer.startTag(null, "th");
            mTestSuiteSerializer.text("Class");
            mTestSuiteSerializer.endTag(null, "th");
            
            mTestSuiteSerializer.startTag(null, "th");
            mTestSuiteSerializer.text("Testcase");
            mTestSuiteSerializer.endTag(null, "th");
            
            mTestSuiteSerializer.startTag(null, "th");
            mTestSuiteSerializer.text("Status");
            mTestSuiteSerializer.endTag(null, "th");
            
            mTestSuiteSerializer.startTag(null, "th");
            mTestSuiteSerializer.text("Time(seconds)");
            mTestSuiteSerializer.endTag(null, "th");
            
            mTestSuiteSerializer.endTag(null, "tr");
            
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private XmlSerializer newSerializer(Writer writer) {
        try {
            XmlPullParserFactory pf = XmlPullParserFactory.newInstance();
            XmlSerializer serializer = pf.newSerializer();
            serializer.setOutput(writer);
            return serializer;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        
    }
    
    @Override
    public void sendStatus(int resultCode, Bundle results) {
    	
    	Log.i("CustomTestRunner", "sendStatus");
        super.sendStatus(resultCode, results);
        switch (resultCode) {
            case REPORT_VALUE_RESULT_ERROR:
            case REPORT_VALUE_RESULT_FAILURE:
            case REPORT_VALUE_RESULT_OK:
            try {
                recordTestResult(resultCode, results);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case REPORT_VALUE_RESULT_START:
                recordTestStart(results);
            default:
                break;
        }
    }
    
    void recordTestStart(Bundle results) {
        mTestStarted = System.currentTimeMillis();
    }

    void recordTestResult(int resultCode, Bundle results) throws IOException {
        float time = (System.currentTimeMillis() - mTestStarted) / 1000.0f;
        String className = results.getString(REPORT_KEY_NAME_CLASS);
        String testMethod = results.getString(REPORT_KEY_NAME_TEST);
        String stack = results.getString(REPORT_KEY_STACK);
        int current = results.getInt(REPORT_KEY_NUM_CURRENT);
        int total = results.getInt(REPORT_KEY_NUM_TOTAL);
        
//        mTestSuiteSerializer.startTag(null, "testcase");
//        mTestSuiteSerializer.attribute(null, "classname", className);
//        mTestSuiteSerializer.attribute(null, "name", testMethod);
        
        mTestSuiteSerializer.startTag(null, "tr");
        mTestSuiteSerializer.startTag(null, "td");
        mTestSuiteSerializer.text(className);
        mTestSuiteSerializer.endTag(null, "td");
        
        mTestSuiteSerializer.startTag(null, "td");
        mTestSuiteSerializer.text(testMethod);
        mTestSuiteSerializer.endTag(null, "td");
        
        if (resultCode != REPORT_VALUE_RESULT_OK) {
//            mTestSuiteSerializer.startTag(null, "failure");
        	mTestSuiteSerializer.startTag(null, "td");
        	mTestSuiteSerializer.startTag(null, "a");
        	mTestSuiteSerializer.attribute(null, "href", "#"+testMethod);
            mTestSuiteSerializer.text("failed");
            mTestSuiteSerializer.endTag(null, "a");
            mTestSuiteSerializer.endTag(null, "td");
            if (stack != null) {
                String reason = stack.substring(0, stack.indexOf('\n'));
                
                failedTests.add(testMethod);
                failedTestClasses.putString(testMethod, className);
                failedTestReasons.putString(testMethod, stack);
                
//                String message = "";
//                int index = reason.indexOf(':');
//                if (index > -1) {
//                    message = reason.substring(index+1);
//                    reason = reason.substring(0, index);
//                }
//                mTestSuiteSerializer.attribute(null, "message", message);
//                mTestSuiteSerializer.attribute(null, "type", reason);
//                mTestSuiteSerializer.text(stack);
                
                
                
            }
//            mTestSuiteSerializer.endTag(null, "failure");
        } else {
        	mTestSuiteSerializer.startTag(null, "td");
            mTestSuiteSerializer.text("success");
            mTestSuiteSerializer.endTag(null, "td");
//            mTestSuiteSerializer.attribute(null, "time", String.format("%.3f", time));
        }
        mTestSuiteSerializer.startTag(null, "td");
        mTestSuiteSerializer.text(String.format("%.3f", time));
        mTestSuiteSerializer.endTag(null, "td");
        mTestSuiteSerializer.endTag(null, "tr");        
//        if (current == total) {
//            mTestSuiteSerializer.startTag(null, "system-out");
//            mTestSuiteSerializer.endTag(null, "system-out");
//            mTestSuiteSerializer.startTag(null, "system-err");
//            mTestSuiteSerializer.endTag(null, "system-err");
//            mTestSuiteSerializer.endTag(null, "testsuite");
//            mTestSuiteSerializer.flush();
//        }
    }

    @Override
    public void finish(int resultCode, Bundle results) {
    	Log.i("CustomTestRunner", "finish");
        endTestSuites();
        super.finish(resultCode, results);
    }

    void endTestSuites() {
        try {
            mTestSuiteSerializer.endTag(null, "table");
			if (failedTests.size() > 0) {
				mTestSuiteSerializer.startTag(null, "h1");
				mTestSuiteSerializer.text("Failure details");
				mTestSuiteSerializer.endTag(null, "h1");
				
				mTestSuiteSerializer.startTag(null, "table");
	            mTestSuiteSerializer.attribute(null, "border", "1");
	            //mTestSuiteSerializer.attribute(null, "style", "width:578px;");
	            
	            mTestSuiteSerializer.startTag(null, "tr");
	            mTestSuiteSerializer.startTag(null, "th");
	            mTestSuiteSerializer.text("Test");
	            mTestSuiteSerializer.endTag(null, "th");
	            
	            mTestSuiteSerializer.startTag(null, "th");
	            mTestSuiteSerializer.text("Failure reason");
	            mTestSuiteSerializer.endTag(null, "th");
	            
	            mTestSuiteSerializer.endTag(null, "tr");
				
				String failedtestname;
				int j;
				for (int i = 0; i < failedTests.size(); i++) {

					j = i+1;
					failedtestname = failedTests.get(i);
					// mTestSuiteSerializer.startTag(null, "br/");
					
					mTestSuiteSerializer.startTag(null, "tr");
					mTestSuiteSerializer.attribute(null, "id", failedtestname);
		            mTestSuiteSerializer.startTag(null, "td");
		            mTestSuiteSerializer.text(failedtestname);
		            mTestSuiteSerializer.endTag(null, "td");
		            
		            mTestSuiteSerializer.startTag(null, "td");
		            mTestSuiteSerializer.text(failedTestReasons.getString(failedtestname));
		            mTestSuiteSerializer.endTag(null, "td");
		            
		            mTestSuiteSerializer.endTag(null, "tr");
					
//					mTestSuiteSerializer.startTag(null, "p");
//					mTestSuiteSerializer.attribute(null, "id", failedtestname);
//					mTestSuiteSerializer.text("Failed test-" + j + " : " + failedtestname +", Class : "
//							+ failedTestClasses.getString(failedtestname)
//							+  ", Issue : "
//							+ failedTestReasons.getString(failedtestname));
//					mTestSuiteSerializer.endTag(null, "p");

				}
				
				mTestSuiteSerializer.endTag(null, "table");
				
			}
            
			mTestSuiteSerializer.startTag(null, "p");
			mTestSuiteSerializer.attribute(null, "style", " bottom: 0; left: 0; width: 100%; text-align: center;");
			mTestSuiteSerializer.text("Copyright © 2013 Qburst Technologies");
			mTestSuiteSerializer.endTag(null, "p");
            mTestSuiteSerializer.endTag(null, "body");
            mTestSuiteSerializer.endTag(null, "html");
            mTestSuiteSerializer.endDocument();
            mTestSuiteSerializer.flush();
            mWriter.flush();
            mWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getFileName() {
//		return JUNIT_TEST_FILE + getDevice() + getTime() + ".html";
//		return JUNIT_TEST_FILE + getTime() + ".html";
    	return JUNIT_TEST_FILE + ".html";
	}
    
    public String getDevice() {
		return android.os.Build.MODEL;
	}
    
    public String getTime() {
    	SimpleDateFormat datetimeformat = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");   
		datetimeformat.setTimeZone(TimeZone.getTimeZone("GMT")); 
	    String datetime = datetimeformat.format(new Date(System.currentTimeMillis()));
	    
	    return datetime;
	}

}
