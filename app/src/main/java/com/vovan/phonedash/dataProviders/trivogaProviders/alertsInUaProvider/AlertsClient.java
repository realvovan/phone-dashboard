package com.vovan.phonedash.dataProviders.trivogaProviders.alertsInUaProvider;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class AlertsClient {
	public static interface AlertsApi {
		@GET("v1/alerts/active.json")
		Call<AlertsResponse> getActiveAlerts(@Query("token") String apiToken);
	}

	public final AlertsApi alertsApi;

	public AlertsClient() {
		this.alertsApi = new Retrofit.Builder()
				.baseUrl("https://api.alerts.in.ua")
				.addConverterFactory(GsonConverterFactory.create())
				.build()
				.create(AlertsApi.class);
	}
}
