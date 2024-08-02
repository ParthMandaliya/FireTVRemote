package com.firetvremote.utils;


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

    public void tvButtonPressed(DeviceConnection connection) {
        runCommand(connection,"input keyboard keyevent " + KeyEvents.KEYCODE_TV);
    }

    public void backButtonPressed(DeviceConnection connection) {
        runCommand(connection,"input keyboard keyevent " + KeyEvents.KEYCODE_BACK);
    }

    public void playPauseButtonPressed(DeviceConnection connection) {
        runCommand(connection,"input keyboard keyevent " + KeyEvents.KEYCODE_MEDIA_PLAY_PAUSE);
    }

    public void fastForwardButtonPressed(DeviceConnection connection) {
        runCommand(connection,"input keyboard keyevent " + KeyEvents.KEYCODE_MEDIA_FAST_FORWARD);
    }

    public void stepBackWardButtonPressed(DeviceConnection connection) {
        runCommand(connection,"input keyboard keyevent " + KeyEvents.KEYCODE_MEDIA_REWIND);
    }

    public void openNetFlix(DeviceConnection connection) {
        String packageName = TVApps.NETFLIX.split("/", 2)[0];
        if (isAppInstalled(connection, packageName))
            runCommand(connection,"am start -W " + TVApps.NETFLIX);
    }

    public void openPrimeVideo(DeviceConnection connection) {}

    public void openYouTube(DeviceConnection connection) {}

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
}
