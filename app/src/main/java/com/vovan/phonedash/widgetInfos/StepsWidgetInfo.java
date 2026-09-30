package com.vovan.phonedash.widgetInfos;

import androidx.annotation.NonNull;

import java.util.Locale;

public class StepsWidgetInfo {
	public final int steps;
	public final int distance;

	public StepsWidgetInfo(int steps, int distance) {
		this.steps = steps;
		this.distance = distance;
	}

	@NonNull
	@Override
	public String toString() {
		return String.format(Locale.getDefault(), "StepsWidgetInfo{steps=%d, distance=%d}", this.steps, this.distance);
	}
}
