package com.cheng.codescanner;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cheng.codescanner.zxing.encode.EncodingHandler;
import com.google.zxing.WriterException;

import java.io.UnsupportedEncodingException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateCodeActivity extends Activity {

     /*ButterKnife绑定控件*/
    @Bind(R.id.et_code_key) //输入要生成码的内容
    EditText etCodeKey;
    @Bind(R.id.btn_create_code) //生成码Button
    Button btnCreateCode;
    @Bind(R.id.iv_2_code) //二维码图片
    ImageView iv2Code;
    @Bind(R.id.iv_bar_code) //条码图片
    ImageView ivBarCode;

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_create_code);
        ButterKnife.bind(this);  //绑定初始化ButterKnife
    }


    /**
     * 按钮监听事件
     */
    @OnClick({R.id.btn_create_code, R.id.btn_create_code_and_img})
    public void clickListener(View view){
        String key = etCodeKey.getText().toString(); //获取输入的内容
        switch (view.getId()){
            case  R.id.btn_create_code: //生成码
                if(TextUtils.isEmpty(key)){
                    Toast.makeText(this,"请输入内容",Toast.LENGTH_SHORT).show();
                }else{
                    create2Code(key);
                    createBarCode(key);
                }
                break;
            case R.id.btn_create_code_and_img: //添加logo
                Bitmap bitmap = create2Code(key);
                Bitmap headBitmap =getHeadBitmap(60);
                if(bitmap != null  && headBitmap != null)
                    createQRCodeBitmapWithPortrait(bitmap, headBitmap);
                break;
        }
    }
    /**
     * 生成二维码
     */
    private Bitmap create2Code(String key){
        Bitmap qrCode = null;
        try {
            qrCode = EncodingHandler.create2Code(key,400);
            iv2Code.setImageBitmap(qrCode);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return qrCode;
    }

    /**
     * 生成条形码
     */
    private Bitmap createBarCode(String key){
        Bitmap barCode = null;
        try {
            barCode = EncodingHandler.createBarCode(key,600,300);
            ivBarCode.setImageBitmap(barCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return barCode;
    }

    /**
     * 初始化图像
     */
    private Bitmap getHeadBitmap(int size) {
        try {
            // BitmapFactory：创建Bitmap的接口类
            // decodeResrource(Resourse, int)： 从图片资源解析
            Bitmap portrait = BitmapFactory.decodeResource(getResources(),R.drawable.robot);
            // 对原有图片压缩显示大小
            Matrix mMatrix = new Matrix();  // 创建矩阵matrix用来操作图像
            float width = portrait.getWidth();  //获取位图的宽度 Bitmap的常用方法
            float height = portrait.getHeight(); //获取位图的高度
            // setScale(float sx,float sy)：缩放-> 参数1：x方向缩放倍数； 参数2：y方向缩放倍数
             mMatrix.setScale(size / width, size / height);
            // public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height, Matrix m, boolean filter)
            // 以source为原图创建新的图片，指定起始坐标，新图像的高宽，对像素值进行变换的可选矩阵，新图像的宽高是否可变。
            return Bitmap.createBitmap(portrait, 0, 0, (int) width, (int) height, mMatrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 在二维码上绘制logo
     */
    // qr是二维码 portrait是logo
    private void createQRCodeBitmapWithPortrait(Bitmap qr, Bitmap portrait) {
        // logo的大小
        int portrait_W = portrait.getWidth();
        int portrait_H = portrait.getHeight();

        // 设置logo要显示的位置，即二维码图片的中间
        int left = (qr.getWidth() - portrait_W) / 2;
        int top = (qr.getHeight() - portrait_H) / 2;
        int right = left + portrait_W;
        int bottom = top + portrait_H;
        // Rect(int left, int top, int right, int bottom): Rect类主要用于表示坐标系中的一块矩形区域，并可以对其做一些简单操作
        Rect rect1 = new Rect(left, top, right, bottom);

        // 取得qr二维码图片上的画笔，即要在二维码图片上绘制我们的logo，绘制内容保存到Bitmap
        Canvas canvas = new Canvas(qr);

        // 设置我们要绘制的范围大小，也就是logo的大小范围
        Rect rect2 = new Rect(0, 0, portrait_W, portrait_H);
        // 开始绘制 drawBitmap(Bitmap,Rect,Rect,Paint);
        // 1.需要绘制的bitmap对象 2.要绘制的bitmap区域 3.要将bitmap绘制在屏幕的什么地方 4.绘制的画笔
        canvas.drawBitmap(portrait, rect2, rect1, null);
    }
}
