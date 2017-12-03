package com.example.lixiang.lab6.myView;

import android.util.Log;
import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class LrcRow implements Comparable<LrcRow> {
    public final static String TAG = "LrcRow";
    //开始播放的时间
    public String strTime;
    public long time;
    //该行歌词的内容
    public String content;

    public LrcRow(){}

    public LrcRow(String strTime,long time,String content){
        this.strTime = strTime;
        this.time = time;
        this.content = content;
    }

    @Override
    public String toString() {
        return "[" + strTime + " ]"  + content;
    }

    /**
     * 读取歌词的每一行内容，转换为LrcRow，加入到集合中
     */
    public static List<LrcRow> createRows(String standardLrcLine){
        try{
            if(standardLrcLine.indexOf("[") != 0 || standardLrcLine.indexOf("]") != 9 ){
                return null;
            }
            int lastIndexOfRightBracket = standardLrcLine.lastIndexOf("]");
            String content = standardLrcLine.substring(lastIndexOfRightBracket + 1, standardLrcLine.length());

            //将时间格式转换一下  [mm:ss.SS][mm:ss.SS] 转换为  -mm:ss.SS--mm:ss.SS-
            String times = standardLrcLine.substring(0,lastIndexOfRightBracket + 1).replace("[", "-").replace("]", "-");
            //通过 ‘-’ 来拆分字符串
            String arrTimes[] = times.split("-");
            List<LrcRow> listTimes = new ArrayList<LrcRow>();
            for(String temp : arrTimes){
                if(temp.trim().length() == 0){
                    continue;
                }
                //拆分
                LrcRow lrcRow = new LrcRow(temp, timeConvert(temp), content);
                listTimes.add(lrcRow);
            }
            return listTimes;
        }catch(Exception e){
            Log.e(TAG,"createRows exception:" + e.getMessage());
            return null;
        }
    }

    //解析时间
    private static long timeConvert(String timeString){
        //因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位
        //将字符串 XX:XX.XX 转换为 XX:XX:XX
        timeString = timeString.replace('.', ':');
        //将字符串 XX:XX:XX 拆分
        String[] times = timeString.split(":");
        // mm:ss:SS
        return Integer.valueOf(times[0]) * 60 * 1000 +//分
                Integer.valueOf(times[1]) * 1000 +//秒
                Integer.valueOf(times[2]) * 10;//毫秒
    }

    //根据歌词的时间来排序
    @Override
    public int compareTo(@NonNull LrcRow another) {
        return (int)(this.time - another.time);
    }
}
