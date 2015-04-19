package thuong.test.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import net.floodlightcontroller.threadpool.IThreadPoolService;

import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import thuong.controller.module.TTCreateFlowConnectHost;
import thuong.process.staticprocess.TTSendPackageSwitch;

/*
 * Delete all flow in swith each 2s when 10s controller start
 */

public class TestDeleteAllFlowInSwitch implements IFloodlightModule, IOFMessageListener{

	
	//no chage infomation
	public static String controllerMac = "aa:aa:aa:aa:aa:aa";
	public static MacAddress controllerMacAdress = MacAddress.of(controllerMac);

	private IFloodlightProviderService floodlightProvider;
	protected static Logger log = LoggerFactory.getLogger(TestDeleteAllFlowInSwitch.class);

	protected SingletonTask upTask, upTask2;
	IThreadPoolService threadPool;

	ArrayList<IOFSwitch> listSwitch =  new ArrayList<IOFSwitch>();

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
		log.info("Nhan nhe");
		for(int i =0; i< listSwitch.size(); i++){
			if(listSwitch.get(i)==sw)
				return Command.CONTINUE;
		}
		listSwitch.add(sw);
		log.info("lisd num: " + listSwitch.size());
		
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
				log.info("Run Thread");
				for(int i = 0; i < listSwitch.size(); i++){
					sendOpennFlowMod(listSwitch.get(i));
				}
				
				upTask.reschedule(2, TimeUnit.SECONDS);
			}
		});
		upTask.reschedule(10, TimeUnit.SECONDS);
	}
	
	// Gui goi tin OFMOD den switch
	public void sendOpennFlowMod(IOFSwitch sw) {

		log.info("Xoa FLow");
		
		if (sw != null) {
			Match.Builder a = sw.getOFFactory().buildMatch();
//			a.setExact(MatchField.ETH_DST, MacAddress.of(controllerMac));
//			a.setExact(MatchField.IN_PORT, OFPort.of(1));
			TTSendPackageSwitch.writeFlowMod(sw, OFFlowModCommand.DELETE,
					OFBufferId.NO_BUFFER, a.build(), OFPort.CONTROLLER);
		}
	}
}
