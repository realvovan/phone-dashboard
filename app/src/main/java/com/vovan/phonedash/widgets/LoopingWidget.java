package com.vovan.phonedash.widgets;

public interface LoopingWidget {
	/**
	 * Override to set the method that runs every loop
	 * @return the time in milliseconds to wait before the next loop
	 */
	long onLoop();

	static <T extends DashboardWidgetBase<?> & LoopingWidget> void startLoopingWidget(T widget) {
		if (widget.container == null) return;
		widget.container.startRunnableForWidget(widget);
	}
	static <T extends DashboardWidgetBase<?> & LoopingWidget> void stopLoopingWidget(T widget) {
		if (widget.container == null) return;
		widget.container.removeRunnableForWidget(widget);
	}
}
