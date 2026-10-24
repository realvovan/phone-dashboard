package com.vovan.phonedash;

import android.os.Handler;

import com.vovan.phonedash.widgetHandlers.*;
import com.vovan.phonedash.widgets.DashboardWidgetBase;
import com.vovan.phonedash.widgets.LoopingWidget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data model for a container that holds a collection of DashboardWidgets
 * organized within a fixed X by Y (columns by rows) grid.
 * This class is independent of Android Activity lifecycles or UI specifics.
 */
public final class WidgetContainer {
	public final int columns;
	public final int rows;
	private final Set<DashboardWidgetBase<?>> widgets;
	private final LoopingWidgetsHandler loopingWidgetsHandler;

	public WidgetContainer(int columns, int rows, Handler handler) {
		this.columns = columns;
		this.rows = rows;
		this.widgets = new HashSet<>();
		this.loopingWidgetsHandler = new LoopingWidgetsHandler(handler);
	}

	/**
	 * Returns a copy of the widgets list currently inside this container.
	 */
	public List<DashboardWidgetBase<?>> getWidgets() {
		return new ArrayList<>(this.widgets);
	}

	/**
	 * Attempts to add a widget to the grid. Validates that the widget
	 * fits within the grid dimensions and does not overlap with existing widgets.
	 *
	 * @param widget The widget to add.
	 * @return true if the widget was successfully added, false otherwise.
	 */
	public boolean addWidget(DashboardWidgetBase<?> widget) {
		if (widget == null) return false;

		// Check if widget fits within the grid boundaries
		if (widget.getGridX() < 0 || widget.getGridY() < 0 ||
				widget.getGridX() + widget.width() > this.columns ||
				widget.getGridY() + widget.height() > this.rows) {
			return false;
		}

		// Check for overlap with existing widgets
		if (this.hasOverlap(widget)) return false;

		return this.widgets.add(widget);
	}

	/**
	 * Removes a widget from the container.
	 *
	 * @param widget The widget to remove.
	 * @return true if the widget was found and removed, false otherwise.
	 */
	public boolean removeWidget(DashboardWidgetBase<?> widget) {
		boolean isRemoved = this.widgets.remove(widget);
		if (!isRemoved) return false;
		widget.onWidgetRemoved();
		if (widget instanceof LoopingWidget) this.loopingWidgetsHandler.stopLoopingWidget((LoopingWidget) widget);
		return true;
	}

	public void notifyWidgetStateChange(DashboardWidgetBase<?> widget, WidgetStateChange newState) {
		if (widget instanceof LoopingWidget) {
			if (newState == WidgetStateChange.STARTED) {
				this.loopingWidgetsHandler.startLoopingWidget((LoopingWidget) widget);
			} else {
				this.loopingWidgetsHandler.stopLoopingWidget((LoopingWidget) widget);
			}
		}
	}

	public boolean isCellEmpty(int x, int y) {
		for (DashboardWidgetBase<?> w : this.widgets) {
			int wLeft = w.getGridX();
			int wTop = w.getGridY();
			int wRight = wLeft + w.width();
			int wBottom = wTop + w.height();
			if (wLeft <= x && wRight >= x && wTop <= y && wBottom >= y) {
				return false;
			}
		}
		return true;
	}

	private boolean hasOverlap(DashboardWidgetBase<?> newWidget) {
		int newLeft = newWidget.getGridX();
		int newTop = newWidget.getGridY();
		int newRight = newLeft + newWidget.width();
		int newBottom = newTop + newWidget.height();

		for (DashboardWidgetBase<?> w : this.widgets) {
			int wLeft = w.getGridX();
			int wTop = w.getGridY();
			int wRight = wLeft + w.width();
			int wBottom = wTop + w.height();

			// Standard AABB overlap check
			if (newLeft < wRight && newRight > wLeft && newTop < wBottom && newBottom > wTop) {
				return true;
			}
		}
		return false;
	}
}
