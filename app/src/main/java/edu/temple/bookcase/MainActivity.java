package edu.temple.bookcase;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BookListFragment.OnBookSelectedInterface {
    BookDetailsFragment bookDetailsFragment;
    ArrayList<Book> books;
    Button searchBtn;
    EditText searchInput;
    String searchQuery;
    boolean singlePane;

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
                            bookObject.getString("duration"),
                            bookObject.getInt("published"),
                            bookObject.getString("cover_url"));
                    // Add newBook to ArrayList<Book>
                    books.add(newBook);
                }

                // Check if we're just using a single pane
                singlePane = (findViewById(R.id.container_2) == null);

                Fragment container1Fragment = getSupportFragmentManager().findFragmentById(R.id.container_1);

                if (container1Fragment == null && singlePane) { // if container_1 has no Fragment already attached to it and we're in singlePane
                    // Attach ViewPagerFragment
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.container_1, ViewPagerFragment.newInstance(books))
                            .commit();
                } else if (container1Fragment instanceof BookListFragment && singlePane) { // if container1Fragment is a BookListFragment, meaning we're coming back to singlePane from landscape mode
                    // Attach ViewPagerFragment
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container_1, ViewPagerFragment.newInstance(books))
                            .commit();
                } else { // it's not singlePane or its null
                    // Attach BookListFragment
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

        // Get user search query if any
        searchInput = findViewById(R.id.searchInput);
        searchBtn = findViewById(R.id.searchBtn);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set search query string that is currently in the searchInput EditText
                searchQuery = searchInput.getText().toString();
//                new Thread() {
//                    @Override
//                    public void run() {
//                        URL url = null;
//                        try {
//                            if (!(searchQuery.equals(""))) {
//                                url = new URL("https://kamorris.com/lab/audlib/booksearch.php?search=" + searchQuery);
//                                Log.d("Search URL is: ", url.toString());
//                                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
//                                StringBuilder builder = new StringBuilder(); // StringBuilder, keep adding on bits of a string
//                                String response;
//                                while ((response = reader.readLine()) != null) {
//                                    builder.append(response);
//                                }
//                                // Need to use Handler
//                                Message msg = Message.obtain();
//                                msg.obj = builder.toString(); // gives you string created from StringBuilder object
//                                booksHandler.sendMessage(msg);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
            }
        });

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
                    .addToBackStack(null) // allows user to hit back arrow and go back to last BookDetailsFragment rather than going back to home screen and closing the app
                    .replace(R.id.container_2, bookDetailsFragment)
                    .commit();
        }
    }
}
