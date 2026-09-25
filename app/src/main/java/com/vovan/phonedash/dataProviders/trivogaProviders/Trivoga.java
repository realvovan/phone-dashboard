package com.vovan.phonedash.dataProviders.trivogaProviders;

import androidx.annotation.Nullable;

import java.time.Instant;

public class Trivoga {
	public enum AlertLevel {
		RED, // missile/massive drone/mixed trivoga in region
		YELLOW, // drone trivoga in region
		GREEN, // no trivoga in region
		UNKNOWN
	}
	public String locationTitle;
	@Nullable
	public Instant startedAt; // null if alertLevel == GREEN
	public AlertLevel alertLevel;

	public static Trivoga createInactiveTrivoga(String region) {
		Trivoga t = new Trivoga();
		t.locationTitle = region;
		t.startedAt = null;
		t.alertLevel = AlertLevel.GREEN;
		return t;
	}
}
