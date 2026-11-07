package com.vovan.phonedash.dataProviders.calendarEventsProviders.formJsonHttpProvider;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

class ColorDeserializer implements JsonDeserializer<Integer> {
	@Override
	public Integer deserialize(
			JsonElement json,
			Type typeOfT,
			JsonDeserializationContext context
	) throws JsonParseException {
		return (int) json.getAsLong();
	}
}
