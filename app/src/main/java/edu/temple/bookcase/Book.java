package edu.temple.bookcase;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
    private boolean bookDownloaded;
    private int duration;
    private int savedProgress;
    private int published;
    private int id;
    private String title;
    private String author;
    private String coverUrl;

    public Book(int id, String title, String author, int duration, int published, String coverUrl) {
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
        duration = in.readInt();
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

    public boolean isBookDownloaded() {
        return bookDownloaded;
    }

    public void setBookDownloaded(boolean bookDownloaded) {
        this.bookDownloaded = bookDownloaded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSavedProgress() {
        return savedProgress;
    }

    public void setSavedProgress(int savedProgress) {
        this.savedProgress = savedProgress;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeInt(duration);
        dest.writeInt(published);
        dest.writeString(coverUrl);
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookDownloaded=" + bookDownloaded +
                ", duration=" + duration +
                ", savedProgress=" + savedProgress +
                ", published=" + published +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                '}';
    }
}
