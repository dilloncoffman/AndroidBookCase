package edu.temple.bookcase;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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
    ArrayList<Book> books = new ArrayList<>();
    boolean singlePane;

    Handler booksHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // Response to process from worker thread, in this case a list of books to parse
            try {
                JSONArray booksArray = new JSONArray(msg.obj.toString()); // one constructor takes String and creates object for you
                for (int i = 0; i < booksArray.length(); i++) {
                    // Get book at index
                    JSONObject bookObject = booksArray.getJSONObject(i);
                    // Create Book using JSON data
                    Book newBook = new Book(bookObject.getInt("id"), bookObject.getString("title"), bookObject.getString("author"), bookObject.getInt("published"), bookObject.getString("cover_url"));
                    // Add newBook to ArrayList<Book>
                    books.add(newBook);
                    Log.d("Book object from JSON: ", newBook.toString());
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
        // Get books from books string-array resource
        Resources res = getResources();

        // Fetch books via API here and add them all to ArrayList<Book> books
        // books.addAll(Arrays.asList(res.getStringArray(R.array.books)));
        new Thread() {
            @Override
            public void run() {
                URL url = null;
                try {
                    // Using NBA API
                    url = new URL("https://kamorris.com/lab/audlib/booksearch.php");
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

        // Check if we're just using a single pane
        singlePane = (findViewById(R.id.container_2) == null);

        Fragment container1Fragment = getSupportFragmentManager().findFragmentById(R.id.container_1);

        if (container1Fragment == null && singlePane) { // if container_1 has no Fragment already attached to it and we're in singlePane
            // Attach ViewPagerFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.container_1, new ViewPagerFragment())
                    .commit();
        } else if (container1Fragment instanceof BookListFragment && singlePane) { // if container1Fragment is a BookListFragment, meaning we're coming back to singlePane from landscape mode
            // Attach ViewPagerFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_1, new ViewPagerFragment())
                    .commit();
        } else { // it's not singlePane or its null
            // Attach BookListFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_1, BookListFragment.newInstance(books))
                    .commit();
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
                    .addToBackStack(null) // allows user to hit back arrow and go back to last BookDetailsFragment rather than going back to home screen and closing the app
                    .replace(R.id.container_2, bookDetailsFragment)
                    .commit();
        }
    }
}
