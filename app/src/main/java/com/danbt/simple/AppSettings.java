package com.danbt.simple;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {
	private static final String UNIT_STRING = "MeasureUnit";
	private static final String TOTAL = "total_meter";
	private static final String PREF_NAME = "RollerApp_2012";
	
	public static int getMeasureUnit(Context context){
		return getInt(context,AppSettings.UNIT_STRING);
	}
	
	public static void setMeasureUnit(Context context,int limit){
		putInt(context,AppSettings.UNIT_STRING,limit);
	}
	
	public static int getTotalm(Context context){
		return getFloat(context,AppSettings.TOTAL);
	}
	
	public static void setTotalm(Context context,float value){
		putFloat(context,AppSettings.TOTAL,value);
	}
	
	private static int getInt(Context context, String tag) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, 0);

		return pref.getInt(tag, 0);
	}
	
	public static void putInt(Context context, String tag, int value) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(tag, value);
		editor.commit();
	}
	
	private static int getFloat(Context context, String tag) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, 0);

		return pref.getInt(tag, 0);
	}
	
	public static void putFloat(Context context, String tag, float value) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor editor = pref.edit();
		editor.putFloat(tag, value);
		editor.commit();
	}
}
