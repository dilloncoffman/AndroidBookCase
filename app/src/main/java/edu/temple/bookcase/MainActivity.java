package edu.temple.bookcase;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements BookListFragment.OnBookSelectedInterface {
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

//        if (singlePane) {
//            // use ViewPager for swiping through BookDetailsFragments
//        }

//        for (Object book : books){
//            Log.d("Book name: ", (String) book);
//        }
    }

    @Override
    public void bookSelected(int position) {
        String bookTitle = books.get(position);
        Log.d("Book selected", bookTitle);
    }
}
