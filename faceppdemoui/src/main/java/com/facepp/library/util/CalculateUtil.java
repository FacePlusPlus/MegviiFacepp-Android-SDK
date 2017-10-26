package com.facepp.library.util;

import android.graphics.Rect;
import android.util.Log;

/**
 * Created by xiejiantao on 2017/10/24.
 */

public class CalculateUtil {

    /**
     *   face的检测rect 转为实际的android 坐标系。
     */
    public static Rect calRealSceenRect(Rect rectFace, int width, int height, int rotation ,Boolean isBackCamera) {
        int oriention;
        oriention = rotation / 90;
        Rect rect = new Rect(rectFace);
        for (int i = 0; i < oriention; i++) {
            for (int j = 0; j < 4; j++) {
                int tempt = rect.top;
                rect.top = rect.right;
                rect.right = rect.bottom;
                rect.bottom = rect.left;
                rect.left = tempt;
            }
        }


        //0 横屏 对应竖屏
        int tempt = rect.top;
        rect.top = rect.left;
        rect.left = rect.bottom;
        rect.bottom = rect.right;
        rect.right = tempt;

        //前置 需要镜像
        if (!isBackCamera){
            int tempMirror=rect.top;
            rect.top=rect.bottom;
            rect.bottom=tempMirror;
        }

        //计算实际宽高的高度差  就转化完成android的标准坐标系。
        rect.left = height - rect.left;
        rect.right = height - rect.right;

        if (!isBackCamera) {
            rect.top = width - rect.top;
            rect.bottom = width - rect.bottom;
        }
        Log.d("xie", "xie after" + "rotation" + rotation + "xie top" + rect.top + "left" + rect.left + "bottom" + rect.bottom + "right" + rect.right);
        return rect;
    }
}
