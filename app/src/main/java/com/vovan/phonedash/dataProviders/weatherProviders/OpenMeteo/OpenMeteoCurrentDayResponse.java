package com.vovan.phonedash.dataProviders.weatherProviders.OpenMeteo;

import java.util.List;

public class OpenMeteoCurrentDayResponse {
	public static class Current {
		public double temperature_2m;
		public int weather_code;
		public int is_day;
	}
	public static class Daily {
		public List<Double> temperature_2m_max;
		public List<Double> temperature_2m_min;
	}
	public static class Hourly {
		public List<String> time;
		public List<Integer> precipitation_probability;
	}

	public Current current;
	public Daily daily;
	public Hourly hourly;
}
