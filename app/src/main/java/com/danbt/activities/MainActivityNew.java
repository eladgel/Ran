package com.danbt.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.danbt.AppConfig;
import com.danbt.R;
import com.danbt.adapters.MySimpleArrayAdapter;
import com.danbt.interfaces.GPSCallback;
import com.danbt.loaders.BTAsyncTaskLoader;
import com.danbt.location.GPSManager;
import com.danbt.services.BluetoothService;
import com.danbt.simple.Constants;
import com.danbt.simple.RGBS;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

import roboguice.activity.RoboActivity;
import roboguice.util.RoboAsyncTask;

/**
 * Contains main method and contains a lot of methods for the app
 *
 * @author Lukas Eipert
 * @version 1.0
 * @since 1.0
 *
 */
public class MainActivityNew extends RoboActivity implements GPSCallback{

    public static final int UNDEFINED = -1;
    public static final int TOKEN_CELL = 0;
    public static final int LAST_MSG_CELL = 1;
    public static final String LAST_MESSAGE = "lastMessage";
    public static final String SCOOTER_BATTERY_LEVEL = "scooterBatteryLevel";
    public static final String MESSAGE = "message";
    public static final String LAST_GUI_UPDATE = "lastGuiUpdate";
    /**
     * Values to be shown on the third screen (ListView)
     */
    private ArrayList<String> listViewArrayList = new ArrayList<String>();
    //	/**
//	 * Array containing listViewArrayList
//	 *
//	 * @see listViewArrayList
//	 */
    private MySimpleArrayAdapter listViewAdapter;
    /**
     * ListView on the third screen containing listViewAdapter
     */
    private ListView listView;

    private boolean firstGUIUpdate = false;

    private GraphicalView mChartView;
    private XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    private XYSeries seriesCurrent = new XYSeries("Current");
    private XYSeries seriesVoltage = new XYSeries("Voltage");
    private XYSeries seriesSpeed = new XYSeries("Speed");
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeriesRenderer rendererCurrent = new XYSeriesRenderer();
    private XYSeriesRenderer rendererVoltage = new XYSeriesRenderer();
    private XYSeriesRenderer rendererSpeed = new XYSeriesRenderer();

    private boolean switcher;
    private int latestVoltage;
    private int latestTimeTilEmpty;
    private int latestScooterBatteryLevel;
    private int latestCurrent;
    private int lastSats = 0;

    private int currentPage;
    private GPSManager gpsManager = null;

    private double mLastVoltageMillis;
    private double mLastCurrentMillis;

    private long mLastLocationMillis = 0;
    private double mLastSpeed = 0;

    private int phoneBatteryLevel = 0;
    private Handler dDelayer = new Handler();
    private long lastGUIUpdate = 0;
    private long lastSend = 0;

    private int currentGraph = 1;

    private double actualspeed;
    private double averagespeed;
    private double speedsum = 0;
    private long mFirstLocationMillis = 0;
    private RGBS rgbs = new RGBS();

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mChatService = null;
    BroadcastReceiver batteryReceiver = null;
    /**
     * Default ViewPager
     */
    private ViewPager myPager;

    private String lastMessage = "";
    /**
     * Context of Application for ViewPager
     */
    private Context cxt;
    private View v;
    private ImageView lightBatPic;
    private TextView debugTV;
//    private BTAsyncTaskLoader loader;
    // *
    // * Methods inherited by Activity (onStart, onCreate, etc.)
    // *

    /**
     * Initialization of the Activity <br>
     * Window is set to full screen with no title <br>
     * PagerAdapter is initialized <br>
     * GPSManager is initialized <br>
     *
     * Registers a BroadCastReciever for the Battery level
     *
     * @param savedInstanceState
     *            Last states of the Activity which can be reloaded
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Setting window flags. No Titlebar shown and Fullscreen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);



        cxt = this;

        // Setting Viewpager to CustompagerAdapter with a cache of 3 pages
        myPager = (ViewPager) findViewById(R.id.myfivepanelpager);
        myPager.setAdapter(new CustomPagerAdapter());
        myPager.setOffscreenPageLimit(3);

        myPager.setOnPageChangeListener(new PageChangeListener());
        myPager.setCurrentItem(1);

        // Establishing listener for GPS Signals
        gpsManager = new GPSManager();
        gpsManager.startListening(cxt);
        gpsManager.setGPSCallback(this);

        dDelayer.removeCallbacks(sSender);
        dDelayer.removeCallbacks(uUpdater);

        // BroadCastreciever for Battery Level
        batteryReceiver = new BroadcastReceiver() {
            int scale = -1;
            int level = -1;

            @Override
            public void onReceive(Context context, Intent intent) {
                level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                phoneBatteryLevel = (level * 100) / scale;
            }

        };
    }

    /**
     * Override onStart Method
     */
    @Override
    public void onStart() {
        super.onStart();

    }

