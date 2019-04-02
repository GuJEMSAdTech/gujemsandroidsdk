package de.guj.ems.mobile.sdk.controllers.adserver;

import android.content.Context;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Bundle;
import android.util.AttributeSet;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest.Builder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import de.guj.ems.mobile.sdk.R;
import de.guj.ems.mobile.sdk.controllers.IOnAdResizeListener;
import de.guj.ems.mobile.sdk.util.AndroidIdRetriever;
import de.guj.ems.mobile.sdk.util.SdkGlobals;
import de.guj.ems.mobile.sdk.util.SdkLog;
import de.guj.ems.mobile.sdk.util.SdkUtil;
import de.guj.ems.mobile.sdk.util.ThirdPartyConnector;
import de.guj.ems.mobile.sdk.util.YieldLab;

/**
 * New ad view settings adapter connecting the app to
 * Google Doubleclick for Publishers by invoking the
 * Google SDK's Request Builder and defining custom criteria
 *
 * @author stein16
 */
public class DFPSettingsAdapter extends AdServerSettingsAdapter {

    private final static String TAG = "DFPSettingsAdapter";

    private final static char STATUS_3G_ON = '3';

    private final static char STATUS_4G_ON = '4';

    private final static char STATUS_GPS_ON = 'g';

    private final static char STATUS_PORTRAIT_MODE = 'p';

    private final static char STATUS_HEADSET_CONNECTED = 'h';

    private final static char STATUS_CHARGER_CONNECTED = 'c';

    private final static char STATUS_WIFI_ON = 'w';

    private final static char STATUS_LANDSCAPE_MODE = 'l';

    private AndroidIdRetriever androidRetriever = null;

    private String zone;

    private boolean useLocation = false;

    private boolean noRectangle = false;

    private boolean noBillboard = false;

    private boolean noDesktopBillboard = false;

    private boolean noLeaderboard = false;

    private boolean noTwoToOne = false;

    private boolean noThreeToOne = false;

    private boolean allowMobileHalfpage = false;

    public DFPSettingsAdapter() {
        super();
    }

    private Map<String, String> addBatteryStatus(Map<String, String> params) {
        params.put(SdkGlobals.EMS_CV_BATT_LEVEL,
                String.valueOf(SdkUtil.getBatteryLevel()));
        return params;
    }

    private Map<String, String> addPhoneStatus(Map<String, String> params) {

        String pVals = "";
        if (SdkUtil.is3G()) {
            pVals += STATUS_3G_ON + ",";
        }
        if (SdkUtil.is4G()) {
            pVals += STATUS_4G_ON + ",";
        }
        if (SdkUtil.isGPSActive()) {
            pVals += STATUS_GPS_ON + ",";
        }
        if (SdkUtil.isPortrait()) {
            pVals += STATUS_PORTRAIT_MODE + ",";
        } else {
            pVals += STATUS_LANDSCAPE_MODE + ",";
        }
        if (SdkUtil.isHeadsetConnected()) {
            pVals += STATUS_HEADSET_CONNECTED + ",";
        }
        if (SdkUtil.isChargerConnected()) {
            pVals += STATUS_CHARGER_CONNECTED + ",";
        }
        if (SdkUtil.isWifi()) {
            pVals += STATUS_WIFI_ON;
        }
        params.put(SdkGlobals.EMS_CV_PHONE_STAT, pVals);
        return params;
    }

    private Map<String, String> getCustomValues() {
        return addPhoneStatus(addBatteryStatus(getParams()));
    }


    /**
     * Acquire the preinitialized request builder for Google Ads with all custom
     * values that were available. Use builder.build() to finalize
     *
     * @return the Google Publisher Ad Request builder
     */
    public Builder getGoogleRequestBuilder(int pos) {
        return this.getGoogleRequestBuilder(pos, false);
    }

