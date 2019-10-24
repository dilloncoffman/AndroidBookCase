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
        if (container1Fragment == null) { // if container_1 has no Fragment already in it, attach BookListFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_1, BookListFragment.newInstance(books))
                    .commit();
        } else if (container1Fragment instanceof BookDetailsFragment) { // if container1Fragment is a BookDetailsFragment, pop it off
            // pop it off stack if it is a BookDetailsFragment so it doesn't show over BookListFragment
            getSupportFragmentManager().popBackStack();
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

        if (singlePane) {
            // use ViewPager
            getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null) // allows us to hit back arrow and go back to last BookListFragment rather than going back to home screen and closing the app
                    .replace(R.id.container_1, bookDetailsFragment)
                    .commit();
        } else {
            // container_2 should always attach the BookDetailsFragment if not in singlePane
            getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null) // allows us to hit back arrow and go back to last BookListFragment rather than going back to home screen and closing the app
                    .replace(R.id.container_2, bookDetailsFragment)
                    .commit();
        }
    }
}