    /**
     * Override onResume
     */
    @Override
    public void onResume() {
        super.onResume();
        if(AppConfig.DEBUG) {
            counter = 0;
        }
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't
            // started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
            if (mChatService.getState() == BluetoothService.STATE_CONNECTED) {
                // Starts sending S again
                dDelayer.postDelayed(sSender, 50);
            }
        }
        registerReceiver(batteryReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));

        dDelayer.postDelayed(uUpdater, 50);
    }

    /**
     * Unregisters gpsListener, BluetoothService, all runnables and closes the
     * app
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        gpsManager.stopListening();
//        gpsManager.setGPSCallback(null);
//        if (mChatService != null) {
//            mChatService.stop();
//        }
//        dDelayer.removeCallbacks(sSender);
//        dDelayer.removeCallbacks(uUpdater);
//

//        gpsManager = null;

////        dDelayer = null;
////        batteryReceiver = null;
////        mChatService = null;
////        mChartView = null;
////        mBluetoothAdapter = null;
////        dataset = null;
////        seriesCurrent = null;
////        seriesVoltage = null;
////        seriesSpeed = null;
////        mRenderer = null;
////        rendererCurrent = null;
////        rendererVoltage = null;
////        rendererSpeed = null;
//        unbindDrawables(findViewById(R.id.RootView1));
//        unbindDrawables(findViewById(R.id.RootView2));
//        unbindDrawables(findViewById(R.id.RootView3));
//        unbindDrawables(findViewById(R.id.RootView));
    }

//    private void unbindDrawables(View view) {
//        if (view.getBackground() != null) {
//            view.getBackground().setCallback(null);
//        }
//        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
//            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
//                unbindDrawables(((ViewGroup) view).getChildAt(i));
//            }
//            ((ViewGroup) view).removeAllViews();
//        }
//    }

    /**
     * remove all runnables
     */
    @Override
    public void onPause() {
        super.onPause();
        dDelayer.removeCallbacks(sSender);
        dDelayer.removeCallbacks(uUpdater);
        unregisterReceiver(batteryReceiver);
    }

    /**
     * remove all runnables
     */
    @Override
    public void onStop() {
        super.onStop();
//        dDelayer.removeCallbacks(sSender);
//        dDelayer.removeCallbacks(uUpdater);
//        gpsManager.stopListening();
//        gpsManager.setGPSCallback(null);

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        CloseApp(null);
    }

    /**
     * If CloseButton is clicked, ask the user whether to terminate the app or
     * not
     *
     * @param view
     */
    public void CloseApp(View view) {
        OnClickListener dialogClickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.close_app)
                .setPositiveButton(android.R.string.yes, dialogClickListener)
                .setNegativeButton(android.R.string.no, dialogClickListener)
                .show();
    }

    // *
    // * Methods connected with the Options Menu
    // *

    /**
     * Create the menu when clicking "Menu Button"
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    /**
     * Clicking the Options in the menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_about: {
                displayAboutDialog();

                return true;
            }
            case R.id.scan: {
/*			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent,
					Constants.REQUEST_CONNECT_DEVICE);
					*/

                startBT(null);

                return true;
            }

        }
        return false;
    }

    /**
     * Showing "about dialog"
     */
    private void displayAboutDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final SpannableString s = new SpannableString(
                this.getText(R.string.about_message));
        Linkify.addLinks(s, Linkify.ALL);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(s);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNeutralButton(R.string.show_license, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
                final SpannableString s = new SpannableString(cxt
                        .getText(R.string.license));
                Linkify.addLinks(s, Linkify.ALL);
                builder.setTitle(getString(R.string.app_name));
                builder.setMessage(s);
                builder.setPositiveButton(android.R.string.ok, null)
                        .setMessage(s);
                builder.create().show();
            }
        });

        builder.create().show();
    }

    // *
    // * Methods connected with Bluetooth
    // *

    /**
     * Setup of the chat (Starting Bluetooth-Service)
     */
    private void setupChat() {

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothService(this, bHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

    }

    /**
     * What happens if you click the Bluetooth Button
     *
     * @param view
     */
    public void startBT(View view) {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null)
                setupChat();
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent,
                    Constants.REQUEST_CONNECT_DEVICE);

        }
        // Launch the DeviceListActivity to see devices and do scan

    }

    /**
     * Sends the S to the Battery.
     */
    private void sendMessage() {

        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {

            Toast.makeText(this, R.string.try_again_to_connect,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the message bytes and tell the BluetoothChatService to write

        mChatService.write(Constants.START_MESSAGE);

        // Reset out string buffer to zero
        mOutStringBuffer.setLength(0);
        lastSend = SystemClock.elapsedRealtime();

    }

    /**
     * Handler that sends S to the BT. The same Handler gets informations back
     * from the BluetoothChatService
     */
    private final Handler bHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ImageView iv = ((ImageView) findViewById(R.id.BluetoothButton));
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            if (iv != null) {
                                iv.setImageResource(R.drawable.connected);
                            }
                            dDelayer.postDelayed(sSender, 50);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            if (iv != null) {
                                iv.setImageResource(R.drawable.nconnected);
                            }
                            break;
                        case BluetoothService.STATE_NONE:
                        case BluetoothService.STATE_LISTEN:
                            if (iv != null) {
                                iv.setImageResource(R.drawable.nconnected);
                            }
                            break;
                        case BluetoothService.STATE_LOST:
                            if (iv != null) {
                                iv.setImageResource(R.drawable.nconnected);
                                drawNice();
                                (((TextView) findViewById(R.id.BigTextM))).setText("");
                                (((TextView) findViewById(R.id.SmallTextM)))
                                        .setText("Disconnect.\nTry Again!");
                                findViewById(R.id.BigTextR)
                                        .setVisibility(View.INVISIBLE);
                                findViewById(R.id.SmallTextR)
                                        .setVisibility(View.INVISIBLE);
                                findViewById(R.id.BigTextL)
                                        .setVisibility(View.INVISIBLE);
                                findViewById(R.id.SmallTextL)
                                        .setVisibility(View.INVISIBLE);
                                findViewById(R.id.PlugImage)
                                        .setVisibility(View.INVISIBLE);
                                firstGUIUpdate = false;
                                dDelayer.removeCallbacks(sSender);
                            }
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ:
                    SystemClock.elapsedRealtime();
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    synchronized (this) {
                        setConcatedMsg(readMessage);
                        String val = getConcatedMsg();

//                        boolean flag = false;
                        if (val.length() >= 24 * 3) {
                            int indexOfFirstV = val.indexOf('V');
                            if (indexOfFirstV != -1) {
//                                flag = true;
                                val = val.substring(indexOfFirstV, val.length() - 1);
                                getValidString(val);
                                clearConcatedMsg();
                            }
                        }
//
//                        if (flag == true) {
//
//                        }
                    }


                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(
                            Constants.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };

    private String concatedMsg;

    public void setConcatedMsg(String addon)
    {
        if(concatedMsg != null)
        {
            concatedMsg += addon;
        }
        else
        {
            concatedMsg = addon;
        }
    }

    public void clearConcatedMsg()
    {
        if(concatedMsg != null)
        {
            concatedMsg = "";
        }
    }

    public String getConcatedMsg()
    {
        return concatedMsg;
    }


    private boolean noGPS;

    /**
     * Showing list of devices to connect to or turning Bluetooth on
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case Constants.REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                    Intent serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent,
                            Constants.REQUEST_CONNECT_DEVICE);
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled,
                            Toast.LENGTH_SHORT).show();
                }
        }
    }
//    public void doSomething()
//    {
//        Integer.
//    }
    /**
     * Connects recieved bytes. If it recieves a string of the format
     * V\\d{5}S\\d{1,3}T\\d{1,5}C-?\\d{1,5}[GD] three times it will call
     * readAndValidateValues with the valid string
     *  0    1   2   3  4
     * "V{5};S{3};T{5};C{5};{G/D};{F}
     * String[] splitted = msg.split(";");
     * //splitted[0] supposed to be Voltage
     * for(String param : splitted)
     * char c = param.charAt(0)
     * switch(c){
     *     V:
     *      break;
     *     S:
     *      break;
     *     T:
     *      break;
     *      etc'
     * }
     * msg.
     * @param readMessage
     */
    public void getValidString(String readMessage) {

        new ResultsAsyncTask(getApplicationContext(), readMessage, lastGUIUpdate).execute();

    }

    class ResultsAsyncTask extends RoboAsyncTask<String>
    {
        private final String msg;
        private final long lastGuiUpdate;

        protected ResultsAsyncTask(Context context, String msg, long lastGuiUpdate) {
            super(context);
            this.msg = msg;
            this.lastGuiUpdate = lastGuiUpdate;
        }

        @Override
        public String call() throws Exception {
            String retVal = null;
            if(msg != null){
                if ((SystemClock.elapsedRealtime() - lastGuiUpdate) > 1500) {
//            lastMessage = msg;//lastMessage + msg;
                    if ((msg.length() - msg.replaceAll("E", "")
                            .length()) >= 3) {
                        String tokens[] = Pattern.compile("E").split(msg);
                        for (int i = 0; i < tokens.length; i++) {
                            if (!Pattern.matches(Constants.STRING_FORMAT, tokens[i])) {
                                tokens[i] = "";
                            }
                        }
                        Arrays.sort(tokens);
                        for (int i = 0; i <= tokens.length - 3; i++) {
                            if (tokens[i].compareTo("") != 0
                                    && tokens[i + 1].compareTo(tokens[i]) == 0
                                    && tokens[i + 2].compareTo(tokens[i]) == 0) {
//                            readAndValidateValues(tokens[i]);
                                retVal = tokens[i];
                                break;
                            }
                        }
                    }

                }
            }

            return retVal;
        }

        @Override
        protected void onSuccess(String string) throws Exception {
            super.onSuccess(string);
            if(string != null) {
                readAndValidateValues(string);
                updateGUI();
            }
        }
    }


    /**
     * Seperates a valid string and check if the values are correct. If values
     * are correct, the last known values and the series for the graph are
     * updated. GUI will also be updated with newest valus. If a value is
     * invalid, the app sends an S again to get valid results faster
     *
     * @param string
     */
    private void readAndValidateValues(String string) {

        boolean sendSAgain = false;
        String tokens[] = Pattern.compile("V|S|T|C|D|G").split(string);
        int voltage = Integer.parseInt(tokens[1]);
        int scooterBatteryLevel = Integer.parseInt(tokens[2]);
        int timeTilEmpty = Integer.parseInt(tokens[3]);
        int current = Integer.parseInt(tokens[4]);

        if (voltage < 5000 || voltage > 60000) {
            sendSAgain = true;
        } else {
            latestVoltage = voltage;
            mLastVoltageMillis = SystemClock.elapsedRealtime();
            seriesVoltage.add(mLastVoltageMillis, voltage);
        }

        if (scooterBatteryLevel < 0 || scooterBatteryLevel > 100) {
            sendSAgain = true;
        } else {
            latestScooterBatteryLevel = scooterBatteryLevel;

        }

        if (timeTilEmpty < 0 || timeTilEmpty > 14500) {
            if (timeTilEmpty > 65535) {
                sendSAgain = true;
            }
            timeTilEmpty = -3;
        }

        if (current < -32768 || current > 20000) {
            sendSAgain = true;
        } else {
            if (current > -100) {
                timeTilEmpty = -1;
            }

            mLastCurrentMillis = SystemClock.elapsedRealtime();
            seriesCurrent.add(mLastCurrentMillis, current);
            latestCurrent = current;

        }

        if (Pattern.matches(".+G", string)) {
            timeTilEmpty = -2;
        }

        latestTimeTilEmpty = timeTilEmpty;




        if (sendSAgain) {
            sendMessage();
            lastGUIUpdate = 1;
        } else {
            lastGUIUpdate = SystemClock.elapsedRealtime();
        }

    }

    // *
    // * Methods connected with GPS
    // *

    /**
     * If recieving a location by the GPS reciever and the accuracy of the GPS
     * Signal is better than 20m we will update the speed
     */
    @Override
    public void onGPSUpdate(Location location) {

        if (location != null) {
            if (location.getAccuracy() < Constants.MAX_GPS_ACCURACY
                    && location.hasSpeed()) {
                updateSpeed(location);
            }
        }

    }

    @Override
    public void onGPSStatusChanged(int sats) {
        if (sats != lastSats) {
            ImageView iv = (ImageView) findViewById(R.id.NumSatellites);
            switch (sats) {
                case 0:
                    iv.setImageResource(R.drawable.numsats0);
                    break;
                case 1:
                case 2:
                    iv.setImageResource(R.drawable.numsats1);
                    break;
                case 3:
                    iv.setImageResource(R.drawable.numsats2);
                    break;
                case 4:
                    iv.setImageResource(R.drawable.numsats3);
                    break;
                default:
                    iv.setImageResource(R.drawable.numsats4);
                    break;
            }
            lastSats = sats;
        }

    }
    @Override
    public void noGPS(boolean b) {
        if(b){
            noGPS=true;
            ImageView iv = (ImageView) findViewById(R.id.Satellite);
            iv.setImageResource(R.drawable.nsatellite);
            iv.setVisibility(View.VISIBLE);
            iv=(ImageView) findViewById(R.id.NumSatellites);
            iv.setVisibility(View.INVISIBLE);
            dDelayer.postDelayed(uUpdater, 30000);
        }else{
            noGPS=false;
            ImageView iv = (ImageView) findViewById(R.id.Satellite);
            iv.setImageResource(R.drawable.satellite);
            iv.setVisibility(View.VISIBLE);
            rgbButton(iv, 0, 0, 0, 0, -1);
            iv=(ImageView) findViewById(R.id.NumSatellites);
            iv.setVisibility(View.VISIBLE);
            dDelayer.postDelayed(uUpdater, 50);
            noGPS=false;
        }
    }

    //	/**
//	 * Updating the speed (if the last GPS update was not longer than
//	 * {@link Constants.MAXTIME_BETWEEN_GPS_UPDATE} ago and the speed increase
//	 * was not higher than {@link Constants.MAX_SPEED_INCREASE})
//	 *
//	 * @param location
//	 */
    private void updateSpeed(Location location) {
        long currentTime = SystemClock.elapsedRealtime();

        // Reset Speed if last GPS update is more than 4s ago
        if (Math.abs((mLastLocationMillis - currentTime)) > Constants.MAXTIME_BETWEEN_GPS_UPDATE) {
            speedsum = 0;
            mFirstLocationMillis = 0;
            mLastSpeed = 0;
        }

        actualspeed = roundDecimal((location.getSpeed() * 3.6), 2);
        if (actualspeed > 2) {
            rgbButton(((ImageView) findViewById(R.id.ButtonMBG)), 51, 181, 229,
                    -1, -1);
            (((TextView) findViewById(R.id.BigTextM)))
                    .setText(actualspeed + "");
            (((TextView) findViewById(R.id.SmallTextM))).setText("km/h");
        }
        if (mFirstLocationMillis != 0) {
            speedsum = speedsum + 1 / 2 * (currentTime - mLastLocationMillis)
                    * (mLastSpeed + actualspeed);
        } else {
            mFirstLocationMillis = currentTime;
        }
        mLastLocationMillis = currentTime;
        mLastSpeed = actualspeed;
        averagespeed = roundDecimal(
                (speedsum / (mLastLocationMillis - mFirstLocationMillis)), 2);
        seriesSpeed.add(mLastLocationMillis, actualspeed);
        updateGUI();

    }

    /**
     * Returns the average if an object traveled it a distance between oldTime
     * and newTime
     *
     * @param distance
     * @param oldTime
     * @param newTime
     * @return
     */
    public static double toSpeedPerhour(float distance, long oldTime,
                                        long newTime) {
        if (newTime == oldTime) {
            // adding 1ms to the newTime to get no devision by zero error
            newTime = newTime + 1;
        }
        double delta = (newTime - oldTime) / 1000;
        double speed = distance / delta;
        double perhour = ((speed * Constants.HOUR_MULTIPLIER) * Constants.KM_MULTIPLIER);
        return perhour;
    }

    // *
    // * Methods connected with the Graphs on Page zero
    // *

    /**
     * Removing all Renderer from mRenderer and all Series from dataset
     */
    private void emptyRenderer() {
        for (int i = 0; i < mRenderer.getSeriesRenderers().length; i++) {
            mRenderer.removeSeriesRenderer(mRenderer.getSeriesRendererAt(i));
        }
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            dataset.removeSeries(i);

        }
        if (mChartView != null) {
            mChartView.repaint();
        }
    }

    /**
     * Update the graph on the first view depending on requested Graph
     */
    private void updateTheGraph() {
        switch (currentGraph) {
            case 1:
                mRenderer.setXAxisMin(mLastLocationMillis - 30000);
                mRenderer.setXAxisMax(mLastLocationMillis);
                mRenderer.setYAxisMin(0);
                mRenderer.setYAxisMax(30);
                emptyRenderer();
                mRenderer.addSeriesRenderer(rendererSpeed);
                dataset.addSeries(seriesSpeed);
                ((TextView) findViewById(R.id.graphHeading))
                        .setText("Speed in km/h");
                ((Button) findViewById(R.id.graphButton)).setText("Show: Voltage");
                break;
            case 2:
                mRenderer.setXAxisMin(mLastVoltageMillis - 30000);
                mRenderer.setXAxisMax(mLastVoltageMillis);
                mRenderer.setYAxisMin(seriesVoltage.getMinY() - 250);
                mRenderer.setYAxisMax(seriesVoltage.getMaxY() + 250);
                emptyRenderer();
                mRenderer.addSeriesRenderer(rendererVoltage);
                dataset.addSeries(seriesVoltage);
                ((TextView) findViewById(R.id.graphHeading))
                        .setText("Voltage in mV");
                ((Button) findViewById(R.id.graphButton)).setText("Show: Current");
                break;
            case 3:
                mRenderer.setXAxisMin(mLastCurrentMillis - 30000);
                mRenderer.setXAxisMax(mLastCurrentMillis);
                mRenderer.setYAxisMin(seriesCurrent.getMinY() - 250);
                mRenderer.setYAxisMax(seriesCurrent.getMaxY() + 250);
                emptyRenderer();
                mRenderer.addSeriesRenderer(rendererCurrent);
                dataset.addSeries(seriesCurrent);
                ((TextView) findViewById(R.id.graphHeading))
                        .setText("Current in mA");
                ((Button) findViewById(R.id.graphButton)).setText("Show: Speed");
                break;
            default:
                break;
        }
        if (mChartView != null) {
            mChartView.repaint();
        }
    }

    /**
     * Method for clicking the button responsible for changing the shown graph
     *
     * @param view
     */
    public void graphButtonClick(View view) {
        currentGraph = currentGraph + 1;
        if (currentGraph > 3) {
            currentGraph = 1;
        }
        updateTheGraph();
    }

    // *
    // * General methods
    // *

    /**
     * Rounds a double to the number of digits behind decimal point
     *
     * @param d
     * @param c
     * @return
     */
    public double roundDecimal(double d, int c) {
        int temp = (int) ((d * Math.pow(10, c)));
        return (((double) temp) / Math.pow(10, c));
    }
    int counter;
    /**
     * Updating GUI depending on current shown screens.
     */
    private void updateGUI() {
        if(AppConfig.DEBUG)
        {
            counter++;
            debugTV.setText(String.format("updates{%d}", counter));
        }
        // If the GUI is updated the first time
        if (!firstGUIUpdate) {
            currentGraph = 1;
            firstGUIUpdate = true;
            Display display = getWindowManager().getDefaultDisplay();
            int w = display.getWidth(); // deprecated
            int h = display.getHeight(); // deprecated
            int x = 0;
            if (w < h) {
                x = (int) (w * 0.45);
            } else {
                x = (int) (h * 0.45);
            }
            float textsize = x * 1 / 3;
            TextView tv = ((TextView) findViewById(R.id.SmallTextM));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize / 3);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv
                    .getLayoutParams();
            params.setMargins(0, (int) textsize + 5, 0, 0);
            tv.setLayoutParams(params);
        }
        // Update view zero
        updateTheGraph();

        // Update view one
        if (((TextView) findViewById(R.id.BigTextL)).getVisibility() == View.INVISIBLE) {
            ((TextView) findViewById(R.id.BigTextL))
                    .setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.SmallTextL))
                    .setVisibility(View.VISIBLE);
            rgbButton(((ImageView) findViewById(R.id.ButtonLBG)), 170, 102,
                    204, -1, -1);
        }

        if (((TextView) findViewById(R.id.BigTextR)).getVisibility() == View.INVISIBLE) {
            ((TextView) findViewById(R.id.BigTextR))
                    .setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.SmallTextR))
                    .setVisibility(View.VISIBLE);
            rgbButton(((ImageView) findViewById(R.id.ButtonRBG)), 51, 181, 229,
                    -1, -1);
        }

        (((TextView) findViewById(R.id.BigTextL))).setText(latestCurrent + "");

        switch (latestTimeTilEmpty) {
            case -3:
                (((TextView) findViewById(R.id.BigTextR))).setText("");
                (((TextView) findViewById(R.id.SmallTextR)))
                        .setText("Calculating...");
                ((ImageView) findViewById(R.id.PlugImage))
                        .setVisibility(View.INVISIBLE);
                break;
            case -2:
                (((TextView) findViewById(R.id.BigTextR))).setText("");
                (((TextView) findViewById(R.id.SmallTextR))).setText("Charging");
                ((ImageView) findViewById(R.id.PlugImage)).setImageResource(R.drawable.plug);
                ((ImageView) findViewById(R.id.PlugImage))
                        .setVisibility(View.VISIBLE);
                break;
            case -1:
                (((TextView) findViewById(R.id.BigTextR))).setText("");
                (((TextView) findViewById(R.id.SmallTextR))).setText("Standby");
                ((ImageView) findViewById(R.id.PlugImage)).setImageResource(R.drawable.check);
                ((ImageView) findViewById(R.id.PlugImage))
                        .setVisibility(View.VISIBLE);
                break;
            default:
                (((TextView) findViewById(R.id.BigTextR)))
                        .setText(latestTimeTilEmpty + "");
                (((TextView) findViewById(R.id.SmallTextR))).setText("min");
                ((ImageView) findViewById(R.id.PlugImage))
                        .setVisibility(View.INVISIBLE);
                break;
            //
        }

        // Shows speed in middle pentagon
        if ((actualspeed > 2 && (SystemClock.elapsedRealtime()
                - mLastLocationMillis < Constants.MAXTIME_BETWEEN_GPS_UPDATE))) {
            rgbButton(((ImageView) findViewById(R.id.ButtonMBG)), 51, 181, 229,
                    -1, -1);
            (((TextView) findViewById(R.id.BigTextM)))
                    .setText(actualspeed + "");
            (((TextView) findViewById(R.id.SmallTextM))).setText("km/h");
            if (switcher) {
                (((TextView) findViewById(R.id.BigTextR)))
                        .setText(latestTimeTilEmpty + "");
                (((TextView) findViewById(R.id.SmallTextR))).setText("min");
                ((ImageView) findViewById(R.id.PlugImage))
                        .setVisibility(View.INVISIBLE);
            } else {
                (((TextView) findViewById(R.id.BigTextR)))
                        .setText(latestScooterBatteryLevel + " %");
                (((TextView) findViewById(R.id.SmallTextR))).setText("Scooter");
                ((ImageView) findViewById(R.id.PlugImage))
                        .setVisibility(View.INVISIBLE);
            }
            updateButtonBackground("scooterBatteryLevel",
                    latestScooterBatteryLevel,
                    ((ImageView) findViewById(R.id.ButtonRBG)));
        } else {
            rgbButton(((ImageView) findViewById(R.id.ButtonRBG)), 51, 181, 229,
                    -1, -1);
            (((TextView) findViewById(R.id.BigTextM)))
                    .setText(latestScooterBatteryLevel + "");
            (((TextView) findViewById(R.id.SmallTextM))).setText("Percent");
            updateButtonBackground("scooterBatteryLevel",
                    latestScooterBatteryLevel,
                    ((ImageView) findViewById(R.id.ButtonMBG)));
        }

        // update second view
        String timeTilEmpty;
        switch (latestTimeTilEmpty) {
            case -3:
                timeTilEmpty = "Calculating...";
                break;
            case -2:
                timeTilEmpty = "Charging";
                break;
            case -1:
                timeTilEmpty = "Standby";
                break;
            default:
                timeTilEmpty = latestTimeTilEmpty + " min";
                break;
        }

        listViewArrayList.removeAll(listViewArrayList);
        listViewArrayList.add("cBattery Info");
        listViewArrayList.add("C_%x" + latestScooterBatteryLevel
                + " %qBattery Capacity");
        listViewArrayList.add("t_0x" + timeTilEmpty + " qTime to Empty");
        listViewArrayList.add("I_.x" + latestCurrent + " mAqCurrent");
        listViewArrayList.add("U_.x" + latestVoltage + " mVqVoltage");
        listViewArrayList.add("cTravel Info");
        listViewArrayList.add("v_.x" + roundDecimal(actualspeed, 1)
                + " km/hqActual Speed");
        listViewArrayList.add("v_Øx" + roundDecimal(averagespeed, 1)
                + " km/hqAverage Speed");
        listViewAdapter.notifyDataSetChanged();
    }

    // *
    // * Runnables
    // *

    /**
     * Runnable that calls itself every 500ms <br>
     * If Constants.MINTIME_BETWEEN_SEND ms between the last send and now and
     * Constants.MINTIME_BETWEEN_GUI_UPDATE ms between the last gui update and
     * now have passed it will send an S to the Battery
     */
    private Runnable sSender = new Runnable() {
        @Override
        public void run() {

            if (((SystemClock.elapsedRealtime() - lastSend) > Constants.MINTIME_BETWEEN_SEND)
                    && (SystemClock.elapsedRealtime() - lastGUIUpdate) > Constants.MINTIME_BETWEEN_GUI_UPDATE) {
                sendMessage();

            }
            dDelayer.postDelayed(this, 500);

        }
    };

    /**
     * Runnable that updates the phone battery level and the date on view one
     * every 20 seconds
     */
    private Runnable uUpdater = new Runnable() {
        @Override
        public void run() {

            if (currentPage == 1) {
                String date = DateFormat.getDateInstance().format(new Date());
                ((TextView) findViewById(R.id.date)).setText(date);
                ((TextView) findViewById(R.id.BigTextO))
                        .setText(phoneBatteryLevel + " %");
                updateButtonBackground("phoneBatteryLevel", phoneBatteryLevel,
                        ((ImageView) findViewById(R.id.ButtonOBG)));
            }
            if(noGPS){
                dDelayer.removeCallbacks(this);
                dDelayer.postDelayed(this, 60000);
            }else{
                if (SystemClock.elapsedRealtime() - mLastLocationMillis < Constants.MAXTIME_BETWEEN_GPS_UPDATE) {
                    ((ImageView) findViewById(R.id.Satellite))
                            .setVisibility(View.VISIBLE);
                    rgbButton(((ImageView) findViewById(R.id.Satellite)), 153, 204,
                            0, -1, -1);
                    if (latestTimeTilEmpty > 0 && !switcher) {
                        switcher = true;
                    } else {
                        switcher = false;
                    }
                    dDelayer.removeCallbacks(this);
                    dDelayer.postDelayed(this, 3000);
                } else {
                    if (((ImageView) findViewById(R.id.Satellite)).getVisibility() == View.INVISIBLE) {
                        ((ImageView) findViewById(R.id.Satellite))
                                .setVisibility(View.VISIBLE);
                        rgbButton(((ImageView) findViewById(R.id.Satellite)), 255,
                                187, 81, -1, -1);
                    } else {
                        ((ImageView) findViewById(R.id.Satellite))
                                .setVisibility(View.INVISIBLE);
                    }
                    dDelayer.removeCallbacks(this);
                    dDelayer.postDelayed(this, 1500);
                }}

        }
    };

    // *
    // * Methods connected to the pentagon view
    // *

    /**
     * If Clicking "SPEED" pentagon or current pentagon on the view one, you
     * will jump to the corresponding graph on view zero
     *
     * @param v
     */
    public void switchToGraph(View v) {
        int id = ((View) v.getParent()).getId();
        if (id == R.id.ButtonM
                && (SystemClock.elapsedRealtime() - mLastLocationMillis < 4000)
                && actualspeed > 2) {
            currentGraph = 1;
        } else if (id == R.id.ButtonL & latestCurrent != 0) {
            currentGraph = 3;
        } else {
            return;
        }
        myPager.setCurrentItem(0, true);
        updateTheGraph();
    }

    /**
     * Updates the background of the pentagon showing the phone battery level or
     * the scooter battery level. Also sets the big battery picture
     * corresponding to the latest scooter battery level
     *
     * @param string
     * @param batteryLevel
     * @param view
     */
    private void updateButtonBackground(String string, int batteryLevel,
                                        ImageView view) {

        if (string.equals(SCOOTER_BATTERY_LEVEL)) {
            int res = UNDEFINED;
            if (batteryLevel > 93) {
                res = R.drawable.liq15;
            } else if (batteryLevel > 87) {
                res = R.drawable.liq14;
            } else if (batteryLevel > 80) {
                res = R.drawable.liq13;
            } else if (batteryLevel > 73) {
                res = R.drawable.liq12;
            } else if (batteryLevel > 67) {
                res = R.drawable.liq11;
            } else if (batteryLevel > 60) {
                res = R.drawable.liq10;
            } else if (batteryLevel > 53) {
                res = R.drawable.liq9;
            } else if (batteryLevel > 47) {
                res = R.drawable.liq8;
            } else if (batteryLevel > 40) {
                res = R.drawable.liq7;
            } else if (batteryLevel > 33) {
                res = R.drawable.liq6;
            } else if (batteryLevel > 27) {
                res = R.drawable.liq5;
            } else if (batteryLevel > 20) {
                res = R.drawable.liq4;
            } else if (batteryLevel > 13) {
                res = R.drawable.liq3;
            } else if (batteryLevel > 7) {
                res = R.drawable.liq2;
            } else if (batteryLevel > 0 || batteryLevel <= 0) {
                res = R.drawable.liq1;
            }
            if(res != UNDEFINED)
            {
                lightBatPic.setImageResource(res);
            }
        }else if (string != "phoneBatteryLevel") {
            return;
        }

        int r = 0;
        int g = 0;
        int b = 0;
        if (batteryLevel >= 100) {
            r = 153;
            g = 204;
            b = 0;
        } else if (batteryLevel > 40) {
            r = (int) (255 + (batteryLevel - 40) * -1.7);
            g = (int) (187 + (batteryLevel - 40) * +0.283);
            b = (int) (51 + (batteryLevel - 40) * -0.85);
        } else if (batteryLevel == 40) {
            r = 255;
            g = 187;
            b = 81;
        } else if (batteryLevel > 0) {
            r = 255;
            g = (int) (68 + batteryLevel * +2.975);
            b = (int) (68 + batteryLevel * -0.425);
        } else if (batteryLevel <= 0) {
            r = 255;
            g = 68;
            b = 68;
        }
        rgbButton(view, r, g, b, -1, -1);
    }

    /**
     * Colors an iv with the givn red, green, blue and transparent values
     *
     * @param iv
     * @param r
     * @param g
     * @param b
     * @param t
     */
    public void rgbButton(ImageView iv, int r, int g, int b, int t, int maxDim) {
        if (iv != null) {
            if (rgbs.update(iv.getId(), r, g, b, t, maxDim)) {
                if (t == -1) {
                    t = 255;
                }
                int resId;
                if (iv.getId() == R.id.ButtonMBG) {
                    resId = R.drawable.middle;
                } else if (iv.getId() == R.id.Satellite) {
                    resId = R.drawable.satellite;
                } else {
                    resId = R.drawable.other;
                }
                maxDim = rgbs.getMaxDim(iv.getId());
                int inSample = 1;
                BitmapFactory.Options opts = new BitmapFactory.Options();
                if (maxDim != -1) {
                    opts.inJustDecodeBounds = true; // Only get the bitmap size,
                    // not the bitmap itself
                    BitmapFactory.decodeResource(getResources(), resId, opts);
                    int w = opts.outHeight, h = opts.outHeight;
                    int maxSize = (w > h) ? w : h; // Get the bigger dimension
                    inSample = maxSize / maxDim;

                }
                opts = new BitmapFactory.Options();
                opts.inSampleSize = inSample;
                Bitmap immutableBitmap = BitmapFactory.decodeResource(
                        getResources(), resId, opts);
                Bitmap mutableBitmap = immutableBitmap.copy(
                        Bitmap.Config.ARGB_8888, true);
                immutableBitmap.recycle();
                immutableBitmap = null;
                Drawable d1 = new BitmapDrawable(mutableBitmap);
                d1.setColorFilter(Color.argb(t, r, g, b), Mode.MULTIPLY);
                iv.setImageDrawable(d1);
            }
        }
    }

    /**
     * Do some adjustments on the pentagon view (scaling, etc.)
     */
    private void drawNice() {
        ArrayList<RelativeLayout> myRel = new ArrayList<RelativeLayout>();
        myRel.add((RelativeLayout) findViewById(R.id.ButtonM));
        myRel.add((RelativeLayout) findViewById(R.id.ButtonRU));
        myRel.add((RelativeLayout) findViewById(R.id.ButtonLU));
        myRel.add((RelativeLayout) findViewById(R.id.ButtonL));
        myRel.add((RelativeLayout) findViewById(R.id.ButtonO));
        myRel.add((RelativeLayout) findViewById(R.id.ButtonR));
        myRel.add((RelativeLayout) findViewById(R.id.Background));
        myRel.add((RelativeLayout) findViewById(R.id.Liq));
        ArrayList<ImageView> myImg = new ArrayList<ImageView>();
        myImg.add((ImageView) findViewById(R.id.ButtonMBG));
        myImg.add((ImageView) findViewById(R.id.ButtonRUBG));
        myImg.add((ImageView) findViewById(R.id.ButtonLUBG));
        myImg.add((ImageView) findViewById(R.id.ButtonLBG));
        myImg.add((ImageView) findViewById(R.id.ButtonOBG));
        myImg.add((ImageView) findViewById(R.id.ButtonRBG));
        Display display = getWindowManager().getDefaultDisplay();
        int w = display.getWidth(); // deprecated
        int h = display.getHeight(); // deprecated
        int x = 0;
        if (w < h) {
            x = (int) (w * 0.45);
        } else {
            x = (int) (h * 0.45);
        }
        int l = ((w - x) / 2);
        int t = (int) ((h - x) * 0.4);

        for (int i = 0; i < myRel.size(); i++) {
            if (i == 1) {
                x = (x * 2 / 3);
            }
            RelativeLayout panel = myRel.get(i);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(x,
                    x);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            if (i == 0) {
                lp.setMargins(l, t, 0, 0);
                rgbButton(myImg.get(i), 97, 97, 97, -1, x);
                float textsize = x * 1 / 3;
                TextView tv = ((TextView) findViewById(R.id.BigTextM));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv
                        .getLayoutParams();
                params.setMargins(0, (int) (textsize / 2), 0, 0);
                tv.setLayoutParams(params);
                tv = ((TextView) findViewById(R.id.SmallTextM));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize / 3);
                params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
                params.setMargins(0, (int) textsize - 30, 0, 0);
                tv.setLayoutParams(params);
            } else if (i == 1) {
                lp.setMargins((int) (l + 0.890625 * x), (int) (t + 1.05 * x),
                        0, 0);
                rgbButton(myImg.get(i), 97, 97, 97, -1, x);

            } else if (i == 2) {
                lp.setMargins((int) (l - 0.390625 * x), (int) (t + 1.05 * x),
                        0, 0);
                rgbButton(myImg.get(i), 97, 97, 97, -1, x);

            } else if (i == 3) {
                lp.setMargins((int) (l - 0.8359375 * x), (int) (t - 0.2 * x),
                        0, 0);
                rgbButton(myImg.get(i), 97, 97, 97, 128, x);
                float textsize = (float) (x * 0.24);
                TextView tv = ((TextView) findViewById(R.id.BigTextL));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv
                        .getLayoutParams();
                params.setMargins(0, (int) (textsize * 1.25), 0, 0);
                tv.setLayoutParams(params);
                tv = ((TextView) findViewById(R.id.SmallTextL));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize / 2);
                params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
                params.setMargins(0, (int) textsize + 5, 0, 0);
                tv.setLayoutParams(params);

            } else if (i == 4) {
                lp.setMargins((int) (l + x * 0.25), (t - x), 0, 0);
                rgbButton(myImg.get(i), 97, 97, 97, -1, x);
                float textsize = (float) (x * 0.24);
                TextView tv = ((TextView) findViewById(R.id.BigTextO));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv
                        .getLayoutParams();
                params.setMargins(0, (int) (textsize * 1.25), 0, 0);
                tv.setLayoutParams(params);
                tv = ((TextView) findViewById(R.id.SmallTextO));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize / 2);
                params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
                params.setMargins(0, (int) textsize + 5, 0, 0);
                tv.setLayoutParams(params);

            } else if (i == 5) {
                lp.setMargins((int) (l + 1.3359375 * x), (int) (t - 0.2 * x),
                        0, 0);
                rgbButton(myImg.get(i), 97, 97, 97, 128, x);
                float textsize = (float) (x * 0.24);
                TextView tv = ((TextView) findViewById(R.id.BigTextR));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv
                        .getLayoutParams();
                params.setMargins(0, (int) (textsize * 1.25), 0, 0);
                tv.setLayoutParams(params);
                ImageView iv = ((ImageView) findViewById(R.id.PlugImage));
                params = (RelativeLayout.LayoutParams) iv.getLayoutParams();
                params.setMargins(0, (int) (textsize), 0, 0);
                iv.setLayoutParams(params);
                tv = ((TextView) findViewById(R.id.SmallTextR));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize / 2);
                params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
                params.setMargins(0, (int) textsize + 5, 0, 0);
                tv.setLayoutParams(params);
            } else if (i == 6) {
                lp = new RelativeLayout.LayoutParams(w, h);
                lp.setMargins(0, 0, 0, (int) (h * 0.75));
            } else if (i == 7) {
                lp = new RelativeLayout.LayoutParams(w, h);
                lp.setMargins((int) (w * 0.15), (int) (h * 0.65),
                        (int) (w * 0.15), 0);
            } else {
                lp.setMargins(0, 1, 0, 0);
            }
            panel.setLayoutParams(lp);
        }
        TextView tv = ((TextView) findViewById(R.id.date));
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tv
                .getLayoutParams();
        int size = (int) (tv.getTextSize());
        int m = lp.topMargin;
        lp = new RelativeLayout.LayoutParams((int) (size * 0.75), size);
        lp.setMargins(0, (int) (m + 0.2 * m), 0, 0);
        lp.addRule(RelativeLayout.LEFT_OF, R.id.GPSStuff);
        ImageView iv = ((ImageView) findViewById(R.id.Satellite));
        iv.setLayoutParams(lp);
        lp = new RelativeLayout.LayoutParams((int) (size * 0.75), size);
        lp.setMargins(0, (int) (m + 0.2 * m), 0, 0);
        lp.addRule(RelativeLayout.RIGHT_OF, R.id.GPSStuff);
        iv = ((ImageView) findViewById(R.id.NumSatellites));
        iv.setLayoutParams(lp);
    }

    // *
    // * Classes
    // *

    /**
     * Extending PagerAdapter for working with three views in total
     *
     * @author Lukas Eipert
     *
     */
    private class CustomPagerAdapter extends PagerAdapter {

        /**
         * Count of pages
         */
        @Override
        public int getCount() {
            return 3;
        }

        /**
         * instantiate the different views
         */
        @Override
        public Object instantiateItem(View collection, int position) {

            View v = new View(cxt.getApplicationContext());
            final LayoutInflater inflater = (LayoutInflater) cxt
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (position) {
                case 0:
                    v = inflater.inflate(R.layout.view_graphs, null, false);
                    break;
                case 1:
                    v = inflater.inflate(R.layout.view_pentagons, null, false);
                    lightBatPic = (ImageView) v.findViewById(R.id.liqBatPic);
                    if(AppConfig.DEBUG)
                    {
                        debugTV = (TextView)v.findViewById(R.id.debug);
                        debugTV.setVisibility(View.VISIBLE);
                    }
                    break;

                case 2:
                    v = inflater.inflate(R.layout.view_list, null, false);

                    listView = (ListView) v.findViewById(R.id.listView);
                    listView.setClickable(true);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        /**
                         * Registering switch to the graphs if clicking on a item in
                         * the list
                         */
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                                int position, long arg3) {
                            if ((position == 6 || position == 7)
                                    && (SystemClock.elapsedRealtime()
                                    - mLastLocationMillis < 4000)
                                    && actualspeed > 2) {
                                currentGraph = 1;
                            } else if (position == 3 && latestCurrent != 0) {
                                currentGraph = 3;
                            } else if (position == 4 && latestVoltage != 0) {
                                currentGraph = 2;
                            } else {
                                return;
                            }
                            myPager.setCurrentItem(0, true);
                            updateTheGraph();
                        }
                    });
                    listViewArrayList = new ArrayList<String>();
                    listViewArrayList.add("cBattery Info");
                    listViewArrayList.add("C_%xNot connectedqBattery Capacity");
                    listViewArrayList.add("t_0xNot connectedqTime to Empty");
                    listViewArrayList.add("I_.xNot connectedqCurrent");
                    listViewArrayList.add("U_.xNot connectedqVoltage");
                    listViewArrayList.add("cTravel Info");
                    listViewArrayList.add("v_.x0 km/hqActual Speed");
                    listViewArrayList.add("v_Øx0 km/hqAverage Speed");

                    listViewAdapter = new MySimpleArrayAdapter(v.getContext(),
                            listViewArrayList);

                    // Assign adapter to ListView
                    listView.setAdapter(listViewAdapter);

                    break;

                default:

                    break;
            }

            ((ViewPager) collection).addView(v, 0);
            if (!once && position == 1) {
                drawNice();
                once = true;
                dDelayer.removeCallbacks(uUpdater);
                dDelayer.postDelayed(uUpdater, 50);
            }
            return v;
        }

        private boolean once = false;

        /*
         * (non-Javadoc)
         *
         * @see
         * android.support.v4.view.PagerAdapter#destroyItem(android.view.View,
         * int, java.lang.Object)
         */
        @Override
        public void destroyItem(View collection, int position, Object view) {
            ((ViewPager) collection).removeView(v);

        }

        /*
         * (non-Javadoc)
         *
         * @see
         * android.support.v4.view.PagerAdapter#isViewFromObject(android.view
         * .View, java.lang.Object)
         */
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * android.support.v4.view.PagerAdapter#finishUpdate(android.view.View)
         */
        @Override
        public void finishUpdate(View arg0) {
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * android.support.v4.view.PagerAdapter#restoreState(android.os.Parcelable
         * , java.lang.ClassLoader)
         */
        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        /*
         * (non-Javadoc)
         *
         * @see android.support.v4.view.PagerAdapter#saveState()
         */
        @Override
        public Parcelable saveState() {
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * android.support.v4.view.PagerAdapter#startUpdate(android.view.View)
         */
        @Override
        public void startUpdate(View arg0) {
        }

    }

    /**
     * Extension of the PageChangeListener
     *
     * @author Lukas Eipert
     *
     */
    private class PageChangeListener extends SimpleOnPageChangeListener {
        /*
         * (non-Javadoc)
         *
         * @see android.support.v4.view.ViewPager.SimpleOnPageChangeListener#
         * onPageSelected(int)
         */
        @Override
        public void onPageSelected(int position) {
            currentPage = position;
            if (currentPage == 0) {
                if (mChartView == null) {
                    rendererCurrent.setColor(Color.RED);
                    rendererCurrent.setPointStyle(PointStyle.SQUARE);
                    rendererCurrent.setFillPoints(true);
                    rendererVoltage.setColor(Color.YELLOW);
                    rendererVoltage.setPointStyle(PointStyle.SQUARE);
                    rendererVoltage.setFillPoints(true);
                    rendererSpeed.setColor(Color.GREEN);
                    rendererSpeed.setPointStyle(PointStyle.SQUARE);
                    rendererSpeed.setFillPoints(true);
                    LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
                    mRenderer.setShowGridX(true);
                    mRenderer.setShowGridY(true);
                    mRenderer.setXTitle("Time");
                    mRenderer.setXLabels(0);
                    mChartView = ChartFactory.getLineChartView(cxt, dataset,
                            mRenderer);
                    layout.addView(mChartView, new LayoutParams(
                            LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
                }
            }
        }
    }


}
