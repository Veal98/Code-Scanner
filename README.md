# Code-Scanner
Android-二维码/条形码生成与识别系统


####  :+1: 二维码/条形码生成
1. **activity_main.xml** 中定义主界面，通过按钮监听事件与**MainActivity** 绑定，并通过intent跳转到**CreateCodeActivity** （需要在AndroidManifest.xml注册)；
2. **activity_create_code.xml** 中定义生成码界面，通过按钮监听事件（利用ButterKnife）与**CreateCodeActivity** 绑定；  
3. 利用google.zxing库中的==EncodingHandler.createCode/createBarCode== 方法创建二维码/条形码
```java
    //条形码生成
    @Bind(R.id.iv_bar_code)
    ImageView ivBarCode;
    private Bitmap createBarCode(String key) {
        Bitmap qrCode = null;
        qrCode = EncodingHandler.createBarCode(key, 600, 300);
        ivBarCode.setImageBitmap(qrCode);
        return qrCode;
    }
    
    //二维码生成
     @Bind(R.id.iv_2_code)
     ImageView iv2Code;
     private Bitmap create2Code(String key) {
        Bitmap qrCode=null;
        qrCode= EncodingHandler.create2Code(key, 400);
        iv2Code.setImageBitmap(qrCode);
        return qrCode;
    }
```
4. 利用==Rect== 类表示坐标系中的一块矩形区域，通过==Canvas== 类绘制二维码图像中间的logo图片
```java
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
```