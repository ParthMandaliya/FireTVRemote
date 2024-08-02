package com.firetvremote.utils;


import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.firetvremote.devconn.DeviceConnection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class RunCommands {
    public void upButtonPressed(@NonNull DeviceConnection connection) {
        runCommand(connection,"input dpad keyevent " + KeyEvents.KEYCODE_DPAD_UP);
    }

    public  void downButtonPressed(@NonNull DeviceConnection connection) {
        runCommand(connection,"input dpad keyevent " + KeyEvents.KEYCODE_DPAD_DOWN);
    }

    public void leftButtonPressed(@NonNull DeviceConnection connection) {
        runCommand(connection,"input dpad keyevent " + KeyEvents.KEYCODE_DPAD_LEFT);
    }

    public void rightButtonPressed(@NonNull DeviceConnection connection) {
        runCommand(connection,"input dpad keyevent " + KeyEvents.KEYCODE_DPAD_RIGHT);
    }

    public void okButtonPressed(@NonNull DeviceConnection connection) {
        runCommand(connection,"input dpad keyevent " + KeyEvents.KEYCODE_DPAD_CENTER);
    }

    public void volumeUpButtonPressed(@NonNull DeviceConnection connection) {
        runCommand(connection,"input keyboard keyevent " + KeyEvents.KEYCODE_VOLUME_UP);
    }

    public void volumeDownButtonPressed(@NonNull DeviceConnection connection) {
        runCommand(connection,"input keyboard keyevent " + KeyEvents.KEYCODE_VOLUME_DOWN);
    }

    public void volumeMuteButtonPressed(@NonNull DeviceConnection connection) {
        runCommand(connection,"input keyboard keyevent " + KeyEvents.KEYCODE_VOLUME_MUTE);
    }

    public void channelUpButtonPressed(DeviceConnection connection) {
        runCommand(connection,"input keyboard keyevent " + KeyEvents.KEYCODE_CHANNEL_UP);
    }

    public void channelDownButtonPressed(DeviceConnection connection) {
        runCommand(connection,"input keyboard keyevent " + KeyEvents.KEYCODE_CHANNEL_DOWN);
    }

    public void backButtonPressed(DeviceConnection connection) {
        runCommand(connection,"input keyboard keyevent " + KeyEvents.KEYCODE_BACK);
    }

    public void playPauseButtonPressed(DeviceConnection connection) {
        runCommand(connection,"input keyboard keyevent " + KeyEvents.KEYCODE_MEDIA_PLAY_PAUSE);
    }

    public boolean fastForwardButtonPressed(DeviceConnection connection, boolean longPressed) {
        if (longPressed)
            runCommand(
                connection,
                "input keyboard keyevent --longpress " + KeyEvents.KEYCODE_MEDIA_FAST_FORWARD
            );
        else
            runCommand(
                connection,
                "input keyboard keyevent " + KeyEvents.KEYCODE_MEDIA_FAST_FORWARD
            );
        return true;
    }

    public boolean stepBackWardButtonPressed(DeviceConnection connection, boolean longPressed) {
        if (longPressed)
            runCommand(
                connection,
                "input keyboard keyevent --longpress " + KeyEvents.KEYCODE_MEDIA_REWIND
            );
        else
            runCommand(
                connection, "input keyboard keyevent " + KeyEvents.KEYCODE_MEDIA_REWIND
            );
        return true;
    }

    public void openNetFlix(DeviceConnection connection) {
        String packageName = TVApps.NETFLIX.split("/", 2)[0];
        if (isAppInstalled(connection, packageName))
            runCommand(connection,"am start -W " + TVApps.NETFLIX);
    }

    public void openPrimeVideo(DeviceConnection connection) {
        String packageName = TVApps.PRIMEVIDEO.split("/", 2)[0];
        if (isAppInstalled(connection, packageName))
            runCommand(connection,"am start -W " + TVApps.PRIMEVIDEO);
    }

    public void openYouTube(DeviceConnection connection) {
        if (isAppInstalled(connection, TVApps.YOUTUBE))
            runCommand(connection,"am start -W " + TVApps.YOUTUBE);
    }

    public void tvButtonPressed(DeviceConnection connection) {
        String packageName = TVApps.MINITV.split("/", 2)[0];
        if (isAppInstalled(connection, packageName))
            runCommand(connection,"am start -W " + TVApps.MINITV);
    }

    public boolean homeButtonPressed(DeviceConnection connection, boolean longPressed) {
        if (longPressed)
            runCommand(connection, "input keyboard keyevent --longpress " + KeyEvents.KEYCODE_HOME);
        else
            runCommand(connection, "input keyboard keyevent " + KeyEvents.KEYCODE_HOME);
        return true;
    }

    public void optionsButtonPressed(DeviceConnection connection) {
        runCommand(connection, "input keyboard keyevent " + KeyEvents.KEYCODE_MENU);
    }

    public void settingsButtonPressed(DeviceConnection connection) {
        runCommand(connection, "am start -W " + TVApps.SETTINGS);
    }

    public void appsButtonPressed(DeviceConnection connection) {
        runCommand(connection, "am start -W " + TVApps.HOME);
    }

    public void sleepButtonPressed(DeviceConnection connection) {
        runCommand(connection, "input keyboard keyevent " + KeyEvents.KEYCODE_SLEEP);
    }

    public void wakeUpButtonPressed(DeviceConnection connection) {
        runCommand(connection,  "input keyboard keyevent " + KeyEvents.KEYCODE_WAKEUP);
    }

    public void searchButtonPressed(DeviceConnection connection, String searchTerm) {
//        runCommand(connection, "input keyevent " + KeyEvents.KEYCODE_MOVE_END);
//        runCommand(connection, "input keyevent $(printf 'KEYCODE_DEL' {1..250})");

//        TODO: Figure out a way to clear the textbox remotely
        runCommand(connection, "input keycombination 113 29 && input keyevent 67");
        runCommand(connection, "input keyboard text " + searchTerm);
    }

    private boolean isAppInstalled(DeviceConnection connection, String package_name) {
        try {
            runCommand(connection, "pm list package | grep " + package_name);
            String op = new String(DeviceConnection.shellStream.read(), StandardCharsets.UTF_8);
            return op.contains(package_name);
        } catch (IOException | InterruptedException ignore) {}
        return false;
    }

    public void runCommand(@NonNull DeviceConnection connection, String cmd) {
        connection.queueCommand(cmd+"\n");
    }
    public void runCommand(@NonNull DeviceConnection connection, byte[] cmd) {
        connection.queueCommand(cmd);
    }
}
