package com.cheng.codescanner;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cheng.codescanner.utils.Constant;
import com.cheng.codescanner.zxing.ScanListener;
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

    private int scanMode;//扫描模式（条形，二维码，全部）

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

    }

    @Override
    public void scanResult(Result rawResult, Bundle bundle) {

    }

    @Override
    public void scanError(Exception e) {

    }
}
