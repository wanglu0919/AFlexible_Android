package com.intertid.aflexible.http;

import com.intertid.aflexible.http.entityhandler.ResponseDataPaser;



/**
 * http返回 回调类
 * 
 * @author wanglu 泰得利通
 * 
 */
public abstract class HttpResposeCallBack<T> {
	private boolean progress = true;
	private int rate = 1000 * 1;// 每秒 刷新频率

	public boolean isProgress() {
		return progress;
	}

	public int getRate() {
		return rate;
	}

	/**
	 * 设置进度,而且只有设置了这个了以后，onLoading才能有效。
	 * 
	 * @param progress
	 *            是否启用进度显示
	 * @param rate
	 *            进度更新频率
	 */
	public HttpResposeCallBack<T> progress(boolean progress, int rate) {
		this.progress = progress;
		this.rate = rate;
		return this;
	}

	public void onStart() {
	};

	/**
	 * onLoading方法有效progress
	 * 
	 * @param count
	 * @param current
	 */
	public void onLoading(long count, long current) {
	};

	public void onSuccess(T t) {
	};

	public void onFailure(Throwable t, int errorNo, String strMsg) {
	};
	
	public  ResponseDataPaser  getResponseDataPaser(){
		return null;
	};
}
