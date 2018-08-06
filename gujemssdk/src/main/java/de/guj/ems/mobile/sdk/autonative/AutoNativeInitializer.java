package de.guj.ems.mobile.sdk.autonative;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.guj.ems.mobile.sdk.util.SdkLog;
import de.guj.ems.mobile.sdk.util.SdkUtil;

/**
 * Created by proeg on 25.08.2017.
 */

class AutoNativeInitializer {

    private static final String TAG = "AutoNativeInitializer";
    private View view;
    private AutoNativeConfig config;
    private Map<String, String> autonativeFieldValueMap;
    private Context context;

    public AutoNativeInitializer(Context context, View view, AutoNativeConfig config, Map<String, String> autonativeFieldValueMap) {
        this.context = context;
        this.view = view;
        this.config = config;
        this.autonativeFieldValueMap = autonativeFieldValueMap;
    }

    public void initializeView() {
        setHeadline();
        setSubHeadline();
        setTeaserText();
        setTeaserImage();
        setAdLabel();
        loadImpressionTracker();
        loadImpressionPixel();
    }

    private void registerOnClickListener(View clickableView) {
        clickableView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openClickUrlInBrowser();
            }
        });
    }

    private void openClickUrlInBrowser() {
        String clickURL = constructClickUrl();
        if (isValid(clickURL)) {
            String clickUrlServer = autonativeFieldValueMap.get("ClickURLServer");
            if (isValid(clickUrlServer)) {
                try {
                    clickUrlServer += URLEncoder.encode(clickURL, "utf-8");
                    SdkLog.i(TAG, "encoded clickURL: " + clickUrlServer);
                    if (this.config.getBrowser() != null) {
                        this.config.getBrowser().clickedOnTeaser(
                                clickUrlServer, clickURL,
                                autonativeFieldValueMap.get("SpecialAdUnit"),
                                autonativeFieldValueMap.get("ArticleId"));
                    } else {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(clickUrlServer)));
                    }
                } catch (Exception e) {
                    SdkLog.e(TAG, "could not encode clickurl", e);
                }
            } else {
                SdkLog.i(TAG, "no ClickURLServer field found in autonative ad");
            }
        } else {
            SdkLog.e(TAG, "no ClickURL could be constructed");
        }
    }

    private String constructClickUrl() {
        String clickURL = autonativeFieldValueMap.get("ClickURL");
        if (!isValid(clickURL)) {
            SdkLog.e(TAG, "no clickurl found - trying to construct custom one from baseurl");
            String baseUrl = SdkUtil.getAutonativeBaseUrl();
            if (baseUrl == null) {
                SdkLog.e(TAG, "baseurl not set. not possible to construct clickurl");
            } else {
                clickURL = baseUrl + "/?an=s:" + autonativeFieldValueMap.get("SpecialAdUnit") + "a:" + autonativeFieldValueMap.get("ArticleId") + "-t:n";
            }
        }
        return clickURL;
    }

    private void setTextViewWithTag(String tag, String text) {
        View targetV = view.findViewWithTag(tag);
        if (targetV != null) {
            if (targetV instanceof TextView) {
                ((TextView) targetV).setText(text);
                registerOnClickListener(targetV);
                SdkLog.i(TAG, "text attribute of view with tag: " + tag + " successfully set");
            } else {
                SdkLog.e(TAG, "view with tag: " + tag + " is not a TextView - cant set text");
            }
        } else {
            SdkLog.e(TAG, "no " + tag + " tag found in view - cant set text");
        }
    }

    private void setHeadline() {
        String headlineText = autonativeFieldValueMap.get("Headline");
        if (isValid(headlineText)) {
            setTextViewWithTag(Constants.AUTO_NATIVE_TAG_HEADLINE, headlineText);
        } else {
            SdkLog.i(TAG, "no Headline field found in autonative ad");
        }
    }

    private void setSubHeadline() {
        String subHeadlineText = autonativeFieldValueMap.get("SubHeadline");
        if (isValid(subHeadlineText)) {
            setTextViewWithTag(Constants.AUTO_NATIVE_TAG_SUBHEADLINE, subHeadlineText);
        } else {
            SdkLog.i(TAG, "no SubHeadline field found in autonative ad");
        }
    }

    private void setTeaserText() {
        String teaserText = autonativeFieldValueMap.get("TeaserText");
        if (isValid(teaserText)) {
            setTextViewWithTag(Constants.AUTO_NATIVE_TAG_TEASERTEXT, teaserText);
        } else {
            SdkLog.i(TAG, "no TeaserText field found in autonative ad");
        }
    }

    private void setTeaserImage() {
        String teaserUrl = autonativeFieldValueMap.get("TeaserImage" + config.getTeaserAspectRatio());
        if (isValid(teaserUrl)) {
            View teaserV = view.findViewWithTag(Constants.AUTO_NATIVE_TAG_TEASER);
            if (teaserV != null) {
                if (teaserV instanceof ImageView) {
                    ImageView iView = (ImageView) teaserV;
                    registerOnClickListener(iView);
                    new ImgToViewLoader(iView).execute(teaserUrl);
                    SdkLog.i(TAG, "ImgToViewLoader started - trying to insert teaserimage to view with tag " + Constants.AUTO_NATIVE_TAG_TEASER);
                } else {
                    SdkLog.e(TAG, "view with tag: " + Constants.AUTO_NATIVE_TAG_TEASER + " is not a ImageView - cant load teaser image");
                }
            } else {
                SdkLog.e(TAG, "no " + Constants.AUTO_NATIVE_TAG_TEASER + " tag found in view - cant load teaser image");
            }
        } else {
            SdkLog.i(TAG, "no TeaserImage field found in autonative ad");
        }
    }

    private void setAdLabel() {
        String adLabel = autonativeFieldValueMap.get("AdLabel");
        if (isValid(adLabel)) {
            boolean showAdLabel = Boolean.parseBoolean(adLabel);
            if (!showAdLabel) {
                View adView = view.findViewWithTag(Constants.AUTO_NATIVE_TAG_ADLABEL);
                if (adView != null) {
                    adView.setVisibility(View.GONE);
                    SdkLog.i(TAG, "AdLabel View successfully removed");
                }
            } else {
                SdkLog.i(TAG, "show AdLabel value is true - dont remove AdLabel View");
            }
        }
    }

    private void loadImpressionTracker() {
        List<String> urls = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            String tracker = autonativeFieldValueMap.get("ImpressionTracker" + i);
            if (isValid(tracker)) {
                urls.add(tracker);
            }
        }
        for (String url : urls) {
            WebView v = new WebView(context);
            v.setVisibility(View.INVISIBLE);
            SdkUtil.evaluateJavascript(v, url);
        }
    }

    private void loadImpressionPixel() {
        String url = autonativeFieldValueMap.get("ViewBasedImpressionPixel");
        if (isValid(url)) {
            WebView v = new WebView(context);
            SdkUtil.loadInvisibleWebView(v, url);
        }
    }

    private boolean isValid(String autoNativeField) {
        boolean valid = false;
        if (autoNativeField != null && !autoNativeField.equals("")) {
            valid = true;
        }
        return valid;
    }
}
