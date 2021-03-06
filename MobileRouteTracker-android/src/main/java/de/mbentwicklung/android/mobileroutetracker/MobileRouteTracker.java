package de.mbentwicklung.android.mobileroutetracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import de.mbentwicklung.android.mobileroutetracker.tracking.LocationTrackingService;
import de.mbentwicklung.android.mobileroutetracker.webservice.LocationSendingService;

/**
 * @author Marc Bellmann <marc.bellmann@mb-entwicklung.de>
 */
public class MobileRouteTracker extends Activity {

	private static final String APP_ID = "MobileRouteTracker";

	private AlarmManager alarmManager;

	private PendingIntent locationSendingService;
	private PendingIntent locationTrackingService;

	private ComponentManager componentManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.componentManager = new ComponentManager(this);
		this.alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		componentManager.getRouteIDEditText().setText(getPrefString(MRTConstants.ID));
		componentManager.getRoutePWEditText().setText(getPrefString(MRTConstants.PW));

		componentManager.getStartButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSendingService();
				startTrackingService();
				componentManager.loadRunState();
			}
		});

		componentManager.getStopButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopSendingService();
				stopTrackingService();
				componentManager.loadWaitState();
			}
		});

		// if (isLocationSendingServiceRunning()) {
		// componentManager.loadStartState();
		// } else {
		// componentManager.loadStopState();
		// }
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		stopSendingService();
	}

	private void startSendingService() {

		final String id = componentManager.getRouteIDEditText().getText().toString();
		final String pw = componentManager.getRoutePWEditText().getText().toString();

		setPrefString(MRTConstants.ID, id);
		setPrefString(MRTConstants.PW, pw);

		final Intent service = new Intent(this, LocationSendingService.class);
		service.putExtra(MRTConstants.ID, id);
		service.putExtra(MRTConstants.PW, pw);
		locationSendingService = PendingIntent.getService(this, 0, service, 0);

		final TimeInterval interval = componentManager.getTimeInterval();
		final long firstStart = System.currentTimeMillis();

		alarmManager.setInexactRepeating(AlarmManager.RTC, firstStart, interval.getTime(),
				locationSendingService);

		Log.d("mobileroutetracker", "LocationSendingService with interval " + interval + " started");
	}

	private void startTrackingService() {

		final Intent service = new Intent(this, LocationTrackingService.class);
		locationTrackingService = PendingIntent.getService(this, 0, service, 0);

		final TimeInterval interval = componentManager.getTimeInterval();
		final long firstStart = System.currentTimeMillis();

		alarmManager.setInexactRepeating(AlarmManager.RTC, firstStart, interval.getTime(),
				locationTrackingService);

		Log.d("mobileroutetracker", "LocationTrackingService with interval " + interval
				+ " started");
	}

	private void stopSendingService() {
		alarmManager.cancel(locationSendingService);
		if (locationSendingService != null) {
			locationSendingService.cancel();
		}
	}

	private void stopTrackingService() {
		alarmManager.cancel(locationTrackingService);
		if (locationTrackingService != null) {
			locationTrackingService.cancel();
		}
	}

	private boolean isLocationSendingServiceRunning() {
		final String className = "de.mbentwicklung.android.mobileroutetracker.LocationSendingService";
		final ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (className.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private String getPrefString(final String key) {
		final SharedPreferences prefs = getSharedPreferences(APP_ID, Context.MODE_PRIVATE);
		return prefs.getString(key, "");
	}

	private void setPrefString(final String key, final String val) {
		final SharedPreferences prefs = getSharedPreferences(APP_ID, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(key, val);
		editor.commit();
	}
}
