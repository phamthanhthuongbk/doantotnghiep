package thuong.controller.ui;

import thuong.controller.module.TTSendPacketThread;

public class TTUIThread extends Thread {
	public TTSendPacketThread sendPacketThread;
	public TTUIThread(TTSendPacketThread sendPacketThread){
		this.sendPacketThread = sendPacketThread;
	}
	
	public UIController uiController;
	@Override
	public void run() {
		super.run();
		uiController = new UIController(sendPacketThread);
		uiController.setVisible(true);
	}

}
