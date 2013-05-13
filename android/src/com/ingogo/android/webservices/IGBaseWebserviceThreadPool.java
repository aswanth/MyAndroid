package com.ingogo.android.webservices;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.entity.mime.MultipartEntity;

import android.util.Log;

import com.ingogo.android.app.IGConstants;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.beans.response.IGBaseResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiListener;

public class IGBaseWebserviceThreadPool extends ThreadPoolExecutor {

	private static final int CORE_POOL_SIZE = 2;
	private static final int MAXIMUM_POOL_SIZE = 4;
	private static final int KEEP_ALIVE = 1;

	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(
			10);
	private boolean isPaused;
	private ReentrantLock pauseLock = new ReentrantLock();
	private Condition unpaused = pauseLock.newCondition();

	private static ThreadFactory threadFactory = new ThreadFactory() {
		
		@Override
		public Thread newThread(Runnable r) {
			// TODO Auto-generated method stub
			
			Thread thread = new Thread(r);
			thread.setPriority(Thread.MIN_PRIORITY);
			return thread;
		}
	};
	private static IGBaseWebserviceThreadPool threadPool = null;

	public static IGBaseWebserviceThreadPool getSharedInstance() {
		if (threadPool == null) {
			threadPool = new IGBaseWebserviceThreadPool();
		}
		return threadPool;
	}

	public void addWerbserviceTask(String url,
			Class<? extends IGBaseResponseBean> responseClass, String params,
			IGApiListener listener) {
		if (url != null) {
			List<String> items = Arrays.asList(url.split(("/")));
			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceReqCall,
					items.get(items.size() - 1));
		}

		IGBaseWebserviceRunnable runnable = new IGBaseWebserviceRunnable(
				listener, params, responseClass, url);
		runnable.setFileEntity(null);
		IGBaseWebserviceThreadPool.getSharedInstance().execute(runnable);
	}
	
	public void addWerbserviceTask(String url,
			Class<? extends IGBaseResponseBean> responseClass, String params,
			 String mobileNumber, String password, IGApiListener listener) { // if you need to set authentication params
		if (url != null) {
			List<String> items = Arrays.asList(url.split(("/")));
			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceReqCall,
					items.get(items.size() - 1));
		}

		IGBaseWebserviceRunnable runnable = new IGBaseWebserviceRunnable(
				listener, params, responseClass, url);
		runnable.setAuthenticationParams(mobileNumber, password);
		runnable.setFileEntity(null);
		IGBaseWebserviceThreadPool.getSharedInstance().execute(runnable);
	}
	public void addWerbserviceTask(String url,
			Class<? extends IGBaseResponseBean> responseClass, String params,
			MultipartEntity fileEntity, IGApiListener listener) { // if you need to set authentication params
		if (url != null) {
			List<String> items = Arrays.asList(url.split(("/")));
			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceReqCall,
					items.get(items.size() - 1));
		}

		IGBaseWebserviceRunnable runnable = new IGBaseWebserviceRunnable(
				listener, params, responseClass, url);
		runnable.setFileEntity(fileEntity);
		IGBaseWebserviceThreadPool.getSharedInstance().execute(runnable);
	}

	public void pause() {
		pauseLock.lock();
		try {
			isPaused = true;
		} finally {
			pauseLock.unlock();
		}
	}

	public void resume() {
		pauseLock.lock();
		try {
			isPaused = false;
			unpaused.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);

		pauseLock.lock();
		try {
			while (isPaused)
				unpaused.await();
		} catch (InterruptedException ie) {
			t.interrupt();
		} finally {
			pauseLock.unlock();
		}
	}

	public void shutDownThreadPool() {
		shutdownNow();
		threadPool = null;
	}
	@Override
	public void execute(Runnable command) {
		// TODO Auto-generated method stub
		super.execute(command);
	}

	public IGBaseWebserviceThreadPool() {

		super(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
				sPoolWorkQueue,threadFactory, new RejectedExecutionHandler() {

					@Override
					public void rejectedExecution(Runnable r,
							ThreadPoolExecutor executor) {
						Log.e("API CALL", "REJECTED HANDLER CALLED");
						try {
							executor.getQueue().put(r);
						} catch (InterruptedException e1) {
						}

					}
				});

	}

}
