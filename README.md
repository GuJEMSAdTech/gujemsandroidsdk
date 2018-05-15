﻿# gujemsandroidsdk

**Latest Version 2.2.2**

## Requirements

The SDK supports **Android 4.0 (Android SDK Version 14) and higher**.
The Example App requires Android SDK Version 18 or higher.
Language Support: **Java**  

## Changelog

- YieldLab Banner Ad Integration

## New Features since 2.1.91

- Smartclip removed
- setNonPersonalizedAdsStatus

## Installation

The SDK is available as a maven library over jcenter.

1. In your project level build.gradle file enable jcenter.

```
allprojects {
    repositories {
        jcenter()
    }
}
```

2. In your module/app level build.gradle file include the gujemssdk as dependency.

```
dependencies {
       compile 'com.gujems.android:gujemssdk:2.2.0'
       [...]
}
```

## Options

The SdkUtil instance provides global options for the sdk:

-NonPersonalizedAdsStatus with setNonPersonalizedAdsStatus method

## Usage
If you are interested in upgrading the SDK please move forward to Chapter "Upgrading from v1.4.x to v2.1.x".
In this chapter we will show you how to add banner ads, interstitial ads and video ads to your android app.

Before you start with the implementation please make sure that you received your Ad Unit Id. 

# Example App

Every feature of this SDK is showcased inside the [exampleApp](../exampleApp).

## Initialization
First of all set the context of the SDKUtil from your MainActivity class, which guarantees the functionality of the GuJEMSSDK.
```java
/*
* MainActivity class 
*/
@Override
protected void onCreate(Bundle savedInstanceState) {
        [...]
        SdkUtil.setContext(getApplicationContext()); 
}
```

### Banner Ad
You have two possibilities to add one or more banner ads to your app. The first way is by adding a view to your layout file.
```xml
<de.guj.ems.mobile.sdk.views.GuJEMSAdView
        android:id="@+id/banner_top" 
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        ems:ems_onAdEmpty="onAdEmpty"
        ems:ems_onAdError="onAdError"
        ems:ems_onAdSuccess="onAdSuccess"
        ems:ems_adUnit="sdktest,1,no" />
```
To get the ems namespace add the following attribute to your Root-View:
```xml
xmlns:ems="http://schemas.android.com/apk/res/[your package path]"
```
Let's have a look on the attributes for GuJEMSAdView:
- ems_onAdEmpty : Method which is called, when no ad was found.
- ems_onAdError : Method which is called, when an error occured
- ems_onAdSuccess : Method which is called, when an ad is loaded successfully
- ems_adUnit : [your Ad Unit Id], [Ad Position], [Is index page?]
- ems_geo : **[true / false]** Add the current geo location if it exists
- ems_kw : **[true / false]** Allow transmission of keywords 
- ems_noRectangle, ems_noBillboard, ems_no[...] : Block special ad sizes for this ad slot

You also have the possibility to add a banner ad programmatically:

- Create a separate layout file for your banner ad 
- Add a GuJEMSAdView to the file. Example:
```xml
<?xml version="1.0" encoding="utf-8"?>
<de.guj.ems.mobile.sdk.views.GuJEMSAdView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:id="@+id/banner_top">
</de.guj.ems.mobile.sdk.views.GuJEMSAdView>
```
- Create the view in your code. Example: 

```java
/* initialize the GujEMSAdView */
GuJEMSAdView gujView = new GuJEMSAdView(getApplicationContext(), R.id.banner_top, false);
/* set Ad Unit Id and position */
gujView.setAdUnitId("sdktest", 1);
/* Is Index Page? */
gujView.getSettings().addCustomRequestParameter("ind", "no");
/* set event listener call when ad is empty */
gujView.setOnAdEmptyListener(new IOnAdEmptyListener() {
    @Override
    public void onAdEmpty() {
        /* Do something */
    }
});
/* set event listener called when error occured */
gujView.setOnAdErrorListener(new IOnAdErrorListener() {
    @Override
    public void onAdError(String msg) {
        /* Do something */
    }

    @Override
    public void onAdError(String msg, Throwable t) {
        /* Do something */
    }
});
/* block rectangle */
gujView.setNoRectangle(true);
/* set Content URL for Targeting */
gujView.setContentUrl("http://gujmedia.de");
/* load ad */
gujView.load();
/* add adView to Layout */
addView(gujView);
```

**OnAdResizeEvent**

You can now listen on resize events for banner ads:

````java
gujView.setOnAdResizeListener(new IOnAdResizeListener() {
    @Override
    public void onResize(double width, double height) {
        System.out.println("Ad resize event w: " + width + " h: " + height);
    }
});
````



#####YieldLab Banner Ad Integration

