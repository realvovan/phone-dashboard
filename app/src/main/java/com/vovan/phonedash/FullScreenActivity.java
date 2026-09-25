package com.vovan.phonedash;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

abstract class FullScreenActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstances) {
		super.onCreate(savedInstances);

		// makes the screen not go dark
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// fullscreen
		WindowCompat.setDecorFitsSystemWindows(this.getWindow(), false);
		WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(
				this.getWindow(), this.getWindow().getDecorView()
		);
		controller.hide(WindowInsetsCompat.Type.systemBars());

		// System bars can temporarily appear when swiping from the edge
		controller.setSystemBarsBehavior(
				WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
		);
	}
}
