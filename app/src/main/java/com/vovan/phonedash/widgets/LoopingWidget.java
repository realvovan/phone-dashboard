package com.vovan.phonedash.widgets;

public interface LoopingWidget {
	/**
	 * Override to set the method that runs every loop
	 * @return the time in milliseconds to wait before the next loop
	 */
	long onLoop();
}
