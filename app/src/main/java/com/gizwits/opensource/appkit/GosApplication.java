package com.gizwits.opensource.appkit;

import android.app.Application;
import android.support.v4.widget.SwipeRefreshLayout;

import com.gizwits.opensource.appkit.ControlModule.GosDeviceControlActivity;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

public class GosApplication extends Application {

	public static int flag = 0;

	@Override
	public void onCreate() {
		StringBuffer param = new StringBuffer();
		param.append("appid=5b3b1fa5");
		param.append(",");
		param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
		param.append(",");
		param.append(SpeechConstant.FORCE_LOGIN + "=true");
		SpeechUtility.createUtility(GosApplication.this, param.toString());

		super.onCreate();
	}


}
