# Code-Scanner
Android-二维码/条形码生成与识别系统

#### 1. 二维码/条形码生成
1. **activity_main.xml（layout)** 定义主界面;
2. **MainActivity.java** 中通过R.id.xxxx定义Button监听事件,通过intent跳转到另一Activity **CreateCode.Activity** ;(需要在**AndroidManifest.xml** 中注册该Activity);
3. **CreateCodeActivity.java** 通过监听事件（利用ButterKnife)与**activity_create_code.xml(layout)** 绑定，利用导入的zxing库中EncodingHandler (zxing/encode) 的create2Code/createBarCode方法生成二维码和条形码