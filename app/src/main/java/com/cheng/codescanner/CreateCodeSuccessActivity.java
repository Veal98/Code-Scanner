package com.cheng.codescanner;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.cheng.codescanner.utils.CommonUtil;
import com.cheng.codescanner.utils.Constant;
import com.cheng.codescanner.zxing.encode.EncodingHandler;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.google.zxing.WriterException;
import java.io.UnsupportedEncodingException;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;


public class CreateCodeSuccessActivity extends Activity {

    /*ButterKnife绑定控件*/


    @Bind(R.id.btn_add_logo) //添加logo
            Button btnAddLogo;
    @Bind(R.id.btn_share_img) //分享
            Button btnShareImage;
    @Bind(R.id.iv_2_code) //二维码图片
            ImageView iv2Code;
    @Bind(R.id.iv_bar_code) //条码图片
            ImageView ivBarCode;
    @Bind(R.id.btn_save_2_code) //保存二维码
            Button save2Code;
    @Bind(R.id.btn_save_bar_code) //保存条形码
            Button saveBarCode;
    @Bind(R.id.btn_circle_scan) //圆形按钮扫描
            ImageButton circleScan;

    Bitmap qrCode = null;
    Bitmap barCode = null;


    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        qrCode = null;
        barCode = null;
        setContentView(R.layout.activity_create_success);
        ButterKnife.bind(this);  //绑定初始化ButterKnife
        //验证权限

        Intent intent = getIntent();
        String key = intent.getStringExtra("etCodeKey"); //获取MainActivity传值
        create2Code(key);
        createBarCode(key);
        PromptDialog promptDialog = new PromptDialog(this);
        promptDialog.showSuccess("生成码成功！");
    }


    /**
     * 按钮监听事件
     */
    @OnClick({R.id.btn_add_logo,R.id.btn_share_img, R.id.btn_save_2_code, R.id.btn_save_bar_code, R.id.btn_circle_scan})
    public void clickListener(View view){

        switch (view.getId()){

            case R.id.btn_add_logo: //添加logo
                if(qrCode==null){
                    PromptDialog promptDialog = new PromptDialog(this);
                    promptDialog.showError("请先生成码");
                    break;
                }else {
                    Bitmap headBitmap =getHeadBitmap(60);
                    if(headBitmap != null) {
                        createQRCodeBitmapWithPortrait(qrCode, headBitmap);
                        PromptDialog promptDialog = new PromptDialog(this);
                        promptDialog.showSuccess("添加成功！");
                    }
                    break;
                }

            case R.id.btn_share_img: //分享图片
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

            case R.id.btn_circle_scan: //圆形扫描按钮
                Intent intent = new Intent(CreateCodeSuccessActivity.this, CommonScanActivity.class);
                // putExtra("A",B)中，AB为键值对，第一个参数为键名，第二个参数为键对应的值。
                // 如果想取出Intent对象中的这些值，需要在你的另一个Activity中用getXXXXXExtra方法，注意需要使用对应类型的方法，参数为键名
                intent.putExtra(Constant.REQUEST_SCAN_MODE,Constant.REQUEST_SCAN_MODE_ALL_MODE);
                startActivity(intent);
                break;
        }
    }

    /**
     * 生成二维码
     */
    private Bitmap create2Code(String key){
        //生成二维码图片
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
            Bitmap portrait = BitmapFactory.decodeResource(getResources(),R.drawable.light);
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
     * @author smallbeef
     */
    public void showChooseDialog() {

        final String[] stringItems = {"分享二维码", "分享条形码"};
        final ActionSheetDialog dialog = new ActionSheetDialog(this, stringItems, null);
        dialog.title("请选择")//
                .titleTextSize_SP(14.5f)//
                .show();

        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        shareImg(qrCode);
                        break;
                    case 1:
                        shareImg(barCode);
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }


        });
    }


}
