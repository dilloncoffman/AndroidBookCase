package edu.temple.bookcase;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    List<String> books = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get books from books string-array resource
        Resources res = getResources();
        books = Arrays.asList(res.getStringArray(R.array.books));

        for (String book : books){
            Log.d("Book name: ", book);
        }
    }
}
