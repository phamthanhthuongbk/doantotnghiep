package thuong.packet.struct;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import thuong.controller.device.TTHostDevice;


/*
 * PaketType:
 * 	1: Hello. Goi tin ban dau cua Switch gui len controller
 */

public class TTBasePacketCustom {
	//PacketType
	
	public static final int PACKET_TYPE_HELLO = 1;
	public static final int PACKET_TYPE_SEND_STRING = 2;
	public static final int PACKET_TYPE_GET_INFOR = 3;
	public static final int PACKET_TYPE_SEND_INFOR = 4;
	public static final int PACKET_TYPE_STOP_SERVER = 99;
	
	//Device
	public TTHostDevice hostDevice;
	
	//Loai Packet
	public int packetType;
	
	//kich thuoc data packet
	public int dataLeng;
	
	//goi tin data
	public byte[] data;
	public TTBasePacketCustom(){
		
	}
	
	public TTBasePacketCustom(int packetType, byte[] data){
		this.packetType = packetType;
		this.data = data;
		this.dataLeng = data.length;
	}

	public int getLengData(){
		return dataLeng;
	}
	public int getLengHeader(){
		return 8;
	}
	public int getLengPacket(){
		return 8 + dataLeng;
	}

	public byte[] getBytePacketHeader() throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);
	    dos.writeInt(packetType);
	    dos.writeInt(dataLeng);
	    dos.flush();
	    return bos.toByteArray();
	}

	public byte[] getBytePacketData() throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);
	    dos.write(data);
	    dos.flush();
	    return bos.toByteArray();
	}
	
	public byte[] getBytePacket() throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);
	    dos.writeInt(packetType);
	    dos.writeInt(dataLeng);
	    dos.write(data);
	    dos.flush();
	    return bos.toByteArray();
	}

	public void createHeaderFromDataHeader(byte[] headerData ){
		this.packetType = TTConvertStatic.ByteArrayToInt(headerData, 0);
		this.dataLeng = TTConvertStatic.ByteArrayToInt(headerData, 4);
	}
	
	public void createPaketFromDataPacket(byte[] dataPacket ){
		this.packetType = TTConvertStatic.ByteArrayToInt(dataPacket, 0);
		this.dataLeng = TTConvertStatic.ByteArrayToInt(dataPacket, 4);
		this.data = new byte[this.dataLeng];
		System.arraycopy(dataPacket, 8, this.data, 0, this.dataLeng);
	}
}
