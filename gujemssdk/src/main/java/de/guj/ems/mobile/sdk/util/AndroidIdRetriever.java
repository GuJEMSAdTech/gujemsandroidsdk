package de.guj.ems.mobile.sdk.util;

import android.os.AsyncTask;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

interface AndroidIdRetrieverRequester {
    public void onRequestFinished();
}

public class AndroidIdRetriever {
    private static AndroidIdRetriever instance = null;
    private AsyncTask<Object, Void, String> task = null;
    private String androidId = "";
    private String androidIdHashed = "";
    private ArrayList<AndroidIdRetrieverRequester> requester = new ArrayList<AndroidIdRetrieverRequester>();

    public static AndroidIdRetriever getInstance() {
        if (instance == null) {
            instance = new AndroidIdRetriever();
        }
        return instance;
    }

    public void addRequester(AndroidIdRetrieverRequester r) {
        if (androidId.equals("")) {
            requester.add(r);
        } else {
            r.onRequestFinished();
        }
    }

    public void execute() {
        if (task == null) this.start();
    }

    public String getAndroidId() {
        return androidId;
    }

    public String getAndroidIdHashed() {
        return androidIdHashed;
    }

    private void start() {
        task = new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... voids) {
                AdvertisingIdClient.Info idInfo = null;
                try {
                    idInfo = AdvertisingIdClient.getAdvertisingIdInfo(SdkUtil.getContext());
                    androidId = SdkUtil.getNonPersonalizedAdsStatus() ? "" : idInfo.getId();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return androidId;
            }


            @Override
            protected void onPostExecute(String advertId) {
                try {
                    // Create MD5 Hash
                    MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                    digest.update(advertId.getBytes());
                    byte messageDigest[] = digest.digest();

                    // Create Hex String
                    StringBuffer hexString = new StringBuffer();
                    for (int i = 0; i < messageDigest.length; i++)
                        hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
                    androidIdHashed = hexString.toString();

                    // walk through...
                    for (int i = 0; i < requester.size(); i++) {
                        AndroidIdRetrieverRequester req = requester.get(i);
                        req.onRequestFinished();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        task.execute();
    }
}
