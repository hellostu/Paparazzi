package com.hellostu.paparazzi.sample.listeners;

import com.hellostu.paparazzi.Listener;

@Listener
public interface OnProgressListener {
    void onProgress(int progress);
}