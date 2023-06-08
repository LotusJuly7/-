package com.hqk35;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONTokener;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class WebClientThread extends Thread {
	private Context context;
	private Handler handler;
	private ProgressDialog dialog;
	private String ipAddress;
	private DeviceLayout view;
	public WebClientThread(Context context, Handler handler, ProgressDialog dialog, String ipAddress) {
		this.context = context;
		this.handler = handler;
		this.dialog = dialog;
		this.ipAddress = ipAddress;
	}
	@Override
	public void run() {
		try {
            Socket socket = new Socket(ipAddress, WebServerThread.port);      //步骤一
            Log.i("客户端", "得到Socket");
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            //socket.setSoTimeout(60000);
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(   //步骤二
                    socket.getOutputStream(), "UTF-8")), true);
            Log.i("客户端", "得到PrintWriter");
            view = new DeviceLayout(context, handler, socket, ipAddress, printWriter);
        	dialog.dismiss();
            handler.obtainMessage(1, 0, 1, view).sendToTarget();
            
            try {
	            String message;
				while ((message = reader.readLine()) != null) {
					Log.i("客户端", "读取一行数据");
					try {
						handler.obtainMessage(1, 1, 2, new Object[] {view, (String) new JSONTokener(message).nextValue()}).sendToTarget();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				Log.i("客户端", "读取完数据");
				socket.close();
				handler.obtainMessage(1, 2, 0, view).sendToTarget(); // 如果是自己断开连接，在上面会抛出Exception，不会走到这里
            } catch (IOException ignored) {}; // 如果是自己断开连接，会走到这里
        } catch (UnknownHostException e) {
        	dialog.dismiss();
        	handler.obtainMessage(1, 0, 0, "IP地址无效").sendToTarget(); // IP地址无效
        } catch (SocketException e) {
            Log.e("客户端", ("连接失败：" + e.getMessage()));   //如果Socket对象获取失败，即连接建立失败，会走到这段逻辑
        	dialog.dismiss();
        	handler.obtainMessage(1, 0, 0, "连接失败").sendToTarget(); // 连接失败
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}
}
