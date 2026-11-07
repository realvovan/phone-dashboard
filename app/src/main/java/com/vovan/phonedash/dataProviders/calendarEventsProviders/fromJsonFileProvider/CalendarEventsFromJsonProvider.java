package com.vovan.phonedash.dataProviders.calendarEventsProviders.fromJsonFileProvider;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.stream.JsonReader;
import com.vovan.phonedash.dataProviders.HttpCallback;
import com.vovan.phonedash.dataProviders.calendarEventsProviders.CalendarEventsProvider;
import com.vovan.phonedash.widgetInfos.CalendarWidgetInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CalendarEventsFromJsonProvider implements CalendarEventsProvider {
	private final Context context;
	private final String assetPath;

	public CalendarEventsFromJsonProvider(Context context, String assetPath) {
		this.context = context;
		this.assetPath = assetPath != null ? assetPath : "calendar_events.json";
	}

	private InputStream openInputStream() throws IOException {
		return this.context.getAssets().open(this.assetPath);
	}

	@Override
	public void getEventsForDay(
			int year, int month, int day,
			boolean includeRepeated,
			HttpCallback<CalendarWidgetInfo.CalendarEvent[]> callback) {

		LocalDate targetDate;
		try {
			targetDate = LocalDate.of(year, month, day);
		} catch (Exception e) {
			callback.onFailure(e);
			return;
		}

		List<CalendarWidgetInfo.CalendarEvent> matchingEvents = new ArrayList<>();
		EventRepeatCyclesModel repeatCyclesModel = null;

		Gson gson = new GsonBuilder()
				.registerTypeAdapter(int.class, (JsonDeserializer<Integer>) (json, typeOfT, ctx) -> (int) json.getAsLong())
				.registerTypeAdapter(Integer.class, (JsonDeserializer<Integer>) (json, typeOfT, ctx) -> (int) json.getAsLong())
				.create();

		try (InputStream inputStream = openInputStream()) {
			try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
				 JsonReader jsonReader = new JsonReader(reader)) {

				jsonReader.beginObject();
				while (jsonReader.hasNext()) {
					String name = jsonReader.nextName();
					if ("EventRepeatCycles".equalsIgnoreCase(name)) {
						repeatCyclesModel = gson.fromJson(jsonReader, EventRepeatCyclesModel.class);
					} else if ("CalendarEvents".equalsIgnoreCase(name)) {
						jsonReader.beginArray();
						while (jsonReader.hasNext()) {
							EventModel event = gson.fromJson(jsonReader, EventModel.class);
							if (event != null && isMatchingEvent(event, targetDate, includeRepeated, repeatCyclesModel)) {
								String duration = event.duration != null ? event.duration : "All day";
								matchingEvents.add(new CalendarWidgetInfo.CalendarEvent(
										event.name,
										duration,
										event.color
								));
							}
						}
						jsonReader.endArray();
					} else {
						jsonReader.skipValue();
					}
				}
				jsonReader.endObject();
			}
		} catch (IOException e) {
			Log.e("JsonCalendarProvider",e.getMessage());
			callback.onFailure(e);
			return;
		}

		callback.onSuccess(matchingEvents.toArray(new CalendarWidgetInfo.CalendarEvent[0]));
	}

	public static boolean isMatchingEvent(
			EventModel event,
			LocalDate targetDate,
			boolean includeRepeated,
			@Nullable EventRepeatCyclesModel repeatCyclesModel) {

		LocalDate eventDate;
		try {
			eventDate = LocalDate.of(event.year, event.month, event.day);
		} catch (Exception e) {
			return false;
		}

		if (targetDate.isBefore(eventDate)) {
			return false;
		}

		if (eventDate.equals(targetDate)) {
			return true;
		}

		if (!includeRepeated) {
			return false;
		}

		if (event.Repeat > 0) {
			long daysBetween = ChronoUnit.DAYS.between(eventDate, targetDate);
			return daysBetween % event.Repeat == 0;
		} else {
			int neverVal = (repeatCyclesModel != null) ? repeatCyclesModel.NEVER : 0;
			int monthlyVal = (repeatCyclesModel != null) ? repeatCyclesModel.MONTHLY : -1;
			int yearlyVal = (repeatCyclesModel != null) ? repeatCyclesModel.YEARLY : -2;

			if (event.Repeat == neverVal) {
				return false;
			} else if (event.Repeat == monthlyVal) {
				int expectedDay = Math.min(eventDate.getDayOfMonth(), targetDate.lengthOfMonth());
				return targetDate.getDayOfMonth() == expectedDay;
			} else if (event.Repeat == yearlyVal) {
				int expectedDay = Math.min(eventDate.getDayOfMonth(), targetDate.lengthOfMonth());
				return targetDate.getMonthValue() == eventDate.getMonthValue()
						&& targetDate.getDayOfMonth() == expectedDay;
			}
		}

		return false;
	}
}

