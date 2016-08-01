package com.comtip.buffetplaylist;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by TipRayong on 12/7/2559.
 */
public class PlayYoutube extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    final String YOUTUBE_API_KEY = "AIzaSyB-iYo9CZ0yb13a4esvDeVOZG2zdTqFf0I";

    //windgets
    YouTubePlayerView youtubeView;
    TextView headerVideo;
    Button   finishBT;

     //ตัวแปร  Bundle รับข้อมูลมาจาก Intent  MainActiviy

    ArrayList<String> playlist = new ArrayList<>();      // ตัวแปร ArrayList สำหรับสร้าง Playlist เพื่อส่งให้ Youtube เล่น
    boolean selectShuffle = false;  //  boolean เล่น playlist แบบ direct หรือ shuffle
    boolean  favPage = false;  // boonlean สำหรับสั่งงาน bundle  ว่าเป็นการรับข้อมูลจาก Favorite List หรือ Search List จากหน้า MainActivity
    String keywords = "Favorite List";
    String [] playlistSearch = new String[50]; // ตัวแปร Array รับผลลัพธ์จาก Search List
    int singleVideo = 0;  // ใช้ตรวจสอบว่าเป็นการเล่นวีดีโอแบบ 1  หรือทั้งหมด

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.youtube_layout);
        youtubeView = (YouTubePlayerView) findViewById(R.id.youtubeView);
        headerVideo = (TextView) findViewById(R.id.headerVideo);
        finishBT = (Button) findViewById(R.id.finishBT);

        Arrays.fill (playlistSearch,null);   // ให้เป็นค่า null ทั้งหมด

        finishBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle  bundle  =  getIntent().getExtras();
        selectShuffle = bundle.getBoolean("selectShuffle",false);
        singleVideo = bundle.getInt("singleVideo");

        favPage = bundle.getBoolean("favPage",false);
        if (favPage) {
            // รับมาจาก Favorite List  ให้ไปไป intialize เลย
            playlist = bundle.getStringArrayList("playlistFavList");
            youtubeView.initialize(YOUTUBE_API_KEY, PlayYoutube.this);

        } else {
            // รับมาจาก Search List ให้เข้ากระบวนการย้ายข้อมูลจาก Array ไป ArrayList
            keywords = bundle.getString("keywords", "ว่างเปล่า");
            playlistSearch = bundle.getStringArray("playlistSearch");
            if (playlistSearch[0] != null) {
                createYoutubePlaylist();
            }
        }

        headerVideo.setText(keywords);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
          youTubePlayer.setFullscreen(true);


          if(selectShuffle) {
              Collections.shuffle(playlist);   // shuffle PlayList
              youTubePlayer.loadVideos(playlist);
          }else {
               if (singleVideo == 0) {
                   youTubePlayer.loadVideos(playlist);   // Direct PlayList
               }  else {
                   youTubePlayer.loadVideo(playlist.get(singleVideo));  //  Play Single
               }
          }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, 1).show();
        } else {
            Toast.makeText(this, "Unknown Error", Toast.LENGTH_LONG).show();
        }
    }



    // ย้ายข้อมูลจาก Array ไป ArrayList
    public void createYoutubePlaylist () {

        for (int i = 0; i < 50; i++) {
            if(playlistSearch[i] != null) {
                playlist.add(i,playlistSearch[i]);
            }
        }

        youtubeView.initialize(YOUTUBE_API_KEY, PlayYoutube.this);
    }
}
