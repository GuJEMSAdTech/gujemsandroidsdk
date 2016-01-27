package de.guj.ems.mobile.sdk.views.video;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;

import de.guj.ems.mobile.sdk.R;
import de.guj.ems.mobile.sdk.controllers.video.IVideoPlayer;
import de.guj.ems.mobile.sdk.controllers.video.VideoPlayerController;
import de.guj.ems.mobile.sdk.util.SdkLog;

/**
 * Videoplayer with ads after IMA3 Android SDK reference implementation
 * 
 * @author stein16
 *
 */
public class GuJEMSVideoPlayer extends RelativeLayout {

	private final static String TAG = "GuJEMSVideoPlayer";

	public interface OnContentCompleteListener {
		public void onContentComplete();
	}

	// The wrapped video player.
	private IVideoPlayer mVideoPlayer;

	// The SDK will render ad playback UI elements into this ViewGroup.
	private ViewGroup mAdUiContainer;

	// Used to track if the current video is an ad (as opposed to a content
	// video).
	private boolean mIsAdDisplayed;

	// Used to track the current content video URL to resume content playback.
	private String mContentVideoUrl;

	// The saved position in the content to resume to after ad playback.
	private int mSavedVideoPosition;

	// Called when the content is completed.
	private OnContentCompleteListener mOnContentCompleteListener;

	// VideoAdPlayer interface implementation for the SDK to send ad play/pause
	// type events.
	private VideoAdPlayer mVideoAdPlayer;

	// ContentProgressProvider interface implementation for the SDK to check
	// content progress.
	private ContentProgressProvider mContentProgressProvider;
	
	// Used to request ads via IMA3 plugin
	protected String mAdTagUrl;
	
	private VideoPlayerController mVideoPlayerController;
	
	private View mPlayButton;
	
	private transient Context mContext;
	
	private boolean mAutoPlayAds = true;

	private final List<VideoAdPlayer.VideoAdPlayerCallback> mAdCallbacks = new ArrayList<VideoAdPlayer.VideoAdPlayerCallback>(
			1);

	private final VideoPlayerController.Logger videoLogger = new VideoPlayerController.Logger() {
		@Override
		public void log(String logMessage) {
			SdkLog.d(TAG, logMessage);
		}
	};
	
	public GuJEMSVideoPlayer(Context context) {
		super(context);
		((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ems_video_player, this, true);
		mContext = context;		
	}

	public GuJEMSVideoPlayer(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ems_video_player, this, true);
		TypedArray tVals = context
				.obtainStyledAttributes(attrs,
						R.styleable.GuJEMSVideoPlayer);
		if (tVals.getString(R.styleable.GuJEMSVideoPlayer_ems_adUnit) != null) {
			setAdUnit(tVals.getString(R.styleable.GuJEMSVideoPlayer_ems_adUnit));
		}
		
		mAutoPlayAds = tVals.getBoolean(R.styleable.GuJEMSVideoPlayer_ems_autoPlayAds, true);
		
		tVals.recycle();
	}