If you want to use YieldLab with your banner ad's, you have to add following initialisation as early as
possible in your app. Following YieldLab Ids are only examples and have to be changed.

````java
HashMap<String, String> ylMap = new HashMap<String, String>();
ylMap.put("aaa", "1111111");
ylMap.put("bbb", "2222222");
ylMap.put("ccc", "3333333");
ylMap.put("ddd", "4444444");
ylMap.put("eee", "5555555");
YieldLab.init(ylMap, 4);
````

In addition to that, following code has to be run before a series of ad-calls. For example after
a new view(app-page) is creaed.

````java
YieldLab.request();
````

### Interstitial Ad
**The interstitial ad can only create programmatically.**
Let's have a look on an example:

```java
Intent target = new Intent(getApplicationContext(), <TARGET-ACTIVITY>);
Intent i = new Intent(getActivity(),
        InterstitialSwitchReceiver.class);
/* set Ad Unit Id */
i.putExtra("adUnitId", "sdktest");
/* set keywords */
i.putExtra("ems_kw", "keyword1, keyword2");
/* initialize an ListenerEvents object -> class wihich implements IOnAdEmptyListener, etc.) */
ListenerEvents le = new ListenerEvents(); 
/* set event listener */
i.putExtra("ems_onAdSuccess", le);
i.putExtra("ems_onAdEmpty", le);
i.putExtra("ems_onAdError", le);
/* set target activity */
i.putExtra("target", target);
/* open interstitial */
getActivity().sendBroadcast(i);
```

If the intent “target” is not set, the interstitial closes itself and returns to the previous view. The Class ListenerEvents isn't part of the SDK.

### Video Ad
Please have a look at the chapter "Video Advertising".

### Inflow Ad
Please have a look at the chapter "InFlow Ad"

## Upgrading from v1.4.x to v2.1.x

If you are not upgrading please contact us for an additional update you will need.

If you previously used version 1.4.x of this SDK there are several important changes you need to pay attention to.

Under the hood we exchanged the Amobee Ad Server with Googles DoubleClick for Publishers (DFP).

Also we did some cleanup to make the SDK better understandable.

#### Remove old SDK installation

First step during upgrade from 1.4.x is to remove all libraries and files belonging to the old SDK installation.
This includes all libraries and manifest files.

Then add the new SDK extract the archive to your workspace and import the existing project.

#### Removed layout attributes which are no longer supported

```xml
ems:ems_nkw <!-- Negative keywords are no longer supported -->
ems:ems_uid <!-- Advertiser ID transmission is handled by Google -->
ems:ems_bfSiteId <!-- The backfill site ID is no longer supported -->
ems:ems_bfZoneId <!-- The backfill zone ID is no longer supported -->
ems:ems_gPubId <!-- The Google publisher ID is no longer supported -->
```

#### Removed all drawables (for now)

All close buttons etc. are handled by the Google SDK 

#### Test mode has been removed

For now the test mode has been removed. We will reintroduce default Google test ads in the next release.

#### Removed custom views

GuJEMSNativeAdView is no longer supported, all display views are handled by the Google SDK
GuJEMSNativeListAdView is no longer supported, all display views are handled by the Google SDK
org.ormma.Browser no longer exists since we have moved on to MRAID

#### Interstitial target has been removed

Providing a target activity / intent to an interstitial is no longer supported. Interstitials will be shown as soon as they are loaded and overlay.

#### Video Interstitial has been removed

Google interstitials are capable of displaying videos by themselves.

#### Other changes

Other than the changes mentioned the SDK also incorporates an internal XML file which maps the old ems_zoneId to a new Google doubleclick identifier. This allows the developer to keep the existing implementation and not having to adjust to Google specific features.

#### New video advertising classes

