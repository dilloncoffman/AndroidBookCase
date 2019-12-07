package edu.temple.bookcase;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements BookListFragment.OnBookSelectedInterface, BookDetailsFragment.BookDetailsInteractions {
    private int nowPlayingBookDuration;
    private String nowPlayingBookTitle;
    private int nowPlayingProgress;
    private Book nowPlayingBook;
    BookDetailsFragment bookDetailsFragment;
    Fragment container1Fragment;
    Fragment container2Fragment; // BookDetailsFragment in landscape
    ArrayList<Book> books;
    TextView nowPlayingBookTitleText;
    Button searchBtn;
    Button pauseBtn;
    Button stopBtn;
    EditText searchInput;
    SeekBar seekBar;
    String searchQuery = "";
    boolean singlePane;
    private boolean paused;
    boolean playing;

    // Lab 9 Service-related variables
    boolean connected;
    Intent playBookIntent;
    AudiobookService.MediaControlBinder mediaControlBinder;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connected = true;
            Log.d("ServiceConnection: ", "Connected");
            mediaControlBinder = (AudiobookService.MediaControlBinder) service; // hold on to Binder that service is returning that describes interactions you can perform
            mediaControlBinder.setProgressHandler(seekBarHandler); // set Handler for SeekBar progress
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ServiceConnection: ", "Disconnected: Service was killed for some reason");
            connected = false; // no longer connected
            mediaControlBinder = null; // to protect against memory leak
        }
    };

    Handler seekBarHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.obj != null) {
                seekBar = findViewById(R.id.seekBar);
                if (seekBar != null) {
                    AudiobookService.BookProgress bookProgress = (AudiobookService.BookProgress) msg.obj;
                    Log.d("Book progress is: ", String.valueOf(bookProgress.getProgress()));
                    Log.d("Now playing book duration is", String.valueOf(nowPlayingBookDuration));
                    Log.d("Now playing book title is", String.valueOf(nowPlayingBookTitle));
                    Log.d("Now playing book progress is", String.valueOf(nowPlayingProgress));
                    if (bookProgress.getProgress() == -353746) {
                        // There was a playback error, stop audio playback
                        if (connected) {
                            mediaControlBinder.stop(); // Stop the book, which changes the playingState in AudiobookService accordingly
                            nowPlayingBookTitle = ""; // Set // Set now playing book title to nothing since book has finished
                            nowPlayingBookTitleText.setText(""); // Set now playing book text to nothing since book has finished
                            seekBar.setProgress(0); // Set SeekBar back to beginning
                        }
                    }

                    if (seekBar != null && (bookProgress.getProgress() < nowPlayingBookDuration)) {
                        seekBar.setProgress(bookProgress.getProgress()); // Set progress of SeekBar to current book's progress
                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    if (connected) {
                                        mediaControlBinder.seekTo(progress);
                                        nowPlayingProgress = progress;
                                    }
                                } else {
                                    nowPlayingProgress = progress;
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                    } else if (seekBar != null && (bookProgress.getProgress() == nowPlayingBookDuration)) { // Book was finished
                        if (connected) {
                            mediaControlBinder.stop(); // Stop the book, which changes the playingState in AudiobookService accordingly
                            nowPlayingBookTitle = ""; // Set // Set now playing book title to nothing since book has finished
                            nowPlayingBookTitleText.setText(""); // Set now playing book text to nothing since book has finished
                            seekBar.setProgress(0); // Set SeekBar back to beginning
                        }
                    }
                }
            }
            return true;
        }
    });

    Handler booksHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // Response to process from worker thread, in this case a list of books to parse
            try {
                JSONArray booksArray = new JSONArray(msg.obj.toString());
                books = new ArrayList<>();
                for (int i = 0; i < booksArray.length(); i++) {
                    // Get book at index
                    JSONObject bookObject = booksArray.getJSONObject(i);
                    // Create Book using JSON data
                    Book newBook = new Book(
                            bookObject.getInt("book_id"),
                            bookObject.getString("title"),
                            bookObject.getString("author"),
                            bookObject.getInt("duration"),
                            bookObject.getInt("published"),
                            bookObject.getString("cover_url"));
                    // Add newBook to ArrayList<Book>
                    books.add(newBook);
                }

                for (int i = 0; i < books.size(); i++) {
                    File[] files = Objects.requireNonNull(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).listFiles();
                    if (files != null) {
                        for (File file : files) {
                            Log.d(" ", file.getName());
                            Log.d("file char: ", String.valueOf(file.getName().charAt(0)));
                            Log.d("book id: ", String.valueOf(books.get(i).getId()));
                            if (String.valueOf(file.getName().charAt(0)).equals(String.valueOf(books.get(i).getId()))) {
                                // Book already has file locally for it
                                Log.d("A book is already downloaded with that ID at: ", file.getAbsolutePath());
                                books.get(i).setBookDownloaded(true);
                            }
                        }
                    }
                }

                container1Fragment = getSupportFragmentManager().findFragmentById(R.id.container_1); // get reference to fragment currently in container_1
                container2Fragment = getSupportFragmentManager().findFragmentById(R.id.container_2); // get reference to fragment currently in container_1
                singlePane = (findViewById(R.id.container_2) == null); // check if in single pane mode

                if (container1Fragment == null && singlePane) { // App opened in portrait mode
                    // App opened in portrait mode
                    Log.d("App opened in portrait mode. Single pane should be true == ", String.valueOf(singlePane));
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container_1, ViewPagerFragment.newInstance(books))
                            .commit();
                } else if (container1Fragment == null) { // App opened in landscape mode
                    Log.d("App opened in landscape mode. Single pane should be false == ", String.valueOf(singlePane));
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container_1, BookListFragment.newInstance(books))
                            .commit();
                } else if (container1Fragment instanceof ViewPagerFragment && !searchQuery.equals("")) { // Books were searched in portrait mode
                    Log.d("Books were searched for in Portrait mode: Single pane should be true == ", String.valueOf(singlePane));
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container_1, ViewPagerFragment.newInstance(books))
                            .commit();
                } else if (container1Fragment instanceof BookListFragment && !searchQuery.equals("")) { // Books were searched in landscape mode
                    Log.d("Books were searched for in Landscape mode: Single pane should be false == ", String.valueOf(singlePane));
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container_1, BookListFragment.newInstance(books))
                            .commit();
                } else if (searchQuery.equals("") && ((container1Fragment instanceof ViewPagerFragment))) { // Empty search made in portrait mode
                    Log.d("Empty search after searching should get back all books. Single pane == ", String.valueOf(singlePane));
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container_1, ViewPagerFragment.newInstance(books))
                            .commit();
                } else if (searchQuery.equals("") && ((container1Fragment instanceof BookListFragment))) { // Empty search made in landscape mode
                    Log.d("Empty search after searching should get back all books. Single pane == ", String.valueOf(singlePane));
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container_1, BookListFragment.newInstance(books))
                            .commit();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            try {
                nowPlayingBook = savedInstanceState.getParcelable("nowPlayingBook");
                nowPlayingBookDuration = savedInstanceState.getInt("nowPlayingBookDuration");
                nowPlayingBookTitle = savedInstanceState.getString("nowPlayingBookTitle");
                nowPlayingProgress = savedInstanceState.getInt("nowPlayingProgress");
                books = savedInstanceState.getParcelableArrayList("books");
                paused = savedInstanceState.getBoolean("paused");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Get user search query if any
        nowPlayingBookTitleText = findViewById(R.id.nowPlayingBookTitle);
        searchInput = findViewById(R.id.searchInput);
        searchBtn = findViewById(R.id.searchBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        stopBtn = findViewById(R.id.stopBtn);
        seekBar = findViewById(R.id.seekBar);

        nowPlayingBookTitleText.setText(nowPlayingBookTitle); // set currently playing's book title even after Activity is restarted
        seekBar.setProgress(nowPlayingProgress); // set current progress even after Activity is restarted
        seekBar.setMax(nowPlayingBookDuration); // set seekBar max even after Activity is restarted

        container1Fragment = getSupportFragmentManager().findFragmentById(R.id.container_1); // get reference to fragment currently in container_1
        container2Fragment = getSupportFragmentManager().findFragmentById(R.id.container_2); // get reference to fragment currently in container_1
        singlePane = (findViewById(R.id.container_2) == null); // check if in single pane mode

        // Bind to AudiobookService
        playBookIntent = new Intent(this, AudiobookService.class);
        Log.d("BINDING TO AUDIOBOOKSERVICE ", "bound to AudiobookService");
        bindService(playBookIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Start-up query
        if (container1Fragment == null && container2Fragment == null) { // if start up, both of these fragment containers are null, do first fetch to get all books
            Log.d("CONTAINER 1 FRAGMENT NULL, FETCHING BOOKS", String.valueOf(singlePane));
            fetchBooks(null);
        }

        // Handle portrait to landscape and vice versa orientation changes
        if (container1Fragment instanceof BookListFragment && singlePane) { // From landscape to portrait after initial load without searching
            Log.d("Went back to portrait from landscape. Single pane should be true == ", String.valueOf(singlePane));
            // if container1Fragment (BookListFragment) != null and it has books, pass those books to ViewPagerFragment
            if (container1Fragment != null && ((BookListFragment) container1Fragment).getBooks() != null) {
                Log.d("Passing books from BookListFragment to ViewPagerFragment. Single pane should be true == ", String.valueOf(singlePane));
                books = ((BookListFragment) container1Fragment).getBooks();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container_1, ViewPagerFragment.newInstance(books))
                        .commit();
            }
        } else if (container1Fragment instanceof ViewPagerFragment && !singlePane) { // From portrait to landscape after initial load without searching
            Log.d("Went from portrait to landscape. Single pane should be false == ", String.valueOf(singlePane));
            // if container1Fragment (ViewPagerFragment) != null and it has books, pass those books to BookListFragment
            if (container1Fragment != null && ((ViewPagerFragment) container1Fragment).getBooks() != null) {
                Log.d("Passing books from ViewPagerFragment to BookListFragment. Single pane should be false == ", String.valueOf(singlePane));
                books = ((ViewPagerFragment) container1Fragment).getBooks();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container_1, BookListFragment.newInstance(books))
                        .commit();
            }
        } else if (container1Fragment instanceof BookListFragment) { // Tablet size
            Log.d("Tablet size: ", String.valueOf(container1Fragment));
            // if container1Fragment (ViewPagerFragment) != null and it has books, pass those books to BookListFragment
            if (container1Fragment != null && ((BookListFragment) container1Fragment).getBooks() != null) {
                Log.d("Passing books from ViewPagerFragment to BookListFragment. Single pane should be false == ", String.valueOf(singlePane));
                books = ((BookListFragment) container1Fragment).getBooks();
                for (int i = 0; i < ((BookListFragment) container1Fragment).getBooks().size(); i++) {
                    Log.d("Books currently in ViewPagerFragment passing to BookListFragment: Single pane should be false == ", String.valueOf(singlePane));
                    Log.d("book: ", ((BookListFragment) container1Fragment).getBooks().get(i).toString());
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container_1, BookListFragment.newInstance(books))
                        .commit();
            }
        }

        // Search click listener
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set search query string that is currently in the searchInput EditText
                searchQuery = searchInput.getText().toString();
                // Remove BookDetailsFragment in landscape mode on search; this is so a book that is unrelated to the search isn't still showing after we get the new books back from the search
                container2Fragment = getSupportFragmentManager().findFragmentById(R.id.container_2); // get reference to fragment currently in container_1=
                if (container2Fragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .remove(container2Fragment)
                            .commit();
                }
                // Do query for new books
                fetchBooks(searchQuery);
            }
        });

        // Pause click listener
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Only if connected to service and currently playing something
                if (connected && !paused) {
                    mediaControlBinder.pause(); // pause the book
                    if (nowPlayingBook.isBookDownloaded()) {
                        // The book being paused is downloaded locally, save its progress to be 10 seconds before it was paused
                        nowPlayingBook.setSavedProgress(nowPlayingProgress - 10);
                        Log.d("NOWPLAYINGBOOK WAS LOCAL AUDIO FILE AND IS PAUSED AT", nowPlayingBook.toString());
                        // Save local audio book file's progress
                        for (int i = 0; i < books.size(); i++) {
                            if (nowPlayingBook.getId() == books.get(i).getId()) {
                                books.get(i).setSavedProgress(nowPlayingProgress - 10);
                            }
                        }
                    }
                    playing = false;
                    paused = true;
                } else if (connected) { // Un-pause the book with the way AudiobookService method for pausing is set up
                    mediaControlBinder.pause(); // Resumes audio playback
                    if (nowPlayingBook.isBookDownloaded() && nowPlayingBook.getSavedProgress() > 0) {
                        // Local audio book file had already been playing before, seek to it's savedProgress
                        mediaControlBinder.seekTo(nowPlayingBook.getSavedProgress());
                        nowPlayingProgress = nowPlayingBook.getSavedProgress();
                    }
                    playing = true;
                    paused = false;
                }
            }
        });

        // Stop click listener
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Only if connected to service
                if (connected) {
                    if (nowPlayingBook.isBookDownloaded()) {
                        // The book being paused is downloaded locally, save its progress to be 10 seconds before it was paused
                        nowPlayingBook.setSavedProgress(nowPlayingProgress - 10);
                        Log.d("NOWPLAYINGBOOK WAS A LOCAL AUDIO FILE", nowPlayingBook.toString());
                        // Reset local audio book file's progress
                        for (int i = 0; i < books.size(); i++) {
                            if (nowPlayingBook.getId() == books.get(i).getId()) {
                                books.get(i).setSavedProgress(0); // Reset its progress to 0 if stopped while playing
                                nowPlayingProgress = 0;
                            }
                        }
                    }
                    mediaControlBinder.stop(); // Stop the book, assuming we'd want to stop (reset progress to beginning) even if in paused state
                    nowPlayingBookTitle = ""; // Set // Set now playing book title to nothing since book has finished
                    nowPlayingBookTitleText.setText(""); // Set now playing book text to nothing since book has finished
                    seekBar.setProgress(0); // Set SeekBar to beginning of book currently being listened to
                    paused = false;
                    playing = false;
                    stopService(playBookIntent);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("nowPlayingBookDuration", nowPlayingBookDuration);
        if (nowPlayingBookTitle != null)
            outState.putString("nowPlayingBookTitle", nowPlayingBookTitle);
        outState.putInt("nowPlayingProgress", nowPlayingProgress);
        if (!(books.isEmpty()))
            outState.putParcelableArrayList("books", books);
        if (nowPlayingBook != null) {
            outState.putParcelable("nowPlayingBook", nowPlayingBook);
        }
        outState.putBoolean("paused", paused);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("UNBINDING FROM AUDIOBOOKSERVICE ", "Unbinded service connection");
        unbindService(serviceConnection);
    }

    /* Fetches books */
    public void fetchBooks(final String searchString) {
        if (searchString == null || searchString.length() == 0) { // if searchQuery is null or a user has deleted all entered text and hit search again, fetch all books
            // Fetch books via API and add them all or some if query provided to ArrayList<Book> books
            new Thread() {
                @Override
                public void run() {
                    URL url = null;
                    try {
                        url = new URL("https://kamorris.com/lab/audlib/booksearch.php");
                        Log.d("No search query entered. URL is: ", url.toString());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                        StringBuilder builder = new StringBuilder(); // StringBuilder, keep adding on bits of a string
                        String response;
                        while ((response = reader.readLine()) != null) {
                            builder.append(response);
                        }
                        // Need to use Handler
                        Message msg = Message.obtain();
                        msg.obj = builder.toString(); // gives you string created from StringBuilder object
                        booksHandler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            // Fetch books via API and add them all or some if query provided to ArrayList<Book> books
            new Thread() {
                @Override
                public void run() {
                    URL url = null;
                    try {
                        url = new URL("https://kamorris.com/lab/audlib/booksearch.php?search=" + searchString);
                        Log.d("Search URL is: ", url.toString());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                        StringBuilder builder = new StringBuilder(); // StringBuilder, keep adding on bits of a string
                        String response;
                        while ((response = reader.readLine()) != null) {
                            builder.append(response);
                        }
                        // Need to use Handler
                        Message msg = Message.obtain();
                        msg.obj = builder.toString(); // gives you string created from StringBuilder object
                        booksHandler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    @Override
    public void bookSelected(int position) {
        Book book = books.get(position); // Get book at position selected

        // Add book to bundle to be passed to the BookDetailsFragment
        bookDetailsFragment = new BookDetailsFragment();
        Bundle detailsBundle = new Bundle();
        detailsBundle.putParcelable(BookDetailsFragment.BOOK_KEY, book);
        bookDetailsFragment.setArguments(detailsBundle);

        if (!singlePane) {
            // container_2 should always attach the BookDetailsFragment if not in singlePane
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_2, bookDetailsFragment)
                    .commit();
        }
    }

    @Override
    public void playBook(Book book) {
        // If there is a local downloaded audio book file, play it
        File[] files = Objects.requireNonNull(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).listFiles();
        if (files != null) {
            Log.d("Files in external storage are ", " ");
            for (File file : files) {
                Log.d(" ", file.getName());
                if (file.getName().contains(String.valueOf(book.getId()))) {
                    // Book already has audio book file locally to play instead of stream
                    Log.d("Playing book from local external storage with absolute path of: ", file.getAbsolutePath());
                    Log.d("Book is downloaded locally already?", String.valueOf(book.isBookDownloaded()));
                    Log.d("Local audio book file's savedProgress: ", String.valueOf(book.getSavedProgress()));
                    Log.d("Local audio book file to play: ", file.toString());
                    // Save previously playing book's progress before playing new audio book file
                    if (nowPlayingBook != null) {
                        // Save it's progress to be 10 seconds before current progress as per lab 10 document
                        nowPlayingBook.setSavedProgress(nowPlayingProgress - 10);
                        Log.d("NOWPLAYINGBOOK WAS LOCAL AUDIO FILE. PROGRESS WAS SAVED BEFORE PLAYING NEW BOOK AT", nowPlayingBook.toString());
                        // Save local audio book file's progress
                        for (int i = 0; i < books.size(); i++) {
                            if (nowPlayingBook.getId() == books.get(i).getId()) {
                                books.get(i).setSavedProgress(nowPlayingProgress - 10);
                            }
                        }
                    }
                    if (file.exists() && connected && book.isBookDownloaded()) {
                        Log.d("Playing local audio book file: ", file.getName());
                        seekBar.setMax(book.getDuration()); // Set seekBar max to currently playing book's duration
                        nowPlayingBookDuration = book.getDuration(); // Holding reference to currently playing book's duration so when it reaches its end, I stop the AudiobookService and reset seekBar
                        nowPlayingBookTitle = book.getTitle(); // Hold reference to now playing book title for when Activity is restarted on orientation change
                        nowPlayingBookTitleText.setText(nowPlayingBookTitle); // Set now playing text
                        mediaControlBinder.play(file, book.getSavedProgress());
                        nowPlayingBook = book;
                        Log.d("NOW PLAYING BOOK IS", nowPlayingBook.toString());
                        playing = true;
                        paused = false;
                    } else {
                        Log.d("No files exist in storage with that name", ":(");
                    }
                    return; // Don't execute AudiobookService code below to stream book, just return from method
                }
            }
        }

        // Otherwise stream the audio book
        if (connected && !(book.isBookDownloaded())) {
            if (nowPlayingBook.isBookDownloaded() && nowPlayingBook != null) {
                // Save it's progress to be 10 seconds before current progress as per lab 10 document
                nowPlayingBook.setSavedProgress(nowPlayingProgress - 10);
                Log.d("NOWPLAYINGBOOK WAS LOCAL AUDIO FILE. PROGRESS WAS SAVED BEFORE PLAYING NEW BOOK AT", nowPlayingBook.toString());
                // Save local audio book file's progress for that specific book
                for (int i = 0; i < books.size(); i++) {
                    if (nowPlayingBook.getId() == books.get(i).getId()) {
                        books.get(i).setSavedProgress(nowPlayingProgress - 10);
                    }
                }
            }
            startService(playBookIntent); // start AudiobookService when playing
            Log.d("Streaming audio book: ", String.valueOf(book.getTitle()));
            Log.d("NOW PLAYING BOOK IS", nowPlayingBook.toString());
            seekBar.setMax(book.getDuration()); // Set seekBar max to currently playing book's duration
            nowPlayingBookDuration = book.getDuration(); // Holding reference to currently playing book's duration so when it reaches its end, I stop the AudiobookService and reset seekBar
            nowPlayingBookTitle = book.getTitle(); // Hold reference to now playing book title for when Activity is restarted on orientation change
            nowPlayingBookTitleText.setText(nowPlayingBookTitle); // Set now playing text
            mediaControlBinder.play(book.getId()); // Always play book from the beginning since it is being streamed
            nowPlayingBook = book;
            playing = true;
            paused = false;
        }
    }

    @Override
    public void downloadBookToStorage(final Book bookToDownload) {
        new Thread() {
            @Override
            public void run() {
                // Download audio book to external public storage directory
                String bookAudioFileName = bookToDownload.getId() + "-" + bookToDownload.getTitle().toLowerCase().replace(" ", "-") + "-book-audio.mp3";
                File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                externalStorageDir.mkdirs(); // make Download directory if it doesn't already exist
                File downloadedAudioBookFile = new File(externalStorageDir, bookAudioFileName);
                // As of API 23+, need user permission to access even external storage regardless of manifest permission. See https://developer.android.com/training/permissions/requesting.html#perm-check
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    URL url;
                    FileOutputStream fos = null;
                    BufferedInputStream reader = null;
                    try {
                        url = new URL("https://kamorris.com/lab/audlib/download.php?id=" + bookToDownload.getId());
                        reader = new BufferedInputStream(url.openStream());

                        Log.d("Downloading book response. URL is: ", url.toString());
                        Log.d("Downloaded audio book path is: ", downloadedAudioBookFile.getPath());
                        fos = new FileOutputStream(downloadedAudioBookFile);
                        byte[] buffer = new byte[30000]; // No book should be over 30 MB in this case
                        int count = 0;
                        while ((count = reader.read(buffer, 0, 30000)) != -1) {
                            fos.write(buffer, 0, count);
                        }
                        bookToDownload.setBookDownloaded(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fos != null) {
                            try {
                                fos.flush();
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    // Request permission from the user to access external storage, this is required as of API 23+
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            }
        }.start();
    }

    @Override
    public void deleteBookFromStorage(final Book bookToDelete) {
        new Thread() {
            @Override
            public void run() {
                // As of API 23+, need user permission to access even external storage regardless of manifest permission. See https://developer.android.com/training/permissions/requesting.html#perm-check
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        File[] files = Objects.requireNonNull(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).listFiles();
                        if (files != null) {
                            for (File file : files) {
                                Log.d("Deleting audio book file: ", file.getName());
                                if (String.valueOf(file.getName().charAt(0)).equals(String.valueOf(bookToDelete.getId()))) {
                                    // Book already has file locally for it
                                    boolean deleted = file.delete();
                                    if (deleted) {
                                        bookToDelete.setBookDownloaded(false);
                                        Log.d("Deleted: ", file.getName());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // Request permission from the user to access external storage, this is required as of API 23+
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            }
        }.start();
    }
}