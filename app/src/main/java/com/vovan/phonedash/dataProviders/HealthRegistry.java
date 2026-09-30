package com.vovan.phonedash.dataProviders;

// The backend writes to the registry instead of some kind of file storage
// because you cannot make an iPhone Shortcut run every hour which would be super cool
// and that would in fact write to a file.
// But instead the shortcut sends info every time i open instagram (lol), so there's really
// no point of keeping it in a local file

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HealthRegistry {
	public interface HealthUpdateCallback {
		void onUpdate();
	}
	private static final List<HealthUpdateCallback> listeners = new ArrayList<>(); // realistically will only hold 1 or 2 callbacks, so no point of making it a linked list
	public static int stepCount;
	public static int distance;
	public static int activeCalories;
	public static int restingCalories;

	public static void addHealthUpdateCallback(HealthUpdateCallback callback) {
		listeners.add(callback);
	}
	public static void removeHealthUpdateCallback(HealthUpdateCallback callback) {
		listeners.remove(callback);
	}
	public static void fireHealthUpdateCallbacks() {
		for (HealthUpdateCallback i : listeners) {
			i.onUpdate();
		}
	}
}
