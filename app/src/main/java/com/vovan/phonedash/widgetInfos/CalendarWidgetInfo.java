package com.vovan.phonedash.widgetInfos;

import androidx.annotation.Nullable;

public class CalendarWidgetInfo {
	public static class CalendarEvent {
		public final String name;
		public final String duration;
		public final int color;

		public CalendarEvent(String name, String duration, int color) {
			this.name = name;
			this.duration = duration;
			this.color = color;
		}

		/**
		 * Initializes an 'All day' event with a provided name and color
		 */
		public CalendarEvent(String name, int color) {
			this(name, "All day", color);
		}
	}

	@Nullable
	public CalendarEvent[] calendarEvents;
	public int currentPage;
	public int eventsPerPage;
	public int date;
	public String weekday;

	public CalendarWidgetInfo(
			int currentPage,
			int eventsPerPage,
			@Nullable CalendarEvent[] calendarEvents,
			int date,
			String weekday) {
		this.calendarEvents = calendarEvents;
		this.currentPage = currentPage;
		this.eventsPerPage = eventsPerPage;
		this.date = date;
		this.weekday = weekday;
	}

	public int getPageCount() {
		if (this.calendarEvents == null) return 0;
		return Math.ceilDiv(this.calendarEvents.length, this.eventsPerPage);
	}

	public void nextPage() {
		if (this.currentPage >= this.getPageCount()) {
			this.currentPage = 1;
			return;
		}
		this.currentPage++;
	}

	public static CalendarWidgetInfo empty(int date, String weekday) {
		return new CalendarWidgetInfo(1,1,null, date, weekday);
	}
}
