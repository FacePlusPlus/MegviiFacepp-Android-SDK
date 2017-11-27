package com.facepp.demo;

import android.app.Activity;
import android.content.Intent;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facepp.demo.bean.FaceActionInfo;
import com.facepp.demo.bean.FeatureInfo;
import com.facepp.demo.facecompare.FaceCompareManager;
import com.facepp.demo.mediacodec.MediaHelper;
import com.facepp.demo.util.CameraMatrix;
import com.facepp.demo.util.ConUtil;
import com.facepp.demo.util.DialogUtil;
import com.facepp.demo.util.ICamera;
import com.facepp.demo.util.MediaRecorderUtil;
import com.facepp.demo.util.OpenGLDrawRect;
import com.facepp.demo.util.OpenGLUtil;
import com.facepp.demo.util.PointsMatrix;
import com.facepp.demo.util.Screen;
import com.facepp.demo.util.SensorEventUtil;
import com.megvii.facepp.sdk.Facepp;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenglActivity extends Activity
        implements PreviewCallback, Renderer, SurfaceTexture.OnFrameAvailableListener {

    private boolean isStartRecorder, is3DPose, isDebug, isROIDetect, is106Points, isBackCamera, isFaceProperty,
            isOneFaceTrackig, isFaceCompare, isShowFaceRect;
    private String trackModel;
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

    private MediaHelper mMediaHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Screen.initialize(this);
        setContentView(R.layout.activity_opengl);

        init();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startRecorder();
//            }
//        }, 2000);

        FaceCompareManager.instance().loadFeature(this);
        ConUtil.toggleHideyBar(this);

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
                if (mICamera==null||mICamera.mCamera==null){
                    return;
                }
                if (compareFaces == null || compareFaces.length <= 0 || carmeraImgData == null) {
                    Toast.makeText(OpenglActivity.this, "当前未检测到人脸", Toast.LENGTH_SHORT).show();
                    return;
                }

//                Log.e("xie","xie rect"+compareFaces[0].rect.top+"bottom"+compareFaces[0].rect.bottom+newestFeature);

                FaceCompareManager.instance().startActivity(OpenglActivity.this, compareFaces, mICamera, carmeraImgData, isBackCamera, faceActionInfo);
            }
        });

        featureTargetText = (TextView) findViewById(R.id.opengl_layout_targetFaceName);
        if (isFaceCompare) {
            btnAddFeature.setVisibility(View.VISIBLE);
        } else {
            btnAddFeature.setVisibility(View.GONE);
        }

        imgIcon = (ImageView) findViewById(R.id.opengl_layout_icon);
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

            String errorCode = facepp.init(this, ConUtil.getFileContent(this, R.raw.megviifacepp_0_5_2_model), isOneFaceTrackig ? 1 : 0);

            //sdk内部其他api已经处理好，可以不判断
            if (errorCode!=null){
                Intent intent=new Intent();
                intent.putExtra("errorcode",errorCode);
                setResult(101,intent);
                finish();
                return;
            }

            Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
            faceppConfig.interval = detection_interval;
            faceppConfig.minFaceSize = min_face_size;
            faceppConfig.roi_left = left;
            faceppConfig.roi_top = top;
            faceppConfig.roi_right = right;
            faceppConfig.roi_bottom = bottom;
            String[] array = getResources().getStringArray(R.array.trackig_mode_array);
            if (trackModel.equals(array[0]))
                faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_FAST;
            else if (trackModel.equals(array[1]))
                faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_ROBUST;
            else if (trackModel.equals(array[2])) {
                faceppConfig.detectionMode = Facepp.FaceppConfig.MG_FPP_DETECTIONMODE_TRACK_RECT;
                isShowFaceRect = true;
            }


            facepp.setFaceppConfig(faceppConfig);

            String version = facepp.getVersion();
            Log.d("ceshi", "onResume:version:" + version);
        } else {
            mDialogUtil.showDialog(getResources().getString(R.string.camera_error));
        }
        mMediaHelper = new MediaHelper(mICamera.cameraWidth, mICamera.cameraHeight, true, mGlSurfaceView);
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
    int preRotation = rotation;

    Facepp.Face[] compareFaces;

    long detectGenderAgeTime;
    final int DETECT_GENDER_INTERVAL = 1000;
    long featureTime = 0;
    private ArrayList<TextView> tvFeatures = new ArrayList<>();

    long matrixTime;
    private int prefaceCount = 0;


    @Override
    public void onPreviewFrame(final byte[] imgData, final Camera camera) {

        //检测操作放到主线程，防止贴点延迟
        int width = mICamera.cameraWidth;
        int height = mICamera.cameraHeight;

        long faceDetectTime_action = System.currentTimeMillis();
        final int orientation = sensorUtil.orientation;
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
            if (faces.length > 0) {
                for (int c = 0; c < faces.length; c++) {

                    if (is106Points)
                        facepp.getLandmarkRaw(faces[c], Facepp.FPP_GET_LANDMARK106);
                    else
                        facepp.getLandmarkRaw(faces[c], Facepp.FPP_GET_LANDMARK81);

                    if (is3DPose) {
                        facepp.get3DPose(faces[c]);
                    }

                    final Facepp.Face face = faces[c];
                    pitch = faces[c].pitch;
                    yaw = faces[c].yaw;
                    roll = faces[c].roll;
                    confidence = faces[c].confidence;


                    //0.4.7之前（包括）jni把所有角度的点算到竖直的坐标，所以外面画点需要再调整回来，才能与其他角度适配
                    //目前getLandmarkOrigin会获得原始的坐标，所以只需要横屏适配好其他的角度就不用适配了，因为texture和preview的角度关系是固定的
                    ArrayList<FloatBuffer> triangleVBList = new ArrayList<FloatBuffer>();
                    for (int i = 0; i < faces[c].points.length; i++) {
                        float x = (faces[c].points[i].x / width) * 2 - 1;
                        if (isBackCamera)
                            x = -x;
                        float y = (faces[c].points[i].y / height) * 2-1;
                        float[] pointf = new float[]{y, x, 0.0f};
                        FloatBuffer fb = mCameraMatrix.floatBufferUtil(pointf);
                        triangleVBList.add(fb);
                    }


                    pointsOpengl.add(triangleVBList);

                    if (mPointsMatrix.isShowFaceRect) {
                        facepp.getRect(faces[c]);
                        FloatBuffer buffer = calRectPostion(faces[c].rect, mICamera.cameraWidth, mICamera.cameraHeight);
                        rectsOpengl.add(buffer);
                    }

                }
            } else {
                pitch = 0.0f;
                yaw = 0.0f;
                roll = 0.0f;
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

            matrixTime = System.currentTimeMillis() - actionMaticsTime;

        }

        if (isSuccess)
            return;
        isSuccess = true;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (faces != null) {

                    confidence = 0.0f;
                    if (faces.length > 0) {


                        //compare ui
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (tvFeatures.size() < faces.length) {
                                    int tvFeaturesSize = tvFeatures.size();
                                    for (int i = 0; i < faces.length - tvFeaturesSize; i++) {
                                        TextView textView = new TextView(OpenglActivity.this);
                                        textView.setTextColor(0xff1a1d20);
                                        tvFeatures.add(textView);
                                    }
                                }
                                for (int i = prefaceCount; i < faces.length; i++) {
                                    ((RelativeLayout) mGlSurfaceView.getParent()).addView(tvFeatures.get(i));
                                }
                                for (int i = faces.length; i < tvFeatures.size(); i++) {
                                    ((RelativeLayout) mGlSurfaceView.getParent()).removeView(tvFeatures.get(i));
                                }
                                prefaceCount = faces.length;
                            }
                        });

                        for (int c = 0; c < faces.length; c++) {

                            final Facepp.Face face = faces[c];
                            if (isFaceProperty) {
                                long time_AgeGender_action = System.currentTimeMillis();
                                facepp.getAgeGender(faces[c]);
                                time_AgeGender_end = System.currentTimeMillis() - time_AgeGender_action;
                                String gender = "man";
                                if (face.female > face.male)
                                    gender = "woman";
                                AttriButeStr = "\nage: " + (int) Math.max(face.age, 1) + "\ngender: " + gender;
                            }


                            // 添加人脸比对
                            if (isFaceCompare) {
                                if (c == 0) {
                                    featureTime = System.currentTimeMillis();
                                }
                                if (facepp.getExtractFeature(face)) {
                                    synchronized (OpenglActivity.this) {
                                        newestFeature = face.feature;
                                        carmeraImgData = imgData;

                                    }

                                    if (c == faces.length - 1) {
                                        compareFaces = faces;
                                    }

                                    final FeatureInfo featureInfo = FaceCompareManager.instance().compare(facepp, face.feature);

                                    final int index = c;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            featureTargetText = tvFeatures.get(index);
                                            if (featureInfo != null) {
                                                featureTargetText.setVisibility(View.VISIBLE);
                                                featureTargetText.setText(featureInfo.title);
                                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) featureTargetText.getLayoutParams();

                                                int txtWidth = featureTargetText.getWidth();
                                                int txtHeight = featureTargetText.getHeight();


                                                PointF noseP = null;
                                                PointF eyebrowP = null;
                                                if (is106Points){
                                                    noseP=face.points[46];
                                                    eyebrowP=face.points[37];
                                                }else{
                                                    noseP=face.points[34];
                                                    eyebrowP=face.points[19];
                                                }
                                                boolean isVertical;
                                                if (orientation==0||orientation==3){
                                                    isVertical=true;
                                                }else{
                                                    isVertical=false;
                                                }
                                                int tops= (int) (((mICamera.cameraWidth-(isVertical?eyebrowP.x:noseP.x)))*(mGlSurfaceView.getHeight()*1.0f/mICamera.cameraWidth));
                                                int lefts= (int) ((mICamera.cameraHeight-(isVertical?noseP.y:eyebrowP.y))*(mGlSurfaceView.getWidth()*1.0f/mICamera.cameraHeight));
                                                if (isBackCamera){
                                                    tops=mGlSurfaceView.getHeight()-tops;
                                                }
                                                tops=tops-txtHeight/2;
                                                lefts=lefts-txtWidth/2;
                                                params.leftMargin = lefts;
                                                params.topMargin = tops;
                                                featureTargetText.setLayoutParams(params);

                                            } else {

                                                featureTargetText.setVisibility(View.INVISIBLE);
                                            }

                                        }
                                    });

                                }
                                if (c == faces.length - 1) {
                                    featureTime = System.currentTimeMillis() - featureTime;
                                }

                            }


                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < tvFeatures.size(); i++) {
                                    ((RelativeLayout) mGlSurfaceView.getParent()).removeView(tvFeatures.get(i));
                                }
                                prefaceCount=0;
                            }
                        });
                        mPointsMatrix.rect = null;
                        compareFaces = null;
                    }


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
                } else {
                    compareFaces = null;
                }
                isSuccess = false;

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



        finish();
    }

    @Override
    protected void onDestroy() {
        if (mMediaHelper!=null)
            mMediaHelper.stopRecording();
        super.onDestroy();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                facepp.release();
            }
        });

    }

    private int mTextureID = -1;
    private SurfaceTexture mSurface;
    private CameraMatrix mCameraMatrix;
    private PointsMatrix mPointsMatrix;

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        // TODO Auto-generated method stub
//		Log.d("ceshi", "onFrameAvailable");
        mGlSurfaceView.requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 黑色背景
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        surfaceInit();
    }

    private void surfaceInit() {
        mTextureID = OpenGLUtil.createTextureID();

        mSurface = new SurfaceTexture(mTextureID);
        if (isStartRecorder) {
            mMediaHelper.startRecording(mTextureID);
        }
        // 这个接口就干了这么一件事，当有数据上来后会进到onFrameAvailable方法
        mSurface.setOnFrameAvailableListener(this);// 设置照相机有数据时进入
        mCameraMatrix = new CameraMatrix(mTextureID);
        mPointsMatrix = new PointsMatrix(isFaceCompare);
        mPointsMatrix.isShowFaceRect = isShowFaceRect;
        mICamera.startPreview(mSurface);// 设置预览容器
        mICamera.actionDetect(this);
        if (isROIDetect)
            drawShowRect();
    }

    private boolean flip = true;

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
        if (isStartRecorder) {
            flip = !flip;
            if (flip) {    // ~30fps
                synchronized (this) {
//                    mMediaHelper.frameAvailable(mtx);
                    mMediaHelper.frameAvailable(mtx);
                }
            }
        }

    }

    private RectF calRect(Rect rect, float width, float height) {
        float top = 1 - (rect.top * 1.0f / height) * 2;
        float left = (rect.left * 1.0f / width) * 2 - 1;
        float right = (rect.right * 1.0f / width) * 2 - 1;
        float bottom = 1 - (rect.bottom * 1.0f / height) * 2;


        RectF rectf = new RectF();
        rectf.top = top;
        rectf.left = left;
        rectf.right = right;
        rectf.bottom = bottom;

        Log.d("ceshi", "calRect: " + rectf);
        return rectf;
    }

    private FloatBuffer calRectPostion(Rect rect, float width, float height) {
        float top = 1 - (rect.top * 1.0f / height) * 2;
        float left = (rect.left * 1.0f / width) * 2 - 1;
        float right = (rect.right * 1.0f / width) * 2 - 1;
        float bottom = 1 - (rect.bottom * 1.0f / height) * 2;

        // 左上角
        float x1 = -top;
        float y1 = left;

        // 右下角
        float x2 = -bottom;
        float y2 = right;

        if (isBackCamera) {
            y1 = -y1;
            y2 = -y2;
        }

        float[] tempFace = {
                x1, y2, 0.0f,
                x1, y1, 0.0f,
                x2, y1, 0.0f,
                x2, y2, 0.0f,
        };

        FloatBuffer buffer = mCameraMatrix.floatBufferUtil(tempFace);
        return buffer;
    }



}
