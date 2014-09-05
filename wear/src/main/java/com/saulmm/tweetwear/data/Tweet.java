package com.saulmm.tweetwear.data;

public class Tweet {
    private String name;
    private String tweet;
    private String username;
    private String id;
    private boolean retweeted;
    private boolean favorite;
    private String time;


    public void setName(String name) {
        this.name = name;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getTweet() {
        return tweet;
    }

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }
}
