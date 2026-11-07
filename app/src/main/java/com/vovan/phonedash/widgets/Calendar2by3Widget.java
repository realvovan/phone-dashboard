package com.vovan.phonedash.widgets;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vovan.phonedash.R;
import com.vovan.phonedash.WidgetContainerLayout;
import com.vovan.phonedash.dataProviders.HttpCallback;
import com.vovan.phonedash.dataProviders.calendarEventsProviders.CalendarEventsProvider;
import com.vovan.phonedash.drawables.SquircleDrawable;
import com.vovan.phonedash.widgetInfos.CalendarWidgetInfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * A 2:3 Calendar Widget created based on the SVG design specification.
 * Displays current date/weekday, up to 3 event cards per page with pill indicators,
 * and page indicator at the bottom. Automatically scales to fit different grid/screen sizes.
 */
public class Calendar2by3Widget
		extends DashboardWidgetBase<CalendarWidgetInfo>
		implements ClickableWidget {
	private final CalendarEventsProvider eventsProvider;

	private final View rootView;
	private final TextView weekdayText;
	private final TextView dateText;
	private final TextView pageIndicator;
	private final TextView noEventsIndicator;

	// parent to pills and cardBoxes
	private final View[] eventCards = new View[3];
	private final View[] pills = new View[3];
	private final View[] cardBoxes = new View[3];
	private final TextView[] eventTitles = new TextView[3];
	private final TextView[] eventTimes = new TextView[3];

	private final SquircleDrawable backgroundDrawable;
	private final Context appContext;
	private boolean isReceiverRegistered = false;

	// events storing
	private CalendarWidgetInfo lastInfo;

	private final BroadcastReceiver dateChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onDateChange();
		}
	};

	@SuppressLint("InflateParams")
	public Calendar2by3Widget(Context context, WidgetContainerLayout container, int gridX, int gridY, CalendarEventsProvider eventsProvider) {
		super(context, container, gridX, gridY);

		this.eventsProvider = eventsProvider;
		this.appContext = context.getApplicationContext();
		this.rootView = LayoutInflater.from(context).inflate(R.layout.calendar_widget, null);

		this.weekdayText = this.rootView.findViewById(R.id.weekday_text);
		this.dateText = this.rootView.findViewById(R.id.date_text);
		this.pageIndicator = this.rootView.findViewById(R.id.page_indicator);
		this.noEventsIndicator = this.rootView.findViewById(R.id.no_events_text);

		this.eventCards[0] = this.rootView.findViewById(R.id.event_card_0);
		this.eventCards[1] = this.rootView.findViewById(R.id.event_card_1);
		this.eventCards[2] = this.rootView.findViewById(R.id.event_card_2);

		this.pills[0] = this.rootView.findViewById(R.id.pill_0);
		this.pills[1] = this.rootView.findViewById(R.id.pill_1);
		this.pills[2] = this.rootView.findViewById(R.id.pill_2);

		this.cardBoxes[0] = this.rootView.findViewById(R.id.card_box_0);
		this.cardBoxes[1] = this.rootView.findViewById(R.id.card_box_1);
		this.cardBoxes[2] = this.rootView.findViewById(R.id.card_box_2);

		this.eventTitles[0] = this.rootView.findViewById(R.id.event_title_0);
		this.eventTitles[1] = this.rootView.findViewById(R.id.event_title_1);
		this.eventTitles[2] = this.rootView.findViewById(R.id.event_title_2);

		this.eventTimes[0] = this.rootView.findViewById(R.id.event_time_0);
		this.eventTimes[1] = this.rootView.findViewById(R.id.event_time_1);
		this.eventTimes[2] = this.rootView.findViewById(R.id.event_time_2);

		for (int i = 0; i < 3; i++) {
			this.eventTitles[i].setMaxLines(2);
			this.eventTitles[i].setEllipsize(TextUtils.TruncateAt.END);
		}

		// Background squircle
		this.backgroundDrawable = new SquircleDrawable(30f, 0xFF111111);
		this.rootView.setBackground(this.backgroundDrawable);

		this.addStateChangeListeners();

		// Auto-scaling when view dimensions change
		this.rootView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
			int width = right - left;
			int height = bottom - top;
			if (width > 0 && height > 0 && (width != (oldRight - oldLeft) || height != (oldBottom - oldTop))) {
				adjustScaling(width, height);
			}
		});

		this.setDefaultWidgetInfo();
	}

	/**
	 * Dynamically adjusts font sizes, paddings, margins, pill sizes, and corner radius based on cell bounds.
	 */
	private void adjustScaling(int width, int height) {
		float squircleWidth = width * 0.95f;
		float squircleHeight = height * 0.95f;
		float insetX = (width - squircleWidth) / 2f;
		float insetY = (height - squircleHeight) / 2f;

		this.backgroundDrawable.setInset(insetX, insetY);

		float scaleX = squircleWidth / 400f;
		float scaleY = squircleHeight / 600f;
		float scale = Math.min(scaleX, scaleY);

		// Root Padding
		int padX = Math.round(insetX + (25f * scale));
		int padY = Math.round(insetY + (20f * scale));
		this.rootView.setPadding(padX, padY, padX, padY);

		// Background corner radius
		this.backgroundDrawable.setCornerRadius(30f * scale);

		// Header Font sizes
		this.weekdayText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 28f * scale);
		this.dateText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 72f * scale);

		// Cards scaling
		int pillWidth = Math.round(12f * scale);
		int pillHeight = Math.round(110f * scale);
		int pillMarginEnd = Math.round(12f * scale);
		int cardMarginBottom = Math.round(14f * scale);
		int cardPaddingStart = Math.round(14f * scale);
		int cardPaddingTop = Math.round(12f * scale);
		int cardPaddingBottom = Math.round(10f * scale);

		for (int i = 0; i < 3; i++) {
			ViewGroup.LayoutParams pillParams = this.pills[i].getLayoutParams();
			if (pillParams != null) {
				pillParams.width = pillWidth;
				pillParams.height = pillHeight;
				if (pillParams instanceof ViewGroup.MarginLayoutParams) {
					((ViewGroup.MarginLayoutParams) pillParams).setMarginEnd(pillMarginEnd);
				}
				this.pills[i].setLayoutParams(pillParams);
			}

			ViewGroup.LayoutParams boxParams = this.cardBoxes[i].getLayoutParams();
			if (boxParams != null) {
				boxParams.height = pillHeight;
				this.cardBoxes[i].setLayoutParams(boxParams);
			}

			this.cardBoxes[i].setPadding(cardPaddingStart, cardPaddingTop, cardPaddingStart, cardPaddingBottom);

			if (i < 2) {
				ViewGroup.LayoutParams cardParams = this.eventCards[i].getLayoutParams();
				if (cardParams instanceof ViewGroup.MarginLayoutParams) {
					((ViewGroup.MarginLayoutParams) cardParams).bottomMargin = cardMarginBottom;
					this.eventCards[i].setLayoutParams(cardParams);
				}
			}

			this.eventTitles[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, 28f * scale);
			this.eventTimes[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, 18f * scale);
		}

		// Page indicator
		this.pageIndicator.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16f * scale);
	}

	/**
	 * Hides or shows an event card by index (0 through 2).
	 *
	 * @param index Index of the card (0, 1, or 2).
	 * @param visible True to show, false to hide.
	 */
	public void setCardVisible(int index, boolean visible) {
		if (index >= 0 && index < 3) {
			this.eventCards[index].setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		} else {
			throw new IndexOutOfBoundsException(String.format("Index %d was out of bounds",index));
		}
	}

	private void onDateChange() {
		LocalDate now = LocalDate.now();
		// EEE for short names and EEEE for full names
		String weekday = now.format(DateTimeFormatter.ofPattern("EEEE",Locale.ENGLISH)).toUpperCase(Locale.ENGLISH);

		this.eventsProvider.getEventsForDay(
				now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
				true,
				new HttpCallback<>() {
					@Override
					public void onSuccess(CalendarWidgetInfo.CalendarEvent[] calendarEvents) {
						lastInfo = new CalendarWidgetInfo(1, 3, calendarEvents, now.getDayOfMonth(), weekday);
						setWidgetInfo(lastInfo);
					}

					@Override
					public void onFailure(Exception e) {
						Log.w("Calendar2by3Widget", Objects.requireNonNull(e.getMessage()));
						lastInfo = new CalendarWidgetInfo(1, 3, null, now.getDayOfMonth(), weekday);
						setWidgetInfo(lastInfo);
					}
				}
		);
	}

	/**
	 * Updates title and time text, and color of an event card by index (0 through 2).
	 */
	private void updateCardContent(
			int index,
			@NonNull String title,
			@NonNull String time,
			int color
	) {
		if (index >= 0 && index < 3) {
			this.eventTitles[index].setText(title);
			this.eventTimes[index].setText(time);

			// Update pill color
			Drawable pillBg = this.pills[index].getBackground();
			if (pillBg != null) {
				if (pillBg instanceof GradientDrawable) {
					((GradientDrawable) pillBg).setColor(color);
				} else {
					pillBg.setTint(color);
				}
			}

			// Update box color (darkened and less saturated)
			int boxColor = getDarkenedAndDesaturatedColor(color);
			Drawable boxBg = this.cardBoxes[index].getBackground();
			if (boxBg != null) {
				if (boxBg instanceof GradientDrawable) {
					((GradientDrawable) boxBg).setColor(boxColor);
				} else {
					boxBg.setTint(boxColor);
				}
			}
		} else {
			throw new IndexOutOfBoundsException(String.format("Index %d was out of bounds",index));
		}
	}

	@Override
	public void startWidget() {
		super.startWidget();
		if (!this.isReceiverRegistered) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_DATE_CHANGED);
			filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
			this.appContext.registerReceiver(this.dateChangeReceiver, filter);
			this.isReceiverRegistered = true;
		}
		this.onDateChange();
}

	@Override
	public void pauseWidget() {
		super.pauseWidget();
		if (this.isReceiverRegistered) {
			try {
				this.appContext.unregisterReceiver(this.dateChangeReceiver);
			} catch (IllegalArgumentException e) {
				// Receiver was not registered
			}
			this.isReceiverRegistered = false;
		}
	}

	@Override
	public int width() {
		return 2;
	}

	@Override
	public int height() {
		return 3;
	}

	@Override
	public void setWidgetInfo(CalendarWidgetInfo info) {
		// hides old cards
		for (int i = 0; i < 3; i++) {
			this.setCardVisible(i, false);
		}

		this.weekdayText.setText(info.weekday);
		this.dateText.setText(String.valueOf(info.date));

		if (info.calendarEvents == null || info.calendarEvents.length == 0) {
			this.noEventsIndicator.setVisibility(View.VISIBLE);
			this.pageIndicator.setVisibility(View.INVISIBLE);
			return;
		}
		this.noEventsIndicator.setVisibility(View.GONE);
		this.pageIndicator.setVisibility(info.calendarEvents.length > info.eventsPerPage ? View.VISIBLE : View.INVISIBLE);
		int pageOffset = (info.currentPage - 1) * info.eventsPerPage;
		for (int cardIndex = 0; cardIndex < info.eventsPerPage && cardIndex < info.calendarEvents.length - pageOffset; cardIndex++) {
			int eventIndex = cardIndex + pageOffset;
			this.setCardVisible(cardIndex, true);
			this.updateCardContent(
					cardIndex,
					info.calendarEvents[eventIndex].name,
					info.calendarEvents[eventIndex].duration,
					info.calendarEvents[eventIndex].color
			);
		}
		this.pageIndicator.setText(String.format(
				Locale.getDefault(),
				"Page: %d/%d",
				info.currentPage,
				info.getPageCount()
		));
	}

	@Override
	public CalendarWidgetInfo getDefaultWidgetInfo() {
		return CalendarWidgetInfo.empty(21,"December");
	}

	@Override
	public View getView() {
		return this.rootView;
	}

	@Override
	public void onClick(View view) {
		if (this.lastInfo == null || this.lastInfo.getPageCount() == 1) return;
		this.lastInfo.nextPage();
		this.setWidgetInfo(this.lastInfo);
	}

	private static int getDarkenedAndDesaturatedColor(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[1] = 0.4f; // saturation
		hsv[2] = 0.15f; // darkness
		return Color.HSVToColor(Color.alpha(color), hsv);
	}
}
