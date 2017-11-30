package com.facepp.demo.mediacodec;

import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.IOException;

/**
 * Created by xiejiantao on 2017/10/27.
 */



public class MediaHelper {

    /**
     * muxer for audio/video recording
     */
    private MediaMuxerWrapper mMuxer;
    private int mCameraWidth,mCameraHeight;// be video
    private GLSurfaceView mSurfaceView;

    private int mTextureId;

    private MediaVideoEncoder mMediaVideoEncode;

    private final float[] mMvpMatrix = new float[16];
    /**
     *
     * @param cameraWidth
     * @param cameraHeight
     * @param isLand  横屏
     */
    public MediaHelper(int cameraWidth, int cameraHeight, boolean isLand, GLSurfaceView surfaceView){
        if (!isLand) {
            mCameraWidth = cameraWidth;
            mCameraHeight = cameraHeight;
        } else {
            mCameraWidth = cameraHeight;
            mCameraHeight = cameraWidth;
        }
        mSurfaceView=surfaceView;
        Matrix.setIdentityM(mMvpMatrix,0);
        Matrix.rotateM(mMvpMatrix,0,270,0,0,1);
        // TODO: 2017/10/27
    }





    /**
     * start resorcing
     * This is a sample project and call this on UI thread to avoid being complicated
     * but basically this should be called on private thread because prepareing
     * of encoder is heavy work
     */
    public void startRecording(int textureId) {
        mTextureId=textureId;
        try {

            mMuxer = new MediaMuxerWrapper(".mp4");	// if you record audio only, ".m4a" is also OK.
            if (true) {
                // for video capturing
                new MediaVideoEncoder(mMuxer, mMediaEncoderListener, mCameraWidth, mCameraHeight);
            }
//            if (true) {
//                // for audio capturing
//                new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
//            }
            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (final IOException e) {
        }
    }

    /**
     * request stop recording
     */
    public void stopRecording() {
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
            // you should not wait here
        }
    }

    /**
     * callback methods from encoder
     */
    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder)
                setVideoEncoder((MediaVideoEncoder)encoder);
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder)
                setVideoEncoder(null);
        }
    };



    private void setVideoEncoder(final MediaVideoEncoder encoder) {
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                synchronized (mSurfaceView) {
                    if (encoder != null) {
                        encoder.setEglContext(EGL14.eglGetCurrentContext(), mTextureId);
                        mMediaVideoEncode=encoder;
                    }

                }
            }
        });
    }

    public void frameAvailable(float[] mStMatrix,float [] mMvpMatrix){
        if (mMediaVideoEncode != null) {
            // notify to capturing thread that the camera frame is available.
//						mVideoEncoder.frameAvailableSoon(mStMatrix);
            mMediaVideoEncode.frameAvailableSoon(mStMatrix, mMvpMatrix);
        }

    }

    public void frameAvailable(float[] mStMatrix){


        if (mMediaVideoEncode != null) {
            // notify to capturing thread that the camera frame is available.
//						mVideoEncoder.frameAvailableSoon(mStMatrix);
            mMediaVideoEncode.frameAvailableSoon(mStMatrix,mMvpMatrix);
        }

    }

    public void frameAvailable(){
        if (mMediaVideoEncode != null) {
            // notify to capturing thread that the camera frame is available.
//						mVideoEncoder.frameAvailableSoon(mStMatrix);
            mMediaVideoEncode.frameAvailableSoon();
        }

    }
}
