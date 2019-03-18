package de.guj.ems.mobile.sdk.util;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.guj.ems.mobile.sdk.R;

/**
 * Various global static methods for initialization, configuration of sdk plus
 * targeting parameters.
 *
 * @author stein16
 */
public class SdkUtil {

    private static int CORRELATOR = 0;

    private volatile static Intent BATTERY_INTENT = null;

    private static Context CONTEXT;

    private static String AUTONATIVE_BASE_URL = null;

    private static Map<String, String[]> DFP_MAP = null;

    private volatile static Intent HEADSET_INTENT = null;

    private volatile static Method KITKAT_JS_METHOD = null;

    private final static Class<?>[] KITKAT_JS_PARAMTYPES = new Class[]{
            String.class, ValueCallback.class};

    private static boolean NPA_STATUS = false;
    private static boolean IS_CHILD = false;

    private static boolean USE_DEBUGGING_AD_SIZE = false;

    /**
     * major sdk version integer
     */
    private final static int MAJOR_VERSION = 2;

    private static DisplayMetrics METRICS = new DisplayMetrics();

    /**
     * minor sdk version integer
     */
    private final static int MINOR_VERSION = 3;

    /**
     * revision sdk version integer
     */
    private final static int REV_VERSION = 3;

    private final static String TAG = "SdkUtil";

    private volatile static TelephonyManager TELEPHONY_MANAGER;

    /**
     * Version string containing major, minor and revision as string divided by
     * underscores for passing it to the adserver
     */
    public final static String VERSION_STR = MAJOR_VERSION + "_"
            + MINOR_VERSION + "_" + REV_VERSION;

    private static WindowManager WINDOW_MANAGER = null;

    public static void setNonPersonalizedAdsStatus(boolean status, boolean isChild) {
        NPA_STATUS = status;
        IS_CHILD = isChild;
    }

    public static boolean getNonPersonalizedAdsStatus() {
        return NPA_STATUS || IS_CHILD;
    }

    public static boolean getIsChildStatus() {
        return IS_CHILD;
    }

    /**
     * Helper method to determine the correct way to execute javascript in a
     * webview. Starting from Android 4.4, the Android webview is a chrome
     * webview and the method to execute javascript has changed from loadUrl to
     * evaluateJavascript
     *
     * @param webView    The webview to exeute the script in
     * @param javascript the actual script
     */
    public static void evaluateJavascript(WebView webView, String javascript) {
        if (KITKAT_JS_METHOD == null && Build.VERSION.SDK_INT >= 19) {
            KITKAT_JS_METHOD = getKitKatJsMethod();
            SdkLog.i(TAG,
                    "G+J EMS SDK AdView: Running in KITKAT mode with new Chromium webview!");

        }

        if (Build.VERSION.SDK_INT < 19) {
            webView.loadUrl("javascript:" + javascript);
        } else
            try {
                KITKAT_JS_METHOD.invoke(webView, javascript, null);
            } catch (Exception e) {
                SdkLog.e(
                        TAG,
                        "FATAL ERROR: Could not invoke Android 4.4 Chromium WebView method evaluateJavascript",
                        e);
            }
    }

    public static void loadInvisibleWebView(WebView webView, String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVisibility(View.INVISIBLE);
        webView.loadUrl(url);
    }

