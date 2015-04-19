package thuong.test.module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import thuong.controller.device.TTHostDevice;
import thuong.controller.module.TTCreateFlowConnectHost;
import thuong.controller.module.TTSendPacketThread;
import thuong.packet.struct.TTBasePacketCustom;
import thuong.process.staticprocess.TTSendPackageSwitch;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.threadpool.IThreadPoolService;

//Test cho topo la 2 host ket noi vs 2 switch lien ket vs nhau
//Topo: smalltopo2. sudo mn --custom mininet/custom/smalltopo2.py --topo btl1 --controller=remote,ip=127.0.0.1,port=6653 --switch ovsk,protocols=OpenFlow13
//Add OpenFlowMod to send package to controller
//Receve UDP Packaget, and sendUDP file
//Get data from UDP
//Send data ton host by UDP 

//In code:
//Replate macadress in host1, host2
//In host terminal: sudo arp -s 10.0.0.3 aa:aa:aa:aa:aa:aa
//In host terminal: javac TestSendUDP.java
//In host terminal: java TestSendUDP
//version 2 no need add arp

public class TestSendUDP implements IFloodlightModule, IOFMessageListener {

	private IFloodlightProviderService floodlightProvider;
	protected static Logger log = LoggerFactory.getLogger(TestSendUDP.class);

	IThreadPoolService threadPool;

	//test PacketSendThread
	TTSendPacketThread sendpcketThread;
	
	@Override
	public String getName() {
		return TestSendUDP.class.getPackage().getName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		OFPacketIn pkIn = (OFPacketIn) msg;
		log.info("Package to Controller: ");
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
				IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

		MacAddress dstMAC = eth.getDestinationMACAddress();

		// Day la co che de kiem soat goi tin host gui len Controller de xu li
		if ((pkIn.getInPort() == OFPort.of(1))
				&& (dstMAC.getLong() == TTCreateFlowConnectHost.controllerMacAdress.getLong())) {
			log.info("SRC: " + msg.toString());
			byte[] data = getDataFromUDPPacket(eth);
			
			TTBasePacketCustom packet = new TTBasePacketCustom();
			packet.createPaketFromDataPacket(data);
			
			log.info("Data " + new String(packet.data));
			log.info("Data leng " + packet.dataLeng);

//			TTSwitchDevice switchDevice = new TTSwitchDevice();
//			switchDevice.helloPacketContext = cntx;
//			switchDevice.helloPacketFromHost = pkIn;
//			switchDevice.iOFSwitch = sw;
//			
//			sendpcketThread.addQueue(new TTBasePacketCustom(2, "hhh".getBytes()), switchDevice);
			
			//send packet toserve
			TTBasePacketCustom basePacketCustom = new TTBasePacketCustom(1, "nam thaodddd".getBytes());
			try {
				sendUDPToHost(sw, pkIn, cntx, basePacketCustom.getBytePacketHeader());
				sendUDPToHost(sw, pkIn, cntx, basePacketCustom.getBytePacketData());
				sendUDPToHost(sw, pkIn, cntx, basePacketCustom.getBytePacketHeader());
				sendUDPToHost(sw, pkIn, cntx, basePacketCustom.getBytePacketData());
				log.info("Send nguoc thanh cmn cong: " + basePacketCustom.getBytePacketData().length);
			} catch (IOException e) {
				log.info("Eo thanh cmn cong");
			}
			
			
		}
		return Command.CONTINUE;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IThreadPoolService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = context
				.getServiceImpl(IFloodlightProviderService.class);
		threadPool = context.getServiceImpl(IThreadPoolService.class);

	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		log.info("STARTTTTTTTTTTTTTTTTT");

		sendpcketThread = new TTSendPacketThread();
		sendpcketThread.start();
		
	}

	// Gui goi tin OFMOD den switch
	public void sendOpennFlowMod(IOFSwitch sw, String host) {

		if (sw != null) {
			Match.Builder a = sw.getOFFactory().buildMatch();
			a.setExact(MatchField.ETH_SRC, MacAddress.of(host));
			a.setExact(MatchField.ETH_DST, MacAddress.of(TTCreateFlowConnectHost.controllerMac));
			a.setExact(MatchField.IN_PORT, OFPort.of(1));
			TTSendPackageSwitch.writeFlowMod(sw, OFFlowModCommand.ADD,
					OFBufferId.NO_BUFFER, a.build(), OFPort.CONTROLLER);
		}
	}

