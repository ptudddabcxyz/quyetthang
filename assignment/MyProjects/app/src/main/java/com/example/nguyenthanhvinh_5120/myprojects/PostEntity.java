package com.example.nguyenthanhvinh_5120.myprojects;

//Class PostEntity chứa các thông tin về bài đăng
class PostEntity{
    int id, liked;
    String name, time, text, link;
    boolean chk;

    PostEntity(int id, String name, String time, String text, String link, int liked, boolean chk){
        setId(id);
        setName(name);
        setTime(time);
        setText(text);
        setLink(link);
        setLiked(liked);
        setChk(chk);
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setLiked(int liked) {
        this.liked = liked;
    }

    public int getLiked() {
        return liked;
    }

    public void setChk(boolean chk) {
        this.chk = chk;
    }

    public boolean getChk(){
        return chk;
    }
}

//Class CommentEntity chứa các thông tin về bình luận
class CommentEntity{
    String name, comment, time;

    CommentEntity(String name, String comment, String time){
        setName(name);
        setComment(comment);
        setTime(time);
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public String getComment() {
        return comment;
    }

    public String getName() {
        return name;
    }
}