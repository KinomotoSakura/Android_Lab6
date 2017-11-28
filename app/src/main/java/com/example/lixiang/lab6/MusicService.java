package com.example.lixiang.lab6;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    public final IBinder binder = new MyBinder();

    public class MyBinder extends Binder{
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch(code){
                case 101:
                    playOrPause();
                    break;
                case 102:
                    stop();
                    break;
                case 103:
                    reply.writeInt(mediaPlayer.getDuration());
                    break;
                case 104:
                    reply.writeInt(mediaPlayer.getCurrentPosition());
                    break;
                case 105:
                    mediaPlayer.seekTo(data.readInt());
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
            mediaPlayer.setDataSource("/data/melt.mp3");
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private void playOrPause(){
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }
    private void stop(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
