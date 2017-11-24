package com.facepp.demo.bean;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by lijie on 2017/7/21.
 */

public class FaceActionInfo implements Serializable{
    public boolean isStartRecorder;
    public boolean is3DPose;
    public boolean isdebug;
    public boolean isROIDetect;
    public boolean is106Points;
    public boolean isBackCamera;
    public int faceSize;
    public int interval;
    public HashMap<String, Integer> resolutionMap;
    public boolean isFaceProperty;
    public boolean isOneFaceTrackig;
    public String trackModel;
    public boolean isFaceCompare;
}
