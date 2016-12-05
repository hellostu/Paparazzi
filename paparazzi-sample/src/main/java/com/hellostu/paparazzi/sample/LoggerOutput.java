package com.hellostu.paparazzi.sample;

import com.hellostu.paparazzi.Listener;

/**
 * Created by stuartlynch on 05/12/2016.
 */

@Listener
public interface LoggerOutput {
    void write(String string);
}