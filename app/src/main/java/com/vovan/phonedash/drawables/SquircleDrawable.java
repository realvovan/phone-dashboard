package com.vovan.phonedash.drawables;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A custom Drawable that renders a solid color within a smooth squircle shape.
 * Supports Figma corner smoothing (default ~0.6).
 */
public class SquircleDrawable extends Drawable {
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Path path = new Path();
	private float cornerRadius;
	private float cornerSmoothing = 0.6f;
	private float insetX = 0f;
	private float insetY = 0f;
	private int fillColor;

	public SquircleDrawable(float cornerRadius, int fillColor) {
		this.cornerRadius = cornerRadius;
		this.fillColor = fillColor;
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(fillColor);
	}

	public void setCornerRadius(float radius) {
		if (this.cornerRadius != radius) {
			this.cornerRadius = radius;
			updatePath();
			invalidateSelf();
		}
	}

	public void setInset(float inset) {
		setInset(inset, inset);
	}

	public void setInset(float insetX, float insetY) {
		if (this.insetX != insetX || this.insetY != insetY) {
			this.insetX = insetX;
			this.insetY = insetY;
			updatePath();
			invalidateSelf();
		}
	}

	public void setFillColor(int color) {
		if (this.fillColor != color) {
			this.fillColor = color;
			paint.setColor(color);
			invalidateSelf();
		}
	}

	public int getFillColor() {
		return fillColor;
	}

	public void setCornerSmoothing(float smoothing) {
		if (this.cornerSmoothing != smoothing) {
			this.cornerSmoothing = smoothing;
			updatePath();
			invalidateSelf();
		}
	}

	@Override
	protected void onBoundsChange(@NonNull Rect bounds) {
		super.onBoundsChange(bounds);
		updatePath();
	}

	private void updatePath() {
		Rect bounds = getBounds();
		int w = bounds.width();
		int h = bounds.height();
		if (w <= 0 || h <= 0) return;

		float left = bounds.left + insetX;
		float top = bounds.top + insetY;
		float right = bounds.right - insetX;
		float bottom = bounds.bottom - insetY;

		if (right <= left || bottom <= top) return;

		RectF rect = new RectF(left, top, right, bottom);

		createSquirclePath(path, rect, cornerRadius, cornerSmoothing);
	}

	static void createSquirclePath(Path path, RectF rect, float r, float smoothing) {
		path.reset();
		float left = rect.left;
		float top = rect.top;
		float right = rect.right;
		float bottom = rect.bottom;
		float w = rect.width();
		float h = rect.height();

		if (r <= 0) {
			path.addRect(rect, Path.Direction.CW);
			return;
		}

		// Clamp corner radius to max half of width or height
		float clampedR = Math.min(r, Math.min(w, h) / 2f);

		// Corner smoothing distance multiplier d = r * (1 + smoothing)
		float p = Math.min(clampedR * (1f + smoothing), Math.min(w, h) / 2f);

		// Control point offsets for smooth cubic bezier curves
		float c2 = p * 0.55228475f;

		// Start top edge after top-left corner
		path.moveTo(left + p, top);

		// Top edge -> Top-Right corner
		path.lineTo(right - p, top);
		path.cubicTo(right - p + c2, top, right, top + p - c2, right, top + p);

		// Right edge -> Bottom-Right corner
		path.lineTo(right, bottom - p);
		path.cubicTo(right, bottom - p + c2, right - p + c2, bottom, right - p, bottom);

		// Bottom edge -> Bottom-Left corner
		path.lineTo(left + p, bottom);
		path.cubicTo(left + p - c2, bottom, left, bottom - p + c2, left, bottom - p);

		// Left edge -> Top-Left corner
		path.lineTo(left, top + p);
		path.cubicTo(left, top + p - c2, left + p - c2, top, left + p, top);

		path.close();
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		canvas.drawPath(path, paint);
	}

	@Override
	public void setAlpha(int alpha) {
		paint.setAlpha(alpha);
		invalidateSelf();
	}

	@Override
	public void setColorFilter(@Nullable ColorFilter colorFilter) {
		paint.setColorFilter(colorFilter);
		invalidateSelf();
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
}
