package com.intertid.aflexible;

import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.intertid.aflexible.activity.AFlexibleActivity;
import com.intertid.aflexible.androidannotations.androidannotations.ViewById;
import com.intertid.aflexible.entity.Channel;
import com.intertid.aflexible.http.AFlexibleHttp;
import com.intertid.aflexible.http.HttpResposeCallBack;
import com.intertid.aflexible.http.entityhandler.ResponseDataPaser;
import com.intertid.aflexible.log.AFlexibleLog;
import com.intertid.aflexible.log.LogController;

public class MainActivity extends AFlexibleActivity {

	@ViewById(value = R.id.btn_get, click = "doGet")
	private Button btn_get;
	@ViewById(value = R.id.btn_post)
	private Button btn_post;
	@ViewById(value = R.id.btn_download)
	private Button btn_download;
	@ViewById(value=R.id.btn_log, click = "doLog")
	private Button btn_log;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}
	
	
	public void doLog(View view){
		
		AFlexibleLog.getAFlexibleLog(new LogController() {

			@Override
			protected boolean isLog() {
				return true;
			}
		}).i("LOG", "asdfsadfs");
		
		
	}

	public void doGet(View view) {

		AFlexibleHttp aHttp = new AFlexibleHttp();

		aHttp.get("http://3g.wuxi.gov.cn/api/channel/ace3e926-9206-4193-a9da-b99955f0ff4b/channels.json",
				new HttpResposeCallBack<List<Channel>>() {

					@Override
					public ResponseDataPaser getResponseDataPaser() {
						return new MyJsonEntityHandler();
					}

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						
						
					}

					@Override
					public void onSuccess(List<Channel> t) {

						super.onSuccess(t);
						Toast.makeText(MainActivity.this, t.size(), Toast.LENGTH_SHORT).show();
						Log.i("AFlex", t.size()+"");
					}

					

				});

	}

}
