package com.example.realbluetoothtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Closeable;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.util.Log;

public class MainActivity extends Activity {
	private InputStream is;
	private OutputStream os;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void host(View v) {
    	Closeable[] c = new Closeable[2];
    	BluetoothHost bh = new BluetoothHost(c);
    	bh.start();
    	while(c[0] == null || c[1] == null)
    	{
    		try {
    		Thread.sleep(50);
    		} catch(InterruptedException ie) {
    			Log.e("MainActivity", "Thread sleep interrupted, host");
    			break;
    		}
    	}
    	is = (InputStream) c[0];
    	os = (OutputStream) c[1];
    }
    
    public void join(View v) {
    	Closeable[] c = new Closeable[2];
    	BluetoothJoin bj = new BluetoothJoin(c);
    	bj.start();
    	while(c[0] != null && c[1] != null)
    	{
    		try {
    		Thread.sleep(50);
    		} catch(InterruptedException ie) {
    			Log.e("MainActivity", "Thread sleep interrupted, join");
    			break;
    		}
    	}
    	is = (InputStream) c[0];
    	os = (OutputStream) c[1];
    }
    
    class BluetoothListener extends Thread {
    	private InputStream is;
    	BluetoothListener(InputStream is) {
    		this.is = is;
    	}
    	
    	public void run() {
    		byte[] buffer = new byte[1024];
    		int bytes;
    		
    		while (true) {
                try {
                    // Read from the InputStream
                    bytes = is.read(buffer);
                    String s = new String(buffer, 0, bytes);
                    buffer = new byte[1024];
                    handle(s);
                } catch (IOException e) {
                	Log.e("MainActivity", "Error reading from input stream");
                    break;
                }
            }
    	}
    }
    
    public void handle(String s) {
    	Context context = getApplicationContext();
    	CharSequence text = s;
    	int duration = Toast.LENGTH_SHORT;

    	Toast toast = Toast.makeText(context, text, duration);
    	toast.show();
    }
    
    public void sendMessage(View v) {
    	try {
    		os.write("HELLO WORLD".getBytes());
    	} catch(IOException ioe) {
    		Log.e("MainActivity", "Error writing to output stream");
    	}
    }
}