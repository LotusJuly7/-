package com.hqk35;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	TextView title;
	LinearLayout listView1;
	LinearLayout.LayoutParams messageItemLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	Button send_btn;
	EditText input, targetIp;
	Thread serverThread;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) { // 发出请求
				if (msg.arg1 == 0) { // 正常
					Object[] obj = (Object[]) msg.obj;
					showMessage((String) obj[0], (String) obj[1], true);
					input.setText("");
					send_btn.setEnabled(true);
				} else if (msg.arg1 == 1) {
					Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
					send_btn.setEnabled(true);
				}
			} else if (msg.what == 2) { // 收到请求
				Object[] obj = (Object[]) msg.obj;
				byte[] ip = (byte[]) obj[0];
				showMessage(new StringBuilder()
						.append(ip[0] & 0xFF).append('.')
						.append(ip[1] & 0xFF).append('.')
						.append(ip[2] & 0xFF).append('.')
						.append(ip[3] & 0xFF).toString(), (String) obj[1], false);
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		title = (TextView) findViewById(R.id.title);
		listView1 = (LinearLayout) findViewById(R.id.listView1);
		//showMessage("通信2101-罗畅", "你好", false);
		//showMessage("提高2101-何其锴", "我要开始写Socket了", true);
		send_btn = (Button) findViewById(R.id.fun_btn);
		send_btn.setOnClickListener(send_btn_onClick);
		input = (EditText) findViewById(R.id.input);
		targetIp = (EditText) findViewById(R.id.targetIp);
		
		registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
		
		serverThread = new WebServerThread(handler);
		serverThread.start();
	}
	static final int port = 52013;
	BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                	if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                        
                    } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                        
                    }
                    if (info.isConnected()) {
            			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            		    int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            		    String ipAddressUrl = new StringBuilder()
            		    		.append("本机IP：")
            		    		.append(ipAddress & 0xFF).append('.')
            		    		.append((ipAddress >> 8) & 0xFF).append('.')
            		    		.append((ipAddress >> 16) & 0xFF).append('.')
            		    		.append((ipAddress >> 24) & 0xFF)
            		    		.toString();
            		    title.setText(ipAddressUrl);
            		} else {
            			title.setText("WiFi未连接");
            		}
                }
            }
        }
    };
    
    
    
    View.OnClickListener send_btn_onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			send_btn.setEnabled(false);
			new WebClientThread(handler, targetIp.getText().toString(), input.getText().toString()).start();
		}
	};
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("  HH:mm:ss");
	void showMessage(String ipAddress, String message, boolean isSend) {
		long time = System.currentTimeMillis();
		if (isSend) {
			listView1.addView(new MessageLayout(this, ipAddress + "  <-" + dateFormat.format(time), message, isSend), messageItemLp);
		} else {
			listView1.addView(new MessageLayout(this, ipAddress + dateFormat.format(time), message, isSend), messageItemLp);
		}
	}
	
	@Override
	protected void onDestroy() {
		serverThread.destroy();
		unregisterReceiver(mWifiReceiver);
		super.onDestroy();
	}
}
