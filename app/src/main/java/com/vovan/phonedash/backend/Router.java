package com.vovan.phonedash.backend;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

final class Router {
	private final Map<String,RouteHandler> routes = new HashMap<>();

	public void register(NanoHTTPD.Method method, String path, RouteHandler handler) {
		String key = method.name() + ":" + path;
		if (this.routes.containsKey(key))
			throw new IllegalArgumentException(String.format("Handler at path %s already exists",key));
		this.routes.put(key, handler);
	}

	public NanoHTTPD.Response dispatch(NanoHTTPD.IHTTPSession session) {
		String key = session.getMethod().name() + ":" + session.getUri();
		RouteHandler handler = this.routes.get(key);

		if (handler == null) {
			return NanoHTTPD.newFixedLengthResponse(
					NanoHTTPD.Response.Status.NOT_FOUND,
					"application/json",
					"{\"error\":\"handler not found\"}"
			);
		}

		try {
			return handler.handle(session);
		} catch (Exception e) {
			Log.e("Router", "Handler error for " + key, e);
			return NanoHTTPD.newFixedLengthResponse(
					NanoHTTPD.Response.Status.INTERNAL_ERROR,
					"application/json",
					"{\"error\":\"internal error\"}"
			);
		}
	}
}
