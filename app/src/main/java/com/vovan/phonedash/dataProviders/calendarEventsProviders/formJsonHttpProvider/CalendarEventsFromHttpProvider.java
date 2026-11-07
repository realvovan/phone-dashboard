package com.vovan.phonedash.dataProviders.calendarEventsProviders.formJsonHttpProvider;

import com.vovan.phonedash.dataProviders.HttpCallback;
import com.vovan.phonedash.dataProviders.HttpUtils;
import com.vovan.phonedash.dataProviders.calendarEventsProviders.CalendarEventsProvider;
import com.vovan.phonedash.dataProviders.calendarEventsProviders.fromJsonFileProvider.CalendarEventsFromJsonProvider;
import com.vovan.phonedash.dataProviders.calendarEventsProviders.fromJsonFileProvider.EventModel;
import com.vovan.phonedash.dataProviders.calendarEventsProviders.fromJsonFileProvider.EventRepeatCyclesModel;
import com.vovan.phonedash.widgetInfos.CalendarWidgetInfo;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class CalendarEventsFromHttpProvider implements CalendarEventsProvider {
	private final CalendarEventsFromHttpClient.CalendarEventsFromJsonApi api;

	public CalendarEventsFromHttpProvider(String url) {
		this.api = new CalendarEventsFromHttpClient(url).calendarEventsFromJsonApi;
	}

	public void getEventsForDay(
			int year, int month, int day,
			boolean includeRepeated,
			HttpCallback<CalendarWidgetInfo.CalendarEvent[]> callback
	) {
		LocalDate targetDate;
		try {
			targetDate = LocalDate.of(year, month, day);
		} catch (Exception e) {
			callback.onFailure(e);
			return;
		}
		this.api.getCalendarEvents().enqueue(new Callback<CalendarEventsFromHttpResponse>() {
			@EverythingIsNonNull
			@Override
			public void onResponse(Call<CalendarEventsFromHttpResponse> call, Response<CalendarEventsFromHttpResponse> response) {
				CalendarEventsFromHttpResponse body = HttpUtils.requireBody(response, callback::onFailure);
				if (body == null) return;
				EventRepeatCyclesModel repeatCyclesModel = body.EventRepeatCycles;
				List<CalendarWidgetInfo.CalendarEvent> matchingEvents = new LinkedList<>();
				for (EventModel i : body.CalendarEvents) {
					if (CalendarEventsFromJsonProvider.isMatchingEvent(i, targetDate, includeRepeated, repeatCyclesModel)) {
						matchingEvents.add(new CalendarWidgetInfo.CalendarEvent(
								i.name,
								i.duration,
								i.color
						));
					}
				}
				callback.onSuccess(matchingEvents.toArray(new CalendarWidgetInfo.CalendarEvent[0]));
			}

			@EverythingIsNonNull
			@Override
			public void onFailure(Call<CalendarEventsFromHttpResponse> call, Throwable t) {
				callback.onFailure(new Exception(t));
			}
		});
	}
}
