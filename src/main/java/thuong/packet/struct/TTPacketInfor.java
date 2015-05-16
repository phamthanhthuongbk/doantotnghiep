package thuong.packet.struct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TTPacketInfor implements TTPacketStruct {


	public long maxMemory;
	public long allocatedMemory;
	public long freeMemory;
	
	@Override
	public byte[] getData() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);
	    dos.writeLong(maxMemory);	
	    dos.writeLong(allocatedMemory);	
	    dos.writeLong(freeMemory);	
	    dos.flush();
	    return bos.toByteArray();
	}

	@Override
	public void processData(byte[] data) throws IOException {
		ByteArrayInputStream bos = new ByteArrayInputStream(data);
	    DataInputStream dos = new DataInputStream(bos);
	    maxMemory = dos.readLong();
	    allocatedMemory = dos.readLong();
	    freeMemory = dos.readLong();
	}

}
