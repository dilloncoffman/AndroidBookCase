package edu.temple.bookcase;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
    private int id;
    private String title;
    private String author;
    private String duration;
    private int published;
    private String coverUrl;

    public Book(int id, String title, String author, String duration, int published, String coverUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.duration = duration;
        this.published = published;
        this.coverUrl = coverUrl;
    }

    protected Book(Parcel in) {
        id = in.readInt();
        title = in.readString();
        author = in.readString();
        published = in.readInt();
        coverUrl = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getPublished() {
        return published;
    }

    public void setPublished(int published) {
        this.published = published;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeInt(published);
        dest.writeString(coverUrl);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", published=" + published +
                ", coverUrl='" + coverUrl + '\'' +
                '}';
    }
}
