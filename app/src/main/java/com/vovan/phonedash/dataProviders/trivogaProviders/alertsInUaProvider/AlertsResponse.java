package com.vovan.phonedash.dataProviders.trivogaProviders.alertsInUaProvider;

import java.util.List;

public class AlertsResponse {
	public static class Alert {
		public String location_title;
		public String location_title_en;
		public String started_at;
		public String alert_level;
	}

	public List<Alert> alerts;
}
