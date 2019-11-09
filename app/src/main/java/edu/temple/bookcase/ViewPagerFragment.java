package edu.temple.bookcase;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewPagerFragment extends Fragment {
    ViewPager mPager;
    PagerAdapter pagerAdapter;
    ArrayList<Book> books;
    ArrayList<BookDetailsFragment> bookDetailsFragments;
    public final static String BOOKS_KEY = "books";

    public ViewPagerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param books ArrayList<String> of book titles
     * @return A new instance of fragment ViewPagerFragment.
     */
    public static ViewPagerFragment newInstance(ArrayList<Book> books) {
        ViewPagerFragment viewPagerFragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(BOOKS_KEY, books);
        viewPagerFragment.setArguments(args);
        return viewPagerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        bookDetailsFragments = new ArrayList<>();
        if (args != null) {
            books = args.getParcelableArrayList(BOOKS_KEY);
            // For each book in the books ArrayList<Book>, create a BookDetailsFragment
            if (books != null) {
                for (int i = 0; i < books.size(); i++) {
                    bookDetailsFragments.add(BookDetailsFragment.newInstance(books.get(i)));
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);
        mPager = view.findViewById(R.id.viewPagerFragment);
        // Instantiate a ViewPager and a PagerAdapter.
        pagerAdapter = new BookDetailsPagerAdapter(getChildFragmentManager(), bookDetailsFragments);
        mPager.setAdapter(pagerAdapter);
        return view;
    }

    public ArrayList<Book> getBooks() {
        return this.books;
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class BookDetailsPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<BookDetailsFragment> bookDetailsFragments; // hold reference to ViewPagers fragments

        BookDetailsPagerAdapter(FragmentManager fm, ArrayList<BookDetailsFragment> bookDetailsFragments) {
            super(fm);
            this.bookDetailsFragments = bookDetailsFragments;
        }

        @Override
        public Fragment getItem(int position) {
            return bookDetailsFragments.get(position);
        }

        @Override
        public int getCount() {
            return bookDetailsFragments.size();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }
}
