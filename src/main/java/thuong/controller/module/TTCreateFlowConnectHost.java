package thuong.controller.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.threadpool.IThreadPoolService;

import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import thuong.process.staticprocess.TTSendPackageSwitch;
import thuong.test.module.TestSendUDP;

//Test cho topo la 2 host ket noi vs 2 switch lien ket vs nhau
//Topo: smalltopo2. sudo mn --custom mininet/custom/smalltopo2.py --topo btl1 --controller=remote,ip=127.0.0.1,port=6653 --switch ovsk,protocols=OpenFlow13
//Add OpenFlowMod for switch to host send package to controller

//New vervion 
//permision all topo 1 switch - 1 host

public class TTCreateFlowConnectHost implements IFloodlightModule, IOFMessageListener{
//	static String host1 = "1e:5b:25:24:cd:77";
//	static String host2 = "c2:a3:46:88:30:35";
	
	//no chage infomation
	public static String controllerMac = "aa:aa:aa:aa:aa:aa";
	public static MacAddress controllerMacAdress = MacAddress.of(controllerMac);

	private IFloodlightProviderService floodlightProvider;
	protected static Logger log = LoggerFactory.getLogger(TestSendUDP.class);

	protected SingletonTask upTask, upTask2;
	IThreadPoolService threadPool;

//	IOFSwitch sw1 = null;
//	IOFSwitch sw2 = null;

	@Override
	public String getName() {
		return TTCreateFlowConnectHost.class.getPackage().getName();
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
//		if (sw.getId().getLong() == 1)
//			sw1 = sw;
//		if (sw.getId().getLong() == 2)
//			sw2 = sw;
		sendOpennFlowMod(sw);
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

//		ScheduledExecutorService ses = threadPool.getScheduledExecutor();
//		upTask = new SingletonTask(ses, new Runnable() {
//			@Override
//			public void run() {
////				sendOpennFlowMod(sw1, host1);
////				sendOpennFlowMod(sw2, host2);
////				sendOpennFlowMod(sw1, host1);
////				sendOpennFlowMod(sw2, host2);
//				
//				upTask.reschedule(5, TimeUnit.SECONDS);
//			}
//		});
//		upTask.reschedule(2, TimeUnit.SECONDS);
		

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
	
	// Gui goi tin OFMOD den switch
	public void sendOpennFlowMod(IOFSwitch sw) {

		if (sw != null) {
			Match.Builder a = sw.getOFFactory().buildMatch();
			a.setExact(MatchField.ETH_DST, MacAddress.of(controllerMac));
//			a.setExact(MatchField.IPV4_DST, IPv4Address.of("10.0.0.99"));
//			a.setExact(MatchField.UDP_DST, TransportPort.of(90));
//			a.setExact(MatchField.IN_PORT, OFPort.of(1));
			
			
			TTSendPackageSwitch.writeFlowMod(sw, OFFlowModCommand.ADD,
					OFBufferId.NO_BUFFER, a.build(), OFPort.CONTROLLER);
		}
	}
}
