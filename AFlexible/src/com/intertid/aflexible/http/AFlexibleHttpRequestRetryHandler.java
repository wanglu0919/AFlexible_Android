package com.intertid.aflexible.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.os.SystemClock;

/**
 * 错误尝试连接处理类
 * 
 * @author wanglu 泰得利通
 * 
 */
public class AFlexibleHttpRequestRetryHandler implements
		HttpRequestRetryHandler {

	private static final int RETRY_SLEEP_TIME_MILLIS = 1000;// 尝试连接时睡眠时间
	private final int maxRetries;// 尝试次数
	private static HashSet<Class<?>> exceptionContinueList = new HashSet<Class<?>>();// 继续尝试
	private static HashSet<Class<?>> exceptionBlreaklist = new HashSet<Class<?>>();// 中断异常

	static {

		exceptionContinueList.add(NoHttpResponseException.class);
		exceptionContinueList.add(UnknownHostException.class);
		exceptionContinueList.add(SocketException.class);

		exceptionBlreaklist.add(InterruptedIOException.class);
		exceptionBlreaklist.add(SSLHandshakeException.class);

	}

	public AFlexibleHttpRequestRetryHandler(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Override
	public boolean retryRequest(IOException exception, int executionCount,
			HttpContext context) {
		boolean retry = true;
		Boolean b = (Boolean) context
				.getAttribute(ExecutionContext.HTTP_REQ_SENT);
		boolean sent = (b != null && b.booleanValue());

		if (executionCount > maxRetries) {
			// 尝试次数超过用户定义的测试，默认5次
			retry = false;
		} else if (exceptionBlreaklist.contains(exception.getClass())) {
			// 线程被用户中断，则不继续尝试
			retry = false;
		} else if (exceptionContinueList.contains(exception.getClass())) {// 继续尝试连接
			retry = true;
		} else if (!sent) {
			retry = true;
		}

		if (retry) {
			HttpUriRequest currentReq = (HttpUriRequest) context
					.getAttribute(ExecutionContext.HTTP_REQUEST);
			retry = currentReq != null
					&& !"POST".equals(currentReq.getMethod());// 不是post 继续尝试连接
		}

		if (retry) {
			// 休眠1秒钟后再继续尝试
			SystemClock.sleep(RETRY_SLEEP_TIME_MILLIS);
		} else {
			exception.printStackTrace();//打印异常
		}

		return retry;

	}

}
