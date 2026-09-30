package com.vovan.phonedash.drawables;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A custom Drawable that renders a capsule / pill shape (maximum corner radius)
 * with a solid fill background, customizable gradient stroke, and configurable grid slot fill ratio.
 */
public class PillGradientDrawable extends Drawable {
	private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private final RectF fillRect = new RectF();
	private final RectF strokeRect = new RectF();

	private int[] strokeGradientColors = new int[]{0xFFB41417, 0xFFBC224E};
	private int fillColor = 0xFF000000;
	private float strokeWidthPx = 4f;
	private float gridFillRatio = 1.0f;

	public PillGradientDrawable() {
		fillPaint.setStyle(Paint.Style.FILL);
		fillPaint.setColor(fillColor);

		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setStrokeWidth(strokeWidthPx);
	}

	public void setStrokeGradientColors(int[] colors) {
		if (colors != null && colors.length >= 2) {
			this.strokeGradientColors = colors;
			invalidateSelf();
		}
	}

	public void setFillColor(int color) {
		this.fillColor = color;
		this.fillPaint.setColor(color);
		invalidateSelf();
	}

	public void setStrokeWidth(float widthPx) {
		this.strokeWidthPx = Math.max(1f, widthPx);
		this.strokePaint.setStrokeWidth(this.strokeWidthPx);
		invalidateSelf();
	}

	public void setGridFillRatio(float ratio) {
		this.gridFillRatio = Math.min(1.0f, Math.max(0.1f, ratio));
		invalidateSelf();
	}

	@Override
	protected void onBoundsChange(@NonNull Rect bounds) {
		super.onBoundsChange(bounds);
		invalidateSelf();
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		Rect bounds = getBounds();
		float w = bounds.width();
		float h = bounds.height();

		if (w <= 0 || h <= 0) {
			return;
		}

		float targetW = w * gridFillRatio;
		float targetH = h * gridFillRatio;

		float insetX = (w - targetW) / 2f;
		float insetY = (h - targetH) / 2f;

		fillRect.set(
				bounds.left + insetX,
				bounds.top + insetY,
				bounds.left + insetX + targetW,
				bounds.top + insetY + targetH
		);

		// Maximum corner radius = half of target height -> completely rounded capsule / pill shape
		float cornerRadius = fillRect.height() / 2f;

		// 1. Draw solid background fill
		canvas.drawRoundRect(fillRect, cornerRadius, cornerRadius, fillPaint);

		// 2. Draw gradient stroke around border
		float halfStroke = strokeWidthPx / 2f;
		strokeRect.set(
				fillRect.left + halfStroke,
				fillRect.top + halfStroke,
				fillRect.right - halfStroke,
				fillRect.bottom - halfStroke
		);

		float strokeRadius = strokeRect.height() / 2f;

		LinearGradient strokeShader = new LinearGradient(
				strokeRect.left, strokeRect.top,
				strokeRect.right, strokeRect.bottom,
				strokeGradientColors,
				null,
				Shader.TileMode.CLAMP
		);
		strokePaint.setShader(strokeShader);

		canvas.drawRoundRect(strokeRect, strokeRadius, strokeRadius, strokePaint);
	}

	@Override
	public void setAlpha(int alpha) {
		fillPaint.setAlpha(alpha);
		strokePaint.setAlpha(alpha);
		invalidateSelf();
	}

	@Override
	public void setColorFilter(@Nullable ColorFilter colorFilter) {
		fillPaint.setColorFilter(colorFilter);
		strokePaint.setColorFilter(colorFilter);
		invalidateSelf();
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
}
