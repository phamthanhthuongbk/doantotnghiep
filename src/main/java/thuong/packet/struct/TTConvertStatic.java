package thuong.packet.struct;

public class TTConvertStatic {
	public static int ByteArrayToInt(byte[] b, int ofset) 
	{
	    int value = 0;
	    for (int i = ofset; i < ofset + 4; i++) {
	    	int k = i - ofset;
	        int shift = (4 - 1 - k) * 8;
	        value += (b[i] & 0x000000FF) << shift;
	    }
	    return value;
	}
}
