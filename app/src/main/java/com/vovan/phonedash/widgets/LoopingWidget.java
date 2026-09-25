package com.vovan.phonedash.widgets;


import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;

public abstract class LoopingWidget<TWidgetInfo> extends DashboardWidgetBase<TWidgetInfo> {
	private final Handler handler;
	protected boolean isRunning = false;
	protected final Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if (!isRunning) { return; }
			long delay = onLoop();
			handler.postDelayed(this,delay);
		}
	};

	protected LoopingWidget(Handler handler, int gridX, int gridY) {
		super(gridX,gridY);

		this.handler = handler;
	}

	protected final void addAutomaticWidgetStartListeners() {
		this.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
			@Override
			public void onViewAttachedToWindow(@NonNull View v) {
				startWidget();
			}

			@Override
			public void onViewDetachedFromWindow(@NonNull View v) {
				pauseWidget();
			}
		});
	}

	@Override
	public void startWidget() {
		if (!this.isRunning) {
			this.isRunning = true;
			this.runnable.run();
		}
	}

	@Override
	public void pauseWidget() {
		this.handler.removeCallbacks(this.runnable);
		this.isRunning = false;
	}

	/**
	 * Override to set the method that runs every loop
	 * @return the time in milliseconds to wait before the next loop
	 */
	protected abstract long onLoop();
}
