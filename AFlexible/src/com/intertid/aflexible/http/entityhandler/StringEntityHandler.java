package com.intertid.aflexible.http.entityhandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;

/**
 * 返回类型是字符串类型返回处理
 * 
 * @author wanglu 泰得利通
 * 
 */
public class StringEntityHandler {

	public Object handleEntity(HttpEntity entity, EntityCallBack callback,
			String charset) throws IOException {
		if (entity == null)
			return null;

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];

		long count = entity.getContentLength();
		long curCount = 0;
		int len = -1;
		InputStream is = entity.getContent();
		while ((len = is.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
			curCount += len;
			if (callback != null)
				callback.callBack(count, curCount, false);
		}
		if (callback != null)
			callback.callBack(count, curCount, true);
		byte[] data = outStream.toByteArray();
		outStream.close();
		is.close();
		return new String(data, charset);
	}

	public Object handleEntity(HttpEntity entity, EntityCallBack callback,
			String charset, ResponseDataPaser responseDataPaser)
			throws IOException {
		String responseData=(String) handleEntity(entity,callback,
				 charset);

		return responseDataPaser.parseResponse(responseData);

	}

}
