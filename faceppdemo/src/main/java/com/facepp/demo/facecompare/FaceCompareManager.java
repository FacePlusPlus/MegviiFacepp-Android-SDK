package com.facepp.demo.facecompare;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import com.facepp.demo.FeatureInfoSettingActivity;
import com.facepp.demo.OpenglActivity;
import com.facepp.demo.bean.FaceActionInfo;
import com.facepp.demo.bean.FeatureInfo;
import com.facepp.demo.util.ConUtil;
import com.facepp.demo.util.ICamera;
import com.megvii.facepp.sdk.Facepp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by lijie on 2017/7/19.
 */

public class FaceCompareManager {

    public static final String TAG = "FaceCompareManager";

    private final static String FACE_FEATURE_INFO_FILE = "/feature.info";
    private final static double BEST_LIKE_VALUE = 73.0;

    private List<FeatureInfo> mFeatureData;
    private static FaceCompareManager mInstance;

    public static FaceCompareManager instance() {
        if (mInstance == null) {
            synchronized (FaceCompareManager.class) {
                if (mInstance == null) {
                    mInstance = new FaceCompareManager();
                }
            }
        }

        return mInstance;
    }

    private FaceCompareManager() {
        mFeatureData = new ArrayList<>();
    }

    public boolean addFeature(Context ctx, FeatureInfo info) {
        if (info.feature == null) {
            return false;
        }


        synchronized (this) {
            int num = new Random().nextInt() % 10000;
            info.title = "user_" + Math.abs(num);
            mFeatureData.add(info);

            FeatureInfo[] arr = new FeatureInfo[mFeatureData.size()];
            arr = mFeatureData.toArray(arr);

            if (!serialization(buildFeatureFilePath(ctx), arr)) {
                mFeatureData.remove(info);
                return false;
            }
            return true;
        }

    }

    public List<FeatureInfo> getFeatureData() {
        return mFeatureData;
    }


    public synchronized FeatureInfo compare(Facepp facepp, final byte[] target) {
            double likeMax =0;
            FeatureInfo infoBest=null;
            for (FeatureInfo featureInfo : mFeatureData) {
                if (featureInfo.isSelected) {
                    double like = facepp.faceCompare(featureInfo.feature, target);
                    Log.d(TAG, "title: " + featureInfo.title + ", compare: " + like);
                    if (like>BEST_LIKE_VALUE&&like>likeMax){
                        likeMax=like;
                        infoBest=featureInfo;
                    }

                }

            }
            return infoBest;
    }


    public void loadFeature(Context ctx) {
        FeatureInfo[] featureInfos = inserialization(buildFeatureFilePath(ctx));
        if (featureInfos != null) {
            synchronized (this) {
                mFeatureData = Arrays.asList(featureInfos);
                mFeatureData = new ArrayList<>(mFeatureData);
            }
        }
    }

    public void refresh(Context ctx) {
        FeatureInfo[] arr = new FeatureInfo[mFeatureData.size()];
        arr = mFeatureData.toArray(arr);
        serialization(buildFeatureFilePath(ctx), arr);
    }


    private static String buildFeatureFilePath(Context ctx) {
        String path = ConUtil.getDiskCachePath(ctx);
        String fileName = path + FACE_FEATURE_INFO_FILE;

        return fileName;
    }

    // 序列化
    private static boolean serialization(String path, FeatureInfo[] list) {
        File file = new File(path);

        if (file.exists()) {
            file.deleteOnExit();
        }

        OutputStream outStream = null;
        ObjectOutputStream objOutString = null;
        try {
            outStream = new FileOutputStream(file);
            objOutString = new ObjectOutputStream(outStream);
            objOutString.writeObject(list);

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objOutString != null) try {
                objOutString.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (outStream != null) try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;

    }

    // 反序列化
    private static FeatureInfo[] inserialization(String path) {
        File file = new File(path);
        InputStream inStream = null;
        ObjectInputStream objInStream = null;

        if (!file.exists()) {
            return null;
        }

        try {
            inStream = new FileInputStream(file);
            objInStream = new ObjectInputStream(inStream);
            FeatureInfo[] list = (FeatureInfo[]) objInStream.readObject();
            return list;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (objInStream != null)
                    objInStream.close();

                if (inStream != null)
                    inStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;

    }


    public void startActivity(OpenglActivity activity, Facepp.Face[] faces, ICamera mICamera, byte[] carmeraImgData, boolean isBackCamera, FaceActionInfo faceActionInfo) {
        ArrayList<FeatureInfo> featureInfos=new ArrayList<>();
        for (int i = 0; i < faces.length; i++) {
            Facepp.Face face = faces[i];
            FeatureInfo info = new FeatureInfo();
            info.feature = face.feature;


            Rect rect = face.rect;
            Bitmap bitmap = mICamera.getBitMapWithRect(carmeraImgData, mICamera.mCamera, !isBackCamera, rect);


            if (bitmap != null) {
                String filePath = ConUtil.saveBitmap(activity, bitmap);
                info.imgFilePath = filePath;
            } else {
                Toast.makeText(activity, "图片处理错误", Toast.LENGTH_SHORT).show();
                return;
            }
            FaceCompareManager.instance().addFeature(activity, info);
            featureInfos.add(info);
        }
        Intent intent = new Intent(activity, FeatureInfoSettingActivity.class);
        intent.putExtra("FaceAction", faceActionInfo);
        intent.putExtra("currentInfos", featureInfos);
        activity.startActivity(intent);
    }

    public boolean removeFeatures(Context ctx,ArrayList<FeatureInfo> currentInfos) {
        if (currentInfos == null) {
            return false;
        }

        synchronized (this) {

            mFeatureData.remove(currentInfos);

            FeatureInfo[] arr = new FeatureInfo[mFeatureData.size()];
            arr = mFeatureData.toArray(arr);

            if (!serialization(buildFeatureFilePath(ctx), arr)) {
                mFeatureData.addAll(currentInfos);
                return false;
            }
            return true;
        }
    }

    public boolean removeFeaturesByPath(Context ctx,ArrayList<FeatureInfo> currentInfos) {
        if (currentInfos == null) {
            return false;
        }

        synchronized (this) {
            for (int i=0;i<currentInfos.size();i++){
                for (int j=0;j<mFeatureData.size();j++){
                    if (mFeatureData.get(j).imgFilePath.equals(currentInfos.get(i).imgFilePath)){
                        mFeatureData.remove(j);
                    }
                }
            }
            FeatureInfo[] arr = new FeatureInfo[mFeatureData.size()];
            arr = mFeatureData.toArray(arr);

            if (!serialization(buildFeatureFilePath(ctx), arr)) {
                mFeatureData.addAll(currentInfos);
                return false;
            }
            return true;
        }
    }
}
