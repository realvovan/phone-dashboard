package com.vovan.phonedash.widgets;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.vovan.phonedash.R;
import com.vovan.phonedash.WidgetContainer;
import com.vovan.phonedash.drawables.StrokedTextView;
import com.vovan.phonedash.drawables.TallTextView;
import com.vovan.phonedash.widgetInfos.ClockWidgetInfo;

import java.util.Calendar;
import java.util.Locale;

public class TallClockWidget
		extends DashboardWidgetBase<ClockWidgetInfo>
		implements LoopingWidget, ClickableWidget {
	private static final int[] clockColors = new int[] {
			0xFF102133, // blue
			0xFF103314, // green
			0xFF361F1F, // pink
			0xFF241A09, // yellow
			0xFF26140B, // orange
			0xFF0D091F, // purple
			0xFF1F0917, // magenta
	};

	private final View rootView;
	private final TallTextView hoursTextView;
	private final TallTextView minutesTextView;
	private final TallTextView secondsTextView;
	private final StrokedTextView separator1;
	private final StrokedTextView separator2;

	private ValueAnimator colorAnimator;

	private int currentTextColor = clockColors[0];
	private int currentColorIndex = 0;

	@SuppressLint("InflateParams")
	public TallClockWidget(Context context, WidgetContainer container, int gridX, int gridY) {
		super(context, container, gridX, gridY);

		this.rootView = LayoutInflater.from(context).inflate(R.layout.tall_clock_widget, null);

		this.hoursTextView = this.rootView.findViewById(R.id.hours);
		this.minutesTextView = this.rootView.findViewById(R.id.minutes);
		this.secondsTextView = this.rootView.findViewById(R.id.seconds);
		this.separator1 = this.rootView.findViewById(R.id.separator1);
		this.separator2 = this.rootView.findViewById(R.id.separator2);

		this.addStateChangeListeners();
		this.setWidgetColor(this.currentTextColor, 0L);

		// Adjust text size and stroke when widget cell dimensions change
		this.rootView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
			int width = right - left;
			int height = bottom - top;
			if (width > 0 && height > 0 && (width != (oldRight - oldLeft) || height != (oldBottom - oldTop))) {
				adjustScaling(width, height);
			}
		});

		this.setDefaultWidgetInfo();
	}

	private void applyLetterSpacing(float spacing) {
		this.hoursTextView.setLetterSpacing(spacing);
		this.minutesTextView.setLetterSpacing(spacing);
		this.secondsTextView.setLetterSpacing(spacing);
		this.separator1.setLetterSpacing(spacing);
		this.separator2.setLetterSpacing(spacing);
	}

	/**
	 * Sets the primary text color and updates the stroke color to be the same color
	 * with its HSV Value component set to 90% by default.
	 *
	 * @param textColor The primary fill color for the numbers and colons.
	 */
	public void setWidgetColor(int textColor, long easingTime) {
		this.currentTextColor = textColor;
		int strokeColor = getStrokeColorFromHsvValue(textColor, 0.9f);
		this.applyColors(textColor, strokeColor, easingTime);
	}

	private void applyColors(int textColor, int strokeColor, long easingTime) {
		if (this.colorAnimator != null) {
			this.colorAnimator.cancel();
		}

		final int startTextColor = this.hoursTextView.getCurrentTextColor();
		final int startStrokeColor = this.hoursTextView.getStrokeColor();
		final ArgbEvaluator evaluator = new ArgbEvaluator();

		this.colorAnimator = ValueAnimator.ofFloat(0f, 1f);
		this.colorAnimator.setDuration(easingTime);
		this.colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		this.colorAnimator.addUpdateListener(animation -> {
			float fraction = animation.getAnimatedFraction();
			int currentTextColor = (int) evaluator.evaluate(fraction, startTextColor, textColor);
			int currentStrokeColor = (int) evaluator.evaluate(fraction, startStrokeColor, strokeColor);
			this.hoursTextView.setTextColor(currentTextColor);
			this.hoursTextView.setStrokeColor(currentStrokeColor);

			this.minutesTextView.setTextColor(currentTextColor);
			this.minutesTextView.setStrokeColor(currentStrokeColor);

			this.secondsTextView.setTextColor(currentTextColor);
			this.secondsTextView.setStrokeColor(currentStrokeColor);

			this.separator1.setTextColor(currentTextColor);
			this.separator1.setStrokeColor(currentStrokeColor);

			this.separator2.setTextColor(currentTextColor);
			this.separator2.setStrokeColor(currentStrokeColor);
		});
		this.colorAnimator.start();
	}

	private void adjustScaling(int width, int height) {
		float targetW = (float) width;
		float targetH = (float) height;

		int paddingX = Math.round(((float) width - targetW) / 2f);
		int paddingY = Math.round(((float) height - targetH) / 2f);
		this.rootView.setPadding(paddingX, paddingY, paddingX, paddingY);

		// Apply letter spacing
		float letterSpacing = 0.02f;
		applyLetterSpacing(letterSpacing);

		// Calculate text size so all 6 digits and 2 separators fit horizontally with letter spacing
		float textSize = Math.min(targetW / 4.7f, targetH / 1.7f);
		float strokeWidth = Math.max(2f, textSize * 0.08f);

		int strokeColor = getStrokeColorFromHsvValue(this.currentTextColor, 0.9f);

		this.hoursTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		this.hoursTextView.setStroke(strokeWidth, strokeColor);

		this.separator1.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		this.separator1.setStroke(strokeWidth, strokeColor);

		this.minutesTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		this.minutesTextView.setStroke(strokeWidth, strokeColor);

		this.separator2.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		this.separator2.setStroke(strokeWidth, strokeColor);

		this.secondsTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		this.secondsTextView.setStroke(strokeWidth, strokeColor);
	}

	@Override
	public int width() {
		return 3;
	}

	@Override
	public int height() {
		return 2;
	}

	@Override
	public long onLoop() {
		Calendar calendar = Calendar.getInstance();
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);

		this.setWidgetInfo(new ClockWidgetInfo(hours, minutes, seconds));

		if (minutes % 30 == 0 && seconds == 0) {
			this.currentColorIndex = (this.currentColorIndex + 1) % clockColors.length;
			this.setWidgetColor(clockColors[this.currentColorIndex], 750L);
		}

		return 1000L;
	}

	@Override
	public void onClick(View view) {
		this.currentColorIndex = (this.currentColorIndex + 1) % clockColors.length;
		this.setWidgetColor(clockColors[this.currentColorIndex], 150L);
	}

	@Override
	public void setWidgetInfo(ClockWidgetInfo info) {
		if (info == null) {
			return;
		}

		this.hoursTextView.setText(String.format(Locale.getDefault(), "%02d", info.hours));
		this.minutesTextView.setText(String.format(Locale.getDefault(), "%02d", info.minutes));
		this.secondsTextView.setText(String.format(Locale.getDefault(), "%02d", info.seconds));
	}

	@Override
	public ClockWidgetInfo getDefaultWidgetInfo() {
		return new ClockWidgetInfo(10, 9, 30);
	}

	@Override
	public View getView() {
		return this.rootView;
	}

	private static int getStrokeColorFromHsvValue(int argbColor, float hsvValuePercentage) {
		float[] hsv = new float[3];
		Color.colorToHSV(argbColor, hsv);
		hsv[2] = hsvValuePercentage;
		return Color.HSVToColor(Color.alpha(argbColor), hsv);
	}
}
