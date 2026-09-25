package com.vovan.phonedash.dataProviders.weatherProviders.OpenMeteo;

import com.vovan.phonedash.dataProviders.HttpCallback;
import com.vovan.phonedash.dataProviders.HttpUtils;
import com.vovan.phonedash.dataProviders.weatherProviders.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class OpenMeteoWeatherProvider implements WeatherProvider {
	private final OpenMeteoClient client = new OpenMeteoClient();

	@Override
	public void getCurrentWeather(
			double latitude,
			double longitude,
			HttpCallback<Weather> callback
	) {
		this.client.weatherApi.getCurrentWeather(
				latitude,
				longitude,
				"temperature_2m_max,temperature_2m_min",
				"temperature_2m,is_day,weather_code",
				"precipitation_probability",
				1)
				.enqueue(new Callback<OpenMeteoCurrentDayResponse>() {
					@Override
					@EverythingIsNonNull
					public void onResponse(Call<OpenMeteoCurrentDayResponse> call, Response<OpenMeteoCurrentDayResponse> response) {
						OpenMeteoCurrentDayResponse body = HttpUtils.requireBody(response,callback::onFailure);
						if (body == null) { return; }

						Weather weather = new Weather();
						weather.currentTemperature = body.current.temperature_2m;
						weather.weatherCode = body.current.weather_code;
						weather.isDay = body.current.is_day == 1;
						weather.maxTemperature = body.daily.temperature_2m_max.get(0);
						weather.minTemperature = body.daily.temperature_2m_min.get(0);

						if (weather.weatherCode < 51) {
							Instant now = Instant.now();
							for (int i = 0; i < body.hourly.time.size(); i++) {
								Instant timeStamp = LocalDateTime.parse(body.hourly.time.get(i)).toInstant(ZoneOffset.UTC);
								if (timeStamp.isBefore(now)) { continue; }
								long secondsBetween = Duration.between(timeStamp,now).getSeconds();
								if (secondsBetween <= 7200) {
									int pptProb = body.hourly.precipitation_probability.get(i);
									if (pptProb < 35) { continue; }
									weather.possibleFuturePptProbability = pptProb;
									weather.possibleFuturePptInHours = secondsBetween / 3600f;
									break;
								}
							}
						}

						callback.onSuccess(weather);
					}

					@Override
					@EverythingIsNonNull
					public void onFailure(Call<OpenMeteoCurrentDayResponse> call, Throwable t) {
						callback.onFailure(new Exception(t));
					}
				});
	}

	@Override
	public void getCoordsForCity(String city, HttpCallback<List<CityCoordinate>> callback) {
		this.client.geocodingApi.getCityCoords(
				city,
				10,
				"en",
				"json"
		).enqueue(new Callback<OpenMeteoCityCoordsResponse>() {
			@Override
			@EverythingIsNonNull
			public void onResponse(Call<OpenMeteoCityCoordsResponse> call, Response<OpenMeteoCityCoordsResponse> response) {
				OpenMeteoCityCoordsResponse body = HttpUtils.requireBody(response,callback::onFailure);
				if (body == null) { return; }

				List<CityCoordinate> coords = new ArrayList<>(body.results.size());
				for (int i = 0; i < body.results.size(); i++) {
					var responseV = body.results.get(i);
					CityCoordinate v = new CityCoordinate();
					v.name = responseV.name;
					v.country = responseV.country;
					v.latitude = responseV.latitude;
					v.longitude = responseV.longitude;
					v.province = responseV.admin1;
					coords.add(v);
				}
				callback.onSuccess(coords);
			}

			@Override
			@EverythingIsNonNull
			public void onFailure(Call<OpenMeteoCityCoordsResponse> call, Throwable t) {
				callback.onFailure(new Exception(t));
			}
		});
	}
}
