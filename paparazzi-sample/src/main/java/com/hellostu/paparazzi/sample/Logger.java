package com.hellostu.paparazzi.sample;

import android.os.Handler;

import com.hellostu.paparazzi.Executor;
import com.hellostu.paparazzi.Listener;
import com.hellostu.paparazzi.sample.listeners.OnProgressListener;
import com.hellostu.paparazzi.sample.listeners.OnStateChangedListener;

/**
 * Created by stuartlynch on 23/05/16.
 */
public class Logger {

    private AudioPlayer audioPlayer;
    private Executor executor = new Executor() {
        private Handler handler = new Handler();
        @Override
        public void execute(Runnable runnable) {
            handler.post(runnable);
        }
    };
    private LoggerOutputs outputs = new LoggerOutputs(executor);

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public Logger(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.audioPlayer.addOnStateChangedListener(onStateChangedListener);
        this.audioPlayer.addOnProgressListener(onProgressListener);
    }

    public void destroy() {
        this.audioPlayer.removeOnStateChangedListener(onStateChangedListener);
        this.audioPlayer.addOnProgressListener(onProgressListener);
    }

    ///////////////////////////////////////////////////////////////
    // Public Properties
    ///////////////////////////////////////////////////////////////

    public void addOutput(LoggerOutput output) {
        outputs.addLoggerOutput(output);
    }

    public void removeOutput(LoggerOutput output) {
        outputs.removeLoggerOutput(output);
    }

    ///////////////////////////////////////////////////////////////
    // AudioPlayer.OnStateChangedListener
    ///////////////////////////////////////////////////////////////

    private OnStateChangedListener onStateChangedListener = new OnStateChangedListener() {
        @Override
        public void onStateChanged(AudioPlayer.State state) {
            switch (state) {
                case PLAYING:
                    outputs.write("Began Playing");
                    break;
                case PREPARING_TO_PAUSE:
                    outputs.write("Began Preparing to Pause");
                    break;
                case PREPARING_TO_PLAY:
                    outputs.write("Began Preparing to Play");
                    break;
                case PAUSED:
                    outputs.write("Paused");
                    break;
                case EMPTY:
                    outputs.write("Player went into empty state");
                    break;
            }
        }
    };

    private OnProgressListener onProgressListener = new OnProgressListener() {
        @Override
        public void onProgress(int progress) {
            outputs.write("Progressed to " + progress + " milliseconds");
        }
    };

}
