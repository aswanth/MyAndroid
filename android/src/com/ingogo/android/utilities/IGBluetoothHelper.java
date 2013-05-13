package com.ingogo.android.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.ingogo.android.R;
import com.ingogo.android.app.IngogoApp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for incoming
 * connections, a thread for connecting with a device, and a thread for
 * performing data transmissions when connected.
 */
public class IGBluetoothHelper {
	// Debugging
	private static final String TAG = "BPBluetoothHelper";
	private static final boolean D = false;

	// Name for the Service Discovery record when creating server socket
	private static final String NAME = "Ingogo";

	// Commonly known UUID for Serial Port Profile
	public static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Member fields
	private final BluetoothAdapter _bAdapter;

	private final IGBluetoothReceiveListener _btReciever;
	private AcceptThread _bAcceptThread;
	private ConnectThread _bConnectThread;
	private ConnectedThread _bConnectedThread;
	private int _bState;

	private boolean _isPairing = false;
	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	


	/**
	 * Constructor. Prepares a new BlueToothReceiverActivity session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param _bReciever
	 *            A Handler to send messages back to the UI Activity
	 */
	public IGBluetoothHelper(Context context, IGBluetoothReceiveListener _bReciever) {
		_bAdapter = BluetoothAdapter.getDefaultAdapter();
		_bState = STATE_NONE;
		_btReciever = _bReciever;

	}
	/**
	 * Constructor. Prepares a new BlueToothReceiverActivity session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param _bReciever
	 *            A Handler to send messages back to the UI Activity
	 */
	public IGBluetoothHelper(Context context, IGBluetoothReceiveListener _bReciever,boolean forPairing) {
		_bAdapter = BluetoothAdapter.getDefaultAdapter();
		_bState = STATE_NONE;
		_btReciever = _bReciever;
		_isPairing = forPairing;

	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(int state) {

		Log.e("Test", "State changed = "+state );
		_bState = state;
		_btReciever.changeOfStatusResult(state);
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return _bState;
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void start() {


		// Cancel any thread attempting to make a connection
		if (_bConnectThread != null) {
			_bConnectThread.cancel();
			_bConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (_bConnectedThread != null) {
			_bConnectedThread.cancel();
			_bConnectedThread = null;
		}

		// Start the thread to listen on a BluetoothServerSocket
		if (_bAcceptThread == null) {
			_bAcceptThread = new AcceptThread();
			_bAcceptThread.start();
		}
		setState(STATE_LISTEN);
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 */
	public synchronized void connect(BluetoothDevice device,boolean isPairing) {

		_isPairing = isPairing;
		// Cancel any thread attempting to make a connection
		if (_bState == STATE_CONNECTING) {
			if (_bConnectThread != null) {
				_bConnectThread.cancel();
				_bConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (_bConnectedThread != null) {
			_bConnectedThread.cancel();
			_bConnectedThread = null;
		}

		// Start the thread to connect with the given device
		_bConnectThread = new ConnectThread(device);
		_bConnectThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {


		// Cancel the thread that completed the connection
		if (_bConnectThread != null) {
			_bConnectThread.cancel();
			_bConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (_bConnectedThread != null) {
			_bConnectedThread.cancel();
			_bConnectedThread = null;
		}

		// Cancel the accept thread because we only want to connect to one
		// device
		if (_bAcceptThread != null) {
			_bAcceptThread.cancel();
			_bAcceptThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		_bConnectedThread = new ConnectedThread(socket);
		_bConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		if(!_isPairing){
		_btReciever.connectedResult(device.getName());
		}else {
			_btReciever.pairingCompletedResult();
		}
		setState(STATE_CONNECTED);

//		String testStringToSend = "Hello World";
//		String hexString = asHex(testStringToSend.getBytes());
//		byte[] unsignedByte = hexStringToByteArray(hexString);
//
//		write(unsignedByte);
	}

	
	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "** stop");

		if (_bConnectThread != null) {
//			try {
//				_bConnectThread._bSocket.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

			_bConnectThread.cancel();
			_bConnectThread = null;
		}
		if (_bConnectedThread != null) {
			try {
				_bConnectedThread._bSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_bConnectedThread.cancel();
			_bConnectedThread = null;
		}
		if (_bAcceptThread != null) {
			_bAcceptThread.cancel();
			_bAcceptThread = null;
		}
		setState(STATE_NONE);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		
		synchronized (this) {
			if (_bState != STATE_CONNECTED)
				return;
			r = _bConnectedThread;
		}
		
		
		// Perform the write unsynchronized
		r.write(out);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		setState(STATE_LISTEN);

		// Send a failure message back to the Activity
		_btReciever.connectionFailedResult(IngogoApp.getSharedApplication().getString(R.string.print_error_report_message));
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		setState(STATE_LISTEN);

		// Send a failure message back to the Activity
		_btReciever.connectionLostResult("Device connection was lost");

	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted (or
	 * until cancelled).
	 */
	private class AcceptThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket _bServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;

			// Create a new listening server socket
			try {
				tmp = _bAdapter.listenUsingRfcommWithServiceRecord(NAME,
						MY_UUID);
			} catch (IOException e) {
				if (D)
					Log.e(TAG, "** listen() failed", e);
			}
			_bServerSocket = tmp;
		}

		public void run() {
			if (D)
				Log.d(TAG, "** BEGIN _bAcceptThread" + this);

			try {

				setName("AcceptThread");
				BluetoothSocket socket = null;

				// Listen to the server socket if we're not connected
				while (_bState != STATE_CONNECTED) {
					try {
						// This is a blocking call and will only return on a
						// successful connection or an exception
						socket = _bServerSocket.accept();
					} catch (IOException e) {
						if (D)
							Log.e(TAG, "** accept() failed", e);

						break;
					}

					// If a connection was accepted
					if (socket != null) {
						synchronized (IGBluetoothHelper.this) {
							switch (_bState) {
							case STATE_LISTEN:
							case STATE_CONNECTING:
								// Situation normal. Start the connected thread.
								connected(socket, socket.getRemoteDevice());
								break;
							case STATE_NONE:
							case STATE_CONNECTED:
								// Either not ready or already connected.
								// Terminate
								// new socket.
								try {
									socket.close();
								} catch (IOException e) {
									if (D)
										Log.e(TAG,
												"** Could not close unwanted socket",
												e);
								}
								break;
							}
						}
					}
				}

			} catch (NullPointerException e) {
				// TODO: handle exception
			}

			if (D)
				Log.i(TAG, "** END _bAcceptThread");
		}

		public void cancel() {
			if (D)
				Log.d(TAG, "** cancel " + this);
			try {
				_bServerSocket.close();
			} catch (IOException e) {
				if (D)
					Log.e(TAG, "** close() of server failed", e);
			} catch (NullPointerException e) {
				// TODO: handle exception
			}
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket _bSocket;
		private final BluetoothDevice _bDevice;

		public ConnectThread(BluetoothDevice device) {
			_bDevice = device;
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				if (D)
					Log.e(TAG, "** create() failed", e);
			}
			_bSocket = tmp;
		}

		public void run() {

			if (D)
				Log.i(TAG, "** BEGIN _bConnectThread");
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			_bAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				_bSocket.connect();
			} catch (IOException e) {
				Log.i("TAG", "Connection failed " + e.getLocalizedMessage());

				setState(STATE_LISTEN);
				if(_isPairing)
				_btReciever.pairingFailedResult();
				else 
					connectionFailed();

				// Close the socket
				try {
					_bSocket.close();
				} catch (IOException e2) {
					if (D)
						Log.e(TAG,
								"** unable to close() socket during connection failure",
								e2);
				}
				// Start the service over to restart listening mode

				IGBluetoothHelper.this.start();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (IGBluetoothHelper.this) {
				_bConnectThread = null;
			}
			Log.i("TAG", "Connection sucessfull");
			// Start the connected thread
				connected(_bSocket, _bDevice);
		}

		public void cancel() {
			try {
				_bSocket.close();
			} catch (IOException e) {
				if (D)
					Log.e(TAG, "** close() of connect socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket _bSocket;
		private final InputStream _bInStream;
		private final OutputStream _bOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			if (D)
				Log.d(TAG, "** create ConnectedThread");

			_bSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				if (D)
					Log.e(TAG, "** temp sockets not created", e);
			}

			_bInStream = tmpIn;
			_bOutStream = tmpOut;
		}

		public void run() {
			if (D)
				Log.i(TAG, "** BEGIN _bConnectedThread");

			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = _bInStream.read(buffer);
					
					_btReciever.readDataResult(buffer);

					// _dataHandler.processData(buffer, bytes);
				} catch (IOException e) {
					if (D)
						Log.e(TAG, "** disconnected", e);
					connectionLost();
					break;
				}
			}
		}

		
		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				_bOutStream.write(buffer);
				_bOutStream.flush();

				// Share the sent message back to the UI Activity
				_btReciever.writeDataResult(buffer);
			} catch (IOException e) {
				if (D)
					Log.e(TAG, "** Exception during write", e);
			}
		}

		public void cancel() {
			try {
				_bSocket.close();
			} catch (IOException e) {
				if (D)
					Log.e(TAG, "** close() of connect socket failed", e);
			}
		}
	}
	
	
}
