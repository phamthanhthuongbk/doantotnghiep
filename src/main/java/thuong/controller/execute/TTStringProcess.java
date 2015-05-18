package thuong.controller.execute;

import thuong.controller.device.TTDeviceManager;
import thuong.controller.module.TTSendPacketThread;
import thuong.controller.ui.UIController;
import thuong.packet.struct.TTBasePacketCustom;

public class TTStringProcess {
	public static void StringProcess(TTBasePacketCustom packet, TTSendPacketThread sendpacketThread, UIController uiController){
		
		int index = TTDeviceManager.GetDeviceID(packet.hostDevice) + 1;
		String logtex = "Host: " + index + "  vua gui doan string: \n"
						+ new String(packet.data);
		uiController.textLog.setText(logtex);
	}
}
