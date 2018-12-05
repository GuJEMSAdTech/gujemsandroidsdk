package de.guj.ems.mobile.sdk.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest.Builder;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.iab.omid.library.emsgujde.adsession.AdSession;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import de.guj.ems.mobile.sdk.R;
import de.guj.ems.mobile.sdk.controllers.GuJEMSAdInterface;
import de.guj.ems.mobile.sdk.controllers.GuJEMSAdListener;
import de.guj.ems.mobile.sdk.controllers.IOnAdEmptyListener;
import de.guj.ems.mobile.sdk.controllers.IOnAdErrorListener;
import de.guj.ems.mobile.sdk.controllers.IOnAdResizeListener;
import de.guj.ems.mobile.sdk.controllers.IOnAdSuccessListener;
import de.guj.ems.mobile.sdk.controllers.adserver.DFPSettingsAdapter;
import de.guj.ems.mobile.sdk.controllers.adserver.IAdServerSettingsAdapter;
import de.guj.ems.mobile.sdk.util.SdkLog;
import de.guj.ems.mobile.sdk.util.SdkUtil;

/**
 * ONLY USE THIS CLASS IF YOU WANT TO ADD THE VIEW PROGRAMMATICALLY INSTEAD OF
 * DEFINING IT WITHIN A LAYOUT.XML FILE!
 *
 * @author stein16
 */
public class GuJEMSAdView extends LinearLayout implements AppEventListener {

    private PublisherAdView adView;
    private AdSession adSession;

    private IAdServerSettingsAdapter settings;

    private final String TAG = "GuJEMSAdView";

    protected boolean hasAdUnitId = false;

    private int position = 0;
    private boolean destroyOnDetach = true;

    private boolean disallowRectangle = false;
    private boolean disallowBillboard = false;
    private boolean disallowDesktopBillboard = false;
    private boolean disallowLeaderboard = false;
    private boolean disallowTwoToOne = false;
    private boolean disallowThreeToOne = false;
    private String contentUrl = "";

    /**
     * Initialize view without configuration
     *
     * @param context android application context
     */
    public GuJEMSAdView(Context context) {
        super(context);
        this.preLoadInitialize(context, null);

    }

    /**
     * Initialize view with attribute set (this is the common constructor)
     *
     * @param context android application context
     * @param set     resource ID of the XML layout file to inflate from
     */
    public GuJEMSAdView(Context context, AttributeSet set) {
        this(context, set, true);
    }

    /**
     * Initialize view with attribute set (this is the common constructor)
     *
     * @param context android application context
     * @param set     resource ID of the XML layout file to inflate from
     * @param load    if set to true, the adview loads implicitly, if false, call
     *                load by yourself
     */
    public GuJEMSAdView(Context context, AttributeSet set, boolean load) {
        super(context, set);
        this.preLoadInitialize(context, set);
        if (load) {
            this.load();
        }
    }

    /**
     * Initialize view from XML
     *
     * @param context android application context
     * @param resId   resource ID of the XML layout file to inflate from
     */
    public GuJEMSAdView(Context context, int resId) {
        this(context, resId, true);
    }

    /**
     * Initialize view from XML
     *
     * @param context android application context
     * @param resId   resource ID of the XML layout file to inflate from
     * @param load    if set to true, the adview loads implicitly, if false, call
     *                load by yourself
     */
    public GuJEMSAdView(Context context, int resId, boolean load) {
        super(context);
        AttributeSet attrs = inflate(resId);
        this.preLoadInitialize(context, attrs);
        this.handleInflatedLayout(attrs);
        if (load) {
            this.load();
        }
    }

    /**
     * Initialize view from XML and add any custom parameters to the request
     *
     * @param context      android application context
     * @param customParams map of custom param names and thiur values
     * @param resId        resource ID of the XML layout file to inflate from
     */
    public GuJEMSAdView(Context context, Map<String, ?> customParams, int resId) {
        this(context, customParams, resId, true);
    }

    /**
     * Initialize view from XML and add any custom parameters to the request
     *
     * @param context      android application context
     * @param customParams map of custom param names and thiur values
     * @param resId        resource ID of the XML layout file to inflate from
     * @param load         if set to true, the adview loads implicitly, if false, call
     *                     load by yourself
     */
    public GuJEMSAdView(Context context, Map<String, ?> customParams,
                        int resId, boolean load) {
        super(context);
        AttributeSet attrs = inflate(resId);
        this.preLoadInitialize(context, attrs);
        this.settings.addCustomParams(customParams);
        this.handleInflatedLayout(attrs);
        if (load) {
            this.load();
        }
    }

