package com.kerwin.utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
            String in;
            while ((in = bfr.readLine()) != null) {
                stringBuilder.append(in);
            }
            is.close();
            isr.close();
            bfr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString().trim();
    }

    public static int isIncloudNumerOrLetter(String str) {
        String ruler = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890-_";
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            String temp = String.valueOf(str.charAt(i));
            if (ruler.contains(new String(temp)))
                count++;
        }
        return count;
    }

    /**
     * 写字符串到本地
     *
     * @param msg
     */
    public static void putStringToSD(String msg, String path) {
        FileWriter filerWriter = null;
        BufferedWriter bufWriter = null;
        if (path == null || "".equals(path)) {
            path = "/mnt/sdcard";
            if (Environment.isExternalStorageEmulated()) {
                path = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
        }
        String filePath = path + "/kk_channles.json";

        File f = new File(filePath);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        try {
            filerWriter = new FileWriter(filePath, true);
            bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(msg);
            bufWriter.newLine();
            bufWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufWriter != null)
                    bufWriter.close();
                if (filerWriter != null)
                    filerWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getStringFromSD(String path) {
        String result = "";
        try {
            FileInputStream f = new FileInputStream(path);
            BufferedReader bis = new BufferedReader(new InputStreamReader(f));
            String line = "";
            while ((line = bis.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }
}
