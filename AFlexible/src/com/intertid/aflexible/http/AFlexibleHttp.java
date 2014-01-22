package com.intertid.aflexible.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;

/**
 * 
 * @author wanglu 泰得利通
 * 
 */
public class AFlexibleHttp {
	private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8 * 1024; // 8KB
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";// 接收变类型
	private static final String ENCODING_GZIP = "gzip";// 编码

	private static int perMaxConnections = 10; // 每个路由最大 http请求最大并发连接数
	private static int maxConnection = 10;// http最大连接数据
	private static int socketTimeout = 10 * 1000; // 超时时间，默认10秒
	private static int maxRetries = 5;// 错误尝试次数，错误异常表请在RetryHandler添加
	private static int httpThreadCount = 3;// http线程池数量

	private final DefaultHttpClient httpClient;// httpClient
	private final HttpContext httpContext;// http上下文
	private String charset = "utf-8";// 编码

	private final Map<String, String> clientHeaderMap;// 头部

	private static final ThreadFactory mSThreadFactory = new ThreadFactory() {

		private final AtomicInteger atomicInteger = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r, "AFlexible#"
					+ atomicInteger.getAndIncrement() + "");
			thread.setPriority(Thread.NORM_PRIORITY - 1);

			return thread;
		}
	};

	private static final Executor executor = Executors.newFixedThreadPool(
			httpThreadCount, mSThreadFactory);

	public AFlexibleHttp() {
		clientHeaderMap = new HashMap<String, String>();//
		BasicHttpParams basicHttpParams = new BasicHttpParams();

		ConnManagerParams.setTimeout(basicHttpParams, socketTimeout);// 设置socket连接超时时间
		ConnManagerParams.setMaxConnectionsPerRoute(basicHttpParams,
				new ConnPerRouteBean(perMaxConnections));// 设置每个路由最大并发连接数
		ConnManagerParams
				.setMaxTotalConnections(basicHttpParams, maxConnection);// 设置最大连接数

		HttpConnectionParams.setSoTimeout(basicHttpParams, socketTimeout);// 设置http超时时间
		HttpConnectionParams.setConnectionTimeout(basicHttpParams,
				socketTimeout);// 设置socket连接超时时间
		HttpConnectionParams.setTcpNoDelay(basicHttpParams, true);
		HttpConnectionParams.setSocketBufferSize(basicHttpParams,
				DEFAULT_SOCKET_BUFFER_SIZE);// 设置socket读取数据缓存

		HttpProtocolParams.setVersion(basicHttpParams, HttpVersion.HTTP_1_1);// 设置协议版本

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));// 注册http协议模式
		schemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));

		ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager(
				basicHttpParams, schemeRegistry);

		httpContext = new SyncBasicHttpContext(new BasicHttpContext());// http上下文实力话
																		// //同步http上下文
		httpClient = new DefaultHttpClient(threadSafeClientConnManager,
				basicHttpParams);
		httpClient.addRequestInterceptor(new HttpRequestInterceptor() {// 设置httpClinet请求拦截器

					@Override
					public void process(HttpRequest request, HttpContext context)
							throws HttpException, IOException {

						if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
							request.addHeader(HEADER_ACCEPT_ENCODING,
									ENCODING_GZIP);// 加入压缩编码头部
						}

						for (String header : clientHeaderMap.keySet()) {

							request.addHeader(header,
									clientHeaderMap.get(header));// 添加http请求头

						}

					}
				});

		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {// 添加http返回拦截器

					@Override
					public void process(HttpResponse response,
							HttpContext context) throws HttpException,
							IOException {

						final HttpEntity httpEntity = response.getEntity();
						if (httpEntity == null) {
							return;

						}
						final Header encoding = httpEntity.getContentEncoding();// 编码
																				// Content-Encoding
																				// 头

						if (encoding != null) {

							for (HeaderElement element : encoding.getElements()) {

								if (element.getName().equalsIgnoreCase(
										ENCODING_GZIP)) {// 返回中包含了Gzip压缩编码

									response.setEntity(new InflatingEntity(
											httpEntity));
									break;

								}
							}

						}

					}
				});

		// 设置错误尝试处理器
		httpClient
				.setHttpRequestRetryHandler(new AFlexibleHttpRequestRetryHandler(
						maxRetries));

	}

	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	public HttpContext getHttpContext() {
		return this.httpContext;
	}

	public void configCharset(String charSet) {
		if (charSet != null && charSet.trim().length() != 0) {
			this.charset = charSet;
		}

	}

	public void configCookieStore(CookieStore cookieStore) {
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

	}

	public void configUserAgent(String userAgent) {
		HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
	}

	/**
	 * 设置网络连接超时时间，默认为10秒钟
	 * 
	 * @param timeout
	 */
	public void configTimeout(int timeout) {
		final HttpParams httpParams = this.httpClient.getParams();
		ConnManagerParams.setTimeout(httpParams, timeout);
		HttpConnectionParams.setSoTimeout(httpParams, timeout);
		HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
	}

	/**
	 * 设置https请求时 的 SSLSocketFactory
	 * 
	 * @param sslSocketFactory
	 */
	public void configSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
		Scheme scheme = new Scheme("https", sslSocketFactory, 443);
		this.httpClient.getConnectionManager().getSchemeRegistry()
				.register(scheme);
	}

	/**
	 * 添加http请求头
	 * 
	 * @param header
	 * @param value
	 */
	public void addHeader(String header, String value) {
		clientHeaderMap.put(header, value);
	}

	/************* get请求 *****************/

	public void get(String url,
			HttpResposeCallBack<? extends Object> httpResposeCallBack) {
		get(url, null, httpResposeCallBack);
	}

	public void get(String url, RequestParams requestParams,
			HttpResposeCallBack<? extends Object> httpResposeCallBack) {
		sendRequest(httpClient, httpContext,
				new HttpGet(getUrlWithQueryString(url, requestParams)), null, httpResposeCallBack);
	}

	public void get(String url, Header[] headers, RequestParams requestParams,
			HttpResposeCallBack<? extends Object> httpResposeCallBack) {
		HttpUriRequest request = new HttpGet(getUrlWithQueryString(url,
				requestParams));
		if (headers != null)
			request.setHeaders(headers);
		sendRequest(httpClient, httpContext, request, null, httpResposeCallBack);
	}

	public Object getSync(String url) {
		return getSync(url, null);
	}

	public Object getSync(String url, RequestParams params) {
		HttpUriRequest request = new HttpGet(getUrlWithQueryString(url, params));
		return sendSyncRequest(httpClient, httpContext, request, null);
	}

	public Object getSync(String url, Header[] headers, RequestParams params) {
		HttpUriRequest request = new HttpGet(getUrlWithQueryString(url, params));
		if (headers != null)
			request.setHeaders(headers);
		return sendSyncRequest(httpClient, httpContext, request, null);
	}

	// -------------------------------get结束---------------------------------//
	// --------------------------------Post请求------------------------------//
	public void post(String url, HttpResposeCallBack<? extends Object> callBack) {
		post(url, null, callBack);
	}

	public void post(String url, RequestParams params,
			HttpResposeCallBack<? extends Object> callBack) {
		post(url, paramsToEntity(params), null, callBack);
	}

	public void post(String url, HttpEntity entity, String contentType,
			HttpResposeCallBack<? extends Object> callBack) {
		sendRequest(httpClient, httpContext,
				addEntityToRequestBase(new HttpPost(url), entity), contentType,
				callBack);
	}

	public <T> void post(String url, Header[] headers, RequestParams params,
			String contentType, HttpResposeCallBack<T> callBack) {
		HttpEntityEnclosingRequestBase request = new HttpPost(url);
		if (params != null)
			request.setEntity(paramsToEntity(params));
		if (headers != null)
			request.setHeaders(headers);
		sendRequest(httpClient, httpContext, request, contentType, callBack);
	}

	public void post(String url, Header[] headers, HttpEntity entity,
			String contentType, HttpResposeCallBack<? extends Object> callBack) {
		HttpEntityEnclosingRequestBase request = addEntityToRequestBase(
				new HttpPost(url), entity);
		if (headers != null)
			request.setHeaders(headers);
		sendRequest(httpClient, httpContext, request, contentType, callBack);
	}

	public Object postSync(String url) {
		return postSync(url, null);
	}

	public Object postSync(String url, RequestParams params) {
		return postSync(url, paramsToEntity(params), null);
	}

	public Object postSync(String url, HttpEntity entity, String contentType) {
		return sendSyncRequest(httpClient, httpContext,
				addEntityToRequestBase(new HttpPost(url), entity), contentType);
	}

	public Object postSync(String url, Header[] headers, RequestParams params,
			String contentType) {
		HttpEntityEnclosingRequestBase request = new HttpPost(url);
		if (params != null)
			request.setEntity(paramsToEntity(params));
		if (headers != null)
			request.setHeaders(headers);
		return sendSyncRequest(httpClient, httpContext, request, contentType);
	}

	public Object postSync(String url, Header[] headers, HttpEntity entity,
			String contentType) {
		HttpEntityEnclosingRequestBase request = addEntityToRequestBase(
				new HttpPost(url), entity);
		if (headers != null)
			request.setHeaders(headers);
		return sendSyncRequest(httpClient, httpContext, request, contentType);
	}

	// -----------------------POST方法结束-------------------------------//

	// ------------------put 请求-----------------------

	public void put(String url, HttpResposeCallBack<? extends Object> callBack) {
		put(url, null, callBack);
	}

	public void put(String url, RequestParams params,
			HttpResposeCallBack<? extends Object> callBack) {
		put(url, paramsToEntity(params), null, callBack);
	}

	public void put(String url, HttpEntity entity, String contentType,
			HttpResposeCallBack<? extends Object> callBack) {
		sendRequest(httpClient, httpContext,
				addEntityToRequestBase(new HttpPut(url), entity), contentType,
				callBack);
	}

	public void put(String url, Header[] headers, HttpEntity entity,
			String contentType, HttpResposeCallBack<? extends Object> callBack) {
		HttpEntityEnclosingRequestBase request = addEntityToRequestBase(
				new HttpPut(url), entity);
		if (headers != null)
			request.setHeaders(headers);
		sendRequest(httpClient, httpContext, request, contentType, callBack);
	}

	public Object putSync(String url) {
		return putSync(url, null);
	}

	public Object putSync(String url, RequestParams params) {
		return putSync(url, paramsToEntity(params), null);
	}

	public Object putSync(String url, HttpEntity entity, String contentType) {
		return putSync(url, null, entity, contentType);
	}

	public Object putSync(String url, Header[] headers, HttpEntity entity,
			String contentType) {
		HttpEntityEnclosingRequestBase request = addEntityToRequestBase(
				new HttpPut(url), entity);
		if (headers != null)
			request.setHeaders(headers);
		return sendSyncRequest(httpClient, httpContext, request, contentType);
	}

	// ------------------delete 请求-----------------------
	public void delete(String url,
			HttpResposeCallBack<? extends Object> callBack) {
		final HttpDelete delete = new HttpDelete(url);
		sendRequest(httpClient, httpContext, delete, null, callBack);
	}

	public void delete(String url, Header[] headers,
			HttpResposeCallBack<? extends Object> callBack) {
		final HttpDelete delete = new HttpDelete(url);
		if (headers != null)
			delete.setHeaders(headers);
		sendRequest(httpClient, httpContext, delete, null, callBack);
	}

	public Object deleteSync(String url) {
		return deleteSync(url, null);
	}

	public Object deleteSync(String url, Header[] headers) {
		final HttpDelete delete = new HttpDelete(url);
		if (headers != null)
			delete.setHeaders(headers);
		return sendSyncRequest(httpClient, httpContext, delete, null);
	}

	// ---------------------下载---------------------------------------
	public HttpHandler<File> download(String url, String filePath,
			HttpResposeCallBack<File> callback) {
		return download(url, null, filePath, false, callback);
	}

	public HttpHandler<File> download(String url, String filePath,
			boolean isResume, HttpResposeCallBack<File> callback) {
		return download(url, null, filePath, isResume, callback);
	}

	public HttpHandler<File> download(String url, RequestParams params,
			String target, HttpResposeCallBack<File> callback) {
		return download(url, params, target, false, callback);
	}

	public HttpHandler<File> download(String url, RequestParams params,
			String target, boolean isResume, HttpResposeCallBack<File> callback) {
		final HttpGet get = new HttpGet(getUrlWithQueryString(url, params));
		HttpHandler<File> handler = new HttpHandler<File>(httpClient,
				httpContext, callback, charset);
		handler.executeOnExecutor(executor, get, target, isResume);
		return handler;
	}

	protected <T> void sendRequest(DefaultHttpClient client,
			HttpContext httpContext, HttpUriRequest uriRequest,
			String contentType, HttpResposeCallBack<T> httpResposeCallBack) {
		if (contentType != null) {
			uriRequest.addHeader("Content-Type", contentType);
		}

		new HttpHandler<T>(client, httpContext, httpResposeCallBack, charset)
				.executeOnExecutor(executor, uriRequest);

	}

	/**
	 * 同步方法 wanglu 泰得利通
	 * 
	 * @param client
	 * @param httpContext
	 * @param uriRequest
	 * @param contentType
	 * @return
	 */
	protected Object sendSyncRequest(DefaultHttpClient client,
			HttpContext httpContext, HttpUriRequest uriRequest,
			String contentType) {
		if (contentType != null) {
			uriRequest.addHeader("Content-Type", contentType);
		}
		return new SyncRequestHandler(client, httpContext, charset)
				.sendRequest(uriRequest);
	}

	public static String getUrlWithQueryString(String url, RequestParams params) {
		if (params != null) {
			String paramString = params.getURLParamString();
			url += "?" + paramString;
		}
		return url;
	}

	/**
	 * 配置错误重试次数
	 * 
	 * @param retry
	 */
	public void configRequestExecutionRetryCount(int count) {
		this.httpClient
				.setHttpRequestRetryHandler(new AFlexibleHttpRequestRetryHandler(
						count));
	}

	private HttpEntityEnclosingRequestBase addEntityToRequestBase(
			HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {
		if (entity != null) {
			requestBase.setEntity(entity);
		}

		return requestBase;
	}

	/**
	 * params中获取entity wanglu 泰得利通
	 * 
	 * @param params
	 * @return
	 */
	private HttpEntity paramsToEntity(RequestParams params) {
		HttpEntity entity = null;

		if (params != null) {
			entity = params.getEntity();
		}

		return entity;
	}

	/**
	 * 转换gzip压缩返回数据
	 * 
	 * @author wanglu 泰得利通
	 * 
	 */
	private static class InflatingEntity extends HttpEntityWrapper {

		@Override
		public InputStream getContent() throws IOException {
			return new GZIPInputStream(wrappedEntity.getContent());
		}

		public InflatingEntity(HttpEntity wrapped) {
			super(wrapped);

		}

		@Override
		public long getContentLength() {
			return -1;
		}

	}

}
