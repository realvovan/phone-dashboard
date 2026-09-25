package com.vovan.phonedash.dataProviders.trivogaProviders.alertsInUaProvider;

import android.util.Log;

import com.vovan.phonedash.BuildConfig;
import com.vovan.phonedash.dataProviders.HttpCallback;
import com.vovan.phonedash.dataProviders.HttpUtils;
import com.vovan.phonedash.dataProviders.trivogaProviders.Trivoga;
import com.vovan.phonedash.dataProviders.trivogaProviders.TrivogaProvider;

import java.time.Instant;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class AlertsProvider implements TrivogaProvider {
	private final AlertsClient.AlertsApi alertsApi = new AlertsClient().alertsApi;
	private final long RESPONSE_CACHING_TIME = 15_000_000_000L; // 15 seconds
	private AlertsResponse cachedResponse;
	private long lastCachedResponse = -1L;
	public void getAlertInRegion(String region, HttpCallback<Trivoga> callback) {
		long currentTime = System.nanoTime();
		if (currentTime - this.lastCachedResponse < RESPONSE_CACHING_TIME && this.cachedResponse != null) {
			Log.i("AlertsClient","Getting alert in region from cached response");
			callback.onSuccess(getTrivogaFromResponse(this.cachedResponse,region));
			return;
		}
		this.alertsApi.getActiveAlerts(BuildConfig.ALERTS_IN_UA_KEY).enqueue(new Callback<AlertsResponse>() {
			@Override
			@EverythingIsNonNull
			public void onResponse(Call<AlertsResponse> call, Response<AlertsResponse> response) {
				AlertsResponse body = HttpUtils.requireBody(response,callback::onFailure);
				if (body == null) { return; }
				cachedResponse = body;
				lastCachedResponse = currentTime;
				callback.onSuccess(getTrivogaFromResponse(body,region));
			}

			@Override
			@EverythingIsNonNull
			public void onFailure(Call<AlertsResponse> call, Throwable t) {
				callback.onFailure(new Exception(t));
			}
		});
	}

	private static Trivoga getTrivogaFromResponse(AlertsResponse response, String region) {
		for (AlertsResponse.Alert alert : response.alerts) {
			if (alert.location_title.equals(region)) {
				Trivoga t = new Trivoga();
				t.locationTitle = alert.location_title;
				t.startedAt = Instant.parse(alert.started_at);
				if (alert.alert_level.equals("yellow")) {
					t.alertLevel = Trivoga.AlertLevel.YELLOW;
				} else if (alert.alert_level.equals("red")) {
					t.alertLevel = Trivoga.AlertLevel.RED;
				} else {
					t.alertLevel = Trivoga.AlertLevel.UNKNOWN;
				}
				return t;
			}
		}
		return Trivoga.createInactiveTrivoga(region);
	}
}
