package com.vovan.phonedash.dataProviders.calendarEventsProviders.formJsonHttpProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

class CalendarEventsFromHttpClient {
	interface CalendarEventsFromJsonApi {
		@GET(".")
		Call<CalendarEventsFromHttpResponse> getCalendarEvents();
	}

	public final CalendarEventsFromJsonApi calendarEventsFromJsonApi;

	public CalendarEventsFromHttpClient(String url) {
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(int.class, new ColorDeserializer())
				.registerTypeAdapter(Integer.class, new ColorDeserializer())
				.create();

		this.calendarEventsFromJsonApi = new Retrofit.Builder()
				.baseUrl(url)
				.addConverterFactory(GsonConverterFactory.create(gson))
				.build()
				.create(CalendarEventsFromJsonApi.class);
	}
}
