package com.danbt.simple;

public interface Constants 
{
	public static final int TEXT_SIZE_SMALL = 15;
	public static final int TEXT_SIZE_LARGE = 80;
	public static final int INDEX_KM = 0;
	public static final int INDEX_MILES = 1;
	public static final int DEFAULT_SPEED_LIMIT = 80;
	public static final int HOUR_MULTIPLIER = 3600;
	public static final double KM_MULTIPLIER = 0.001;
	public static final int EARTH_RADIUS = 6367500;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	
	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;
	
	// START MESSAGE (The message after which the MCU starts sending data)
	public static final byte[] START_MESSAGE = "S".getBytes();
		
	// REGEX that finds the voltage (5 digits), State of charge (1 to 3 digits)
	// Time till end (1 to 5 digits), current (- 1 to 5 digits) 
	// and the Battery Charging (G) or Discharging (D)
	public static final String STRING_FORMAT = "V\\d{5}S\\d{1,3}T\\d{1,5}C-?\\d{1,5}[GD]";
	
	public static final int MINTIME_BETWEEN_GUI_UPDATE = 3000;
	public static final int MINTIME_BETWEEN_SEND = 500;
	public static final int MAXTIME_BETWEEN_GPS_UPDATE = 4000;
	
	//Debugging mode (logs)
	public static final int MEASUREMENT_INDEX = 0;
	public static final double MAX_SPEED_INCREASE = 50;
	public static final int MAX_GPS_ACCURACY = 20;

}
