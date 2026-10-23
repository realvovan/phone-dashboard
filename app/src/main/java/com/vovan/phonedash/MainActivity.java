package com.vovan.phonedash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.vovan.phonedash.backend.DashboardServerService;
import com.vovan.phonedash.dataProviders.trivogaProviders.alertsInUaProvider.AlertsProvider;
import com.vovan.phonedash.dataProviders.weatherProviders.OpenMeteo.OpenMeteoWeatherProvider;
import com.vovan.phonedash.widgets.*;

public class MainActivity extends FullScreenActivity {
	private final boolean showGridSquares = false;
	private WidgetContainerLayout container;

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
		Handler handler = new Handler(Looper.getMainLooper());
		// end dependencies

		// backend server
		ContextCompat.startForegroundService(this, new Intent(this, DashboardServerService.class));

		// widget container
		container = new WidgetContainerLayout(this,handler,8,4);

		container.addWidget(ClockWidget.class,0,0);
		container.addWidget(BatteryWidget.class,2,0);
		container.addWidget(StepsWidget.class,0,1,8000,6500);
		container.addWidget(TrivogaWidget.class,2,1,alertsProvider,"м. Київ");
		container.addWidget(WeatherWidget.class,3,0,weatherProvider,"Tetiiv",49.3695,29.6779); // hard coded the coords for my city lol;
		TallClockWidget tallClockWidget = new TallClockWidget(this,container.widgetContainer,5,0);
		tallClockWidget.setWidgetColor(0xFF102133);
		container.addWidget(tallClockWidget);
		handler.postDelayed(() -> {
			// creates the widget after 3.5 seconds so that hopefully it can use the cached response lolol
			container.addWidget(TrivogaWidget.class,2,2,alertsProvider,"Білоцерківський район");
		},3500);

		if (this.showGridSquares) {
			for (int x = 0; x < container.getColumns(); x++) {
				for (int y = 0; y < container.getRows(); y++) {
					container.addWidget(new TestGradientWidget(this, container.widgetContainer, x, y));
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
		handler.post(new Runnable() {
			private final WidgetPixelShifter shifter = new WidgetPixelShifter();
			@Override
			public void run() {
				this.shifter.shift(container);
				handler.postDelayed(this,30000); // shifts every 30 seconds
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		for (DashboardWidgetBase<?> i : container.widgetContainer.getWidgets()) {
			container.widgetContainer.removeWidget(i);
		}
	}
}
