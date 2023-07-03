package com.hqk35;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabItemWidget extends LinearLayout {
	public TabItemWidget(Context context, String label, int iconResId) {
		super(context);
		float scale = this.getResources().getDisplayMetrics().density;
		
		setOrientation(VERTICAL);
		setPadding(0, (int) (5f * scale), 0, 0);
		setGravity(Gravity.CENTER_HORIZONTAL);
		
		ImageView icon = new ImageView(context);
		icon.setImageResource(iconResId);
		addView(icon, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		TextView text = new TextView(context);
		text.setTextColor(getResources().getColorStateList(R.color.tab_label_text));
		text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
		text.setText(label);
		addView(text, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}
}
