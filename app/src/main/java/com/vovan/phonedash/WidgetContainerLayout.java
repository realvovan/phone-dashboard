package com.vovan.phonedash;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.vovan.phonedash.widgets.DashboardWidgetBase;

/**
 * A custom ViewGroup that handles putting widget UI elements on the screen.
 * It reads the layout configuration from a WidgetContainer data model and positions
 * each widget's view inside an X by Y grid.
 * This design allows multiple instances of WidgetContainerLayout to be held by an Activity
 * (e.g., inside a ViewPager) to support swiping between multiple widget containers in the future.
 */
public class WidgetContainerLayout extends ViewGroup {
	private final WidgetContainer widgetContainer;

	// Cached grid geometry to share between onMeasure and onLayout
	private int mCellSize = 0;
	private int mOffsetX = 0;
	private int mOffsetY = 0;

	public WidgetContainerLayout(Context context, WidgetContainer widgetContainer) {
		super(context);
		this.widgetContainer = widgetContainer;

		// Inflate and add views for all widgets currently in the container
		for (DashboardWidgetBase<?> widget : widgetContainer.getWidgets()) {
			View widgetView = widget.getView();
			if (widgetView != null) {
				addView(widgetView);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Total available usable screen size (respects status/navigation/cutout insets)
		int availableWidth = MeasureSpec.getSize(widthMeasureSpec);
		int availableHeight = MeasureSpec.getSize(heightMeasureSpec);

		int columns = widgetContainer.columns;
		int rows = widgetContainer.rows;

		if (columns <= 0 || rows <= 0) {
			setMeasuredDimension(availableWidth, availableHeight);
			return;
		}

		// Determine the maximum possible size for a single cell so the entire grid fits inside bounds
		int maxCellWidth = availableWidth / columns;
		int maxCellHeight = availableHeight / rows;

		// Pick the smaller one to guarantee squares remain perfectly uniform and aspect-ratio locked (1:1)
		mCellSize = Math.min(maxCellWidth, maxCellHeight);

		// Compute total grid dimensions
		int gridTotalWidth = mCellSize * columns;
		int gridTotalHeight = mCellSize * rows;

		// Center the grid inside the available screen area
		mOffsetX = (availableWidth - gridTotalWidth) / 2;
		mOffsetY = (availableHeight - gridTotalHeight) / 2;

		// Measure each child view to perfectly fill its allocated grid coordinate span
		for (DashboardWidgetBase widget : widgetContainer.getWidgets()) {
			View child = widget.getView();
			if (child != null) {
				int childWidth = mCellSize * widget.width();
				int childHeight = mCellSize * widget.height();

				int childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
				int childHeightSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
				child.measure(childWidthSpec, childHeightSpec);
			}
		}

		setMeasuredDimension(availableWidth, availableHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// Position each child view using the calculated cell size and centering offsets
		for (DashboardWidgetBase widget : widgetContainer.getWidgets()) {
			View child = widget.getView();
			if (child != null) {
				int left = mOffsetX + (mCellSize * widget.getGridX());
				int top = mOffsetY + (mCellSize * widget.getGridY());
				int right = left + (mCellSize * widget.width());
				int bottom = top + (mCellSize * widget.height());

				child.layout(left, top, right, bottom);
			}
		}
	}
}