    private synchronized static Intent getBatteryIntent() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        BATTERY_INTENT = getContext().getApplicationContext().registerReceiver(
                null, ifilter);
        return BATTERY_INTENT;
    }


    /**
     * Get the battery charge level in percent
     *
     * @return Integer value [0..100], indicating battery charge level in
     * percent
     */
    public synchronized static int getBatteryLevel() {
        if (BATTERY_INTENT == null) {
            // synchronized (BATTERY_INTENT) {
            try {
                BATTERY_INTENT = getBatteryIntent();
            } catch (ReceiverCallNotAllowedException e) {
                SdkLog.w(TAG,
                        "Skipping start of phone status receivers from start interstitial.");
                BATTERY_INTENT = null;
                return 100;
            }
            // }
        }
        int level = BATTERY_INTENT.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = BATTERY_INTENT.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return (int) (100.0f * (level / (float) scale));
    }

    /**
     * Get local storage path for files
     *
     * @return fodler where local files may be stored
     */
    static File getConfigFileDir() {
        return getContext().getFilesDir();
    }

    /**
     * Get android application context
     *
     * @return context (if set before)
     */
    public final static Context getContext() {
        return CONTEXT;
    }

    /**
     * Get the AutoNative Base URL
     *
     * @return AUTONATIVE_BASE_URL
     */
    public final static String getAutonativeBaseUrl() {
        return AUTONATIVE_BASE_URL;
    }

    /**
     * Get screen density in dots per inch
     *
     * @return screen density in dots per inch
     */
    public static float getDensity() {
        return SdkUtil.getMetrics().density;
    }

    /**
     * Get screen density (hdpi, mdpi, ldpi)
     *
     * @return android screen density
     */
    public static int getDensityDpi() {
        return SdkUtil.getMetrics().densityDpi;
    }


    protected static synchronized Map<String, String[]> getDfpMapping() {
        if (DFP_MAP == null) {
            XmlPullParser parser = getContext().getResources().getXml(R.xml.ems_dfpmapping);
            DFP_MAP = new HashMap<String, String[]>();
            try {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    String name = parser.getName();
                    if (name != null && name.equals("zone")) {
                        DFP_MAP.put(parser.getAttributeValue(null, "name"), new String[]{parser.getAttributeValue(null, "adunit"), parser.getAttributeValue(null, "position"), parser.getAttributeValue(null, "index")});
                    }
                }
            } catch (Exception e) {
                SdkLog.e(TAG, "Error loading DFP mapping.", e);
            }
        }

        return DFP_MAP;
    }

    private synchronized static Intent getHeadsetIntent() {
        HEADSET_INTENT = getContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        return HEADSET_INTENT;
    }

    private synchronized static Method getKitKatJsMethod() {
        try {
            KITKAT_JS_METHOD = Class.forName("android.webkit.WebView")
                    .getDeclaredMethod("evaluateJavascript",
                            KITKAT_JS_PARAMTYPES);
            KITKAT_JS_METHOD.setAccessible(true);
        } catch (Exception e0) {
            SdkLog.e(
                    TAG,
                    "FATAL ERROR: Could not invoke Android 4.4 Chromium WebView method evaluateJavascript",
                    e0);
        }
        return KITKAT_JS_METHOD;
    }

    /**
     * Gets the location.
     *
     * @return the location as an array of doubles
     */
    public static double[] getLocation() {
        Location lastKnown = getLocationObj();
        double[] loc = new double[4];

        if (lastKnown != null) {
            loc[0] = lastKnown.getLatitude();
            loc[1] = lastKnown.getLongitude();
            loc[2] = lastKnown.getSpeed() * 3.6;
            loc[3] = lastKnown.getAltitude();
            SdkLog.i(TAG, "Location [" + lastKnown.getProvider() + "] is "
                    + loc[0] + "x" + loc[1] + "," + loc[2] + "," + loc[3]);
            return loc;
        }

        return null;
    }

    /**
     * Gets the location
     *
     * @return the android location object
     */
    public static Location getLocationObj() {
        LocationManager lm = (LocationManager) getContext().getSystemService(
                Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(false);
        Iterator<String> provider = providers.iterator();
        Location lastKnown = null;

        long age = 0;
        int maxage = getContext().getResources().getInteger(
                R.integer.ems_location_maxage_ms);
        while (provider.hasNext()) {
            try {
                lastKnown = lm.getLastKnownLocation(provider.next());
            } catch (SecurityException e) {
                SdkLog.e(TAG, "could not get lastknownlocation");
            }
            if (lastKnown != null) {

                age = System.currentTimeMillis() - lastKnown.getTime();
                if (age <= maxage) {
                    break;
                } else {
                    SdkLog.d(TAG, "Location [" + lastKnown.getProvider()
                            + "] is " + (age / 60000) + " min old. [max = "
                            + (maxage / 60000) + "]");
                }
            }
        }

        return lastKnown;
    }

    private static DisplayMetrics getMetrics() {
        if (SdkUtil.WINDOW_MANAGER == null) {
            SdkUtil.WINDOW_MANAGER = getWinMgr();
        }
        if (SdkUtil.WINDOW_MANAGER != null) {
            SdkUtil.WINDOW_MANAGER.getDefaultDisplay().getMetrics(SdkUtil.METRICS);
        }
        return METRICS;
    }

    /**
     * Get screen width in pixels
     *
     * @return screen width in pixels
     */
    public static int getScreenHeight() {
        return SdkUtil.getMetrics().heightPixels;
    }

    /**
     * Get screen height in pixels
     *
     * @return screen height in pixels
     */
    public static int getScreenWidth() {
        return SdkUtil.getMetrics().widthPixels;
    }

    private synchronized static TelephonyManager getTelephonyManager() {
        TELEPHONY_MANAGER = (TelephonyManager) SdkUtil.getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        return TELEPHONY_MANAGER;
    }

    private static WindowManager getWinMgr() {
        if (SdkUtil.WINDOW_MANAGER == null) {
            if (SdkUtil.getContext() != null) {
                SdkUtil.WINDOW_MANAGER = (WindowManager) SdkUtil.getContext()
                        .getSystemService(Context.WINDOW_SERVICE);
            } else {
                SdkLog.i(TAG, "Context for SdkUtil not set - pls call SdkUtil.setContext(Context c) as early as possible in your app");
            }
        }
        return WINDOW_MANAGER;
    }

    /**
     * Check whether phone has mobile 3G connection
     *
     * @return
     */
    public static boolean is3G() {
        if (!isWifi()) {
            if (TELEPHONY_MANAGER == null) {
                TELEPHONY_MANAGER = getTelephonyManager();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (TELEPHONY_MANAGER.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                    return false;
                }
            }
            switch (TELEPHONY_MANAGER.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Check whether phone has mobile 4G connection
     *
     * @return
     */
    @SuppressLint("InlinedApi")
    public static boolean is4G() {
        if (!isWifi() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (TELEPHONY_MANAGER == null) {
                TELEPHONY_MANAGER = (TelephonyManager) SdkUtil.getContext()
                        .getSystemService(Context.TELEPHONY_SERVICE);
            }
            return TELEPHONY_MANAGER.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE;
        }
        return false;
    }

    /**
     * Check whether a charger is connected to the device
     *
     * @return true if a charger is connected
     */
    public static boolean isChargerConnected() {
        if (BATTERY_INTENT == null) {
            try {
                BATTERY_INTENT = getBatteryIntent();
            } catch (ReceiverCallNotAllowedException e) {
                SdkLog.w(TAG,
                        "Skipping start of phone status receivers from start interstitial.");
                BATTERY_INTENT = null;
                return false;
            }
        }
        int cp = BATTERY_INTENT.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return cp == BatteryManager.BATTERY_PLUGGED_AC
                // || cp == BatteryManager.BATTERY_PLUGGED_WIRELESS
                || cp == BatteryManager.BATTERY_PLUGGED_USB;
    }

    /**
     * Check wheter GPS is active / allowed
     *
     * @return
     */
    public static boolean isGPSActive() {
        final LocationManager manager = (LocationManager) CONTEXT
                .getSystemService(Context.LOCATION_SERVICE);
        try {
            return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            SdkLog.w(TAG,
                    "Access fine location not allowed by app - assuming no GPS");
            return false;
        }
    }

    /**
     * Check whether a headset is connected to the device
     *
     * @return true if a headset is connected
     */
    public static boolean isHeadsetConnected() {
        try {
            HEADSET_INTENT = getHeadsetIntent();
        } catch (Exception e) {
            SdkLog.e(TAG, "Error getting headset status.", e);
        }
        return HEADSET_INTENT != null ? HEADSET_INTENT.getIntExtra("state", 0) != 0
                : false;
    }

    /**
     * Detect phablets and tablets
     *
     * @return true if we are on device larger than a phone
     */
    public static boolean isLargerThanPhone() {
        return getContext().getResources().getBoolean(R.bool.largeDisplay);
    }

    /**
     * Check whether device is online. if
     * android.Manifest.permission.ACCESS_NETWORK_STATE is not granted or the
     * state cannot be determined, the device will alsways be assumed to be
     * online.
     *
     * @return true if device is connected to any network
     */
    public static boolean isOnline() {

        Context c = SdkUtil.getContext();
        if (c.getPackageManager().checkPermission(
                permission.ACCESS_NETWORK_STATE, c.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            SdkLog.w(TAG,
                    "Access Network State not granted in Manifest - assuming ONLINE.");
            return true;
        }

        final ConnectivityManager conMgr = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            int t = conMgr.getActiveNetworkInfo().getType();
            return t == ConnectivityManager.TYPE_MOBILE ||
                    t == ConnectivityManager.TYPE_WIFI;
        } catch (Exception e) {
            SdkLog.w(TAG, "Exception in getNetworkInfo - assuming ONLINE.");
            return true;
        }
    }

    /**
     * Check whether device is in portait mode
     *
     * @return true if portrait mode, false if landscape mode
     */
    public static boolean isPortrait() {
        int r = getWinMgr().getDefaultDisplay().getRotation();
        return r == Surface.ROTATION_0 || r == Surface.ROTATION_180;
    }

    /**
     * Check whether device is connected via WiFi. if
     * android.Manifest.permission.ACCESS_NETWORK_STATE is not granted or the
     * state cannot be determined, the device will always be assumed to be
     * online via a mobile concection.
     *
     * @return true if device is connected via wifi
     */
    public static boolean isWifi() {

        Context c = SdkUtil.getContext();
        if (c.getPackageManager().checkPermission(
                permission.ACCESS_NETWORK_STATE, c.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            SdkLog.w(TAG,
                    "Access Network State not granted in Manifest - assuming mobile connection.");
            return false;
        }

        final ConnectivityManager conMgr = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            int t = conMgr.getActiveNetworkInfo().getType();
            return t == ConnectivityManager.TYPE_WIFI;
        } catch (Exception e) {
            SdkLog.w(TAG,
                    "Exception in getNetworkInfo - assuming mobile connection.");
            return false;
        }
    }

    public final static String mapToDfp(String zone) {
        Map<String, String[]> dfp = getDfpMapping();
        if (zone != null && dfp.get(zone) != null) {
            String[] xml = dfp.get(zone);
            String adunit = getContext().getResources().getString(R.string.ems_dfpNetwork) + xml[0];
            String pos = xml[1];
            String ind = xml.length > 2 ? dfp.get(zone)[2] : "";
            return adunit + "," + pos + "," + ind;
        } else {
            SdkLog.e(TAG, "There is no mapping defined for zone " + zone + " using a TEST ad unit");
            return getContext().getResources().getString(R.string.ems_dfpNetwork) + "sdktest,1";
        }
    }

    /**
     * Set application context
     *
     * @param c android application context
     */
    public final static void setContext(Context c) {
        CONTEXT = c;
    }

    public final static void setAutonativeBaseUrl(String autonativeBaseUrl) {
        AUTONATIVE_BASE_URL = autonativeBaseUrl;
    }

    public static String getAppPackageName() {
        if (CONTEXT == null) return "not set";
        return CONTEXT.getPackageName();
    }

    public static String getAdUnitId() {
        return "6032";
    }

    public static int getCorrelator() {
        return (int) System.currentTimeMillis();
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

}