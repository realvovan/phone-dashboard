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
public class GradientSquircleDrawable extends Drawable {
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Path path = new Path();
	private float cornerRadius;
	private float cornerSmoothing = 0.6f;
	private float insetX = 0f;
	private float insetY = 0f;
	private int[] gradientColors;

	public GradientSquircleDrawable(float cornerRadius, int[] gradientColors) {
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
		setInset(inset, inset);
	}

	public void setInset(float insetX, float insetY) {
		if (this.insetX != insetX || this.insetY != insetY) {
			this.insetX = insetX;
			this.insetY = insetY;
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

		float left = bounds.left + insetX;
		float top = bounds.top + insetY;
		float right = bounds.right - insetX;
		float bottom = bounds.bottom - insetY;

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
		SquircleDrawable.createSquirclePath(path,rect,r,smoothing);
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
