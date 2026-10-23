package com.vovan.phonedash.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class TallTextView extends StrokedTextView {
	private float stretchY = 2.5f;

	public TallTextView(Context context) {
		super(context);
	}

	public TallTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TallTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setStretchY(float stretchY) {
		this.stretchY = stretchY;
		this.requestLayout();
		this.invalidate();
	}

	public float getStretchY() {
		return this.stretchY;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredHeight() * stretchY));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		canvas.scale(1f, stretchY);
		super.onDraw(canvas);
		canvas.restore();
	}
}