    /**
     * Initialize view from XML and add matching or non-matching keywords as
     * well as any custom parameters to the request
     *
     * @param context      android application context
     * @param customParams map of custom param names and their values
     * @param kws          matching keywords
     * @param nkws         non-matching keywords
     * @param resId        resource ID of the XML layout file to inflate from
     */
    public GuJEMSAdView(Context context, Map<String, ?> customParams,
                        String[] kws, String nkws[], int resId) {
        this(context, customParams, kws, nkws, resId, true);
    }

    /**
     * Initialize view from XML and add matching or non-matching keywords as
     * well as any custom parameters to the request
     *
     * @param context      android application context
     * @param customParams map of custom param names and their values
     * @param kws          matching keywords
     * @param nkws         non-matching keywords
     * @param resId        resource ID of the XML layout file to inflate from
     * @param load         if set to true, the adview loads implicitly, if false, call
     *                     load by yourself
     */
    public GuJEMSAdView(Context context, Map<String, ?> customParams,
                        String[] kws, String nkws[], int resId, boolean load) {
        super(context);
        AttributeSet attrs = inflate(resId);
        this.preLoadInitialize(context, attrs, kws, nkws);
        this.settings.addCustomParams(customParams);
        this.handleInflatedLayout(attrs);
        if (load) {
            this.load();
        }
    }

    /**
     * Initialize view from XML and add matching or non-matching keywords
     *
     * @param context android application context
     * @param kws     matching keywords
     * @param nkws    non-matching keywords
     * @param resId   resource ID of the XML layout file to inflate from
     */
    public GuJEMSAdView(Context context, String[] kws, String nkws[], int resId) {
        this(context, kws, nkws, resId, true);
    }

    /**
     * Initialize view from XML and add matching or non-matching keywords
     *
     * @param context android application context
     * @param kws     matching keywords
     * @param nkws    non-matching keywords
     * @param resId   resource ID of the XML layout file to inflate from
     * @param load    if set to true, the adview loads implicitly, if false, call
     *                load by yourself
     */
    public GuJEMSAdView(Context context, String[] kws, String nkws[],
                        int resId, boolean load) {
        super(context);
        AttributeSet attrs = inflate(resId);
        this.preLoadInitialize(context, attrs, kws, nkws);
        this.handleInflatedLayout(attrs);
        if (load) {
            this.load();
        }
    }

    public void setAdSession(AdSession adSession) {
        this.adSession = adSession;
    }

    public AdSession getAdSession() {
        return this.adSession;
    }

    public ViewGroup.LayoutParams getNewLayoutParams(int w, int h) {
        return new ViewGroup.LayoutParams(w, h);
    }

    public IOnAdErrorListener getOnAdErrorListener() {
        return settings.getOnAdErrorListener();
    }

    public IOnAdEmptyListener getOnAdEmptyListener() {
        return settings.getOnAdEmptyListener();
    }

    public IOnAdSuccessListener getOnAdSuccessListener() {
        return settings.getOnAdSuccessListener();
    }

    public IOnAdResizeListener getOnAdResizeListener() {
        return settings.getOnAdResizeListener();
    }

    private void handleInflatedLayout(AttributeSet attrs) {
        int w = attrs.getAttributeIntValue(
                "http://schemas.android.com/apk/res/android", "layout_width",
                ViewGroup.LayoutParams.MATCH_PARENT);
        int h = attrs.getAttributeIntValue(
                "http://schemas.android.com/apk/res/android", "layout_height",
                ViewGroup.LayoutParams.WRAP_CONTENT);
        String bk = attrs.getAttributeValue(
                "http://schemas.android.com/apk/res/android", "background");
        if (getLayoutParams() != null) {
            getLayoutParams().width = w;
            getLayoutParams().height = h;
        } else {
            setLayoutParams(getNewLayoutParams(w, h));
        }

        if (bk != null) {
            setBackgroundColor(Color.parseColor(bk));
        }
    }

