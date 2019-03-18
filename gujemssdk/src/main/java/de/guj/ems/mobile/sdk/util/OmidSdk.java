package de.guj.ems.mobile.sdk.util;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;

import com.iab.omid.library.emsgujde.Omid;
import com.iab.omid.library.emsgujde.adsession.AdEvents;
import com.iab.omid.library.emsgujde.adsession.AdSession;
import com.iab.omid.library.emsgujde.adsession.AdSessionConfiguration;
import com.iab.omid.library.emsgujde.adsession.AdSessionContext;
import com.iab.omid.library.emsgujde.adsession.Owner;
import com.iab.omid.library.emsgujde.adsession.Partner;
import com.iab.omid.library.emsgujde.adsession.VerificationScriptResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by proeg on 22.10.2018.
 */

class OmidSdk {

    private static OmidSdk instance;
    private boolean activated = false;
    private boolean jsLibraryLoaded = false;
    private String jsLibrary = null;
    private Partner partner;

    private OmidSdk() {
    }

    public static OmidSdk getInstance() {
        if (OmidSdk.instance == null) {
            OmidSdk.instance = new OmidSdk();
        }
        return OmidSdk.instance;
    }

    public String getJsLibrary() {
        return this.jsLibrary;
    }

    public void init(Context context) {
        try {
            this.activated = Omid.activateWithOmidApiVersion(Omid.getVersion(), context);
            if (!activated) {
                SdkLog.e("OmidSdk", "could not activate omidSdk");
            }
            this.partner = Partner.createPartner("Emsgujde", "2.2.4");
            this.loadJsLibrary();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void loadJsLibrary() {
        SdkLog.i("OmidSdk", "load js library");
        String OMID_JS_SERVICE_URL = "static.emsservice.de";
        HttpRequestConfig config = new HttpRequestConfig(OMID_JS_SERVICE_URL, "GET", "android/omsdk-v1.js");
        HttpConnectionTask task = new HttpConnectionTask();
        task.setHttpRequestConfig(config);
        task.setHttpOnTaskCompleted(new HttpOnTaskCompleted() {
            @Override
            public void onTaskCompleted(String s) {
                jsLibraryLoaded = true;
                jsLibrary = s;
                SdkLog.i("OmidSdk", "js library loaded: " + s);
            }
        });
        task.execute();
    }

    public AdSession createAdSession(View adView) {
        SdkLog.i("OmidSdk", "createAdSession");
        Owner owner = Owner.NATIVE;
        List<VerificationScriptResource> verificationScriptResources = new ArrayList<>();
        String customReferenceData = "";
        AdSession adSession = null;
        try {
            AdSessionContext adSessionContext = AdSessionContext.createNativeAdSessionContext(partner, this.getJsLibrary(),
                    verificationScriptResources, customReferenceData);
            AdSessionConfiguration adSessionConfiguration =
                    AdSessionConfiguration.createAdSessionConfiguration(owner, null, false);
            adSession = AdSession.createAdSession(adSessionConfiguration, adSessionContext);
            adSession.registerAdView(adView);
            adSession.start();
            new StopSessionTimer(adSession, 5000, 5000).start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return adSession;
    }

    public void impressionOccurred(AdSession adSession) {
        SdkLog.i("OmidSdk", "impressionOccurred");
        try {
            AdEvents adEvents = AdEvents.createAdEvents(adSession);
            adEvents.impressionOccurred();
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void stopAdSession(AdSession adSession) {
        SdkLog.i("OmidSdk", "stopAdSession");
        adSession.finish();
        adSession = null;
    }
}

class StopSessionTimer extends CountDownTimer {

    public AdSession adSession;

    public StopSessionTimer(AdSession adSession, long duration, long interval) {
        super(duration, interval);
        this.adSession = adSession;
    }

    public void onTick(long millisUntilFinished) {
    }

    public void onFinish() {
        OmidSdk.getInstance().stopAdSession(this.adSession);
    }
}
