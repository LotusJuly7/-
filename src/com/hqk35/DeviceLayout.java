package com.hqk35;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

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
	private TextView text2;
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
		
		TextView text1 = new TextView(context);
		LayoutParams text1_lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		text1_lp.bottomMargin = (int) (2f * scale);
		text1.setTextColor(0xff03081a);
		text1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f);
		text1.setText(title);
		iag.addView(text1, text1_lp);
		
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
	                    android:text="Á«Ò¶" />
	                <TextView
	                    android:layout_width="match_parent"
	                    android:layout_height="wrap_content"
	                    android:layout_marginRight="12dp"
	                    android:paddingRight="10dp"
	                    android:textColor="#878b99"
	                    android:textSize="14dp"
	                    android:text="ÄãºÃ" />
	            </LinearLayout>
	        </LinearLayout>
		*/
	}
	
	public void setMessage(String message, boolean isSend) {
		text2.setText(message);
		if (contentView != null) {
			contentView.showMessage(message, isSend);
		}
	}
	
	private ChatLayout contentView;
	@Override
	public boolean performClick() {
		boolean result = super.performClick();
		if (contentView == null) {
			contentView = (ChatLayout) LayoutInflater.from(context).inflate(R.layout.activity_main, null);
			contentView.bind(handler, socket, title, this, printWriter);
			((Activity) context).getWindow().addContentView(contentView, new WindowManager.LayoutParams());
		}
		return result;
	}
	
	public void unbindContentView() {
		contentView = null;
	}
	
	public void onPassiveDisconnect() {
		printWriter.close();
		try {
			socket.close();
			if (contentView != null) {
				contentView.onPassiveDisconnect();
			}
			removeSelf();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void removeSelf() {
		((ViewGroup) getParent()).removeView(this);
	}
}
