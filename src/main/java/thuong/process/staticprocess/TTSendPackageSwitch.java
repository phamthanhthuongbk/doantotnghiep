package thuong.process.staticprocess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.learningswitch.LearningSwitch;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.LoggerFactory;

import thuong.test.module.TestSendOpenFLowMod;
import ch.qos.logback.classic.Logger;

/*
 Create by Pham Thanh Thuong
 HEDSPI-DHBK Ha Noi
 Aplication system

 Tap trung cac ham thuc thi gui goi tin tu controller xuong switch
 */

public class TTSendPackageSwitch {
    protected static Logger log = (Logger) LoggerFactory.getLogger(TTSendPackageSwitch.class);
	// flow-mod - for use in the cookie
    public static final int LEARNING_SWITCH_APP_ID = 1;
    // LOOK! This should probably go in some class that encapsulates
    // the app cookie management
    public static final int APP_ID_BITS = 12;
    public static final int APP_ID_SHIFT = (64 - APP_ID_BITS);
    public static final long LEARNING_SWITCH_COOKIE = (long) (LEARNING_SWITCH_APP_ID & ((1 << APP_ID_BITS) - 1)) << APP_ID_SHIFT;

    // more flow-mod defaults
    public static short FLOWMOD_DEFAULT_IDLE_TIMEOUT = 5000; // in seconds
    public static short FLOWMOD_DEFAULT_HARD_TIMEOUT = 0; // infinite
    public static short FLOWMOD_PRIORITY = 0;

    // for managing our map sizes
    public static final int MAX_MACS_PER_SWITCH  = 1000;

    // normally, setup reverse flow as well. Disable only for using cbench for comparison with NOX etc.
    public static final boolean LEARNING_SWITCH_REVERSE_FLOW = true;

    /**
     * Writes a OFFlowMod to a switch.
     * @param sw The switch tow rite the flowmod to.
     * @param command The FlowMod actions (add, delete, etc).
     * @param bufferId The buffer ID if the switch has buffered the packet.
     * @param match The OFMatch structure to write.
     * @param outPort The switch port to output it to.
     */
    public static void writeFlowMod(IOFSwitch sw, OFFlowModCommand command, OFBufferId bufferId,
            Match match, OFPort outPort) {
        OFFlowMod.Builder fmb;
        if (command == OFFlowModCommand.DELETE) {
        	fmb = sw.getOFFactory().buildFlowDelete();
        } else {
        	fmb = sw.getOFFactory().buildFlowAdd();
        }
        fmb.setMatch(match);
        fmb.setCookie((U64.of(TTSendPackageSwitch.LEARNING_SWITCH_COOKIE)));
        fmb.setIdleTimeout(TTSendPackageSwitch.FLOWMOD_DEFAULT_IDLE_TIMEOUT);
        fmb.setHardTimeout(TTSendPackageSwitch.FLOWMOD_DEFAULT_HARD_TIMEOUT);
        fmb.setPriority(TTSendPackageSwitch.FLOWMOD_PRIORITY);
        fmb.setBufferId(bufferId);
        fmb.setOutPort((command == OFFlowModCommand.DELETE) ? OFPort.ANY : outPort);
        Set<OFFlowModFlags> sfmf = new HashSet<OFFlowModFlags>();
        if (command != OFFlowModCommand.DELETE) {
        	sfmf.add(OFFlowModFlags.SEND_FLOW_REM);
        }
        fmb.setFlags(sfmf);
        

        // set the ofp_action_header/out actions:
        // from the openflow 1.0 spec: need to set these on a struct ofp_action_output:
        // uint16_t type; /* OFPAT_OUTPUT. */
        // uint16_t len; /* Length is 8. */
        // uint16_t port; /* Output port. */
        // uint16_t max_len; /* Max length to send to controller. */
        // type/len are set because it is OFActionOutput,
        // and port, max_len are arguments to this constructor
        List<OFAction> al = new ArrayList<OFAction>();
        al.add(sw.getOFFactory().actions().buildOutput().setPort(outPort).setMaxLen(Integer.MAX_VALUE).build());
        fmb.setActions(al);

        if (log.isTraceEnabled()) {
            log.trace("{} {} flow mod {}",
                      new Object[]{ sw, (command == OFFlowModCommand.DELETE) ? "deleting" : "adding", fmb.build() });
        }

        // and write it out
        sw.write(fmb.build());
    }

