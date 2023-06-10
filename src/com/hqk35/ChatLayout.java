package com.hqk35;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChatLayout extends LinearLayout {
	private Context context;
	private TextView title, disconnect;
	private LinearLayout listView1;
	private LinearLayout.LayoutParams messageItemLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	private Button send_btn;
	private EditText input;
	public ChatLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}
	@Override
	protected void onFinishInflate() {
		title = (TextView) findViewById(R.id.title);
		findViewById(R.id.e89).setOnClickListener(navigateBack);
		disconnect = (TextView) findViewById(R.id.ivTitleBtnRightText);
		disconnect.setOnClickListener(disconnect_onClick);
		listView1 = (LinearLayout) findViewById(R.id.listView1);
		
		send_btn = (Button) findViewById(R.id.fun_btn);
		send_btn.setOnClickListener(send_btn_onClick);
		input = (EditText) findViewById(R.id.input);
	}
	
	private Handler handler;
	private Socket socket;
	private DeviceLayout deviceLayout;
	private PrintWriter printWriter;
	public void bind(Handler handler, Socket socket, String title, DeviceLayout deviceLayout, PrintWriter printWriter) {
		this.handler = handler;
		this.socket = socket;
		this.title.setText(title);
		this.deviceLayout = deviceLayout;
		this.printWriter = printWriter;
	}
	
	private View.OnClickListener navigateBack = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			((ViewGroup) ((Activity) context).findViewById(android.R.id.content)).removeView(ChatLayout.this);
		}
	};
	
	private View.OnClickListener send_btn_onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			send_btn.setEnabled(false);
			new SendThread(handler, socket, deviceLayout, printWriter, input.getText().toString()).start();
		}
	};

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	public void showMessage(long time, String message, boolean isSend) {
		listView1.addView(new MessageLayout(context, dateFormat.format(time), message, isSend), messageItemLp);
		if (isSend && disconnect.getVisibility() == View.VISIBLE) {
			input.setText("");
			send_btn.setEnabled(true);
		}
	}
	
	@Override
    protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		deviceLayout.unbindContentView(); // 解除对自己的引用
	}
	
	private View.OnClickListener disconnect_onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			send_btn.setEnabled(false);
			printWriter.close();
			try {
				socket.close();
				v.setVisibility(View.GONE);
				deviceLayout.removeSelf();
				Toast.makeText(context, "已断开连接", Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	public void onPassiveDisconnect(String message) { // 隐藏“断开”按钮
		send_btn.setEnabled(false);
		disconnect.setVisibility(View.GONE);
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	
	private static class SendThread extends Thread {
		private Handler handler;
		//private Socket socket;
		private DeviceLayout deviceLayout;
		private PrintWriter printWriter;
		private String message;
		public SendThread(Handler handler, Socket socket, DeviceLayout deviceLayout, PrintWriter printWriter, String message) {
			this.handler = handler;
			//this.socket = socket;
			this.deviceLayout = deviceLayout;
			this.printWriter = printWriter;
			this.message = message;
		}
		@Override
		public void run() {
			printWriter.print(JSONObject.quote(message));
			printWriter.print('\n');
            printWriter.flush();
            Log.i("客户端", "flush");
            handler.obtainMessage(1/*这里可能会有一些问题，暂时没区分自己是客户端还是服务器*/, 1, 1, new Object[] {deviceLayout, message}).sendToTarget();
            /*printWriter.close();
            try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		}
	}
}
