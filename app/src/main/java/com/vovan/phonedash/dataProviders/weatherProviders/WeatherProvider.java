package com.vovan.phonedash.dataProviders.weatherProviders;

import com.vovan.phonedash.dataProviders.HttpCallback;

import java.util.List;

public interface WeatherProvider {
	void getCurrentWeather(double latitude, double longitude, HttpCallback<Weather> callback);
	void getCoordsForCity(String city,HttpCallback<List<CityCoordinate>> callback);
}