The experimental use of video interstitials as a preroll player has been removed. See [Video Advertising] (#video) on how to utilize the integrated video player

#### GuJEMSAdView.setGooglePublisherId is deprecated

The Google publisher ID no longer needs to be set manually

#### Android Marshmallow permission management

All permissions that may be revoked by the user are optional and checked for. Your app should work flawlessly in case a user removes, for example, the location permission.

#### Manifest permissions

All SDK permissions other than location and networking have been commented out in the manifest. Please check the manifest if you previously had certain permissions set, e.g. for camera access or vibration

#### Ad sizes

By default the GuJEMSAdViews accept all feasible ad sizes. You can block large ad sizes by either:

- Adding layout attributes to your view
```xml
	ems:ems_noRectangle="true" <!-- blocks 300x350 ads on smartphones -->
	ems:ems_noBillboard="true" <!-- blocks 1024x220 (landscape) and 768x300 (portrait) ads on tablets -->
	ems:ems_noDesktopBillboard="true" <!-- blocks 800x250 ads on tablets -->
	ems:ems_noLeaderboard="true" <!-- blocks 728x90 and 768x90 ads on both smartphones and tables -->
	ems:ems_noTwoToOne="true" <!-- blocks 300x150 ads on smartphones -->
```
- Programmatically supressing them
```java
	GuJEMSAdView.setNoRectangle(true)
	GuJEMSAdView.setNoBillboard(true)
	GuJEMSAdView.setNoDesktopBillboard(true)
	GuJEMSAdView.setNoLeaderboard(true)
	GuJEMSAdView.setNoTwoToOne(true)
```
#### "as" parameter as custom value

This is no longer supported. Set the zone or adunit with the respective setter method for GuJEMSAdView	


#### Google Ad Exchange

Providing inventory to the Google Ad Exchange for programmatic advertising is handled internally by the SDK and configured by G+J e|MS via the adserver.

<a name="inflow"></a>
## InFlow Ad
Usage only in ScrollViews!

You have two possibilities to add an InFlow to your app. The first way is by adding a view to your layout file.

 ```xml
<de.guj.ems.mobile.sdk.views.video.GuJEMSInFlowView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:id="@+id/InFlowPlayer"
    app:ems_adUnit="">
</de.guj.ems.mobile.sdk.views.video.GuJEMSInFlowView>
 ```
- ems_adUnit : [your InFlow Ad Unit Id]

You also have the possibility to add InFlow ad programmatically:

1. Add the InFlow View to your layout
 ```xml
<de.guj.ems.mobile.sdk.views.video.GuJEMSInFlowView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:id="@+id/InFlowPlayer"
</de.guj.ems.mobile.sdk.views.video.GuJEMSInFlowView>
 ```
2. Add the following code to your App:
```java
GuJEMSInFlowView inflow = (GuJEMSInFlowView)rootView.findViewById(R.id.InFlowPlayer);
/** Change the colour of the sound on/off button and the close button  -> use Hexcode**/
inflow.setColorToButtons("#00a600");
/** Set AdUnit **/
inflow.setAdUnit(adUnitId);
```

## SmartClip

-Removed

## Video Advertising

The new SDK comes with the current beta version of the Google IMA3 SDK for Android. A player capable of displaying ads from G+J e|MS is included as well as a view displaying videos with ads: GuJEMSVideoView. Both the view and the player are based on the IMA3 reference implementation.

### Here's how to incorporate the player

In your activity's or fragment's layout include the player like this
```xml
    <de.guj.ems.mobile.sdk.views.video.GuJEMSVideoPlayer
        android:id="@+id/ems_sample_video_player"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="center"
        android:gravity="center"
        ems:ems_adUnit="[provided_adUnit]">
```

[provided_adUnit] will be a string you receive from G+J e|MS - it reflects the app's name or category in the app where video ads should be displayed when playing video. If you are unable to set the adUnit via xml, here's the programmatic way to do it 

```java
	// get player view
	mVideoPlayerWithAdPlayback = (GuJEMSVideoPlayer)findViewById(R.id.ems_sample_video_player);

	// set ad unit
	mVideoPlayerWithAdPlayback.setAdUnit("stern/panorama");
```

In your activity or fragment tell the player which content video to load

```java
	setContentView(R.layout.video_sample);
		
	// get player view
	mVideoPlayerWithAdPlayback = (GuJEMSVideoPlayer)findViewById(R.id.ems_sample_video_player);

	// specifiy conent video url
	mVideoPlayerWithAdPlayback.getVideoPlayerController().setContentVideo(testContentUrl);
```

Add callbacks to your video player like this

```java
	// add callback
	mVideoPlayerWithAdPlayback.setOnContentCompleteListener(new OnContentCompleteListener() {

		@Override
		public void onContentComplete() {
			SdkLog.d(TAG, "onContentComplete");
			finish();
		}

	});
```

Tell the player to play and request ads like this

```java
	mVideoPlayerWithAdPlayback.requestAndPlayAds();
```

Depending on the lifecyle of your avctivity / fragment, add these

```java
    @Override
    public void onResume() {
       mVideoPlayerWithAdPlayback.resume();
       super.onResume();
    }
    
    @Override
    public void onPause() {
    	mVideoPlayerWithAdPlayback.pause();
        super.onPause();
    }
```

Everything else is handled via G+J e|MS and the respective adserver.


##AutoNative Ads

From Version 2.1.7 and onwards you can use AutoNative ads in your app.
Our implementation for AutoNative ads allows you to freely style and structure your AutoNative ads.

####How does it work?

Android provides the functionality to give every view a tag attribute. Within our sdk there are
5 different types of tags related to AutoNative:

1. Teaser Tag for an ImageView: **ems_auto_native_teaser**
2. Headline Tag for a TextView: **ems_auto_native_headline**
3. SubHeadline Tag for a TextView: **ems_auto_native_sub_headline**
4. TeaserText Tag for a TextView: **ems_auto_native_teaser_text**
5. AdLabel Tag for a generic View: **ems_auto_native_ad_label**

For example, you can give a TextView the Headline Tag

```xml
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:tag="ems_auto_native_headline"/>
```           

Our sdk provides a view/layout with the neccessary information, if it uses our predefined tags.
In the example above, the TextView's text would be set to the headline of the AutoNative ad.
To use AutoNative ads within your app, you have to do two distinct steps.

1. Create a layout for your AutoNative ad and place all or some of the four tags previously
mentioned on the corresponding views. The only restriction is that you can use every tag only one
time per layout. Example of an AutoNative Layout with every possible tag:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/autonative1"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="#DDDDDD"
    android:gravity="center_horizontal"
    android:orientation="vertical"
    android:padding="10dp"
    android:visibility="visible">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:tag="ems_auto_native_ad_label"
        android:background="@android:color/white"
        android:padding="2px"
        android:text="Advertisement" />
    <ImageView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:adjustViewBounds="true"
        android:tag="ems_auto_native_teaser" />
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:gravity="center_horizontal"
        android:tag="ems_auto_native_headline"
        android:textSize="26sp" />
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:gravity="center_horizontal"
        android:tag="ems_auto_native_sub_headline"
        android:textSize="20sp" />
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:gravity="center_horizontal"
        android:tag="ems_auto_native_teaser_text"
        android:textSize="12sp" />
</LinearLayout>
```  

Very important is the correct mapping of tags and views. The **ems_auto_native_teaser** tag
has to be placed inside an ImageView, every other tag inside a TextView.

2. Initializing the AutoNativeTask. In order to insert the necessary information into the
AutoNative ad, you have to instantiate an AutoNativeLoader. This loader notifies
the GUJEmsSDK to insert the AutoNative ad informations into the views - this initialization mechanism
is an asynchronous operation. The code for this process looks like this:

```java
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_autonative, container, false);
        url = "http://www.your-domain.com";
        SdkUtil.setAutonativeBaseUrl(url);
        int correlator = SdkUtil.getCorrelator();
        this.createView(11, 1, correlator, rootView.findViewById(R.id.autonative1));
        this.createView(12, 2, correlator, rootView.findViewById(R.id.autonative2));
        return rootView;
    }

    private void createView(int pos, int tile, int correlator, View view) {
        try {
            AutoNativeConfig autoNativeConfig = new AutoNativeConfig(
                    AutoNativeConfig.TeaserAspectRatio.OneToOne,
                    util.getStringSettingByKey(GlobalData.preferenceAdUnit),
                    pos,
                    tile,
                    correlator
            );
            new AutoNativeLoader(autoNativeConfig, getContext(), view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```

First you have to set the base URL for your app. This is important for the click url, which leads
the user to the linked article. After that you have to create an AutoNativeConfig object. This configuration
expects five arguments.
1. TeaserAspectRatio: possible formats are 1:1, 2:1, 2:3.
2. AdUnitId which is able to deliver AutoNative ads.
3. Position: Ad position 
4. Tile: number to indicate consecutive AutoNative ads within one "page/view". If for example
three AutoNative ads are present on one "page/view", the first ad gets the Tile value 1, the second 2
and the third 3. The tile number is used to prevent multiple instances of the same ad.
5. Correlator: long random number. indicates a correlation of ad calls. Every sequence of related AutoNative ad calls
(on the same page) should have the same correlator. You can use our convenience method SdkUtil.getCorrelator().

The last step to get a working AutoNative ad is to instantiate the AutoNativeTaskInitializer. This
object expects the **AutoNativeConfig**, the context and the view which contains the 
previously mentioned AutoNative tags. In this case we use the example AutoNative layout from above,
with the id **R.id.autonative1**.

####AutoNative AdLabel

Every AutoNative Ad has a flag, which indicates if an Ad Label should be presented to the user or not.
 You can map your individually styled Ad Label to our Sdk with the **ems_auto_native_ad_label** tag. 
 If the Ad Label flag of the request is false, our sdk removes the 
 view containing the tag. If the flag is true, nothing happens and the user can see your Ad Label.

####Error Handling

-**Empty ad request response**: If an autonative ad request returns an empty response (no ad available),
the autonative view gets automatically removed.

##Facebook Audience Integration

An example usage for the Facebook Audience Sdk can be found in our exampleApp under the folder
"thirdParty".