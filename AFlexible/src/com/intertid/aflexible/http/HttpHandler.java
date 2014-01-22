package com.intertid.aflexible.http;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import android.os.SystemClock;

import com.intertid.aflexible.exception.AFlexibleException;
import com.intertid.aflexible.http.entityhandler.EntityCallBack;
import com.intertid.aflexible.http.entityhandler.FileEntityHandler;
import com.intertid.aflexible.http.entityhandler.StringEntityHandler;

public class HttpHandler<T> extends AsyncTask<Object, Object, Object> implements
		EntityCallBack {

	private final static int UPDATE_START = 1;// 开始
	private final static int UPDATE_LOADING = 2;// 加载中
	private final static int UPDATE_FAILURE = 3;// 失败
	private final static int UPDATE_SUCCESS = 4;// 成功
	private final AbstractHttpClient client;
	private final HttpContext context;
	private int executionCount = 0;// 执行次数
	private final HttpResposeCallBack<T> callback;
	private String charset;
	private long time;
	private String filePath = null; // 下载保存文件的路径
	private boolean isDownLoadResume = false; // 是否断点续传
	private final StringEntityHandler mStrEntityHandler = new StringEntityHandler();
	private final FileEntityHandler mFileEntityHandler = new FileEntityHandler();

	public HttpHandler(AbstractHttpClient client, HttpContext context,
			HttpResposeCallBack<T> callback, String charset) {
		this.client = client;
		this.context = context;
		this.callback = callback;
		this.charset = charset;

	}

	private void makeRequestWithRetries(HttpUriRequest request)
			throws IOException {
		if (isDownLoadResume && filePath != null) {
			File downloadFile = new File(filePath);
			long fileLen = 0;
			if (downloadFile.isFile() && downloadFile.exists()) {
				fileLen = downloadFile.length();
			}
			if (fileLen > 0)
				request.setHeader("RANGE", "bytes=" + fileLen + "-");
		}

		boolean retry = true;
		IOException cause = null;
		HttpRequestRetryHandler retryHandler = client
				.getHttpRequestRetryHandler();
		while (retry) {
			try {
				if (!isCancelled()) {
					HttpResponse response = client.execute(request, context);
					if (!isCancelled()) {
						handleResponse(response);
					}
				}
				return;
			} catch (UnknownHostException e) {
				publishProgress(UPDATE_FAILURE, e, 0,
						"unknownHostException：can't resolve host");
				return;
			} catch (IOException e) {
				cause = e;
				retry = retryHandler.retryRequest(cause, ++executionCount,
						context);
			} catch (NullPointerException e) {
				// HttpClient 4.0.x 之前的一个bug
				// http://code.google.com/p/android/issues/detail?id=5255
				cause = new IOException("NPE in HttpClient" + e.getMessage());
				retry = retryHandler.retryRequest(cause, ++executionCount,
						context);
			} catch (Exception e) {
				cause = new IOException("Exception" + e.getMessage());
				retry = retryHandler.retryRequest(cause, ++executionCount,
						context);
			}
		}
		if (cause != null)
			throw cause;
		else
			throw new IOException("未知网络错误");
	}

	/**
	 * 处理返回请求 wanglu 泰得利通
	 * 
	 * @param response
	 */
	private void handleResponse(HttpResponse response) {
		StatusLine status = response.getStatusLine();
		if (status.getStatusCode() >= 300) {// 访问出现错误
			String errorMsg = "response status error code:"
					+ status.getStatusCode();
			if (status.getStatusCode() == 416 && isDownLoadResume) {
				errorMsg += " \n maybe you have download complete.";
			}
			publishProgress(
					UPDATE_FAILURE,
					new HttpResponseException(status.getStatusCode(), status
							.getReasonPhrase()), status.getStatusCode(),
					errorMsg);
		} else {
			try {
				HttpEntity entity = response.getEntity();
				Object responseBody = null;
				if (entity != null) {
					time = SystemClock.uptimeMillis();
					if (filePath != null) {
						responseBody = mFileEntityHandler.handleEntity(entity,
								this, filePath, isDownLoadResume);
					} else { // 字符型串返回处理
						if (callback.getResponseDataPaser() != null) {
							responseBody = mStrEntityHandler.handleEntity(
									entity, this, charset,
									callback.getResponseDataPaser());
						} else {
							responseBody = mStrEntityHandler.handleEntity(
									entity, this, charset);
						}

					}

				}

				publishProgress(UPDATE_SUCCESS, responseBody);

			} catch (IOException e) {
				publishProgress(UPDATE_FAILURE, e, 0, e.getMessage());
			} catch (AFlexibleException e) {
				publishProgress(UPDATE_FAILURE, e, 0, e.getMessage());
				e.printStackTrace();
			}

		}
	}

	public boolean isStopDownload() {
		return mFileEntityHandler.isStop();
	}

	/**
	 * @param stop
	 *            停止下载任务
	 */
	public void stopDownload() {
		mFileEntityHandler.setStop(true);
	}

	@Override
	protected Object doInBackground(Object... params) {

		if (params != null && params.length == 3) {

			filePath = String.valueOf(params[1]);
			isDownLoadResume = (Boolean) params[2];

		}

		try {
			publishProgress(UPDATE_START); // 开始
			makeRequestWithRetries((HttpUriRequest) params[0]);// 开始任务

		} catch (IOException e) {
			publishProgress(UPDATE_FAILURE, e, 0, e.getMessage()); // 结束
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	protected void onProgressUpdate(Object... values) {
		int update = Integer.valueOf(String.valueOf(values[0]));
		switch (update) {
		case UPDATE_START:
			if (callback != null)
				callback.onStart();
			break;
		case UPDATE_LOADING:
			if (callback != null)
				callback.onLoading(Long.valueOf(String.valueOf(values[1])),
						Long.valueOf(String.valueOf(values[2])));
			break;
		case UPDATE_FAILURE:
			if (callback != null)
				callback.onFailure((Throwable) values[1], (Integer) values[2],
						(String) values[3]);
			break;
		case UPDATE_SUCCESS:
			if (callback != null)
				callback.onSuccess((T) values[1]);
			break;
		default:
			break;
		}
		super.onProgressUpdate(values);
	}

	@Override
	public void callBack(long count, long current, boolean mustNoticeUI) {

		if (callback != null && callback.isProgress()) {
			if (mustNoticeUI) {
				publishProgress(UPDATE_LOADING, count, current);
			} else {
				long thisTime = SystemClock.uptimeMillis();
				if (thisTime - time >= callback.getRate()) {
					time = thisTime;
					publishProgress(UPDATE_LOADING, count, current);
				}
			}
		}

	}

}
