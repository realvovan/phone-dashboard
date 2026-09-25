package com.vovan.phonedash.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Handler;

import com.vovan.phonedash.R;
import com.vovan.phonedash.dataProviders.HttpCallback;
import com.vovan.phonedash.dataProviders.weatherProviders.Weather;
import com.vovan.phonedash.dataProviders.weatherProviders.WeatherProvider;
import com.vovan.phonedash.drawables.SquircleDrawable;

import java.util.Locale;

/**
 * A 2x2 Weather Dashboard Widget created based on the Figma design specification.
 * Automatically scales paddings, font sizes, margins, and squircle corner radius
 * based on the allocated cell dimensions.
 */
public class WeatherWidget extends LoopingWidget<Weather> {
	// colors
	private static final int[] CLEAR_SKY_DAY_COLORS = new int[]{0xFF59B8F2,0xFF388CE0};
	// also use for drizzle night or snow night
	private static final int[] CLEAR_SKY_NIGHT_COLORS = new int[]{0xFF070C43,0xFF102A63};
	private static final int[] PARTLY_CLOUDY_DAY_COLORS = new int[]{0xFFA1C0D3,0xFF2789EA};
	private static final int[] PARTLY_CLOUDY_NIGHT_COLORS = new int[]{0xFF282A38,0xFF0A1353};
	private static final int[] CLOUDY_COLORS = new int[]{0xFFA1C0D3,0xFF638AB1};
	private static final int[] FOGGY_COLORS = new int[]{0xFFCFBA8B,0xFF715E2B};
	private static final int[] DRIZZLE_DAY_COLORS = new int[]{0xFF4263E8,0xFF715E2B};
	private static final int[] RAIN_OR_SHOWER_COLORS = new int[]{0xFF465471,0xFF314165};
	private static final int[] SNOW_DAY_COLORS = new int[]{0xFF97D6F0,0xFF3C8DA3};
	private static final int[] HEAVY_SNOW_COLORS = new int[]{0xFFC7D4F0,0xFF7E899F};
	private static final int[] THUNDER_COLORS = new int[]{0xFF313024,0xFF232831};
	private static final int[] WEATHER_UNAVAILABLE_COLORS = new int[]{0xFF3D444A,0xFF3D444A};

	// Reference base dimensions from Figma spec
	private static final float BASE_DESIGN_SIZE = 170f;
	private static final float BASE_PADDING = 14.4f;
	private static final float BASE_CORNER_RADIUS = 20f;
	private static final float WIDGET_GRID_FILL_RATIO = 0.95f;

	private final WeatherProvider weatherProvider;

	private double latitude;
	private double longitude;

	private final View rootView;
	private final ImageView weatherIcon;
	private final TextView cityName;
	private final TextView currentTemp;
	private final TextView conditions;
	private final TextView highTemp;
	private final TextView lowTemp;
	private final TextView rainReminder;
	private final TextView weatherUnavailable;
	private final SquircleDrawable backgroundDrawable;

