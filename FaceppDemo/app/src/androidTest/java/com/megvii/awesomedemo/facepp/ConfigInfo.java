package com.megvii.awesomedemo.facepp;

import android.content.Context;

import com.facepp.library.util.ConUtil;
import com.megvii.facepp.sdk.Facepp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by lijie on 2017/7/26.
 */

public class ConfigInfo {

    private static final String CONFIG_FILE_NAME = "/config.txt";


    int minFaceSize;
    int rotation;
    int interval;
    int detectionMode;
    int roi_left;
    int roi_top;
    int roi_right;
    int roi_bottom;
    int one_face_tracking;
    int cameraWidth;
    int cameraHeight;


    @Override
    public String toString() {
        return "ConfigInfo{" +
                "minFaceSize=" + minFaceSize +
                ", rotation=" + rotation +
                ", interval=" + interval +
                ", detectionMode=" + detectionMode +
                ", roi_left=" + roi_left +
                ", roi_top=" + roi_top +
                ", roi_right=" + roi_right +
                ", roi_bottom=" + roi_bottom +
                ", one_face_tracking=" + one_face_tracking +
                ", cameraWidth=" + cameraWidth +
                ", cameraHeight=" + cameraHeight +
                '}';
    }

    static ConfigInfo loadConfigInfo(Context ctx){
        String path = ConUtil.getDiskCachePath(ctx);
        String fileName = path + CONFIG_FILE_NAME;

        File file = new File(fileName);
        if (!file.exists()){
            return createConfigFile(file);
        }

        String encoding = "UTF-8";
        FileInputStream fileInputStream = null;
        InputStreamReader streamReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileInputStream = new FileInputStream(fileName);
            streamReader = new InputStreamReader(fileInputStream, encoding);
            bufferedReader = new BufferedReader(streamReader);

            StringBuilder stringBuilder = new StringBuilder();
            String lineText;
            while( (lineText = bufferedReader.readLine()) != null){
                stringBuilder.append(lineText);
            }

            String configInfo = stringBuilder.toString();
            return parseConfigInfo(configInfo);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (streamReader != null){
                try {
                    streamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fileInputStream != null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return null;
    }

    private static ConfigInfo parseConfigInfo(String configInfo){
        if (configInfo == null || configInfo.length() <= 0)
            return null;

        try {
            ConfigInfo info = new ConfigInfo();
            JSONObject jsonObject = new JSONObject(configInfo);
            info.detectionMode = jsonObject.getInt("detectionMode");
            info.interval = jsonObject.getInt("interval");
            info.minFaceSize = jsonObject.getInt("minFaceSize");
            info.one_face_tracking = jsonObject.getInt("one_face_tracking");
            info.roi_left = jsonObject.getInt("roi_left");
            info.roi_top = jsonObject.getInt("roi_top");
            info.roi_bottom = jsonObject.getInt("roi_bottom");
            info.roi_right = jsonObject.getInt("roi_right");
            info.rotation = jsonObject.getInt("rotation");
            info.cameraWidth = jsonObject.getInt("cameraWidth");
            info.cameraHeight = jsonObject.getInt("cameraHeight");

            return info;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static ConfigInfo createConfigFile(File configfile) {
        final int WIDTH = 1920;
        final int HEIGHT = 1080;

        ConfigInfo configInfo = new ConfigInfo();
        configInfo.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_NORMAL; // 有三种选择
        configInfo.interval = 100;
        configInfo.minFaceSize = 200;
        configInfo.one_face_tracking = 1;
        configInfo.roi_left = 0;
        configInfo.roi_top = 0;
        configInfo.roi_bottom = HEIGHT;
        configInfo.roi_right = WIDTH;
        configInfo.rotation = 270; // 默认为正向
        configInfo.cameraWidth = WIDTH;
        configInfo.cameraHeight = HEIGHT;


        // 拼接json
        String jsonInfo = String.format("{" + "\"minFaceSize\":%d,"
                        + "\"rotation\":%d,"
                        + "\"interval\":%d,"
                        + "\"detectionMode\":%d,"
                        + "\"roi_left\":%d,"
                        + "\"roi_top\":%d,"
                        + "\"roi_right\":%d,"
                        + "\"roi_bottom\":%d,"
                        + "\"one_face_tracking\":%d,"
                        + "\"cameraWidth\":%d,"
                        + "\"cameraHeight\":%d"
                        + "}", configInfo.minFaceSize, configInfo.rotation, configInfo.interval,
                configInfo.detectionMode, configInfo.roi_left, configInfo.roi_top,
                configInfo.roi_right, configInfo.roi_bottom, configInfo.one_face_tracking,
                configInfo.cameraWidth, configInfo.cameraHeight
        );

        FileWriter writer = null;
        BufferedWriter bufferedWriter = null;
        try {
            writer = new FileWriter(configfile, false);
            bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(jsonInfo);
            bufferedWriter.flush();

            return configInfo;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null){
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

}
