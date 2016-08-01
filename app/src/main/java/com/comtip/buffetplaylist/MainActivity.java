package com.comtip.buffetplaylist;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //ตัวแปรสำหรับสร้าง url  json query Search
    final String  googleapisSearch = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=";
    String keywords = "";
    String  order = "";
    final String  typePlaylist = "&type=video&maxResults=50&key=";
    final String APIkey = "AIzaSyB-iYo9CZ0yb13a4esvDeVOZG2zdTqFf0I";
    String querySearch;

    // Widgets
    EditText SearchEdit;
    Button SearchBT;
    ListView plList;

    //Array สำหรับแสดงผลลัพธืการ Search  เอาแค่ 50 อันดับแรก
    String [] titelSearch = new String[50];
    String [] playlistSearch = new String[50];

    int singleVideo = 0;
    boolean selectShuffle = false;

    // ตัวแปรสำหรับพักข้อมูลที่โดนชี้ตำแหน่งในขณะนั้น
    String videoID = null;
    String titleVideo = null;

    // Favorite
    ArrayList<String> titleFavList = new ArrayList<>();
    ArrayList<String> playlistFavList = new ArrayList<>();
    Button FavBT;
    int indexFav = 0;

    // กำหนดค่า Default  Favorite
    String  saveTitle ="";
    String  saveVideoID = "";

    //บันทึกก่อนปิด
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    //สถานะ ListView เป็น Favorite หรือ Search
    boolean favPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupWidgets();
    }


     //  บันทึกข้อมูลก่อนปิดแอพ
    @Override
    protected void onPause() {
        super.onPause();

        editor.putString("saveTitle",saveTitle);
        editor.putString("saveVideoID",saveVideoID);
        editor.putInt("indexFav",indexFav);
        editor.commit();

    }

    // เตรียม Windgets พื้นฐาน
    public void setupWidgets () {
        SearchBT = (Button) findViewById(R.id.SearchBT);
        SearchEdit = (EditText)findViewById(R.id.SearchEdit);
        plList =  (ListView) findViewById(R.id.plList);

        SearchBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keywords = SearchEdit.getText().toString();
                searchOption();
            }
        });

        FavBT = (Button) findViewById(R.id.FavBT);
        FavBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favList();
            }
        });

        // โหลดข้อมูลล่าสุด

        sp = this.getSharedPreferences("Save Mode", Context.MODE_PRIVATE);
        editor = sp.edit();
        saveTitle = sp.getString("saveTitle","");
        saveVideoID = sp.getString("saveVideoID","");
        indexFav = sp.getInt("indexFav",0);


        if ((!saveVideoID.isEmpty())&&(!saveTitle.isEmpty())){

            String []  bufferTitltFav = saveTitle.split("\\✎");
            String []  bufferPlaylistFav = saveVideoID.split("\\✎");

            for (int i = 0; i < indexFav;i++){
                titleFavList.add(i,bufferTitltFav[i]);
                playlistFavList.add(i,bufferPlaylistFav[i]);
            }
            favList();
        }

    }


    // Option ของปุ่ม Search
    public void searchOption () {
        AlertDialog.Builder alertDB = new AlertDialog.Builder(MainActivity.this);
        alertDB.setTitle("Search  "+keywords+" Order By ? ");

        alertDB.setNegativeButton("✎ New Release ✎", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                order="&order=date";
                querySearch = googleapisSearch+keywords+order+typePlaylist+APIkey;
                new SearchPlaylist().execute();

            }
        });

        alertDB.setNeutralButton("✎ Popular ✎", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                order="&order=viewCount";
                querySearch = googleapisSearch+keywords+order+typePlaylist+APIkey;
                new SearchPlaylist().execute();
            }
        });

        alertDB.setPositiveButton("✎ Normal ✎", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                order="";
                querySearch = googleapisSearch+keywords+order+typePlaylist+APIkey;
                new SearchPlaylist().execute();
            }
        });

        AlertDialog alertO = alertDB.create();
        alertO.show();
    }


    //Search AsynTask
    private class  SearchPlaylist  extends AsyncTask<Void,String,Void> {

        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Arrays.fill(titelSearch,null);
            Arrays.fill (playlistSearch,null);

            pd = new ProgressDialog(MainActivity.this);
            pd.setTitle("กำลังค้นหา "+keywords);
            pd.setMessage("รอสักครู่ . . .");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            OkHttpClient okHttpClient = new OkHttpClient();
            Request.Builder builder = new Request.Builder();
            Request request = builder.url(querySearch).build();

            String searchPage = "";

            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    searchPage = response.body().string();

                    Gson gsonSearch = new Gson();
                    GsonSearchYoutube searchOBJ =  gsonSearch.fromJson(searchPage,GsonSearchYoutube.class);
                    for (int i = 0; i < searchOBJ.getItems().size(); i++) {
                        playlistSearch[i] = searchOBJ.getItems().get(i).getId().getVideoId();
                        titelSearch[i] = searchOBJ.getItems().get(i).getSnippet().getTitle();
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();
            searchList();
        }
    }

    //  กำหนด ListView ของหน้า Search
    public void searchList () {
        CustomList adapter = new CustomList(this,titelSearch);
        plList.setAdapter(adapter);
        plList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                videoID = playlistSearch[position];
                titleVideo = titelSearch[position];
                singleVideo = position;

                if (videoID != null){
                    favPage = false;
                    menuPlaylist();
                }
            }
        });
    }

    //  กำหนด ListView ของหน้า Favorite
    public void favList(){
        CustomArrayList  adapter = new CustomArrayList(this,titleFavList);
        plList.setAdapter(adapter);
        plList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                videoID = playlistFavList.get(position);
                titleVideo = titleFavList.get(position);
                singleVideo = position;

                if (videoID != null) {
                    favPage = true;
                    menuPlaylist();
                }
            }
        });
    }

    //  แสดง เมนูสำหรับสั่งงานต่างๆใน Playlist
    public  void  menuPlaylist () {
        AlertDialog.Builder alertDB = new AlertDialog.Builder(MainActivity.this);
        alertDB.setTitle(titleVideo);

        if(favPage) {
            //กรณีอยู่หน้า Favorite
            alertDB.setNegativeButton("✪ Delete ✪", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog.Builder alertDeletee = new AlertDialog.Builder(MainActivity.this);
                    alertDeletee.setTitle("Delete " + titleVideo + "  ? ");
                    alertDeletee.setPositiveButton("✔ Yes ✔", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Deleter Array Row
                            saveVideoID = saveVideoID.replace(videoID + "✎", "");
                            saveTitle = saveTitle.replace(titleVideo + "✎", "");

                            if (indexFav != 0) {
                                indexFav = indexFav - 1;
                            }

                            recreate(); // สั่ง Recrate Activity ทั้งหมดตั้งแต่ต้น
                        }
                    });


                    alertDeletee.setNegativeButton("✘ No ✘", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //no Action
                        }
                    });

                    AlertDialog alertD = alertDeletee.create();
                    alertD.show();
                }
            });

        } else {
            //กรณีอยู่หน้า Search
            alertDB.setNegativeButton("♡ Favorite ♡", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //save  to  favorite playlist

                    saveTitle += titleVideo+ "✎";
                    saveVideoID += videoID+ "✎";
                    titleFavList.add(indexFav, titleVideo);
                    playlistFavList.add(indexFav, videoID);
                    Toast.makeText(MainActivity.this, "Add  " + titleFavList.get(indexFav) + " to Favorite List !!!", Toast.LENGTH_SHORT).show();
                    indexFav = indexFav + 1;

                }
            });
        }

        alertDB.setPositiveButton("✦ Play All ✦" ,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder alertPlayAll = new AlertDialog.Builder(MainActivity.this);
                alertPlayAll.setTitle("PlayList ?");
                alertPlayAll.setPositiveButton("✦ Shuffle ✦", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Play Shuffle PlayList
                        selectShuffle = true;
                        singleVideo = 0;
                        intetntYoutube();
                    }
                });

                alertPlayAll.setNegativeButton("✧ Direct ✧", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Play Direct PlayList
                        selectShuffle = false;
                        singleVideo = 0;
                        intetntYoutube();
                    }
                });

                AlertDialog alertPL = alertPlayAll.create();
                alertPL.show();

            }
        });

        alertDB.setNeutralButton("✦ Play This Video ✦", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //  Play Single Video
                selectShuffle = false;
                intetntYoutube();
            }
        });

        AlertDialog alert = alertDB.create();
        alert.show();
    }

    // ส่งข้อมูลไปให้ PlayYoutube
    public void  intetntYoutube () {
        Intent intent = new Intent(this,PlayYoutube.class);

        // ข้อมูลจำเป็นที่ต้อง Intent ส่งไปให้ PlayYoutube
        intent.putExtra("favPage",favPage);
        intent.putExtra("selectShuffle", selectShuffle);
        intent.putExtra("singleVideo", singleVideo);

         // ข้อมูลที่ต้องเลือก Intent ส่ง ว่ามาจากหน้า Favorite List หรือ Search List
        if(favPage) {
            intent.putExtra("playlistFavList",playlistFavList);
        }
        else {
            intent.putExtra("keywords", keywords);
            intent.putExtra("playlistSearch", playlistSearch);
        }
        startActivity(intent);
    }

    // กดปุ่ม Back แล้วปิด Activity ทั้งหมด
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
