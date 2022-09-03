//*********************************************************************************************************//
//A simple full screen ad player for Android. Autostart and launcher.
//Created 03.06.2022
//Created by Novikov Dmitry
//MainActivity class
//*********************************************************************************************************//

package com.ndpcode.adplayer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //hide android navigation bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //create thread for player object
        //task
        Runnable adPlayerTask = () -> {
            AdPlayer myPlayer = new AdPlayer(this, R.id.imageViewMain, R.id.videoViewMain);
            myPlayer.start();
        };
        //and thread
        Thread adPlayerThread = new Thread(adPlayerTask);
        adPlayerThread.start();
    }
}