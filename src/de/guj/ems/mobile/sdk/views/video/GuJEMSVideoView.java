package de.guj.ems.mobile.sdk.views.video;

import java.util.ArrayList;
import java.util.List;

import de.guj.ems.mobile.sdk.controllers.video.IVideoPlayer;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Videoview with player with ads after IMA3 reference implementation
 *  
 * @author stein16
 *
 */
public class GuJEMSVideoView extends VideoView implements IVideoPlayer {
	
	private enum PlaybackState {
        STOPPED, PAUSED, PLAYING
    }

    private MediaController mMediaController;
    private PlaybackState mPlaybackState;
    private final List<PlayerCallback> mVideoPlayerCallbacks = new ArrayList<PlayerCallback>(1);	

	public GuJEMSVideoView(Context context) {
		super(context);
		init();
	}

	public GuJEMSVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GuJEMSVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

    private void init() {
        if (isInEditMode()) {
        	ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(320, 240);
        	setLayoutParams(lp);
        }
        else {
	    	mPlaybackState = PlaybackState.STOPPED;
	        mMediaController = new MediaController(getContext());
	        mMediaController.setAnchorView(this);
	        enablePlaybackControls();
	
	        // Set OnCompletionListener to notify our callbacks when the video is completed.
	        super.setOnCompletionListener(new OnCompletionListener() {
	
	            @Override
	            public void onCompletion(MediaPlayer mediaPlayer) {
	                // Reset the MediaPlayer.
	                // This prevents a race condition which occasionally results in the media
	                // player crashing when switching between videos.
	                disablePlaybackControls();
	                mediaPlayer.reset();
	                mediaPlayer.setDisplay(getHolder());
	                enablePlaybackControls();
	                mPlaybackState = PlaybackState.STOPPED;
	
	                for (PlayerCallback callback : mVideoPlayerCallbacks) {
	                    callback.onCompleted();
	                }
	            }
	        });
	
	        // Set OnErrorListener to notify our callbacks if the video errors.
	        super.setOnErrorListener(new OnErrorListener() {
	
	            @Override
	            public boolean onError(MediaPlayer mp, int what, int extra) {
	                mPlaybackState = PlaybackState.STOPPED;
	                for (PlayerCallback callback : mVideoPlayerCallbacks) {
	                    callback.onError();
	                }
	
	                // Returning true signals to MediaPlayer that we handled the error. This will
	                // prevent the completion handler from being called.
	                return true;
	            }
	        });
        }
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        // The OnCompletionListener can only be implemented by SampleVideoPlayer.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        // The OnErrorListener can only be implemented by SampleVideoPlayer.
        throw new UnsupportedOperationException();
    }

    // Methods implementing the VideoPlayer interface.
    @Override
    public void play() {
        start();
    }

    @Override
    public void start() {
        super.start();
        switch (mPlaybackState) {
            case STOPPED:
                for (PlayerCallback callback : mVideoPlayerCallbacks) {
                    callback.onPlay();
                }
                break;
            case PAUSED:
                for (PlayerCallback callback : mVideoPlayerCallbacks) {
                    callback.onResume();
                }
                break;
            default:
                // Already playing; do nothing.
                break;
        }
        mPlaybackState = PlaybackState.PLAYING;
    }

    @Override
    public void pause() {
        super.pause();
        mPlaybackState = PlaybackState.PAUSED;
        for (PlayerCallback callback : mVideoPlayerCallbacks) {
            callback.onPause();
        }
    }

    @Override
    public void stopPlayback() {
        super.stopPlayback();
        mPlaybackState = PlaybackState.STOPPED;
    }

    @Override
    public void seekTo(int msec) {
        for (PlayerCallback callback : mVideoPlayerCallbacks) {
            callback.onSeekTo(msec);
        }
        //Call super at the end because in most cases the listener wants to capture the current position
        //before the seekTo method is executed, to calculate the delta to detect direction (backward / forward).
        super.seekTo(msec);
    }

    @Override
    public void disablePlaybackControls() {
        setMediaController(null);
    }

    @Override
    public void enablePlaybackControls() {
        setMediaController(mMediaController);
    }

    @Override
    public void addPlayerCallback(PlayerCallback callback) {
        mVideoPlayerCallbacks.add(callback);
    }

    @Override
    public void removePlayerCallback(PlayerCallback callback) {
        mVideoPlayerCallbacks.remove(callback);
    }
}