	public GuJEMSVideoPlayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ems_video_player, this, true);
		TypedArray tVals = context
				.obtainStyledAttributes(attrs,
						R.styleable.GuJEMSVideoPlayer);
		if (tVals.getString(R.styleable.GuJEMSVideoPlayer_ems_adUnit) != null) {
			setAdUnit(tVals.getString(R.styleable.GuJEMSVideoPlayer_ems_adUnit));
		}
		
		mAutoPlayAds = tVals.getBoolean(R.styleable.GuJEMSVideoPlayer_ems_autoPlayAds, true);
		
		tVals.recycle();
	}

	protected void init() {
		mIsAdDisplayed = false;
		mSavedVideoPosition = 0;
		
		mVideoPlayer = (GuJEMSVideoView) findViewById(R.id.ems_video_player_view);
		mAdUiContainer = (ViewGroup) findViewById(
				R.id.ems_video_player_ui);

		// Define VideoAdPlayer connector.
		mVideoAdPlayer = new VideoAdPlayer() {
			@Override
			public void playAd() {
				SdkLog.d(TAG, "Request to play ad.");
				mIsAdDisplayed = true;
				mVideoPlayer.play();
			}

			@Override
			public void loadAd(String url) {
				SdkLog.d(TAG, "Request to load ad [" + url + "]");
				mIsAdDisplayed = true;
				mVideoPlayer.setVideoPath(url);
			}

			@Override
			public void stopAd() {
				SdkLog.d(TAG, "Request to stop ad");
				mVideoPlayer.stopPlayback();
			}

			@Override
			public void pauseAd() {
				SdkLog.d(TAG, "Request to pause ad");
				mVideoPlayer.pause();
			}

			@Override
			public void resumeAd() {
				SdkLog.d(TAG, "Request to resume ad");
				playAd();
			}

			@Override
			public void addCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
				mAdCallbacks.add(videoAdPlayerCallback);
			}

			@Override
			public void removeCallback(
					VideoAdPlayerCallback videoAdPlayerCallback) {
				mAdCallbacks.remove(videoAdPlayerCallback);
			}

			@Override
			public VideoProgressUpdate getAdProgress() {
				if (!mIsAdDisplayed || mVideoPlayer.getDuration() <= 0) {
					return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
				}
				return new VideoProgressUpdate(
						mVideoPlayer.getCurrentPosition(),
						mVideoPlayer.getDuration());
				
			}
			
		};
		
        // When Play is clicked, request ads and hide the button.
        if (mPlayButton != null) {
			mPlayButton.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	                mVideoPlayerController.requestAndPlayAds();
	                view.setVisibility(View.GONE);
	            }
	        });
        }
		
        mVideoPlayerController = new VideoPlayerController(
        		getContext(),
                this,
                mPlayButton,
                mAdUiContainer,
                "DE",
                mAutoPlayAds,
                null,
                videoLogger);

		mContentProgressProvider = new ContentProgressProvider() {
			@Override
			public VideoProgressUpdate getContentProgress() {
				if (mIsAdDisplayed || mVideoPlayer.getDuration() <= 0) {
					return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
				}
				return new VideoProgressUpdate(
						mVideoPlayer.getCurrentPosition(),
						mVideoPlayer.getDuration());
			}
		};

		// Set player callbacks for delegating major video events.
		mVideoPlayer.addPlayerCallback(new GuJEMSVideoView.PlayerCallback() {
			@Override
			public void onPlay() {
				if (mIsAdDisplayed) {
					for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
						callback.onPlay();
					}
				}
			}

			@Override
			public void onPause() {
				if (mIsAdDisplayed) {
					for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
						callback.onPause();
					}
				}
			}

			@Override
			public void onResume() {
				if (mIsAdDisplayed) {
					for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
						callback.onResume();
					}
				}
			}

			@Override
			public void onError() {
				if (mIsAdDisplayed) {
					for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
						callback.onError();
					}
				}
			}

			@Override
			public void onSeekTo(int videoPosition) {
			}

			@Override
			public void onCompleted() {
				if (mIsAdDisplayed) {
					for (VideoAdPlayer.VideoAdPlayerCallback callback : mAdCallbacks) {
						callback.onEnded();
					}
				} else {
					// Alert an external listener that our content video is
					// complete.
					if (mOnContentCompleteListener != null) {
						mOnContentCompleteListener.onContentComplete();
					}
				}
			}
		});

	}

	public void setOnContentCompleteListener(
			OnContentCompleteListener onContentCompleteListener) {
		mOnContentCompleteListener = onContentCompleteListener;

	}

	/**
	 * Set the path of the video to be played as content.
	 */
	public void setContentVideoPath(String contentVideoUrl) {
		mContentVideoUrl = contentVideoUrl;
	}

	/**
	 * Save the playback progress state of the currently playing video.
	 */
	public void savePosition() {
		mSavedVideoPosition = mVideoPlayer.getCurrentPosition();
	}

	/**
	 * Restore the currently loaded video to its previously saved playback
	 * progress state.
	 */
	public void restorePosition() {
		mVideoPlayer.seekTo(mSavedVideoPosition);
	}

	public void pauseContentForAdPlayback() {
		mVideoPlayer.disablePlaybackControls();
		savePosition();
		mVideoPlayer.stopPlayback();
	}

	public void resumeContentAfterAdPlayback() {
		mIsAdDisplayed = false;
		restorePosition();

		if (mContentVideoUrl == null || mContentVideoUrl.length() < 10) {
			SdkLog.w(TAG, "No content URL specified. [" + mContentVideoUrl + "]");
			return;
		}
		
		mVideoPlayer.setVideoPath(mContentVideoUrl);
		mVideoPlayer.enablePlaybackControls();
		mVideoPlayer.play();
	}

    /**
     * Returns the UI element for rendering video ad elements.
     */
    public ViewGroup getAdUiContainer() {
        return mAdUiContainer;
    }

    /**
     * Returns an implementation of the SDK's VideoAdPlayer interface.
     */
    public VideoAdPlayer getVideoAdPlayer() {
        return mVideoAdPlayer;
    }

    /**
     * Returns if an ad is displayed.
     */
    public boolean getIsAdDisplayed() {
        return mIsAdDisplayed;
    }

    public ContentProgressProvider getContentProgressProvider() {
        return mContentProgressProvider;
    }

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (!isInEditMode()) {
			init();
			if (this.mAdTagUrl != null) {
				mVideoPlayerController.setAdTagUrl(this.mAdTagUrl);
			}
		}
	}

	public String getContentVideoUrl() {
		return mContentVideoUrl;
	}

	public void setContentVideoUrl(String mContentVideoUrl) {
		this.mContentVideoUrl = mContentVideoUrl;
	}

	public void setAdUnit(String adUnit, boolean inStream) {
		this.mAdTagUrl = inStream ? mContext.getString(R.string.ems_videoAdcall).replaceAll("\\[adunit\\]", adUnit) : mContext.getString(R.string.ems_inflowAdcall).replaceAll("\\[adunit\\]", adUnit);
		SdkLog.i(TAG, (inStream ? "In-stream" : "Out-stream") + " DFP video ad call: " + this.mAdTagUrl);
		if (mVideoPlayerController != null) {
			mVideoPlayerController.setAdTagUrl(this.mAdTagUrl);
		}
	}
	
	public void setAdUnit(String adUnit) {
		this.setAdUnit(adUnit, true);
	}
	
	public VideoPlayerController getVideoPlayerController() {
		return mVideoPlayerController;
	}
    
	public void requestAndPlayAds() {
		mVideoPlayerController.requestAndPlayAds();
	}
	
	public void pause() {
		mVideoPlayerController.pause();
	}
	
	public void resume() {
		mVideoPlayerController.resume();
	}	
	
	public VideoView getVideoView() {
		return (VideoView)mVideoPlayer;
	}
    
}