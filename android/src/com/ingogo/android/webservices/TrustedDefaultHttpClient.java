package com.ingogo.android.webservices;


import java.io.InputStream;
import java.security.KeyStore;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.HttpParams;

import android.content.Context;

import com.ingogo.android.R;

public class TrustedDefaultHttpClient extends DefaultHttpClient {

  final Context context;

  public TrustedDefaultHttpClient(ClientConnectionManager connManager, HttpParams params, Context context) {
	  
	  super(connManager, params);
	  setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
	  this.context = context;
  }
  
  public TrustedDefaultHttpClient(Context context) {
	  setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));

	  this.context = context;
  }

  @Override protected ClientConnectionManager createClientConnectionManager() {
    SchemeRegistry registry = new SchemeRegistry();
    registry.register(
        new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    registry.register(new Scheme("https", newSslSocketFactory(), 443));
    return new SingleClientConnManager(getParams(), registry);
  }

  private SSLSocketFactory newSslSocketFactory() {
    try {
      KeyStore trusted = KeyStore.getInstance("BKS");
      InputStream in = context.getResources().openRawResource(R.raw.mystore);
      try {
        trusted.load(in, "mysecret".toCharArray());
      } finally { 
        in.close();
      }
      return new SSLSocketFactory(trusted);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }
}
