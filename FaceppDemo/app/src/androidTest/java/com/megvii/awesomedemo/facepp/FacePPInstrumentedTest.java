package com.megvii.awesomedemo.facepp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.facepp.library.util.ConUtil;
import com.megvii.facepp.sdk.Facepp;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by lijie on 2017/7/26.
 */
@RunWith(AndroidJUnit4.class)
public class FacePPInstrumentedTest {

    private static final String TAG = "FacePPInstrumentedTest";
    public static final String TEST_IMG_FILE = "/Pictures/GPUImage/test.jpg";
//    public static final String TEST_IMG_FILE = "/Pictures/test.jpg";

    private static Facepp sFacepp;
    private static Context sContext;
    private static ConfigInfo sConfigInfo;

    // 测试3dpose
    private final byte FACE_3DPOSE_ATTR = 1 << 0;
    // 测eyestatus
    private final byte FACE_EYESTATUS_ATTR = 1 << 1;
    // 测试mouthstatus
    private final byte FACE_MOUTHSTATUS_ATTR = 1 << 2;
    // 测试MinorityStatus
    private final byte FACE_MINORITYSTATUS_ATTR = 1 << 3;
    // 测试Blurness
    private final byte FACE_BLURNESS_ATTR = 1 << 4;
    // 测试age、gender
    private final byte FACE_AGEGENDER_ATTR = 1 << 5;
    // 测试feature
    private final byte FACE_FEATURE_ATTR = 1 << 6;

    private int mTestAbility = 0;

    @BeforeClass
    public static void firstTestInit() {
        sContext = InstrumentationRegistry.getTargetContext();
        sFacepp = new Facepp();
        
        sConfigInfo = ConfigInfo.loadConfigInfo(sContext);
        Log.d(TAG, "firstTestInit: " + sConfigInfo);
    }
    
    
    @Before
    public void onceTestBefore() {
        // 每个test case测试之前执行
        assertNotNull(sConfigInfo);

        byte[] modelData = ConUtil.getFileContent(sContext, com.facepp.library.R.raw.megviifacepp_0_5_0_model); //
        String errorCode = sFacepp.init(sContext, modelData);
        assertNull(errorCode);

        Facepp.FaceppConfig faceppConfig = sFacepp.getFaceppConfig();
        assertNotNull(faceppConfig);

        faceppConfig.detectionMode = sConfigInfo.detectionMode;
        faceppConfig.interval = sConfigInfo.interval;
        faceppConfig.minFaceSize = sConfigInfo.minFaceSize;
        faceppConfig.one_face_tracking = sConfigInfo.one_face_tracking;
        faceppConfig.rotation = sConfigInfo.rotation;

        sFacepp.setFaceppConfig(faceppConfig);

        // 添加测试项目
        mTestAbility |= FACE_3DPOSE_ATTR;
        mTestAbility |= FACE_EYESTATUS_ATTR;
        mTestAbility |= FACE_MOUTHSTATUS_ATTR;
        mTestAbility |= FACE_AGEGENDER_ATTR;
        mTestAbility |= FACE_MINORITYSTATUS_ATTR;

        Log.d(TAG, "onceTestBefore");
        
    }
    
    
    @After
    public void onceTestAfter() {
        // 每个test case 测试之后执行
        sFacepp.release();
        Log.d(TAG, "onceTestAfter");
    }
    

    @Test
    public void testAllAttribute() throws Exception{
        // 测试所有人脸属性
        String fileName = ConUtil.getSDRootPath() + TEST_IMG_FILE;
        boolean isTestImgExist = new File(fileName).exists();
        assertTrue(isTestImgExist);

        Log.d(TAG, "testAllAttribute: fileName:" + fileName);

        // 获取bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(fileName);
        assertNotNull(bitmap);

        bitmap = adjustPhotoRotation(bitmap, 90);
        assertNotNull(bitmap);

        byte[] yuv21Data = getNV21(bitmap.getWidth(), bitmap.getHeight(), bitmap);

        Facepp.Face[] faces = sFacepp.detect(yuv21Data, bitmap.getWidth(), bitmap.getHeight(), Facepp.IMAGEMODE_NV21);
        assertNotNull(faces);

        Log.d(TAG, "testAllAttribute: size:" + faces.length);
        assertEquals(faces.length, 1);

        testFaceAttrAdapter(faces[0]);
    }

    private void testFaceAttrAdapter(Facepp.Face face){
        if ((mTestAbility & FACE_3DPOSE_ATTR) == FACE_3DPOSE_ATTR){
            test3DPose(face);
        }

        if ((mTestAbility & FACE_EYESTATUS_ATTR) == FACE_EYESTATUS_ATTR){
            testEyeStatus(face);
        }

        if ((mTestAbility & FACE_MOUTHSTATUS_ATTR) == FACE_MOUTHSTATUS_ATTR){
            testMouthStatus(face);
        }

        if ((mTestAbility & FACE_AGEGENDER_ATTR) == FACE_AGEGENDER_ATTR){
            testAgeGender(face);
        }

        if ((mTestAbility & FACE_MINORITYSTATUS_ATTR) == FACE_MINORITYSTATUS_ATTR){
            testMinorityStatus(face);
        }
    }

    private void testAgeGender(Facepp.Face face){
        boolean result = sFacepp.getAgeGender(face);
        assertTrue(result);

        String gender = "man";
        if (face.female > face.male){
            gender = "woman";
        }
        Log.d(TAG, "testAgeGender: female:" + face.female + ", male:" + face.male);
        String attrStr = "\nage: " + (int) Math.max(face.age, 1) + "\ngender: " + gender;
        Log.d(TAG, "testAgeGender:" + attrStr);
    }

    private void test3DPose(Facepp.Face face){
        Log.d(TAG, "test3DPose: ");

    }

    private void testEyeStatus(Facepp.Face face){
        Log.d(TAG, "testEyeStatus: ");
    }

    private void testMouthStatus(Facepp.Face face){
        Log.d(TAG, "testMouthStatus: ");
    }

    private void testMinorityStatus(Facepp.Face face) {
        Log.d(TAG, "testMinorityStatus: ");
        boolean result = sFacepp.getMouthStatus(face);
        assertTrue(result);

        float minority = face.minority;
        Log.d(TAG, "testMinorityStatus: minority:" + minority);

    }

    private void testBlurness(Facepp.Face face){

    }


    private Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);

        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            return bm1;
        } catch (OutOfMemoryError ex) {
        }
        return null;
    }

    byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }
    

}
