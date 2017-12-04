package com.example.lixiang.lab6;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;

import com.example.lixiang.lab6.myView.ILrcView;
import com.example.lixiang.lab6.myView.ILrcBuilder;
import com.example.lixiang.lab6.myView.LrcBuilder;
import com.example.lixiang.lab6.myView.LrcRow;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private ILrcView mLrcView;
    //更新歌词的频率，每秒更新一次
    private int mPalyTimerDuration = 100;
    //更新歌词的定时器
    private Timer mTimer;
    //更新歌词的定时任务
    private TimerTask mTask;
    private ObjectAnimator animator;
    private Button play_button, stop_button, quit_button;
    private TextView state, music_time, total_time, name;
    private ImageView album_image;
    private SeekBar seekBar;
    private IBinder mBinder;
    private boolean isPlaying = false;
    public boolean hasPermission = false;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private ServiceConnection sc;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);
        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = service;
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                sc = null;
            }
        };
        initView();
        bindButton();
        connection();
        setAnimator();
        mThread.start();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 123:
                    total_time.setText(time.format(PlayerGetDuration_Position(103)));
                    music_time.setText(time.format(PlayerGetDuration_Position(104)));  //获得当前播放进度
                    seekBar.setMax(PlayerGetDuration_Position(103));  //设置进度条最大值
                    seekBar.setProgress(PlayerGetDuration_Position(104));//设置进度条进度
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                PlayerSeek(seekBar.getProgress());
                            }
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    break;
            }
        }
    };

    Thread mThread = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (sc != null && hasPermission) {
                    mHandler.obtainMessage(123).sendToTarget();
                }
            }
        }
    };

    private void initView(){
        mLrcView = (ILrcView)findViewById(R.id.lyrics);
        play_button = (Button)findViewById(R.id.play_pause);
        stop_button = (Button)findViewById(R.id.stop);
        quit_button = (Button)findViewById(R.id.quit);
        name = (TextView)findViewById(R.id.name);
        state = (TextView)findViewById(R.id.state);
        music_time = (TextView)findViewById(R.id.time1);
        total_time = (TextView)findViewById(R.id.time2);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        album_image = (ImageView)findViewById(R.id.image);

        mLrcView.setVisibility(View.INVISIBLE);
        album_image.setVisibility(View.VISIBLE);
        if (hasPermission) initLrc();
    }

    private void initLrc() {
        ILrcBuilder builder = new LrcBuilder();
        InputStream in = builder.setDataSource(Environment.getExternalStorageDirectory() + "/Music");
        String lrc = builder.readStreamToString(in, Charset.forName("UTF-8"));
        List<LrcRow> rows = builder.getLrcRows(lrc);
        mLrcView.setLrc(rows);
    }

    private class LrcTask extends TimerTask{
        @Override
        public void run() {
            //获取歌曲播放的位置
            final long timePassed = PlayerGetDuration_Position(104);
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //滚动歌词
                    mLrcView.seekLrcToTime(timePassed);
                }
            });

        }
    };

    private class MyListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.play_pause:
                    PlayerStart_Stop(101);
                    if(mTimer == null){
                        mTimer = new Timer();
                        mTask = new LrcTask();
                        mTimer.scheduleAtFixedRate(mTask, 0, mPalyTimerDuration);
                    }
                    if(isPlaying) {
                        animator.pause();
                        state.setText("Paused");
                        play_button.setText("Play");
                        isPlaying = false;
                    } else{
                        animator.resume();
                        state.setText("Playing");
                        play_button.setText("Paused");
                        isPlaying = true;
                    }
                    break;
                case R.id.stop:
                    PlayerStart_Stop(102);
                    mLrcView.seekLrcToTime(0);
                    state.setText("Stopped");
                    play_button.setText("Play");
                    animator.start();
                    animator.pause();
                    isPlaying = false;
                    break;
                case R.id.quit:
                    mHandler.removeCallbacks(mThread);
                    unbindService(sc);
                    sc = null;
                    try{
                        MainActivity.this.finish(); //结束MainActivity
                        System.exit(0);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case R.id.lyrics:
                        mLrcView.setVisibility(View.INVISIBLE);
                        album_image.setVisibility(View.VISIBLE);
                    break;
                case R.id.image:
                        mLrcView.setVisibility(View.VISIBLE);
                        album_image.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    }

    private void bindButton(){
        play_button.setOnClickListener(new MyListener());
        stop_button.setOnClickListener(new MyListener());
        quit_button.setOnClickListener(new MyListener());
        album_image.setOnClickListener(new MyListener());
        mLrcView.setOnClickListener(new MyListener());
    }

    private void PlayerStart_Stop(int code){ //控制音乐-101播放/暂停-102停止
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try{
            mBinder.transact(code,data,reply,0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private int PlayerGetDuration_Position(int code){ //获得音乐的-103时长-104当前时间
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try{
            mBinder.transact(code,data,reply,0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return reply.readInt();
    }
    private void PlayerSeek(int position){ //设置播放器播放位置
        int code = 105;
        Parcel data = Parcel.obtain();
        data.writeInt(position);
        Parcel reply = Parcel.obtain();
        try{
            mBinder.transact(code,data,reply,0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void connection(){
        Intent intent = new Intent(this,MusicService.class);
        startService(intent);
        bindService(intent, sc, Context.BIND_AUTO_CREATE);
    }

    public void setAnimator(){
        animator = ObjectAnimator.ofFloat(album_image, "rotation", 0, 360);
        animator.setDuration(23333);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.start();
        animator.pause();
    }

    public void verifyStoragePermissions(Activity activity) {
        try {
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "READ_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
            else {
                hasPermission = true;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true;
            PlayerStart_Stop(102);
            initLrc();
        } else {
            Toast.makeText(this,"请允许申请权限之后重新启动！！",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }
}
