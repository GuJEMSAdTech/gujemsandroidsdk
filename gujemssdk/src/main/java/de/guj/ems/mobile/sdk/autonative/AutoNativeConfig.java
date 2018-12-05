package de.guj.ems.mobile.sdk.autonative;

/**
 * Created by proeg on 28.08.2017.
 */

public class AutoNativeConfig {

    public enum TeaserAspectRatio {
        OneToOne("11"),
        TwoToOne("12"),
        ThreeToTwo("13");

        private final String teaserAspectRatioDescription;

        private TeaserAspectRatio(String value) {
            teaserAspectRatioDescription = value;
        }

        public String toString() {
            return teaserAspectRatioDescription;
        }
    }

    private TeaserAspectRatio ar;
    private String adUnit;
    private int tile;
    private long correlator;
    private int position;
    private AutoNativeCustomBrowser browser = null;

    public AutoNativeConfig(TeaserAspectRatio ar, String adUnit, int position, int tile, long correlator) {
        this.ar = ar;
        this.adUnit = adUnit;
        this.position = position;
        this.tile = tile;
        this.correlator = correlator;
    }

    public AutoNativeConfig(TeaserAspectRatio ar, String adUnit, int position, int tile, long correlator, AutoNativeCustomBrowser browser) {
        this.ar = ar;
        this.adUnit = adUnit;
        this.position = position;
        this.tile = tile;
        this.correlator = correlator;
        this.browser = browser;
    }

    public TeaserAspectRatio getTeaserAspectRatio() {
        return ar;
    }

    public String getAdUnit() {
        return adUnit;
    }

    public int getTile() {
        return tile;
    }

    public long getCorrelator() {
        return correlator;
    }

    public int getPosition() {
        return position;
    }

    public AutoNativeCustomBrowser getBrowser() { return browser; }

}
