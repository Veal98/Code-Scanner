/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cheng.codescanner.zxing.decode;

import android.os.Handler;
import android.os.Looper;

import com.cheng.codescanner.zxing.ScanManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * This thread is decoding the images.
 * 设置解码的格式，这里是一个可以优化的点，可以不用设置这么多格式，只设置与自己业务有关的解码格式。
 */
public class DecodeThread extends Thread {

	public static final String BARCODE_BITMAP = "barcode_bitmap";

	public static final int BARCODE_MODE = 0X100;
	public static final int QRCODE_MODE = 0X200;
	public static final int ALL_MODE = 0X300;

	final ScanManager scanManager;
	static  Map<DecodeHintType, Object> hints;
	Handler handler;
	final CountDownLatch handlerInitLatch;

	public DecodeThread(ScanManager scanManager, int decodeMode) {

		this.scanManager = scanManager;
		handlerInitLatch = new CountDownLatch(1);

		hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);

		Collection<BarcodeFormat> decodeFormats = new ArrayList<BarcodeFormat>();
		decodeFormats.addAll(EnumSet.of(BarcodeFormat.AZTEC));
		decodeFormats.addAll(EnumSet.of(BarcodeFormat.PDF_417));

		switch (decodeMode) {
		case BARCODE_MODE:
			decodeFormats.addAll(DecodeFormatManager.getBarCodeFormats());
			break;

		case QRCODE_MODE:
			decodeFormats.addAll(DecodeFormatManager.getQrCodeFormats());
			break;

		case ALL_MODE:
			decodeFormats.addAll(DecodeFormatManager.getBarCodeFormats());
			decodeFormats.addAll(DecodeFormatManager.getQrCodeFormats());
			break;

		default:
			break;
		}

		hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
	}

	public Handler getHandler() {
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
			// continue?
		}
		return handler;
	}

	/**
	 * 在子线程中实例化了一个 Handler 与当前线程绑定
	 */
	@Override
	public void run() {
		Looper.prepare();
		handler = new DecodeHandler(scanManager, hints);
		handlerInitLatch.countDown();
		Looper.loop();
	}

	public static Map<DecodeHintType, Object> getHints() {
		return hints;
	}
}
