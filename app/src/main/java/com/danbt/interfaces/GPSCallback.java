package com.danbt.interfaces;

import android.location.Location;

public interface GPSCallback
{
	public abstract void onGPSUpdate(Location location);
	public abstract void onGPSStatusChanged(int satellites);
	public abstract void noGPS(boolean b);
}
