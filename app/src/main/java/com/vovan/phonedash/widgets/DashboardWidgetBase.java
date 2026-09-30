package com.vovan.phonedash.widgets;

import android.content.Context;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.vovan.phonedash.WidgetContainer;

/**
 * Generic base class for all dashboard widgets.
 * Stores dimensions and position within a dashboard grid structure.
 */
public abstract class DashboardWidgetBase<TWidgetInfo> {
	private int gridX;
	private int gridY;
	protected WidgetContainer container;

	// Context parameter is required to inflate all widgets, even though it isn't stored in the widget
	protected DashboardWidgetBase(Context context, WidgetContainer container, int gridX, int gridY) {
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

	/**
	 * This method is called when the widget is removed from its container
	 * Override to clean up any resources used by the widget
	 */
	@CallSuper
	public void onWidgetRemoved() {
		this.container = null;
	}

	public void setDefaultWidgetInfo() {
		this.setWidgetInfo(this.getDefaultWidgetInfo());
	}
}
