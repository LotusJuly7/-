package com.hqk35;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DiscoverLayout extends LinearLayout {
	private Context context;
	private NsdManager nsdManager;
	public DiscoverLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
	}
	private TextView discover_status;
	private LinearLayout nearby_device_list;
	@Override
	protected void onFinishInflate() {
		Log.i("DiscoverLayout", "onFinishInflate");
		discover_status = (TextView) findViewById(R.id.discover_status);
		nearby_device_list = (LinearLayout) findViewById(R.id.nearby_device_list);
	}
	private Handler handler = new Handler() {
		boolean isServing = false, isDiscovering = false;
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				Toast.makeText(context, (String) msg.obj, Toast.LENGTH_SHORT).show();
			} else if (msg.what == 2) { // 通过arg1区分广播和扫描，通过arg2指示状态
				if (msg.arg1 == 1) { // 广播
					isServing = msg.arg2 == 1;
				} else if (msg.arg1 == 2) { // 扫描
					isDiscovering = msg.arg2 == 1;
				}
				if (isServing) { // 广播开
					if (isDiscovering) {
						discover_status.setText("广播已开启 | 正在扫描");
					} else {
						discover_status.setText("广播已开启");
					}
				} else { // 广播关
					if (isDiscovering) {
						discover_status.setText("广播关闭 | 正在扫描");
					} else {
						discover_status.setText("广播关闭");
					}
				}
			} else if (msg.what == 3) {
				NsdServiceInfo service = (NsdServiceInfo) msg.obj;
				if (msg.arg1 == 1) { // 找到设备
					for (int i = 0; i < nearby_device_list.getChildCount(); i++) {
						if (((ServiceItemLayout) nearby_device_list.getChildAt(i)).isSame(service)) {
							return;
						}
					}
					// 如果是一个新设备
					ServiceItemLayout device_item = new ServiceItemLayout(context, service, msg.arg2 == 1);
					device_item.setOnClickListener(device_item_onClick);
					nearby_device_list.addView(device_item);
				} else if (msg.arg1 == 2) {
					for (int i = 0; i < nearby_device_list.getChildCount(); i++) {
						ServiceItemLayout device_item = (ServiceItemLayout) nearby_device_list.getChildAt(i);
						if (device_item.isSame(service)) {
							nearby_device_list.removeView(device_item);
							return;
						}
					}
					// 如果是不在列表中的设备，会走到这里
				}
			} else if (msg.what == 4) { // 解析设备
				NsdServiceInfo serviceInfo = (NsdServiceInfo) msg.obj;
				for (int i = 0; i < nearby_device_list.getChildCount(); i++) {
	            	ServiceItemLayout device_item = (ServiceItemLayout) nearby_device_list.getChildAt(i);
					if (device_item.isSame(serviceInfo)) {
						device_item.resolve(serviceInfo);
						break;
					}
				}
			}
		}
	};
	
	private boolean isWindowVisible = false;
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		Log.i("窗口可见性改变", "visibility" + visibility);
		isWindowVisible = visibility == View.VISIBLE;
		onVisibilityChanged(); // 启动或停止NSD功能
    }
	private boolean isViewVisible = false;
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (changedView == this) {
			Log.i("可见性改变", "visibility" + visibility);
			isViewVisible = visibility == View.VISIBLE;
			onVisibilityChanged(); // 启动或停止NSD功能
		}
    }
	private void onVisibilityChanged() {
		if (isWindowVisible && isViewVisible) {
			if (registrationListener == null) {
				registerService(WebServerThread.port);
			}
			if (discoveryListener == null) {
				discoverServices();
			}
		} else {
			if (registrationListener != null) {
				nsdManager.unregisterService(registrationListener);
				registrationListener = null;
				myServiceName = null;
			}
			if (discoveryListener != null) {
				nsdManager.stopServiceDiscovery(discoveryListener);
				discoveryListener = null;
				nearby_device_list.removeAllViews();
			}
		}
	}
	private static final String SERVICE_TYPE = "_nsdchat._tcp.";
	private NsdManager.RegistrationListener registrationListener;
	private void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(Settings.Global.getString(context.getContentResolver(), "unified_device_name")); // 如果与同一网络下广播的其他服务冲突，名称可能会变
        serviceInfo.setServiceType(SERVICE_TYPE); // 该服务的协议和传输层
        serviceInfo.setPort(port);
        
        registrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            	myServiceName = NsdServiceInfo.getServiceName(); // 解决冲突后实际广播的名称
                Log.i("服务名称", myServiceName);
                handler.obtainMessage(2, 1, 1).sendToTarget(); // 广播已开启
            }
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            	handler.obtainMessage(1, "NSD广播开启失败，错误码：" + errorCode).sendToTarget();
            }
            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) { // 仅当调用NsdManager.unregisterService()并传入此监听器时触发
                handler.obtainMessage(2, 1, 0).sendToTarget(); // 广播已关闭
            }
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            	handler.obtainMessage(1, "NSD广播关闭失败，错误码：" + errorCode).sendToTarget();
            }
        };
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }
	private String myServiceName; // 自己广播的服务名称
	private NsdManager.DiscoveryListener discoveryListener;
	private void discoverServices() {
		discoveryListener = new NsdManager.DiscoveryListener() {
	        @Override
	        public void onDiscoveryStarted(String regType) {
                handler.obtainMessage(2, 2, 1).sendToTarget(); // 扫描已开启
	        }
	        @Override
	        public void onServiceFound(NsdServiceInfo service) {
	            if (service.getServiceName().equals(myServiceName)) {
	            	handler.obtainMessage(3, 1, 1, service).sendToTarget(); // 找到自己
	            } else {
	            	handler.obtainMessage(3, 1, 0, service).sendToTarget(); // 找到设备
	            }
	        }
	        @Override
	        public void onServiceLost(NsdServiceInfo service) {
	        	handler.obtainMessage(3, 2, 0, service).sendToTarget(); // 丢失设备
	        }
	        @Override
	        public void onDiscoveryStopped(String serviceType) {
                handler.obtainMessage(2, 2, 0).sendToTarget(); // 扫描已关闭
	        }
	        @Override
	        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
	            nsdManager.stopServiceDiscovery(this);
            	handler.obtainMessage(1, "NSD扫描开启失败，错误码：" + errorCode).sendToTarget();
	        }
	        @Override
	        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
	            nsdManager.stopServiceDiscovery(this);
            	handler.obtainMessage(1, "NSD扫描关闭失败，错误码：" + errorCode).sendToTarget();
	        }
	    };
		nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
	}
	
	private View.OnClickListener device_item_onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ServiceItemLayout device_item = (ServiceItemLayout) v;
			if (device_item.isResolved()) { // 已解析，可以连接
				
			} else { // 未解析，准备解析
				try {
					nsdManager.resolveService(device_item.getServiceInfo(), resolveListener);
				} catch (IllegalArgumentException e) {
					Toast.makeText(context, "正在解析中，请稍后重试", Toast.LENGTH_SHORT).show();
				}
			}
		}
	};
	private NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            handler.obtainMessage(1, "NSD服务解析失败，错误码：" + errorCode).sendToTarget();
        }
        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            handler.obtainMessage(4, serviceInfo).sendToTarget(); // 解析设备
        }
    };
}
