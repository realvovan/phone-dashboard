package com.vovan.phonedash;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import com.vovan.phonedash.widgets.DashboardWidgetBase;

import java.lang.reflect.Constructor;

/**
 * A custom ViewGroup that handles putting widget UI elements on the screen.
 * It reads the layout configuration from a WidgetContainer data model and positions
 * each widget's view inside an X by Y grid.
 * The layout also exposes the WidgetContainer, however it is preferable to use WidgetContainerLayout
 * to add and remove widgets
 */
public final class WidgetContainerLayout extends ViewGroup {
	public final WidgetContainer widgetContainer;
	private final Context context;

	// Cached grid geometry to share between onMeasure and onLayout
	private int mCellSize = 0;
	private int mOffsetX = 0;
	private int mOffsetY = 0;

	public WidgetContainerLayout(Context context, WidgetContainer widgetContainer) {
		super(context);
		this.widgetContainer = widgetContainer;
		this.context = context;

		// Inflate and add views for all widgets currently in the container
		for (DashboardWidgetBase<?> widget : widgetContainer.getWidgets()) {
			View widgetView = widget.getView();
			if (widgetView != null) {
				addView(widgetView);
			}
		}
	}
	public WidgetContainerLayout(Context context, Handler handler, int columns, int rows) {
		super(context);
		this.widgetContainer = new WidgetContainer(columns,rows,handler);
		this.context = context;
	}

	public boolean addWidget(DashboardWidgetBase<?> widget) {
		boolean isAdded = this.widgetContainer.addWidget(widget);
		if (!isAdded) return false;
		View widgetView = widget.getView();
		if (widgetView != null) {
			this.addView(widgetView);
			return true;
		}
		throw new IllegalArgumentException(String.format(
				"Widget %s contains no View to be added to Layout",
				widget.getClass().getName()
		));
	}

	/**
	 * Creates and adds the widget to the WidgetContainer and the Layout
	 * @param widgetClass Type of the widget to be created and added
	 * @param gridX Horizontal position of the widget on the grid
	 * @param gridY Vertical position of the widget on the grid
	 * @param params Other parameters the widget accepts. If provided parameters are malformed, the method throws a RuntimeException
	 * @return Returns the created widget
	 * @param <T> The type of the widget to be created
	 */
	public <T extends DashboardWidgetBase<?>> T addWidget(Class<T> widgetClass, int gridX, int gridY, Object... params) {
		Constructor<?>[] constructors = widgetClass.getConstructors();
		if (constructors.length != 1) throw new IllegalArgumentException(String.format(
				"Widget's constructor must have exactly 1 constructor. %s has %d",
				widgetClass.getName(), constructors.length
		));
		final int BASE_ARG_COUNT = 4;
		Constructor<?> constructor = constructors[0];
		Object[] args = new Object[params.length + BASE_ARG_COUNT];
		args[0] = this.context;
		args[1] = this.widgetContainer;
		args[2] = gridX;
		args[3] = gridY;
		System.arraycopy(params,0,args,BASE_ARG_COUNT,params.length);

		try {
			T widget = widgetClass.cast(constructor.newInstance(args));
			boolean isAdded = this.addWidget(widget);
			if (isAdded) return widget;
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			throw new RuntimeException("Failed to create widget " + widgetClass.getName(), e);
		}
		throw new RuntimeException("Unexpected error occurred trying to create widget " + widgetClass.getName());
	}

	public boolean removeWidget(DashboardWidgetBase<?> widget) {
		boolean isRemoved = this.widgetContainer.removeWidget(widget);
		if (!isRemoved) return false;
		View widgetView = widget.getView();
		if (widgetView != null) {
			this.removeView(widgetView);
			return true;
		}
		return false;
	}

	public int getColumns() { return this.widgetContainer.columns; }
	public int getRows() { return this.widgetContainer.rows; }

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

		// Account for the extra margin created by the pixel shifter
		int usableWidth = Math.max(0, availableWidth - (WidgetPixelShifter.SHIFT_LENGTH * 2));
		int usableHeight = Math.max(0, availableHeight - (WidgetPixelShifter.SHIFT_LENGTH * 2));

		// Determine the maximum possible size for a single cell so the entire grid fits inside usable bounds
		int maxCellWidth = usableWidth / columns;
		int maxCellHeight = usableHeight / rows;

		// Pick the smaller one to guarantee squares remain perfectly uniform and aspect-ratio locked (1:1)
		mCellSize = Math.min(maxCellWidth, maxCellHeight);

		// Compute total grid dimensions
		int gridTotalWidth = mCellSize * columns;
		int gridTotalHeight = mCellSize * rows;

		// Center the grid inside the available screen area (guaranteeing >= 10px margin on all sides)
		mOffsetX = (availableWidth - gridTotalWidth) / 2;
		mOffsetY = (availableHeight - gridTotalHeight) / 2;

		// Measure each child view to perfectly fill its allocated grid coordinate span
		for (DashboardWidgetBase<?> widget : widgetContainer.getWidgets()) {
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
		for (DashboardWidgetBase<?> widget : widgetContainer.getWidgets()) {
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
