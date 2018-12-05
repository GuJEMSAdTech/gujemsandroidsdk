package de.guj.ems.mobile.sdk.autonative;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import de.guj.ems.mobile.sdk.util.SdkLog;

/**
 * Created by proeg on 24.08.2017.
 */

/**
 * Wrapper to hide AsyncTask. Prevents calling of execute multiple times.
 */
public class AutoNativeLoader {
    public AutoNativeLoader(AutoNativeConfig config, Context context, View view) {
        new AutoNativeTaskExecutor(config, context, view);
    }
}

class AutoNativeTaskExecutor extends AsyncTask<Object, Object, Map<String, String>> {

    private static final String TAG = "AutoNativeTaskExecutor";
    private final String[] xmlFields = new String[]{
            "ImpressionTracker1", "ImpressionTracker2", "ImpressionTracker3", "ImpressionTracker4",
            "ClickURL", "ClickUrlNewWindow", "AdLabel", "PresenterLabel", "PresenterText", "PresenterLabelOnlyImage",
            "PresenterName", "PresenterImage", "PresenterLogoOrientation", "TeaserImage12", "TeaserImage11", "TeaserImage13",
            "SubHeadline", "Headline", "TeaserText", "ViewBasedImpressionPixel", "SpecialAdUnit", "ArticleId",
            "TopicPerPublisher", "ClickURLServer", "ClickTracker", "creativeId"
    };
    private View view;
    private AutoNativeConfig config;
    private Context context;

    public AutoNativeTaskExecutor(AutoNativeConfig config, Context context, View view) {
        this.config = config;
        this.view = view;
        this.context = context;
        this.execute();
    }

    private URL buildUrl() {
        URL url = null;
        try {
            String urlTemplate = "https://pubads.g.doubleclick.net/gampad/adx?iu={0}&sz=200x200&t=pos%3D{1}&m=text/xml&tile={2}&c={3}&dpt=1";
            Object[] urlValues = new Object[]
                    {
                            this.config.getAdUnit(),
                            this.config.getPosition(),
                            this.config.getTile(),
                            ("" + this.config.getCorrelator())
                    };
            url = new URL(MessageFormat.format(urlTemplate, urlValues));
        } catch (Exception e) {
            SdkLog.e(TAG, "Error building url", e);
        }
        return url;
    }

    @Override
    protected Map<String, String> doInBackground(Object... strings) {
        URL url = buildUrl();
        Map<String, String> autoNativeXmlFieldValueMap = new HashMap<>();
        if (url != null) {
            try {
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                SdkLog.i(TAG, "AutoNative url: " + url.toString());
                SSLContext sc;
                sc = SSLContext.getInstance("TLS");
                sc.init(null, null, new java.security.SecureRandom());
                conn.setSSLSocketFactory(sc.getSocketFactory());
                conn.setReadTimeout(5000);
                conn.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    SdkLog.i(TAG, "Successfully sent autonative request.");
                } else {
                    SdkLog.e(TAG, "Error returned from autonative request. responsecode: " + responseCode);
                }
                autoNativeXmlFieldValueMap = parseXML(conn.getInputStream());
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                conn.disconnect();
            } catch (Exception e) {
                SdkLog.e(TAG, "error in https request: " + e.toString());
            }
        } else {
            SdkLog.e(TAG, "error in url - dont send autonative request");
        }
        return autoNativeXmlFieldValueMap;
    }

    @Override
    protected void onPostExecute(Map<String, String> result) {
        if (!(result == null || result.size() == 0)) {
            AutoNativeInitializer initializer = new AutoNativeInitializer(context, view, config, new HashMap<>(result));
            initializer.initializeView();
        } else {
            SdkLog.e(TAG, "could not retrieve autonative ad - remove view");
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            } else {
                SdkLog.e(TAG, "could not remove view - view is null");
            }
        }
    }

    private Map<String, String> parseXML(InputStream xmlStream) {
        XmlPullParser parser = Xml.newPullParser();
        Map<String, String> res = null;
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(xmlStream, null);
            parser.nextTag();
            res = traverseXML(parser);
        } catch (Exception e) {
            SdkLog.e(TAG, "error parsing autonative xml", e);
        }
        return res;
    }

    private Map<String, String> traverseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        Map<String, String> xmlFieldValueMap = new HashMap<>();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    for (String tag : xmlFields) {
                        String name = parser.getName();
                        if (name.equals(tag)) {
                            xmlFieldValueMap.put(name, readText(parser).trim());
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = parser.next();
        }
        return xmlFieldValueMap;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

}
