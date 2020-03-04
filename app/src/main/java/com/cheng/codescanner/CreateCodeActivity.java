package com.cheng.codescanner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.cheng.codescanner.utils.CommonUtil;
import com.cheng.codescanner.zxing.encode.EncodingHandler;
import com.google.zxing.WriterException;

import java.io.UnsupportedEncodingException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptButton;
import me.leefeng.promptlibrary.PromptDialog;

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
    @Bind(R.id.btn_save_2_code) //保存二维码
    Button save2Code;
    @Bind(R.id.btn_save_bar_code) //保存条形码
    Button saveBarCode;

    Bitmap qrCode = null;
    Bitmap barCode = null;



    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_create_code);
        ButterKnife.bind(this);  //绑定初始化ButterKnife
        //验证权限
        verifyStoragePermissions(this);
    }


    /**
     * 按钮监听事件
     */
    @OnClick({R.id.btn_create_code, R.id.btn_create_code_and_img, R.id.btn_save_2_code, R.id.btn_save_bar_code})
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

//            case R.id.btn_create_code_and_img: //添加logo
//                Bitmap bitmap = create2Code(key);
//                Bitmap headBitmap =getHeadBitmap(60);
//                if(bitmap != null  && headBitmap != null)
//                    createQRCodeBitmapWithPortrait(bitmap, headBitmap);
//                break;
            case R.id.btn_create_code_and_img: //分享图片
                if(qrCode==null){
                    PromptDialog promptDialog = new PromptDialog(this);
                    promptDialog.showError("请先生成码");
                    break;
                }else {
                    showChooseDialog();
                    break;
                }

            case R.id.btn_save_2_code: //保存二维码
                if(qrCode == null){
                    PromptDialog promptDialog = new PromptDialog(this);
                    promptDialog.showError("请先生成二维码");
                    break;
                }else {
                    CommonUtil.saveBitmap2file(qrCode, getApplicationContext());
                    PromptDialog promptDialog1 = new PromptDialog(this);
                    promptDialog1.showSuccess("二维码保存成功");
                    break;
                }

            case R.id.btn_save_bar_code: //保存条形码
                if(barCode == null){
                    PromptDialog promptDialog = new PromptDialog(this);
                    promptDialog.showError("请先生成条形码");
                    break;
                }else {
                    CommonUtil.saveBitmap2file(barCode, getApplicationContext());
                    PromptDialog promptDialog2 = new PromptDialog(this);
                    promptDialog2.showSuccess("条形码保存成功");
                    break;
                }
        }
    }

    /**
     * 生成二维码
     */
    private Bitmap create2Code(String key){
        qrCode = null;
        try {
            // key是二维码代表的内容
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
        barCode = null;
        try {
            barCode = EncodingHandler.createBarCode(key,600,300);
            ivBarCode.setImageBitmap(barCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return barCode;
    }

    /**
     * 初始化logo图像
     */
    private Bitmap getHeadBitmap(int size) {
        try {
            // BitmapFactory：创建Bitmap的接口类
            // decodeResrource(Resourse, int)： 通过资源id获取Bitmap位图
            Bitmap portrait = BitmapFactory.decodeResource(getResources(),R.drawable.robot);
            // 获取到位图之后对位图进行操作
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
        // 1. 获取logo的大小
        int portrait_W = portrait.getWidth();
        int portrait_H = portrait.getHeight();

        // 2. 设置logo要显示的位置，即二维码图片的中间
        int left = (qr.getWidth() - portrait_W) / 2;
        int top = (qr.getHeight() - portrait_H) / 2;
        int right = left + portrait_W;
        int bottom = top + portrait_H;
        // Rect(int left, int top, int right, int bottom): Rect类主要用于表示坐标系中的一块矩形区域，并可以对其做一些简单操作
        Rect rect1 = new Rect(left, top, right, bottom);

        // 3. 准备一个画板 ，在上面放上准备好的二维码图片
        Canvas canvas = new Canvas(qr);

        // 4. 在二维码图片上绘制logo
        // 设置我们要绘制的范围大小，也就是logo的大小范围
        Rect rect2 = new Rect(0, 0, portrait_W, portrait_H);
        // 开始绘制 drawBitmap(Bitmap,Rect,Rect,Paint);
        // 参数1.需要绘制的bitmap对象 2.要绘制的bitmap区域 3.要将bitmap绘制在屏幕的什么地方 4.绘制的画笔
        canvas.drawBitmap(portrait, rect2, rect1, null);


    }

    /**
     * 权限认证
     */

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };


    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 分享图片(直接将bitamp转换为Uri)
     * @param bitmap
     * 由于我们获取到的是图片的Bitmap格式，为了方便直接将其转换为Uri，
     * 可直接调用系统保存图片的方法或者我们上面自定义的图片存储方法，都可以得到Uri。之后给startActivity传入一个ACTION_SEND的Intent
     */
    public void shareImg(Bitmap bitmap){
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");//设置分享内容的类型
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(intent);
    }

    /**
     * 弹出选择框（分享二维码、分享条形码）
     *
     * @author xch
     */
    private void showChooseDialog() {



        AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(this);
        choiceBuilder.setCancelable(false);
        choiceBuilder
                .setTitle("选择")
                .setSingleChoiceItems(new String[]{"分享二维码", "分享条形码 "}, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0://分享二维码
                                        shareImg(qrCode);
                                        break;
                                    case 1:// 从相册选择
                                        shareImg(barCode);
                                        break;
                                    default:
                                        break;
                                }
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        choiceBuilder.create();
        choiceBuilder.show();
    }
}
