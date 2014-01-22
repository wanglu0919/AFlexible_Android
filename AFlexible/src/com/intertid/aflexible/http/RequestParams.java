package com.intertid.aflexible.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

/**
 * 请求参数包装
 * 
 * @author wanglu 泰得利通
 * 
 */
public class RequestParams {

	private static String ENCODING = "UTF-8";

	protected ConcurrentMap<String, String> urlParams;
	protected ConcurrentMap<String, FileWrapper> fileParams;

	public RequestParams() {
		init();
	}

	public RequestParams(Map<String, String> params) {
		init();

		for (Map.Entry<String, String> entry : params.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	public RequestParams(String paramName, String paramValue) {
		init();
		put(paramName, paramValue);
	}

	public RequestParams(Object... parameNameAndValues) {
		init();
		int len = parameNameAndValues.length;
		if (len % 2 != 0)
			throw new IllegalArgumentException(
					"Supplied arguments must be even");
		for (int i = 0; i < len; i += 2) {
			String key = String.valueOf(parameNameAndValues[i]);
			String val = String.valueOf(parameNameAndValues[i + 1]);
			put(key, val);
		}
	}

	public void put(String paramName, File file) throws FileNotFoundException {
		put(paramName, new FileInputStream(file), file.getName());
	}

	public void put(String paramName, InputStream stream, String fileName) {
		put(paramName, stream, fileName, null);
	}

	public void put(String paramName, InputStream stream) {
		put(paramName, stream, null);
	}

	public void remove(String paramName) {
		urlParams.remove(paramName);
		fileParams.remove(paramName);
	}

	/**
	 * 添加 inputStream 到请求中.
	 * 
	 * @param key
	 *            the key name for the new param.
	 * @param stream
	 *            the input stream to add.
	 * @param fileName
	 *            the name of the file.
	 * @param contentType
	 *            the content type of the file, eg. application/json
	 */
	public void put(String paramName, InputStream stream, String fileName,
			String contentType) {
		if (paramName != null && stream != null) {
			fileParams.put(paramName, new FileWrapper(stream, fileName,
					contentType));
		}
	}

	public void put(String key, String value) {
		if (key != null && value != null) {
			urlParams.put(key, value);
		}
	}

	/**
	 * Returns an HttpEntity containing all request parameters
	 */
	public HttpEntity getEntity() {
		HttpEntity entity = null;

		if (!fileParams.isEmpty()) {//如果有上传文件存在
			MultipartEntity multipartEntity = new MultipartEntity();

			// Add string params
			for (ConcurrentHashMap.Entry<String, String> entry : urlParams
					.entrySet()) {
				multipartEntity.addPart(entry.getKey(), entry.getValue());
			}

			// Add file params
			int currentIndex = 0;
			int lastIndex = fileParams.entrySet().size() - 1;
			for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams
					.entrySet()) {
				FileWrapper file = entry.getValue();
				if (file.inputStream != null) {
					boolean isLast = currentIndex == lastIndex;
					if (file.contentType != null) {
						multipartEntity.addPart(entry.getKey(),
								file.getFileName(), file.inputStream,
								file.contentType, isLast);
					} else {
						multipartEntity.addPart(entry.getKey(),
								file.getFileName(), file.inputStream, isLast);
					}
				}
				currentIndex++;
			}

			entity = multipartEntity;
		} else {
			try {
				entity = new UrlEncodedFormEntity(getParamsList(), ENCODING);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return entity;
	}

	protected List<BasicNameValuePair> getParamsList() {
		List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();

		for (ConcurrentHashMap.Entry<String, String> entry : urlParams
				.entrySet()) {
			lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		return lparams;
	}

	/**
	 * 
	 * wanglu 泰得利通
	 * 
	 * @return
	 */
	public String getURLParamString() {
		return URLEncodedUtils.format(getParamsList(), ENCODING);
	}

	private void init() {

		urlParams = new ConcurrentHashMap<String, String>();
		fileParams = new ConcurrentHashMap<String, FileWrapper>();
	}

	private static class FileWrapper {
		public String fileName;
		public InputStream inputStream;
		public String contentType;

		public FileWrapper(InputStream inputStream, String fileName,
				String contentType) {
			this.fileName = fileName;
			this.inputStream = inputStream;
			this.contentType = contentType;
		}

		public String getFileName() {
			if (fileName != null) {
				return fileName;
			} else {
				return "nofilename";
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (ConcurrentHashMap.Entry<String, String> entry : urlParams
				.entrySet()) {
			if (result.length() > 0)
				result.append("&");

			result.append(entry.getKey());
			result.append("=");
			result.append(entry.getValue());
		}

		for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams
				.entrySet()) {
			if (result.length() > 0)
				result.append("&");

			result.append(entry.getKey());
			result.append("=");
			result.append("FILE");
		}

		return result.toString();
	}

}
