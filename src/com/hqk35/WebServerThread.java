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
		        // 接收客户端套接字。
		        if (!mServerSocket.isClosed()) {
		        	try {
			        	// 阻塞接受客户端。
			            Socket socket = mServerSocket.accept();
			            byte[] ipAddress = socket.getInetAddress().getAddress();
			            Log.i("服务器", "===server accept===");
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
			Log.i("服务器", "开始线程");
			StringBuilder result = new StringBuilder();
			char[] buffer = new char[1024];
			int len;
			while ((len = reader.read(buffer)) != -1) { // 如果写>0的话，会漏掉空的行
				Log.i("服务器", "读取一段数据");
				result.append(buffer, 0, len);
			}
			Log.i("服务器", "读取完数据");
			String message = result.toString();
			Log.i("数据", message);
			handler.obtainMessage(2, new Object[] {ipAddress, message}).sendToTarget();
            /*// 返回数据
			OutputStream os = null;
            try {
            	os = mSocket.getOutputStream();
            	os.write("HTTP/1.1 200\r\nContent-Type: text/plain\r\n\r\nHello\r\n".getBytes());
            	os.flush();
            	Log.i("服务器", "flush");
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
