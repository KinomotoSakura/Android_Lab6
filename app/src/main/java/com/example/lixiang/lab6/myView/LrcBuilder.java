package com.example.lixiang.lab6.myView;

import android.util.Log;
import com.example.lixiang.lab6.myView.ILrcBuilder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.InputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LrcBuilder implements ILrcBuilder {
    private static final String TAG = "LrcBuilder";

    public InputStream setDataSource(String path){
        try {
            String fileName = "melt.lrc";
            File f = new File(path, fileName);
            return new FileInputStream(f);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String readStreamToString(InputStream inputStream, Charset charset){
        try {
            InputStreamReader inputReader = new InputStreamReader(inputStream, charset);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String result="";
            while((line = bufReader.readLine()) != null){
                if(line.trim().equals(""))
                    continue;
                result += line + "\r\n";
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<LrcRow> getLrcRows(String rawLrc) {
        Log.d(TAG,"getLrcRows by rawString");
        if(rawLrc == null || rawLrc.length() == 0){
            Log.e(TAG,"getLrcRows rawLrc null or empty");
            return null;
        }
        StringReader reader = new StringReader(rawLrc);
        BufferedReader br = new BufferedReader(reader);
        String line = null;
        List<LrcRow> rows = new ArrayList<LrcRow>();
        try{
            //循环地读取歌词的每一行
            do {
                line = br.readLine();
                Log.d(TAG,"lrc raw line: " + line);
                if(line != null && line.length() > 0){
                    //解析每一行歌词 得到每行歌词的集合
                    List<LrcRow> lrcRows = LrcRow.createRows(line);
                    if(lrcRows != null && lrcRows.size() > 0){
                        for(LrcRow row : lrcRows){
                            rows.add(row);
                        }
                    }
                }
            }while(line != null);

            if (rows.size() > 0){
                // 根据歌词行的时间排序
                Collections.sort(rows);
                if(rows!=null && rows.size()>0){
                    for(LrcRow lrcRow:rows){
                        Log.d(TAG, "lrcRow:" + lrcRow.toString());
                    }
                }
            }
        }catch(Exception e){
            Log.e(TAG,"parse exception:" + e.getMessage());
            return null;
        }finally{
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
        }
        return rows;
    }
}
