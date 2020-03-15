package com.cheng.codescanner.history;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    //带全部参数的构造函数，此构造函数必不可少
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    //带两个参数的构造函数，调用的其实是带三个参数的构造函数
    public DBHelper(Context context,String name){
        this(context,name,1);
    }
    //带三个参数的构造函数，调用的是带所有参数的构造函数
    public DBHelper(Context context,String name,int version){
        this(context, name,null,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("Create DataBase!");
        //创建数据库sql语句 并 执行
        String sql = "create table history(history_time varchar(100), history_text varchar(100))";
        db.execSQL(sql);
    }

    /**
     * 在原来的软件上更新会从这里开始，不卸载在线更新
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 2){//如果版本是1.0的，升级下面的内容或修改
            String sql_upgrade = "alter table history add history_time varchar(100)";//增加一个列sex
            db.execSQL(sql_upgrade);
            Log.i("onUpgrade","你在没有卸载的情况下，在线更新了版本3.0,同时列表增加了一个列history_time");
        }
        Log.i("Y","update a Database");
    }
}
