package com.cheng.codescanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cheng.codescanner.utils.Constant;
import com.cheng.codescanner.zxing.ScanListener;
import com.cheng.codescanner.zxing.ScanManager;
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

    /**
     * 此处监听使用匿名内部类 + ButterKnife
     */
    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.activity_scan_code);
        ButterKnife.bind(this);
        // 利用getIntExtra() 取出putExtra()传入的值
        scanMode = getIntent().getIntExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_ALL_MODE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.qrcode_g_gallery:  //打开图库
                showPictures(PHOTOREQUESTCODE);
                break;
            case R.id.iv_light:
                scanManager.switchLight();
                break;
            case R.id.qrcode_ic_back: //退出扫码
                finish();
                break;
            case R.id.authorize_return: //左上角返回
                finish();
                break;
            default:
                break;

        }
    }

    /**
     * 调用图库，获取本地图片
     * @param requestMode 读取SD存储卡请求码
     */
    public void showPictures(int requestMode){
        // 原：Intent.ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");  //使用setType()进行过滤，选择图片。 intent.setType(“video/;image/”) 同时选择视频和图片
        startActivityForResult(intent, requestMode);
    }
    @Override
    public void scanResult(Result rawResult, Bundle bundle) {

    }

    @Override
    public void scanError(Exception e) {

    }
}
