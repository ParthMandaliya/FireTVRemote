package com.firetvremote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cgutman.adblib.AdbCrypto;
import com.firetvremote.ui.Dialog;
import com.firetvremote.ui.SpinnerDialog;


public class MainActivity extends AppCompatActivity {

    public Button connectButton = null;
    public EditText ipField = null;
    public EditText portField = null;

    public SpinnerDialog keygenSpinner;

    public final static String PREFS_FILE = "AdbConnectPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* Setup our controls */
        initializeComponents();
        setupListeners();

        loadPreferences();

        AdbCrypto crypto = AdbUtils.readCryptoConfig(getFilesDir());
        if (crypto == null) {
            /* We need to make a new pair */
            keygenSpinner = SpinnerDialog.displayDialog(this,
                    "Generating RSA Key Pair",
                    "This will only be done once.",
                    true);

            new Thread(() -> {
                AdbCrypto crypto1;

                crypto1 = AdbUtils.writeNewCryptoConfig(getFilesDir());
                keygenSpinner.dismiss();

                if (crypto1 == null)
                {
                    Dialog.displayDialog(MainActivity.this, "Key Pair Generation Failed",
                            "Unable to generate and save RSA key pair",
                            true);
                    return;
                }

                Dialog.displayDialog(MainActivity.this, "New Key Pair Generated",
                        "Devices running 4.2.2 will need to be plugged in to a computer the next time you connect to them",
                        false);
            }).start();
        } else {
            connect();
        }
    }

    private void setupListeners() {
        connectButton.setOnClickListener(l -> connect());
    }

    private void initializeComponents() {
        ipField = findViewById(R.id.ipAddressField);
        portField = findViewById(R.id.portField);
        connectButton = findViewById(R.id.connect);
    }

    public void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_FILE, 0);
        ipField.setText(prefs.getString("IP", ""));
        portField.setText(prefs.getString("Port", "5555"));
    }

    public void savePreferences() {
        SharedPreferences.Editor prefs = getSharedPreferences(PREFS_FILE, 0).edit();
        prefs.putString("IP", ipField.getText().toString());
        prefs.putString("Port", portField.getText().toString());
        prefs.apply();
    }

    public void connect() {
        Intent shellIntent = new Intent(this, AdbShell.class);
        int port;

        shellIntent.putExtra("IP", ipField.getText().toString());
        try {
            port = Integer.parseInt(portField.getText().toString());
            if (port <= 0 || port > 65535) {
                Dialog.displayDialog(this, "Invalid Port", "The port number must be between 1 and 65535", false);
                return;
            }
            shellIntent.putExtra("Port", port);
        } catch (NumberFormatException e) {
            Dialog.displayDialog(this, "Invalid Port", "The port must be an integer", false);
            return;
        }

        savePreferences();
        startActivity(shellIntent);
    }
}