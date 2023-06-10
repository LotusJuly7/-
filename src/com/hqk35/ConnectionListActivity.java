package com.hqk35;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectionListActivity extends Activity {
	TextView ivTitleName;
	LinearLayout recent_chat_list;
	EditText targetIp;
	Thread serverThread;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) { // �Լ��ǿͻ���
				if (msg.arg1 == 0) { // ��������
					if (msg.arg2 == 1) { // ����
						showDevice((DeviceLayout) msg.obj);
					} else if (msg.arg2 == 0) {
						Toast.makeText(ConnectionListActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
					}
				} else if (msg.arg1 == 1) {
					Object[] obj = (Object[]) msg.obj;
					if (msg.arg2 == 1) { // ������Ϣ
						((DeviceLayout) obj[0]).setMessage((String) obj[1], true);
					} else if (msg.arg2 == 2) { // �յ���Ϣ
						((DeviceLayout) obj[0]).setMessage((String) obj[1], false);
					}
				} else if (msg.arg1 == 2) { // �Է��Ͽ�����
					((DeviceLayout) msg.obj).onPassiveDisconnect("�Է��Ͽ�������");
				}
			} else if (msg.what == 2) { // �Լ��Ƿ�����
				if (msg.arg1 == 0) { // ��������
					showDevice((DeviceLayout) msg.obj);
				} else if (msg.arg1 == 1) {
					Object[] obj = (Object[]) msg.obj;
					if (msg.arg2 == 1) { // ������Ϣ
						((DeviceLayout) obj[0]).setMessage((String) obj[1], true);
					} else if (msg.arg2 == 2) { // �յ���Ϣ
						((DeviceLayout) obj[0]).setMessage((String) obj[1], false);
					}
				} else if (msg.arg1 == 2) { // �Է��Ͽ�����
					((DeviceLayout) msg.obj).onPassiveDisconnect("�Է��Ͽ�������");
				} else if (msg.arg1 == 3) { // ����Ͽ�����
					if (msg.arg2 == 1) { // ����������
						((DeviceLayout) msg.obj).onPassiveDisconnect("����������");
					}
				}
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connection_list);
		
		ivTitleName = (TextView) findViewById(R.id.ivTitleName);
		recent_chat_list = (LinearLayout) findViewById(R.id.recent_chat_list);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
		registerReceiver(mWifiReceiver, intentFilter);
		
		serverThread = new WebServerThread(this, handler);
		serverThread.start();
	}
	
	BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
		String wifiIpText = null;
		boolean isApEnabled = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (info.isConnected()) {
            			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            		    int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
            		    String ipAddressUrl = new StringBuilder()
            		    		.append("����IP��")
            		    		.append(ipAddress & 0xFF).append('.')
            		    		.append((ipAddress >> 8) & 0xFF).append('.')
            		    		.append((ipAddress >> 16) & 0xFF).append('.')
            		    		.append((ipAddress >> 24) & 0xFF)
            		    		.toString();
            		    wifiIpText = ipAddressUrl;
            		} else {
            			wifiIpText = null;
            		}
                }
            } else if (intent.getAction().equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
            	isApEnabled = intent.getIntExtra("wifi_state", 0) == 13;
            }
            if (wifiIpText != null) {
            	ivTitleName.setText(wifiIpText);
            } else if (isApEnabled) {
            	ivTitleName.setText("WiFi�ȵ��ѿ���");
            } else {
            	ivTitleName.setText("WiFiδ����");
            }
        }
    };
    
    AlertDialog targetIp_dialog;
    public void connect(View v) {
    	if (targetIp_dialog == null) {
	    	targetIp = new EditText(this);
	    	targetIp.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f);
	    	targetIp.setHint("������Ŀ��IP��ַ");
	    	targetIp.setSingleLine();
	    	targetIp.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_NORMAL);
	    	targetIp.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
	    	AlertDialog.Builder targetIp_dialog_builder = new AlertDialog.Builder(this);
	    	targetIp_dialog_builder.setTitle("���������豸").setView(targetIp);
	    	targetIp_dialog_builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
	    		@Override
	    		public void onClick(DialogInterface dialog, int which) {
	    			String ipAddress = targetIp.getText().toString();
	    		    ProgressDialog connecting_dialog = ProgressDialog.show(ConnectionListActivity.this, null, "�������ӵ� " + ipAddress, true, false);
	    		    new WebClientThread(ConnectionListActivity.this, handler, connecting_dialog, ipAddress).start();
	    		}
	    	});
	    	targetIp_dialog_builder.setNegativeButton("ȡ��", null);
	    	targetIp_dialog = targetIp_dialog_builder.create();
    	}
    	targetIp_dialog.show();
    }
	
	void showDevice(DeviceLayout view) {
		recent_chat_list.addView(view);
	}
	
	@Override
	public void onBackPressed() {
		ViewGroup contentLayout = (ViewGroup) findViewById(android.R.id.content);
		int childCount = contentLayout.getChildCount();
		if (childCount >= 2) {
			contentLayout.removeViewAt(childCount - 1);
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onDestroy() {
		serverThread.destroy();
		unregisterReceiver(mWifiReceiver);
		super.onDestroy();
	}
}
