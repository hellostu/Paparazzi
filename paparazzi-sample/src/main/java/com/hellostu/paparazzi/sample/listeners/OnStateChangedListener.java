package com.hellostu.paparazzi.sample.listeners;

import com.hellostu.paparazzi.Listener;
import com.hellostu.paparazzi.sample.AudioPlayer;

@Listener
public interface OnStateChangedListener {
    void onStateChanged(AudioPlayer.State state);
}