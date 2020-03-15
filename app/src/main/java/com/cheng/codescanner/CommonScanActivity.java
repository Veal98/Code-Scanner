package com.cheng.codescanner;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.cheng.codescanner.history.DBHelper;
import com.cheng.codescanner.utils.Constant;
import com.cheng.codescanner.zxing.ScanListener;
import com.cheng.codescanner.zxing.ScanManager;
import com.cheng.codescanner.zxing.decode.DecodeThread;
import com.cheng.codescanner.zxing.decode.Utils;
import com.flyco.animation.BounceEnter.BounceTopEnter;
import com.flyco.animation.SlideExit.SlideBottomExit;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.google.zxing.Result;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.leefeng.promptlibrary.PromptDialog;

public final class CommonScanActivity extends Activity implements ScanListener, View.OnClickListener {

    @Bind(R.id.service_register_rescan) //再次扫描
    Button rescan;
    @Bind(R.id.scan_image) //扫描的图片
    ImageView scan_image;

    @Bind(R.id.scan_hint) //提示性文字
    TextView scan_hint;
    @Bind(R.id.tv_scan_result) //扫描结果
    TextView tv_scan_result;

    private int scanMode;  //扫描模式（条形，二维码，全部）

    final int PHOTOREQUESTCODE = 1111;  //读取SD存储卡请求码 ( 用于showPictures() )

    ScanManager scanManager; //调用ScanManager实现扫描解码、闪光灯

    // View主要适用于主动更新、刷新情况，SurfaceView主要适用于被动更新、刷新情况；
    SurfaceView scanPreview = null;  //整个扫描界面
    View scanContainer;  //整个扫描界面总容器
    View scanCropView;  //扫描的图片和动态红线
    ImageView scanLine;  //动态红线
    TextView iv_light;  //闪光灯
    TextView qrcode_g_gallery;  //打开图库
    TextView qrcode_ic_back;   //取消扫描

    private SQLiteDatabase db;

    /**
     * 此处监听使用匿名内部类 + ButterKnife
     */
    @Override
    public void onCreate(Bundle icicle){
        // Window：屏幕上的某块显示区域，用来承载 View。
        // WindowManagerService（WMS）：Android 框架层的一个服务进程，用来管理 Window。
        Window window = getWindow();
        // FLAG_KEEP_SCREEN_ON：只要这个界面可见，屏幕就亮着
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(icicle);
        setContentView(R.layout.activity_scan_code);
        ButterKnife.bind(this);
        // 利用getIntExtra() 取出其他Activity中的putExtra()传入的值,设置扫描模式
        scanMode = getIntent().getIntExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_ALL_MODE);
        checkPermission(); //摄像头权限申请

        // 创建数据库
        DBHelper dbHelper = new DBHelper(CommonScanActivity.this, "db_history", null, 3);
        db = dbHelper.getWritableDatabase();

