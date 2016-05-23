package com.hellostu.paparazzi.sample;

import com.hellostu.paparazzi.annotations.Listener;

/**
 * Created by stuartlynch on 23/05/16.
 */
public class Logger {

    private AudioPlayer audioPlayer;
    private Outputs outputs = new Outputs();

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

    public void addOutput(Output output) {
        outputs.addListener(output);
    }

    public void removeOutput(Output output) {
        outputs.removeListener(output);
    }

    ///////////////////////////////////////////////////////////////
    // AudioPlayer.OnStateChangedListener
    ///////////////////////////////////////////////////////////////

    private AudioPlayer.OnStateChangedListener onStateChangedListener = new AudioPlayer.OnStateChangedListener() {
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

    private AudioPlayer.OnProgressListener onProgressListener = new AudioPlayer.OnProgressListener() {
        @Override
        public void onProgress(int progress) {
            outputs.write("Progressed to " + progress + " milliseconds");
        }
    };

    ///////////////////////////////////////////////////////////////
    // interface: Output
    ///////////////////////////////////////////////////////////////

    @Listener
    public interface Output {
        void write(String string);
    }

}
