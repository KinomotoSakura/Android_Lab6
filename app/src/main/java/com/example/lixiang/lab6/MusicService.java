package com.example.lixiang.lab6;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.Environment;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    public final IBinder binder = new MyBinder();

    public class MyBinder extends Binder{
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch(code){
                case 101:  //开始or暂停
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                    }
                    break;
                case 102:  //停止
                    if (mediaPlayer != null){
                        mediaPlayer.stop();
                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory() +"/melt.mp3");
                            mediaPlayer.prepare();
                            mediaPlayer.setLooping(true);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    break;
                case 103:  //获取文件长度
                    if (mediaPlayer != null) {
                        reply.writeInt(mediaPlayer.getDuration());
                    }
                    break;
                case 104:  //获取已播放时间
                    if (mediaPlayer != null) {
                        reply.writeInt(mediaPlayer.getCurrentPosition());
                    }
                    break;
                case 105:  //设置进度
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(data.readInt());
                    }
                    break;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }
    @Override
    public IBinder onBind(Intent intent){
        return binder;
    }
    @Override
    public void onCreate(){
        super.onCreate();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public MusicService(){
        try {
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory() + "/melt.mp3");
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
