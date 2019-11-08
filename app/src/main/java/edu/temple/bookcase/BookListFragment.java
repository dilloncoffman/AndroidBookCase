package edu.temple.bookcase;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BookListFragment.OnBookSelectedInterface} interface
 * to handle interaction events.
 * Use the {@link BookListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookListFragment extends Fragment {
    ArrayList<Book> books;

    public final static String BOOKS_KEY = "books";

    private OnBookSelectedInterface fragmentParent;

    public BookListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param books ArrayList<String> of book titles
     * @return A new instance of fragment BookListFragment.
     */
    public static BookListFragment newInstance(ArrayList<Book> books) {
        BookListFragment bookListFragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(BOOKS_KEY, books);
        bookListFragment.setArguments(args);
        return bookListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            books = args.getParcelableArrayList(BOOKS_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ListView listView = (ListView) inflater.inflate(R.layout.fragment_book_list, container, false);

        listView.setAdapter(new ArrayAdapter<>((Context) fragmentParent, android.R.layout.simple_list_item_1, books));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fragmentParent.bookSelected(position);
            }
        });

        return listView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBookSelectedInterface) {
            fragmentParent = (OnBookSelectedInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBookSelectedInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentParent = null;
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
    public interface OnBookSelectedInterface {
        void bookSelected(int position);
    }
}
