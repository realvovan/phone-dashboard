package com.vovan.phonedash.dataProviders.calendarEventsProviders;

import com.vovan.phonedash.widgetInfos.CalendarWidgetInfo;

public interface CalendarEventsProvider {
	CalendarWidgetInfo.CalendarEvent[] getEventsForDay(
			int year, int month, int day,
			boolean includeRepeated);
}
