package edu.temple.bookcase;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BookDetailsFragment.OnBookPlay} interface
 * to handle interaction events.
 * Use the {@link BookDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookDetailsFragment extends Fragment {

    public static final String BOOK_KEY = "book";
    private OnBookPlay fragmentParent;
    ConstraintLayout bookDetailsView;
    ImageView bookCover;
    TextView bookTitle, bookAuthor, bookPublishedIn, bookPageLength;
    Book book;
    Button playBtn;


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
        bookDetailsView = (ConstraintLayout) inflater.inflate(R.layout.fragment_book_details, container, false);
        return bookDetailsView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookCover = Objects.requireNonNull(getView()).findViewById(R.id.bookCover);
        bookTitle = Objects.requireNonNull(getView()).findViewById(R.id.bookTitle);
        bookAuthor = getView().findViewById(R.id.bookAuthor);
        bookPublishedIn = getView().findViewById(R.id.bookPublishedIn);
        bookPageLength = getView().findViewById(R.id.bookPageLength);
        if (book != null) {
            displayBook(book);
            playBtn = getView().findViewById(R.id.playBtn);
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   fragmentParent.playBook(book.getId());
                }
            });
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookDetailsFragment.OnBookPlay) {
            fragmentParent = (BookDetailsFragment.OnBookPlay) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBookPlay interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentParent = null;
    }


    // Public method for parent Activity to "talk" to BookDetailsFragment
    public void displayBook(Book book) {
        Picasso.get().load(book.getCoverUrl()).into(bookCover);
        bookTitle.setText(book.getTitle());
        bookTitle.setGravity(Gravity.CENTER);

        bookAuthor.setText(book.getAuthor());
        bookAuthor.setGravity(Gravity.CENTER);

        bookPublishedIn.setText(String.format(getResources().getString(R.string.publishedIn), book.getPublished()));
        bookPublishedIn.setGravity(Gravity.CENTER);

        bookPageLength.setText(String.format(getResources().getString(R.string.pageLength), book.getDuration()));
        bookPageLength.setGravity(Gravity.CENTER);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnBookPlay {
        void playBook(int bookId);
    }
}
