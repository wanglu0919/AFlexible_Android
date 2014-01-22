package com.intertid.aflexible.log;

import android.util.Log;
/**
 * log管理
 * @author wanglu 泰得利通
 *
 */
public class AFlexibleLog {

	private static AFlexibleLog aFlexibleLog;
	private LogController logController;

	private AFlexibleLog() {
	};

	public static synchronized AFlexibleLog getAFlexibleLog(
			LogController logController) {

		if (aFlexibleLog == null) {
			aFlexibleLog = new AFlexibleLog();
			aFlexibleLog.logController = logController;

		}

		return aFlexibleLog;
	}

	public void i(String tag, String msg) {

		if (logController.isLog()) {
			Log.i(tag, msg);
		}

	}

	public void v(String tag, String msg) {
		if (logController.isLog()) {
			Log.v(tag, msg);
		}
	}

	public void d(String tag, String msg) {
		if (logController.isLog()) {
			Log.d(tag, msg);
		}

	}

	public void e(String tag, String msg) {
		if (logController.isLog()) {
			Log.e(tag, msg);
		}

	}

}
