package com.androsz.electricsleep.ui;

import android.content.Intent;
import android.os.Bundle;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.service.SleepAccelerometerService;

public class SleepActivity extends CustomTitlebarActivity {

	@Override
	public int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_sleep;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.startService(new Intent(this, SleepAccelerometerService.class));
	}
}
