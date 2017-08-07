package com.facepp.library;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facepp.library.util.ConUtil;
import com.facepp.library.util.DialogUtil;
import com.facepp.library.util.ICamera;
import com.megvii.facepp.sdk.Facepp;

import java.util.ArrayList;
import java.util.HashMap;

import static android.os.Build.VERSION_CODES.M;

public class FaceppActionActivity extends Activity implements OnClickListener {

    private int min_face_size = 200;
    private int detection_interval = 100;
    private String resolution = "640*480";
    private ArrayList<HashMap<String, Integer>> cameraSize;
    private RelativeLayout mListRel;
    private ListView mListView;
    private ListAdapter mListAdapter;
    private LayoutInflater mInflater;
    private boolean isShowListView;
    private HashMap<String, Integer> resolutionMap;
    private DialogUtil mDialogUtil;

    private boolean isStartRecorder, is3DPose, isDebug, isROIDetect, is106Points, isBackCamera, isFaceProperty,
            isOneFaceTrackig;
    private int[] imageItemImages_gray = {R.drawable.record_gray, R.drawable.three_d_gray, R.drawable.debug_gray,
            R.drawable.area_gray, R.drawable.point81, R.drawable.frontphone, R.drawable.faceproperty_gray, R.drawable
            .debug_gray};
    private int[] imageItemImages_blue = {R.drawable.record_blue, R.drawable.three_d_blue, R.drawable.debug_blue,
            R.drawable.area_blue, R.drawable.point106, R.drawable.backphone, R.drawable.faceproperty_blue, R.drawable
            .debug_blue};
    private int[] imageItemTexts = {R.string.record, R.string.pose_3d, R.string.debug, R.string.roi, R.string.landmarks, R.string.front, R.string.attributes, R.string.trackig_mode};
    private int[] editItemStrs = {R.string.min_face, R.string.resolution, R.string.interval, R.string.one_face_trackig, R.string.trackig_mode};

    private RelativeLayout[] imageItem_Rels;
    private RelativeLayout[] textItem_Rels;
    private TextView[] editItemTexts;
    private String[] editValues = {min_face_size + "", resolution, detection_interval + "", "NO", "Normal"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faceppaction_layout);

