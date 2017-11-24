package com.facepp.demo.bean;

import java.io.Serializable;

/**
 * Created by lijie on 2017/7/19.
 */

public class FeatureInfo implements Serializable {

    public int id;
    public String title;
    public byte[] feature;
    public boolean isSelected;
    public String imgFilePath;

}
