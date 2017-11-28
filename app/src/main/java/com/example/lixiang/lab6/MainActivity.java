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
    private IBinder iBinder;
    private boolean flag = false;

    public android.os.Handler handler = new android.os.Handler();
    public ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iBinder = service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            iBinder = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        bindButton();
        connection();
        setAnimator();
    }

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

    private class MyButton implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.play_pause:
                    handler.post(mThread);
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
                    animator.end();
                    PlayerStart_Stop(102);
                    state.setText("Stopped");
                    flag = false;
                    play_button.setText("Play");
                    setAnimator();
                    break;
                case R.id.quit:
                    MainActivity.this.finish();
                    unbindService(sc);
                    sc = null;
                    System.exit(0);
                    break;//退出程序要解绑服务
            }
        }
    }

    private void bindButton(){
        play_button.setOnClickListener(new MyButton());
        stop_button.setOnClickListener(new MyButton());
        quit_button.setOnClickListener(new MyButton());
    }

    private void PlayerStart_Stop(int code){ //控制音乐播放或停止 101为播放 102为停止
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try{
            iBinder.transact(code,data,reply,0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private int PlayerGetDuration_Position(int code){ //获得音乐的时长以及当前播放时间 103 为时长 104为当前时间
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try{
            iBinder.transact(code,data,reply,0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return reply.readInt();
    }
    private void PlayerSeek(int position){ //设置播放器从给定时间点的音乐位置播放
        int code = 105;
        Parcel data = Parcel.obtain();
        data.writeInt(position);
        Parcel reply = Parcel.obtain();
        try{
            iBinder.transact(code,data,reply,0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void connection(){
        Intent intent = new Intent(this,MusicService.class);
        startService(intent);
        bindService(intent, sc, Context.BIND_AUTO_CREATE);
    }
    public Thread mThread = new Thread() {
        @Override
        public void run() {
            SimpleDateFormat time = new SimpleDateFormat("mm:ss");  //定义时间格式
            total_time.setText(time.format(PlayerGetDuration_Position(103)));
            music_time.setText(time.format(PlayerGetDuration_Position(104)));  //获得当前播放进度,并格式化为时间格式
            seekBar.setMax(PlayerGetDuration_Position(103));  //设置进度条最大数值
            seekBar.setProgress(PlayerGetDuration_Position(104));//设置进度条当前进度为音乐当前播放的位置
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {   //实现改变拖动条后设置当前音乐播放进度为相应时间
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        PlayerSeek(seekBar.getProgress()); //将当前音频播放位置设置为进度条的进度
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            handler.postDelayed(mThread, 100);
        }

    };
    public void setAnimator(){
        animator = ObjectAnimator.ofFloat(album_image, "rotation", 0, 360); //0-360度旋转
        animator.setDuration(10000); //转速
        animator.setRepeatCount(ObjectAnimator.INFINITE); //无限次重复
        animator.setRepeatMode(ObjectAnimator.RESTART); //当单词动画播放完成后，重新播放
        animator.setInterpolator(new LinearInterpolator()); //匀速
    }
}
