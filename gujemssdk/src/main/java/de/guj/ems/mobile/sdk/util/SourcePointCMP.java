package de.guj.ems.mobile.sdk.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


import com.sourcepoint.cmplibrary.ConsentLib;
import com.sourcepoint.cmplibrary.ConsentLibException;

import java.util.ArrayList;
import java.util.Date;

public class SourcePointCMP {

    private static final String STORAGE_TIMESTAMP_KEY = "consent_timestamp";
    private static final String STORAGE_NPA_KEY = "consent_npa";
    private static final long CONSENT_REFRESH_RATE = (long) (24 * 60 * 60 * 1000);

    private static String TAG = "SourcePointCMP";
    private Activity mainActivity;
    private SharedPreferences sharedPref;
    private boolean stage;
    private boolean internalStage;
    private SharedPreferences storage;
    private long lastConsentUpdateTimestamp;
    private boolean inTestingMode = false;

    public SourcePointCMP(Activity mainActivity, SharedPreferences sharedPref,boolean stage, boolean internalStage) {
        this.configure(mainActivity, sharedPref, stage, internalStage);
    }

    public SourcePointCMP(Activity mainActivity, SharedPreferences sharedPref,boolean stage, boolean internalStage, boolean testing) {
        this.configure(mainActivity, sharedPref, stage, internalStage);
        this.inTestingMode = testing;
    }

    public void initConsent() {
        if (this.inTestingMode && !showConsentMessage()) {
            interpret(allowPersonalizedAds());
            return;
        }
        try {
            ConsentLib cLib = ConsentLib.newBuilder(212, SdkUtil.getAppPackageName(), this.mainActivity)
                .setPage("main")
                .setMmsDomain("mms.adalliance.io")
                .setStage(stage)
                .setInternalStage(internalStage)
                .setOnMessageChoiceSelect(new ConsentLib.Callback() {
                    @Override
                    public void run(ConsentLib c) {
                        storage.edit().putLong(STORAGE_TIMESTAMP_KEY, new Date().getTime()).apply();
                        interpret(c.choiceType.toString().equals("11"));
                    }
                })
                .setOnInteractionComplete(new ConsentLib.Callback() {
                    @Override
                    public void run(ConsentLib c) {
                        try {
                            boolean[] IABPurposeConsents = c.getIABPurposeConsents(new int[]{3});
                            interpret(IABPurposeConsents[0]);
                        } catch (ConsentLibException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .build();
            cLib.run();
        } catch (ConsentLibException e) {
            SdkLog.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void configure(Activity mainActivity, SharedPreferences sharedPref,boolean stage, boolean internalStage) {
        this.mainActivity = mainActivity;
        this.sharedPref = sharedPref;
        this.stage = stage;
        this.internalStage = internalStage;
        this.storage = mainActivity.getSharedPreferences("de.guj.ems.mobile.sdk.cmp", Context.MODE_PRIVATE);
        if(this.storage.contains(STORAGE_TIMESTAMP_KEY)) {
            this.lastConsentUpdateTimestamp = this.storage.getLong(STORAGE_TIMESTAMP_KEY, new Date().getTime());
        } else {
            this.lastConsentUpdateTimestamp = 0;
        }
    }

    private boolean allowPersonalizedAds() {
        boolean allow = true;
        if(this.storage.contains(STORAGE_NPA_KEY)) {
            allow = this.storage.getBoolean(STORAGE_NPA_KEY, true);
        }
        return allow;
    }

    private boolean showConsentMessage() {
        boolean show = false;
        Long current = new Date().getTime();
        Long lastUpdate = this.storage.getLong(STORAGE_TIMESTAMP_KEY, new Date().getTime());
        long diff = current - lastUpdate;
        SdkLog.i(TAG, "last consent time diff: " + diff);
        if(current - lastUpdate > CONSENT_REFRESH_RATE) {
            show = true;
        }
        return show;
    }

    private void interpret(Boolean status) {
        if (status) {
            SdkUtil.setNonPersonalizedAdsStatus(false, false);
            storage.edit().putBoolean(STORAGE_NPA_KEY, false).apply();
        } else {
            // block personalized ads
            SdkUtil.setNonPersonalizedAdsStatus(true, false);
            storage.edit().putBoolean(STORAGE_NPA_KEY, true).apply();
        }
    }
}
