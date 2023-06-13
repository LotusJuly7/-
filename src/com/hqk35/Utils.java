package com.hqk35;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Utils {
	public static String getIpAddress(String prefix) {
    	try {
	    	for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	    		NetworkInterface intf = en.nextElement();
	    		if (intf.getName().startsWith(prefix)) {
    	    		for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
    	    		    InetAddress inetAddress = enumIpAddr.nextElement();
    	    		    if (inetAddress.getClass() == Inet4Address.class) {
    	    		    	return inetAddress.getHostAddress().toString();
    	    		    }
    	    		}
	    		}
	    	}
    	} catch (SocketException e) {
    		e.printStackTrace();
    	}
    	return null;
    }
}
