package com.vovan.phonedash.widgetInfos;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class BatteryWidgetInfo {
	public final int percentage;
	public final boolean isCharging;

	public BatteryWidgetInfo(int percentage, boolean isCharging) {
		this.isCharging = isCharging;
		this.percentage = percentage;
	}

	@NotNull
	@Override
	public String toString() {
		return String.format(Locale.getDefault(),"%d%% | IsCharging: %b", this.percentage, this.isCharging);
	}
}
