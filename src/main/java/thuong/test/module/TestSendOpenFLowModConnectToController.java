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
import org.projectfloodlight.openflow.types.DatapathId;
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
import net.floodlightcontroller.core.IListener.Command;
import net.floodlightcontroller.core.OFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.hub.Hub;
import net.floodlightcontroller.threadpool.IThreadPoolService;

//Test cho topo la 2 host ket noi vs 2 switch lien ket vs nhau
//topo: smalltopo2
//replate macadress in host1, host2
//add openflow to send package to controller

public class TestSendOpenFLowModConnectToController implements
		IFloodlightModule, IOFMessageListener {

	static String host1 = "c2:4b:23:35:51:1c";
	static String host2 = "22:e8:4b:54:88:03";

	private IFloodlightProviderService floodlightProvider;
	protected static Logger log = LoggerFactory
			.getLogger(TestSendOpenFLowMod.class);

	protected SingletonTask upTask;
	IThreadPoolService threadPool;

	boolean stateW1 = false;
	boolean stateW2 = false;

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// We don't provide any services, return null
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// We don't provide any services, return null
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
	public void startUp(FloodlightModuleContext context) {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		log.info("STARTTTTTTTTTTTTTTTTT");

		ScheduledExecutorService ses = threadPool.getScheduledExecutor();
		upTask = new SingletonTask(ses, new Runnable() {
			@Override
			public void run() {
				upTask.reschedule(5, TimeUnit.SECONDS);
			}
		});
		upTask.reschedule(5, TimeUnit.SECONDS);
	}

	@Override
	public String getName() {
		return TestSendOpenFLowMod.class.getPackage().getName();
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

		OFPacketIn packetin = (OFPacketIn) msg;

		log.info("Packet " + packetin.getVersion());
		log.info("Sw " + sw.getId().toString());
		log.info("Sw " + sw.getId().getLong());
		
		if (stateW1) {
			log.info("GOi tin duoc gui chu dong len serve ti sw1");
		}
		if (stateW2) {
			log.info("GOi tin duoc gui chu dong len serve ti sw2");
		}
//		if (stateW1 && stateW2)
//			return Command.CONTINUE;

		// log.info("Packet " + packetin.getMatch());
		if (packetin.getInPort() == OFPort.of(1)) {
			log.info("port 1");
			// if (sw.getId() == DatapathId.of("00:00:00:00:00:00:00:01")) {
			if (sw.getId().getLong() == 1) {

				// Tao OFlow and send to 2 switch
				log.info("sw 1");

				Match.Builder a = sw.getOFFactory().buildMatch();
				a.setExact(MatchField.ETH_SRC, MacAddress.of(host1));
				a.setExact(MatchField.IN_PORT, OFPort.of(1));
				TTSendPackageSwitch.writeFlowMod(sw, OFFlowModCommand.ADD,
						OFBufferId.NO_BUFFER, a.build(), OFPort.CONTROLLER);
				// TTSendPackageSwitch.writePacketOutForPacketIn(sw, packetin,
				// OFPort.of(2));
//				TTSendPackageSwitch.writePacketOutForPacketIn(sw, packetin,
//						OFPort.CONTROLLER);

				stateW1 = true;
			}

			// if (sw.getId() == DatapathId.of("00:00:00:00:00:00:00:02")) {
			if (sw.getId().getLong() == 2) {
				// Tao OFlow and send to 2 switch
				log.info("sw 1");

				Match.Builder a = sw.getOFFactory().buildMatch();
				a.setExact(MatchField.ETH_SRC, MacAddress.of(host2));
				a.setExact(MatchField.IN_PORT, OFPort.of(1));
				TTSendPackageSwitch.writeFlowMod(sw, OFFlowModCommand.ADD,
						OFBufferId.NO_BUFFER, a.build(), OFPort.CONTROLLER);
				// TTSendPackageSwitch.writePacketOutForPacketIn(sw, packetin,
				// OFPort.of(2));
//				TTSendPackageSwitch.writePacketOutForPacketIn(sw, packetin,
//						OFPort.CONTROLLER);

				stateW2 = true;
			}

		}

		return Command.CONTINUE;
	}

	public void senOfMod() {

	}
}
