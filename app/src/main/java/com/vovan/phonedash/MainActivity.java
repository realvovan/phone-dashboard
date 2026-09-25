package com.vovan.phonedash;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.vovan.phonedash.dataProviders.HttpCallback;
import com.vovan.phonedash.dataProviders.trivogaProviders.Trivoga;
import com.vovan.phonedash.dataProviders.trivogaProviders.alertsInUaProvider.AlertsProvider;
import com.vovan.phonedash.dataProviders.weatherProviders.OpenMeteo.OpenMeteoWeatherProvider;
import com.vovan.phonedash.widgets.*;

public class MainActivity extends FullScreenActivity {
	// MainActivity owns the WidgetContainer instance(s)
	private WidgetContainer widgetContainer;

	private final boolean showGridSquares = false;

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

		widgetContainer = new WidgetContainer(8, 4,handler);

		widgetContainer.addWidget(new ClockWidget(this, widgetContainer, 0, 0));
		widgetContainer.addWidget(new BatteryWidget(this, widgetContainer, 2, 0));
		widgetContainer.addWidget(new TrivogaWidget(this, widgetContainer, alertsProvider, "м. Київ", 2, 1));
		widgetContainer.addWidget(new TrivogaWidget(this, widgetContainer, alertsProvider, "Білоцерківський район", 2, 2));
		widgetContainer.addWidget(new WeatherWidget(this, widgetContainer, weatherProvider, 3, 0,"Tetiiv",49.3695,29.6779)); // hard coded the coords for my city lol;

		if (this.showGridSquares) {
			for (int x = 0; x < widgetContainer.columns; x++) {
				for (int y = 0; y < widgetContainer.rows; y++) {
					if (widgetContainer.isCellEmpty(x, y)) {
						widgetContainer.addWidget(new TestGradientWidget(this, widgetContainer, x, y));
					}
				}
			}
		}

		// Instantiate the separate class/view responsible for displaying the container contents
		WidgetContainerLayout containerLayout = new WidgetContainerLayout(this, widgetContainer);

		// Configure LayoutParams to fill the screen parent container
		ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
				ConstraintLayout.LayoutParams.MATCH_PARENT,
				ConstraintLayout.LayoutParams.MATCH_PARENT
		);
		containerLayout.setLayoutParams(layoutParams);

		// Add the container's layout to the main screen layout
		mainLayout.addView(containerLayout);
	}
}