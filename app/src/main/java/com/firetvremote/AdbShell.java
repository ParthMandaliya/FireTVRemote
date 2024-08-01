package com.firetvremote;


import com.cgutman.adblib.AdbCrypto;
import com.firetvremote.console.ConsoleBuffer;
import com.firetvremote.devconn.DeviceConnection;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.widget.Button;

import com.firetvremote.console.CommandHistory;
import com.firetvremote.devconn.DeviceConnectionListener;
import com.firetvremote.devconn.KeyEvents;
import com.firetvremote.service.ShellService;
import com.firetvremote.ui.Dialog;
import com.firetvremote.ui.SpinnerDialog;

public class AdbShell extends Activity implements DeviceConnectionListener {

    private Button upButton = null;
    private Button downButton = null;
    private Button leftButton = null;
    private Button rightButton = null;
    private Button okButton = null;

    private Intent service = null;
    private ShellService.ShellServiceBinder binder = null;
    private SpinnerDialog connectWaiting = null;

    private CommandHistory commandHistory = null;
    private StringBuilder commandBuffer = new StringBuilder();

    private DeviceConnection connection = null;

    private String hostName = null;
    private int port;

    private final static String PREFS_FILE = "AdbCmdHistoryPrefs";
    private static final int MAX_COMMAND_HISTORY = 15;

    private ServiceConnection serviceConn = new ServiceConnection() {
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

    public void onNewIntent(Intent shellIntent) {
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

        setTitle("ADB Shell - "+hostName+":"+port);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service);
        }
        else {
            startService(service);
        }

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
        upButton = (Button) findViewById(R.id.firetv_up);
        downButton = (Button) findViewById(R.id.firetv_down);
        leftButton = (Button) findViewById(R.id.firetv_left);
        rightButton = (Button) findViewById(R.id.firetv_right);
        okButton = (Button) findViewById(R.id.firetv_ok);

        upButton.setOnClickListener(l -> {
            upButtonPressed();
        });
        downButton.setOnClickListener(l -> {
            downButtonPressed();
        });
        leftButton.setOnClickListener(l -> {
            leftButtonPressed();
        });
        rightButton.setOnClickListener(l -> {
            rightButtonPressed();
        });
        okButton.setOnClickListener(l -> {
            okButtonPressed();
        });

        commandHistory = CommandHistory.loadCommandHistoryFromPrefs(MAX_COMMAND_HISTORY, this, PREFS_FILE);
        service = new Intent(this, ShellService.class);
        onNewIntent(getIntent());
    }

    private void upButtonPressed() {
        runCommand("input keyboard keyevent " + Integer.toString(KeyEvents.KEYCODE_DPAD_UP));
    }

    private  void downButtonPressed() {
        runCommand("input keyboard keyevent " + Integer.toString(KeyEvents.KEYCODE_DPAD_DOWN));
    }

    private void leftButtonPressed() {
        runCommand("input keyboard keyevent " + Integer.toString(KeyEvents.KEYCODE_DPAD_LEFT));
    }

    private void rightButtonPressed() {
        runCommand("input keyboard keyevent " + Integer.toString(KeyEvents.KEYCODE_DPAD_RIGHT));
    }

    private void okButtonPressed() {
        runCommand("input keyboard keyevent " + Integer.toString(KeyEvents.KEYCODE_DPAD_CENTER));
    }

    public void runCommand(String cmd) {
        commandBuffer.append(cmd);
        commandHistory.add(cmd);
        commandBuffer.append('\n');
        connection.queueCommand(commandBuffer.toString());
        commandBuffer.setLength(0);
    }

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
    public void notifyConnectionFailed(DeviceConnection devConn, Exception e) {
        connectWaiting.dismiss();
        connectWaiting = null;

        Dialog.displayDialog(this, "Connection Failed", e.getMessage(), true);
    }

    @Override
    public void notifyStreamFailed(DeviceConnection devConn, Exception e) {
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
    ) {
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public void consoleUpdated(DeviceConnection devConn, ConsoleBuffer console) {

    }
}
