package com.v.GREWords;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class FullList extends Activity {

    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;
    DataBaseHelper data = new DataBaseHelper(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_list);
        ListView list = (ListView) findViewById(R.id.listView);
        listItems = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        populateList();
        list.setAdapter(adapter);
        list.setTextFilterEnabled(true);

    }


    private void populateList() {

        for (int i = 0; i < 4800; i++) {
            listItems.add(data.getWordInOrder());
        }
        adapter.notifyDataSetChanged();
    }


}
