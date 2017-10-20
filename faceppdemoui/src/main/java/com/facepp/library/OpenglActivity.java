package com.facepp.library;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facepp.library.bean.FaceActionInfo;
import com.facepp.library.bean.FeatureInfo;
import com.facepp.library.facecompare.FaceCompareManager;
import com.facepp.library.util.CameraMatrix;
import com.facepp.library.util.ConUtil;
import com.facepp.library.util.DialogUtil;
import com.facepp.library.util.ICamera;
import com.facepp.library.util.MediaRecorderUtil;
import com.facepp.library.util.OpenGLDrawRect;
import com.facepp.library.util.OpenGLUtil;
import com.facepp.library.util.PointsMatrix;
import com.facepp.library.util.Screen;
import com.facepp.library.util.SensorEventUtil;
import com.megvii.facepp.sdk.Facepp;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenglActivity extends Activity
        implements PreviewCallback, Renderer, SurfaceTexture.OnFrameAvailableListener {

    private boolean isStartRecorder, is3DPose, isDebug, isROIDetect, is106Points, isBackCamera, isFaceProperty,
            isOneFaceTrackig, isFaceCompare;
    private String trackModel;
    private boolean isTiming = true; // 是否是定时去刷新界面;
    private int printTime = 31;
    private GLSurfaceView mGlSurfaceView;
    private ICamera mICamera;
    private Camera mCamera;
    private DialogUtil mDialogUtil;
    private TextView debugInfoText, debugPrinttext, AttriButetext;
    private TextView featureTargetText;
    private ImageButton btnAddFeature;
    private HandlerThread mHandlerThread = new HandlerThread("facepp");
    private Handler mHandler;
    private Facepp facepp;
    private MediaRecorderUtil mediaRecorderUtil;
    private int min_face_size = 200;
    private int detection_interval = 25;
    private HashMap<String, Integer> resolutionMap;
    private SensorEventUtil sensorUtil;
    private float roi_ratio = 0.8f;
    private byte[] newestFeature;
    private byte[] carmeraImgData;

    private int screenWidth;
    private int screenHeight;
    private boolean isSurfaceCreated;

    private FaceActionInfo faceActionInfo;
    private ImageView imgIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Screen.initialize(this);
        setContentView(R.layout.activity_opengl);

        init();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startRecorder();
            }
        }, 2000);

        FaceCompareManager.instance().loadFeature(this);

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;

    }

    private void init() {
        if (android.os.Build.MODEL.equals("PLK-AL10"))
            printTime = 50;

        faceActionInfo = (FaceActionInfo) getIntent().getSerializableExtra("FaceAction");

        isStartRecorder = faceActionInfo.isStartRecorder;
        is3DPose = faceActionInfo.is3DPose;
        isDebug = faceActionInfo.isdebug;
        isROIDetect = faceActionInfo.isROIDetect;
        is106Points = faceActionInfo.is106Points;
        isBackCamera = faceActionInfo.isBackCamera;
        isFaceProperty = faceActionInfo.isFaceProperty;
        isOneFaceTrackig = faceActionInfo.isOneFaceTrackig;
        isFaceCompare = faceActionInfo.isFaceCompare;
        trackModel = faceActionInfo.trackModel;

        min_face_size = faceActionInfo.faceSize;
        detection_interval = faceActionInfo.interval;
        resolutionMap = faceActionInfo.resolutionMap;

        facepp = new Facepp();

        sensorUtil = new SensorEventUtil(this);

        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mGlSurfaceView = (GLSurfaceView) findViewById(R.id.opengl_layout_surfaceview);
        mGlSurfaceView.setEGLContextClientVersion(2);// 创建一个OpenGL ES 2.0
        // context
        mGlSurfaceView.setRenderer(this);// 设置渲染器进入gl
        // RENDERMODE_CONTINUOUSLY不停渲染
        // RENDERMODE_WHEN_DIRTY懒惰渲染，需要手动调用 glSurfaceView.requestRender() 才会进行更新
        mGlSurfaceView.setRenderMode(mGlSurfaceView.RENDERMODE_WHEN_DIRTY);// 设置渲染器模式
        mGlSurfaceView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFocus();
            }
        });

        mICamera = new ICamera();
        mDialogUtil = new DialogUtil(this);
        debugInfoText = (TextView) findViewById(R.id.opengl_layout_debugInfotext);
        AttriButetext = (TextView) findViewById(R.id.opengl_layout_AttriButetext);
        debugPrinttext = (TextView) findViewById(R.id.opengl_layout_debugPrinttext);
        if (isDebug)
            debugInfoText.setVisibility(View.VISIBLE);
        else
            debugInfoText.setVisibility(View.INVISIBLE);

        btnAddFeature = (ImageButton) findViewById(R.id.opengl_layout_addFaceInfo);
        btnAddFeature.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 保存feature数据
                FeatureInfo info = new FeatureInfo();
                synchronized (OpenglActivity.this) {
                    info.feature = newestFeature;
                    Bitmap bitmap = mICamera.getBitMap(carmeraImgData, !isBackCamera);
                    if (bitmap != null) {
                        String filePath = ConUtil.saveBitmap(OpenglActivity.this, bitmap);
                        info.imgFilePath = filePath;
                    }

                }
                FaceCompareManager.instance().addFeature(OpenglActivity.this, info);

                Intent intent = new Intent(OpenglActivity.this, FeatureInfoSettingActivity.class);
                intent.putExtra("FaceAction", faceActionInfo);
                startActivity(intent);
            }
        });

        featureTargetText = (TextView) findViewById(R.id.opengl_layout_targetFaceName);
        if (isFaceCompare){
            btnAddFeature.setVisibility(View.VISIBLE);
        }else {
            btnAddFeature.setVisibility(View.GONE);
        }

        imgIcon = (ImageView) findViewById(R.id.opengl_layout_icon);
    }

    /**
     * 用于测试一些api
     */
    private void newMethodCall(){
        Log.d("xie", "newMethodCall: "+facepp.GetFaceConfidenceFilter()+facepp.getSDKBundleId()+facepp.getJenkinsNumber());

        imgIcon.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("xie", "newMethodCall: "+facepp.resetTrack()+facepp.GetFaceConfidenceFilter()+facepp.getSDKBundleId());
            }
        },5000);

    }

    /**
     * 开始录制
     */
    private void startRecorder() {
        if (isStartRecorder) {
            int Angle = 360 - mICamera.Angle;
            if (isBackCamera)
                Angle = mICamera.Angle;
            mediaRecorderUtil = new MediaRecorderUtil(this, mCamera, mICamera.cameraWidth, mICamera.cameraHeight);
            isStartRecorder = mediaRecorderUtil.prepareVideoRecorder(Angle);
            if (isStartRecorder) {
                boolean isRecordSucess = mediaRecorderUtil.start();
                if (isRecordSucess)
                    mICamera.actionDetect(this);
                else
                    mDialogUtil.showDialog(getResources().getString(R.string.no_record));
            }
        }
    }

    private void autoFocus() {
        if (mCamera != null && isBackCamera) {
            mCamera.cancelAutoFocus();
            Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(parameters);
            mCamera.autoFocus(null);
        }
    }

    private int Angle;

    @Override
    protected void onResume() {
        super.onResume();
        ConUtil.acquireWakeLock(this);
        startTime = System.currentTimeMillis();
        mCamera = mICamera.openCamera(isBackCamera, this, resolutionMap);
        if (mCamera != null) {
            Angle = 360 - mICamera.Angle;
            if (isBackCamera)
                Angle = mICamera.Angle;

            RelativeLayout.LayoutParams layout_params = mICamera.getLayoutParam();
            mGlSurfaceView.setLayoutParams(layout_params);

            int width = mICamera.cameraWidth;
            int height = mICamera.cameraHeight;

            int left = 0;
            int top = 0;
            int right = width;
            int bottom = height;
            if (isROIDetect) {
                float line = height * roi_ratio;
                left = (int) ((width - line) / 2.0f);
                top = (int) ((height - line) / 2.0f);
                right = width - left;
                bottom = height - top;
            }

            String errorCode = facepp.init(this, ConUtil.getFileContent(this, R.raw.megviifacepp_0_5_0_model));
            Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
            faceppConfig.interval = detection_interval;
            faceppConfig.minFaceSize = min_face_size;
            faceppConfig.roi_left = left;
            faceppConfig.roi_top = top;
            faceppConfig.roi_right = right;
            faceppConfig.roi_bottom = bottom;
            if (isOneFaceTrackig)
                faceppConfig.one_face_tracking = 1;
            else
                faceppConfig.one_face_tracking = 0;
            String[] array = getResources().getStringArray(R.array.trackig_mode_array);
            if (trackModel.equals(array[0]))
                faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_FAST;
            else if (trackModel.equals(array[1]))
                faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_ROBUST;
            else if (trackModel.equals(array[2]))
                faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_RECT;

            facepp.setFaceppConfig(faceppConfig);

            String version = facepp.getVersion();
            Log.d("ceshi", "onResume:version:" + version);
        } else {
            mDialogUtil.showDialog(getResources().getString(R.string.camera_error));
        }

//        newMethodCall();
    }

    private void setConfig(int rotation) {
        Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
        if (faceppConfig.rotation != rotation) {
            faceppConfig.rotation = rotation;
            facepp.setFaceppConfig(faceppConfig);
        }
    }

    /**
     * 画绿色框
     */
    private void drawShowRect() {
        mPointsMatrix.vertexBuffers = OpenGLDrawRect.drawCenterShowRect(isBackCamera, mICamera.cameraWidth,
                mICamera.cameraHeight, roi_ratio);
    }

    boolean isSuccess = false;
    float confidence;
    float pitch, yaw, roll;
    long startTime;
    long time_AgeGender_end = 0;
    String AttriButeStr = "";
    int rotation = Angle;

    @Override
    public void onPreviewFrame(final byte[] imgData, final Camera camera) {
        if (isSuccess)
            return;

        isSuccess = true;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int width = mICamera.cameraWidth;
                int height = mICamera.cameraHeight;

                long faceDetectTime_action = System.currentTimeMillis();
                int orientation = sensorUtil.orientation;
                if (orientation == 0)
                    rotation = Angle;
                else if (orientation == 1)
                    rotation = 0;
                else if (orientation == 2)
                    rotation = 180;
                else if (orientation == 3)
                    rotation = 360 - Angle;

                setConfig(rotation);

                final Facepp.Face[] faces = facepp.detect(imgData, width, height, Facepp.IMAGEMODE_NV21);
                final long algorithmTime = System.currentTimeMillis() - faceDetectTime_action;

                if (faces != null) {
                    long actionMaticsTime = System.currentTimeMillis();
                    ArrayList<ArrayList> pointsOpengl = new ArrayList<ArrayList>();
                    ArrayList<FloatBuffer> rectsOpengl = new ArrayList<FloatBuffer>();

                    confidence = 0.0f;

                    if (faces.length > 0) {
                        for (int c = 0; c < faces.length; c++) {
                            if (is106Points)
                                facepp.getLandmark(faces[c], Facepp.FPP_GET_LANDMARK106);
                            else
                                facepp.getLandmark(faces[c], Facepp.FPP_GET_LANDMARK81);

                            if (is3DPose) {
                                facepp.get3DPose(faces[c]);
                            }

                            Facepp.Face face = faces[c];

                            if (isFaceProperty) {
                                long time_AgeGender_action = System.currentTimeMillis();
                                facepp.getAgeGender(faces[c]);
                                time_AgeGender_end = System.currentTimeMillis() - time_AgeGender_action;
                                String gender = "man";
                                if (face.female > face.male)
                                    gender = "woman";
                                AttriButeStr = "\nage: " + (int) Math.max(face.age, 1) + "\ngender: " + gender;
                            }

                            pitch = faces[c].pitch;
                            yaw = faces[c].yaw;
                            roll = faces[c].roll;
                            confidence = faces[c].confidence;

                            if (mPointsMatrix.isShowFaceRect){
                                facepp.getRect(faces[c]);
                            }

                            Log.e("xie","rect org"+face.rect.left+" "+face.rect.bottom+" "+face.rect.top+" "+face.rect.right);
                            FloatBuffer buffer = calRectPostion(faces[c].rect, mICamera.cameraWidth, mICamera.cameraHeight);
                            rectsOpengl.add(buffer);


                            if (orientation == 1 || orientation == 2) {
                                width = mICamera.cameraHeight;
                                height = mICamera.cameraWidth;
                            }

                            final PointF point19 = face.points[19];
                            final PointF point26 = face.points[26];
                            final PointF point37 = face.points[37];
                            final PointF point38 = face.points[38];

                            ArrayList<FloatBuffer> triangleVBList = new ArrayList<FloatBuffer>();
                            for (int i = 0; i < faces[c].points.length; i++) {
                                float x = (faces[c].points[i].x / height) * 2 - 1;
                                if (isBackCamera)
                                    x = -x;
                                float y = 1 - (faces[c].points[i].y / width) * 2;
                                float[] pointf = new float[]{x, y, 0.0f};
                                if (orientation == 1)
                                    pointf = new float[]{-y, x, 0.0f};
                                if (orientation == 2)
                                    pointf = new float[]{y, -x, 0.0f};
                                if (orientation == 3)
                                    pointf = new float[]{-x, -y, 0.0f};

                                FloatBuffer fb = mCameraMatrix.floatBufferUtil(pointf);
                                triangleVBList.add(fb);

                                if (is106Points) {
                                    if (i == 37){
                                        point37.x = pointf[0];
                                        point37.y = pointf[1];
                                    }else if (i == 38){
                                        point38.x = pointf[0];
                                        point38.y = pointf[1];
                                    }
                                } else {
                                    if (i == 19) {
                                        point19.x = pointf[0];
                                        point19.y = pointf[1];
                                    } else if (i == 26) {
                                        point26.x = pointf[0];
                                        point26.y = pointf[1];
                                    }
                                }

                            }

                            pointsOpengl.add(triangleVBList);

                            // 添加人脸比对
                            if (isFaceCompare) {
                                long actionFeatureTime = System.currentTimeMillis();
                                if (facepp.getExtractFeature(face)) {
                                    synchronized (OpenglActivity.this) {
                                        newestFeature = face.feature;
                                        carmeraImgData = imgData;
                                    }

                                    final FeatureInfo featureInfo = FaceCompareManager.instance().compare(facepp, face.feature);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (featureInfo != null){
                                                featureTargetText.setVisibility(View.VISIBLE);
                                                featureTargetText.setText(featureInfo.title);
                                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) featureTargetText.getLayoutParams();

                                                int imageW = mGlSurfaceView.getWidth();
                                                int imageH = mGlSurfaceView.getHeight();
                                                float left1, left2, top;
                                                if (is106Points){
                                                    left1 = ((imageW / 2) * (1 - point37.x) / imageW) * screenWidth;
                                                    left2 = ((imageW / 2) * (1 - point38.x) / imageW) * screenWidth;
                                                    top = ((imageH / 2) * (1 - point37.y) / imageH) * screenHeight - ((screenHeight - imageH) / 2) - 100;
                                                }else {
                                                    left1 = ((imageW / 2) * (1 - point19.x) / imageW) * screenWidth;
                                                    left2 = ((imageW / 2) * (1 - point26.x) / imageW) * screenWidth;
                                                    top = ((imageH / 2) * (1 - point19.y) / imageH) * screenHeight - ((screenHeight - imageH) / 2) - 100;
                                                }

                                                int txtWidth = featureTargetText.getWidth();
                                                int txtHeight = featureTargetText.getHeight();

                                                params.leftMargin = (int) ((left1 + left2) / 2) - txtWidth / 2;
                                                params.topMargin = (int) (top) - txtHeight / 2;
                                                featureTargetText.setLayoutParams(params);
                                            }else {
                                                featureTargetText.setVisibility(View.INVISIBLE);
                                            }

                                        }
                                    });

                                }
                                final long featureTime = System.currentTimeMillis() - actionFeatureTime;

                            }
                        }
                    } else {
                        pitch = 0.0f;
                        yaw = 0.0f;
                        roll = 0.0f;
                        mPointsMatrix.rect = null;

                    }

                    synchronized (mPointsMatrix) {
                        if (faces.length > 0 && is3DPose)
                            mPointsMatrix.bottomVertexBuffer = OpenGLDrawRect.drawBottomShowRect(0.15f, 0, -0.7f, pitch,
                                    -yaw, roll, rotation);
                        else
                            mPointsMatrix.bottomVertexBuffer = null;
                        mPointsMatrix.points = pointsOpengl;
                        mPointsMatrix.faceRects = rectsOpengl;

                    }

                    final long matrixTime = System.currentTimeMillis() - actionMaticsTime;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String logStr = "\ncameraWidth: " + mICamera.cameraWidth + "\ncameraHeight: "
                                    + mICamera.cameraHeight + "\nalgorithmTime: " + algorithmTime + "ms"
                                    + "\nmatrixTime: " + matrixTime + "\nconfidence:" + confidence;
                            debugInfoText.setText(logStr);
                            if (faces.length > 0 && isFaceProperty && AttriButeStr != null && AttriButeStr.length() > 0)
                                AttriButetext.setText(AttriButeStr + "\nAgeGenderTime:" + time_AgeGender_end);
                            else
                                AttriButetext.setText("");
                        }
                    });
                }
                isSuccess = false;
                if (!isTiming) {
                    timeHandle.sendEmptyMessage(1);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        ConUtil.releaseWakeLock();
        if (mediaRecorderUtil != null) {
            mediaRecorderUtil.releaseMediaRecorder();
        }
        mICamera.closeCamera();
        mCamera = null;

        timeHandle.removeMessages(0);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        facepp.release();
    }

    private int mTextureID = -1;
    private SurfaceTexture mSurface;
    private CameraMatrix mCameraMatrix;
    private PointsMatrix mPointsMatrix;

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        // TODO Auto-generated method stub
//		Log.d("ceshi", "onFrameAvailable");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 黑色背景
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        surfaceInit();
    }

    private void surfaceInit(){
        mTextureID = OpenGLUtil.createTextureID();
        mSurface = new SurfaceTexture(mTextureID);
        // 这个接口就干了这么一件事，当有数据上来后会进到onFrameAvailable方法
        mSurface.setOnFrameAvailableListener(this);// 设置照相机有数据时进入
        mCameraMatrix = new CameraMatrix(mTextureID);
        mPointsMatrix = new PointsMatrix(isFaceCompare);
        mICamera.startPreview(mSurface);// 设置预览容器
        mICamera.actionDetect(this);
        if (isTiming) {
            timeHandle.sendEmptyMessageDelayed(0, printTime);
        }
        if (isROIDetect)
            drawShowRect();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 设置画面的大小
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        ratio = 1; // 这样OpenGL就可以按照屏幕框来画了，不是一个正方形了

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        // Matrix.perspectiveM(mProjMatrix, 0, 0.382f, ratio, 3, 700);
    }

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    @Override
    public void onDrawFrame(GL10 gl) {
        final long actionTime = System.currentTimeMillis();
//		Log.w("ceshi", "onDrawFrame===");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);// 清除屏幕和深度缓存
        float[] mtx = new float[16];
        mSurface.getTransformMatrix(mtx);
        mCameraMatrix.draw(mtx);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1f, 0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        mPointsMatrix.draw(mMVPMatrix);

        if (isDebug) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final long endTime = System.currentTimeMillis() - actionTime;
                    debugPrinttext.setText("printTime: " + endTime);
                }
            });
        }
        mSurface.updateTexImage();// 更新image，会调用onFrameAvailable方法
    }

    private RectF calRect(Rect rect, float width, float height){
        float top = 1 - (rect.top * 1.0f / height) * 2;
        float left = (rect.left  * 1.0f/ width) * 2 - 1;
        float right = (rect.right * 1.0f / width) * 2 - 1;
        float bottom = 1 - (rect.bottom  * 1.0f/ height) * 2;


        RectF rectf = new RectF();
        rectf.top = top;
        rectf.left = left;
        rectf.right = right;
        rectf.bottom = bottom;

        Log.d("ceshi", "calRect: " + rectf);
        return rectf;
    }

    private FloatBuffer calRectPostion(Rect rect, float width, float height){
        float top = 1 - (rect.top * 1.0f / height) * 2;
        float left = (rect.left  * 1.0f/ width) * 2 - 1;
        float right = (rect.right * 1.0f / width) * 2 - 1;
        float bottom = 1 - (rect.bottom  * 1.0f/ height) * 2;

        // 左上角
        float x1 = -top;
        float y1 = left;

        // 右下角
        float x2 = -bottom;
        float y2 = right;

        if (isBackCamera){
            y1 = -y1;
            y2 = -y2;
        }

        float[] tempFace = {
                x1, y2,0.0f,
                x1, y1, 0.0f,
                x2, y1, 0.0f,
                x2, y2,0.0f,
        };

        FloatBuffer buffer =  mCameraMatrix.floatBufferUtil(tempFace);
        return buffer;
    }


    Handler timeHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mGlSurfaceView.requestRender();// 发送去绘制照相机不断去回调
                    timeHandle.sendEmptyMessageDelayed(0, printTime);
                    break;
                case 1:
                    mGlSurfaceView.requestRender();// 发送去绘制照相机不断去回调
                    break;
            }
        }
    };

}
