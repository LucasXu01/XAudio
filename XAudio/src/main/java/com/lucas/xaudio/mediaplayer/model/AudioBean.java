package com.lucas.xaudio.mediaplayer.model;


import java.io.Serializable;

/**
 * 1.歌曲实体
 * 2.引入greendao以后扩展了许多
 */
public class AudioBean implements Serializable {

    // TODO: 2021/1/12  id一定需要   最下面的相等需要用到  构造方法必须加上id的

    public String id;
    //地址
    public String mUrl;
    //歌名
    public String name;
    //作者
    public String author;
    //所属专辑
    public String album;
    public String albumInfo;
    //专辑封面
    public String albumPic;
    //时长
    public String totalTime;

    public AudioBean(String mUrl) {
        this.mUrl = mUrl;
    }

    public AudioBean(String id, String mUrl, String name, String author,
                     String album, String albumInfo, String albumPic,
                     String totalTime) {
        this.id = id;
        this.mUrl = mUrl;
        this.name = name;
        this.author = author;
        this.album = album;
        this.albumInfo = albumInfo;
        this.albumPic = albumPic;
        this.totalTime = totalTime;
    }


    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMUrl() {
        return this.mUrl;
    }

    public void setMUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumPic() {
        return this.albumPic;
    }

    public void setAlbumPic(String albumPic) {
        this.albumPic = albumPic;
    }

    public String getAlbumInfo() {
        return this.albumInfo;
    }

    public void setAlbumInfo(String albumInfo) {
        this.albumInfo = albumInfo;
    }

    public String getTotalTime() {
        return this.totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }


    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof AudioBean)) {
            return false;
        }
        return ((AudioBean) other).id.equals(this.id);
    }
}
