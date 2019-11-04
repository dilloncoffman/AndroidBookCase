package edu.temple.bookcase;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailsFragment extends Fragment {

    TextView bookTitleTextView;
    public static final String BOOK_KEY = "book";
    Book book;


    public BookDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param book Book.
     * @return A new instance of fragment BookDetailsFragment.
     */
    public static BookDetailsFragment newInstance(Book book) {
        BookDetailsFragment bookDetailsFragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(BOOK_KEY, book);
        bookDetailsFragment.setArguments(args);
        return bookDetailsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            book = args.getParcelable(BOOK_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        bookTitleTextView = (TextView) inflater.inflate(R.layout.fragment_book_details, container, false);
        if (book != null) {
            displayBook(book);
        }
        return bookTitleTextView;
    }

    // Public method for parent Activity to "talk" to BookDetailsFragment
    public void displayBook(Book book) {
        bookTitleTextView.setGravity(Gravity.CENTER);
        bookTitleTextView.setText(book.getTitle());
        bookTitleTextView.setTextSize(35);
    }
}
