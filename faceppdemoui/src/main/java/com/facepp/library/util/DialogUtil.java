package com.facepp.library.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.facepp.library.R;

public class DialogUtil {

    private Activity activity;

    public DialogUtil(Activity activity) {
        this.activity = activity;
    }

    public void showDialog(String message) {
        AlertDialog alertDialog = new Builder(activity)
                .setTitle(message)
                .setNegativeButton(activity.getResources().getString(R.string.complete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                }).setCancelable(false).create();
        alertDialog.show();
    }

    public void showTrackModel(final TextView textView) {
        RadioOnClick OnClick = new RadioOnClick(textView);
        AlertDialog ad = new AlertDialog.Builder(activity).setTitle(activity.getResources().getString(R.string.trackig_mode))
                .setSingleChoiceItems(R.array.trackig_mode_array, OnClick.getIndex(), OnClick).create();
        ListView areaListView = ad.getListView();
        ad.show();
    }


    class RadioOnClick implements DialogInterface.OnClickListener {
        private int index;
        private TextView textView;

        public RadioOnClick(final TextView textView) {
            this.textView = textView;
            String[] array = activity.getResources().getStringArray(R.array.trackig_mode_array);
            String trackModel = textView.getText().toString().trim();
            for (int i = 0; i < array.length; i++) {
                if (trackModel.equals(array[i])) {
                    index = i;
                    break;
                }
            }
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            setIndex(whichButton);
            String[] array = activity.getResources().getStringArray(R.array.trackig_mode_array);
            textView.setText(array[index]);
            dialog.dismiss();
            textView = null;
        }
    }

    public void showEditText(final TextView text, final int index) {
        Builder builder = new Builder(activity);
        builder.setTitle(getTitle(index)); // 设置对话框标题
        builder.setIcon(android.R.drawable.btn_star); // 设置对话框标题前的图标
        LayoutParams tvLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        final EditText edit = new EditText(activity);
        edit.setLayoutParams(tvLp);
        edit.setText(text.getText().toString());
        edit.setSelection(text.getText().toString().length());

        final InputMethodManager imm = (InputMethodManager) edit.getContext()
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);

        builder.setView(edit);
        builder.setPositiveButton(activity.getResources().getString(R.string.complete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String str = edit.getText().toString();
                if (isNum(str)) {
                    ConUtil.showToast(activity, activity.getResources().getString(R.string.number) + "！");
                    return;
                } else {
                    try {
                        String value = getContent(str, index);
                        setTextSzie(text, value.length());
                        text.setText(value);
                    } catch (Exception e) {
                        ConUtil.showToast(activity, activity.getResources().getString(R.string.number) + "！");
                    }
                }

                // 取消重命名时候隐藏软键盘
                imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
            }
        });
        builder.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 取消重命名时候隐藏软键盘
                imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
            }
        });

        builder.setCancelable(true); // 设置按钮是否可以按返回键取消,false则不可以取消
        AlertDialog dialog = builder.create(); // 创建对话框
        dialog.setCanceledOnTouchOutside(true); // 设置弹出框失去焦点是否隐藏,即点击屏蔽其它地方是否隐藏
        dialog.show();
    }

    public static void setTextSzie(TextView text, int num) {
        if (num < 3)
            text.setTextSize(20);
        else if (num < 5)
            text.setTextSize(18);
        else if (num < 7)
            text.setTextSize(16);
        else if (num < 9)
            text.setTextSize(14);
        else if (num >= 9)
            text.setTextSize(12);
    }


    public String getTitle(int index) {
        String title = "请输入";
        String min_vlue = activity.getResources().getString(R.string.min_value);
        String max_vlue = activity.getResources().getString(R.string.max_value);
        switch (index) {
            case 0:
                title = min_vlue + " 33\n" + max_vlue + " 2147483647";
                break;
            case 1:
                title = min_vlue + " 1\n" + max_vlue + " 2147483647";
                break;
            case 2:
                title = min_vlue + " 0\n" + max_vlue + " 1";
                break;
            case 3:
                title = min_vlue + " 0\n" + max_vlue + " 1";
                break;
            case 4:
                title = min_vlue + " 0\n" + max_vlue + " 1";
                break;
        }
        return title;
    }

    public String getContent(String str, int index) {
        String content = str;
        switch (index) {
            case 0:
                long faceSize = (long) Float.parseFloat(content);
                if (faceSize < 33)
                    faceSize = 33;
                else if (faceSize > 2147483647)
                    faceSize = 2147483647;

                content = faceSize + "";
                break;
            case 1:
                long interval = (long) Float.parseFloat(content);
                if (interval < 1)
                    interval = 1;
                else if (interval > 2147483647)
                    interval = 2147483647;

                content = interval + "";
                break;
            case 2:
            case 3:
            case 4:
                float vlaue = Float.parseFloat(content);
                if (vlaue < 0)
                    vlaue = 0;
                else if (vlaue > 1)
                    vlaue = 1;

                content = vlaue + "";
                break;
        }
        return content;
    }

    public boolean isNum(String str) {
        String reg = "[a-zA-Z]+";
        return str.matches(reg);
    }

    public void onDestory() {
        activity = null;
    }
}