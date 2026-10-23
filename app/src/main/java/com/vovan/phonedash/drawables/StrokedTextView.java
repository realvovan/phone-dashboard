package com.vovan.phonedash.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class StrokedTextView extends AppCompatTextView {
	private float strokeWidth = 0f;
	private int strokeColor = 0xFF000000;

	public StrokedTextView(Context context) {
		super(context);
	}

	public StrokedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public StrokedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setStroke(float strokeWidth, int strokeColor) {
		this.strokeWidth = strokeWidth;
		this.strokeColor = strokeColor;
		invalidate();
	}

	public void setStrokeWidth(float strokeWidth) {
		this.strokeWidth = strokeWidth;
		invalidate();
	}

	public void setStrokeColor(int strokeColor) {
		this.strokeColor = strokeColor;
		invalidate();
	}

	public float getStrokeWidth() {
		return this.strokeWidth;
	}

	public int getStrokeColor() {
		return this.strokeColor;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (this.strokeWidth > 0) {
			TextPaint paint = getPaint();
			int currentTextColor = getCurrentTextColor();

			// 1. Draw text stroke outline
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(this.strokeWidth);
			setTextColor(this.strokeColor);
			super.onDraw(canvas);

			// 2. Draw text fill inside stroke
			paint.setStyle(Paint.Style.FILL);
			setTextColor(currentTextColor);
		}
		super.onDraw(canvas);
	}
}
