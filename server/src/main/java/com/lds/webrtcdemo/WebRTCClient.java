package com.lds.webrtcdemo;

import android.app.Activity;
import android.content.Context;
import android.media.projection.MediaProjection;

import org.webrtc.Camera2Enumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WebRTCClient {

    private SurfaceViewRenderer localSurfaceView;
    private PeerConnection mPeerConnection;
    private SignalingClient client;
    private PeerConnectionFactory factory;
    private VideoTrack videoTrack;

    private void initWebRTC(Context context) {
        final EglBase eglBase = EglBase.create();

        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context.getApplicationContext())
                        //.setFieldTrials(fieldTrials)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());
        factory= PeerConnectionFactory.builder()
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBase.getEglBaseContext()))
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), true, true))
                .createPeerConnectionFactory();

        VideoCapturer videoCapturer = createScreenCapturer();

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        VideoSource videoSource = factory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, context.getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(720, 1080, 30);

        localSurfaceView.init(eglBase.getEglBaseContext(), null);
        localSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localSurfaceView.setMirror(true);
        localSurfaceView.setEnableHardwareScaler(false);

        videoTrack = factory.createVideoTrack("v1", videoSource);
        videoTrack.setEnabled(true);
        videoTrack.addSink(localSurfaceView);
    }

    private VideoCapturer createScreenCapturer() {
        // TODO
        return new ScreenCapturerAndroid(
                null, new MediaProjection.Callback() {
            @Override
            public void onStop() {
            }
        });
    }

    private void createPeerConnectionInternal() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer iceServer = new PeerConnection.IceServer("turn:10.11.130.47", "webrtc", "turnserver");
        iceServers.add(iceServer);


        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        PeerConnection.Observer mPeerConnectionObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                System.out.println("mPeerConnectionObserver onSignalingChange");
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                System.out.println("mPeerConnectionObserver onIceConnectionChange");
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                System.out.println("mPeerConnectionObserver onIceConnectionReceivingChange");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                System.out.println("mPeerConnectionObserver onIceGatheringChange");
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                System.out.println("mPeerConnectionObserver onIceCandidate: " + iceCandidate);
                client.sendLocalIceCandidate(iceCandidate);
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                System.out.println("mPeerConnectionObserver onIceCandidatesRemoved");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                System.out.println("mPeerConnectionObserver onAddStream");
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                System.out.println("mPeerConnectionObserver onRemoveStream");

            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                System.out.println("mPeerConnectionObserver dataChannel");
            }

            @Override
            public void onRenegotiationNeeded() {
                System.out.println("mPeerConnectionObserver onRenegotiationNeeded");
            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                System.out.println("mPeerConnectionObserver onAddTrack");
            }
        };
        mPeerConnection = factory.createPeerConnection(rtcConfig, mPeerConnectionObserver);
        mPeerConnection.addTrack(videoTrack, Collections.singletonList("ARDAMS"));
    }

}
