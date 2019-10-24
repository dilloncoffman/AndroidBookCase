package edu.temple.bookcase;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements BookListFragment.OnBookSelectedInterface {
    BookDetailsFragment bookDetailsFragment;
    ArrayList<String> books = new ArrayList<>();
    boolean singlePane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get books from books string-array resource
        Resources res = getResources();
        books.addAll(Arrays.asList(res.getStringArray(R.array.books)));
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
        String bookTitle = books.get(position); // Get bookTitle for the selected position

        // Add bookTitle to bundle to be passed to the BookDetailsFragment
        bookDetailsFragment = new BookDetailsFragment();
        Bundle detailsBundle = new Bundle();
        detailsBundle.putString(BookDetailsFragment.BOOK_TITLE_KEY, bookTitle);
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
