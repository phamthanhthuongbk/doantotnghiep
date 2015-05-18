package thuong.controller.execute;

import java.io.IOException;

import thuong.controller.device.TTDeviceManager;
import thuong.controller.module.TTSendPacketThread;
import thuong.controller.ui.UIController;
import thuong.packet.struct.TTBasePacketCustom;
import thuong.packet.struct.TTPacketInfor;

public class TTInforProcess {
	public static void InforProcess(TTBasePacketCustom packet, TTSendPacketThread sendpacketThread, UIController uiController){
		TTPacketInfor packetInfor = new TTPacketInfor();
		try {
			packetInfor.processData(packet.data);
			
			int index = TTDeviceManager.GetDeviceID(packet.hostDevice) + 1;
			String logtex = "Host: " + index + "  vua gui thong tin Ram"
							+ "\n allocatedMemory:   "+ packetInfor.allocatedMemory + "  "
							+ "\n freeMemory:        "+ packetInfor.freeMemory + "  " 
							+ "\n maxMemory:         "+ packetInfor.maxMemory + "  ";
			uiController.textLog.setText(logtex);

//			System.out.print("\n allocatedMemory:   "+ packetInfor.allocatedMemory + "  \n");
//			System.out.print("\n freeMemory:        "+ packetInfor.freeMemory + "  \n");
//			System.out.print("\n maxMemory:         "+ packetInfor.maxMemory + "  \n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
