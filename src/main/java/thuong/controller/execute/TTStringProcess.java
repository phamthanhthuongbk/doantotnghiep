package thuong.controller.execute;

import thuong.controller.module.TTSendPacketThread;
import thuong.packet.struct.TTBasePacketCustom;

public class TTStringProcess {
	public static void StringProcess(TTBasePacketCustom packet, TTSendPacketThread sendpacketThread){
		System.out.println("String : " + new String(packet.data));
		System.out.println("\n");
	}
}
