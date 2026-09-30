package com.vovan.phonedash.backend;

import android.util.Log;

import com.vovan.phonedash.dataProviders.HealthRegistry;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

final class WebServer extends NanoHTTPD {
	private final Router router;

	public WebServer(int port) {
		super(port);
		this.router = new Router();
		this.registerRoutes();
	}

	private void registerRoutes() {
		this.router.register(Method.POST, "/health", this::handleHourlyHealthInfo);
	}

	@Override
	public Response serve(IHTTPSession session) {
		return this.router.dispatch(session);
	}

	// Handlers
	private Response handleHourlyHealthInfo(IHTTPSession session) {
		try {
			JSONObject healthObj = parseBodyFromPOST(session);
			HealthRegistry.stepCount = healthObj.getInt("steps");
			HealthRegistry.distance = healthObj.getInt("distance");
			HealthRegistry.activeCalories = healthObj.getInt("active_energy");
			HealthRegistry.restingCalories = healthObj.getInt("resting_energy");
			HealthRegistry.fireHealthUpdateCallbacks();
			return ok();
		} catch (Exception e) {
			Log.w("WebServer", "Exception caught in handleHourlyHealthInfo", e);
			return badRequest();
		}
	}


	private static JSONObject parseBodyFromPOST(IHTTPSession session) throws Exception {
		try {
			Map<String,String> files = new HashMap<>();
			session.parseBody(files);
			return new JSONObject(files.get("postData"));
		} catch (Exception e) {
			throw new Exception("Could not parse JSON object", e);
		}
	}
	private static Response ok() {
		return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"ok\":true}");
	}
	private static Response badRequest() {
		return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Bad request");
	}
}
