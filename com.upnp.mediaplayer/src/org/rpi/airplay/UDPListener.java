package org.rpi.airplay;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.apache.log4j.Logger;

/**
 * Listen on a given socket
 * 
 * @author bencall
 * 
 */
public class UDPListener extends Thread {

	private Logger log = Logger.getLogger(this.getClass());
	// Constantes
	public static final int MAX_PACKET = 2048;

	// Variables d'instances
	private DatagramSocket socket;
	private boolean stopThread = false;
	private AudioEventQueue queue = null;
	private Thread threadMessageQueue = null;

	public UDPListener(DatagramSocket socket) {
		super();
		this.socket = socket;
		queue = new AudioEventQueue();
		threadMessageQueue = new Thread(queue, "AudioEventQueue");
		threadMessageQueue.start();
		this.start();
	}

	public void run() {
		boolean fin = stopThread;
		while (!fin) {
			byte[] buffer = new byte[MAX_PACKET];
			DatagramPacket p = new DatagramPacket(buffer, buffer.length);
			try {
				synchronized (socket) {
					if (socket != null) {
						socket.receive(p);
						if (queue != null) {
							queue.put(p);
						}
					}
				}
			} catch (Exception e) {
				log.error("Error in Run", e);
			}

			// Stop
			synchronized (this) {
				fin = this.stopThread;
			}
		}
	}

	public synchronized void stopThread() {

		try {
			if (queue != null) {
				queue.stop();
				queue = null;
			}
		} catch (Exception e) {
			log.error("Error Stopping AudioEventQueue", e);
		}

		try {
			if (threadMessageQueue != null) {
				threadMessageQueue = null;
			}
			threadMessageQueue = null;
		} catch (Exception e) {
			log.error("Error Stopping Thread", e);
		}
		this.stopThread = true;
	}

	public void flush() {
		queue.clear();
	}
}