package com.vovan.phonedash.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.vovan.phonedash.WidgetContainer;
import com.vovan.phonedash.widgetInfos.EmptyWidgetInfo;

/**
 * A 1x1 test widget with a gradient fill and a visible border
 * to help visualize grid square boundaries.
 */
public class TestGradientWidget extends DashboardWidgetBase<EmptyWidgetInfo> {
	private final View view;

	public TestGradientWidget(Context context, WidgetContainer container, int gridX, int gridY) {
		super(context,container, gridX, gridY);

		this.view = new View(context);

		// Create a gradient background: diagonal from Top-Left to Bottom-Right
		// Using distinct vibrant colors so the squares stand out
		GradientDrawable gradientDrawable = new GradientDrawable(
				GradientDrawable.Orientation.TL_BR,
				new int[]{Color.parseColor("#FF416C"), Color.parseColor("#FF4B2B")}
		);

		// Add a clear 2dp white border/stroke to identify the grid edges
		float density = context.getResources().getDisplayMetrics().density;
		int strokeWidth = (int) (2 * density);
		gradientDrawable.setStroke(strokeWidth, Color.WHITE);
		gradientDrawable.setCornerRadius(4 * density); // slightly rounded corners

		this.view.setBackground(gradientDrawable);
	}

	@Override
	public int width() {
		return 1;
	}

	@Override
	public int height() {
		return 1;
	}

	@Override
	public void startWidget() {}
	@Override
	public void pauseWidget() {}
	@Override
	public void setWidgetInfo(EmptyWidgetInfo i) {}
	@Override
	public EmptyWidgetInfo getDefaultWidgetInfo() { return EmptyWidgetInfo.INSTANCE; }
	@Override
	public View getView() {
		return this.view;
	}
}
