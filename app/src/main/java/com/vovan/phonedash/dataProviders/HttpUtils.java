package com.vovan.phonedash.dataProviders;

import androidx.annotation.Nullable;

import java.util.function.Consumer;

import retrofit2.Response;

public class HttpUtils {
	@Nullable
	public static <T> T requireBody(Response<T> response, Consumer<Exception> failureCallback) {
		T body = response.body();
		if (!response.isSuccessful() || body == null) {
			failureCallback.accept(new Exception("HTTP " + response.code()));
			return null;
		}
		return body;
	}
}
