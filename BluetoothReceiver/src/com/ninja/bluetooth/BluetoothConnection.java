package com.ninja.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Modeled on from code from BluetoothChat. Source: <sdk>/platforms/android-<version>/samples/ 
 * Handles the Bluetooth connections
 */
public class BluetoothConnection {
	// Debugging
	private static final String TAG = "BluetoothConnection";
	private static final boolean D = true;

	//Standard SerialPortService ID
	private static final UUID MY_UUID_SECURE = UUID.fromString(Constants.STANDARD_SERIAL_PORT_UUID_STRING); 

	// Member fields
	private final BluetoothAdapter mAdapter;
	private Handler mHandler;
	private ConnectedThread mConnectedThread;
	private ConnectThread mConnectThread;
	private int mState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
	public static final int STATE_CONNECTED = 2; // now connected to a remote device

	private static BluetoothConnection btConnection;
	
	public static BluetoothConnection getBluetoothConnection(Context context, Handler handler){
		if(btConnection == null){
			btConnection = new BluetoothConnection(context, handler);
		}
		return btConnection;
	}
	
	private BluetoothConnection(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}

	/**
	 * Set the current state of the connection
	 * @param state An integer defining the current connection state
	 */
	private synchronized void setState(int state) {
		if (D){
			Log.d(TAG, "setState() " + mState + " -> " + state);
		}
		mState = state;
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * Establishes a connection to the Panic Button
	 */
	public synchronized void connectToPanicButton() {
		if (D){
			Log.d(TAG, "Connecting to Panic Button");
		}
		
		BluetoothDevice panicButtonDevice = mAdapter.getRemoteDevice(Constants.PANIC_BUTTON_MAC_ADDRESS);
		connect(panicButtonDevice);
	}
	
	 /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    private synchronized void connect(BluetoothDevice device) {
		if (D) {
			Log.d(TAG, "connect to: " + device);
		}

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
    }


	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * @param socket The BluetoothSocket on which the connection was made
	 * @param device The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		if (D){
			Log.d(TAG, "connected");
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();
		
		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "stop");

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		
		setState(STATE_NONE);
	}
	
	 /**
     * On failure, continue attempts to connect to Panic Button
     */
    private void connectionFailed() {
        connectToPanicButton();
    }

    /**
     * If connection is lost abruptly, reconnect to Panic Button
     */
    private void connectionLost() {
        connectToPanicButton();
    }
    
     /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
             tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
              
            } catch (IOException e) {
                Log.d(TAG, "Socket create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.d(TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothConnection.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.d(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
        }

        public void run() {
            Log.v(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    String message = Utils.parseArduinoMessage(buffer);
                    
                    // Send the obtained bytes to the BuzzGuardianService 
                    Log.d(TAG, "Incoming data from Bluetooth Connection");
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, message)
                            .sendToTarget();
               
                } catch (IOException e) {
                    Log.d(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
