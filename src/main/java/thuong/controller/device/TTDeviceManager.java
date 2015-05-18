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

	//Tim kiem device tuong tu
	public static TTHostDevice GetDevice(TTHostDevice hostDevice){
		for(int i = 0; i< listDevice.size(); i++){
			TTHostDevice tempDevice = listDevice.get(i);
			if((tempDevice.idSwitch == hostDevice.idSwitch)&&(tempDevice.portSwitch.getPortNumber() == hostDevice.portSwitch.getPortNumber()))
				return tempDevice;
		}
		return null;
	}
	
	//Tim kiem device tuong tu
	public static int GetDeviceID(TTHostDevice hostDevice){
		for(int i = 0; i< listDevice.size(); i++){
			TTHostDevice tempDevice = listDevice.get(i);
			if((tempDevice.idSwitch == hostDevice.idSwitch)&&(tempDevice.portSwitch.getPortNumber() == hostDevice.portSwitch.getPortNumber()))
				return i;
		}
		return -1;
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
