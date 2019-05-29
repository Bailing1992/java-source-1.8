package com.lichao.timedealwith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * describe:
 *
 * @author lichao
 * @date 2018/12/29
 */
public class TimeDealwith {


    /**
     *
     * describe: 时间处理；
     *
     * @param:
     * @return:
     * @auther: lichao
     * @date: 2018/12/29 10:25 AM
     */
    public static void main(String [] args)
    {

        /**
         *
         * describe: 使用Calendar处理当前时间；
         *
         * 可以对时间进行处理；
         *
         */
        int y,m,d,h,mi,s;
        Calendar cal = Calendar.getInstance();
        y = cal.get(Calendar.YEAR);
        m = cal.get(Calendar.MONTH);
        d = cal.get(Calendar.DATE);
        h = cal.get(Calendar.HOUR_OF_DAY);
        mi = cal.get(Calendar.MINUTE);
        s = cal.get(Calendar.SECOND);
        System.out.println("现在时刻是" + y + "年" + m + "月" + d + "日" + h + "时" + mi + "分" + s + "秒");



        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy年MM月dd日hh时mm分ss秒");
        System.out.println(f.format(c.getTime()));


        /**
         *
         * describe: 获取昨天的时间；
         *
         */


        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DATE,-1);
        Date data = cal2.getTime();
        SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd");
        String yesterday = sp.format(data);//获取昨天日期
        System.out.println(f.format(yesterday));


        /**
         *
         * describe: 获取昨天的时间；
         *
         */
        Date data2 = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
        SimpleDateFormat sp2 = new SimpleDateFormat("yyyy-MM-dd");
        String yesterday2 = sp2.format(data2);//获取昨天日期
        System.out.println(f.format(yesterday2));

        /**
         *
         * describe: 使用Data处理当前时间；
         *
         */
        String temp_str;
        Date dt = new Date();
        //最后的aa表示“上午”或“下午” HH表示24小时制 如果换成hh表示12小时制
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss aa");
        temp_str = sdf.format(dt);
        System.out.println(temp_str);

    }


}
