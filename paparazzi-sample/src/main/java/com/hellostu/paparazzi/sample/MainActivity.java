package com.hellostu.paparazzi.sample;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.audio_button)    Button      audioButton;
    @BindView(R.id.seek_bar)        SeekBar     seekBar;
    @BindView(R.id.logger_view)     LoggerView  loggerView;

    private AudioPlayer audioPlayer;
    private boolean     userIsSeeking = false;

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SampleApplication application = (SampleApplication) getApplication();

        audioPlayer = application.getAudioPlayer();
        audioPlayer.addOnStateChangedListener(onStateChangedListener);
        audioPlayer.addOnProgressListener(onProgressListener);
        onStateChangedListener.onStateChanged(audioPlayer.getState());

        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        application.getLogger().addOutput(new Logger.Output() {
            @Override
            public void write(String string) {
                loggerView.writeLog(string);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioPlayer.removeOnStateChangedListener(onStateChangedListener);
        audioPlayer.removeOnProgressListener(onProgressListener);
    }

    @OnClick(R.id.audio_button)
    public void clickAudioButton(Button button) {
        switch (audioPlayer.getState()) {
            case PLAYING:
            case PREPARING_TO_PLAY:
                audioPlayer.pause();
                break;
            case PAUSED:
            case PREPARING_TO_PAUSE:
                audioPlayer.play();
                break;
            case EMPTY:
                audioPlayer.load(Uri.parse("https://archive.org/download/testmp3testfile/mpthreetest.mp3"));
                audioPlayer.play();
                break;
        }
    }

    ///////////////////////////////////////////////////////////////
    // SeekBar.OnSeekBarChangedListener
    ///////////////////////////////////////////////////////////////

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            userIsSeeking = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            userIsSeeking = false;
            audioPlayer.seek(seekBar.getProgress());
        }
    };

    ///////////////////////////////////////////////////////////////
    // AudioPlayer.OnStateChangedListener
    ///////////////////////////////////////////////////////////////

    private AudioPlayer.OnStateChangedListener onStateChangedListener = new AudioPlayer.OnStateChangedListener() {
        @Override
        public void onStateChanged(AudioPlayer.State state) {
            switch (audioPlayer.getState()) {
                case PLAYING:
                case PREPARING_TO_PLAY:
                    audioButton.setText("PAUSE");
                    break;
                case PAUSED:
                case PREPARING_TO_PAUSE:
                    audioButton.setText("PLAY");
                    break;
                case EMPTY:
                    audioButton.setText("LOAD CONTENT");
                    break;
            }
            seekBar.setMax(audioPlayer.getDuration());
        }
    };

    ///////////////////////////////////////////////////////////////
    // AudioPlayer.OnProgressListener
    ///////////////////////////////////////////////////////////////

    private AudioPlayer.OnProgressListener onProgressListener = new AudioPlayer.OnProgressListener() {
        @Override
        public void onProgress(int progress) {
            if(userIsSeeking == false) {
                seekBar.setProgress(progress);
            }
        }
    };

}

