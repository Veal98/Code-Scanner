package com.cheng.codescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cheng.codescanner.utils.Constant;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        ButterKnife.bind(this);  //绑定初始化ButterKnife
//
//    }
//
//    @OnClick({R.id.create_code, R.id.scan_2code, R.id.scan_bar_code, R.id.scan_code})
//    public void clickListener(View view){
//        Intent intent;
//        switch (view.getId()){
//            case R.id.create_code: //生成码
//                intent = new Intent(this,CreateCodeActivity.class);
//                startActivity(intent);
//                break;
//        }
//    }

    /**
     * 此处监听使用内部类
     * 和匿名内部类不同，使用优点:可以在该类中进行复用,可直接访问外部类的所有界面组件
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button create_code = (Button) findViewById(R.id.create_code);
        Button scan_2code = (Button) findViewById(R.id.scan_2code);
        Button scan_bar_code = (Button) findViewById(R.id.scan_bar_code);
        Button scan_code = (Button) findViewById(R.id.scan_code);
        // 直接new一个内部类对象作为参数
        create_code.setOnClickListener(new ClickListener());
        scan_2code.setOnClickListener(new ClickListener());
        scan_bar_code.setOnClickListener(new ClickListener());
        scan_code.setOnClickListener(new ClickListener());
    }

    // 定义一个内部类，实现View.OnClickListener接口，并重写onClick()方法
    class ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            Intent intent;
            switch (view.getId()){
                case R.id.create_code: //生成码
                    intent = new Intent(MainActivity.this,CreateCodeActivity.class);
                    startActivity(intent);
                    break;

                case R.id.scan_2code: //扫描二维码
                    intent = new Intent(MainActivity.this, CommonScanActivity.class);
                    // putExtra("A",B)中，AB为键值对，第一个参数为键名，第二个参数为键对应的值。
                    // 如果想取出Intent对象中的这些值，需要在另一个Activity中用getXXXXXExtra方法，注意需要使用对应类型的方法，参数为键名
                    intent.putExtra(Constant.REQUEST_SCAN_MODE,Constant.REQUEST_SCAN_MODE_QRCODE_MODE);
                    startActivity(intent);
                    break;

                case R.id.scan_bar_code: //扫描条形码
                    intent = new Intent(MainActivity.this, CommonScanActivity.class);
                    intent.putExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_BARCODE_MODE);
                    startActivity(intent);
                    break;

                case R.id.scan_code: //扫描条形码或者二维码
                    intent = new Intent(MainActivity.this, CommonScanActivity.class);
                    intent.putExtra(Constant.REQUEST_SCAN_MODE,Constant.REQUEST_SCAN_MODE_ALL_MODE);
                    startActivity(intent);
                    break;

            }
        }
    }
}
