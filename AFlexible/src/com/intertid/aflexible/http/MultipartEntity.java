package com.intertid.aflexible.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

/**
 * 复杂类型包装
 * 
 * @author wanglu 泰得利通
 * 
 */
public class MultipartEntity implements HttpEntity {
	private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
			.toCharArray();
	private String boundary = null;// 分割符
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	boolean isSetLast = false;
	boolean isSetFirst = false;

	public MultipartEntity() {

		final StringBuffer buf = new StringBuffer();
		final Random rand = new Random();
		for (int i = 0; i < 30; i++) {
			buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
		}
		this.boundary = buf.toString();

	}

	/**
	 * 写出头部分隔符 wanglu 泰得利通
	 */
	public void writeFirstBoundaryIfNeeds() {
		if (!isSetFirst) {
			try {
				out.write(("--" + boundary + "\r\n").getBytes());
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		isSetFirst = true;
	}

	/**
	 * 写最后一个分隔符 wanglu 泰得利通
	 */
	public void writeLastBoundaryIfNeeds() {
		if (isSetLast) {
			return;
		}

		try {
			out.write(("\r\n--" + boundary + "--\r\n").getBytes());
		} catch (final IOException e) {
			e.printStackTrace();
		}

		isSetLast = true;
	}

	public void addPart(final String paramName, final String paramValue) {
		writeFirstBoundaryIfNeeds();
		try {
			out.write(("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n\r\n")
					.getBytes());
			out.write(paramValue.getBytes());
			out.write(("\r\n--" + boundary + "\r\n").getBytes());
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void addPart(final String paramName, final String fileName,
			final InputStream fin, final boolean isLast) {
		addPart(paramName, fileName, fin, "application/octet-stream", isLast);
	}

	/**
	 * 
	 * wanglu 泰得利通
	 * 
	 * @param paramName
	 *            文件表单名称
	 * @param fileName
	 *            //文件名
	 * @param fin
	 *            //输入流
	 * @param type
	 *            //文件类型
	 * @param isLast
	 *            // 是否是提交数据最后
	 */
	public void addPart(final String paramName, final String fileName,
			final InputStream fin, String type, final boolean isLast) {
		writeFirstBoundaryIfNeeds();
		try {
			type = "Content-Type: " + type + "\r\n";
			out.write(("Content-Disposition: form-data; name=\"" + paramName
					+ "\"; filename=\"" + fileName + "\"\r\n").getBytes());
			out.write(type.getBytes());
			out.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes());

			final byte[] tmp = new byte[4096];
			int l = 0;
			while ((l = fin.read(tmp)) != -1) {
				out.write(tmp, 0, l);
			}
			if (!isLast)
				out.write(("\r\n--" + boundary + "\r\n").getBytes());
			out.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fin.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void addPart(final String paramName, final File value,
			final boolean isLast) {
		try {
			addPart(paramName, value.getName(), new FileInputStream(value),
					isLast);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void consumeContent() throws IOException,
			UnsupportedOperationException {
		if (isStreaming()) {
			throw new UnsupportedOperationException(
					"Streaming entity does not implement #consumeContent()");
		}
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return new ByteArrayInputStream(out.toByteArray());
	}

	@Override
	public Header getContentEncoding() {
		return null;
	}

	@Override
	public long getContentLength() {
		writeLastBoundaryIfNeeds();
		return out.toByteArray().length;
	}

	@Override
	public Header getContentType() {
		return new BasicHeader("Content-Type", "multipart/form-data; boundary="
				+ boundary);

	}

	@Override
	public boolean isChunked() {
		return false;
	}

	@Override
	public boolean isRepeatable() {
		return false;
	}

	@Override
	public boolean isStreaming() {
		return false;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		outstream.write(out.toByteArray());
	}

}
