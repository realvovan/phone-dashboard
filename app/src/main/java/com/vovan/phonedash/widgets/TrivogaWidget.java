package com.vovan.phonedash.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vovan.phonedash.R;
import com.vovan.phonedash.WidgetContainerLayout;
import com.vovan.phonedash.dataProviders.HttpCallback;
import com.vovan.phonedash.dataProviders.trivogaProviders.Trivoga;
import com.vovan.phonedash.dataProviders.trivogaProviders.TrivogaProvider;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TrivogaWidget
		extends DashboardWidgetBase<Trivoga>
		implements LoopingWidget {
	private static final int SAFE_COLOR = 0xFF4EB14E;  // Green
	private static final int YELLOW_COLOR = 0xFFF2DB33;
	private static final int RED_COLOR = 0xFFBF2727;
	private static final int GRAY_COLOR = 0xFF676767;
	private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

	private final TrivogaProvider trivogaProvider;

	private final View rootView;
	private final FrameLayout circleContainer;
	private final ImageView noTrivogaShield;
	private final ImageView trivogaAlert;
	private final TextView provinceLabel;

	private String regionName;

	@SuppressLint("InflateParams")
	public TrivogaWidget(
			Context context,
			WidgetContainerLayout container,
			int gridX, int gridY,
			TrivogaProvider trivogaProvider,
			String regionName)
	{
		super(context, container, gridX, gridY);

		this.trivogaProvider = trivogaProvider;
		this.rootView = LayoutInflater.from(context).inflate(R.layout.trivoga_widget, null);

		this.circleContainer = this.rootView.findViewById(R.id.circle_container);
		this.noTrivogaShield = this.rootView.findViewById(R.id.no_trivoga_shield);
		this.trivogaAlert = this.rootView.findViewById(R.id.trivoga_alert);
		this.provinceLabel = this.rootView.findViewById(R.id.province_label);

		this.setRegion(regionName);
		this.addStateChangeListeners();

		// Auto-scale layout elements when the widget dimensions change
		this.rootView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
			int width = right - left;
			int height = bottom - top;
			if (width > 0 && height > 0 && (width != (oldRight - oldLeft) || height != (oldBottom - oldTop))) {
				adjustScaling(width, height);
			}
		});

		// Initialize with default widget info
		this.setDefaultWidgetInfo();
	}

	/**
	 * Adjusts circle size, icon dimensions, font size, and margins dynamically based on grid cell size.
	 */
	private void adjustScaling(int width, int height) {
		int cellSize = Math.min(width, height);

		int padding = Math.round(cellSize * 0.08f);
		this.rootView.setPadding(padding, padding, padding, padding);

		int circleSize = Math.round(cellSize * 0.52f);
		ViewGroup.LayoutParams circleParams = this.circleContainer.getLayoutParams();
		if (circleParams != null) {
			circleParams.width = circleSize;
			circleParams.height = circleSize;
			this.circleContainer.setLayoutParams(circleParams);
		}

		int iconSize = Math.round(circleSize * 0.60f);
		ViewGroup.LayoutParams shieldParams = this.noTrivogaShield.getLayoutParams();
		if (shieldParams != null) {
			shieldParams.width = iconSize;
			shieldParams.height = iconSize;
			this.noTrivogaShield.setLayoutParams(shieldParams);
		}

		ViewGroup.LayoutParams alertParams = this.trivogaAlert.getLayoutParams();
		if (alertParams != null) {
			alertParams.width = iconSize;
			alertParams.height = iconSize;
			this.trivogaAlert.setLayoutParams(alertParams);
		}

		int marginTop = Math.round(cellSize * 0.04f);
		if (this.provinceLabel.getLayoutParams() instanceof LinearLayout.LayoutParams) {
			LinearLayout.LayoutParams labelParams = (LinearLayout.LayoutParams) this.provinceLabel.getLayoutParams();
			labelParams.topMargin = marginTop;
			this.provinceLabel.setLayoutParams(labelParams);
		}

		float textSize = cellSize * 0.09f;
		this.provinceLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
	}

	public void setRegion(String newRegion) {
		Trivoga defaultTrivoga = this.getDefaultWidgetInfo();
		defaultTrivoga.locationTitle = newRegion;
		this.regionName = newRegion;
		this.setWidgetInfo(defaultTrivoga);
	}

	public String getRegion() { return this.regionName; }

	@Override
	public int width() {
		return 1;
	}

	@Override
	public int height() {
		return 1;
	}

	@Override
	public long onLoop() {
		this.trivogaProvider.getAlertInRegion(this.regionName, new HttpCallback<Trivoga>() {
			@Override
			public void onSuccess(Trivoga trivoga) {
				setWidgetInfo(trivoga);
			}

			@Override
			public void onFailure(Exception e) {
				Log.w("TrivogaWidget", Objects.requireNonNull(e.getMessage()));
				setDefaultWidgetInfo();
			}
		});
		return 20000L;
	}

	@Override
	public void setWidgetInfo(Trivoga info) {
		this.provinceLabel.setText(getProvinceLabelText(info));

		if (info.alertLevel == Trivoga.AlertLevel.GREEN) {
			this.noTrivogaShield.setVisibility(View.VISIBLE);
			this.trivogaAlert.setVisibility(View.GONE);
			this.circleContainer.getBackground().setTint(SAFE_COLOR);
		} else {
			this.noTrivogaShield.setVisibility(View.GONE);
			this.trivogaAlert.setVisibility(View.VISIBLE);
			if (info.alertLevel == Trivoga.AlertLevel.RED) {
				this.circleContainer.getBackground().setTint(RED_COLOR);
			} else if (info.alertLevel == Trivoga.AlertLevel.YELLOW) {
				this.circleContainer.getBackground().setTint(YELLOW_COLOR);
			} else {
				this.circleContainer.getBackground().setTint(GRAY_COLOR);
			}
		}
	}

	@Override
	public Trivoga getDefaultWidgetInfo() {
		Trivoga t = new Trivoga();
		t.locationTitle = this.regionName == null ? "Test region" : this.regionName;
		t.startedAt = null;
		t.alertLevel = Trivoga.AlertLevel.UNKNOWN;
		return t;
	}

	@Override
	public View getView() {
		return this.rootView;
	}

	private static String getProvinceLabelText(Trivoga trivoga) {
		String regionText =
				"м. Київ".equals(trivoga.locationTitle) || "м. Севастополь".equals(trivoga.locationTitle)
				? trivoga.locationTitle + "\n "
				: trivoga.locationTitle.replaceFirst(" ","\n");


		if (trivoga.startedAt == null) {
			return regionText;
		} else {
			ZonedDateTime localTime = trivoga.startedAt.atZone(ZoneId.systemDefault());
			String formattedTime = localTime.format(timeFormatter);
			return String.format(
					"%s (з %s)",
					regionText,
					formattedTime
			);
		}
	}
}
