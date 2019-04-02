package de.guj.ems.mobile.sdk.util;

import de.guj.ems.mobile.sdk.R;

public class SdkGlobals {

    /**
     * xml layout attributes prefix
     */
    public final static String EMS_ATTRIBUTE_PREFIX = SdkUtil.getContext()
            .getString(R.string.ems_attributePrefix);

    /**
     * xml layout attributes prefix for listeners
     */
    public final static String EMS_LISTENER_PREFIX = EMS_ATTRIBUTE_PREFIX
            + "onAd";

    /**
     * Global attribute name for listener which reacts to empty ad
     */
    public final static String EMS_ERROR_LISTENER = SdkUtil.getContext()
            .getString(R.string.ems_onAdError);

    /**
     * Global attribute name for listener which reacts to empty ad
     */
    public final static String EMS_EMPTY_LISTENER = SdkUtil.getContext()
            .getString(R.string.ems_onAdEmpty);

    /**
     * Global attribute name for allowing geo localization for a placement
     */
    public final static String EMS_GEO = SdkUtil.getContext().getString(
            R.string.ems_geo);

    /**
     * Global attribute name for identifying keywords add to the request
     */
    public final static String EMS_KEYWORDS = SdkUtil.getContext().getString(
            R.string.ems_keyword);

    /**
     * Global attribute name for the geographical latitude
     */
    public final static String EMS_LAT = SdkUtil.getContext().getString(
            R.string.ems_latitude);
    /**
     * Global attribute name for the geographical longitude
     */
    public final static String EMS_LON = SdkUtil.getContext().getString(
            R.string.ems_longitude);

    /**
     * xml layout attribute for success listener
     */
    public final static String EMS_SUCCESS_LISTENER = SdkUtil.getContext()
            .getString(R.string.ems_onAdSuccess);

    /**
     * Global attribute name for identifying a placement
     */
    public final static String EMS_ZONEID = SdkUtil.getContext().getString(
            R.string.ems_zoneId);

    /**
     * Global attribute name for identifying a DFP ad unit
     */
    public final static String EMS_ADUNIT = SdkUtil.getContext().getString(
            R.string.ems_adUnit);

    /**
     * Global attribute name for battery level custom value
     */
    public final static String EMS_CV_BATT_LEVEL = SdkUtil.getContext().getString(R.string.ems_pBatteryLevel);

    /**
     * Global attribute name for phone status custom value
     */
    public final static String EMS_CV_PHONE_STAT = SdkUtil.getContext().getString(R.string.ems_pStatusParam);

    /**
     * Global attribute name for sdv version custom value
     */
    public final static String EMS_CV_SDV_VER = SdkUtil.getContext().getString(R.string.ems_pSdkVersion);

    /**
     * Global attribute name for gps velocity custom value
     */
    public final static String EMS_CV_GPS_VELO = SdkUtil.getContext().getString(R.string.ems_pGpsVelocity);

    /**
     * Global attribute name for gps velocity custom value
     */
    public final static String EMS_CV_GPS_ALT = SdkUtil.getContext().getString(R.string.ems_pGpsAltitude);

    /**
     * Global attribute name for disabling rectangles
     */
    public final static String EMS_NO_RECTANGLE = SdkUtil.getContext().getString(R.string.ems_noRectangle);

    /**
     * Global attribute name for disabling billboards
     */
    public final static String EMS_NO_BILLBOARD = SdkUtil.getContext().getString(R.string.ems_noBillboard);

    /**
     * Global attribute name for disabling desktop billboards
     */
    public final static String EMS_NO_DESKTOP_BILLBOARD = SdkUtil.getContext().getString(R.string.ems_noDesktopBillboard);

    /**
     * Global attribute name for disabling 2:1
     */
    public final static String EMS_NO_TWO_TO_ONE = SdkUtil.getContext().getString(R.string.ems_noTwoToOne);

    /**
     * Global attribute name for enable mobile halfpage
     */
    public final static String EMS_ALLOW_MOBILE_HALFPAGE = SdkUtil.getContext().getString(R.string.ems_allowMobileHalfpage);

    /**
     * Global attribute name for disabling 3:1
     */
    public final static String EMS_NO_THREE_TO_ONE = SdkUtil.getContext().getString(R.string.ems_noThreeToOne);

    /**
     * Global attribute name for disabling leaderboards
     */
    public final static String EMS_NO_LEADERBOARD = SdkUtil.getContext().getString(R.string.ems_noLeaderboard);
}
