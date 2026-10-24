package com.vovan.phonedash.widgetHandlers;

import android.os.Handler;

import com.vovan.phonedash.widgets.LoopingWidget;

import java.util.HashMap;
import java.util.Map;

public final class LoopingWidgetsHandler {
	private final Handler handler;
	private final Map<LoopingWidget,Runnable> runnables;

	public LoopingWidgetsHandler(Handler handler) {
		this.handler = handler;
		this.runnables = new HashMap<>();
	}

	public void startLoopingWidget(LoopingWidget widget) {
		if (this.runnables.containsKey(widget)) return;
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				long delay = widget.onLoop();
				handler.postDelayed(this,delay);
			}
		};
		this.runnables.put(widget,runnable);
		this.handler.post(runnable);
	}

	public void stopLoopingWidget(LoopingWidget widget) {
		Runnable runnable = this.runnables.remove(widget);
		if (runnable != null) this.handler.removeCallbacks(runnable);
	}
}
