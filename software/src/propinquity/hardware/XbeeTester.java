package propinquity.hardware;

import processing.core.*;

import java.util.*;
import java.lang.System;
import com.rapplogic.xbee.api.*;
import com.rapplogic.xbee.api.wpan.*;
import gnu.io.*;

/**
 * Testing the xbee radio problems
 *
 */
public class XbeeTester extends PApplet {

	// Unique serialization ID
	static final long serialVersionUID = 6340508174717159412L;

	XBee base, remote;
	Listener remoteListener;

	int currentFrameID;
	int uniqueID;

	Vector<Integer> lostFrames;

	public void setup() {
		size(1024, 800);

		base = new XBee();
		remote = new XBee();

		try {
			base.open("/dev/tty.usbserial-A8004YWG", 57600);
		} catch(XBeeException e) {
			System.out.println(e.getMessage());
			System.out.println("Failed to connect to base");
		}

		try {
			remote.open("/dev/tty.usbserial-A700eE3z", 38400);
		} catch(XBeeException e) {
			System.out.println(e.getMessage());
			System.out.println("Failed to connect to remote");
		}

		try {
			Thread.sleep(500);
		} catch(Exception e) {

		}

		System.out.println(getNI(base));
		System.out.println(getNI(remote));

		remoteListener = new Listener(remote);

		(new Thread(new Runnable() {

			public void run() {
				while(true) {
					long time = System.currentTimeMillis();
					XBeeAddress16 addr = new XBeeAddress16(((5 & 0xFF00) >> 8), 5 & 0x00FF);
					
					int[] fullPayload = new int[] {uniqueID%256};
					// System.out.println("L"+uniqueID%256);
					uniqueID++;
					
					TxRequest16 request = new TxRequest16(addr, getNextFrameId(), fullPayload);

					// try {
					// 	// send a request and wait up to timeout milliseconds for the response
					// 	TxStatusResponse response = (TxStatusResponse)base.sendSynchronous(request, 1000);

					// 	if (response.isSuccess()) {
					// 		// System.out.println("Sent OK!");
					// 	} else {
					// 		System.out.println("Failed to send");
					// 	}
					// } catch(XBeeTimeoutException e) {
					// 	System.out.println("\t\tTimeout sending request");
					// } catch(XBeeException e) {
					// 	System.out.println("\t\tException sending request");
					// }

					try {
						base.sendAsynchronous(request);
					} catch(XBeeException e) {
						System.out.println("\t\tException sending request");
					}


					try {
						Thread.sleep(3);
					} catch(Exception e) {

					}

					// System.out.println( System.currentTimeMillis() - time);
				}
			}
		})).start();
	}

	public String getNI(XBee xbee) {
		String ni = null;
		XBeeResponse response = null;

		try {
			response = xbee.sendSynchronous(new AtCommand("NI"), 1000);
		} catch(XBeeTimeoutException e) {
			System.out.println("\t\tTimeout getting NI");
			return ni;
		} catch(XBeeException e) {
			System.out.println("\t\tException getting NI");
			return ni;
		}

		if(response != null && response.getApiId() == ApiId.AT_RESPONSE) {
			AtCommandResponse atResponse = (AtCommandResponse)response;
			if(atResponse.isOk()) {
				ni = new String(atResponse.getValue(), 0, atResponse.getValue().length);
			} else {
				System.out.println("\t\tNI Command was not successful");
				return ni;
			}
		} else {
			System.out.println("\t\tNI Response was null or wrong type");
			return ni;
		}

		return ni;
	}

	public void draw() {
		background(0);
	}

	int getNextFrameId() {
		currentFrameID++;
		if(currentFrameID > 255) currentFrameID = 1;
		return currentFrameID;
	}
	
	static public void main(String args[]) {
		PApplet.main(new String[] { "propinquity.hardware.XbeeTester" });
	}

	class Listener implements PacketListener {

		int current = 0;
		int missingIndex = 0;
		int[] missing;

		Listener(XBee xbee) {
			missing = new int[100];
			xbee.addPacketListener(Listener.this);
		}

		public void processResponse(XBeeResponse response) {
			switch(response.getApiId()) {
				case RX_16_RESPONSE: {
					RxResponse16 rx_response = (RxResponse16)response;
					int addr = rx_response.getRemoteAddress().get16BitValue();
					int[] data = rx_response.getData();

					if(data.length != 1) {
						System.out.println("Faulty packet");
					} else {
						if(data[0] > current+1) {
							System.out.println("skiped " + current + " - " + data[0]);
						} else {
							// System.out.println("R"+data[0]);
						}
						current = data[0];
					}
					break;
				}
			}	
		}

	}

}
