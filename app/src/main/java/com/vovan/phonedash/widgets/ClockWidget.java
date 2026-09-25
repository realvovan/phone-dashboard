package com.vovan.phonedash.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vovan.phonedash.R;
import com.vovan.phonedash.widgetInfos.ClockWidgetInfo;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Locale;

public class ClockWidget extends LoopingWidget<ClockWidgetInfo> {
	private final View rootView;
	private final TextView hoursTextView;
	private final TextView minutesTextView;
	private final TextView secondsTextView;
	private final TextView separator1;
	private final TextView separator2;

	@SuppressLint("InflateParams")
	public ClockWidget(Context context, Handler handler, int gridX, int gridY) {
		super(handler,gridX, gridY);
		this.rootView = LayoutInflater.from(context).inflate(R.layout.clock_widget, null);

		this.hoursTextView = this.rootView.findViewById(R.id.hours);
		this.minutesTextView = this.rootView.findViewById(R.id.minutes);
		this.secondsTextView = this.rootView.findViewById(R.id.seconds);
		this.separator1 = this.rootView.findViewById(R.id.separator1);
		this.separator2 = this.rootView.findViewById(R.id.separator2);

		// Automatically start/stop updates based on view attachment lifecycle
		this.addAutomaticWidgetStartListeners();
//		this.rootView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//			@Override
//			public void onViewAttachedToWindow(@NonNull View v) {
//				startWidget();
//			}
//
//			@Override
//			public void onViewDetachedFromWindow(@NotNull View v) {
//				pauseWidget();
//			}
//		});

		// Adjust text size when the view is laid out or its size changes
		this.rootView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
			int width = right - left;
			int height = bottom - top;
			if (width > 0 && height > 0 && (width != (oldRight - oldLeft) || height != (oldBottom - oldTop))) {
				adjustTextSize(width, height);
			}
		});
	}

	/**
	 * Adjusts the text size of all TextViews based on the widget's dimensions.
	 */
	private void adjustTextSize(int width, int height) {
		// Calculate a text size that scales with the widget.
		// We use the smaller of:
		// 1. A fraction of the height (to fit vertically)
		// 2. A fraction of the width (to fit "00:00:00" horizontally)
		float textSize = Math.min(height * 0.5f, width / 7.5f);

		this.hoursTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		this.minutesTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		this.secondsTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * 0.85f); // Seconds slightly smaller
		this.separator1.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		this.separator2.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
	}

	@Override
	public int width() { return 2; }

	@Override
	public int height() { return 1;	}

	@Override
	protected long onLoop() {
		Calendar calendar = Calendar.getInstance();
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);

		setWidgetInfo(new ClockWidgetInfo(hours,minutes,seconds));

		return 1000;
	}

	@Override
	public void setWidgetInfo(ClockWidgetInfo info) {
		this.hoursTextView.setText(String.format(Locale.getDefault(), "%02d", info.hours));
		this.minutesTextView.setText(String.format(Locale.getDefault(), "%02d", info.minutes));
		this.secondsTextView.setText(String.format(Locale.getDefault(), "%02d", info.seconds));
	}

	@Override
	public ClockWidgetInfo getDefaultWidgetInfo() { return new ClockWidgetInfo(10,9,30); }

	@Override
	public View getView() {
		return this.rootView;
	}
}
