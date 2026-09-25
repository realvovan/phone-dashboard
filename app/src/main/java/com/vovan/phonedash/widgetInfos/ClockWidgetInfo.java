package com.vovan.phonedash.widgetInfos;

import androidx.annotation.NonNull;

import java.util.Locale;

public class ClockWidgetInfo {
	public final int hours;
	public final int minutes;
	public final int seconds;

	public ClockWidgetInfo(int h, int m, int s) {
		this.hours = h;
		this.minutes = m;
		this.seconds = s;
	}

	@NonNull
	@Override
	public String toString() {
		return String.format(Locale.getDefault(),"%d hours : %d minutes : %d seconds", this.hours, this.minutes, this.seconds);
	}
}
