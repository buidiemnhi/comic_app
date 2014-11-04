package com.bsp.comicapp.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ChangeBackgroundReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
	//	Toast.makeText(context, "ssss", Toast.LENGTH_LONG).show();
		Intent intent1=new Intent("Intent_Background");
		intent1.putExtra("Background", true);
		context.sendBroadcast(intent1);
	}

}
