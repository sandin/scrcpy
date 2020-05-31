package com.lds.webrtcdemo;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public class SignalingClient {
    private static final String TAG = SignalingClient.class.getSimpleName();
    private WebSocket mWebSocket;



    public interface Callback {

        void onVideoOffer(String targetUsername, String sdp);

        void onNewIceCandidate(IceCandidate iceCandidate);
    }

    private Long mClientId;
    private String mTargetUsername;
    private JSONArray mUsers;

    public SignalingClient() {

    }

    public void connect(final Callback callback) {
        AsyncHttpClient.getDefaultInstance().websocket("http://10.11.130.47:6503", "json", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                /*
                webSocket.send("a string");
                webSocket.send(new byte[10]);
                */
                mWebSocket = webSocket;
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    public void onStringAvailable(String s) {
                        Log.d(TAG, "[recv] " + s);
                        try {
                            JSONObject jo = JSON.parseObject(s);
                            String type = jo.getString("type");
                            switch (type) {
                                case "id":
                                    mClientId = jo.getLong("id");
                                    JSONObject resp = new JSONObject();
                                    resp.put("type", "username");
                                    resp.put("name", "lds-android"); // TODO
                                    resp.put("date", System.currentTimeMillis());
                                    resp.put("id", mClientId);
                                    sendToServer(mWebSocket, resp);
                                    break;
                                case "userlist":
                                    mUsers = jo.getJSONArray("users");
                                    for (int i = 0; i < mUsers.size(); i++) {
                                        String username = mUsers.getString(i);
                                        System.out.println("username: " + username);
                                    }
                                    break;
                                case "video-offer": {
                                    mTargetUsername = jo.getString("name");
                                    Log.d(TAG, "Received video chat offer from " + mTargetUsername);
                                    String sdp = jo.getJSONObject("sdp").getString("sdp");
                                    callback.onVideoOffer(mTargetUsername, sdp);
                                    break;
                                }
                                case "new-ice-candidate": {
                                    mTargetUsername = jo.getString("name");
                                    JSONObject candidate = jo.getJSONObject("candidate");
                                    IceCandidate iceCandidate = toJavaCandidate(candidate);
                                    callback.onNewIceCandidate(iceCandidate);
                                    break;
                                }
                                default:
                                    // pass
                            }

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
                /*
                webSocket.setDataCallback(new DataCallback() {
                    public void onDataAvailable(DataEmitter emitter, ByteBufferList byteBufferList) {
                        System.out.println("I got some bytes!");
                        // note that this data has been read
                        byteBufferList.recycle();
                    }
                });
                */
            }
        });

    }


    public void sendAnswerSdp(String targetUsername, SessionDescription sessionDescription) {
        JSONObject resp = new JSONObject();
        resp.put("type", "video-answer");
        resp.put("name", "lds-android"); // TODO
        resp.put("target", targetUsername);
        {
            JSONObject sdp = new JSONObject();
            sdp.put("sdp", sessionDescription.description);
            sdp.put("type", "answer");
            resp.put("sdp", sdp);
        }
        //resp.put("id", mClientId);
        sendToServer(mWebSocket, resp);
    }

    private static IceCandidate toJavaCandidate(JSONObject json) {
        return new IceCandidate(json.getString("id"), json.getInteger("label"), json.getString("candidate"));
    }

    public void sendLocalIceCandidate(IceCandidate iceCandidate) {
        JSONObject resp = new JSONObject();
        resp.put("type", "new-ice-candidate");
        resp.put("target", mTargetUsername);
        resp.put("name", "lds-android"); // TODO
        {
            JSONObject candidate = new JSONObject();
            candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
            candidate.put("sdpMid", iceCandidate.sdpMid);
            candidate.put("candidate", iceCandidate.sdp);
            resp.put("candidate", candidate);
        }
        sendToServer(mWebSocket, resp);
    }

    private void sendToServer(WebSocket webSocket, JSONObject msg) {
        Log.d(TAG, "[send] " + msg.toJSONString());
        webSocket.send(msg.toJSONString());
    }

}
