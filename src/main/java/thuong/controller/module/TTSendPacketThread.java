package thuong.controller.module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.UDP;

import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.python.modules.synchronize;

import com.fasterxml.jackson.databind.node.TreeTraversingParser;

import thuong.controller.device.TTHostDevice;
import thuong.packet.struct.TTBasePacketCustom;

class TTNode{
	TTBasePacketCustom packetNode;
	TTHostDevice targetNode;
	public TTNode(TTBasePacketCustom packet, TTHostDevice target){
		this.packetNode = packet;
		this.targetNode = target;
	}
}

public class TTSendPacketThread extends Thread {

	private final Queue<TTNode> queue; 
	
	public TTSendPacketThread(){

		queue = new LinkedList<TTNode>();
	} 

	public void addQueue(TTBasePacketCustom packet, TTHostDevice target) {
		synchronized (queue) {
			// Add work to the queue
			queue.add(new TTNode(packet, target));

			// Notify the monitor object that all the threads
			// are waiting on. This will awaken just one to
			// begin processing work from the queue
			queue.notify();
		}

	}
	

	@Override
	public void run() {
		while (true) {
			try {
				synchronized (queue) {
					while (queue.isEmpty())
						queue.wait();
					TTNode node = queue.remove();
					TTBasePacketCustom packet = node.packetNode;
					TTHostDevice target = node.targetNode;
					sendUDPToHost(target.iOFSwitch, target.helloPacketFromHost, target.helloPacketContext,target.eth, packet.getBytePacketHeader());
					sendUDPToHost(target.iOFSwitch, target.helloPacketFromHost, target.helloPacketContext,target.eth, packet.getBytePacketData());
		
				}

			} catch (InterruptedException ie) {
//				ThreadLog("Error Thread");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}

	public void sendUDPToHost(IOFSwitch sw, OFPacketIn pi,
			FloodlightContext cntx, Ethernet eth, byte[] data) {
//		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
//				IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

		IPv4 ip = (IPv4) eth.getPayload();

		UDP udpHost = (UDP) ip.getPayload();

		IPacket udpController = new Ethernet()
				.setSourceMACAddress(
						TTCreateFlowConnectHost.controllerMacAdress.getBytes())
				.setDestinationMACAddress(eth.getSourceMACAddress())
				.setEtherType(EthType.of(Ethernet.TYPE_IPv4))
				.setVlanID(eth.getVlanID())
				.setPriorityCode(eth.getPriorityCode())
				.setPayload(
						new IPv4()
								// .setChecksum(ip.getChecksum())
								.setDestinationAddress(ip.getSourceAddress())
								.setSourceAddress(ip.getDestinationAddress())
								.setProtocol(IpProtocol.UDP)
								.setDiffServ(ip.getDiffServ())
								.setFlags(ip.getFlags())
								.setFragmentOffset(ip.getFragmentOffset())
								.setIdentification(ip.getIdentification())
								.setOptions(ip.getOptions())
								.setTtl(ip.getTtl())
								.setVersion(ip.getVersion())
								.setPayload(

										new UDP()
												// .setChecksum(
												// udpHost.getChecksum())
												.setDestinationPort(
														udpHost.getSourcePort())
												.setSourcePort(
														udpHost.getDestinationPort())
												.setPayload(
														new Data()
																.setData(data)))

				);

		pushPacket(udpController, sw, OFBufferId.NO_BUFFER, OFPort.ANY, (pi
				.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort()
				: pi.getMatch().get(MatchField.IN_PORT)), cntx, true);
	}

	/**
	 * used to push any packet - borrowed routine from Forwarding
	 * 
	 * @param OFPacketIn
	 *            pi
	 * @param IOFSwitch
	 *            sw
	 * @param int bufferId
	 * @param short inPort
	 * @param short outPort
	 * @param FloodlightContext
	 *            cntx
	 * @param boolean flush
	 */
	public void pushPacket(IPacket packet, IOFSwitch sw, OFBufferId bufferId,
			OFPort inPort, OFPort outPort, FloodlightContext cntx, boolean flush) {
		// if (log.isTraceEnabled()) {
		// log.trace("PacketOut srcSwitch={} inPort={} outPort={}",
		// new Object[] { sw, inPort, outPort });
		// }

		OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();

		// set actions
		List<OFAction> actions = new ArrayList<OFAction>();
		actions.add(sw.getOFFactory().actions().buildOutput().setPort(outPort)
				.setMaxLen(Integer.MAX_VALUE).build());

		pob.setActions(actions);

		// set buffer_id, in_port
		pob.setBufferId(bufferId);
		pob.setInPort(inPort);

		// set data - only if buffer_id == -1
		if (pob.getBufferId() == OFBufferId.NO_BUFFER) {
			if (packet == null) {
//				log.error("BufferId is not set and packet data is null. "
//						+ "Cannot send packetOut. "
//						+ "srcSwitch={} inPort={} outPort={}", new Object[] {
//						sw, inPort, outPort });
				return;
			}
			byte[] packetData = packet.serialize();
			pob.setData(packetData);
		}

//		log.info("Send UDP");

		sw.write(pob.build());
	}
	
	public void setLogEnable(boolean logEnable) {
		this.logEnable = logEnable;
	}

	private boolean logEnable = true;
	public void ThreadLog(String log) {
		if (logEnable)
			System.out.print("\nSendUDPThread: " + log);
	}

}
