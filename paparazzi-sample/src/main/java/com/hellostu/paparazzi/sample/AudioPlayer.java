package com.hellostu.paparazzi.sample;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;

import com.hellostu.paparazzi.annotations.Listener;

import java.io.IOException;

/**
 * Created by stuartlynch on 23/05/16.
 */
public class AudioPlayer {

    ///////////////////////////////////////////////////////////////
    // interface: OnProgressListener
    ///////////////////////////////////////////////////////////////

    @Listener
    public interface OnProgressListener {
        void onProgress(int progress);
    }

    ///////////////////////////////////////////////////////////////
    // interface: OnStateChangedListener
    ///////////////////////////////////////////////////////////////

    @Listener
    public interface OnStateChangedListener {
        void onStateChanged(State state);
    }

    ///////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////

    private Handler     progressHandler;
    private boolean     observingAudioProgress = false;

    private Context     context;
    private MediaPlayer mediaPlayer;

    private StateManager stateManager = new StateManager();
    private OnProgressListeners onProgressListeners = new OnProgressListeners();

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public AudioPlayer(Context context) {
        this.context = context;
        this.progressHandler = new Handler();

        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnPreparedListener(onPreparedListener);
        this.mediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
    }

    ///////////////////////////////////////////////////////////////
    // Public Properties
    ///////////////////////////////////////////////////////////////

    public void addOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        stateManager.onStateChangedListeners.addListener(onStateChangedListener);
    }

    public void removeOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        stateManager.onStateChangedListeners.removeListener(onStateChangedListener);
    }

    public void addOnProgressListener(OnProgressListener onProgressListener) {
        onProgressListeners.addListener(onProgressListener);
    }

    public void removeOnProgressListener(OnProgressListener onProgressListener) {
        onProgressListeners.removeListener(onProgressListener);
    }

    public State getState() {
        return stateManager.getState();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getProgress() {
        return mediaPlayer.getCurrentPosition();
    }

    ///////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////

    public void load(Uri uri) {
        try {
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.prepareAsync();
            stateManager.setState(State.PREPARING_TO_PAUSE);
        } catch (IOException e) {
            e.printStackTrace();
            stateManager.setState(State.EMPTY);
        }
    }

    public void play() {
        switch (getState()) {
            case PAUSED:
                mediaPlayer.start();
                stateManager.setState(State.PLAYING);
                break;
            case PREPARING_TO_PAUSE:
                stateManager.setState(State.PREPARING_TO_PLAY);
                break;
            case PLAYING:
            case PREPARING_TO_PLAY:
            case EMPTY:
                //LEAVE STATE UNCHANGED
        }
    }

    public void pause() {
        switch (getState()) {
            case PLAYING:
                mediaPlayer.pause();
                stateManager.setState(State.PAUSED);
                break;
            case PREPARING_TO_PLAY:
                stateManager.setState(State.PREPARING_TO_PAUSE);
                break;
            case PAUSED:
            case PREPARING_TO_PAUSE:
            case EMPTY:
                //LEAVE STATE UNCHANGED
        }
    }

    public void seek(int seekTo) {
        switch (getState()) {
            case PLAYING:
                stateManager.setState(State.PREPARING_TO_PLAY);
                break;
            case PAUSED:
                stateManager.setState(State.PREPARING_TO_PAUSE);
                break;
            case EMPTY:
                return;
        }

        mediaPlayer.seekTo(seekTo);
    }

    ///////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////

    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if(observingAudioProgress) {
                progressHandler.postDelayed(progressRunnable, 500);
                onProgressListeners.onProgress(mediaPlayer.getCurrentPosition());
            }
        }
    };

    private void startObservingProgress() {
        if(observingAudioProgress) { return; }

        observingAudioProgress = true;
        progressHandler.post(progressRunnable);
    }

    private void stopObservingProgress() {
        observingAudioProgress = false;
        progressHandler.removeCallbacksAndMessages(null);
    }

    private void onStateChanged(State state) {
        switch (state) {
            case PLAYING:
                startObservingProgress();
                break;
            case PAUSED:
            case EMPTY:
                stopObservingProgress();
                break;
        }
    }

    ///////////////////////////////////////////////////////////////
    // Listeners
    ///////////////////////////////////////////////////////////////

    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            onFinishedLoading();
        }
    };

    private MediaPlayer.OnSeekCompleteListener onSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            onFinishedLoading();
        }
    };

    private void onFinishedLoading() {
        switch (stateManager.getState()) {
            case PREPARING_TO_PLAY:
            case PLAYING:
                mediaPlayer.start();
                stateManager.setState(State.PLAYING);
                break;
            case PAUSED:
            case PREPARING_TO_PAUSE:
                mediaPlayer.pause();
                stateManager.setState(State.PAUSED);
                break;
            case EMPTY:
                //Unexpected state here, but leave as it is
        }
    }

    ///////////////////////////////////////////////////////////////
    // enum: State
    ///////////////////////////////////////////////////////////////

    public enum State {
        PLAYING,
        PAUSED,
        PREPARING_TO_PLAY,
        PREPARING_TO_PAUSE,
        EMPTY
    }

    private class StateManager {

        private State state = State.EMPTY;
        private OnStateChangedListeners onStateChangedListeners = new OnStateChangedListeners();

        public State getState() {
            return state;
        }

        public void setState(State state) {
            if(state == this.state) { return; }
            this.state = state;
            onStateChanged(state);
            onStateChangedListeners.onStateChanged(state);
        }
    }

}
