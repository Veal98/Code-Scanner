package com.cheng.codescanner;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.codescanner.utils.Constant;
import com.cheng.codescanner.zxing.ScanListener;
import com.cheng.codescanner.zxing.ScanManager;
import com.cheng.codescanner.zxing.decode.DecodeThread;
import com.cheng.codescanner.zxing.decode.Utils;
import com.google.zxing.Result;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class CommonScanActivity extends Activity implements ScanListener, View.OnClickListener {

    @Bind(R.id.service_register_rescan) //再次扫描
    Button rescan;
    @Bind(R.id.scan_image) //扫描的图片
    ImageView scan_image;
    @Bind(R.id.authorize_return) //左上角返回键
    ImageView authorize_return;
    @Bind(R.id.common_title_TV_center) //扫描界面顶部Title
    TextView title;
    @Bind(R.id.scan_hint) //提示性文字
    TextView scan_hint;
    @Bind(R.id.tv_scan_result) //扫描结果
    TextView tv_scan_result;

    private int scanMode;  //扫描模式（条形，二维码，全部）

    final int PHOTOREQUESTCODE = 1111;  //读取SD存储卡请求码 ( 用于showPictures() )

    ScanManager scanManager; //用于打开闪光灯

    // View主要适用于主动更新、刷新情况，SurfaceView主要适用于被动更新、刷新情况；
    SurfaceView scanPreview = null;  //整个扫描界面
    View scanContainer;  //整个扫描界面总容器
    View scanCropView;  //扫描的图片和动态红线
    ImageView scanLine;  //动态红线
    TextView iv_light;  //闪光灯
    TextView qrcode_g_gallery;  //打开图库
    TextView qrcode_ic_back;   //取消扫描


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
        // 利用getIntExtra() 取出MainActivity中的putExtra()传入的值,设置扫描模式
        scanMode = getIntent().getIntExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_ALL_MODE);
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
     * void dog没有加任何修饰符，我们通常说这是默认的访问模式。这种访问模式只允许在同一个包中进行访问。
     */
    void initView() {
        // 根据解码模式进行对应的文字匹配显示
        switch(scanMode){
            case DecodeThread.BARCODE_MODE:
                title.setText("条形码扫描");
                scan_hint.setText("将条形码对入取景框，即可自动扫描");
                break;
            case DecodeThread.QRCODE_MODE:
                title.setText("二维码扫描");
                scan_hint.setText("将二维码对入取景框，即可自动扫描");
                break;
            case DecodeThread.ALL_MODE:
                    title.setText("二维码或条形码扫描");
                    scan_hint.setText("将二维码或条形码对入取景框，即可自动扫描");
                    break;
        }
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
        authorize_return.setOnClickListener(this);  //左上角返回主界面
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
            case R.id.authorize_return: //左上角返回
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
     * 实现接口类 ScanListener
     * @param rawResult  结果对象
     * @param bundle  存放了截图，或者是空的
     */
    @Override
    public void scanResult(Result rawResult, Bundle bundle) {  //该方法在ScanManager.java中调用
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
}
