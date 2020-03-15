package com.cheng.codescanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.cheng.codescanner.utils.Constant;
import com.cheng.codescanner.zxing.utils.BeepManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    /*ButterKnife绑定控件*/

    @Bind(R.id.et_code_key) //输入要生成码的内容
    EditText etCodeKey;
    @Bind(R.id.btn_create_code) //生成码Button
    Button btnCreateCode;
    @Bind(R.id.btn_scan_code) //扫描
    Button btnScanCode;
    @Bind(R.id.btn_clear) //清除文本框
    ImageButton btnClear;
    @Bind(R.id.btn_show_history)
    Button btnShowHistory;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    /**
     * 按钮监听事件
     */
    @OnClick({R.id.btn_create_code, R.id.btn_scan_code, R.id.btn_clear,R.id.btn_show_history})
    public void clickListener(View view) {
        String key = etCodeKey.getText().toString(); //获取输入的内容，二维码信息

        switch (view.getId()) {
            case R.id.btn_create_code: //生成码,添加文字
                Intent intent;
                if (TextUtils.isEmpty(key)) {
                    Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
                } else {
                    intent = new Intent(MainActivity.this,CreateCodeSuccessActivity.class);
                    intent.putExtra("etCodeKey", key);
                    startActivity(intent);
                    break;
                }
                break;
            case R.id.btn_scan_code: //扫描条形码或者二维码
                intent = new Intent(MainActivity.this, CommonScanActivity.class);
                // putExtra("A",B)中，AB为键值对，第一个参数为键名，第二个参数为键对应的值。
                // 如果想取出Intent对象中的这些值，需要在你的另一个Activity中用getXXXXXExtra方法，注意需要使用对应类型的方法，参数为键名
                intent.putExtra(Constant.REQUEST_SCAN_MODE,Constant.REQUEST_SCAN_MODE_ALL_MODE);
                startActivity(intent);
                break;
            case R.id.btn_clear: //文本框清除
                etCodeKey.setText("");
                break;
            case R.id.btn_show_history:
                intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
                break;





        }
    }


}
