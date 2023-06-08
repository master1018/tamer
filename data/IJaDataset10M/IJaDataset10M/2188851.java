package net.sf.istcontract.aws.communication.protocol.ContractSetUpProtocol;

import net.sf.istcontract.aws.SimplePercept;
import net.sf.istcontract.aws.communication.message.Message;
import net.sf.istcontract.aws.communication.message.MessageFactory;
import net.sf.istcontract.aws.communication.message.MessageTemplate;
import net.sf.istcontract.aws.communication.performative.*;
import net.sf.istcontract.aws.communication.protocol.IConversationListener;
import net.sf.istcontract.aws.communication.protocol.MalformedMessageException;
import net.sf.istcontract.aws.communication.protocol.NoSuchConversationException;
import net.sf.istcontract.aws.communication.protocol.NoSuchProtocolException;
import net.sf.istcontract.aws.communication.protocol.ProtocolInstance;
import net.sf.istcontract.aws.communication.protocol.ProtocolViolationException;
import net.sf.istcontract.aws.configuration.InvalidAgentConfiguration;
import net.sf.istcontract.aws.exception.AgentException;
import net.sf.istcontract.aws.input.domain.EBookContract;
import net.sf.istcontract.aws.knowledge.ontology.OntologyConcept;
import net.sf.istcontract.aws.utils.AgentLogger;
import net.sf.istcontract.aws.reasoning.behaviour.Behaviour;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 
 *
 * This a behaivour stub, created to be able to handle each and every performative type the specified role
 * is able to handle, according to the protocol.
 * 
 * Just add your own behaviour here, mainly:
 * + Choices, when several messages can be sent (uncomment the choice - stubs)
 * + Ontological contents to be sent along with messages
 *
 *
 * @author Autogenerated by ingenias2contractcodegenerator (Ignasi Gomez-Sebastia)
 *
 */
public class NotaryBehaivour extends Behaviour {

    HashMap<String, String> acceptances = InitHashMap();

    @Override
    protected void runInform(IConversationListener comm) throws AgentException {
        try {
            Performative receivedPerf = (Performative) this.percept;
            String ID = comm.buildID(receivedPerf, "Participant");
            MessageTemplate[] NextMsg;
            MessageTemplate replyMessage;
            OntologyConcept onto = null;
            String sender = receivedPerf.getSender();
            AgentLogger.log("----> Agent '" + sender + "' claims to have accepted contract");
            registry_acceptance(sender);
            if (acceptedAll()) {
                AgentLogger.log("----> All agents have accepted contract, Notary is storing and notifying");
                StoreContract();
                NextMsg = comm.joinToConversationReceivedMessage(ContractSetUpProtocol.class, NotaryRole.class, receivedPerf);
                Message MsgToSend;
                replyMessage = this.getBehaivoralChoice("NotaryReply", NextMsg);
                Inform replyPerformative = new Inform(receivedPerf.getIdMessage(), receivedPerf.getIdDialog(), receivedPerf.getInReplyTo(), receivedPerf.getSender(), receivedPerf.getReceiver(), onto, receivedPerf.getProtocolName(), receivedPerf.getReplyWith());
                String receiverName = "";
                Iterator<String> it = this.acceptances.keySet().iterator();
                while (it.hasNext()) {
                    receiverName = (String) it.next();
                    MsgToSend = MessageFactory.createResponse(replyPerformative, replyMessage, onto);
                    MsgToSend.setReceiver(receiverName);
                    comm.getAgentTransport().sendMessage(MsgToSend);
                }
            }
        } catch (Exception E) {
            String dummy = getStackTrace(E);
            AgentLogger.log(dummy);
        }
    }

    @Override
    protected void runNotUnderstood(IConversationListener comm) throws AgentException {
        Performative receivedPerf = (Performative) this.percept;
        String sender = receivedPerf.getSender();
        AgentLogger.log("--------------WARNING---------------------");
        AgentLogger.log("----> Agent '" + sender + "' did not understood my last message");
        AgentLogger.log("------------------------------------------");
    }

    private void registry_acceptance(String sender) {
        this.acceptances.put(sender, "true");
    }

    private HashMap<String, String> InitHashMap() {
        HashMap<String, String> res = new HashMap<String, String>();
        res.put("InitiatorCSP", "false");
        res.put("1_ParticipantCSP", "false");
        res.put("2_ParticipantCSP", "false");
        res.put("3_ParticipantCSP", "false");
        return res;
    }

    public void UpdateHashMap(HashMap<String, String> Map) {
        this.acceptances = Map;
    }

    private boolean acceptedAll() {
        Iterator<String> it = this.acceptances.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            String accp = (String) this.acceptances.get(key);
            if (accp.equalsIgnoreCase("false")) {
                return false;
            }
        }
        return true;
    }

    private void StoreContract() {
        AgentLogger.log("--------------NOTICE---------------------");
        AgentLogger.log("Whish I knew how to store contracts...");
        AgentLogger.log("------------------------------------------");
    }

    public String getStackTrace(Throwable t) {
        java.io.StringWriter stringWritter = new java.io.StringWriter();
        java.io.PrintWriter printWritter = new java.io.PrintWriter(stringWritter, true);
        t.printStackTrace(printWritter);
        printWritter.flush();
        stringWritter.flush();
        return stringWritter.toString();
    }
}