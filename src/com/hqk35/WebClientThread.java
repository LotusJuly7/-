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
            Socket socket = new Socket(ipAddress, MainActivity.port);      //����һ
            Log.i("�ͻ���", "�õ�Socket");
            socket.setSoTimeout(60000);
            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(   //�����
                    socket.getOutputStream(), "UTF-8")), true);
            Log.i("�ͻ���", "�õ�PrintWriter");
            printWriter.print(message);
            printWriter.flush();
            printWriter.close();
            Log.i("�ͻ���", "flush");
            /*String receiveMsg;
            while ((receiveMsg = in.readLine()) != null) {                                      //������
            	Log.i("�ͻ���", "�յ����ݣ�" + receiveMsg);
            }*/
            socket.close();
            Log.i("�ͻ���", "�ر�Socket");
            handler.obtainMessage(1, 0, 0, new Object[] {ipAddress, message}).sendToTarget();
        } catch (UnknownHostException e) {
        	handler.obtainMessage(1, 1, 0, "IP��ַ��Ч").sendToTarget();
        } catch (IOException e) {
            Log.e("�ͻ���", ("����ʧ�ܣ�" + e.getMessage()));   //���Socket�����ȡʧ�ܣ������ӽ���ʧ�ܣ����ߵ�����߼�
        	handler.obtainMessage(1, 1, 0, "����ʧ��").sendToTarget();
        }
	}
}
