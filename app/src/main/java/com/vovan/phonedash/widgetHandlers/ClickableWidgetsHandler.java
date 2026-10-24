package com.vovan.phonedash.widgetHandlers;

import com.vovan.phonedash.widgets.ClickableWidget;
import com.vovan.phonedash.widgets.DashboardWidgetBase;

public final class ClickableWidgetsHandler {
	public <T extends DashboardWidgetBase<?> & ClickableWidget> void startClickableWidget(T widget) {
		widget.getView().setOnClickListener(widget::onClick);
	}

	public void pauseClickableWidget(DashboardWidgetBase<?> widget) {
		widget.getView().setOnClickListener(null);
	}
}
