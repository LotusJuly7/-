package com.hqk35;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.json.JSONException;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class WebServerThread extends Thread {
	private Context context;
	private Handler handler;
	private ServerSocket mServerSocket;
	private boolean isLooping = true;
	public WebServerThread(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}
	public static final int port = 52013;
	@Override
	public void run() {
		try {
		    mServerSocket = new ServerSocket(port);
		    //mServerSocket.setReuseAddress(true);
		    while (isLooping && !mServerSocket.isClosed()) {
		        // ���տͻ����׽��֡�
		        try {
		        	// �������ܿͻ��ˡ�
		            Socket socket = mServerSocket.accept();
		            byte[] ipAddress = socket.getInetAddress().getAddress();
		            Log.i("������", "===server accept===");
			        new ServerThread(socket, context, handler, ipAddress).start();
	        	} catch (SocketException e) {
	        		break;
	        	}
		    }
		    if (!mServerSocket.isClosed()) {
    			mServerSocket.close();
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void destroy() {
		isLooping = false;
		try {
			if (!mServerSocket.isClosed()) {
    			mServerSocket.close();
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class ServerThread extends Thread {
	private Socket mSocket;
	private Context context;
	private Handler handler;
	private byte[] ipAddress;
	private DeviceLayout view;
	public ServerThread(Socket socket, Context context, Handler handler, byte[] ipAddress) {
		mSocket = socket;
		this.context = context;
		this.handler = handler;
		this.ipAddress = ipAddress;
	}
	@Override
	public void run() {
		PrintWriter printWriter = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "UTF-8"));
			Log.i("������", "��ʼ�߳�");
			printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(   //�����
                    mSocket.getOutputStream(), "UTF-8")), true);
            Log.i("�ͻ���", "�õ�PrintWriter");
			view = new DeviceLayout(context, handler, mSocket, new StringBuilder()
					.append(ipAddress[0] & 0xFF).append('.')
					.append(ipAddress[1] & 0xFF).append('.')
					.append(ipAddress[2] & 0xFF).append('.')
					.append(ipAddress[3] & 0xFF).toString(), printWriter);
			handler.obtainMessage(2, 0, 0, view).sendToTarget();
			String message;
			while ((message = reader.readLine()) != null) {
				Log.i("������", "��ȡһ������");
				try {
					handler.obtainMessage(2, 1, 2, new Object[] {view, (String) new JSONTokener(message).nextValue()}).sendToTarget();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			Log.i("������", "��ȡ������");
            /*// ��������
			OutputStream os = null;
            try {
            	os = mSocket.getOutputStream();
            	os.write("HTTP/1.1 200\r\nContent-Type: text/plain\r\n\r\nHello\r\n".getBytes());
            	os.flush();
            	Log.i("������", "flush");
            } finally {
            	os.close();
            }*/
			printWriter.close();
			mSocket.close();
			mSocket = null;
			handler.obtainMessage(2, 2, 0, view).sendToTarget(); // ������Լ��Ͽ����ӣ���������׳�Exception�������ߵ�����
		} catch (SocketException e) {
			e.printStackTrace();
			if (e.getMessage().equals("Connection reset")) {
				if (printWriter != null) {
					printWriter.close();
				}
				try {
					mSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				handler.obtainMessage(2, 3, 1, view).sendToTarget(); // ����������
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (mSocket != null) {
	            try {
					mSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
