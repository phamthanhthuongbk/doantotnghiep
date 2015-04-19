package thuong.controller.module;

import java.util.LinkedList;
import java.util.Queue;

import thuong.controller.device.TTDeviceManager;
import thuong.controller.device.TTHostDevice;
import thuong.packet.struct.TTBasePacketCustom;

public class TTProcessPacketThread extends Thread{

	public TTSendPacketThread sendpacketThread;
	
	private int numberPacket = 0;
	
	private final Queue<TTBasePacketCustom> queue ; 
	
	public TTProcessPacketThread(){
		ThreadLog("Khoi tao thread Process");
		queue = new LinkedList<TTBasePacketCustom>();
	}
	
	public void addQueue(TTBasePacketCustom packet) {
//		System.out.print("\nHam add queue thuc thi");
		synchronized (queue) {
			// Add work to the queue
			queue.add(packet);

			// Notify the monitor object that all the threads
			// are waiting on. This will awaken just one to
			// begin processing work from the queue
			queue.notify();
		}

	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Runnable work = null;

				synchronized (queue) {
					while (queue.isEmpty())
						queue.wait();

					// Get the next work item off of the queue
					TTBasePacketCustom packet = queue.remove();

					//Day la Doan de chen codexuli
					processPacket(packet);
					
					
//					System.out.print("\nHam gui thuc thi");
				}

				// Process the work item
			} catch (InterruptedException ie) {
				ThreadLog("Error Thread");
			} 
		}
	}
	public void setLogEnable(boolean logEnable) {
		this.logEnable = logEnable;
	}

	private boolean logEnable = true;
	public void ThreadLog(String log) {
		if (logEnable)
			System.out.print("\nProcessThread: " + log);
	}

	
	private void processPacket(TTBasePacketCustom packet){
		numberPacket++;
		ThreadLog("Goi tin thu: " + numberPacket);
		ThreadLog("Data Type: " + packet.packetType);
		ThreadLog("Data Leng: " + packet.dataLeng);
		ThreadLog("Data : " + new String(packet.data));
		ThreadLog("\n");

		for(int i = 0; i< TTDeviceManager.listDevice.size(); i++){
			TTHostDevice hostDevice = TTDeviceManager.listDevice.get(i);
			sendpacketThread.addQueue(new TTBasePacketCustom(TTBasePacketCustom.PACKET_TYPE_SEND_STRING, ("Gia tri i:  " + i + "  " +"Switch: " + hostDevice.idSwitch + "  Port" + hostDevice.portSwitch.toString()).getBytes()), TTDeviceManager.listDevice.get(i));
//			sendpacketThread.addQueue(new TTBasePacketCustom(2, ("Gia tri i2: " + i + "  " +"Switch: " + hostDevice.idSwitch + "  Port" + hostDevice.portSwitch.toString()).getBytes()), TTDeviceManager.listDevice.get(i));
//			sendpacketThread.addQueue(new TTBasePacketCustom(2, ("Gia tri i3: " + i + "  " +"Switch: " + hostDevice.idSwitch + "  Port" + hostDevice.portSwitch.toString()).getBytes()), TTDeviceManager.listDevice.get(i));
		}
		
	}
	
}
