package com.vovan.phonedash.backend;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.vovan.phonedash.MainActivity;
import com.vovan.phonedash.R;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class DashboardServerService extends Service {
	private WebServer server;

	@Override
	public void onCreate() {
		super.onCreate();
		this.startForeground(1, buildNotification());
		this.server = new WebServer(8080);
		try {
			this.server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		} catch (IOException e) {
			Log.e("DashboardServer", "Failed to start", e);
		}
	}

	@Override
	public void onDestroy() {
		if (this.server != null) this.server.stop();
		super.onDestroy();
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		this.stopSelf();
		super.onTaskRemoved(rootIntent);
	}

	@Override
	public IBinder onBind(Intent intent) { return null; }

	private Notification buildNotification() {
		final String CHANNEL_ID	= "dashboard_server";
		// only do that for android 8+ (API 26)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					CHANNEL_ID,
					"Dashboard server",
					NotificationManager.IMPORTANCE_LOW
			);
			NotificationManager nm = this.getSystemService(NotificationManager.class);
			nm.createNotificationChannel(channel);
		}

		Intent openApp = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				this, 0, openApp, PendingIntent.FLAG_IMMUTABLE
		);
		return new NotificationCompat.Builder(this, CHANNEL_ID)
				.setContentTitle("Dashboard running")
				.setContentText("Listening for data on port 8080")
				.setContentIntent(pendingIntent)
				.setSmallIcon(R.drawable.ic_weather_clear_sky)
				.setOngoing(true)
				.build();
	}
}
