package com.firetvremote.utils;

import androidx.annotation.NonNull;

import com.firetvremote.devconn.DeviceConnection;

import org.jetbrains.annotations.NotNull;


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

    public void runCommand(@NonNull DeviceConnection connection, @NotNull String cmd) {
        connection.queueCommand(cmd+"\n");
    }
}
