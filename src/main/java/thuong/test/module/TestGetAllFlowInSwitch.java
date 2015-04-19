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
import net.floodlightcontroller.core.IListener.Command;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.core.web.StatsReply;
import net.floodlightcontroller.threadpool.IThreadPoolService;

import org.projectfloodlight.openflow.protocol.OFBarrierRequest;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFFlowStatsEntry;
import org.projectfloodlight.openflow.protocol.OFFlowStatsReply;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.match.Match;
//import org.projectfloodlight.openflow.protocol.ver10.OFFlowStatsReplyVer10;

import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;

import thuong.controller.module.TTCreateFlowConnectHost;

/*
 * 
 *Get All Flow And Log in Screen  
 *Function processStastRequest() 
 * 
 * 
 */

public class TestGetAllFlowInSwitch implements IFloodlightModule,
		IOFMessageListener {

	private IFloodlightProviderService floodlightProvider;
	protected static Logger log = LoggerFactory
			.getLogger(TestGetAllFlowInSwitch.class);

	protected SingletonTask upTask, upTask2;
	IThreadPoolService threadPool;

	ArrayList<IOFSwitch> listSwitch = new ArrayList<IOFSwitch>();

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

		log.info("" + msg.getType());

		if (msg.getType() == OFType.STATS_REPLY)
			log.info("Co goi tin STATS_REPLY");
		if (msg.getType() == OFType.STATS_REQUEST)
			log.info("Co goi tin STATS_REQUEST");

		log.info("Nhan nhe");
		for (int i = 0; i < listSwitch.size(); i++) {
			if (listSwitch.get(i) == sw)
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
		// floodlightProvider.addOFMessageListener(OFType.BARRIER_REPLY, this);
		floodlightProvider.addOFMessageListener(OFType.STATS_REPLY, this);
		floodlightProvider.addOFMessageListener(OFType.STATS_REQUEST, this);
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		floodlightProvider.addOFMessageListener(OFType.HELLO, this);
		log.info("STARTTTTTTTTTTTTTTTTT");

		ScheduledExecutorService ses = threadPool.getScheduledExecutor();
		upTask = new SingletonTask(ses, new Runnable() {
			@Override
			public void run() {
				log.info("Run Thread");
				for (int i = 0; i < listSwitch.size(); i++) {
					processStastRequest(listSwitch.get(i));
				}

				upTask.reschedule(2, TimeUnit.SECONDS);
			}
		});
		upTask.reschedule(10, TimeUnit.SECONDS);
	}

	// Gui goi tin OFMOD den switch
	public void processStastRequest(IOFSwitch sw) {

		log.info("SendStastRequest for Switch: " + sw.getId().getLong());

		ListenableFuture<?> future;
		List<OFFlowStatsReply> values = null;
		Match match;
		OFStatsRequest<?> req = null;
		match = sw.getOFFactory().buildMatch().build();
		req = sw.getOFFactory().buildFlowStatsRequest().setMatch(match)
				.setOutPort(OFPort.ANY).setTableId(TableId.ALL).build();

		try {
			if (req != null) {
				future = sw.writeStatsRequest(req);
				values = (List<OFFlowStatsReply>) future.get(10, TimeUnit.SECONDS);
				for (int i = 0; i < values.size(); i++) {
					
					OFFlowStatsReply tempReply = values.get(i);

					//Start Reply
					log.info("Type: "+ tempReply.getStatsType());
					log.info("Version: "+ tempReply.getType());
					log.info("List Entry Flow");
					
					List<OFFlowStatsEntry> listEntry = tempReply.getEntries();
					for(int j = 0; j< listEntry.size(); j++){
						OFFlowStatsEntry entry = listEntry.get(j);
						log.info("Entry " + j + ":  " + entry.toString());
					}
				}
			}
		} catch (Exception e) {
			log.error("Failure retrieving statistics from switch " + sw, e);
		}
	}
}