	// Ham nay dung de lay ra Data cua goi tin UDP

	public byte[] getDataFromUDPPacket(Ethernet eth) {
		// log.info("TYPE: " + eth.getEtherType());
		IPv4 ip = (IPv4) eth.getPayload();
		if (ip.getProtocol().equals(IpProtocol.UDP)) {
			// log.info("PROTOCOL: UDP");
			UDP udp = (UDP) ip.getPayload();
			Data data = (Data) udp.getPayload();
			// log.info("PACKET: " + new String(data.getData()));
			return data.getData();
		}
		return null;
	}

	public void processUDPPacKet(IOFSwitch sw, OFPacketIn pkIn,
			FloodlightContext cntx) {
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
				IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		VlanVid vlan = VlanVid.ofVlan(eth.getVlanID());
		MacAddress srcMac = eth.getSourceMACAddress();
		MacAddress dstMac = eth.getDestinationMACAddress();
		OFPort inPort = OFPort.of(1);
		// A retentive builder will remember all MatchFields of the parent the
		// builder was generated from
		// With a normal builder, all parent MatchFields will be lost if any
		// MatchFields are added, mod, del
		// TODO (This is a bug in Loxigen and the retentive builder is a
		// workaround.)
		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IN_PORT, inPort)
				.setExact(MatchField.ETH_SRC, srcMac)
				.setExact(MatchField.ETH_DST, dstMac);
		if (!vlan.equals(VlanVid.ZERO)) {
			mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan));
		}
		// TODO Detect switch type and match to create hardware-implemented flow
		// TODO Set option in config file to support specific or MAC-only
		// matches
		if (eth.getEtherType().getValue() == Ethernet.TYPE_IPv4) {
			IPv4 ip = (IPv4) eth.getPayload();
			IPv4Address srcIp = ip.getSourceAddress();
			IPv4Address dstIp = ip.getDestinationAddress();
			mb.setExact(MatchField.IPV4_SRC, srcIp)
					.setExact(MatchField.IPV4_DST, dstIp)
					.setExact(MatchField.ETH_TYPE, EthType.IPv4);
			if (ip.getProtocol().equals(IpProtocol.TCP)) {
				TCP tcp = (TCP) ip.getPayload();
				mb.setExact(MatchField.IP_PROTO, IpProtocol.TCP)
						.setExact(MatchField.TCP_SRC, tcp.getSourcePort())
						.setExact(MatchField.TCP_DST, tcp.getDestinationPort());
			} else if (ip.getProtocol().equals(IpProtocol.UDP)) {
				UDP udp = (UDP) ip.getPayload();
				mb.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
						.setExact(MatchField.UDP_SRC, udp.getSourcePort())
						.setExact(MatchField.UDP_DST, udp.getDestinationPort());
			}
		} else if (eth.getEtherType().getValue() == Ethernet.TYPE_ARP) {
			mb.setExact(MatchField.ETH_TYPE, EthType.ARP);
		}
	}

	public void sendUDPToHost(IOFSwitch sw, OFPacketIn pi,
			FloodlightContext cntx, byte[] data) {
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
				IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

		IPv4 ip = (IPv4) eth.getPayload();

		UDP udpHost = (UDP) ip.getPayload();

		IPacket udpController = new Ethernet()
				.setSourceMACAddress(TTCreateFlowConnectHost.controllerMacAdress.getBytes())
				.setDestinationMACAddress(eth.getSourceMACAddress())
				.setEtherType(EthType.of(Ethernet.TYPE_IPv4))
				.setVlanID(eth.getVlanID())
				.setPriorityCode(eth.getPriorityCode())
				.setPayload(
						new IPv4()
//								.setChecksum(ip.getChecksum())
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
//												.setChecksum(
//														udpHost.getChecksum())
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
		if (log.isTraceEnabled()) {
			log.trace("PacketOut srcSwitch={} inPort={} outPort={}",
					new Object[] { sw, inPort, outPort });
		}

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
				log.error("BufferId is not set and packet data is null. "
						+ "Cannot send packetOut. "
						+ "srcSwitch={} inPort={} outPort={}", new Object[] {
						sw, inPort, outPort });
				return;
			}
			byte[] packetData = packet.serialize();
			pob.setData(packetData);
		}

		log.info("Send UDP");

		sw.write(pob.build());
	}

}
