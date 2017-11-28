package com.example.lixiang.lab6;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
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

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {
    private ObjectAnimator animator;
    private Button play_button, stop_button, quit_button;
    private TextView state, music_time, total_time, name;
    private ImageView album_image;
    private SeekBar seekBar;
    private IBinder mBinder;
    private boolean flag = false;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private ServiceConnection sc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        findView();
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
                if (sc != null) {
                    mHandler.obtainMessage(123).sendToTarget();
                }
            }
        }
    };

    private void findView(){
        play_button = (Button)findViewById(R.id.play_pause);
        stop_button = (Button)findViewById(R.id.stop);
        quit_button = (Button)findViewById(R.id.quit);
        name = (TextView)findViewById(R.id.name);
        state = (TextView)findViewById(R.id.state);
        music_time = (TextView)findViewById(R.id.time1);
        total_time = (TextView)findViewById(R.id.time2);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        album_image = (ImageView)findViewById(R.id.image);
    }

    private class MyListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.play_pause:
                    PlayerStart_Stop(101);
                    if(flag) {
                        animator.pause();
                        state.setText("Paused");
                        play_button.setText("Play");
                        flag = false;
                    } else{
                        animator.resume();
                        state.setText("Playing");
                        play_button.setText("Paused");
                        flag = true;
                    }
                    break;
                case R.id.stop:
                    PlayerStart_Stop(102);
                    state.setText("Stopped");
                    play_button.setText("Play");
                    animator.start();
                    animator.pause();
                    flag = false;
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
            }
        }
    }

    private void bindButton(){
        play_button.setOnClickListener(new MyListener());
        stop_button.setOnClickListener(new MyListener());
        quit_button.setOnClickListener(new MyListener());
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

}
