package com.kerwin.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Kerwin on 2017/3/4.
 */

public class Kutils {
    /**
     * 从asset路径下读取对应文件转String输出
     *
     * @param mContext
     * @return
     */
    public static String getJson(Context mContext, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            // 字节流
            InputStream is = mContext.getAssets().open(fileName);// 打开assets文件夹中的文件
            InputStreamReader isr = new InputStreamReader(is);// 字符流，编码要与指定字节流一样啊  , "UTF-8"
            BufferedReader bfr = new BufferedReader(isr);
            // bfr.readLine();//读取文件中的一行数据
            Log.d("assets", bfr.readLine());
            String in;
            while ((in = bfr.readLine()) != null) {
                stringBuilder.append(in);
                Log.d("content", in);
            }
            is.close();
            isr.close();
            bfr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString().trim();
    }
}
