package com.vovan.phonedash.widgets;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.vovan.phonedash.R;
import com.vovan.phonedash.WidgetContainerLayout;
import com.vovan.phonedash.dataProviders.HealthRegistry;
import com.vovan.phonedash.dataProviders.HealthRegistry.*;
import com.vovan.phonedash.drawables.PillGradientDrawable;
import com.vovan.phonedash.widgetInfos.StepsWidgetInfo;

import java.util.Locale;

public class StepsWidget
		extends DashboardWidgetBase<StepsWidgetInfo>
		implements ClickableWidget {
	private enum WidgetMode { STEPS, DISTANCE }

	private static final int[] STEP_MODE_COLORS = new int[]{0xFFB41417, 0xFFBC224E};
	private static final int[] DISTANCE_MODE_COLORS = new int[]{0xFFB4165A, 0xFF8F1F7B};

	private final View rootView;
	private final RelativeLayout outerFrame;
	private final ImageView shoeIcon;
	private final TextView stepsTitleTextView;
	private final TextView stepCountOverlayTextView;
	private final ProgressBar progressBar;
	private final View progressContainer;
	private final PillGradientDrawable backgroundDrawable;
	private Activity activity;

	private final HealthUpdateCallback healthUpdateCallback = () -> {
		activity.runOnUiThread(() -> setWidgetInfo(new StepsWidgetInfo(
				HealthRegistry.stepCount,
				HealthRegistry.distance
		)));
	};

	private WidgetMode mode = WidgetMode.STEPS;
	private int stepGoal;
	private int distanceGoal;

	@SuppressLint("InflateParams")
	public StepsWidget(Context context, WidgetContainerLayout container, int gridX, int gridY, int stepGoal, int distanceGoal) {
		super(context, container, gridX, gridY);

		this.activity = (Activity) context;
		this.rootView = LayoutInflater.from(context).inflate(R.layout.steps_widget, null);

		this.outerFrame = this.rootView.findViewById(R.id.outer_frame);
		this.shoeIcon = this.rootView.findViewById(R.id.shoe_icon);
		this.stepsTitleTextView = this.rootView.findViewById(R.id.steps_title);
		this.stepCountOverlayTextView = this.rootView.findViewById(R.id.step_count_overlay);
		this.progressBar = this.rootView.findViewById(R.id.step_progress_bar);
		this.progressContainer = this.rootView.findViewById(R.id.progress_container);

		// Initialize capsule background with max corner radius & gradient stroke for outer_frame
		this.backgroundDrawable = new PillGradientDrawable();
		this.backgroundDrawable.setFillColor(0xFF000000); // Solid black fill
		this.backgroundDrawable.setStrokeGradientColors(STEP_MODE_COLORS);
		this.backgroundDrawable.setGridFillRatio(1.0f);
		this.outerFrame.setBackground(this.backgroundDrawable);

		this.setProgressBarGradientColors(new int[] {getDarkenedColor(STEP_MODE_COLORS[0],0.25f), getDarkenedColor(STEP_MODE_COLORS[1],0.25f)});

		this.setStepGoal(stepGoal);
		this.setDistanceGoal(distanceGoal);

		// Apply monospace / Iosevka charon mono font
		this.setupTypeface(context);

		this.addStateChangeListeners();

		// Auto-scale elements based on cell dimensions
		this.rootView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
			int width = right - left;
			int height = bottom - top;
			if (width > 0 && height > 0 && (width != (oldRight - oldLeft) || height != (oldBottom - oldTop))) {
				adjustScaling(width, height);
			}
		});
	}

	private void setupTypeface(Context context) {
		Typeface typeface = getTypeface(context);
		if (typeface != null) {
			this.stepsTitleTextView.setTypeface(typeface, Typeface.BOLD);
			this.stepCountOverlayTextView.setTypeface(typeface, Typeface.BOLD);
		} else {
			this.stepsTitleTextView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
			this.stepCountOverlayTextView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
		}
	}

	private static Typeface getTypeface(Context context) {
		try {
			int fontId = context.getResources().getIdentifier("iosevka_charon_mono", "font", context.getPackageName());
			return (fontId != 0) ? ResourcesCompat.getFont(context, fontId) : null;
		} catch (Exception e) {
			return null;
		}
	}

	public int getStepGoal() { return this.stepGoal; }
	public void setStepGoal(int newGoal) {
		this.stepGoal = newGoal;
		if (this.mode == WidgetMode.STEPS) {
			this.progressBar.setMax(newGoal);
			this.stepCountOverlayTextView.setText(
					String.format(Locale.getDefault(), "%d / %d", HealthRegistry.stepCount, this.stepGoal)
			);
		}
	}
	public int getDistanceGoal() { return this.distanceGoal; }
	public void setDistanceGoal(int newGoal) {
		this.distanceGoal = newGoal;
		if (this.mode == WidgetMode.DISTANCE) {
			this.progressBar.setMax(newGoal);
			this.stepCountOverlayTextView.setText(
					String.format(Locale.getDefault(), "%d / %d m", HealthRegistry.distance, this.distanceGoal)
			);
		}
	}

	/**
	 * Allows changing the progress bar fill gradient colors dynamically in code.
	 *
	 * @param colors Array of ARGB colors for the linear gradient fill (e.g. startColor and endColor).
	 */
	private void setProgressBarGradientColors(int[] colors) {
		if (colors == null || colors.length < 2) {
			return;
		}
		Drawable progressDrawable = this.progressBar.getProgressDrawable();
		if (progressDrawable != null) {
			Drawable mutated = progressDrawable.mutate();
			if (mutated instanceof LayerDrawable) {
				LayerDrawable layerDrawable = (LayerDrawable) mutated;
				Drawable clipItem = layerDrawable.findDrawableByLayerId(android.R.id.progress);
				if (clipItem instanceof ClipDrawable) {
					Drawable inner = ((ClipDrawable) clipItem).getDrawable();
					if (inner instanceof GradientDrawable) {
						((GradientDrawable) inner).setColors(colors);
					}
				} else if (clipItem instanceof GradientDrawable) {
					((GradientDrawable) clipItem).setColors(colors);
				}
			}
		}
	}

	private void adjustScaling(int width, int height) {
		// 1. Outer Frame: occupies bottom 48% of total height, 95% of total width
		int frameHeight = Math.round((float) height * 0.48f);
		int frameWidth = Math.round((float) width * 0.95f);

		ViewGroup.LayoutParams frameParams = this.outerFrame.getLayoutParams();
		if (frameParams != null) {
			frameParams.height = frameHeight;
			frameParams.width = frameWidth;
			this.outerFrame.setLayoutParams(frameParams);
		}

		// Frame stroke width
		float strokeWidth = Math.max(3f, frameHeight * 0.08f);
		this.backgroundDrawable.setStrokeWidth(strokeWidth);

		// Frame internal paddings
		int padX = Math.round(frameWidth * 0.05f);
		int padTop = Math.round(frameHeight * 0.08f);
		int padBottom = Math.round(frameHeight * 0.12f);
		this.outerFrame.setPadding(padX, padTop, padX, padBottom);

		// 2. Shoe Icon: offset to top-left, height spans from top (y=0) to ~58% of total widget height
		int shoeHeight = Math.round((float) height * 0.58f);
		int shoeWidth = Math.round((float) width * 0.4f);
		if (this.shoeIcon.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
			RelativeLayout.LayoutParams shoeParams = (RelativeLayout.LayoutParams) this.shoeIcon.getLayoutParams();
			shoeParams.height = shoeHeight;
			shoeParams.width = shoeWidth;
			shoeParams.setMarginStart(Math.round((float) width * 0.025f));
			this.shoeIcon.setLayoutParams(shoeParams);
		}

		// 3. "STEPS:" Label text size
		float titleTextSize = frameHeight * 0.22f;
		this.stepsTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);

		// 4. Progress Container Height & Step Overlay Text Size
		int progressContainerHeight = Math.round(frameHeight * 0.38f);
		ViewGroup.LayoutParams progressParams = this.progressContainer.getLayoutParams();
		if (progressParams != null) {
			progressParams.height = progressContainerHeight;
			this.progressContainer.setLayoutParams(progressParams);
		}

		float overlayTextSize = progressContainerHeight * 0.62f;
		this.stepCountOverlayTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, overlayTextSize);
	}

	@Override
	public int width() {
		return 2;
	}

	@Override
	public int height() {
		return 1;
	}

	@Override
	public void startWidget() {
		super.startWidget();
		HealthRegistry.addHealthUpdateCallback(this.healthUpdateCallback);
		this.healthUpdateCallback.onUpdate();
	}

	@Override
	public void pauseWidget() {
		super.pauseWidget();
		HealthRegistry.removeHealthUpdateCallback(this.healthUpdateCallback);
	}

	@Override
	public void setWidgetInfo(StepsWidgetInfo info) {
		if (this.mode == WidgetMode.STEPS) {
			this.stepCountOverlayTextView.setText(
					String.format(Locale.getDefault(), "%d / %d", info.steps, this.stepGoal)
			);
			this.progressBar.setMax(this.stepGoal);
			this.progressBar.setProgress(Math.min(info.steps, this.stepGoal));
		} else {
			this.stepCountOverlayTextView.setText(
					String.format(Locale.getDefault(), "%d / %d m", info.distance, this.distanceGoal)
			);
			this.progressBar.setMax(this.distanceGoal);
			this.progressBar.setProgress(Math.min(info.distance, this.distanceGoal));
		}
	}

	@Override
	public StepsWidgetInfo getDefaultWidgetInfo() {
		return new StepsWidgetInfo(4000, 3250);
	}

	@Override
	public View getView() {
		return this.rootView;
	}

	@Override
	public void onWidgetRemoved() {
		super.onWidgetRemoved();
		this.activity = null;
	}

	@Override
	public void onClick(View view) {
		if (this.mode == WidgetMode.STEPS) {
			// change to Distance mode
			this.mode = WidgetMode.DISTANCE;
			this.stepsTitleTextView.setText("DISTANCE:");
			this.shoeIcon.setImageResource(R.drawable.shoe_purple);
			this.backgroundDrawable.setStrokeGradientColors(DISTANCE_MODE_COLORS);
			this.setProgressBarGradientColors(new int[] {
					getDarkenedColor(DISTANCE_MODE_COLORS[0], 0.25f), getDarkenedColor(DISTANCE_MODE_COLORS[1], 0.25f)
			});
		} else {
			// change to Steps mode
			this.mode = WidgetMode.STEPS;
			this.stepsTitleTextView.setText("STEPS:");
			this.shoeIcon.setImageResource(R.drawable.shoe);
			this.backgroundDrawable.setStrokeGradientColors(STEP_MODE_COLORS);
			this.setProgressBarGradientColors(new int[] {
					getDarkenedColor(STEP_MODE_COLORS[0], 0.25f), getDarkenedColor(STEP_MODE_COLORS[1], 0.25f)
			});
		}
		this.setWidgetInfo(new StepsWidgetInfo(
				HealthRegistry.stepCount,
				HealthRegistry.distance
		));
	}

	private static int getDarkenedColor(int argbColor, float x) {
		int a = (argbColor >> 24) & 0xFF;
		int r = (argbColor >> 16) & 0xFF;
		int g = (argbColor >> 8)  & 0xFF;
		int b = argbColor & 0xFF;

		float factor = 1.0f - x;

		r = (int)(r * factor);
		g = (int)(g * factor);
		b = (int)(b * factor);

		return (a << 24) | (r << 16) | (g << 8) | b;
	}
}