        init();
        initData();
        onClickListener();
        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestCameraPerm();
            }
        }, 500);
    }

    private void init() {
        mDialogUtil = new DialogUtil(this);
        TextView title = (TextView) findViewById(R.id.title_layout_titleText);
        title.setText(getResources().getString(R.string.title));
        findViewById(R.id.title_layout_returnRel).setVisibility(View.GONE);
        Button enterBtn = (Button) findViewById(R.id.landmark_enterBtn);
        enterBtn.setText(getResources().getString(R.string.detect_face));
        enterBtn.setOnClickListener(this);
        mInflater = LayoutInflater.from(this);
        findViewById(R.id.activity_rootRel).setOnClickListener(this);
        mListRel = (RelativeLayout) findViewById(R.id.landmark_listRel);
        mListRel.setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.landmark_list);
        mListView.setVerticalScrollBarEnabled(false);

        mListAdapter = new ListAdapter();
        mListView.setAdapter(mListAdapter);
        RelativeLayout rel0 = (RelativeLayout) findViewById(R.id.landmark_imageitem_0);
        RelativeLayout rel1 = (RelativeLayout) findViewById(R.id.landmark_imageitem_1);
        RelativeLayout rel2 = (RelativeLayout) findViewById(R.id.landmark_imageitem_2);
        RelativeLayout rel3 = (RelativeLayout) findViewById(R.id.landmark_imageitem_3);
        RelativeLayout rel4 = (RelativeLayout) findViewById(R.id.landmark_imageitem_4);
        RelativeLayout rel5 = (RelativeLayout) findViewById(R.id.landmark_imageitem_5);
        RelativeLayout rel6 = (RelativeLayout) findViewById(R.id.landmark_imageitem_6);
        imageItem_Rels = new RelativeLayout[]{rel0, rel1, rel2, rel3, rel4, rel5, rel6};
        RelativeLayout textRel0 = (RelativeLayout) findViewById(R.id.landmark_edititem_0);
        RelativeLayout textRel1 = (RelativeLayout) findViewById(R.id.landmark_edititem_1);
        RelativeLayout textRel2 = (RelativeLayout) findViewById(R.id.landmark_edititem_2);
        RelativeLayout textRel3 = (RelativeLayout) findViewById(R.id.landmark_edititem_3);
        RelativeLayout textRel4 = (RelativeLayout) findViewById(R.id.landmark_edititem_4);
        textItem_Rels = new RelativeLayout[]{textRel0, textRel1, textRel2, textRel3, textRel4};
    }

    private void initData() {
        for (int i = 0; i < imageItem_Rels.length; i++) {
            imageItem_Rels[i].setOnClickListener(this);
            ImageView image = (ImageView) imageItem_Rels[i].findViewById(R.id.image_item_image);
            image.setImageResource(imageItemImages_gray[i]);
            TextView text = (TextView) imageItem_Rels[i].findViewById(R.id.image_item_text);
            text.setText(getResources().getString(imageItemTexts[i]));
            text.setTextColor(0XFFD0D0D0);
            if (i == 5) {
                text.setTextColor(0XFF30364C);
                text.setText(getResources().getString(R.string.front));
            } else if (i == 4) {
                text.setTextColor(0XFF30364C);
                text.setText(getResources().getString(R.string.landmarks));
            }
        }

        editItemTexts = new TextView[5];
        for (int i = 0; i < textItem_Rels.length; i++) {
            textItem_Rels[i].setOnClickListener(this);
            TextView text = (TextView) textItem_Rels[i].findViewById(R.id.edit_item_text);
            text.setText(getResources().getString(editItemStrs[i]));
            editItemTexts[i] = (TextView) textItem_Rels[i].findViewById(R.id.edit_item_edit);
            editItemTexts[i].setText(editValues[i]);
            mDialogUtil.setTextSzie(editItemTexts[i], editValues[i].length());
        }

        editItemTexts[1].setFocusable(false);
        editItemTexts[1].setClickable(false);
        // editItemedits[1].setTextSize(18);
    }

    private void onclickImageItem(int index, boolean isSelect) {
        ImageView image = (ImageView) imageItem_Rels[index].findViewById(R.id.image_item_image);
        TextView text = (TextView) imageItem_Rels[index].findViewById(R.id.image_item_text);
        if (isSelect) {
            text.setTextColor(0XFF30364C);
            image.setImageResource(imageItemImages_blue[index]);
            if (index == 5)
                text.setText(getResources().getString(R.string.back));
        } else {
            if (index != 5 && index != 4)
                text.setTextColor(0XFFD0D0D0);

            image.setImageResource(imageItemImages_gray[index]);
            if (index == 5)
                text.setText(getResources().getString(R.string.front));
        }
    }

    private void requestCameraPerm() {
        if (android.os.Build.VERSION.SDK_INT >= M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        EXTERNAL_STORAGE_REQ_CAMERA_CODE);
            } else
                getCameraSizeList();
        } else
            getCameraSizeList();
    }

    public static final int EXTERNAL_STORAGE_REQ_CAMERA_CODE = 10;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == EXTERNAL_STORAGE_REQ_CAMERA_CODE)
            getCameraSizeList();
    }


    private void getCameraSizeList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int cameraId = 1;
                if (isBackCamera)
                    cameraId = 0;
                cameraSize = ICamera.getCameraPreviewSize(cameraId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private void isShowListView() {
        isShowListView = !isShowListView;
        if (isShowListView)
            mListRel.setVisibility(View.GONE);
        else
            mListRel.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();
        if (ID == R.id.title_layout_returnRel) {
            finish();
        } else if (ID == R.id.landmark_edititem_0) {
            mDialogUtil.showEditText(editItemTexts[0], 0);
        } else if (ID == R.id.landmark_edititem_1) {
            ConUtil.isGoneKeyBoard(FaceppActionActivity.this);
            isShowListView();
        } else if (ID == R.id.landmark_edititem_2) {
            mDialogUtil.showEditText(editItemTexts[2], 1);
        } else if (ID == R.id.landmark_edititem_3) {
            isOneFaceTrackig = !isOneFaceTrackig;
            if (isOneFaceTrackig)
                editItemTexts[3].setText(getResources().getString(R.string.one_face_trackig_true));
            else
                editItemTexts[3].setText(getResources().getString(R.string.one_face_trackig_false));
        } else if (ID == R.id.landmark_edititem_4) {
            mDialogUtil.showTrackModel(editItemTexts[4]);
        } else if (ID == R.id.landmark_listRel) {
            isShowListView();
        } else if (ID == R.id.activity_rootRel) {
            ConUtil.isGoneKeyBoard(FaceppActionActivity.this);
        } else if (ID == R.id.landmark_imageitem_0) {
            isStartRecorder = !isStartRecorder;
            onclickImageItem(0, isStartRecorder);
        } else if (ID == R.id.landmark_imageitem_1) {
            is3DPose = !is3DPose;
            onclickImageItem(1, is3DPose);
        } else if (ID == R.id.landmark_imageitem_2) {
            isDebug = !isDebug;
            onclickImageItem(2, isDebug);
        } else if (ID == R.id.landmark_imageitem_3) {
            isROIDetect = !isROIDetect;
            onclickImageItem(3, isROIDetect);
        } else if (ID == R.id.landmark_imageitem_4) {
            is106Points = !is106Points;
            onclickImageItem(4, is106Points);
        } else if (ID == R.id.landmark_imageitem_5) {
            isBackCamera = !isBackCamera;
            onclickImageItem(5, isBackCamera);
            getCameraSizeList();
        } else if (ID == R.id.landmark_imageitem_6) {
            if (!Facepp.getAbility(ConUtil.getFileContent(this, R
                    .raw.megviifacepp_0_4_7_model)).contains(Facepp.Ability.AGEGENDER)) {
                ConUtil.showToast(this, getResources().getString(R.string.detector));
                return;
            }
            isFaceProperty = !isFaceProperty;
            onclickImageItem(6, isFaceProperty);
        } else if (ID == R.id.landmark_enterBtn) {
            min_face_size = (int) Long.parseLong(editItemTexts[0].getText().toString());
            detection_interval = (int) Long.parseLong(editItemTexts[2].getText().toString());
            Log.w("ceshi", "min_face_size===" + min_face_size + ", " + detection_interval);

            if (isStartRecorder)
                if (resolutionMap != null) {
                    int width = resolutionMap.get("width");
                    int height = resolutionMap.get("height");
                    if (width == 1056 && height == 864)
                        resolutionMap = null;
                    if (isBackCamera) {
                        if (width == 960 && height == 720)
                            resolutionMap = null;
                        if (width == 800 && height == 480)
                            resolutionMap = null;
                    }
                }

            startActivity(new Intent(FaceppActionActivity.this, OpenglActivity.class)
                    .putExtra("isStartRecorder", isStartRecorder).putExtra("is3DPose", is3DPose)
                    .putExtra("isdebug", isDebug).putExtra("ROIDetect", isROIDetect)
                    .putExtra("is106Points", is106Points).putExtra("isBackCamera", isBackCamera)
                    .putExtra("faceSize", min_face_size).putExtra("interval", detection_interval)
                    .putExtra("resolution", resolutionMap).putExtra("isFaceProperty", isFaceProperty)
                    .putExtra("isOneFaceTrackig", isOneFaceTrackig)
                    .putExtra("trackModel", editItemTexts[4].getText().toString().trim()));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.ACTION_DOWN == event.getAction() && mListRel.getVisibility() != View.GONE) {
            isShowListView();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (cameraSize == null)
                return 0;
            Log.w("ceshi", "cameraSize.size() === " + cameraSize.size());
            return cameraSize.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHoder hoder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.cameralist_item, null);
                hoder = new ViewHoder();
                hoder.name = (TextView) convertView.findViewById(R.id.cameralist_item_nameText);
                convertView.setTag(hoder);
            } else {
                hoder = (ViewHoder) convertView.getTag();
            }

            HashMap<String, Integer> map = cameraSize.get(position);

            hoder.name.setText(map.get("width") + " * " + map.get("height"));
            return convertView;
        }

        class ViewHoder {
            TextView name, time, num;
        }
    }

    private void onClickListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                isShowListView();
                resolutionMap = cameraSize.get(position);
                String str = resolutionMap.get("width") + "*" + resolutionMap.get("height");
                mDialogUtil.setTextSzie(editItemTexts[1], str.length());
                editItemTexts[1].setText(str);
            }
        });
    }
}