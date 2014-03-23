package com.example.realbluetoothtest;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothJoin extends Thread {
	private final BluetoothSocket mySocket;
	private final BluetoothDevice myFriendDevice;
	private BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private final Closeable[] c;

	//First spot will hold an input stream, second spot will hold an output stream.  Wait until both are occupied
	public BluetoothJoin(Closeable[] c) {
		// Use a temporary object that is later assigned to mmSocket, because mmSocket is final
		BluetoothSocket tmp = null;

		Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();

		//TASK:  Allow user to select device to connect to
		Iterator<BluetoothDevice> i = pairedDevices.iterator();
		myFriendDevice = i.next();

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			// MY_UUID is the app's UUID string, also used by the server code
			tmp = myFriendDevice.createRfcommSocketToServiceRecord(UUID.fromString("426a19a0-b234-11e3-a5e2-0800200c9a66"));
		} catch (IOException e) {
			Log.e("BluetoothJoin", "IOException at Socket creation");
		}
		mySocket = tmp;
		
		this.c = c;
	}

	public void run() {
		// Cancel discovery because it will slow down the connection
		myBluetoothAdapter.cancelDiscovery();

		try {
			// Connect the device through the socket. This will block until it succeeds or throws an exception
			mySocket.connect();
		} catch (IOException connectException) {
			Log.e("BluetoothJoin", "IOException at connect");
			// Unable to connect; close the socket and get out
			try {
				mySocket.close();
			} catch (IOException closeException) {
				Log.e("BluetoothJoin", "IOException at close");
			}
			return;
		}
		try {
			c[0] = mySocket.getInputStream();
			c[1] = mySocket.getOutputStream();
		} catch (IOException ioe) {
			Log.e("BluetoothJoin", "IOException at stream creation");
		}
	}

	//Will cancel an in-progress connection, and close the socket
	public void cancel() {
		try {
			mySocket.close();
		} catch (IOException e) {
		}
	}
}
