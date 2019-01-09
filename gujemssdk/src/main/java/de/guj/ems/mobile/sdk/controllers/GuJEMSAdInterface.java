package de.guj.ems.mobile.sdk.controllers;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;

import com.google.android.gms.ads.doubleclick.PublisherAdView;

import org.json.JSONException;
import org.json.JSONObject;

import de.guj.ems.mobile.sdk.aqt.AqtRequestCollector;
import de.guj.ems.mobile.sdk.util.SdkLog;
import de.guj.ems.mobile.sdk.util.SdkUtil;
import de.guj.ems.mobile.sdk.util.ThirdPartyConnector;
import de.guj.ems.mobile.sdk.views.GuJEMSAdView;

/**
 * Add e|MS specific functionality by providing an AppEventListener interface
 *
 * @author stein16
 */
public class GuJEMSAdInterface {

    public static GuJEMSAdInterface getInstance() {
        if (GuJEMSAdInterface.instance == null) {
            GuJEMSAdInterface.instance = new GuJEMSAdInterface();
        }
        return GuJEMSAdInterface.instance;
    }

    private static GuJEMSAdInterface instance = null;

    private final static String TAG = "GuJEMSAdInterface";

    /**
     * Makes the phone vibrate once for l milliseconds. If the vibrate
     * permission is not granted in the AndroidManifest.xml, an exception is
     * thrown and caught.
     *
     * @param l length of vibration
     */
    @JavascriptInterface
    public void vibrateOnce(long l) {
        SdkLog.i(TAG, "ems_vibrate: " + l + " ms");
        try {
            Context c = SdkUtil.getContext();
            if (c.getPackageManager().checkPermission(permission.VIBRATE,
                    c.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                throw new SecurityException(
                        "Vibrate Permission not granted in Manifest");
            }
            Vibrator v = (Vibrator) c
                    .getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(l);
        } catch (Exception e) {
            SdkLog.e(TAG, "Vibration not possible in this app.", e);
        }
    }

    /**
     * Makes the phone vibrate as in the pattern given in l. If the vibrate
     * permission is not granted in the AndroidManifest.xml, an exception is
     * thrown and caught. The pattern starts with a length of x milliseconds for
     * an initial pause. Each length is followed by another value indicating the
     * pause until the next vibration. To vibrate twice for 100ms initially with
     * a pause of 200ms between the vibration tones, the pattern would thus be
     * [0,100,200,100].
     *
     * @param l pattern of vibration
     */
    @JavascriptInterface
    public void vibratePattern(long[] l) {
        SdkLog.i(TAG, "ems_vibrate: pattern called.");
        try {
            Context c = SdkUtil.getContext();
            if (c.getPackageManager().checkPermission(permission.VIBRATE,
                    c.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                throw new SecurityException(
                        "Vibrate Permission not granted in Manifest");
            }
            Vibrator v = (Vibrator) c
                    .getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(l, -1);
        } catch (Exception e) {
            SdkLog.e(TAG, "Vibration not possible in this app.", e);
        }
    }

    @JavascriptInterface
    public void hideAdView(PublisherAdView view) {
        SdkLog.i(TAG, "ems_hideadview called.");
        view.setVisibility(View.GONE);
    }

    public void doAppEvent(GuJEMSAdView gujView, PublisherAdView adView, String name, String info) {
        SdkLog.i(TAG, "doAppEvent from ad received: " + name + " ; " + info);
        switch (name.toLowerCase()) {
            case "vibratepattern":
                try {
                    String[] vals = info.split(",");
                    long[] l = new long[vals.length];
                    for (int i = 0; i < vals.length; i++) {
                        l[i] = Long.parseLong(vals[i]);
                    }
                    vibratePattern(l);
                } catch (Exception e) {
                    SdkLog.e(TAG, "Error parsing length value for vibration", e);
                }
                break;
            case "vibrateonce":
                try {
                    long l = Long.parseLong(info);
                    vibrateOnce(l);
                } catch (Exception e) {
                    SdkLog.e(TAG, "Error parsing length value for vibration", e);
                }
                break;
            case "hideadview":
            case "noad":
                hideAdView(adView);
                break;
            case "handoveradviewtofacebook":
                hideAdView(adView);
                ThirdPartyConnector.getInstance().callByType(ThirdPartyConnector.facebook, adView, info);
                break;
            case "setsize":
                setAdSize(info, adView, gujView);
                break;
            case "aqt":
                try {
                    JSONObject json = new JSONObject(info);
                    AqtRequestCollector.getInstance().addRequest(
                            json.getString("creativeId"),
                            adView.getAdUnitId(),
                            SdkUtil.getAppPackageName(),
                            json.getString("code")
                    );
                } catch (JSONException e) {
                    SdkLog.e(TAG, "Error parsing json from aqt event", e);
                }
                break;
            case "log":
                SdkLog.i(TAG, "Custom log message from ad received:\n" + info);
                break;
            case "creativeId":
                SdkLog.i(TAG, "AdRequest Info creativeId: " + info);
                break;
            case "lineItemId":
                SdkLog.i(TAG, "AdRequest Info lineItemId: " + info);
                break;
            case "slot":
                SdkLog.i(TAG, "AdRequest Info slot: " + info);
                break;
            case "adunit":
                SdkLog.i(TAG, "AdRequest Info adunit: " + info);
                break;
            case "requestedAdSizes":
                SdkLog.i(TAG, "AdRequest Info requestedAdSizes: " + info);
                break;
            default:
        }
    }

    private void setLayoutSize(int width, int height, PublisherAdView adView) {
        ViewGroup.LayoutParams lp = adView.getLayoutParams();
        lp.width = width;
        lp.height = height;
        adView.setLayoutParams(lp);
    }

    /**
     * Parses desired width and height for adview from received app event data
     * changes size of adview instantly or over time, if animation parameters were send
     *
     * @param data   received app event data. Example format: width:height:[optional]durationOfAnimation:[optional]stepsOfAnimation
     * @param adView container which
     */
    @JavascriptInterface
    public void setAdSize(String data, final PublisherAdView adView, final GuJEMSAdView gujView) {
        SdkLog.i(TAG, "set ad size");
        String[] dimensions = data.split(":");
        int width = 0;
        int height = 0;
        DisplayMetrics metrics = adView.getContext().getResources().getDisplayMetrics();
        try {
            if ("max".equals(dimensions[0])) {
                //set to max width
                width = metrics.widthPixels;
            } else {
                width = Integer.parseInt(dimensions[0]);
            }
            if ("max".equals(dimensions[1])) {
                //set to max height
                height = metrics.heightPixels;
            } else {
                height = Integer.parseInt(dimensions[1]);
            }
            if (dimensions.length == 2) {
                /**
                 * request received in the form of:
                 *  ("setsize","width:height")
                 *  Example:
                 *  ("setsize","320:240")
                 *  only set ad size
                 */
                setLayoutSize(width, height, adView);
                this.callAdResizeListener(gujView, width, height);
            } else if (dimensions.length == 4) {
                /**
                 * request received in the form of:
                 *  ("setsize","width:height:durationOfAnimation:stepsOfAnimation")
                 *  Example:
                 *  ("setsize","320:240:800:10")
                 *  set ad size over time
                 */
                final double oldWidth = (int) (adView.getWidth() / metrics.density);
                final double oldHeight = (int) (adView.getHeight() / metrics.density);
                final double targetWidth = width;
                final double targetHeight = height;
                final double duration = Integer.parseInt(dimensions[2]);
                final double steps;
                if (Integer.parseInt(dimensions[3]) > 0) {
                    steps = Integer.parseInt(dimensions[3]);
                } else {
                    steps = 1;
                }
                final double w = width;
                final double h = height;
                for (int i = 1; i <= steps; i++) {
                    final int step = i;
                    final int curi = i;
                    adView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setLayoutSize((int) (oldWidth + ((targetWidth - oldWidth) / steps) * step), (int) (oldHeight + ((targetHeight - oldHeight) / steps) * step), adView);
                            if(curi == steps) {
                                callAdResizeListener(gujView, w, h);
                            }
                        }
                    }, (long) (duration / steps) * step);
                }
            }
        } catch (NumberFormatException e) {
            SdkLog.e(TAG, "Error parsing numbers for setSize", e);
        }
    }

    private void callAdResizeListener(GuJEMSAdView gujView, double width, double height) {
        if(gujView != null) {
            if(gujView.getOnAdResizeListener() != null) {
                gujView.getOnAdResizeListener().onResize(width, height);
            }
        }
    }

}
