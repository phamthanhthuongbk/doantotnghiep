package thuong.controller.execute;

import java.io.IOException;

import thuong.controller.module.TTSendPacketThread;
import thuong.packet.struct.TTBasePacketCustom;
import thuong.packet.struct.TTPacketInfor;

public class TTInforProcess {
	public static void InforProcess(TTBasePacketCustom packet, TTSendPacketThread sendpacketThread){
		TTPacketInfor packetInfor = new TTPacketInfor();
		try {
			packetInfor.processData(packet.data);

			System.out.print("\n allocatedMemory:   "+ packetInfor.allocatedMemory + "  \n");
			System.out.print("\n freeMemory:        "+ packetInfor.freeMemory + "  \n");
			System.out.print("\n maxMemory:         "+ packetInfor.maxMemory + "  \n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
