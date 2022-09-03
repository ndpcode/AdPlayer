//*********************************************************************************************************//
//AdVideoSlide class for videos playing that implements the IAdSlide interface.
//Created 03.06.2022
//Created by Novikov Dmitry
//*********************************************************************************************************//

package com.ndpcode.adplayer;

import android.media.MediaPlayer;
import android.view.View;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.Exchanger;

public class AdVideoSlide implements IAdSlide {

    private final AppCompatActivity mainActivity;
    private final VideoView videoView;
    private Exchanger<Boolean> videoStoppedExchanger;
    private Exchanger<Integer> exchSlideControl;
    private String videoPath;

    AdVideoSlide(AppCompatActivity parentActivity, VideoView appVideoView)
    {
        mainActivity = parentActivity;
        videoView = appVideoView;
        videoStoppedExchanger = new Exchanger<>();
    }

    @Override
    public Boolean Load(String sourcePath, Exchanger<Integer> exchanger) {
        //if (!sourcePath)??? return false;
        this.videoPath = sourcePath;
        //if (!exchanger)??? return false;
        this.exchSlideControl = exchanger;

        return true;
    }

    MediaPlayer.OnCompletionListener videoViewCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            try{
                videoView.stopPlayback();
            }
            catch(Exception ex){
                //System.out.println(ex.getMessage());
            }
            try{
                Boolean inputMsg = videoStoppedExchanger.exchange(true);
            }
            catch(InterruptedException ex){
                //System.out.println(ex.getMessage());
            }
        }
    };

    @Override
    public void run() {
        //config video view
        mainActivity.runOnUiThread(() -> {
            videoView.setVideoPath(videoPath);
            videoView.setOnCompletionListener(videoViewCompletionListener);
            videoView.requestFocus(0);
            videoView.start();
            videoView.setVisibility(View.VISIBLE);
        });

        Boolean inputMsg = false;
        while (!inputMsg){
            try{
                inputMsg = videoStoppedExchanger.exchange(false);
                Thread.sleep(500);
            }
            catch(InterruptedException ex){
                //System.out.println(ex.getMessage());
            }
        }

        //set video as invisible
        mainActivity.runOnUiThread(() -> videoView.setVisibility(View.INVISIBLE));

        //message that video show ended
        try{
            Integer externMsg = exchSlideControl.exchange(1);
        }
        catch(InterruptedException ex){
            //System.out.println(ex.getMessage());
        }
    }
}