	@SuppressLint("InflateParams")
	public WeatherWidget(
			Context context,
			Handler handler,
			WeatherProvider weatherProvider,
			int gridX, int gridY,
			String city, double latitude, double longitude
	) {
		super(handler, gridX, gridY);

		this.weatherProvider = weatherProvider;

		this.rootView = LayoutInflater.from(context).inflate(R.layout.weather_widget, null);

		this.weatherIcon = this.rootView.findViewById(R.id.weather_icon);
		this.cityName = this.rootView.findViewById(R.id.city_name);
		this.currentTemp = this.rootView.findViewById(R.id.current_temp);
		this.conditions = this.rootView.findViewById(R.id.conditions);
		this.highTemp = this.rootView.findViewById(R.id.high_temp);
		this.lowTemp = this.rootView.findViewById(R.id.low_temp);
		this.rainReminder = this.rootView.findViewById(R.id.rain_reminder);
		this.weatherUnavailable = this.rootView.findViewById(R.id.weather_unavailable);

		this.setCity(city,latitude,longitude);

		// Setup Squircle background with default clear sky gradient
		this.backgroundDrawable = new SquircleDrawable(BASE_CORNER_RADIUS, CLEAR_SKY_DAY_COLORS);
		this.rootView.setBackground(this.backgroundDrawable);

		this.addAutomaticWidgetStartListeners();

		// Dynamic auto-scaling based on actual widget dimensions
		this.rootView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
			int width = right - left;
			int height = bottom - top;
			if (width > 0 && height > 0 && (width != (oldRight - oldLeft) || height != (oldBottom - oldTop))) {
				adjustScaling(width, height);
			}
		});
	}

	/**
	 * Adjusts font sizes, paddings, corner radius, and margins dynamically based on widget size.
	 */
	private void adjustScaling(int width, int height) {
		float containerSize = Math.min(width, height);
		float squircleSize = containerSize * WIDGET_GRID_FILL_RATIO;
		float inset = (containerSize - squircleSize) / 2f;

		// 1. Inset squircle shape within allocated grid cell
		backgroundDrawable.setInset(inset);

		// 2. Scale factor relative to base design size (170px)
		float scale = squircleSize / BASE_DESIGN_SIZE;

		// 3. Root padding (outer inset gap + inner design padding)
		int totalPadding = Math.round(inset + (BASE_PADDING * scale));
		rootView.setPadding(totalPadding, totalPadding, totalPadding, totalPadding);

		// 4. Background squircle corner radius
		backgroundDrawable.setCornerRadius(BASE_CORNER_RADIUS * scale);

		// 5. Icon size and font sizes (scaled relative to squircle size)
		int iconSize = Math.round(36f * scale);
		ViewGroup.LayoutParams iconParams = weatherIcon.getLayoutParams();
		if (iconParams != null) {
			iconParams.width = iconSize;
			iconParams.height = iconSize;
			weatherIcon.setLayoutParams(iconParams);
		}
		View topRow = rootView.findViewById(R.id.top_row);
		if (topRow != null) {
			topRow.setMinimumHeight(Math.round(28f * scale));
		}
		cityName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 13f * scale);
		currentTemp.setTextSize(TypedValue.COMPLEX_UNIT_PX, 52f * scale);
		conditions.setTextSize(TypedValue.COMPLEX_UNIT_PX, 12f * scale);
		highTemp.setTextSize(TypedValue.COMPLEX_UNIT_PX, 13f * scale);
		lowTemp.setTextSize(TypedValue.COMPLEX_UNIT_PX, 13f * scale);
		rainReminder.setTextSize(TypedValue.COMPLEX_UNIT_PX, 12f * scale);

		// 6. Margins/spacing inside High/Low vertical block
		int spacing = Math.round(2f * scale);
		if (highTemp.getLayoutParams() instanceof LinearLayout.LayoutParams) {
			LinearLayout.LayoutParams highParams = (LinearLayout.LayoutParams) highTemp.getLayoutParams();
			highParams.topMargin = spacing;
			highTemp.setLayoutParams(highParams);
		}
		if (lowTemp.getLayoutParams() instanceof LinearLayout.LayoutParams) {
			LinearLayout.LayoutParams lowParams = (LinearLayout.LayoutParams) lowTemp.getLayoutParams();
			lowParams.topMargin = spacing;
			lowTemp.setLayoutParams(lowParams);
		}
	}
	
	public void setConditionGradient(int[] gradientColors) {
		this.backgroundDrawable.setGradientColors(gradientColors);
	}

	public void setCity(String city, double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;

		this.cityName.setText(city);
	}

	public void setWeatherUnavailable() {
		this.weatherUnavailable.setVisibility(View.VISIBLE);
		this.setConditionGradient(WEATHER_UNAVAILABLE_COLORS);

		this.weatherIcon.setVisibility(View.GONE);
		this.currentTemp.setVisibility(View.GONE);
		this.conditions.setVisibility(View.GONE);
		this.highTemp.setVisibility(View.GONE);
		this.lowTemp.setVisibility(View.GONE);
		this.rainReminder.setVisibility(View.GONE);
	}

	@Override
	public int width() { return 2; }

	@Override
	public int height() { return 2;	}

	@Override
	public void setWidgetInfo(Weather info) {
		this.currentTemp.setText(String.format(Locale.getDefault(),"%d°",Math.round(info.currentTemperature)));
		this.lowTemp.setText(String.format(Locale.getDefault(),"L:%d°",Math.round(info.minTemperature)));
		this.highTemp.setText(String.format(Locale.getDefault(),"H:%d°",Math.round(info.maxTemperature)));
		this.conditions.setText(getConditionsFromWeatherCode(info.weatherCode));
		this.weatherIcon.setImageResource(getIconFromWeatherCode(info.weatherCode, info.isDay));
		this.setConditionGradient(getColorsFromWeatherCode(info.weatherCode,info.isDay));

		if (info.possibleFuturePptInHours != -1f) {
			String formattedHours;
			if (info.possibleFuturePptInHours < 0.67f) {
				formattedHours = "less than an hour";
			} else if (info.possibleFuturePptInHours < 1.2f) {
				formattedHours = "1 hour";
			} else if (info.possibleFuturePptInHours < 1.8f) {
				formattedHours = "1.5 hours";
			} else {
				formattedHours = "2 hours";
			}
			this.rainReminder.setText(String.format(Locale.getDefault(),"%d%% precip. chance\nin %s",info.possibleFuturePptProbability,formattedHours));

			this.conditions.setVisibility(View.GONE);
			this.highTemp.setVisibility(View.GONE);
			this.lowTemp.setVisibility(View.GONE);
			this.rainReminder.setVisibility(View.VISIBLE);
		} else {
			this.conditions.setVisibility(View.VISIBLE);
			this.highTemp.setVisibility(View.VISIBLE);
			this.lowTemp.setVisibility(View.VISIBLE);
			this.rainReminder.setVisibility(View.GONE);
		}

		this.weatherIcon.setVisibility(View.VISIBLE);
		this.currentTemp.setVisibility(View.VISIBLE);
	}

	@Override
	public Weather getDefaultWidgetInfo() { return new Weather(); }

	@Override
	public View getView() {
		return this.rootView;
	}

	@Override
	protected long onLoop() {
		this.weatherProvider.getCurrentWeather(this.latitude, this.longitude, new HttpCallback<Weather>() {
			@Override
			public void onSuccess(Weather weather) {
				setWidgetInfo(weather);
				Log.i("WeatherWidget","Updated weather");
			}

			@Override
			public void onFailure(Exception e) {
				setWeatherUnavailable();
				Log.w("WeatherWidget",String.format(
						"Encountered an error while trying to fetch the current weather: %s",
						e.getMessage()
				));
			}
		});
		return 1_800_000; // 30 minutes
	}

	private static String getConditionsFromWeatherCode(int code) {
		switch (code) {
			case 0: return "Clear";
			case 1: return "Mostly clear";
			case 2: return "Partly cloudy";
			case 3: return "Cloudy";
			case 45: case 48: return "Foggy";
			case 51: case 53: case 55: return "Drizzle";
			case 61: case 63: case 80: return "Rain";
			case 81: case 82: case 65: return "Showers";
			case 56: case 57: case 66: case 67: return "Icy rain";
			case 77: case 71: case 73: case 85: return "Snow";
			case 75: case 86: return "Heavy snow";
			case 95: case 96: case 99: return "Thunder storm";
			default: return "Unknown conditions";
		}
	}
	private static int[] getColorsFromWeatherCode(int code, boolean isDay) {
		switch (code) {
			case 0: case 1: return isDay ? CLEAR_SKY_DAY_COLORS : CLEAR_SKY_NIGHT_COLORS;
			case 2: return isDay ? PARTLY_CLOUDY_DAY_COLORS : PARTLY_CLOUDY_NIGHT_COLORS;
			case 3: return CLOUDY_COLORS;
			case 45: case 48: return FOGGY_COLORS;
			case 51: case 53: case 55: return isDay ? DRIZZLE_DAY_COLORS : CLEAR_SKY_NIGHT_COLORS;
			case 61: case 63: case 80: case 81: case 82:
				case 65:case 56: case 57: case 66: case 67: return RAIN_OR_SHOWER_COLORS;
			case 77: case 71: case 73: case 85: return isDay ? SNOW_DAY_COLORS : CLEAR_SKY_NIGHT_COLORS;
			case 75: case 86: return HEAVY_SNOW_COLORS;
			case 95: case 96: case 99: return THUNDER_COLORS;
			default: return WEATHER_UNAVAILABLE_COLORS;
		}
	}

	private static int getIconFromWeatherCode(int code, boolean isDay) {
		switch (code) {
			case 0: return isDay ? R.drawable.ic_weather_clear_sky : R.drawable.ic_weather_clear_night;
			case 1: return isDay ? R.drawable.ic_weather_mostly_clear : R.drawable.ic_weather_mostly_clear_night;
			case 2: return isDay ? R.drawable.ic_weather_partly_cloudy : R.drawable.ic_weather_partly_cloudy_night;
			case 3: return R.drawable.ic_weather_cloudy;
			case 45: case 48: return R.drawable.ic_weather_foggy;
			case 51: case 53: case 55: return R.drawable.ic_weather_drizzle;
			case 61: case 63: case 80: return R.drawable.ic_weather_rain;
			case 81: case 82: case 65: return R.drawable.ic_weather_showers;
			case 56: case 57: case 66: case 67: return R.drawable.ic_weather_icy_rain;
			case 77: case 71: case 73: case 85: return R.drawable.ic_weather_snow;
			case 75: case 86: return R.drawable.ic_weather_heavy_snow;
			case 95: case 96: case 99: return R.drawable.ic_weather_thunderstorm;
			default: return R.drawable.ic_weather_clear_sky;
		}
	}
}
