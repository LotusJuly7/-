package com.hqk35;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import android.os.Handler;
import android.util.Log;

public class WebServerThread extends Thread {
	Handler handler;
	ServerSocket mServerSocket;
	boolean isLooping = true;
	public WebServerThread(Handler handler) {
		this.handler = handler;
	}
	@Override
	public void run() {
		try {
		    mServerSocket = new ServerSocket(MainActivity.port);
		    //mServerSocket.setReuseAddress(true);
		    while (isLooping) {
		        // ���տͻ����׽��֡�
		        if (!mServerSocket.isClosed()) {
		        	try {
			        	// �������ܿͻ��ˡ�
			            Socket socket = mServerSocket.accept();
			            byte[] ipAddress = socket.getInetAddress().getAddress();
			            Log.i("������", "===server accept===");
				        new ServerThread(socket, handler, ipAddress).start();
		        	} catch (SocketException e) {
		        		break;
		        	}
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
			mServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class ServerThread extends Thread {
	Socket mSocket;
	Handler handler;
	byte[] ipAddress;
	public ServerThread(Socket socket, Handler handler, byte[] ipAddress) {
		mSocket = socket;
		this.handler = handler;
		this.ipAddress = ipAddress;
	}
	@Override
	public void run() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "utf-8"));
			Log.i("������", "��ʼ�߳�");
			StringBuilder result = new StringBuilder();
			char[] buffer = new char[1024];
			int len;
			while ((len = reader.read(buffer)) != -1) { // ���д>0�Ļ�����©���յ���
				Log.i("������", "��ȡһ������");
				result.append(buffer, 0, len);
			}
			Log.i("������", "��ȡ������");
			String message = result.toString();
			Log.i("����", message);
			handler.obtainMessage(2, new Object[] {ipAddress, message}).sendToTarget();
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
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
            try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
