package com.vovan.phonedash.dataProviders.weatherProviders.OpenMeteo;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;

public class OpenMeteoClient {
	public static interface WeatherApi {
		@GET("v1/forecast")
		Call<OpenMeteoCurrentDayResponse> getCurrentWeather(
				@Query("latitude") double latitude,
				@Query("longitude") double longitude,
				@Query("daily") String dailyParams,
				@Query("current") String currentParams,
				@Query("hourly") String hourlyParams,
				@Query("forecast_days") int days
		);
	}

	public static interface GeocodingApi {
		@GET("v1/search")
		Call<OpenMeteoCityCoordsResponse> getCityCoords(
				@Query("name") String name,
				@Query("count") int count,
				@Query("language") String language,
				@Query("format") String format
		);
	}

	public final WeatherApi weatherApi;
	public final GeocodingApi geocodingApi;

	public OpenMeteoClient() {
		this.weatherApi = new Retrofit.Builder()
				.baseUrl("https://api.open-meteo.com/")
				.addConverterFactory(GsonConverterFactory.create())
				.build()
				.create(WeatherApi.class);
		this.geocodingApi = new Retrofit.Builder()
				.baseUrl("https://geocoding-api.open-meteo.com")
				.addConverterFactory(GsonConverterFactory.create())
				.build()
				.create(GeocodingApi.class);
	}
}
