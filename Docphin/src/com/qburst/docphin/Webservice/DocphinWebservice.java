package com.qburst.docphin.Webservice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;
import android.util.Log;

import com.qburst.docphin.app.DocphinApiConstants;
import com.qburst.docphin.app.DocphinApp;
import com.qburst.docphin.easyssl.EasySSLSocketFactory;
import com.qburst.docphin.utilities.DocphinUtilities;

public class DocphinWebservice extends AsyncTask<String, Void, String>{
	


    private String _postParams = null;
    private static DefaultHttpClient _httpClient = null;
    private HttpResponse _httpResponse = null;
    private DocphinWebserviceListener _apiListener;
    private int _httpStatus = 0;


    public DocphinWebservice( String postParams, DocphinWebserviceListener apiListener)
    {
        this._apiListener = apiListener;
        this._postParams = postParams;
        this.acceptUntrustedCertificates();
    }
    
    private void acceptUntrustedCertificates()
    {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(),
                443));

        HttpParams params = new BasicHttpParams();
        int timeoutConnection = 5000;
        HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
        int timeoutSocket = 10000;
        HttpConnectionParams.setSoTimeout(params, timeoutSocket);

        params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
        params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
                new ConnPerRouteBean(30));
        params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

        ClientConnectionManager cm =
                new SingleClientConnManager(null, schemeRegistry);
//        _httpClient =
//                new TrustedDefaultHttpClient(cm, params, DocphinApp
//                        .getSharedApplication().getApplicationContext());
        _httpClient = new DefaultHttpClient();
        
    }

    
    protected String doInBackground(String... urls)
    {
        String serverResponse = null;
        Log.d("Url", _apiListener.getSoapURL());
        if (DocphinUtilities.isNetworkAvailable(DocphinApp.getSharedApplication()
                .getApplicationContext())) {
            try {
                if (_postParams == null) {
                        _httpResponse = this.doJsonGetRequest();                    
                } else {
                    // POST
                    _httpResponse = this.doJsonPostRequest();
                }

                if (_httpResponse != null) {
                    serverResponse =
                            DocphinUtilities.convertStreamToString(_httpResponse
                                    .getEntity().getContent());
                    _httpStatus = _httpResponse.getStatusLine().getStatusCode();
                } else {
                    serverResponse = DocphinApiConstants.kApiError;
                }
            } catch (ClientProtocolException e) {
                serverResponse = DocphinApiConstants.kApiError;
                e.printStackTrace();
            } catch (IOException e) {
                serverResponse = DocphinApiConstants.kApiError;
                e.printStackTrace();
            }
        } else {
            serverResponse = DocphinApiConstants.kNetworkError;
        }
        return serverResponse;
    }

    @Override
    protected void onPostExecute(String serverResponse)
    {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put(DocphinApiConstants.kHttpStatusKey, _httpStatus);
        Log.i("serverResponse", serverResponse);
        if (serverResponse.equalsIgnoreCase(DocphinApiConstants.kNetworkError)) {

            
            _apiListener.WebserviceFailedWithError(serverResponse, _httpStatus);

        } else if (serverResponse != null
                && (_httpStatus == DocphinApiConstants.kHttpStatusOK || _httpStatus == DocphinApiConstants.kHttpStatusCreated)) {

            _apiListener.WebserviceFinishedWithResponse(serverResponse, _httpStatus);
        }else if (_httpStatus == 401) {
            _apiListener.WebserviceFailedWithError(DocphinApiConstants.kAuthenticationErrorExceptionKey, _httpStatus);

        } else if (_httpStatus == 500) {
            _apiListener.WebserviceFailedWithError(DocphinApiConstants.kInternalServerErrorMessage,_httpStatus);

        } else if (serverResponse.equalsIgnoreCase(DocphinApiConstants.kApiError)) {
            _apiListener.WebserviceFailedWithError(serverResponse, _httpStatus);

        } else {
            _apiListener.WebserviceFinishedWithResponse(serverResponse, _httpStatus);
        }

    }    

    private HttpResponse doJsonPostRequest()
            throws ClientProtocolException
    {

        HttpResponse httpResponse = null;

        try {
            HttpConnectionParams.setConnectionTimeout(_httpClient.getParams(),

            		DocphinApiConstants.kRequestTimeOutInMills);
            HttpConnectionParams.setSoTimeout(_httpClient.getParams(),
            		DocphinApiConstants.kRequestTimeOutInMills);

            HttpPost httpPost = new HttpPost(DocphinApiConstants.BASE_URL);

            // Add post body.
            Log.d("method", httpPost.getMethod());
            

            StringEntity se = new StringEntity(_postParams, HTTP.UTF_8);
            httpPost.setHeader("Content-Type", "text/xml; charset=utf-8");
            httpPost.setHeader("SOAPAction", _apiListener.getSoapURL());
            
            se.setContentType("text/xml");

            httpPost.setEntity(se);

            // Execute the request
            httpResponse = _httpClient.execute(httpPost);
            Log.i("response",httpResponse.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
        Log.d("reached", httpResponse.toString());
        return httpResponse;
    }
    
    private HttpResponse doJsonGetRequest()
            throws ClientProtocolException
    {

        HttpResponse httpResponse = null;

        try {
            HttpConnectionParams.setConnectionTimeout(_httpClient.getParams(),

            		DocphinApiConstants.kRequestTimeOutInMills);
            HttpConnectionParams.setSoTimeout(_httpClient.getParams(),
            		DocphinApiConstants.kRequestTimeOutInMills);

            HttpGet httpGet = new HttpGet(DocphinApiConstants.BASE_URL);

            // Add post body.
            Log.d("method", httpGet.getMethod());
            
            // Execute the request
            httpResponse = _httpClient.execute(httpGet);
            Log.i("response",httpResponse.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
        Log.d("reached", httpResponse.toString());
        return httpResponse;
    }

}
