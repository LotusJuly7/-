package com.hqk35;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Handler;
import android.util.Log;

public class WebClientThread extends Thread {
	Handler handler;
	String ipAddress, message;
	public WebClientThread(Handler handler, String ipAddress, String message) {
		this.handler = handler;
		this.ipAddress = ipAddress;
		this.message = message;
	}
	@Override
	public void run() {
		try {
            Socket socket = new Socket(ipAddress, MainActivity.port);      //步骤一
            Log.i("客户端", "得到Socket");
            socket.setSoTimeout(60000);
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(   //步骤二
                    socket.getOutputStream(), "UTF-8")), true);
            Log.i("客户端", "得到PrintWriter");
            printWriter.print(message);
            printWriter.flush();
            printWriter.close();
            Log.i("客户端", "flush");
            /*String receiveMsg;
            while ((receiveMsg = in.readLine()) != null) {                                      //步骤三
            	Log.i("客户端", "收到数据：" + receiveMsg);
            }*/
            socket.close();
            Log.i("客户端", "关闭Socket");
            handler.obtainMessage(1, 0, 0, new Object[] {ipAddress, message}).sendToTarget();
        } catch (UnknownHostException e) {
        	handler.obtainMessage(1, 1, 0, "IP地址无效").sendToTarget();
        } catch (IOException e) {
            Log.e("客户端", ("连接失败：" + e.getMessage()));   //如果Socket对象获取失败，即连接建立失败，会走到这段逻辑
        	handler.obtainMessage(1, 1, 0, "连接失败").sendToTarget();
        }
	}
}
