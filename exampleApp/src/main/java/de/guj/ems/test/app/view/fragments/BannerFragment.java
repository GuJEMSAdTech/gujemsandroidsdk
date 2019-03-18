package de.guj.ems.test.app.view.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.guj.ems.mobile.sdk.controllers.IOnAdEmptyListener;
import de.guj.ems.mobile.sdk.controllers.IOnAdErrorListener;
import de.guj.ems.mobile.sdk.controllers.IOnAdResizeListener;
import de.guj.ems.mobile.sdk.controllers.IOnAdSuccessListener;
import de.guj.ems.mobile.sdk.util.SdkUtil;
import de.guj.ems.mobile.sdk.util.ThirdPartyConnector;
import de.guj.ems.mobile.sdk.util.YieldLab;
import de.guj.ems.mobile.sdk.views.GuJEMSAdView;
import de.guj.ems.test.app.GlobalData;
import de.guj.ems.test.app.R;
import de.guj.ems.test.app.thirdParty.ExternalTargetingConnector;
import de.guj.ems.test.app.thirdParty.FacebookAudienceConnector;
import de.guj.ems.test.app.util;

public class BannerFragment extends Fragment {

    private FacebookAudienceConnector fb = null;
    private ExternalTargetingConnector et = null;
    private LinearLayout rl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_banner, container, false);
        if (fb == null) {
            fb = new FacebookAudienceConnector(getContext());
        }
        if (et == null) {
            et = new ExternalTargetingConnector(getContext());
        }

        SdkUtil.setContext(getContext());

        ThirdPartyConnector.getInstance().registerCallback(fb);
        ThirdPartyConnector.getInstance().registerCallback(et);

        this.rl = (LinearLayout) rootView.findViewById(R.id.BannerFragmentView);

        rootView.findViewById(R.id.refreshBanner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanUpBanners();
                renderBanners();
            }
        });
        return rootView;
    }

    @Override
    public void onDestroyView() {
        ThirdPartyConnector.getInstance().removeCallback(fb);
        ThirdPartyConnector.getInstance().removeCallback(et);
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        this.cleanUpBanners();
        this.renderBanners();
        super.onResume();
    }

    private void cleanUpBanners() {
        this.rl.removeAllViewsInLayout();
    }

    private void renderBanners() {
        YieldLab.request();
        try {
//            Button b = new Button(getActivity());
//            b.setText(R.string.FragmentBannerReload);
//            //b.setCol
//            b.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    cleanUpBanners();
//                    renderBanners();
//                }
//            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            params.gravity = Gravity.CENTER_HORIZONTAL;
            //b.setLayoutParams(params);
            //this.rl.addView(b, 0);

            this.rl.addView(this.createTextView(getActivity(), "Top (1)"), 0);
            TextView adMessage1 = this.createTextView(getActivity(), "");
            this.rl.addView(this.createGuJEMSAdView(getActivity(), R.layout.layout_bannertop, 1, adMessage1), 1);
            this.rl.addView(adMessage1, 2);
            this.rl.addView(this.createTextView(getActivity(), "Mid1 (2)"), 3);
            TextView adMessage2 = this.createTextView(getActivity(), "");
            this.rl.addView(this.createGuJEMSAdView(getActivity(), R.layout.layout_bannermd1, 2, adMessage2), 4);
            this.rl.addView(adMessage2, 5);
            this.rl.addView(this.createTextView(getActivity(), "Mid2 (3)"), 6);
            TextView adMessage3 = this.createTextView(getActivity(), "");
            this.rl.addView(this.createGuJEMSAdView(getActivity(), R.layout.layout_bannermd2, 3, adMessage3), 7);
            this.rl.addView(adMessage3, 8);
            this.rl.addView(this.createTextView(getActivity(), "Bottom (10)"), 9);
            TextView adMessage4 = this.createTextView(getActivity(), "");
            this.rl.addView(this.createGuJEMSAdView(getActivity(), R.layout.layout_bannerbottom, 10, adMessage4), 10);
            this.rl.addView(adMessage4, 11);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private GuJEMSAdView createGuJEMSAdView(Context c, int XMLId, int position, final TextView message) {
        final GuJEMSAdView gujView = new GuJEMSAdView(getActivity(), XMLId, false);
        try {
            Boolean index = util.getBooleanSettingByKey(GlobalData.preferenceIndex);
            String adUnitId = util.getStringSettingByKey(GlobalData.preferenceAdUnit);
            gujView.getSettings().addCustomRequestParameter("ind", index ? "yes" : "no");
            gujView.getSettings().addCustomRequestParameter("idx", index ? "true" : "false");
            String kw = util.getStringSettingByKey(GlobalData.preferenceKeywords);
            if (!kw.trim().equals("")) {
                gujView.getSettings().addCustomRequestParameter("kw", kw);
            }
            gujView.setAdUnitId(adUnitId, position);
            this.disableSizes(gujView, position);
            String cUrl = util.getStringSettingByKey(GlobalData.preferenceContentUrl + position);
            if (!cUrl.equals("")) {
                gujView.setContentUrl(cUrl);
            }
            gujView.setOnAdSuccessListener(new IOnAdSuccessListener() {
                @Override
                public void onAdSuccess() {
                }
            });
            gujView.setOnAdEmptyListener(new IOnAdEmptyListener() {
                @Override
                public void onAdEmpty() {
                    message.setTextColor(Color.YELLOW);
                    message.setText("Ad is empty");
                }
            });
            gujView.setOnAdErrorListener(new IOnAdErrorListener() {
                @Override
                public void onAdError(String msg) {
                    message.setTextColor(Color.RED);
                    message.setText("Ad error: " + msg);
                }

                @Override
                public void onAdError(String msg, Throwable t) {
                    message.setTextColor(Color.RED);
                    message.setText("Ad error: " + msg);
                }
            });
            gujView.setOnAdResizeListener(new IOnAdResizeListener() {
                @Override
                public void onResize(double width, double height) {
                    System.out.println("Ad resize event w: " + width + " h: " + height);
                }
            });
            gujView.load();
        } catch (Exception e) {
        }
        return gujView;
    }

    private TextView createTextView(Context c, String content) {
        TextView textView = new TextView(getActivity());
        textView.setText(content);

        return textView;
    }

    private void disableSizes(GuJEMSAdView gujView, int pos) {
        try {
            if (util.getBooleanSettingByKey(GlobalData.preferenceBannerRectangle + pos)) {
                gujView.setNoRectangle(true);
            }
            if (util.getBooleanSettingByKey(GlobalData.preferenceBannerBillboard + pos)) {
                gujView.setNoBillboard(true);
            }
            if (util.getBooleanSettingByKey(GlobalData.preferenceBannerDesktopBillboard + pos)) {
                gujView.setNoDesktopBillboard(true);
            }
            if (util.getBooleanSettingByKey(GlobalData.preferenceBannerLeaderboard + pos)) {
                gujView.setNoLeaderboard(true);
            }
            if (util.getBooleanSettingByKey(GlobalData.preferenceBannerThreeOnOne + pos)) {
                gujView.setNoThreeToOne(true);
            }
            if (util.getBooleanSettingByKey(GlobalData.preferenceBannerTwoOnOne + pos)) {
                gujView.setNoTwoToOne(true);
            }
        } catch (Exception e) {
        }
    }
}