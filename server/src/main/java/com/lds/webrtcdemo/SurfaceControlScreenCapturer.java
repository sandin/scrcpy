package com.lds.webrtcdemo;

import android.content.Context;

import org.webrtc.CapturerObserver;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;

public class SurfaceControlScreenCapturer implements VideoCapturer {

    @Override
    public void initialize(SurfaceTextureHelper surfaceTextureHelper, Context context, CapturerObserver capturerObserver) {

    }

    @Override
    public void startCapture(int i, int i1, int i2) {

    }

    @Override
    public void stopCapture() throws InterruptedException {

    }

    @Override
    public void changeCaptureFormat(int i, int i1, int i2) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isScreencast() {
        return false;
    }
}
