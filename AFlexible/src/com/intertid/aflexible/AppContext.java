package com.intertid.aflexible;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.intertid.aflexible.util.StringUtils;

/**
 * 全局应用程序类：用于保存和调用全局应用配置及访问网络数据 app上下文对象
 * 
 * @author wanglu
 */
@SuppressLint("DefaultLocale")
public class AppContext extends Application {

	public static final int NETTYPE_WIFI = 0x01;// 无线网
	public static final int NETTYPE_CMWAP = 0x02;// CMWap网
	public static final int NETTYPE_CMNET = 0x03;

	@Override
	public void onCreate() {
		super.onCreate();

	}

	/**
	 * 检查当前网络状态
	 * 
	 * @return true 已连接false未连接
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	/**
	 * 获取当前网络类型
	 * 
	 * @return 0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络
	 */
	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if (!StringUtils.isEmpty(extraInfo)) {
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
	}

	/**
	 * 获取软件版本号
	 * 
	 * @author 泰得利通 wanglu
	 * @return
	 */
	public String getAppVersion() {

		PackageManager pm = getPackageManager();

		PackageInfo packageInfo;
		try {
			packageInfo = pm.getPackageInfo(getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return null;

	}

}
