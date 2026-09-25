package com.vovan.phonedash.weatherProviderTests;

import org.junit.Test;
import static org.junit.Assert.*;

import com.vovan.phonedash.dataProviders.HttpCallback;
import com.vovan.phonedash.dataProviders.weatherProviders.CityCoordinate;
import com.vovan.phonedash.dataProviders.weatherProviders.OpenMeteo.OpenMeteoClient;
import com.vovan.phonedash.dataProviders.weatherProviders.OpenMeteo.OpenMeteoWeatherProvider;
import com.vovan.phonedash.dataProviders.weatherProviders.Weather;
import com.vovan.phonedash.dataProviders.weatherProviders.WeatherProvider;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.util.List;

public class OpenMeteoTests {
	CountDownLatch latch = new CountDownLatch(1);
}
