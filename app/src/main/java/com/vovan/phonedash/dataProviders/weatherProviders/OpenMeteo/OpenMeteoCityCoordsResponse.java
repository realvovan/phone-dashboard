package com.vovan.phonedash.dataProviders.weatherProviders.OpenMeteo;

import java.util.List;

public class OpenMeteoCityCoordsResponse {
	public static class Result {
		public String name;
		public double latitude;
		public double longitude;
		public String country;
		public String admin1;
	}

	public List<Result> results;
}
