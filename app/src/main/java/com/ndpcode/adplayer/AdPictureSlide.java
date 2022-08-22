package com.ndpcode.adplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.concurrent.Exchanger;

public class AdPictureSlide implements IAdSlide {

    private final AppCompatActivity mainActivity;
    private final ImageView imageView;

    private Exchanger<Integer> exchSlideControl;
    private String imagePath;
    private Integer imageShowTime = 30; //time for image show, seconds
    final private Integer imageShowMaxTime = 600; //image show max time, seconds
    final private Integer imageShowMinTime = 1; //image show min time, seconds

    AdPictureSlide(AppCompatActivity parentActivity, ImageView appImageView)
    {
        mainActivity = parentActivity;
        imageView = appImageView;
    }

    @Override
    public Boolean Load(String sourcePath, Exchanger<Integer> exchanger) {
        //if (!sourcePath)??? return false;
        this.imagePath = sourcePath;
        //if (!exchanger)??? return false;
        this.exchSlideControl = exchanger;

        return true;
    }

    @Override
    public void run() {
        //load image
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            if (imageBitmap != null)
            {
                mainActivity.runOnUiThread(() -> {
                    //set bitmap for ImageView
                    imageView.setImageBitmap(imageBitmap);
                    //set ImageView as visible
                    imageView.setVisibility(View.VISIBLE);
                });
            }
        }

        //pause during image show
        if (imageShowTime > imageShowMaxTime)
        {
            imageShowTime = imageShowMaxTime;
        }
        if (imageShowTime < imageShowMinTime)
        {
            imageShowTime = imageShowMinTime;
        }
        while (imageShowTime-- > 0)
        {
            Integer inputMsg;
            try{
                Thread.sleep(1000);
                inputMsg = exchSlideControl.exchange(0);
            }
            catch(InterruptedException ex){
                //System.out.println(ex.getMessage());
            }
        }

        //set image as invisible
        mainActivity.runOnUiThread(() -> imageView.setVisibility(View.INVISIBLE));

        //message that image show ended
        try{
            Integer inputMsg = exchSlideControl.exchange(1);
        }
        catch(InterruptedException ex){
            //System.out.println(ex.getMessage());
        }
    }
}
