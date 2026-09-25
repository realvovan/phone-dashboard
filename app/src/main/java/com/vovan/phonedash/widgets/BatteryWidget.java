package com.vovan.phonedash.widgets;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.BatteryManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vovan.phonedash.R;
import com.vovan.phonedash.WidgetContainer;
import com.vovan.phonedash.widgetInfos.BatteryWidgetInfo;

import java.util.Locale;

/**
 * A battery status widget that displays current charge percentage,
 * a visual battery level, and a charging indicator.
 * Updates are triggered by system battery broadcasts.
 */
public class BatteryWidget extends DashboardWidgetBase<BatteryWidgetInfo> {
	private final View rootView;
	private final ProgressBar progressBar;
	private final TextView percentageTextView;
	private final ImageView chargingBolt;
	private final View batteryContainer;
	private final Context context;
	private boolean isWidgetRunning = false;

	private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				onBatteryChanged(intent);
			}
		}
	};

	@SuppressLint("InflateParams")
	public BatteryWidget(Context context, WidgetContainer container, int gridX, int gridY) {
		super(container, gridX, gridY);

		this.rootView = LayoutInflater.from(context).inflate(R.layout.battery_widget, null);
		this.progressBar = this.rootView.findViewById(R.id.battery_progress);
		this.percentageTextView = this.rootView.findViewById(R.id.battery_percentage);
		this.chargingBolt = this.rootView.findViewById(R.id.charging_bolt);
		this.batteryContainer = this.rootView.findViewById(R.id.battery_container);
		this.context = context.getApplicationContext();

		// Start/stop listening for battery updates based on view lifecycle
		this.addStateChangeListeners();

		// Dynamic scaling based on widget size
		this.rootView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
			int width = right - left;
			int height = bottom - top;
			if (width > 0 && height > 0 && (width != (oldRight - oldLeft) || height != (oldBottom - oldTop))) {
				adjustScaling(width, height);
			}
		});
	}

	/**
	 * Adjusts the size of the battery icon and text to fit the widget dimensions.
	 */
	private void adjustScaling(int width, int height) {
		float scale = Math.min(width, height) / 100f; // Base scale on 100px size

		// Scale the battery container
		ViewGroup.LayoutParams containerParams = batteryContainer.getLayoutParams();
		containerParams.width = (int) (64 * scale);
		containerParams.height = (int) (32 * scale);
		batteryContainer.setLayoutParams(containerParams);

		// Scale the progress bar (fill) relative to the container
		ViewGroup.MarginLayoutParams progressParams = (ViewGroup.MarginLayoutParams) progressBar.getLayoutParams();
		progressParams.width = (int) (48 * scale);
		progressParams.height = (int) (22 * scale);
		progressParams.setMarginStart((int) (5 * scale));
		progressBar.setLayoutParams(progressParams);

		// Scale the bolt
		ViewGroup.LayoutParams boltParams = chargingBolt.getLayoutParams();
		boltParams.width = (int) (20 * scale);
		boltParams.height = (int) (20 * scale);
		chargingBolt.setLayoutParams(boltParams);

		// Adjust text size
		percentageTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16 * scale);
	}

	/**
	 * Extracts battery info from the intent and updates the UI components.
	 */
	private void onBatteryChanged(Intent intent) {
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		int batteryPct = (level >= 0 && scale > 0) ? (level * 100) / scale : 0;

		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
				status == BatteryManager.BATTERY_STATUS_FULL;

		this.setWidgetInfo(new BatteryWidgetInfo(batteryPct,isCharging));
	}

	@Override
	public int width() { return 1; }

	@Override
	public int height() { return 1; }

	@Override
	public void startWidget() {
		if (!this.isWidgetRunning) {
			this.isWidgetRunning = true;
			IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			// registerReceiver returns the last sticky intent for the filter
			Intent stickyIntent = this.context.registerReceiver(this.batteryReceiver, filter);
			if (stickyIntent != null) {
				this.onBatteryChanged(stickyIntent); // sets the value on widget start
			}
		}
	}

	@Override
	public void pauseWidget() {
		try {
			this.context.unregisterReceiver(this.batteryReceiver);
		} catch (IllegalArgumentException e) {
			// Receiver not registered
		}
	}

	@Override
	public void setWidgetInfo(BatteryWidgetInfo info) {
		this.progressBar.setProgress(info.percentage);
		this.percentageTextView.setText(String.format(Locale.getDefault(), "%d%%", info.percentage));
		this.chargingBolt.setVisibility(info.isCharging ? View.VISIBLE : View.GONE);
		int color;
		if (info.percentage <= 20) {
			color = 0xFFF44336;
		} else if (info.isCharging) {
			color = 0xFF47C450;
		} else {
			color = 0xFF5b5b5b;
		}
		this.progressBar.setProgressTintList(ColorStateList.valueOf(color));
	}

	@Override
	public BatteryWidgetInfo getDefaultWidgetInfo() { return new BatteryWidgetInfo(67,false); }
	@Override
	public View getView() {
		return this.rootView;
	}
}
