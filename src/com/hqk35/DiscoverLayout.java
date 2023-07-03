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
			} else if (msg.what == 2) { // ͨ��arg1���ֹ㲥��ɨ�裬ͨ��arg2ָʾ״̬
				if (msg.arg1 == 1) { // �㲥
					isServing = msg.arg2 == 1;
				} else if (msg.arg1 == 2) { // ɨ��
					isDiscovering = msg.arg2 == 1;
				}
				if (isServing) { // �㲥��
					if (isDiscovering) {
						discover_status.setText("�㲥�ѿ��� | ����ɨ��");
					} else {
						discover_status.setText("�㲥�ѿ���");
					}
				} else { // �㲥��
					if (isDiscovering) {
						discover_status.setText("�㲥�ر� | ����ɨ��");
					} else {
						discover_status.setText("�㲥�ر�");
					}
				}
			} else if (msg.what == 3) {
				NsdServiceInfo service = (NsdServiceInfo) msg.obj;
				if (msg.arg1 == 1) { // �ҵ��豸
					for (int i = 0; i < nearby_device_list.getChildCount(); i++) {
						if (((ServiceItemLayout) nearby_device_list.getChildAt(i)).isSame(service)) {
							return;
						}
					}
					// �����һ�����豸
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
					// ����ǲ����б��е��豸�����ߵ�����
				}
			} else if (msg.what == 4) { // �����豸
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
		Log.i("���ڿɼ��Ըı�", "visibility" + visibility);
		isWindowVisible = visibility == View.VISIBLE;
		onVisibilityChanged(); // ������ֹͣNSD����
    }
	private boolean isViewVisible = false;
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (changedView == this) {
			Log.i("�ɼ��Ըı�", "visibility" + visibility);
			isViewVisible = visibility == View.VISIBLE;
			onVisibilityChanged(); // ������ֹͣNSD����
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
        serviceInfo.setServiceName(Settings.Global.getString(context.getContentResolver(), "unified_device_name")); // �����ͬһ�����¹㲥�����������ͻ�����ƿ��ܻ��
        serviceInfo.setServiceType(SERVICE_TYPE); // �÷����Э��ʹ����
        serviceInfo.setPort(port);
        
        registrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
            	myServiceName = NsdServiceInfo.getServiceName(); // �����ͻ��ʵ�ʹ㲥������
                Log.i("��������", myServiceName);
                handler.obtainMessage(2, 1, 1).sendToTarget(); // �㲥�ѿ���
            }
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            	handler.obtainMessage(1, "NSD�㲥����ʧ�ܣ������룺" + errorCode).sendToTarget();
            }
            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) { // ��������NsdManager.unregisterService()������˼�����ʱ����
                handler.obtainMessage(2, 1, 0).sendToTarget(); // �㲥�ѹر�
            }
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            	handler.obtainMessage(1, "NSD�㲥�ر�ʧ�ܣ������룺" + errorCode).sendToTarget();
            }
        };
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }
	private String myServiceName; // �Լ��㲥�ķ�������
	private NsdManager.DiscoveryListener discoveryListener;
	private void discoverServices() {
		discoveryListener = new NsdManager.DiscoveryListener() {
	        @Override
	        public void onDiscoveryStarted(String regType) {
                handler.obtainMessage(2, 2, 1).sendToTarget(); // ɨ���ѿ���
	        }
	        @Override
	        public void onServiceFound(NsdServiceInfo service) {
	            if (service.getServiceName().equals(myServiceName)) {
	            	handler.obtainMessage(3, 1, 1, service).sendToTarget(); // �ҵ��Լ�
	            } else {
	            	handler.obtainMessage(3, 1, 0, service).sendToTarget(); // �ҵ��豸
	            }
	        }
	        @Override
	        public void onServiceLost(NsdServiceInfo service) {
	        	handler.obtainMessage(3, 2, 0, service).sendToTarget(); // ��ʧ�豸
	        }
	        @Override
	        public void onDiscoveryStopped(String serviceType) {
                handler.obtainMessage(2, 2, 0).sendToTarget(); // ɨ���ѹر�
	        }
	        @Override
	        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
	            nsdManager.stopServiceDiscovery(this);
            	handler.obtainMessage(1, "NSDɨ�迪��ʧ�ܣ������룺" + errorCode).sendToTarget();
	        }
	        @Override
	        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
	            nsdManager.stopServiceDiscovery(this);
            	handler.obtainMessage(1, "NSDɨ��ر�ʧ�ܣ������룺" + errorCode).sendToTarget();
	        }
	    };
		nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
	}
	
	private View.OnClickListener device_item_onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ServiceItemLayout device_item = (ServiceItemLayout) v;
			if (device_item.isResolved()) { // �ѽ�������������
				
			} else { // δ������׼������
				try {
					nsdManager.resolveService(device_item.getServiceInfo(), resolveListener);
				} catch (IllegalArgumentException e) {
					Toast.makeText(context, "���ڽ����У����Ժ�����", Toast.LENGTH_SHORT).show();
				}
			}
		}
	};
	private NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            handler.obtainMessage(1, "NSD�������ʧ�ܣ������룺" + errorCode).sendToTarget();
        }
        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            handler.obtainMessage(4, serviceInfo).sendToTarget(); // �����豸
        }
    };
}