    /**
     * Pushes a packet-out to a switch.  The assumption here is that
     * the packet-in was also generated from the same switch.  Thus, if the input
     * port of the packet-in and the outport are the same, the function will not
     * push the packet-out.
     * @param sw        switch that generated the packet-in, and from which packet-out is sent
     * @param match     OFmatch
     * @param pi        packet-in
     * @param outport   output port
     */
    public static void pushPacket(IOFSwitch sw, Match match, OFPacketIn pi, OFPort outport) {
        if (pi == null) {
            return;
        }
        
        OFPort inPort = (pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT));

        // The assumption here is (sw) is the switch that generated the
        // packet-in. If the input port is the same as output port, then
        // the packet-out should be ignored.
        if (inPort.equals(outport)) {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to do packet-out to the same " +
                          "interface as packet-in. Dropping packet. " +
                          " SrcSwitch={}, match = {}, pi={}",
                          new Object[]{sw, match, pi});
                return;
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("PacketOut srcSwitch={} match={} pi={}",
                      new Object[] {sw, match, pi});
        }

        OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();

        // set actions
        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(sw.getOFFactory().actions().buildOutput().setPort(outport).setMaxLen(Integer.MAX_VALUE).build());

        pob.setActions(actions);
       
        // If the switch doens't support buffering set the buffer id to be none
        // otherwise it'll be the the buffer id of the PacketIn
        if (sw.getBuffers() == 0) {
            // We set the PI buffer id here so we don't have to check again below
            pi = pi.createBuilder().setBufferId(OFBufferId.NO_BUFFER).build();
            pob.setBufferId(OFBufferId.NO_BUFFER);
        } else {
            pob.setBufferId(pi.getBufferId());
        }

        pob.setInPort(inPort);

        // If the buffer id is none or the switch doesn's support buffering
        // we send the data with the packet out
        if (pi.getBufferId() == OFBufferId.NO_BUFFER) {
            byte[] packetData = pi.getData();
            pob.setData(packetData);
        }

        sw.write(pob.build());
    }

    /**
     * Writes an OFPacketOut message to a switch.
     * @param sw The switch to write the PacketOut to.
     * @param packetInMessage The corresponding PacketIn.
     * @param egressPort The switchport to output the PacketOut.
     */
    public static void writePacketOutForPacketIn(IOFSwitch sw, OFPacketIn packetInMessage, OFPort egressPort) {
        // from openflow 1.0 spec - need to set these on a struct ofp_packet_out:
        // uint32_t buffer_id; /* ID assigned by datapath (-1 if none). */
        // uint16_t in_port; /* Packet's input port (OFPP_NONE if none). */
        // uint16_t actions_len; /* Size of action array in bytes. */
        // struct ofp_action_header actions[0]; /* Actions. */
        /* uint8_t data[0]; */ /* Packet data. The length is inferred
                                  from the length field in the header.
                                  (Only meaningful if buffer_id == -1.) */

        OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();

        // Set buffer_id, in_port, actions_len
        pob.setBufferId(packetInMessage.getBufferId());
        pob.setInPort(packetInMessage.getVersion().compareTo(OFVersion.OF_12) < 0 ? packetInMessage.getInPort() : packetInMessage.getMatch().get(MatchField.IN_PORT));

        // set actions
        List<OFAction> actions = new ArrayList<OFAction>(1);
        actions.add(sw.getOFFactory().actions().buildOutput().setPort(egressPort).setMaxLen(Integer.MAX_VALUE).build());
        pob.setActions(actions);

        // set data - only if buffer_id == -1
        if (packetInMessage.getBufferId() == OFBufferId.NO_BUFFER) {
            byte[] packetData = packetInMessage.getData();
            pob.setData(packetData);
        }

        // and write it out
        sw.write(pob.build());

    }

}
