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
            Socket socket = new Socket(ipAddress, WebServerThread.port);      //����һ
            Log.i("�ͻ���", "�õ�Socket");
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            //socket.setSoTimeout(60000);
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(   //�����
                    socket.getOutputStream(), "UTF-8")), true);
            Log.i("�ͻ���", "�õ�PrintWriter");
            view = new DeviceLayout(context, handler, socket, ipAddress, printWriter);
        	dialog.dismiss();
            handler.obtainMessage(1, 0, 1, view).sendToTarget();
            
            try {
	            String message;
				while ((message = reader.readLine()) != null) {
					Log.i("�ͻ���", "��ȡһ������");
					try {
						handler.obtainMessage(1, 1, 2, new Object[] {view, (String) new JSONTokener(message).nextValue()}).sendToTarget();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				Log.i("�ͻ���", "��ȡ������");
				socket.close();
				handler.obtainMessage(1, 2, 0, view).sendToTarget(); // ������Լ��Ͽ����ӣ���������׳�Exception�������ߵ�����
            } catch (IOException ignored) {}; // ������Լ��Ͽ����ӣ����ߵ�����
        } catch (UnknownHostException e) {
        	dialog.dismiss();
        	handler.obtainMessage(1, 0, 0, "IP��ַ��Ч").sendToTarget(); // IP��ַ��Ч
        } catch (SocketException e) {
            Log.e("�ͻ���", ("����ʧ�ܣ�" + e.getMessage()));   //���Socket�����ȡʧ�ܣ������ӽ���ʧ�ܣ����ߵ�����߼�
        	dialog.dismiss();
        	handler.obtainMessage(1, 0, 0, "����ʧ��").sendToTarget(); // ����ʧ��
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}
}
