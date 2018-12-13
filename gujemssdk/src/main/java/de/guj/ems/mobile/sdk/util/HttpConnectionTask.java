package de.guj.ems.mobile.sdk.util;


import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class HttpConnectionTask extends AsyncTask<String, String, String> {

    private static final String TAG = "HttpConnectionTask";
    private HttpOnTaskCompleted callback = null;
    private HttpRequestConfig requestConfig = null;
    private HashMap<String, String> outputWriter = null;

    public HttpConnectionTask() {
    }

    public void setHttpRequestConfig(HttpRequestConfig config) {
        this.requestConfig = config;
    }

    public void setHttpOnTaskCompleted(HttpOnTaskCompleted listener) {
        this.callback = listener;
    }

    public void setWriteHashMap(HashMap<String, String> map) {
        this.outputWriter = map;
    }

    @Override
    protected void onPostExecute(String result) {
        if (this.callback != null) {
            callback.onTaskCompleted(result);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        URL url = this.buildURL();
        String result = "";
        try {
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            SSLContext sc;
            sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new java.security.SecureRandom());
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod(this.requestConfig.getHttpMethod());
            conn.setDoInput(true);
            if (this.outputWriter != null) {
                conn.setDoOutput(true);
            }
            conn.connect();
            if (this.outputWriter != null) {
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                boolean first = true;
                for (Map.Entry<String, String> entry : this.outputWriter.entrySet()) {
                    if (!first) wr.write("&");
                    first = false;
                    wr.write(entry.getKey() + "=" + entry.getValue());
                }
                wr.flush();
                wr.close();
            }

            int responseCode = conn.getResponseCode();
            InputStream stream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), 8);
            String line = "";
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            reader.close();
            conn.disconnect();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                SdkLog.i(TAG, "Successfully sent httpRequest");
            } else {
                SdkLog.e(TAG, "Error returned from httpRequest");
            }
        } catch (Exception e) {
            SdkLog.e(TAG, "Error building url", e);
        }
        return result;
    }

    private URL buildURL() {
        URL res = null;
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https").authority(this.requestConfig.getUrl());
        String[] path = this.requestConfig.getPath().split("/");
        for (int i = 0; i < path.length; i++) {
            builder.appendPath(path[i]);
        }
        for (Map.Entry<String, String> entry : this.requestConfig.getUrlParams().entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        Uri uri = builder.build();
        try {
            res = new URL(uri.toString());
        } catch (MalformedURLException e) {
            SdkLog.e(TAG, "Error building url", e);
        }
        return res;
    }
}
