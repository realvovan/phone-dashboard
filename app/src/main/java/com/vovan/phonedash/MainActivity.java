package com.vovan.phonedash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;

import androidx.activity.EdgeToEdge;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.vovan.phonedash.backend.DashboardServerService;
import com.vovan.phonedash.dataProviders.calendarEventsProviders.CalendarEventsProvider;
import com.vovan.phonedash.dataProviders.calendarEventsProviders.formJsonHttpProvider.CalendarEventsFromHttpProvider;
import com.vovan.phonedash.dataProviders.calendarEventsProviders.fromJsonFileProvider.CalendarEventsFromJsonProvider;
import com.vovan.phonedash.dataProviders.trivogaProviders.alertsInUaProvider.AlertsProvider;
import com.vovan.phonedash.dataProviders.weatherProviders.OpenMeteo.OpenMeteoWeatherProvider;
import com.vovan.phonedash.widgets.*;

public class MainActivity extends FullScreenActivity {
	private final static long CONTAINER_TAP_ANIMATION_LEN = 100L;
	private final static boolean SHOW_GRID_SQUARES = false;
	private WidgetContainerLayout container;
	private WidgetPixelShifter shifter;
	private WidgetContainerDimmer dimmer;
	private Handler handler;
	private final Runnable scaleContainerToOneRunnable = new Runnable() {
		@Override
		public void run() {
			container.animate()
					.setDuration(CONTAINER_TAP_ANIMATION_LEN)
					.scaleX(1f)
					.scaleY(1f)
					.start();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_main);

		ConstraintLayout mainLayout = findViewById(R.id.main);

		ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		// dependencies
		OpenMeteoWeatherProvider weatherProvider = new OpenMeteoWeatherProvider();
		AlertsProvider alertsProvider = new AlertsProvider();
		CalendarEventsProvider calendarEventsProvider = new CalendarEventsFromHttpProvider(BuildConfig.CALENDAR_EVENTS_URL);
		handler = new Handler(Looper.getMainLooper());
		// end dependencies

		// backend server
		ContextCompat.startForegroundService(this, new Intent(this, DashboardServerService.class));

		// widget container
		container = new WidgetContainerLayout(this, handler, 8, 4);

		container.addWidget(TallClockWidget.class, 0, 0);
		container.addWidget(Calendar2by3Widget.class, 5, 0, calendarEventsProvider);
		container.addWidget(BatteryWidget.class, 7, 0);
		container.addWidget(WeatherWidget.class,3,1,weatherProvider,"Tetiiv",49.3695,29.6779); // hard coded the coords for my city lol;
		container.addWidget(StepsWidget.class, 3, 0, 8000, 6500);
		container.addWidget(TrivogaWidget.class,7,1,alertsProvider,"м. Київ");
		container.addWidget(TrivogaWidget.class,7,2,alertsProvider,"Білоцерківський район");

		if (SHOW_GRID_SQUARES) {
			for (int x = 0; x < container.getColumns(); x++) {
				for (int y = 0; y < container.getRows(); y++) {
					container.addWidget(new TestGradientWidget(this, container, x, y));
				}
			}
		}

		// Configure LayoutParams to fill the screen parent container
		ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
				ConstraintLayout.LayoutParams.MATCH_PARENT,
				ConstraintLayout.LayoutParams.MATCH_PARENT
		);
		container.setLayoutParams(layoutParams);

		// Add the container's layout to the main screen layout
		mainLayout.addView(container);

		// pixel shifting for the widgets
		shifter = new WidgetPixelShifter(container, handler);
		shifter.startShifting(30_000L, true);

		// dimming the screen every six minutes
		dimmer = new WidgetContainerDimmer(container, handler);
		dimmer.startDimming(360_000L, 5000L, false);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean handled = super.dispatchTouchEvent(ev);
		if (dimmer.getIsDimming() || (ev.getAction() == MotionEvent.ACTION_DOWN && !handled)) {
			dimmer.resetDimming();
			handler.removeCallbacks(scaleContainerToOneRunnable);
			final float minZoom = 1f;
			final float maxZoom = 1.15f;
			float currentScale = container.getScaleX();
			float newScale = Math.min(
					currentScale + 0.03f * (1f - (currentScale - minZoom) / (maxZoom - minZoom)),
					maxZoom
			);
			container.animate()
					.setDuration(CONTAINER_TAP_ANIMATION_LEN)
					.scaleX(newScale)
					.scaleY(newScale)
					.start();
			handler.postDelayed(scaleContainerToOneRunnable, CONTAINER_TAP_ANIMATION_LEN);
		}

		return handled;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		shifter.close();
		dimmer.close();
		for (DashboardWidgetBase<?> i : container.getWidgets()) {
			boolean isRemoved = container.removeWidget(i);
			if (!isRemoved)
				Log.w("MainActivity",String.format(
						"Failed to remove widget of type %s on MainActivity destroying",
						i.getClass().getName()
				));
		}
	}
}
