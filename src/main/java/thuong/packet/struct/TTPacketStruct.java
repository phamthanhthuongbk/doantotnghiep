package thuong.packet.struct;

import java.io.IOException;
public interface TTPacketStruct {
	public byte[] getData() throws IOException;
	public void processData(byte[] data) throws IOException;
}
