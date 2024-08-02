package com.firetvremote;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firetvremote.utils.RunCommands;


public class Search extends Activity {

    private EditText searchTermEditText = null;
    private Button searchButton = null;

    private RunCommands runCommands = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyboard_layout);

        runCommands = new RunCommands();

        initializeComponents();
        setupListeners();
    }

    private void initializeComponents() {
        searchTermEditText = findViewById(R.id.firetv_search_term);
        searchButton = findViewById(R.id.firetv_send);

        searchTermEditText.requestFocus();
    }

    private void setupListeners() {
        searchButton.setOnClickListener(l -> runCommands.searchButtonPressed(
            AdbShell.connection, searchTermEditText.getText().toString().trim()
        ));
    }
}