    private AttributeSet inflate(int resId) {
        AttributeSet as = null;
        Resources r = getResources();
        XmlResourceParser parser = r.getLayout(resId);

        int state = 0;
        do {
            try {
                state = parser.next();
            } catch (XmlPullParserException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (state == XmlPullParser.START_TAG) {
                if (parser.getName().equals(
                        "de.guj.ems.mobile.sdk.views.GuJEMSAdView")
                        || parser.getName().equals(
                        "de.guj.ems.mobile.sdk.views.GuJEMSListAdView") ||
                        parser.getName().equals(
                                "de.guj.ems.mobile.sdk.views.GuJEMSNativeContentAdView")) {
                    as = Xml.asAttributeSet(parser);
                    break;
                } else {
                    SdkLog.w(TAG, "Unknown parser " + parser.getName());
                }
            }
        } while (state != XmlPullParser.END_DOCUMENT);

        return as;
    }

    /**
     * Perform the actual request. Should only be invoked if a constructor with
     * the boolean load flag was used and it was false
     */
    public void load() {
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        if (lp != null) {
            lp.height = 0;
            this.setLayoutParams(lp);
        }
        if (settings != null) {
            SdkUtil.setContext(getContext());
            // Start request if online
            if (SdkUtil.isOnline()) {
                SdkLog.i(TAG, settings.hashCode() + " starting ad request");

                Builder requestBuilder = ((DFPSettingsAdapter) settings)
                        .getGoogleRequestBuilder(this.position, this.hasAdUnitId);

                if (!this.contentUrl.equals("")) {
                    requestBuilder.setContentUrl(this.contentUrl);
                }

                if (!this.hasAdUnitId) {
                    String adunit = ((DFPSettingsAdapter) settings)
                            .mapToDfpAdUnit();
                    SdkLog.d(TAG, settings.hashCode() + " using mapped DFP ad unit "
                            + adunit);
                    adView.setAdUnitId(adunit);
                } else {
                    SdkLog.d(TAG,
                            settings.hashCode() + " using directly set DFP ad unit "
                                    + adView.getAdUnitId());
                }
                adView.loadAd(requestBuilder.build());
            }
            // Do nothing if offline
            else {
                SdkLog.i(TAG, "No network connection - not requesting ads.");
                setVisibility(GONE);
            }
        } else {
            SdkLog.w(TAG, settings.hashCode() + " has no settings.");
            setLayoutParams(getNewLayoutParams(
                    (int) (300.0 * SdkUtil.getDensity()),
                    (int) (50.0 * SdkUtil.getDensity())));
            setVisibility(VISIBLE);
            if (this.settings != null
                    && this.settings.getOnAdSuccessListener() != null) {
                this.settings.getOnAdSuccessListener().onAdSuccess();
            }
        }
    }

    @Override
    public void onAppEvent(String arg0, String arg1) {
        SdkLog.d(TAG, settings.hashCode() + " received app event " + arg0 + "(" + arg1
                + ")");
        GuJEMSAdInterface.getInstance().doAppEvent(this, this.adView, arg0, arg1);
    }

    public void setNoRectangle(Boolean rS) {
        this.disallowRectangle = rS;
        this.setAdSizes();
    }

    public void setNoThreeToOne(Boolean rS) {
        this.disallowThreeToOne = rS;
        this.setAdSizes();
    }

    public void setNoBillboard(Boolean rS) {
        this.disallowBillboard = rS;
        this.setAdSizes();
    }

    public void setNoDesktopBillboard(Boolean rS) {
        this.disallowDesktopBillboard = rS;
        this.setAdSizes();
    }

    public void setNoLeaderboard(Boolean rS) {
        this.disallowLeaderboard = rS;
        this.setAdSizes();
    }

    public void setNoTwoToOne(Boolean rS) {
        this.disallowTwoToOne = rS;
        this.setAdSizes();
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    private void setAdSizes() {
        if (SdkUtil.isLargerThanPhone()) {
            AdSize[] adSizes = {AdSize.BANNER, new AdSize(768, 90),
                    new AdSize(728, 90), new AdSize(768, 300),
                    new AdSize(1024, 220), new AdSize(800, 250),
                    new AdSize(300, 250), new AdSize(1, 1)};
            if (((DFPSettingsAdapter) this.settings).isNoRectangle() || this.disallowRectangle) {
                SdkLog.d(TAG, settings.hashCode() + " removing accepted size: 300x250");
                adSizes[6] = new AdSize(1, 1);
            }
            if (((DFPSettingsAdapter) this.settings).isNoDesktopBillboard() ||
                    this.disallowDesktopBillboard) {
                SdkLog.d(TAG, settings.hashCode() + " removing accepted sizes: 800x250");
                adSizes[5] = new AdSize(1, 1);
            }
            if (((DFPSettingsAdapter) this.settings).isNoBillboard() || this.disallowBillboard) {
                SdkLog.d(TAG, settings.hashCode()
                        + " removing accepted sizes: 1024x220, 768x300");
                adSizes[4] = adSizes[3] = new AdSize(1, 1);
            }
            if (((DFPSettingsAdapter) this.settings).isNoLeaderboard() ||
                    this.disallowLeaderboard) {
                SdkLog.d(TAG, settings.hashCode()
                        + " removing accepted sizes: 728x90, 768x90");
                adSizes[2] = adSizes[1] = new AdSize(1, 1);
            }
            if(SdkUtil.getUseDebuggingAdSize()) {
                ArrayList<AdSize> list = new ArrayList<AdSize>();
                list.addAll(Arrays.asList(adSizes));
                list.add(new AdSize(200,446));
                adSizes = list.toArray(new AdSize[list.size()]);
            }
            adView.setAdSizes(adSizes);
        } else {
            AdSize[] adSizes = {AdSize.BANNER, //0
                    new AdSize(320, 50),//1
                    new AdSize(300, 75), //0
                    new AdSize(300, 50),//1
                    new AdSize(768, 90), //2
                    new AdSize(728, 90),//3
                    new AdSize(300, 150),//4
                    new AdSize(300, 250), //5
                    new AdSize(1, 1),//6
                    new AdSize(320, 53),//7
                    new AdSize(320, 75),//8
                    new AdSize(320, 80), //9
                    new AdSize(320, 100),//10
                    new AdSize(320, 106),//11
                    new AdSize(320, 150),//12
                    new AdSize(320, 160),//13
                    new AdSize(320, 250),//14
                    new AdSize(320, 320), //15
                    new AdSize(320, 416),//16
                    new AdSize(300, 100)//17

            };
            if (((DFPSettingsAdapter) this.settings).isNoTwoToOne() || this.disallowTwoToOne) {
                SdkLog.d(TAG, settings.hashCode() + " removing accepted size: 300x150");
                adSizes[6] = new AdSize(1, 1);
                adSizes[14] = new AdSize(1, 1);
                adSizes[15] = new AdSize(1, 1);
            }

            if (((DFPSettingsAdapter) this.settings).isNoThreeToOne() || this.disallowThreeToOne) {
                SdkLog.d(TAG, settings.hashCode() + " removing accepted size: 300x150");
                adSizes[12] = new AdSize(1, 1);
                adSizes[19] = new AdSize(1, 1);
            }
            if (((DFPSettingsAdapter) this.settings).isNoRectangle() || this.disallowRectangle) {
                SdkLog.d(TAG, settings.hashCode() + " removing accepted size: 300x250");
                adSizes[7] = new AdSize(1, 1);
                adSizes[16] = new AdSize(1, 1);
                adSizes[17] = new AdSize(1, 1);
                adSizes[18] = new AdSize(1, 1);
            }

            if (((DFPSettingsAdapter) this.settings).isNoLeaderboard() || this.disallowLeaderboard) {
                SdkLog.d(TAG, settings.hashCode()
                        + " removing accepted sizes: 728x90, 768x90");
                adSizes[4] = adSizes[5] = new AdSize(1, 1);
            }
            if(SdkUtil.getUseDebuggingAdSize()) {
                ArrayList<AdSize> list = new ArrayList<AdSize>();
                list.addAll(Arrays.asList(adSizes));
                list.add(new AdSize(200,446));
                adSizes = list.toArray(new AdSize[list.size()]);
            }
            adView.setAdSizes(adSizes);
        }
    }

    protected void preLoadInitialize(Context context, AttributeSet set) {
        setVisibility(View.GONE);
        this.adView = new PublisherAdView(context);
        TypedArray tVals = context.obtainStyledAttributes(set,
                R.styleable.GuJEMSAdView);
        String adUnit = tVals.getString(R.styleable.GuJEMSAdView_ems_adUnit);
        tVals.recycle();

        if (set != null && !isInEditMode()) {
            this.settings = new DFPSettingsAdapter();
            this.settings.setup(context, set);
            this.setAdSizes();
            if (adUnit != null) {
                setAdUnitId(adUnit, position);
            }
        }

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        this.addView(this.adView, lp);
        this.adView.setAdListener(new GuJEMSAdListener(this));
        this.adView.setAppEventListener(this);
    }

    protected void preLoadInitialize(Context context, AttributeSet set,
                                     String[] kws, String[] nkws) {
        setVisibility(View.GONE);

        this.adView = new PublisherAdView(context);
        TypedArray tVals = context.obtainStyledAttributes(set,
                R.styleable.GuJEMSAdView);
        String adUnit = tVals.getString(R.styleable.GuJEMSAdView_ems_adUnit);
        tVals.recycle();
        if (set != null) {
            this.settings = new DFPSettingsAdapter();
            this.settings.setup(context, set, kws);
            this.setAdSizes();
            if (adUnit != null) {
                setAdUnitId(adUnit, position);
            }
        }

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        this.addView(this.adView, lp);
        this.adView.setAdListener(new GuJEMSAdListener(this));
        this.adView.setAppEventListener(this);

        if (nkws != null) {
            SdkLog.w(TAG, "Negative keywords are no longer supported.");
        }

    }

    /**
     * Programmatically set the ems_zoneId attribute for this view
     *
     * @param zoneId the zoneId as provided by G+J EMS
     */
    public void setEmsZoneId(String zoneId) {
        ((DFPSettingsAdapter) this.settings).setEmsZoneId(zoneId);
    }

    /**
     * If you have a Google adUnitId instead of an EMS zoneId, you can set it
     * here instead of having them mapped to Google DFP.
     *
     * @param adUnitId
     */
    public void setAdUnitId(String adUnitId, int position) {
        if (adUnitId != null && adUnitId.indexOf(",") >= 0) {
            String[] vals = adUnitId.split(",");
            this.position = Integer.parseInt(vals[1]);
            this.adView.setAdUnitId(getContext().getResources().getString(
                    R.string.ems_dfpNetwork)
                    + vals[0].replace("/6032/", "").replaceAll("\\/6032\\/", ""));
            if ("yes".equals(vals[2])) {
                settings.addCustomRequestParameter("ind", "yes");
            }
        } else {
            this.position = position;
            this.adView.setAdUnitId(getContext().getResources().getString(
                    R.string.ems_dfpNetwork)
                    + adUnitId.replace("/6032/", "").replaceAll("\\/6032\\/", ""));
        }
        settings.addCustomRequestParameter("pos", this.position);
        SdkLog.d(
                TAG,
                settings.hashCode()
                        + " received ad unit "
                        + this.adView.getAdUnitId()
                        + " [pos="
                        + this.position
                        + ", ind="
                        + (settings.getParams().get("ind") != null ? settings
                        .getParams().get("ind") : "no") + "]");
        this.hasAdUnitId = true;
    }

    public void makeAdVisibile() {
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        if (lp != null) {
            AdSize a = this.adView.getAdSize();
            if (a.getHeight() != 1 && a.getWidth() != 1) {
                lp.height = -2;
            }
            this.setLayoutParams(lp);
        }
    }

    /**
     * Add a listener to the view which responds to empty ad responses
     *
     * @param l Implemented listener
     */
    public void setOnAdEmptyListener(IOnAdEmptyListener l) {
        this.settings.setOnAdEmptyListener(l);
    }

    /**
     * Add a listener to the view which responds to errors while requesting ads
     *
     * @param l Implemented listener
     */
    public void setOnAdErrorListener(IOnAdErrorListener l) {
        this.settings.setOnAdErrorListener(l);
    }

    /**
     * Add a listener to the view which responds to successful ad requests
     *
     * @param l Implemented listener
     */
    public void setOnAdSuccessListener(IOnAdSuccessListener l) {
        this.settings.setOnAdSuccessListener(l);
    }

    /**
     * Add a listener to the view which responds to an ad resize event
     *
     * @param l Implemented listener
     */
    public void setOnAdResizeListener(IOnAdResizeListener l) {
        this.settings.setOnAdResizeListener(l);
    }

    /**
     * Get a reference to the view's ad settings
     *
     * @return the view's settings
     */
    public IAdServerSettingsAdapter getSettings() {
        return settings;
    }

    /**
     * destroy view
     */
    public void destroyView() {
        removeAllViews();
    }

    /**
     * tell view that on detach : dont detroy
     */
    public void setNoDestroyOnDetach(boolean val) {
        this.destroyOnDetach = !val;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.destroyOnDetach) {
            this.destroyView();
        }
    }

    @Deprecated
    public void setGooglePublisherId(String id) {
        SdkLog.w(TAG,
                "setGooglePublisherId is no longer necessary and no longer supported.");
    }

}
