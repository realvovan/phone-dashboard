package com.vovan.phonedash.dataProviders.trivogaProviders.alertsInUaProvider;

import android.util.Log;

import androidx.annotation.Nullable;

import com.vovan.phonedash.BuildConfig;
import com.vovan.phonedash.dataProviders.HttpCallback;
import com.vovan.phonedash.dataProviders.HttpUtils;
import com.vovan.phonedash.dataProviders.trivogaProviders.Trivoga;
import com.vovan.phonedash.dataProviders.trivogaProviders.TrivogaProvider;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class AlertsProvider implements TrivogaProvider {
	private final AlertsClient.AlertsApi alertsApi = new AlertsClient().alertsApi;
	private static final long RESPONSE_CACHING_TIME = 20_000_000_000L; // 20 seconds
	private AlertsResponse cachedResponse;
	private long lastCachedResponse = -1L;
	@Nullable
	private CompletableFuture<Void> alertRequestLock;
	public synchronized void getAlertInRegion(String region, HttpCallback<Trivoga> callback) {
		long currentTime = System.nanoTime();
		if (currentTime - this.lastCachedResponse < RESPONSE_CACHING_TIME && this.cachedResponse != null) {
			Log.i("AlertsClient","Getting alert in region from cached response");
			callback.onSuccess(getTrivogaFromResponse(this.cachedResponse,region));
			return;
		}

		// if another request already in processing, wait for it to complete and use the cached value
		if (this.alertRequestLock != null) {
			this.alertRequestLock.thenRun(() -> {
				Log.i("AlertsClient","Getting alert in region from CompletableFuture");
				if (cachedResponse == null) {
					callback.onFailure(new Exception(
							"No cached response found after the request lock was released. This means a previous request has failed, please try again later"
					));
				} else {
					callback.onSuccess(getTrivogaFromResponse(cachedResponse,region));
				}
			});
			return;
		}
		this.alertRequestLock = new CompletableFuture<>();

		Log.i("AlertsClient","Getting alert in region from Alerts.in.ua server");
		this.alertsApi.getActiveAlerts(BuildConfig.ALERTS_IN_UA_KEY).enqueue(new Callback<AlertsResponse>() {
			@Override
			@EverythingIsNonNull
			public void onResponse(Call<AlertsResponse> call, Response<AlertsResponse> response) {
				AlertsResponse body = HttpUtils.requireBody(response,callback::onFailure);
				if (body == null) { return; }
				cachedResponse = body;
				lastCachedResponse = currentTime;
				callback.onSuccess(getTrivogaFromResponse(body,region));
				// release the lock
				alertRequestLock.complete(null);
				alertRequestLock = null;
			}

			@Override
			@EverythingIsNonNull
			public void onFailure(Call<AlertsResponse> call, Throwable t) {
				cachedResponse = null;
				lastCachedResponse = currentTime;
				callback.onFailure(new Exception(t));
				//release the lock
				alertRequestLock.complete(null);
				alertRequestLock = null;
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
