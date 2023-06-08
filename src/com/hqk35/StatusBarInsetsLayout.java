package com.hqk35;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class StatusBarInsetsLayout extends FrameLayout {

	public StatusBarInsetsLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected boolean fitSystemWindows(Rect insets) {
        insets.bottom = 0;
        super.fitSystemWindows(insets);
        return false;
    }
}
