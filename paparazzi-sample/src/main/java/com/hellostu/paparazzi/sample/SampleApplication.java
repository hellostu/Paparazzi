package com.hellostu.paparazzi.sample;

import android.app.Application;
import android.util.Log;

/**
 * Created by stuartlynch on 23/05/16.
 */
public class SampleApplication extends Application implements LoggerOutput {

    private AudioPlayer audioPlayer;
    private Logger logger;

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    @Override
    public void onCreate() {
        super.onCreate();

        audioPlayer = new AudioPlayer(this);
        logger = new Logger(audioPlayer);
        logger.addOutput(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        logger.destroy();
    }

    ///////////////////////////////////////////////////////////////
    // Public Properties
    ///////////////////////////////////////////////////////////////

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public Logger getLogger() {
        return logger;
    }

    ///////////////////////////////////////////////////////////////
    // implementation: Logger.Output
    ///////////////////////////////////////////////////////////////

    @Override
    public void write(String string) {
        Log.i("SampleApplication", string);
    }
}
