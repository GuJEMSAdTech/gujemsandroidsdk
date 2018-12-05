package de.guj.ems.mobile.sdk.util;

import android.util.Log;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest.Builder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class YieldLab extends HttpConnectionTask implements HttpOnTaskCompleted {

    private static final String TAG = "YieldLab";

    private static ArrayList<YieldLabElement> elements = new ArrayList<YieldLabElement>();
    private static YieldLab instance = null;
    private static HashMap<String, String> map = null;
    private static int dType = 0;

    private HashMap<String, String> hashMap = null;

    private YieldLab(HashMap<String, String> idList, int deviceType) {
        this.hashMap = idList;
        this.setHttpRequestConfig(this.prepareRequest(idList, deviceType));
        this.setHttpOnTaskCompleted(this);
    }

    public static void init(HashMap<String, String> idList, int deviceType) {
        map = idList;
        dType = deviceType;
    }

    public static void request() {
        if (map != null) {
            instance = new YieldLab(map, dType);
            instance.execute();
        }
    }

    public static boolean appendToAdCall(Builder request) {
        if (!elements.isEmpty()) {
            for (int i = 0; i < elements.size(); i++) {
                for (Map.Entry<String, String> entry : elements.get(i).getDataForCall().entrySet()) {
                    request.addCustomTargeting(entry.getKey(), entry.getValue());
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private HttpRequestConfig prepareRequest(HashMap<String, String> idList, int deviceType) {
        AndroidIdRetriever aIdRet = AndroidIdRetriever.getInstance();
        aIdRet.execute();
        String list = "";
        for (Map.Entry<String, String> map : idList.entrySet()) {
            list += map.getValue() + ",";
        }
        HttpRequestConfig httpRequestConfig = new HttpRequestConfig("ad.yieldlab.net", "GET", "/yp/" + list);
        httpRequestConfig.addUrlParam("ts", String.valueOf(SdkUtil.getCorrelator()));
        httpRequestConfig.addUrlParam("json", "true");
        httpRequestConfig.addUrlParam("pvid", "true");
        httpRequestConfig.addUrlParam("yl_rtb_ifa", aIdRet.getAndroidId());
        httpRequestConfig.addUrlParam("yl_rtb_devicetype", String.valueOf(deviceType));
        return httpRequestConfig;
    }

    @Override
    public void onTaskCompleted(String s) {
        try {
            JSONArray json = new JSONArray(s);
            if (json.length() > 0) {
                elements.clear();
                for (int i = 0; i < json.length(); i++) {
                    JSONObject element = json.getJSONObject(i);
                    long ylId = 0;
                    String partner = "";
                    ylId = element.getLong("c.ylid");
                    partner = element.getString("c.partner");
                    elements.add(new YieldLabElement(
                            element.getLong("id"),
                            ylId,
                            element.getString("price"),
                            partner,
                            element.getString("pvid"),
                            this.hashMap
                    ));
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }
}

class YieldLabElement {
    private String pvid = "";
    private String price = "";
    private long id = 0;
    private long ylid = 0;
    private String partner = "";
    private HashMap<String, String> map = null;

    public YieldLabElement(long id, long ylid, String price, String partner, String pvid, HashMap<String, String> map) {
        this.id = id;
        this.price = price;
        this.partner = partner;
        this.pvid = pvid;
        this.map = map;
        this.ylid = ylid;
    }

    public HashMap<String, String> getDataForCall() {
        HashMap<String, String> map = new HashMap<>();
        String position = null;
        for (Map.Entry<String, String> hashMap : this.map.entrySet()) {
            if (hashMap.getValue().equals(String.valueOf(this.id))) {
                position = hashMap.getKey();
                break;
            }
        }
        if (position != null) {
            if (this.ylid != 0) {
                map.put("ylid" + position + "c", String.valueOf(this.ylid));
                map.put("ylp" + position, this.price);
            } else {
                map.put("ylid" + position, String.valueOf(this.id));
            }
            map.put("yl" + position, String.valueOf(this.id));
            map.put("ylid" + position + "pa", String.valueOf(this.partner));
            map.put("ylpvid" + position, this.pvid);
        }
        return map;
    }
}

