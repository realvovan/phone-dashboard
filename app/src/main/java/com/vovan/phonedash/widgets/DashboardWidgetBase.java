package com.vovan.phonedash.widgets;

import android.view.View;

/**
 * Generic base class for all dashboard widgets.
 * Stores dimensions and position within a dashboard grid structure.
 */
public abstract class DashboardWidgetBase<TWidgetInfo> {
	private int gridX;
	private int gridY;

	protected DashboardWidgetBase(int gridX, int gridY) {
		this.gridX = gridX;
		this.gridY = gridY;
	}

	public abstract int width();
	public abstract int height();
	public abstract void startWidget();
	public abstract void pauseWidget();
	public abstract void setWidgetInfo(TWidgetInfo info);
	public abstract View getView();
	public abstract TWidgetInfo getDefaultWidgetInfo();

	public final int getGridX() {
		return this.gridX;
	}
	public final int getGridY() {
		return this.gridY;
	}

	public final void setGridPosition(int x, int y) {
		this.gridX = x;
		this.gridY = y;
	}
	public void setDefaultWidgetInfo() {
		this.setWidgetInfo(this.getDefaultWidgetInfo());
	}
}
