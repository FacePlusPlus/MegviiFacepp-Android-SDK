package com.megvii.beautify;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.megvii.beautify.cameragl.OpenglUtil;
import com.megvii.beautify.component.TexturePbufferRenderer;
import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.login.LoadingActivity;
import com.megvii.beautify.util.ConUtil;
import com.megvii.beautify.util.SysUtil;
import com.megvii.beautify.util.Util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by xiejiantao on 2017/8/29.
 */
@RunWith(AndroidJUnit4.class)
public class UiTest {
    //设置初始启动测试Activity，ActivityTestRule的构造函数的第三个参数是否启动Activity

    Context appContext;


    final int mRepeatCount=1;
    final int mInitCount=1;
    final int mProcessCount=1;
    final int mReleaseCount=1;

    String result = "";
    int resCode;
    long time;

    final Integer LOCK = 1;

    @Rule
    public ActivityTestRule<LoadingActivity> mActivityRule = new ActivityTestRule<>(
            LoadingActivity.class,false,false);

    @Before
    public void useAppContext() throws Exception {
        // Context of the app under test.
         appContext = InstrumentationRegistry.getTargetContext();

       // assertEquals("com.megvii.beautifya", appContext.getPackageName());
    }


//    @Test
//    public  void  testBeauty(){
//        TextureSurfaceRenderer surfaceRun=new TextureSurfaceRenderer(new SurfaceTexture(10),1280,720) {
//            @Override
//            protected boolean draw() {
//                return false;
//            }
//
//            @Override
//            protected void initGLComponents() {
//               System.out.println("OpenGL init OK. start draw22...");
//               int res= BeaurifyJniSdk.nativeCreateBeautyHandle(appContext, 1280,
//                        720, 90,
//                        ConUtil.getFileContent(appContext, R.raw.mgbeautify_1_2_3_model),
//                        ConUtil.getFileContent(appContext, R.raw.megviifacepp_0_4_7_model)
//                );
//                System.out.println("OpenGL init OK. start draw4..."+res);
//                assertEquals(res, 1111);
//            }
//
//            @Override
//            protected void deinitGLComponents() {
//
//            }
//
//            @Override
//            public SurfaceTexture getSurfaceTexture() {
//                return null;
//            }
//        };
//
//      //  assertEquals(0, 1);
//    }

    @Test
    public  void  testBeautyPbuffer(){



        TexturePbufferRenderer   surfaceRun  = new TexturePbufferRenderer() {

            private int[] mOutTextureId;
            private int[] mInTextureId;

            @Override
            protected boolean draw() {
                time = System.currentTimeMillis();
                for (int i = 0; i < mProcessCount; i++) {
                    resCode |= BeaurifyJniSdk.nativeProcessTexture(mInTextureId[0], mOutTextureId[0]);
                }
                assertEquals(resCode,0);
                time = mProcessCount == 0 ? (long) ((System.currentTimeMillis() - time) * 1.0f / mInitCount) : 0;
                result += "   process完mem" + SysUtil.getNativeMemoryInfo() + "   code" + resCode + "    time" + time + "\n";
                resCode = 0;
                return false;

            }

            @Override
            protected void initGLComponents() {
                //确保只一次
                if (mOutTextureId == null || mOutTextureId.length < 1) {
                    mOutTextureId = OpenglUtil.initTextureID(1280, 720);
                    mInTextureId = OpenglUtil.initTextureID(1280, 720);
                }

                time = System.currentTimeMillis();
                for (int i = 0; i < mInitCount; i++) {
                    //既没有崩溃也没有出错，然后从这里中断了。错误信息没打印出来，单元测试还通过了
                    resCode |= BeaurifyJniSdk.nativeCreateBeautyHandle(appContext, 1280,
                            720, 90, Util.MG_FPP_DETECTIONMODE_TRACKING,
                            ConUtil.getFileContent(appContext, R.raw.mgbeautify_1_2_4_model),
                            ConUtil.getFileContent(appContext, R.raw.megviifacepp_0_4_7_model)
                    );

                    System.out.println("OpenGL init OK. start draw222...");
                }
                assertEquals(resCode,0);
                time = (long) ((System.currentTimeMillis() - time) * 1.0f / mInitCount);
                result += "   init完mem" + SysUtil.getNativeMemoryInfo() + "   code" + resCode + "    time" + time + "\n";
                resCode = 0;
                System.out.println("OpenGL init OK. start draw44..." + resCode);
            }

            @Override
            protected void deinitGLComponents() {

                time = System.currentTimeMillis();
                for (int i = 0; i < mReleaseCount; i++) {
                    resCode |= BeaurifyJniSdk.nativeReleaseResources();
                    assertEquals(resCode,0);
                }
                time = mReleaseCount == 0 ? (long) ((System.currentTimeMillis() - time) * 1.0f / mReleaseCount) : 0;
                result += "   release完mem" + SysUtil.getNativeMemoryInfo() + "   code" + resCode + "    time" + time + "\n";
                resCode = 0;
                if (mOutTextureId != null || mOutTextureId.length > 1) {
                    GLES20.glDeleteTextures(1, mOutTextureId, 0);
                    GLES20.glDeleteTextures(1, mInTextureId, 0);

                }

            }

            @Override
            public SurfaceTexture getSurfaceTexture() {
                return null;
            }

            @Override
            public void run() {
                System.out.println("OpenGL init OK. start draw1...");
                initEGL();
                result += "初始内存" + SysUtil.getNativeMemoryInfo();
                for (int i = 0; i < mRepeatCount; i++) {
                    initGLComponents();
                    Log.d(LOG_TAG, "OpenGL init OK. start draw3...");
                    draw();
                    deinitGLComponents();
                }


                deinitEGL();
                result += "   release完mem" + SysUtil.getNativeMemoryInfo() ;
                Log.e("xie","执行结果"+result);
                assertEquals(resCode,0);
                synchronized (LOCK) {
                    LOCK.notify();
                }
            }

        };

        synchronized (LOCK) {
            try {
                LOCK.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


          assertEquals(0, 0);
    }


//    @Test
//    public void startActivity(){
//        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        Intent intent = new Intent(context,LoadingActivity.class);
//
//        mActivityRule.launchActivity(intent);
//    }

}
