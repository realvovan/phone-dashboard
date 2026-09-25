package com.vovan.phonedash.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import com.vovan.phonedash.WidgetContainer;

/**
 * Generic base class for all dashboard widgets.
 * Stores dimensions and position within a dashboard grid structure.
 */
public abstract class DashboardWidgetBase<TWidgetInfo> {
	private int gridX;
	private int gridY;
	protected final WidgetContainer container;

	protected DashboardWidgetBase(WidgetContainer container, int gridX, int gridY) {
		this.gridX = gridX;
		this.gridY = gridY;
		this.container = container;
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
	protected final void addStateChangeListeners() {
		this.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
			@Override
			public void onViewAttachedToWindow(@NonNull View v) {
				startWidget();
			}

			@Override
			public void onViewDetachedFromWindow(@NonNull View v) {
				pauseWidget();
			}
		});
	}
	public void setDefaultWidgetInfo() {
		this.setWidgetInfo(this.getDefaultWidgetInfo());
	}
}