    public Builder getGoogleRequestBuilder(int pos, boolean hasAdUnit) {
        Map<String, String> customValues = getCustomValues();

        Iterator<String> cvI = customValues.keySet().iterator();
        Builder adRequestBuilder = new PublisherAdRequest.Builder();
        if (SdkUtil.getNonPersonalizedAdsStatus()) {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            if (SdkUtil.getIsChildStatus()) {
                extras.putBoolean("tag_for_under_age_of_consent", true);
            }
            adRequestBuilder = adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
        }
        while (cvI.hasNext()) {
            String key = cvI.next();
            String val = customValues.get(key);
            if (!"as".equals(key) && val.indexOf(",") >= 0) {
                adRequestBuilder = adRequestBuilder.addCustomTargeting(key,
                        Arrays.asList(val.split(",")));
            } else {
                adRequestBuilder = adRequestBuilder
                        .addCustomTargeting(key, val);
            }
            SdkLog.d(TAG, hashCode() + " adding custom key value [" + key + ", " + val + "]");
        }

        if (useLocation) {
            try {
                Location locObj = SdkUtil.getLocationObj();
                adRequestBuilder = adRequestBuilder.setLocation(locObj);
                SdkLog.d(TAG, this + " added location [" + locObj + "]");
            } catch (Exception e) {
                SdkLog.e(TAG, hashCode() + " problem setting request location", e);
            }
        }

        if (!hasAdUnit) {
            String adunit = SdkUtil.mapToDfp(this.zone);
            if (adunit != null) {
                String[] adc = adunit.split(",");
                if (pos <= 0 && adc.length > 1) {
                    SdkLog.d(TAG,
                            hashCode() + " adding custom key value [pos, " + adunit.split(",")[1]
                                    + "]");
                    adRequestBuilder = adRequestBuilder.addCustomTargeting("pos",
                            adc[1]);
                } else if (pos > 0) {
                    SdkLog.d(TAG, hashCode() + " adding custom key value [pos, " + pos + "]");
                    adRequestBuilder = adRequestBuilder.addCustomTargeting("pos",
                            String.valueOf(pos));
                } else {
                    SdkLog.w(TAG, "No position value provided. The SDK cannot where in your view the ad view i.!");
                }

                if (adc.length > 2 && adc[2].length() > 1) {
                    SdkLog.d(TAG, hashCode() + " adding custom key value [ind, " + adc[2] + "]");
                    adRequestBuilder = adRequestBuilder.addCustomTargeting("ind",
                            adc[2]);
                }
            }
        }

        if (!this.androidRetriever.getAndroidIdHashed().equals("")) {
            adRequestBuilder = adRequestBuilder.addCustomTargeting("idfa", this.androidRetriever.getAndroidIdHashed());
        }

        ThirdPartyConnector.getInstance().callByType(ThirdPartyConnector.targeting, adRequestBuilder);

        YieldLab.appendToAdCall(adRequestBuilder);

        return adRequestBuilder.addCustomTargeting(SdkGlobals.EMS_CV_SDV_VER,
                SdkUtil.VERSION_STR);
    }

    /**
     * Maps old adserver placement ids to Google compliant adunits
     *
     * @return a comma separated string with the adunit (without network), the ad position and the index value (index page yes/no)
     */
    public String mapToDfpAdUnit() {
        String adunit = SdkUtil.mapToDfp(this.zone);
        SdkLog.i(TAG, hashCode() + " zone " + this.zone + " maps to adUnit " + adunit);
        if (adunit != null && adunit.indexOf(",") >= 0) {
            return adunit.split(",")[0];
        }
        return adunit;
    }

