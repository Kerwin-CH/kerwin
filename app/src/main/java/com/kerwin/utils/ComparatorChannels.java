package com.kerwin.utils;

import com.kerwin.bean.Channels;

import java.util.Comparator;

/**
 * Created by Kerwin on 2017/3/5.
 * Channels排序比较器
 * 在这里实现排序的具体算法
 */

public class ComparatorChannels implements Comparator<Channels> {
    /**
     * 如果channels1小于channels2,返回一个负数;如果channels1大于channels2，返回一个正数;如果他们相等，则返回0;
     */
    @Override
    public int compare(Channels channels1, Channels channels2) {
        /**
         *采用积分制进行排序比较，高清2分，中央1分；
         */
        int c1 = 0, c2 = 0;

        if (channels1.getName().contains("高清")) c1 += 2;
        if (channels1.getName().contains("CCTV")) c1 += 1;
        if (channels2.getName().contains("高清")) c2 += 2;
        if (channels2.getName().contains("CCTV")) c2 += 1;

        if (c1 > c2) {
            return -1;
        } else if (c1 < c2) {
            return 1;
        }
        return 0;
    }
}
