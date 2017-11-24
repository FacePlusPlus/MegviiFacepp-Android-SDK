package com.megvii.faceppdemo;

import android.content.Context;
import android.os.Debug;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.facepp.demo.util.ConUtil;

import com.megvii.facepp.sdk.Facepp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

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

//    @Rule
//    public ActivityTestRule<LoadingActivity> mActivityRule = new ActivityTestRule<>(
//            LoadingActivity.class,false,false);

    @Before
    public void useAppContext() throws Exception {
        // Context of the app under test.
         appContext = InstrumentationRegistry.getTargetContext();

       // assertEquals("com.megvii.beautifya", appContext.getPackageName());
    }

    public static int getNativeMemoryInfo() {
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);


        return memoryInfo.nativePss;
    }

    @Test
    public void  initrelease(){
        for (int i=0;i<100;i++){
            Facepp facepp=new Facepp();
            facepp.init(appContext, ConUtil.getFileContent(appContext, com.facepp.demo.R.raw.megviifacepp_0_5_2_model),0);
            facepp.release();
            facepp.shutDown();
            System.out.println("init release memo" + getNativeMemoryInfo());

        }
    }




}
