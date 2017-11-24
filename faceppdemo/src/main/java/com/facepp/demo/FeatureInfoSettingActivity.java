package com.facepp.demo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.facepp.demo.adapter.FeatureInfoAdapter;
import com.facepp.demo.bean.FaceActionInfo;
import com.facepp.demo.bean.FeatureInfo;
import com.facepp.demo.facecompare.FaceCompareManager;
import com.facepp.demo.util.ConUtil;
import com.facepp.demo.util.DialogUtil;

import java.util.ArrayList;
import java.util.List;

public class FeatureInfoSettingActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ListView mListView;
    private TextView mCancleText, mSureText;
    private FeatureInfoAdapter mAdapter;
    private FaceActionInfo mFaceActionInfo;
    private DialogUtil mDialog;
    private ArrayList<FeatureInfo> currentInfos;

//    private ArrayList<Boolean> mSelectPos = new ArrayList<>();
    private ModifFeatureInfo[] mItemSelectStatusArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_info_setting);
        ConUtil.toggleHideyBar(this);
        init();
    }

    private void init(){
        mFaceActionInfo = (FaceActionInfo) getIntent().getSerializableExtra("FaceAction");
        currentInfos= (ArrayList<FeatureInfo>) getIntent().getSerializableExtra("currentInfos");
        mListView = (ListView) findViewById(R.id.featureinfo_layout_listview);
        mCancleText = (TextView) findViewById(R.id.featureinfo_layout_cancle);
        mSureText = (TextView) findViewById(R.id.featureinfo_layout_sure);
        TextView title = (TextView) findViewById(R.id.title_layout_titleText);
        title.setText(getResources().getString(R.string.face_compare_title));

        initSelectStatusArr();
        mAdapter = new FeatureInfoAdapter(this, FaceCompareManager.instance().getFeatureData(), mItemSelectStatusArr);
        mListView.setAdapter(mAdapter);


        findViewById(R.id.title_layout_returnRel).setOnClickListener(this);
        findViewById(R.id.featureinfo_layout_cancle).setOnClickListener(this);
        findViewById(R.id.featureinfo_layout_sure).setOnClickListener(this);

        mListView.setOnItemClickListener(this);
        mDialog = new DialogUtil(this);

        Bitmap bitmap = null;
//        bitmap.copyPixelsFromBuffer();
//        bitmap
    }

    private void initSelectStatusArr(){
        List<FeatureInfo> data = FaceCompareManager.instance().getFeatureData();
        mItemSelectStatusArr = new ModifFeatureInfo[data.size()];
        for(int i = 0; i < mItemSelectStatusArr.length; i++){
            ModifFeatureInfo info = new ModifFeatureInfo();
            info.isSelected = data.get(i).isSelected;
            info.name = data.get(i).title;
            mItemSelectStatusArr[i] = info;
        }
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();
        if (ID == R.id.title_layout_returnRel){
            boolean success=  FaceCompareManager.instance().removeFeaturesByPath(FeatureInfoSettingActivity.this,currentInfos);
            startActivity(new Intent(this, OpenglActivity.class).putExtra("FaceAction", mFaceActionInfo));
            finish();
        }else if (ID == R.id.featureinfo_layout_cancle){
            for(int i = 0; i < mItemSelectStatusArr.length; i++){
                mItemSelectStatusArr[i].isSelected = false;
            }

            mAdapter.notifyDataSetChanged();
        }else if (ID == R.id.featureinfo_layout_sure){
            // 只有操作【确定】时，才生效
            List<FeatureInfo> dataList = FaceCompareManager.instance().getFeatureData();
            List<FeatureInfo> deleted=new ArrayList<>();
            for(int i = 0; i < mItemSelectStatusArr.length; i++){
                if (mItemSelectStatusArr[i].isSelected){
                    dataList.get(i).isSelected = mItemSelectStatusArr[i].isSelected;
                    dataList.get(i).title = mItemSelectStatusArr[i].name;
                }else{
                    deleted.add(dataList.get(i));
                }
            }
            dataList.removeAll(deleted);
            FaceCompareManager.instance().refresh(this);
            startActivity(new Intent(this, OpenglActivity.class).putExtra("FaceAction", mFaceActionInfo));
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        boolean success=  FaceCompareManager.instance().removeFeaturesByPath(FeatureInfoSettingActivity.this,currentInfos);
        startActivity(new Intent(this, OpenglActivity.class).putExtra("FaceAction", mFaceActionInfo));
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
       TextView textView = (TextView) view.findViewById(R.id.feature_item_username);
        android.app.AlertDialog dialog=mDialog.showEditText(textView, new DialogUtil.OnEditModifComplateListener() {
            @Override
            public void onModifComplate(String name) {
                mItemSelectStatusArr[position].name = name;
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ConUtil.toggleHideyBar(FeatureInfoSettingActivity.this);
            }
        });

        ConUtil.toggleHideyBar(FeatureInfoSettingActivity.this);

    }

    public static class ModifFeatureInfo{
        public boolean isSelected;
        public String name;
    }
}
