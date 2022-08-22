package com.ndpcode.adplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Exchanger;

public class AdPlayer {

    //enum for define player content types
    enum AdContentType {
        PICTURE,
        VIDEO
    }

    //description of one player element content
    public class AdFileItem {
        private final String fileName;
        private final AdContentType fileType;

        AdFileItem(String fName, AdContentType fType){
            fileName = fName;
            fileType = fType;
        }

        public String SlideFileName(){
            return fileName;
        }

        public AdContentType SlideContentType()
        {
            return fileType;
        }
    }

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final String adSourceFilesFolder = "AdPlayer";
    private final List<String> pictureFileExtensions = Arrays.asList(".jpg", ".jpeg", ".bmp", ".png");
    private final List<String> videoFileExtensions = Arrays.asList(".mp4", ".mov", ".mpeg");
    private final AppCompatActivity mainActivity;
    private final ImageView imageView;
    private final VideoView videoView;
    private ArrayList<AdFileItem> adFilesList = new ArrayList<AdFileItem>();
    private Boolean loopSlides;

    AdPlayer(AppCompatActivity parentActivity, int imageViewId, int videoViewId)
    {
        //check permissions for acces to storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(parentActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //ask for permission
            parentActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        mainActivity = parentActivity;
        imageView = parentActivity.findViewById(imageViewId);
        videoView = parentActivity.findViewById(videoViewId);
    }

    //scan files list for playing
    private Boolean scanFilesList()
    {
        //path to source folder
        String adSourceFilesPath = Environment.getExternalStorageDirectory().toString() + "/" + adSourceFilesFolder;
        File dirRes = new File(adSourceFilesPath);
        if (dirRes == null){
            return false;
        }
        if (!dirRes.canRead()){
            return false;
        }
        //get files list from source folder
        File[] sourceFilesList = dirRes.listFiles();
        if (sourceFilesList == null){
            return false;
        }
        if (sourceFilesList.length == 0){
            return false;
        }
        //clear files list for slides
        adFilesList.clear();
        //and fill again
        for (File oneFile : sourceFilesList)
        {
            //file path and extension
            String filePath = oneFile.getPath();
            String fileExt = filePath.substring(filePath.lastIndexOf("."));
            if (filePath.length() != 0 && fileExt.length() != 0)
            {
                //create files list items depending on extension
                if (pictureFileExtensions.contains(fileExt))
                {
                    adFilesList.add(new AdFileItem(filePath, AdContentType.PICTURE));
                } else if (videoFileExtensions.contains(fileExt))
                {
                    adFilesList.add(new AdFileItem(filePath, AdContentType.VIDEO));
                }
            }
        }
        if (adFilesList.size() == 0){
            return false;
        }
        return true;
    }

    public void start()
    {
        //set image and video as invisible
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setVisibility(View.INVISIBLE);
                videoView.setVisibility(View.INVISIBLE);
            }
        });

        Exchanger<Integer> exchControl = new Exchanger<Integer>();

        //get files list for slides
        if (!scanFilesList()){
            return;
        }
        //now play slides by list
        //loop slides playback - on
        loopSlides = true;
        while (loopSlides)
        {
            //one playing iteration
            for (AdFileItem oneSlideFile : adFilesList)
            {
                if (oneSlideFile.fileName == null || oneSlideFile.fileName.length() == 0){
                    continue;
                }

                IAdSlide adSlide;
                switch (oneSlideFile.SlideContentType()){
                    case PICTURE:
                        adSlide = new AdPictureSlide(mainActivity, imageView);
                        break;
                    case VIDEO:
                        adSlide = new AdVideoSlide(mainActivity, videoView);
                        break;
                    default:
                        continue;
                };

                adSlide.Load(oneSlideFile.SlideFileName(), exchControl);
                Thread adSlideThread = new Thread(adSlide);
                adSlideThread.start();

                Boolean adSlideStopped = false;
                while (!adSlideStopped) {
                    try {
                        Integer inputMsg = exchControl.exchange(10);
                        if (inputMsg == 1) {
                            adSlideStopped = true;
                        }
                    } catch (InterruptedException ex) {
                        //System.out.println(ex.getMessage());
                    }
                }

                try {
                    adSlideThread.join();
                } catch (InterruptedException ex) {
                    //System.out.println(ex.getMessage());
                }

                adSlideThread = null;
                adSlide = null;
            }
        }
    }
}
