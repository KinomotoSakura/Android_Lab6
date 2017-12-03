package com.example.lixiang.lab6.myView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.lixiang.lab6.myView.ILrcView;

import java.util.List;

public class LrcView extends View implements ILrcView {
    public final static String TAG = "LrcView";
    //模式，只有播放
    public final static int DISPLAY_MODE_NORMAL = 0;
    private int mDisplayMode = DISPLAY_MODE_NORMAL;
    //所有行的歌词
    private List<LrcRow> mLrcRows;
    //当前高亮歌词的行数
    private int mPlayingRow = 0;
    //当前高亮歌词的颜色
    private int mPlayingRowColor = Color.parseColor("#FF4081");
    //不高亮歌词的颜色
    private int mNormalRowColor = Color.GRAY;
    //歌词字体大小
    private int mLrcFontSize = 32;
    //两行歌词之间的间距
    private int mPaddingY = 10;
    private String mLoadingLrcTip = "Downloading lrc...";
    private Paint mPaint;

    public LrcView(Context context, AttributeSet attr) {
        super(context, attr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(mLrcFontSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int height = getHeight(); // height of this view
        final int width = getWidth(); // width of this view
        //当没有歌词的时候
        if (mLrcRows == null || mLrcRows.size() == 0) {
            if (mLoadingLrcTip != null) {
                mPaint.setColor(mPlayingRowColor);
                mPaint.setTextSize(mLrcFontSize);
                mPaint.setTextAlign(Align.CENTER);
                canvas.drawText(mLoadingLrcTip, width / 2, height / 2 - mLrcFontSize, mPaint);
            }
            return;
        }

        int rowY = 0; // vertical point of each row.
        final int rowX = width / 2;
        int rowNum = 0;
        // 第一步-高亮地画出正在要高亮的的那句歌词
        String highlightText = mLrcRows.get(mPlayingRow).content;
        int highlightRowY = height / 2 - mLrcFontSize;
        mPaint.setColor(mPlayingRowColor);
        mPaint.setTextSize(mLrcFontSize);
        mPaint.setTextAlign(Align.CENTER);
        canvas.drawText(highlightText, rowX, highlightRowY, mPaint);

        //第二步-画出正在播放的那句歌词的上面可以展示出来的歌词
        mPaint.setColor(mNormalRowColor);
        mPaint.setTextSize(mLrcFontSize);
        mPaint.setTextAlign(Align.CENTER);
        rowNum = mPlayingRow - 1;
        rowY = highlightRowY - mPaddingY - mLrcFontSize;
        while (rowY > -mLrcFontSize && rowNum >= 0) {
            String text = mLrcRows.get(rowNum).content;
            canvas.drawText(text, rowX, rowY, mPaint);
            rowY -=  (mPaddingY + mLrcFontSize);
            rowNum --;
        }

        //第三步-画出正在播放的那句歌词的下面的可以展示出来的歌词
        rowNum = mPlayingRow + 1;
        rowY = highlightRowY + mPaddingY + mLrcFontSize;
        while (rowY < height && rowNum < mLrcRows.size()) {
            String text = mLrcRows.get(rowNum).content;
            canvas.drawText(text, rowX, rowY, mPaint);
            rowY += (mPaddingY + mLrcFontSize);
            rowNum ++;
        }
    }

    //设置歌词
    public void setLrc(List<LrcRow> lrcRows) {
        mLrcRows = lrcRows;
        invalidate();
    }

    //设置要高亮的歌词为第几行歌词
    public void seekLrc(int position, boolean cb) {
        if (mLrcRows == null || position < 0 || position > mLrcRows.size()) {
            return;
        }
        LrcRow lrcRow = mLrcRows.get(position);
        mPlayingRow = position;
        invalidate();
    }

    //歌词滚动
    public void seekLrcToTime(long time) {
        if (mLrcRows == null || mLrcRows.size() == 0) {
            return;
        }
        if (mDisplayMode != DISPLAY_MODE_NORMAL) {
            return;
        }
        Log.d(TAG, "seekLrcToTime:" + time);

        for (int i = 0; i < mLrcRows.size(); i++) {
            LrcRow current = mLrcRows.get(i);
            LrcRow next = i + 1 == mLrcRows.size() ? null : mLrcRows.get(i + 1);
            /**
             *  正在播放的时间大于current行的歌词的时间而小于next行歌词的时间， 设置要高亮的行为current行
             *  正在播放的时间大于current行的歌词，而current行为最后一句歌词时，设置要高亮的行为current行
             */
            if ((time >= current.time && next != null && time < next.time)
                    || (time > current.time && next == null)){
                seekLrc(i, false);
                return;
            }
        }
    }
}
