package edu.temple.bookcase;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailsFragment extends Fragment {

    TextView textView;
    public static final String BOOK_TITLE_KEY = "book title";
    String bookTitle;


    public BookDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bookTitle String.
     * @return A new instance of fragment BookDetailsFragment.
     */
    public static BookDetailsFragment newInstance(String bookTitle) {
        BookDetailsFragment bookDetailsFragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putString(BOOK_TITLE_KEY, bookTitle);
        bookDetailsFragment.setArguments(args);
        return bookDetailsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            bookTitle = args.getString(BOOK_TITLE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        textView = (TextView) inflater.inflate(R.layout.fragment_book_details, container, false);
        if (bookTitle != null) {
            displayBook(bookTitle);
        }
        return textView;
    }

    // Public method for parent Activity to "talk" to BookDetailsFragment
    public void displayBook(String title) {
        textView.setText(title);
    }
}
