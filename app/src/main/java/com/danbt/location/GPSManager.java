package com.danbt.location;

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.danbt.interfaces.GPSCallback;

public class GPSManager {
	private static final int gpsMinTime = 3000;
	private static final int gpsMinDistance = 0;

	private static LocationManager locationManager = null;
	private static LocationListener locationListener = null;
	private static GPSCallback gpsCallback = null;
	private static GpsStatus.Listener listener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
				if (locationManager != null) {
					GpsStatus status = locationManager.getGpsStatus(null);
					Iterable<GpsSatellite> sats = status.getSatellites();
					int i = 0;
					for (GpsSatellite s : sats) {
						if (s.usedInFix()) {
							i++;
						}
					}
					GPSManager.gpsCallback.onGPSStatusChanged(i);
				}
			}
		}
	};

	public GPSManager() {
		GPSManager.locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(final Location location) {
				if (GPSManager.gpsCallback != null) {
					if (!locationManager
							.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						GPSManager.gpsCallback.noGPS(true);
					}else{
						GPSManager.gpsCallback.noGPS(false);
						GPSManager.gpsCallback.onGPSUpdate(location);
					}
				}

			}

			@Override
			public void onProviderDisabled(final String provider) {
				if (!locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					GPSManager.gpsCallback.noGPS(true);
				}else{
					GPSManager.gpsCallback.noGPS(false);
				}
			}

			@Override
			public void onProviderEnabled(final String provider) {
				if (!locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					GPSManager.gpsCallback.noGPS(true);
				}else{
					GPSManager.gpsCallback.noGPS(false);
				}
			}

			@Override
			public void onStatusChanged(final String provider,
					final int status, final Bundle extras) {
			}

		};
	}

	public GPSCallback getGPSCallback() {
		return GPSManager.gpsCallback;
	}

	public void setGPSCallback(final GPSCallback gpsCallback) {
		GPSManager.gpsCallback = gpsCallback;
	}

	public void startListening(final Context context) {
		if (GPSManager.locationManager == null) {
			GPSManager.locationManager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
		}
		locationManager.addGpsStatusListener(listener);
		final Criteria criteria = new Criteria();

		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setSpeedRequired(false);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		final String bestProvider = GPSManager.locationManager.getBestProvider(
				criteria, true);

		if (bestProvider != null && bestProvider.length() > 0) {
			GPSManager.locationManager.requestLocationUpdates(bestProvider,
					GPSManager.gpsMinTime, GPSManager.gpsMinDistance,
					GPSManager.locationListener);
		} else {
			final List<String> providers = GPSManager.locationManager
					.getProviders(true);

			for (final String provider : providers) {
				GPSManager.locationManager.requestLocationUpdates(provider,
						GPSManager.gpsMinTime, GPSManager.gpsMinDistance,
						GPSManager.locationListener);
			}

		}
	}

	public void stopListening() {
		try {
			if (GPSManager.locationManager != null) {
				if (GPSManager.locationListener != null) {
					GPSManager.locationManager
							.removeUpdates(GPSManager.locationListener);
				}
				if (listener != null) {
					// locationManager.removeGpsStatusListener(listener);
				}
			}

			GPSManager.locationManager = null;
		} catch (final Exception ex) {

		}
	}
}
