package com.facepp.demo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facepp.demo.FeatureInfoSettingActivity;
import com.facepp.demo.R;
import com.facepp.demo.bean.FeatureInfo;
import com.facepp.demo.util.ConUtil;

import java.util.List;

/**
 * Created by lijie on 2017/7/20.
 */

public class FeatureInfoAdapter extends BaseAdapter {

    private Context mCtx;
    private List<FeatureInfo> mData;
    private FeatureInfoSettingActivity.ModifFeatureInfo[] mItemSelectStatusArr;

    public FeatureInfoAdapter(Context ctx, List<FeatureInfo> data, FeatureInfoSettingActivity.ModifFeatureInfo[] selectStatus){
        this.mCtx = ctx;
        this.mData = data;
        this.mItemSelectStatusArr = selectStatus;
    }

    @Override
    public int getCount() {
        int size = mData == null ? 0 : mData.size();
        return size;
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            convertView = View.inflate(mCtx, R.layout.feature_list_item, null);
            holder = new ViewHolder();
            holder.ckChangeListener = new ItemCkChangeListener(position);
            holder.imgHeadIcon = (ImageView) convertView.findViewById(R.id.feature_item_image);
            holder.textUserName = (TextView) convertView.findViewById(R.id.feature_item_username);
            holder.imgSelectStatus = (ImageView) convertView.findViewById(R.id.feature_item_select);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        ItemCkChangeListener ckChangeListener = holder.ckChangeListener;
        ckChangeListener.setPos(position);
        holder.imgSelectStatus.setOnClickListener(ckChangeListener);


        FeatureInfo info = mData.get(position);
        holder.textUserName.setText(info.title);
        FeatureInfoSettingActivity.ModifFeatureInfo modifFeatureInfo = mItemSelectStatusArr[position];
        if (modifFeatureInfo.isSelected) holder.imgSelectStatus.setImageResource(R.drawable.check);
        else holder.imgSelectStatus.setImageResource(R.drawable.uncheck);

        // 显示头像
        if (info.imgFilePath != null){
            int size = ConUtil.dip2px(mCtx, 50f);
            Bitmap bitmap = ConUtil.revitionImage(info.imgFilePath, size, size);
            if (bitmap != null){
                holder.imgHeadIcon.setImageBitmap(bitmap);
            }
        }


        return convertView;
    }

    class ItemCkChangeListener implements View.OnClickListener{

        private int pos;

        public ItemCkChangeListener(int pos){
            this.pos = pos;
        }

        void setPos(int pos){
            this.pos = pos;
        }


        @Override
        public void onClick(View v) {
            ImageView imageView = (ImageView) v;
            String check = (String) imageView.getTag();
            if (check.equals("0")){
                v.setTag("1");
                imageView.setImageResource(R.drawable.check);
                mItemSelectStatusArr[pos].isSelected = true;
            }else if (check.equals("1")){
                v.setTag("0");
                imageView.setImageResource(R.drawable.uncheck);
                mItemSelectStatusArr[pos].isSelected = false;
            }
        }
    }

    class ViewHolder{
        ImageView imgHeadIcon;
        TextView textUserName;
        ImageView imgSelectStatus;

        ItemCkChangeListener ckChangeListener;
    }
}
