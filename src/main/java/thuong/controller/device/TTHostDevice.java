package thuong.controller.device;

import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.types.OFPort;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Ethernet;

public class TTHostDevice {
	public IOFSwitch iOFSwitch = null; 
	public long idSwitch = -1;
	public OFPort portSwitch = OFPort.of(-1);
	public OFPacketIn helloPacketFromHost = null;
	public FloodlightContext helloPacketContext = null;
	public Ethernet eth = null;
}

