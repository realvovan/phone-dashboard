package com.vovan.phonedash;

import android.os.Handler;

final class WidgetContainerDimmer {
	private final static long DIMMING_ANIMATION_LEN = 10000L;
	private final static long WAKING_ANIMATION_LEN = 400L;

	private Handler handler;
	private WidgetContainerLayout container;
	private long dimmingCooldown;
	// how long the screen stay dark for
	private long dimmingDuration;
	private boolean isDimming = false;

	private Runnable dimmingRunnable = new Runnable() {
		@Override
		public void run() {
			dimContainer();
			handler.postDelayed(wakingRunnable, DIMMING_ANIMATION_LEN + dimmingDuration);
		}
	};
	private Runnable wakingRunnable = new Runnable() {
		@Override
		public void run() {
			wakeContainer();
			handler.postDelayed(dimmingRunnable, WAKING_ANIMATION_LEN + dimmingCooldown);
		}
	};

	public WidgetContainerDimmer(WidgetContainerLayout container, Handler handler) {
		this.container = container;
		this.handler = handler;
	}

	public void dimContainer() {
		this.isDimming = true;
		this.container.animate()
				.alpha(0f)
				.setDuration(DIMMING_ANIMATION_LEN)
				.start();
	}

	public void wakeContainer() {
		this.isDimming = false;
		this.container.animate()
				.alpha(1f)
				.setDuration(WAKING_ANIMATION_LEN)
				.start();
	}

	/**
	 * Begins periodically dimming the screen to help prevent pixel burn-in
	 * @param cooldown Time in milliseconds between each subsequent screen dimming
	 * @param dimDuration Time in milliseconds for how long the screen stays dimmed
	 * @param startImmediately Whether to dim the screen immediately upon calling this method
	 */
	public void startDimming(long cooldown, long dimDuration, boolean startImmediately) {
		this.dimmingCooldown = cooldown;
		this.dimmingDuration = dimDuration;
		if (startImmediately) {
			this.handler.post(this.dimmingRunnable);
		} else {
			this.handler.postDelayed(this.dimmingRunnable, cooldown);
		}
	}

	/**
	 * Stops the periodic screen dimming and wakes it up if it was dimming
	 */
	public void stopDimming() {
		this.handler.removeCallbacks(this.dimmingRunnable);
		this.handler.removeCallbacks(this.wakingRunnable);
		this.wakeContainer();
	}

	/**
	 * Resets the dimming progress and cooldown. Useful for when you tap the screen and want to wake the screen up and reset all the timers
	 */
	public void resetDimming() {
		this.stopDimming();
		this.startDimming(this.dimmingCooldown, this.dimmingDuration, false);
	}

	/**
	 * Returns whether the container is being currently dimmed or is fully dimmed
	 */
	public boolean getIsDimming() {
		return this.isDimming;
	}

	public void close() {
		this.stopDimming();
		this.container = null;
		this.handler = null;
		this.dimmingRunnable = null;
		this.wakingRunnable = null;
	}
}
