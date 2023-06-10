package com.hqk35;

import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DeviceLayout extends LinearLayout {
	private Context context;
	private Handler handler;
	private Socket socket;
	private String title;
	private PrintWriter printWriter;
	private TextView text1_2, text2;
	public DeviceLayout(Context context, Handler handler, Socket socket, String title, PrintWriter printWriter) {
		super(context);
		this.context = context;
		this.handler = handler;
		this.socket = socket;
		this.title = title;
		this.printWriter = printWriter;
		float scale = this.getResources().getDisplayMetrics().density;
		
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) (72f * scale)));
		setOrientation(HORIZONTAL);
		setBackgroundResource(R.drawable.jm);
		setClickable(true);
		
		LinearLayout iag = new LinearLayout(context);
		LayoutParams iag_lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
		iag_lp.gravity = Gravity.CENTER_VERTICAL;
		iag_lp.leftMargin = (int) (78f * scale);
		iag.setOrientation(LinearLayout.VERTICAL);
		addView(iag, iag_lp);
		
		LinearLayout text1 = new LinearLayout(context);
		LayoutParams text1_lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		text1_lp.bottomMargin = (int) (2f * scale);
		text1.setOrientation(HORIZONTAL);
		iag.addView(text1, text1_lp);
		
		TextView text1_1 = new TextView(context);
		text1_1.setTextColor(0xff03081a);
		text1_1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f);
		text1_1.setText(title);
		text1.addView(text1_1, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
		
		text1_2 = new TextView(context);
		LayoutParams text1_2_lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		text1_2_lp.gravity = Gravity.BOTTOM;
		text1_2_lp.rightMargin = (int) (16f * scale);
		text1_2.setPadding(0, 0, 0, (int) (2f * scale));
		text1_2.setIncludeFontPadding(false);
		text1_2.setTextColor(0xff878b99);
		text1_2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
		text1_2.setText(dateFormat.format(System.currentTimeMillis()));
		text1.addView(text1_2, text1_2_lp);
		
		text2 = new TextView(context);
		LayoutParams text2_lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		text2_lp.rightMargin = (int) (12f * scale);
		text2.setPadding(0, 0, (int) (10f * scale), 0);
		text2.setTextColor(0xff878b99);
		text2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f);
		iag.addView(text2, text2_lp);
		
		/*
			<LinearLayout
	            android:layout_width="match_parent"
	            android:layout_height="72dp"
	            android:orientation="horizontal"
	            android:background="@drawable/jm"
	            android:clickable="true">
	            <LinearLayout
	                android:layout_width="0px"
	                android:layout_height="wrap_content"
	                android:layout_weight="1"
	                android:layout_gravity="center_vertical"
	                android:layout_marginLeft="78dp"
	                android:orientation="vertical">
	                <TextView
	                    android:layout_width="match_parent"
	                    android:layout_height="wrap_content"
	                    android:layout_marginBottom="2dp"
	                    android:textColor="#03081a"
	                    android:textSize="17dp"
	                    android:text="莲叶" />
	                <TextView
	                    android:layout_width="match_parent"
	                    android:layout_height="wrap_content"
	                    android:layout_marginRight="12dp"
	                    android:paddingRight="10dp"
	                    android:textColor="#878b99"
	                    android:textSize="14dp"
	                    android:text="你好" />
	            </LinearLayout>
	        </LinearLayout>
		*/
	}
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
	
	private static class ChatLog {
		long time;
		String message;
		boolean isSend;
		ChatLog next = null;
		ChatLog(long time, String message, boolean isSend) {
			this.time = time;
			this.message = message;
			this.isSend = isSend;
		}
	}
	private ChatLog chatLog_head, chatLog_tail; // 消息记录链表
	public void setMessage(String message, boolean isSend) {
		long time = System.currentTimeMillis();
		// 暂存消息记录
		if (chatLog_head == null) {
			chatLog_head = new ChatLog(time, message, isSend);
			chatLog_tail = chatLog_head;
		} else {
			chatLog_tail.next = new ChatLog(time, message, isSend);
			chatLog_tail = chatLog_tail.next;
		}
		// 更新UI
		text1_2.setText(dateFormat.format(time));
		text2.setText(message);
		if (contentView != null) {
			contentView.showMessage(time, message, isSend);
		}
	}
	
	private ChatLayout contentView;
	@Override
	public boolean performClick() {
		boolean result = super.performClick();
		if (contentView == null) {
			contentView = (ChatLayout) LayoutInflater.from(context).inflate(R.layout.activity_main, null);
			contentView.bind(handler, socket, title, this, printWriter);
			for (ChatLog chatLog = chatLog_head; chatLog != null; chatLog = chatLog.next) { // 显示消息记录
				contentView.showMessage(chatLog.time, chatLog.message, chatLog.isSend);
			}
			((Activity) context).getWindow().addContentView(contentView, new WindowManager.LayoutParams());
		}
		return result;
	}
	
	public void unbindContentView() {
		contentView = null;
	}
	
	public void onPassiveDisconnect(String message) {
		if (contentView != null) {
			contentView.onPassiveDisconnect(message);
		}
		removeSelf();
	}
	
	public void removeSelf() {
		((ViewGroup) getParent()).removeView(this);
	}
}
