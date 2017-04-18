package com.facepp.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facepp.library.FaceppActionActivity;
import com.facepp.library.util.ConUtil;
import com.facepp.library.util.DialogUtil;
import com.facepp.library.util.SharedUtil;
import com.facepp.library.util.Util;
import com.megvii.awesomedemo.facepp.R;
import com.megvii.facepp.sdk.Facepp;

import java.util.Locale;
//import com.megvii.licensemanager.sdk.LicenseManager;

public class LoadingActivity extends Activity {
	private TextView WarrantyText;
	private ProgressBar WarrantyBar;
	private Button againWarrantyBtn;
	private SharedUtil mSharedUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);



		init();
	}

	private void init() {
		mSharedUtil = new SharedUtil(this);
		WarrantyText = (TextView) findViewById(R.id.loading_layout_WarrantyText);
		WarrantyBar = (ProgressBar) findViewById(R.id.loading_layout_WarrantyBar);
		againWarrantyBtn = (Button) findViewById(R.id.loading_layout_againWarrantyBtn);
		String authTime0 = ConUtil.getFormatterDate(Facepp.getApiExpirationMillis(this, ConUtil.getFileContent(this, R
				.raw.megviifacepp_0_4_7_model)));
	}

	@Override
	protected void onResume() {
		super.onResume();
		String language_save = mSharedUtil.getStringValueByKey("language");
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		if(!language.equals(language_save))
		   showLanguage(language);


		initData();
		network();
		onClickListener();
	}

	private void initData() {
		WarrantyText.setText(getResources().getString(R.string.auth_progress));
		againWarrantyBtn.setText(getResources().getString(R.string.auth_again));
		if (Util.API_KEY == null || Util.API_SECRET == null) {
			if (!ConUtil.isReadKey(this)) {
				DialogUtil mDialogUtil = new DialogUtil(this);
				mDialogUtil.showDialog(getResources().getString(R.string.key_secret));
			}
		}
	}

	private void network() {
		if (Facepp.getSDKAuthType(ConUtil.getFileContent(this, R.raw
				.megviifacepp_0_4_7_model)) == 2) {// 非联网授权
			authState(true);
			return;
		}

//		againWarrantyBtn.setVisibility(View.GONE);
//		WarrantyText.setText(getResources().getString(R.string.auth_progress));
//		WarrantyBar.setVisibility(View.VISIBLE);
//		final LicenseManager licenseManager = new LicenseManager(this);
//		licenseManager.setExpirationMillis(Facepp.getApiExpirationMillis(this, ConUtil.getFileContent(this, R.raw
//				.megviifacepp_0_4_7_model)));
//
//		String uuid = ConUtil.getUUIDString(LoadingActivity.this);
//		long[] apiName = {Facepp.getApiName()};
//
//		licenseManager.takeLicenseFromNetwork(uuid, Util.API_KEY, Util.API_SECRET, apiName,
//				LicenseManager.DURATION_30DAYS, new LicenseManager.TakeLicenseCallback() {
//					@Override
//					public void onSuccess() {
//						authState(true);
//					}
//
//					@Override
//					public void onFailed(int i, byte[] bytes) {
//						authState(false);
//					}
//				});
	}

	private void freshView(){
       Intent intent = new Intent(this, LoadingActivity.class);
       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
       startActivity(intent);
	   finish();
	}

	protected void showLanguage(String language) {
		//设置应用语言类
		Resources resources = getResources();
		Configuration config = resources.getConfiguration();
		DisplayMetrics dm = resources.getDisplayMetrics();
		if (language.equals("zh")) {
			config.locale = Locale.SIMPLIFIED_CHINESE;
		} else {
			config.locale = Locale.ENGLISH;
		}
		resources.updateConfiguration(config, dm);
		mSharedUtil.saveStringValue("language", language);
		freshView();
	}


	private void authState(boolean isSuccess) {
		if (isSuccess) {

			Intent intent = new Intent();
			intent.setClass(this, FaceppActionActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//If set, and the activity being launched is already running in the current task, then instead of launching a new instance of that activity,all of the other activities on top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent.
			startActivity(intent);

			finish();
		} else {
			WarrantyBar.setVisibility(View.GONE);
			againWarrantyBtn.setVisibility(View.VISIBLE);
			WarrantyText.setText(getResources().getString(R.string.auth_fail));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	private void onClickListener() {
		againWarrantyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				network();
			}
		});
		findViewById(R.id.loading_layout_rootRel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ConUtil.isGoneKeyBoard(LoadingActivity.this);
			}
		});
	}
}
