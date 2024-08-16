package com.firetvremote;


import com.cgutman.adblib.AdbCrypto;
import com.firetvremote.console.ConsoleBuffer;
import com.firetvremote.devconn.DeviceConnection;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firetvremote.devconn.DeviceConnectionListener;
import com.firetvremote.service.ShellService;
import com.firetvremote.ui.Dialog;
import com.firetvremote.ui.SpinnerDialog;
import com.firetvremote.utils.RunCommands;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AdbShell extends Activity implements DeviceConnectionListener {

    private Button upButton = null;
    private Button downButton = null;
    private Button leftButton = null;
    private Button rightButton = null;
    private Button okButton = null;
    private Button volumeUpButton = null;
    private Button volumeDownButton = null;
    private Button volumeMuteButton = null;
    private Button channelUpButton = null;
    private Button channelDownButton = null;
    private Button netflixButton = null;
    private Button primeVideoButton = null;
    private Button youtubeButton = null;
    private Button tvButton = null;
    private Button backButton = null;
    private Button backwardButton = null;
    private Button fastForwardButton = null;
    private Button playPauseButton = null;
    private Button homeButton = null;
    private Button optionsButton = null;
    private Button appsButton = null;
    private Button settingsButton = null;
    private Button sleepButton = null;
    private Button keyboardButton = null;
    private Button wakeUpButton = null;
    private Button headphoneButton = null;
    private Button disconnectButton = null;

    private Intent service = null;
    private ShellService.ShellServiceBinder binder = null;
    private SpinnerDialog connectWaiting = null;

    protected static DeviceConnection connection = null;

    private RunCommands runCommands = null;

    private String hostName = null;
    private int port;

    private final ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            binder = (ShellService.ShellServiceBinder)arg1;
            if (connection != null) {
                binder.removeListener(connection, AdbShell.this);
            }
            connection = AdbShell.this.connectOrLookupConnection(hostName, port);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            binder = null;
        }
    };

    public void onNewIntent(@NonNull Intent shellIntent) {
        hostName = shellIntent.getStringExtra("IP");
        port = shellIntent.getIntExtra("Port", -1);
        if (hostName == null || port == -1) {
            // If we were launched with no connection info, this was probably a pending intent
            // that's attempting to bring up the current in-progress connection. If we don't
            // have an existing connection, then we can do nothing and must finish ourselves.
            if (connection == null || binder == null) {
                finish();
            }
            return;
        }

        startForegroundService(service);

        if (binder == null) {
            /* Bind the service if we're not bound already. After binding, the callback will
             * perform the initial connection. */
            getApplicationContext().bindService(service, serviceConn, Service.BIND_AUTO_CREATE);
        }
        else {
            /* We're already bound, so do the connect or lookup immediately */
            if (connection != null) {
                binder.removeListener(connection, this);
            }
            connection = connectOrLookupConnection(hostName, port);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remote_layout);

        /* Setup our controls */
        initializeComponents();

        service = new Intent(this, ShellService.class);
        onNewIntent(getIntent());

        runCommands = new RunCommands();

        setupListeners();
    }

    private void initializeComponents() {
        upButton = findViewById(R.id.firetv_up);
        downButton = findViewById(R.id.firetv_down);
        leftButton = findViewById(R.id.firetv_left);
        rightButton = findViewById(R.id.firetv_right);
        okButton = findViewById(R.id.firetv_ok);
        volumeUpButton = findViewById(R.id.firetv_volup);
        volumeDownButton = findViewById(R.id.firetv_voldown);
        volumeMuteButton = findViewById(R.id.firetv_mute);
        channelUpButton = findViewById(R.id.firetv_channel_up);
        channelDownButton = findViewById(R.id.firetv_channel_down);
        netflixButton = findViewById(R.id.firetv_netflix);
        primeVideoButton = findViewById(R.id.firetv_primevideo);
        youtubeButton = findViewById(R.id.firetv_youtube);
        tvButton = findViewById(R.id.firetv_tv);
        backButton = findViewById(R.id.firetv_back);
        backwardButton = findViewById(R.id.firetv_backward);
        playPauseButton = findViewById(R.id.firetv_play_pause);
        fastForwardButton = findViewById(R.id.firetv_fast_forward);
        homeButton = findViewById(R.id.firetv_home);
        optionsButton = findViewById(R.id.firetv_options);
        appsButton = findViewById(R.id.firetv_apps);
        settingsButton = findViewById(R.id.firetv_settings);
        sleepButton = findViewById(R.id.firetv_sleep);
        keyboardButton = findViewById(R.id.firetv_keyboard);
        wakeUpButton = findViewById(R.id.firetv_wakeup);
        headphoneButton = findViewById(R.id.firetv_headphone);
        disconnectButton = findViewById(R.id.firetv_disconnect);
    }

    private void setupListeners() {
        upButton.setOnClickListener(l -> runCommands.upButtonPressed(connection));
        downButton.setOnClickListener(l -> runCommands.downButtonPressed(connection));
        leftButton.setOnClickListener(l -> runCommands.leftButtonPressed(connection));
        rightButton.setOnClickListener(l -> runCommands.rightButtonPressed(connection));
        okButton.setOnClickListener(l -> runCommands.okButtonPressed(connection));
        volumeUpButton.setOnClickListener(l -> runCommands.volumeUpButtonPressed(connection));
        volumeDownButton.setOnClickListener(l -> runCommands.volumeDownButtonPressed(connection));
        volumeMuteButton.setOnClickListener(l -> runCommands.volumeMuteButtonPressed(connection));
        channelUpButton.setOnClickListener(l -> runCommands.channelUpButtonPressed(connection));
        channelDownButton.setOnClickListener(l -> runCommands.channelDownButtonPressed(connection));
        netflixButton.setOnClickListener(l -> runCommands.openNetFlix(connection));
        primeVideoButton.setOnClickListener(l -> runCommands.openPrimeVideo(connection));
        youtubeButton.setOnClickListener(l -> runCommands.openYouTube(connection));
        tvButton.setOnClickListener(l -> runCommands.tvButtonPressed(connection));
        backButton.setOnClickListener(l -> runCommands.backButtonPressed(connection));
        backwardButton.setOnClickListener(l -> runCommands.stepBackWardButtonPressed(connection, false));
        backwardButton.setOnLongClickListener(l -> runCommands.stepBackWardButtonPressed(connection, true));
        playPauseButton.setOnClickListener(l -> runCommands.playPauseButtonPressed(connection));
        fastForwardButton.setOnClickListener(l -> runCommands.fastForwardButtonPressed(connection, false));
        fastForwardButton.setOnLongClickListener(l -> runCommands.fastForwardButtonPressed(connection, true));
        homeButton.setOnClickListener(l -> runCommands.homeButtonPressed(connection, false));
        homeButton.setOnLongClickListener(l -> runCommands.homeButtonPressed(connection, true));
        optionsButton.setOnClickListener(l -> runCommands.optionsButtonPressed(connection));
        appsButton.setOnClickListener(l -> runCommands.appsButtonPressed(connection));
        settingsButton.setOnClickListener(l -> runCommands.settingsButtonPressed(connection));
        sleepButton.setOnClickListener(l -> runCommands.sleepButtonPressed(connection));
        keyboardButton.setOnClickListener(l -> keyboardButtonPressed());
        wakeUpButton.setOnClickListener(l -> runCommands.wakeUpButtonPressed(connection));
        headphoneButton.setOnClickListener(l -> runCommands.headphoneButtonPressed(connection));
        disconnectButton.setOnClickListener(l -> disconnectButtonPressed());

        headphoneButton.setOnLongClickListener(l -> findProRemoteListener(connection));
    }

    public boolean findProRemoteListener(DeviceConnection connection) {
        try {
            runCommands.headphoneButtonPressed(connection);
            String op = new String(DeviceConnection.shellStream.read(), StandardCharsets.UTF_8);
            Toast.makeText(this, op, Toast.LENGTH_SHORT).show();
        } catch (InterruptedException | IOException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void disconnectButtonPressed() {
        new Thread(() -> {
            connection.close();
        }).start();

        AdbUtils.deleteCryptoConfigFiles(getFilesDir());
        Intent parentActivityIntent = getParentActivityIntent();
        if (parentActivityIntent != null) {
            startActivity(parentActivityIntent);
            finish();
        }
    }

    private void keyboardButtonPressed() {
        Intent showKeyboardLayout = new Intent(this, Search.class);
        startActivity(showKeyboardLayout);
    }

    @NonNull
    private DeviceConnection startConnection(String host, int port) {
        /* Display the connection progress spinner */
        connectWaiting = SpinnerDialog.displayDialog(this, "Connecting to "+hostName+":"+port,
                "Please make sure the target device has network ADB enabled.\n\n"+
                        "You may need to accept a prompt on the target device if you are connecting "+
                        "to it for the first time from this device.", true);

        /* Create the connection object */
        DeviceConnection conn = binder.createConnection(host, port);

        /* Add this activity as a connection listener */
        binder.addListener(conn, this);

        /* Begin the async connection process */
        conn.startConnect();

        return conn;
    }

    @NonNull
    private DeviceConnection connectOrLookupConnection(String host, int port) {
        DeviceConnection conn = binder.findConnection(host, port);
        if (conn == null) {
            /* No existing connection, so start the connection process */
            conn = startConnection(host, port);
        }
        else {
            /* Add ourselves as a new listener of this connection */
            binder.addListener(conn, this);
        }
        return conn;
    }

    @Override
    public void notifyConnectionEstablished(DeviceConnection devConn) {
        connectWaiting.dismiss();
        connectWaiting = null;
    }

    @Override
    public void notifyConnectionFailed(DeviceConnection devConn, @NonNull Exception e) {
        connectWaiting.dismiss();
        connectWaiting = null;

        Dialog.displayDialog(this, "Connection Failed", e.getMessage(), true);
    }

    @Override
    public void notifyStreamFailed(DeviceConnection devConn, @NonNull Exception e) {
        Dialog.displayDialog(this, "Connection Terminated", e.getMessage(), true);
    }

    @Override
    public void notifyStreamClosed(DeviceConnection devConn) {
        Dialog.displayDialog(this, "Connection Closed", "The connection was gracefully closed.", true);
    }

    @Override
    public AdbCrypto loadAdbCrypto(DeviceConnection devConn) {
        return AdbUtils.readCryptoConfig(getFilesDir());
    }

    @Override
    public boolean canReceiveData() {
        /* We just handle console updates */
        return false;
    }

    @Override
    public void receivedData(
        DeviceConnection devConn, byte[] data, int offset, int length
    ) {}

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public void consoleUpdated(DeviceConnection devConn, ConsoleBuffer console) {}
}
