package com.vovan.phonedash.dataProviders.calendarEventsProviders.formJsonHttpProvider;

import com.vovan.phonedash.dataProviders.calendarEventsProviders.fromJsonFileProvider.EventModel;
import com.vovan.phonedash.dataProviders.calendarEventsProviders.fromJsonFileProvider.EventRepeatCyclesModel;

import java.util.List;

class CalendarEventsFromHttpResponse {
	// json from file storage and from the web expect the same format
	// so im just reusing the classes idk

	public EventRepeatCyclesModel EventRepeatCycles;
	public List<EventModel> CalendarEvents;
}