    /**
     * Constructor with all attributes stored in an AttributeSet
     *
     * @param context android application context
     * @param set     attribute set with configuration
     */
    @Override
    public void setup(Context context, AttributeSet set) {
        super.setup(context, set);
        this.getGoogleAdId(context);
        TypedArray tVals = context.obtainStyledAttributes(set,
                R.styleable.GuJEMSAdView);
        if (getAttrsToParams().get(SdkGlobals.EMS_ZONEID) != null) {
            this.zone = tVals.getString(R.styleable.GuJEMSAdView_ems_zoneId);
        }
        if (getAttrsToParams().get(SdkGlobals.EMS_GEO) != null) {
            if (tVals.getBoolean(R.styleable.GuJEMSAdView_ems_geo, false)) {
                double[] loc = SdkUtil.getLocation();
                if (loc != null && 0.0 != loc[0]) {
                    useLocation = true;
                    putAttrToParam(SdkGlobals.EMS_CV_GPS_VELO,
                            SdkGlobals.EMS_CV_GPS_VELO);
                    putAttrValue(SdkGlobals.EMS_CV_GPS_VELO,
                            String.valueOf((int) loc[2]));
                    putAttrToParam(SdkGlobals.EMS_CV_GPS_ALT,
                            SdkGlobals.EMS_CV_GPS_ALT);
                    putAttrValue(SdkGlobals.EMS_CV_GPS_ALT,
                            String.valueOf((int) loc[3]));
                } else {
                    SdkLog.i(TAG, hashCode() + " location too old or not fetchable.");
                }
            } else {
                SdkLog.d(TAG, hashCode() + " location fetching not allowed by adspace.");
            }
        }

        this.noBillboard = getAttrsToParams().get(SdkGlobals.EMS_NO_BILLBOARD) != null
                && tVals.getBoolean(R.styleable.GuJEMSAdView_ems_noBillboard,
                false);
        this.noDesktopBillboard = getAttrsToParams().get(
                SdkGlobals.EMS_NO_DESKTOP_BILLBOARD) != null
                && tVals.getBoolean(
                R.styleable.GuJEMSAdView_ems_noDesktopBillboard, false);
        this.noTwoToOne = getAttrsToParams().get(SdkGlobals.EMS_NO_TWO_TO_ONE) != null
                && tVals.getBoolean(R.styleable.GuJEMSAdView_ems_noTwoToOne,
                false);
        this.noThreeToOne = getAttrsToParams().get(SdkGlobals.EMS_NO_THREE_TO_ONE) != null
                && tVals.getBoolean(R.styleable.GuJEMSAdView_ems_noThreeToOne,
                false);
        this.noLeaderboard = getAttrsToParams().get(
                SdkGlobals.EMS_NO_LEADERBOARD) != null
                && tVals.getBoolean(R.styleable.GuJEMSAdView_ems_noLeaderboard,
                false);
        this.noRectangle = getAttrsToParams().get(SdkGlobals.EMS_NO_RECTANGLE) != null
                && tVals.getBoolean(R.styleable.GuJEMSAdView_ems_noRectangle,
                false);
        this.allowMobileHalfpage = getAttrsToParams().get(SdkGlobals.EMS_ALLOW_MOBILE_HALFPAGE) != null
                && tVals.getBoolean(R.styleable.GuJEMSAdView_ems_allowMobileHalfpage,
                false);

        tVals.recycle();
    }

