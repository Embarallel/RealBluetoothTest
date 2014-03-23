package com.example.realbluetoothtest;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothHost extends Thread {
	private final BluetoothServerSocket myServerSocket;
	private final Closeable[] c;
	
	//First spot will hold an input stream, second spot will hold an output stream.  Wait until both are occupied
	public BluetoothHost(Closeable[] c) { 
		// Use a temporary object that is later assigned to mmServerSocket,
		// because mmServerSocket is final
		BluetoothServerSocket tmp = null;
		try {
			// MY_UUID is the app's UUID string, also used by the client code
			tmp = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord("ServerTest", UUID.fromString("426a19a0-b234-11e3-a5e2-0800200c9a66"));
		} catch (IOException e) {
			Log.e("BluetoothHost", "IOException at ServerSocket creation");
		}
		myServerSocket = tmp;
		this.c = c;
	}
	
	public void run() {
		BluetoothSocket mySocket = null;
		// Keep listening until exception occurs or a socket is returned
		while (true) {
			try {
				mySocket = myServerSocket.accept();
			} catch (IOException e) {
				Log.e("BluetoothHost", "IOException at Socket creation");
				break;
			}
			// If a connection was accepted
			if (mySocket != null) {
				try {
					c[0] = mySocket.getInputStream();
					c[1] = mySocket.getOutputStream();
					myServerSocket.close();
				} catch (IOException ioe) {
					Log.e("BluetoothHost", "IOException at stream creation");
				}
				break;
			}
		}
	}
	
	public void cancel() {
		try {
			myServerSocket.close();
		} catch (IOException e) { }
	}
}
