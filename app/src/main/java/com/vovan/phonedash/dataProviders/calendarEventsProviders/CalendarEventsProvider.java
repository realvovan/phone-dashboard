package com.vovan.phonedash.dataProviders.calendarEventsProviders;

import com.vovan.phonedash.dataProviders.HttpCallback;
import com.vovan.phonedash.widgetInfos.CalendarWidgetInfo;

public interface CalendarEventsProvider {
	void getEventsForDay(
			int year, int month, int day,
			boolean includeRepeated,
			HttpCallback<CalendarWidgetInfo.CalendarEvent[]> callback);
}
