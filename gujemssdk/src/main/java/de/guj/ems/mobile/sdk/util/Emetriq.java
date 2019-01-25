package de.guj.ems.mobile.sdk.util;

public class Emetriq extends HttpConnectionTask implements AndroidIdRetrieverRequester {

    private static Emetriq instance = null;

    private String idfa = "";
    private AndroidIdRetriever retriever = null;
    private String contentUrl = "";
    private String appId = "";

    public static Emetriq getInstance() {
        if (instance == null) {
            instance = new Emetriq();
        }
        return instance;
    }

    Emetriq() {
        appId = SdkUtil.getAppPackageName();
        retriever = AndroidIdRetriever.getInstance();
        retriever.execute();
        retriever.addRequester(this);
    }

    public void setContentUrl(String contentUrl) {
        if (!this.contentUrl.equals(contentUrl)) {
            this.contentUrl = contentUrl;
            this.submitToServer();
        }
    }

    @Override
    public void onRequestFinished() {
        idfa = retriever.getAndroidId();
        this.submitToServer();
    }

    private HttpRequestConfig prepareRequest() {
        HttpRequestConfig httpRequestConfig = new HttpRequestConfig("aps.xplosion.de", "GET", "/data");
        httpRequestConfig.addUrlParam("sid", "13626");
        httpRequestConfig.addUrlParam("device_id", this.idfa);
        httpRequestConfig.addUrlParam("app_id", this.appId);
        httpRequestConfig.addUrlParam("link", this.contentUrl);
        httpRequestConfig.addUrlParam("os", "android");
        return httpRequestConfig;
    }

    private void submitToServer() {
        if (!this.contentUrl.equals("") && !this.idfa.equals("")) {
            this.setHttpRequestConfig(this.prepareRequest());
            this.execute();
        }
    }
}
