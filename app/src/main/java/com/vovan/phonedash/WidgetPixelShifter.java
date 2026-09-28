package com.vovan.phonedash;

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

	public void shift(WidgetContainerLayout containerLayout) {
		int[] dir = SHIFT_PATTERNS[currentShiftPatternIndex].shifts[currentShiftDirectionIndex];
		containerLayout.setTranslationX(dir[0] * STEP_SIZE + containerLayout.getTranslationX());
		containerLayout.setTranslationY(dir[1] * STEP_SIZE + containerLayout.getTranslationY());

		currentShiftStep++;
		if (currentShiftStep != SHIFT_LENGTH) return;
		currentShiftStep = 0;
		currentShiftDirectionIndex++;
		if (currentShiftDirectionIndex != SHIFT_PATTERNS[currentShiftPatternIndex].shifts.length) return;
		currentShiftDirectionIndex = 0;
		currentShiftPatternIndex = getRandomShiftPatternIndex();

		containerLayout.setTranslationX(0);
		containerLayout.setTranslationY(0);
	}

	private static int[] createShift(int x, int y) { return new int[] {x,y}; }
	private static int getRandomShiftPatternIndex() { return (int)(Math.random() * SHIFT_PATTERNS.length); }
}
