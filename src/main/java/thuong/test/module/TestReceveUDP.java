package thuong.test.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import thuong.process.staticprocess.TTSendPackageSwitch;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.threadpool.IThreadPoolService;

//Test cho topo la 2 host ket noi vs 2 switch lien ket vs nhau
//Topo: smalltopo2
//Replate macadress in host1, host2
//Add OpenFlowMod to send package to controller
//Receve UDP Packaget

//In host terminal: sudo arp -s 10.0.0.3 aa:aa:aa:aa:aa:aa
//In host terminal: javac TestSendUDP.java
//In host terminal: java TestSendUDP

public class TestReceveUDP implements IFloodlightModule, IOFMessageListener {
	static String host1 = "2e:72:2d:77:f1:2b";
	static String host2 = "92:45:3e:87:61:a5";
	static String controllerMac = "aa:aa:aa:aa:aa:aa";
	static MacAddress controllerMacAdress = MacAddress.of(controllerMac);

	private IFloodlightProviderService floodlightProvider;
	protected static Logger log = LoggerFactory.getLogger(TestReceveUDP.class);

	protected SingletonTask upTask;
	IThreadPoolService threadPool;

	IOFSwitch sw1 = null;
	IOFSwitch sw2 = null;

	@Override
	public String getName() {
		return TestReceveUDP.class.getPackage().getName();
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
		log.info("Package to Controller");
		OFPacketIn pkIn = (OFPacketIn) msg;
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
				IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

		MacAddress dstMAC = eth.getDestinationMACAddress();
		
		//Day la co che de kiem soat goi tin host gui len Controller de xu li
		if ((pkIn.getInPort() == OFPort.of(1))
				&& (dstMAC.getLong() == controllerMacAdress.getLong()))
			log.info("SRC: " + msg.toString());
		if (sw.getId().getLong() == 1)
			sw1 = sw;
		if (sw.getId().getLong() == 2)
			sw2 = sw;
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

		ScheduledExecutorService ses = threadPool.getScheduledExecutor();
		upTask = new SingletonTask(ses, new Runnable() {
			@Override
			public void run() {
				sendOpennFlowMod(sw1, host1);
				sendOpennFlowMod(sw2, host2);
				upTask.reschedule(5, TimeUnit.SECONDS);
			}
		});
		upTask.reschedule(2, TimeUnit.SECONDS);

	}

	// Gui goi tin OFMOD den switch
	public void sendOpennFlowMod(IOFSwitch sw, String host) {

		if (sw != null) {
			Match.Builder a = sw.getOFFactory().buildMatch();
			a.setExact(MatchField.ETH_SRC, MacAddress.of(host));
			a.setExact(MatchField.ETH_DST, MacAddress.of(controllerMac));
			a.setExact(MatchField.IN_PORT, OFPort.of(1));
			TTSendPackageSwitch.writeFlowMod(sw, OFFlowModCommand.ADD,
					OFBufferId.NO_BUFFER, a.build(), OFPort.CONTROLLER);
		}
	}

}