        initView();
    }

    /**
     * The activity has become visible (it is now "resumed").
     * @return
     */
    @Override
    public void onResume(){
        super.onResume();
        scanManager.onResume();
        rescan.setVisibility(View.INVISIBLE);  //再次扫描按钮设为不可见
        scan_image.setVisibility(View.INVISIBLE);
    }

    /**
     *  Another activity is taking focus (this activity is about to be "paused").
     */
    @Override
    public void onPause(){
        super.onPause();
        scanManager.onPause();
    }

    /**
     * 初始化
     * public void 称之为公共的。被它修饰的类，属性和方法不仅可以跨类访问， 而且允许跨包(package)访问。
     * void 没有加任何修饰符，我们通常说这是默认的访问模式。这种访问模式只允许在同一个包中进行访问。
     */
    void initView() {
        // 根据解码模式进行对应的文字匹配显示
        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);  //整个扫描界面
        scanContainer = findViewById(R.id.capture_container);  //整个扫描界面容器
        scanCropView = findViewById(R.id.capture_crop_view);  //扫描框和动态红线
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);  //动态红线
        qrcode_g_gallery = (TextView) findViewById(R.id.qrcode_g_gallery);  //图库
        qrcode_g_gallery.setOnClickListener(this);  //绑定监听事件
        qrcode_ic_back = (TextView) findViewById(R.id.qrcode_ic_back);  //取消扫描
        qrcode_ic_back.setOnClickListener(this);
        iv_light = (TextView) findViewById(R.id.iv_light);  //闪光灯
        iv_light.setOnClickListener(this);
        rescan.setOnClickListener(this);  //为再次扫描绑定监听事件
        // 构造出扫描管理器
        scanManager = new ScanManager(this, scanPreview, scanContainer, scanCropView, scanLine, scanMode,this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.qrcode_g_gallery:  //打开图库
                showPictures(PHOTOREQUESTCODE);
                break;
            case R.id.iv_light:  //闪光灯切换
                scanManager.switchLight();
                break;
            case R.id.qrcode_ic_back: //退出扫码
                finish();
                break;
            case R.id.service_register_rescan: //再次扫描
                startScan();
                break;
            default:
                break;

        }
    }

    /**
     * 调用图库，获取所有本地图片
     * @param requestMode 读取SD存储卡请求码
     */
    public void showPictures(int requestMode){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);   // 原：Intent.ACTION_PICK
        intent.setType("image/*");  //使用setType()进行过滤，选择图片。 intent.setType(“video/;image/”) 同时选择视频和图片
        startActivityForResult(intent, requestMode);
        // 请求码----requestCode：自己设置（PHOTOREQUESTCODE）
        // 请求结果----- resultCode ：如若正确返回，则返回RESULT_OK
    }

    /**
     * 选取图库中的图片
     * @param requestCode  startActivityForResult(intent, requestMode);
     * @param resultCode  返回码
     * @param data 传过来的数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String photo_path;
        if (resultCode == RESULT_OK) {  //首先判断是否返回成功
            switch (requestCode) {
                case PHOTOREQUESTCODE:
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = this.getContentResolver().query(data.getData(), proj, null, null, null);
                    if (cursor.moveToFirst()) {
                        int colum_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        photo_path = cursor.getString(colum_index);
                        if (photo_path == null) {
                            photo_path = Utils.getPath(getApplicationContext(), data.getData());
                        }
                        scanManager.scanningImage(photo_path);
                    }
            }
        }
    }

    /**
     * 再次扫描
     */
    void startScan(){
        // Visibility属性有三种状态,分别为:“visible ”、“invisible”、“gone”。其主要作用就是用来设置对应控件的显示和隐藏。
        // 其中“visible ”为控件可见状态；“invisible”、“gone”均为隐藏，但是两者有区别。
        // 控件Visibility属性设置为“invisible”的时候，该控件不可见，但是UI上面会预留其所对的控件占用的控件；
        // 而设置为“gone”的时候，在UI上面是不会给其预留空间的。

        // 初次扫描的时候再次扫描按钮是不可见的，如果再次扫描按钮是可见的（即已经扫描过图片了）
        if(rescan.getVisibility() == View.VISIBLE){
            rescan.setVisibility(View.INVISIBLE); //将再次扫描按钮置为不可见
            scan_image.setVisibility(View.GONE);  //将中间的扫描图片置为不可见（扫描框仍然是可见的）
            scanManager.reScan();  //调用zxing库中ScanManager类的reScan方法
        }
    }

    /**
     * 扫描结果
     * 实现接口类 ScanListener
     * @param rawResult  结果对象
     * @param bundle  存放了截图，或者是空的
     */
    @Override
    public void scanResult(final Result rawResult, Bundle bundle) {  //该方法在ScanManager.java中调用
        if(!scanManager.isScanning()){  //如果当前不是在扫描状态
            //设置再次扫描按钮对用户可见
            rescan.setVisibility(View.VISIBLE);
            scan_image.setVisibility(View.VISIBLE);
            Bitmap barcode = null;
            // Bundle类用作携带数据，它类似于Map，用于存放key-value名值对形式的值
            // Bundle类的方法：public byte[] getByteArray (String key)
            // Returns the value associated with the given key,
            // or null if no mapping of the desired type exists for the given key or a null value is explicitly associated with the key.
            byte[] compressBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
            if(compressBitmap != null){
                //将图片流转化为Bitmap类型
                barcode = BitmapFactory.decodeByteArray(compressBitmap, 0, compressBitmap.length, null);
                //复制位图。根据该位图的大小产生一个新位图，根据指定的结构设置新位图的结构，然后把位图的像素拷贝到新位图中。
                barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
            }
            scan_image.setImageBitmap(barcode);
        }
        // 显示扫描结果
        rescan.setVisibility(View.VISIBLE);  //结果界面设置再次扫描按钮对用户可见
        scan_image.setVisibility(View.VISIBLE);
        tv_scan_result.setVisibility(View.VISIBLE);
        tv_scan_result.setText("扫描结果：" + rawResult.getText());

        // 获取当前时间
        Date date = new Date();
        String time = date.toLocaleString();
        System.out.println(time);

        // 插入数据库
        ContentValues contentValues = new ContentValues();
        contentValues.put("history_time",time);
        contentValues.put("history_text", rawResult.getText());
        db.insert("history", null, contentValues);
        System.out.println("插入成功");



        // 如果扫描结果为网址：则直接打开浏览器加载
        if(rawResult.getText().startsWith("http://") || rawResult.getText().startsWith("https://")){
            Uri url = Uri.parse(rawResult.getText());
            Intent intent = new Intent(Intent.ACTION_VIEW, url);
            startActivity(intent);
        }
        // 扫描结果是图片
//        else if(rawResult.getText().endsWith(".jpg") || rawResult.getText().endsWith(".jpeg") || rawResult.getText().endsWith(".png")){
//            Uri url = Uri.parse(rawResult.getText());
//            Intent intent = new Intent(Intent.ACTION_VIEW, url);
//            startActivity(intent);
//        }

        // 扫描结果是数字
        else {
            // 进入动画
            BounceTopEnter mBasIn = new BounceTopEnter();
            // 退出动画
            SlideBottomExit mBasOut = new SlideBottomExit();
            final NormalDialog dialog = new NormalDialog(this);
            dialog.content(rawResult.getText()) // （必须）内容文案
                    .btnNum(2)
                    .btnText("取消","复制")
                    // .contentGravity(Gravity.CENTER_VERTICAL)  // 内容的显示位置，默认为Gravity.CENTER_VERTICAL
                    // .contentTextColor(Color.RED) // 内容文字的颜色
                    // .contentTextSize(10)  // 内容文字大小，单位sp
                    // .isTitleShow(true)  // 是否显示标题，默认显示
                    .title("扫描结果")   // （必须）设置标题,如果不设置标题默认为：“温馨提示”
                    // .titleTextColor(Color.RED)  // 标题颜色
                    // .titleTextSize(10)  // 标题字体大小，单位sp
                    // .titleLineColor(Color.RED) // 设置标题下方分割线的颜色
                    // .dividerColor(Color.BLUE) // 设置分隔按钮的线的颜色
                    .cornerRadius(5)  // 设置弹出的dialog的圆角程度，单位dp，默认值为3
                    // .bgColor(Color.BLACK) // 设置dialog的背景颜色，默认为：#ffffff（白色）
                    // .btnTextColor(Color.RED,Color.BLUE) // 设置按钮上字体的颜色
                    .btnPressColor(Color.parseColor("#58ACFA"))// 按钮按下时的颜色
                    .showAnim(mBasIn) //
                    .dismissAnim(mBasOut)//
                    // .widthScale(0.85f)//设置对话框的宽度占屏幕宽度的比例0~1
                    .show();

            dialog.setOnBtnClickL(
                    //取消
                    new OnBtnClickL() {
                        @Override
                        public void onBtnClick() {
                            dialog.dismiss();
                        }
                    },
                    // 复制
                    new OnBtnClickL() {
                        @Override
                        public void onBtnClick() {
                            //获取剪贴板管理器：
                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            // 将ClipData内容放到系统剪贴板里。
                            cm.setPrimaryClip(ClipData.newPlainText("copy", rawResult.getText()));
                            dialog.dismiss();
                            PromptDialog promptDialog = new PromptDialog(CommonScanActivity.this);
                            promptDialog.showSuccess("复制成功");

                        }
                    });
        }





    }

    /**
     * 实现ScanListener的接口
     * @param e
     */
    @Override
    public void scanError(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        //相机扫描出错时
        if(e.getMessage()!=null&&e.getMessage().startsWith("相机")){
            scanPreview.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 相机权限动态申请
     */

    public void checkPermission()
    {
        int targetSdkVersion = 0;
        String[] PermissionString={Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        try {
            final PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;//获取应用的Target版本
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
//            Log.e("err", "检查权限_err0");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Build.VERSION.SDK_INT是获取当前手机版本 Build.VERSION_CODES.M为6.0系统
            //如果系统>=6.0
            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                //第 1 步: 检查是否有相应的权限
                boolean isAllGranted = checkPermissionAllGranted(PermissionString);
                if (isAllGranted) {
                    //Log.e("err","所有权限已经授权！");
                    return;
                }
                // 一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
                ActivityCompat.requestPermissions(this,
                        PermissionString, 1);
            }
        }
    }

    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                //Log.e("err","权限"+permission+"没有授权");
                return false;
            }
        }
        return true;
    }

    //申请权限结果返回处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                // 所有的权限都授予了
                Log.e("err","权限都授权了");
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                //容易判断错
                //MyDialog("提示", "某些权限未开启,请手动开启", 1) ;
            }
        }
    }
}
