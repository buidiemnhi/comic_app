package com.bsp.comicapp.util;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver{

	
	
	@Override
	public void onReceive(final Context context, final Intent intent) {
		// TODO Auto-generated method stub
		 ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		    // test for connection
		    if (cm.getActiveNetworkInfo() != null
		            && cm.getActiveNetworkInfo().isAvailable()
		            && cm.getActiveNetworkInfo().isConnected()) {
		    	
		    			  Config.isConnectedInternet = true;
		    			  
		    			  Log.e("NET", "Internet Connection  Present");
		    } else {
		        Log.e("NET", "Internet Connection Not Present");
		    	Config.isConnectedInternet = false;
		    	//Toast.makeText(context, "ssss", Toast.LENGTH_LONG).show();
		    	
   			  
		    }
		    Intent intent2 = new Intent(Config.INTERNET_DETECT);
		    intent2.putExtra(Config.EXTRA_ISCONNECTED, Config.isConnectedInternet);
		    context.sendBroadcast(intent2);
		   
		   
	}
	
	
}
