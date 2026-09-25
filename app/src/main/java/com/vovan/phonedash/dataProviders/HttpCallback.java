package com.vovan.phonedash.dataProviders;

public interface HttpCallback<TResponse> {
	void onSuccess(TResponse response);
	void onFailure(Exception e);
}
