package thuong.controller.device;

import java.util.ArrayList;

public class TTDeviceManager {
	public static ArrayList<TTHostDevice> listDevice = new ArrayList<>();
	
	
	// Them mot new device
	public static void AddNewDevice(TTHostDevice newDevice){
		for(int i = 0; i< listDevice.size(); i++){
			TTHostDevice tempDevice = listDevice.get(i);
			if((tempDevice.idSwitch == newDevice.idSwitch)&&(tempDevice.portSwitch.getPortNumber() == newDevice.portSwitch.getPortNumber()))
				return;
		}
		listDevice.add(newDevice);
		System.out.println("Number Of Device: " + listDevice.size());
		LogAllDevice();
	}
	
	//Log device
	public static void LogAllDevice(){
		for(int i =0; i< listDevice.size(); i++){
			TTHostDevice tempDevice = listDevice.get(i);
			System.out.println("\nDevice :  " + i);
			System.out.println("IDSwitch: " + tempDevice.idSwitch);
			System.out.println("IDPort:   " + tempDevice.portSwitch.getPortNumber());
		}
	}
	
	//Lay ra mot device
	public static TTHostDevice GetDevice(long idSwitch){
		for(int i = 0; i< listDevice.size(); i++){
			TTHostDevice tempDevice = listDevice.get(i);
			if(tempDevice.idSwitch == idSwitch)
				return tempDevice;
		}
		return null;	
	}
}
