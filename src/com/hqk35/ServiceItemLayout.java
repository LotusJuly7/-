package com.hqk35;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ServiceItemLayout extends FrameLayout {
	private float scale;
	private TextView info;
	public ServiceItemLayout(Context context, NsdServiceInfo service, boolean isSelf) {
		super(context);
		scale = getResources().getDisplayMetrics().density;
		this.service = service;
		
		LinearLayout.LayoutParams device_item_lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		device_item_lp.bottomMargin = (int) (12f * scale);
		setLayoutParams(device_item_lp);
		setBackgroundResource(R.drawable.discover_device_item_bg);
		
		LinearLayout device_info = new LinearLayout(context);
		LayoutParams device_info_lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		device_info_lp.leftMargin = (int) (15f * scale);
		device_info_lp.rightMargin = (int) (30f * scale);
		device_info_lp.topMargin = device_info_lp.bottomMargin = (int) (12f * scale);
		device_info.setOrientation(LinearLayout.VERTICAL);
		addView(device_info, device_info_lp);
		
		TextView f5e = new TextView(context);
		LinearLayout.LayoutParams f5e_lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		f5e_lp.bottomMargin = (int) (8f * scale);
		f5e.setSingleLine(true);
		f5e.setEllipsize(TextUtils.TruncateAt.MIDDLE);
		f5e.setIncludeFontPadding(false);
		f5e.setTextColor(0xff03081a);
		f5e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
		f5e.setText(service.getServiceName());
		device_info.addView(f5e, f5e_lp);
		
		info = new TextView(context);
		info.setSingleLine(true);
		info.setEllipsize(TextUtils.TruncateAt.END);
		info.setIncludeFontPadding(false);
		info.setTextColor(0xffa6a6a6);
		info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
		info.setText(service.getServiceType());
		device_info.addView(info, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		LayoutParams dce_lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		dce_lp.rightMargin = (int) (15f * scale);
		if (isSelf) {
			TextView e10 = new TextView(context);
			e10.setSingleLine(true);
			e10.setTextColor(0xffb0b3bf);
			e10.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
			e10.setText("����");
			addView(e10, dce_lp);
		} else {
			ImageView dce = new ImageView(context);
			dce.setImageResource(R.drawable.skin_icon_arrow_right_normal);
			dce.setContentDescription(null);
			addView(dce, dce_lp);
		}
	}
	private NsdServiceInfo service;
	public NsdServiceInfo getServiceInfo() {
		return service;
	}
	public boolean isSame(NsdServiceInfo service) {
		if (this.service.getServiceName().equals(service.getServiceName())) {
			// �������ƥ�䣬�ٿ����������Ƿ�ƥ��
			String[] serviceType = this.service.getServiceType().split("\\.");
			String[] serviceType_new = service.getServiceType().split("\\.");
			for (int i = 0; i < serviceType.length; i++) {
				String s = serviceType[i]; // ���еķ�����������
				if (s.isEmpty()) {
					continue;
				}
				for (int j = 0; ; j++) { // ���µķ���������Ѱ�����е�ÿ��������������
					if (j < serviceType_new.length) {
						if (serviceType_new[j] != null && serviceType_new[j].equals(s)) { // ����µķ����������ҵ���ƥ����
							serviceType_new[j] = null;
							break;
						}
					} else {
						return false; // �µķ���������δ�ҵ�ƥ����
					}
				}
			}
			for (int j = 0; j < serviceType_new.length; j++) {
				if (serviceType_new[j] != null && !serviceType_new[j].isEmpty()) { // ����µķ��������д���δ��ƥ����˵�������
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
	private boolean isResolved = false;
	public boolean isResolved() {
		return isResolved;
	}
	public void resolve(NsdServiceInfo service) {
		this.service = service;
		isResolved = true;
		InetAddress host = service.getHost();
		if (host instanceof Inet4Address) {
			info.setText(host.getHostAddress() + ":" + service.getPort());
		} else if (host instanceof Inet6Address) {
			info.setText("[" + host.getHostAddress() + "]:" + service.getPort());
		}
	}
}
