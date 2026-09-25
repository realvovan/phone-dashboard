package com.vovan.phonedash.drawables;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A custom Drawable that renders a linear gradient within a smooth squircle shape.
 * Supports Figma corner smoothing (default ~0.6).
 */
public class SquircleDrawable extends Drawable {
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Path path = new Path();
	private float cornerRadius;
	private float cornerSmoothing = 0.6f;
	private float inset = 0f;
	private int[] gradientColors;

	public SquircleDrawable(float cornerRadius, int[] gradientColors) {
		this.cornerRadius = cornerRadius;
		this.gradientColors = gradientColors != null ? gradientColors : new int[]{0xFF59B8F2, 0xFF388CE0};
	}

	public void setCornerRadius(float radius) {
		if (this.cornerRadius != radius) {
			this.cornerRadius = radius;
			updatePathAndShader();
			invalidateSelf();
		}
	}

	public void setInset(float inset) {
		if (this.inset != inset) {
			this.inset = inset;
			updatePathAndShader();
			invalidateSelf();
		}
	}

	public void setGradientColors(int[] colors) {
		if (colors != null && colors.length >= 2) {
			this.gradientColors = colors;
			updatePathAndShader();
			invalidateSelf();
		}
	}

	public void setCornerSmoothing(float smoothing) {
		if (this.cornerSmoothing != smoothing) {
			this.cornerSmoothing = smoothing;
			updatePathAndShader();
			invalidateSelf();
		}
	}

	@Override
	protected void onBoundsChange(@NonNull Rect bounds) {
		super.onBoundsChange(bounds);
		updatePathAndShader();
	}

	private void updatePathAndShader() {
		Rect bounds = getBounds();
		int w = bounds.width();
		int h = bounds.height();
		if (w <= 0 || h <= 0) return;

		float left = bounds.left + inset;
		float top = bounds.top + inset;
		float right = bounds.right - inset;
		float bottom = bounds.bottom - inset;

		if (right <= left || bottom <= top) return;

		RectF rect = new RectF(left, top, right, bottom);

		LinearGradient shader = new LinearGradient(
				0, top, 0, bottom,
				gradientColors,
				null,
				Shader.TileMode.CLAMP
		);
		paint.setShader(shader);

		createSquirclePath(path, rect, cornerRadius, cornerSmoothing);
	}

	private static void createSquirclePath(Path path, RectF rect, float r, float smoothing) {
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
