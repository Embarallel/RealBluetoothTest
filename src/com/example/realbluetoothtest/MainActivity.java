package com.example.realbluetoothtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        ServerThread myAccept = new ServerThread();
        myAccept.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startServer(View v) {
    	ServerThread at = new ServerThread();
    	at.start();
    }
    
    public void startClient(View v) {
    	ClientThread ct = new ClientThread();
    	ct.start();
    }
    
    public void sendMessage(View v) {
    	ms.write("HELLO WORLD".getBytes());
    }
    
    public void receiveMessage(View v) {
    	String s = mc.get();
    	Context context = getApplicationContext();
    	CharSequence text = s;
    	int duration = Toast.LENGTH_SHORT;

    	Toast toast = Toast.makeText(context, text, duration);
    	toast.show();
    }
    
    //**********************************************************************************************************************
    //BEGIN MEMBER CLASSES
    //**********************************************************************************************************************
    
    private ManageServer ms; 
    
    private class ServerThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
     
        public ServerThread() {
        	
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("ServerTest", UUID.fromString("426a19a0-b234-11e3-a5e2-0800200c9a66"));
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }
     
        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    ms = new ManageServer(socket);
                    try {
                    	mmServerSocket.close();
                    }
                    catch(IOException ioe) { }
                    break;
                }
            }
        }
     
        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }
    
    private class ManageServer {
    	private OutputStream os;
    	
    	public ManageServer(BluetoothSocket bs)
    	{
    		try	{
    		os = bs.getOutputStream();
    		}
    		catch(IOException ioe) {}	
    	}
    	
    	public void write(byte[] bytes) {
            try {
                os.write(bytes);
            } catch (IOException e) { }
        }
    }
    
    private ManageClient mc;
    
    private class ClientThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        public ClientThread() {
        	
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            
            Iterator<BluetoothDevice> i = pairedDevices.iterator();
            mmDevice = i.next();
     
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("426a19a0-b234-11e3-a5e2-0800200c9a66"));
            } catch (IOException e) { }
            mmSocket = tmp;
        }
     
        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();
     
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }
     
            // Do work to manage the connection (in a separate thread)
            mc = new ManageClient(mmSocket);
            mc.start();
        }
     
        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    
    class ManageClient extends Thread {
    	private InputStream is;
    	public ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(1);
    	
    	public ManageClient(BluetoothSocket bs) {
    		try	{
        		is = bs.getInputStream();
        		}
        		catch(IOException ioe) {}	
    	}
    	
    	@Override
    	public void run() {
    		byte[] buffer = new byte[1024];
    		int bytes;
    		
    		while (true) {
                try {
                    // Read from the InputStream
                    bytes = is.read(buffer);
                    String s = new String(buffer, 0, bytes);
                    buffer = new byte[1024];
                    queue.put(s);
                } catch (IOException e) {
                    break;
                } catch (InterruptedException ie) {
                	break;
                }
            }
    	}
    	
    	public String get() {
    		try {
    			return queue.take(); 
    		}
    		catch(InterruptedException ie) {
    			return "ERROR";
    		}
    	}
    }
}