    /**
     * Constructor with configuration in bundle
     *
     * @param context       android application context
     * @param savedInstance bundle with configuration
     */
    @Override
    public void setup(Context context, Bundle savedInstance) {
        super.setup(context, savedInstance);
        this.getGoogleAdId(context);
        if (getAttrsToParams().get(SdkGlobals.EMS_ZONEID) != null) {
            this.zone = savedInstance.getString(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                    + SdkGlobals.EMS_ZONEID);
        }
        if (getAttrsToParams().get(SdkGlobals.EMS_GEO) != null) {
            if (savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                    + SdkGlobals.EMS_GEO, false)) {
                double[] loc = SdkUtil.getLocation();
                if (loc != null && 0.0 != loc[0]) {

                    putAttrToParam(SdkGlobals.EMS_CV_GPS_VELO,
                            SdkGlobals.EMS_CV_GPS_VELO);
                    putAttrValue(SdkGlobals.EMS_CV_GPS_VELO,
                            String.valueOf((int) loc[2]));
                    putAttrToParam(SdkGlobals.EMS_CV_GPS_ALT,
                            SdkGlobals.EMS_CV_GPS_ALT);
                    putAttrValue(SdkGlobals.EMS_CV_GPS_ALT,
                            String.valueOf((int) loc[3]));
                    SdkLog.i(TAG, hashCode() + " using " + loc[0] + "x" + loc[1]
                            + " as location.");
                } else {
                    SdkLog.i(TAG, hashCode() + " location too old or not fetchable.");
                }
            } else {
                SdkLog.d(TAG, hashCode() + " location fetching not allowed by adspace.");
            }
        }
        this.noBillboard = getAttrsToParams().get(SdkGlobals.EMS_NO_BILLBOARD) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_NO_BILLBOARD, false);
        this.noDesktopBillboard = getAttrsToParams().get(
                SdkGlobals.EMS_NO_DESKTOP_BILLBOARD) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_NO_DESKTOP_BILLBOARD, false);
        this.noTwoToOne = getAttrsToParams().get(SdkGlobals.EMS_NO_TWO_TO_ONE) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_NO_TWO_TO_ONE, false);
        this.noThreeToOne = getAttrsToParams().get(SdkGlobals.EMS_NO_THREE_TO_ONE) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_NO_THREE_TO_ONE, false);
        this.noLeaderboard = getAttrsToParams().get(
                SdkGlobals.EMS_NO_LEADERBOARD) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_NO_LEADERBOARD, false);
        this.noRectangle = getAttrsToParams().get(SdkGlobals.EMS_NO_RECTANGLE) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_NO_RECTANGLE, false);
        this.allowMobileHalfpage = getAttrsToParams().get(SdkGlobals.EMS_ALLOW_MOBILE_HALFPAGE) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_ALLOW_MOBILE_HALFPAGE, false);
    }

    public void setEmsZoneId(String zone) {
        this.zone = zone;
    }

    @Override
    public void setup(Context context, Bundle savedInstance, String[] keywords) {
        super.setup(context, savedInstance);
        this.getGoogleAdId(context);
        if (getAttrsToParams().get(SdkGlobals.EMS_ZONEID) != null) {
            this.zone = savedInstance.getString(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                    + SdkGlobals.EMS_ZONEID);
        }
        if (getAttrsToParams().get(SdkGlobals.EMS_GEO) != null) {
            if (savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                    + SdkGlobals.EMS_GEO, false)) {
                double[] loc = SdkUtil.getLocation();
                if (loc != null && 0.0 != loc[0]) {

                    putAttrToParam(SdkGlobals.EMS_CV_GPS_VELO,
                            SdkGlobals.EMS_CV_GPS_VELO);
                    putAttrValue(SdkGlobals.EMS_CV_GPS_VELO,
                            String.valueOf((int) loc[2]));
                    putAttrToParam(SdkGlobals.EMS_CV_GPS_ALT,
                            SdkGlobals.EMS_CV_GPS_ALT);
                    putAttrValue(SdkGlobals.EMS_CV_GPS_ALT,
                            String.valueOf((int) loc[3]));
                    SdkLog.i(TAG, hashCode() + " using " + loc[0] + "x" + loc[1]
                            + " as location.");
                } else {
                    SdkLog.i(TAG, hashCode() + " location too old or not fetchable.");
                }
            } else {
                SdkLog.d(TAG, hashCode() + " location fetching not allowed by adspace.");
            }
        }

        this.noBillboard = getAttrsToParams().get(SdkGlobals.EMS_NO_BILLBOARD) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_NO_BILLBOARD, false);
        this.noDesktopBillboard = getAttrsToParams().get(
                SdkGlobals.EMS_NO_DESKTOP_BILLBOARD) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_NO_DESKTOP_BILLBOARD, false);
        this.noTwoToOne = getAttrsToParams().get(SdkGlobals.EMS_NO_TWO_TO_ONE) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_NO_TWO_TO_ONE, false);
        this.noThreeToOne = getAttrsToParams().get(SdkGlobals.EMS_NO_THREE_TO_ONE) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_NO_THREE_TO_ONE, false);
        this.noLeaderboard = getAttrsToParams().get(
                SdkGlobals.EMS_NO_LEADERBOARD) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_NO_LEADERBOARD, false);
        this.noRectangle = getAttrsToParams().get(SdkGlobals.EMS_NO_RECTANGLE) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_NO_RECTANGLE, false);
        this.allowMobileHalfpage = getAttrsToParams().get(SdkGlobals.EMS_ALLOW_MOBILE_HALFPAGE) != null
                && savedInstance.getBoolean(SdkGlobals.EMS_ATTRIBUTE_PREFIX
                + SdkGlobals.EMS_ALLOW_MOBILE_HALFPAGE, false);
    }

    /**
     * Check whether 300x250 ads are blocked for the view with these settings
     *
     * @return true if 300x250 ads are blocked
     */
    public boolean isNoRectangle() {
        return noRectangle;
    }

    /**
     * Set whether 300x250 ads are blocked for the view with these settings
     *
     * @param noRectangle set to true if 300x250 ads are blocked
     */
    public void setNoRectangle(boolean noRectangle) {
        this.noRectangle = noRectangle;
    }

    /**
     * Check whether 1024x220 (landscape) and 768x300 (portrait) ads are blocked for the view with these settings
     *
     * @return true if 1024x220 (landscape) and 768x300 (portrait) ads are blocked
     */
    public boolean isNoBillboard() {
        return noBillboard;
    }

    /**
     * Set whether 1024x220 (landscape) and 768x300 (portrait)ads are blocked for the view with these settings
     *
     * @param noBillboard set true if 1024x220 (landscape) and 768x300 (portrait) ads are blocked
     */
    public void setNoBillboard(boolean noBillboard) {
        this.noBillboard = noBillboard;
    }

    /**
     * Check whether 800x250 ads are blocked for the view with these settings
     *
     * @return true if 800x250 ads are blocked
     */
    public boolean isNoDesktopBillboard() {
        return noDesktopBillboard;
    }

    /**
     * Set whether 800x250 ads are blocked for the view with these settings
     *
     * @param noDesktopBillboard set true if 800x250 ads are blocked
     */
    public void setNoDesktopBillboard(boolean noDesktopBillboard) {
        this.noDesktopBillboard = noDesktopBillboard;
    }

    /**
     * Check whether 728x90 and 768x90 ads are blocked for the view with these settings
     *
     * @return true if 728x90 and 768x90 ads are blocked
     */
    public boolean isNoLeaderboard() {
        return noLeaderboard;
    }

    /**
     * Set whether 728x90 and 768x90  ads are blocked for the view with these settings
     *
     * @param noLeaderboard set true if 728x90 and 768x90  ads are blocked
     */
    public void setNoLeaderboard(boolean noLeaderboard) {
        this.noLeaderboard = noLeaderboard;
    }

    /**
     * Check whether 300x150 ads are blocked for the view with these settings
     *
     * @return true if 300x150 ads are blocked
     */
    public boolean isNoTwoToOne() {
        return noTwoToOne;
    }

    /**
     * Set whether 300x150 ads are blocked for the view with these settings
     *
     * @param noTwoToOne set true if 300x150  ads are blocked
     */
    public void setNoTwoToOne(boolean noTwoToOne) {
        this.noTwoToOne = noTwoToOne;
    }

    /**
     * Check whether 320x100, 300x100 ads are blocked for the view with these settings
     *
     * @return true if 320x100, 300x100 ads are blocked
     */
    public boolean isNoThreeToOne() {
        return noThreeToOne;
    }

    /**
     * is Mobile HPA allowed?
     * @return true / false
     */
    public boolean isAllowMobileHalfpage() {
        return allowMobileHalfpage;
    }

    /**
     * set status for allowMobileHPA
     * @param allowMobileHalfpage true if allowed
     */
    public void setAllowMobileHalfpage(boolean allowMobileHalfpage) {
        this.allowMobileHalfpage = allowMobileHalfpage;
    }

    /**
     * Set whether 320x100, 300x100 ads are blocked for the view with these settings
     *
     * @param noThreeToOne set true if 320x100, 300x100  ads are blocked
     */
    public void setNoThreeToOne(boolean noThreeToOne) {
        this.noThreeToOne = noThreeToOne;
    }

    private void getGoogleAdId(Context context) {
        this.androidRetriever = AndroidIdRetriever.getInstance();
        this.androidRetriever.execute();
    }

}