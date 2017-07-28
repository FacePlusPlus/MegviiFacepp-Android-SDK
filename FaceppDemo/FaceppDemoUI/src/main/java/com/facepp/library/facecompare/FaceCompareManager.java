package com.facepp.library.facecompare;

import android.content.Context;

import com.facepp.library.bean.FeatureInfo;
import com.facepp.library.util.ConUtil;
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
    private final static String FACE_FEATURE_INFO_FILE = "/feature.info";
    private final static double BEST_LIKE_VALUE = 70.0;

    private List<FeatureInfo> mFeatureData;
    private static FaceCompareManager mInstance;

    public static FaceCompareManager instance(){
        if (mInstance == null){
            synchronized (FaceCompareManager.class){
                if (mInstance == null) {
                    mInstance = new FaceCompareManager();
                }
            }
        }

        return mInstance;
    }

    private FaceCompareManager(){
        mFeatureData = new ArrayList<>();
    }

    public boolean addFeature(Context ctx, FeatureInfo info){
        if (info.feature == null){
            return false;
        }


        synchronized (this){
            int num = new Random().nextInt() % 10000;
            info.title = "user_" + Math.abs(num);
            mFeatureData.add(info);

            FeatureInfo[] arr = new FeatureInfo[mFeatureData.size()];
            arr = mFeatureData.toArray(arr);

            if (!serialization(buildFeatureFilePath(ctx), arr))
            {
                mFeatureData.remove(info);
                return false;
            }

            return true;
        }

    }

    public List<FeatureInfo> getFeatureData(){
        return mFeatureData;
    }


    public FeatureInfo compare(Facepp facepp, final byte[] target){
        synchronized (this){
            for(FeatureInfo featureInfo : mFeatureData){
                if (featureInfo.isSelected){
                    double like = facepp.faceCompare(featureInfo.feature, target);
                    if (like >= BEST_LIKE_VALUE){
                        return featureInfo;
                    }
                }

            }
        }

        return null;
    }


    public void loadFeature(Context ctx){
        FeatureInfo[] featureInfos = inserialization(buildFeatureFilePath(ctx));
        if (featureInfos != null){
            synchronized (this) {
                mFeatureData = Arrays.asList(featureInfos);
                mFeatureData = new ArrayList<>(mFeatureData);
            }
        }
    }

    public void refresh(Context ctx){
        FeatureInfo[] arr = new FeatureInfo[mFeatureData.size()];
        arr = mFeatureData.toArray(arr);
        serialization(buildFeatureFilePath(ctx), arr);
    }


    private static String buildFeatureFilePath(Context ctx){
        String path = ConUtil.getDiskCachePath(ctx);
        String fileName = path + FACE_FEATURE_INFO_FILE;

        return fileName;
    }

    // 序列化
    private static boolean serialization(String path, FeatureInfo[] list) {
        File file = new File(path);

        if (file.exists()){
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
        }finally {
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
    private static FeatureInfo[] inserialization(String path){
        File file = new File(path);
        InputStream inStream = null;
        ObjectInputStream objInStream = null;

        if (!file.exists()){
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
        }finally {
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



}
