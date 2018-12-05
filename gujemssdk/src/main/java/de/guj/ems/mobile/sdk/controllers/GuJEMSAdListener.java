package de.guj.ems.mobile.sdk.controllers;

import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import de.guj.ems.mobile.sdk.controllers.adserver.IAdServerSettingsAdapter;
import de.guj.ems.mobile.sdk.util.SdkLog;
import de.guj.ems.mobile.sdk.views.GuJEMSAdView;

/**
 * Interface for redirecting internal Ad View events to the Google SDK
 *
 * @author stein16
 */
public class GuJEMSAdListener extends AdListener {

    private final static String TAG = "GuJEMSAdListener";

    private GuJEMSAdView adView;

    private IAdServerSettingsAdapter settings;

    private String logPrefix;

    /**
     * Listener for when an ad view instance is available
     *
     * @param adView The ad view
     */
    public GuJEMSAdListener(GuJEMSAdView adView) {
        super();
        this.adView = adView;
        this.settings = adView.getSettings();
        logPrefix = String.valueOf(settings.hashCode());
    }

    /**
     * Listener for when no ad view instance is available
     *
     * @param settings request settings
     */
    public GuJEMSAdListener(IAdServerSettingsAdapter settings) {
        super();
        this.settings = settings;
        this.adView = null;
        logPrefix = String.valueOf(settings.hashCode());
    }

    @Override
    public void onAdClosed() {
        SdkLog.d(TAG, logPrefix + ": onAdClosed.");
        super.onAdClosed();
    }

    private void onAdError(String error) {
        if (this.settings.getOnAdErrorListener() != null) {
            this.settings.getOnAdErrorListener().onAdError(error);
        } else {
            SdkLog.e(TAG, logPrefix + ": " + error);
        }
    }

    private void onAdEmpty() {
        if (this.settings.getOnAdEmptyListener() != null) {
            this.settings.getOnAdEmptyListener().onAdEmpty();
        } else {
            SdkLog.e(TAG, logPrefix + " ad failed to load: No valid ad found.");
        }
    }

    private void onAdSuccess() {
        if (this.adView != null) {
            this.adView.makeAdVisibile();
        }
        if (this.settings.getOnAdSuccessListener() != null) {
            this.settings.getOnAdSuccessListener().onAdSuccess();
        } else {
            SdkLog.d(TAG, logPrefix + " ad loaded.");
        }
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        switch (errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                onAdError("Ad failed to load: Internal error.");
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                onAdError("Ad failed to load: Invalid request.");
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                onAdError("Ad failed to load: Network error.");
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                onAdEmpty();
        }
        super.onAdFailedToLoad(errorCode);
    }

    @Override
    public void onAdLeftApplication() {
        SdkLog.d(TAG, logPrefix + ": onAdLeftApplication.");
        super.onAdLeftApplication();
    }

    @Override
    public void onAdLoaded() {
        if (this.adView != null) {
            SdkLog.i(TAG, logPrefix + " request finished.");
            this.adView.setVisibility(View.VISIBLE);
/*            this.adView.setAdSession(OmidSdk.getInstance().createAdSession(this.adView));
            if (this.adView.getAdSession() != null) {
                OmidSdk.getInstance().impressionOccurred(this.adView.getAdSession());
            }*/
        }
        onAdSuccess();
        super.onAdLoaded();
    }

    @Override
    public void onAdOpened() {
        SdkLog.d(TAG, logPrefix + ": onAdOpened.");
        super.onAdOpened();
    }

}
