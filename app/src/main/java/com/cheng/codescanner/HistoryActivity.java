package com.cheng.codescanner;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cheng.codescanner.history.DBHelper;
import com.cheng.codescanner.history.History;
import com.flyco.animation.BounceEnter.BounceTopEnter;
import com.flyco.animation.SlideExit.SlideBottomExit;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;

public class HistoryActivity extends Activity {


    @Bind(R.id.lv)
    ListView lv;
    @Bind(R.id.btn_clear_history)
    Button btnClearHistory;

    private ArrayList<History> historyList;

    DBHelper dbHelper = new DBHelper(HistoryActivity.this,"db_history",null, 3);


    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.history_list);
        ButterKnife.bind(this);
        //打开数据库
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        System.out.println("查询数据库");
        //创建游标对象
        Cursor cursor = db.query("history", new String[]{"history_time","history_text"}, null, null, null, null, null);

        historyList = new ArrayList<>();

        //利用游标遍历所有数据对象
        while (cursor.moveToNext()) {
            String history_time = cursor.getString(cursor.getColumnIndex("history_time"));
            String history_text = cursor.getString(cursor.getColumnIndex("history_text"));
            System.out.println("查询结果：" + history_time + history_text);
            History history = new History(history_time, history_text);
            historyList.add(history);
        }

        MyAdapter myAdapter = new MyAdapter();
        lv.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();

        cursor.close(); // 关闭游标，释放资源
    }

    /**
     * 按钮监听事件
     */
    @OnClick({R.id.btn_clear_history})
    public void clickListener(View view) {
        switch (view.getId()) {
            case R.id.btn_clear_history: //清除历史记录
                String sql = "delete from history";
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL(sql);
                PromptDialog promptDialog = new PromptDialog(this);

                //创建游标对象
                Cursor cursor = db.query("history", new String[]{"history_time","history_text"}, null, null, null, null, null);

                historyList = new ArrayList<>();

                //利用游标遍历所有数据对象
                while (cursor.moveToNext()) {
                    String history_time = cursor.getString(cursor.getColumnIndex("history_time"));
                    String history_text = cursor.getString(cursor.getColumnIndex("history_text"));
                    System.out.println("查询结果：" + history_time + history_text);
                    History history = new History(history_time, history_text);
                    historyList.add(history);
                }
                lv.setAdapter(new MyAdapter());
                cursor.close(); // 关闭游标，释放资源

                promptDialog.showSuccess("删除成功！");
                break;
        }
    }

    /**
     * ListView适配器类
     */
    private class MyAdapter extends BaseAdapter {
        //获取集合中有多少条元素,由系统调用
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return historyList.size();
        }

        /**
         * 返回相应条目
         * @param position
         * @return
         */
        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        //由系统调用，返回一个view对象作为listview的条目
        /*
         * position：本次getView方法调用所返回的view对象在listView中处于第几个条目，position的值就为多少
         * */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = new TextView(HistoryActivity.this);
            tv.setTextSize(18);
            tv.setTextColor(Color.BLACK);
            //获取集合中的元素
            History history = historyList.get(position);
            tv.setText(history.toString());

            return tv;
        }

    }

}
