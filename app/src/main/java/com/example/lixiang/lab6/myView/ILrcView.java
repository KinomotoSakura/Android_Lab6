package com.example.lixiang.lab6.myView;

import android.view.View;

import com.example.lixiang.lab6.myView.LrcRow;
import java.util.List;

public interface ILrcView {
    void setLrc(List<LrcRow> lrcRows);
    void setVisibility(int visibility);
    void setOnClickListener(View.OnClickListener l);
    void seekLrcToTime(long time);  //音乐播放的时候调用该方法滚动歌词，高亮正在播放的那句歌
}
