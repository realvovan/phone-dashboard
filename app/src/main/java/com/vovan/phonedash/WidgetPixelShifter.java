package com.vovan.phonedash;

import android.os.Handler;

final class ShiftPattern {
	public final int[][] shifts;
	public ShiftPattern(int[]... shifts) {
		this.shifts = shifts;
	}
}
final class WidgetPixelShifter {
	private static final ShiftPattern[] SHIFT_PATTERNS = new ShiftPattern[] {
			// +----+
			// |    |
			// +----+
			new ShiftPattern(createShift(1,0), createShift(0,1), createShift(-1,0), createShift(0,-1)),
			// +---+
			//  \ /
			//   /
			//  / \
			// +---+
			new ShiftPattern(createShift(1,0), createShift(-1,1), createShift(1,0), createShift(-1,-1)),
			// +      +
			// |\    /|
			// | \  / |
			// |  \/  |
			// |  /\  |
			// | /  \ |
			// |/    \|
			// +      +
			new ShiftPattern(createShift(0,1), createShift(1,-1), createShift(0,1), createShift(-1,-1))
	};
	public static final int SHIFT_LENGTH = 10;
	public static final int STEP_SIZE = 3;

	// keeps track of the randomized pattern, which automatically picks the next pattern once the current one ends
	private int currentShiftPatternIndex = getRandomShiftPatternIndex();
	// keeps track of the current shift direction within the pattern
	private int currentShiftDirectionIndex = 0;
	// keeps track of the current step within the shift direction
	private int currentShiftStep = 0;
	private long shiftCooldown = 0L;
	private WidgetContainerLayout container;
	private Handler handler;

	private Runnable shifterRunnable = new Runnable() {
		@Override
		public void run() {
			shift();
			handler.postDelayed(this, shiftCooldown);
		}
	};

	public WidgetPixelShifter(WidgetContainerLayout container, Handler handler) {
		this.container = container;
		this.handler = handler;
	}

	public void shift() {
		int[] dir = SHIFT_PATTERNS[currentShiftPatternIndex].shifts[currentShiftDirectionIndex];
		this.container.setTranslationX(dir[0] * STEP_SIZE + this.container.getTranslationX());
		this.container.setTranslationY(dir[1] * STEP_SIZE + this.container.getTranslationY());

		currentShiftStep++;
		if (currentShiftStep != SHIFT_LENGTH) return;
		currentShiftStep = 0;
		currentShiftDirectionIndex++;
		if (currentShiftDirectionIndex != SHIFT_PATTERNS[currentShiftPatternIndex].shifts.length) return;
		currentShiftDirectionIndex = 0;
		currentShiftPatternIndex = getRandomShiftPatternIndex();

		this.container.setTranslationX(0);
		this.container.setTranslationY(0);
	}

	/**
	 * @param cooldown Cooldown in milliseconds between the shifts
	 * @param startImmediately Whether to shift once after calling this method without the cooldown
	 */
	public void startShifting(long cooldown, boolean startImmediately) {
		this.shiftCooldown = cooldown;
		if (startImmediately) {
			this.handler.post(this.shifterRunnable);
		} else {
			this.handler.postDelayed(this.shifterRunnable, cooldown);
		}
	}

	/**
	 * @param resetShiftPatter The shift pattern and its current step will persist
	 *                         when you call startShifting() if this parameter is true,
	 *                         otherwise a new shift pattern will be selected
	 */
	public void stopShifting(boolean resetShiftPatter) {
		this.handler.removeCallbacks(this.shifterRunnable);
		if (resetShiftPatter) {
			this.resetShifting(true);
		}
	}

	/**
	 * Sets the translation of the container to (0, 0)
	 * @param changePatter Whether to change the pattern when you call startShifting()
	 */
	public void resetShifting(boolean changePatter) {
		if (changePatter) this.currentShiftPatternIndex = getRandomShiftPatternIndex();
		this.currentShiftDirectionIndex = 0;
		this.currentShiftStep = 0;
		this.container.setTranslationX(0);
		this.container.setTranslationY(0);
	}

	public void close() {
		this.stopShifting(true);
		this.container = null;
		this.handler = null;
		this.shifterRunnable = null;
	}

	private static int[] createShift(int x, int y) { return new int[] {x,y}; }
	private static int getRandomShiftPatternIndex() { return (int)(Math.random() * SHIFT_PATTERNS.length); }
}
