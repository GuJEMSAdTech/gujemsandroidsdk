# gujemsandroidssdk

## Requirements

The SDK supports **Android 2.2 and higher**.  
Language Support: **Java**  
Use **Android SDK Tools v23 or higher**.
Dependencies **Google Play Services v25 or higher required**

## Installation

The SDK is available as a downloadable Eclipse project. A gradle build archive will be available soon.

## Upgrading from v1.4.x to v2.0.x

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

Google interstitials are capable of displaying video by themselves.

#### Other changes

Other than the changes mentioned the SDK also incorporates an internal XML file which maps the old ems_zoneId to a new Google doubleclick identifier. This allows the developer to keep the existing implementation and not having to adjust to Google specific features.

#### New video advertising classes

The experimental use of video interstitials as a preroll player has been removed. See [Video Advertising] (#video) on how to utilize the integrated video player

#### GuJEMSAdView.setGooglePublisherId is deprecated

The Google publisher ID no longer needs to be set manually

#### Native Content Ads

We are now supporting Google's Native Content Ads. See [Native advertising} (#native) on how to utilize native ads an adjust them to your layout.

#### Android Marshmallow permission management

All permissions that may be revoked by the user are optional and checked for. Your app should work flawlessly in case a user removes, for example, the location permission.

#### Manifest permissions

All SDK permissions other than location adn networking have been commented out in the manifest. Please check the manifest if you previously had certain permissions set, e.g. for camera access or vibration

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

<a name="#video"></a>
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

<a name="#native"></a>
## Native Advertising

The new SDK comes with the current beta version of Google NativeContentAds. These ads are meant to be fully customized and display content in the same style and layout as your app. Native content ads are typically filled with things like a headline, an image, a logo and a call to action.

### Here's how to incorporate native content ads

In your activity's or fragment's layout include the view like this (please not that the view is ONLY capable of displaying native ads and should not replace or be placed instead of normal ad views.

```xml
<de.guj.ems.mobile.sdk.views.GuJEMSNativeContentAdView
	android:id="@+id/ad24757"
	android:layout_width="match_parent"
	android:layout_height="wrap_content"
	ems:ems_adUnit="[provided_adUnit]"
/> 
```

The view accepts all the same additional attributes as GuJEMSAdView. [provided_adUnit] will be a string you receive from G+J e|MS - it reflects the app's name or category in the app where native ads should be displayed. If you are unable to set the adUnit via xml, you can set the ad unit programmatically with setAdUnit.

### Here's how to change the style and layout for the view

The SDK folder contains

R.layout.ems_nativead
R.values.ems_nativead_style

These may be adapted to your needs - as long as non of the views's holding content are removed. You may also create copies for large resolutions devices or unified smartphone/tablet apps.

The SDK will take care of filling the view with the corresponding received from the adserver.