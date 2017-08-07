package com.facepp.library.util;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class MediaRecorderUtil {

    private MediaRecorder mMediaRecorder;
    public int cameraWidth;
    public int cameraHeight;
    private Camera mCamera;
    private Camera.PreviewCallback mCallback;

    public MediaRecorderUtil(Camera.PreviewCallback mCallback, Camera mCamera,
                             int cameraWidth, int cameraHeight) {
        this.mCamera = mCamera;
        this.cameraWidth = cameraWidth;
        this.cameraHeight = cameraHeight;
        this.mCallback = mCallback;
        mMediaRecorder = new MediaRecorder();
    }

    public boolean start() {
        try {
            mMediaRecorder.start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean prepareVideoRecorder(int angle) {
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mCamera.setPreviewCallback(mCallback);
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setOrientationHint(angle);
        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        CamcorderProfile camcorderProfile = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        camcorderProfile.videoFrameWidth = cameraWidth;
        camcorderProfile.videoFrameHeight = cameraHeight;
        Log.w("ceshi", "cameraWidth===" + cameraWidth + ", cameraHeight==="
                + cameraHeight);
        Log.w("ceshi", "camcorderProfile.videoFrameWidth==="
                + camcorderProfile.videoFrameWidth
                + ", camcorderProfile.videoFrameHeight==="
                + camcorderProfile.videoFrameHeight);
        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(camcorderProfile);
        mMediaRecorder.setVideoEncodingBitRate(1048576 * 40);
        // 每秒 4帧
        //mMediaRecorder.setVideoFrameRate(50);

        // mMediaRecorder.setVideoFrameRate(5);
        // mMediaRecorder.setCaptureRate(5);
        // mMediaRecorder.setVideoSize(320, 240);
        // Step 4: Set output file
        File dir = new File(Environment.getExternalStorageDirectory(),
                "megvii81point_video");
        if (!dir.exists())
            dir.mkdirs();
        mMediaRecorder.setOutputFile(new File(dir, ""
                + System.currentTimeMillis() + ".mp4").getAbsolutePath());

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    // public boolean prepareVideoRecorder() {
    // // Step 1: Unlock and set camera to MediaRecorder
    // mCamera.unlock();
    // mMediaRecorder.setCamera(mCamera);
    //
    // mMediaRecorder.setOrientationHint(270);
    // // Step 2: Set sources
    // mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    // mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    // CamcorderProfile camcorderProfile = CamcorderProfile
    // .get(CamcorderProfile.QUALITY_LOW);
    // // Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
    // camcorderProfile.videoFrameWidth = 640;
    // camcorderProfile.videoFrameHeight = 480;
    // // camcorderProfile.videoCodec = MediaRecorder.VideoEncoder.MPEG_4_SP;
    // // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
    // mMediaRecorder.setProfile(camcorderProfile);
    // mMediaRecorder.setVideoFrameRate(5);
    // mMediaRecorder.setCaptureRate(5);
    // // mMediaRecorder.setVideoSize(320, 240);
    // // Step 4: Set output file
    // File dir = new File(Environment.getExternalStorageDirectory(),
    // "facepp_video");
    // if (!dir.exists())
    // dir.mkdirs();
    // mMediaRecorder.setOutputFile(new File(dir, ""
    // + System.currentTimeMillis() + ".mp4").getAbsolutePath());
    //
    // // Step 5: Prepare configured MediaRecorder
    // try {
    // mMediaRecorder.prepare();
    // } catch (IllegalStateException e) {
    // releaseMediaRecorder();
    // return false;
    // } catch (IOException e) {
    // releaseMediaRecorder();
    // return false;
    // }
    // return true;
    // }

    public void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            Log.w("ceshi", "mMediaRecorder.reset(");
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if
            // the activity pauses.
            mCamera.lock();
            mCamera = null;
        }
    }

}
