package com.comtip.buffetplaylist;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TipRayong on 24/7/2559.
 */
public class GsonSearchYoutube {


    @SerializedName("items") private List <Item> items = new ArrayList<Item>();

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    class Item {
        @SerializedName("id") private id id;

        public Item.id getId() {
            return id;
        }

        public void setId(Item.id id) {
            this.id = id;
        }

        class id{
            @SerializedName("videoId") private String videoId ;

            public String getVideoId() {
                return videoId;
            }

            public void setVideoId(String videoId) {
                this.videoId = videoId;
            }
        }


        @SerializedName("snippet") private snippet snippet;
            public Item.snippet getSnippet() {
             return snippet;
          }

             public void setSnippet(Item.snippet snippet) {
             this.snippet = snippet;
          }

        class snippet {
            @SerializedName("title") private  String title;

              public String getTitle() {
                   return title;
               }

              public void setTitle(String title) {
                  this.title = title;
              }
        }
    }


}
