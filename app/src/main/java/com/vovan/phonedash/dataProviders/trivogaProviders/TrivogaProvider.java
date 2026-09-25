package com.vovan.phonedash.dataProviders.trivogaProviders;

import com.vovan.phonedash.dataProviders.HttpCallback;

public interface TrivogaProvider {
	void getAlertInRegion(String region, HttpCallback<Trivoga> callback);
}
