package thuong.controller.module;

import java.util.LinkedList;
import java.util.Queue;

import thuong.controller.device.TTDeviceManager;
import thuong.controller.device.TTHostDevice;
import thuong.controller.execute.TTInforProcess;
import thuong.controller.execute.TTStringProcess;
import thuong.controller.ui.UIController;
import thuong.packet.struct.TTBasePacketCustom;
import thuong.packet.struct.TTPacketInfor;

public class TTProcessPacketThread extends Thread{

	public TTSendPacketThread sendpacketThread;
	public UIController uiController;
	
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
				//UI test
				uiController = new UIController(sendpacketThread);
				uiController.setVisible(true);

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
//		ThreadLog("Data Type: " + packet.packetType);
//		ThreadLog("Data Leng: " + packet.dataLeng);
//		ThreadLog("Data : " + new String(packet.data));
//		ThreadLog("\n");

//		uiController.textLog.setText(log);
			
		switch (packet.packetType) {
		case TTBasePacketCustom.PACKET_TYPE_HELLO:
			//Nhan dc goi tin hello
			uiController.newHostConnect();
			break;
		case TTBasePacketCustom.PACKET_TYPE_SEND_STRING:
			//Nhan dc string tu host
			TTStringProcess.StringProcess(packet, sendpacketThread);
			break;
		case TTBasePacketCustom.PACKET_TYPE_SEND_INFOR:
			//Nhan duoc thong tin cau hinh ve host
			TTInforProcess.InforProcess(packet, sendpacketThread);
			break;
		case TTBasePacketCustom.PACKET_TYPE_STOP_SERVER:
			break;
		default:
			break;
		}
	}
	
}
