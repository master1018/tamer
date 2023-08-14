public class ImpsChatSessionManager extends ChatSessionManager
            implements ServerTransactionListener {
    private ImpsConnection mConnection;
    private ImpsTransactionManager mTransactionManager;
    private ArrayList<Message> mMessageQueue;
    private boolean mStartNotifying;
    ImpsChatSessionManager(ImpsConnection connection) {
        mConnection = connection;
        mMessageQueue = new ArrayList<Message>();
        mTransactionManager = connection.getTransactionManager();
        mTransactionManager.setTransactionListener(ImpsTags.NewMessage, this);
        mTransactionManager.setTransactionListener(ImpsTags.DeliveryReport_Request, this);
    }
    @Override
    protected void sendMessageAsync(final ChatSession ses, final Message message) {
        message.setFrom(mConnection.getSession().getLoginUserAddress());
        if(message.getDateTime() == null) {
            message.setDateTime(new Date());
        }
        Primitive primitive = createSendMessagePrimitive(message);
        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {
            @Override
            public void onResponseOk(Primitive response) { }
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                ses.onSendMessageError(message, error);
            }
        };
        tx.sendRequest(primitive);
    }
    public void notifyServerTransaction(ServerTransaction tx) {
        Primitive primitive = tx.getRequest();
        if (ImpsTags.NewMessage.equals(primitive.getType())) {
            Message msg = extractMessage(primitive);
            Primitive response = new Primitive(ImpsTags.MessageDelivered);
            response.addElement(ImpsTags.MessageID, msg.getID());
            tx.sendResponse(response);
            synchronized(mMessageQueue) {
                if(mStartNotifying){
                    processMessage(msg);
                } else {
                    mMessageQueue.add(msg);
                }
            }
        } else if(ImpsTags.DeliveryReport_Request.equals(primitive.getType())) {
            tx.sendStatusResponse(ImpsConstants.SUCCESS_CODE);
            ImErrorInfo error = ImpsUtils.checkResultError(primitive);
            if(error != null) {
                PrimitiveElement msgInfo = primitive.getElement(ImpsTags.MessageInfo);
                String msgId = msgInfo.getChildContents(ImpsTags.MessageID);
                PrimitiveElement recipent = msgInfo.getChild(ImpsTags.Recipient);
                ImpsAddress recipentAddress = ImpsAddress.fromPrimitiveElement(
                        recipent.getFirstChild());
                ChatSession session = findSession(recipentAddress);
                if(session != null) {
                    session.onSendMessageError(msgId, error);
                } else {
                    ImpsLog.log("Session has closed when received delivery error: "
                            + error);
                }
            }
        }
    }
    public void start() {
        synchronized (mMessageQueue) {
            mStartNotifying = true;
            for (Message message : mMessageQueue) {
                processMessage(message);
            }
            mMessageQueue.clear();
        }
    }
    private Message extractMessage(Primitive primitive) {
        String msgBody = primitive.getElementContents(ImpsTags.ContentData);
        if (msgBody == null) {
            msgBody = "";
        }
        Message msg = new Message(msgBody);
        PrimitiveElement msgInfo = primitive.getElement(ImpsTags.MessageInfo);
        String id = msgInfo.getChildContents(ImpsTags.MessageID);
        msg.setID(id);
        PrimitiveElement sender = msgInfo.getChild(ImpsTags.Sender);
        msg.setFrom(ImpsAddress.fromPrimitiveElement(sender.getFirstChild()));
        PrimitiveElement recipent = msgInfo.getChild(ImpsTags.Recipient);
        if (recipent != null && recipent.getFirstChild() != null) {
            msg.setTo(ImpsAddress.fromPrimitiveElement(recipent.getFirstChild()));
        } else {
            msg.setTo(mConnection.getLoginUser().getAddress());
        }
        String dateTime = msgInfo.getChildContents(ImpsTags.DateTime);
        if (dateTime != null) {
            try {
                Time t = new Time();
                t.parse(dateTime);
                msg.setDateTime(new Date(t.toMillis(false )));
            } catch (TimeFormatException e) {
                msg.setDateTime(new Date());
            }
        } else {
            msg.setDateTime(new Date());
        }
        return msg;
    }
    private Primitive createSendMessagePrimitive(Message message) {
        Primitive primitive = new Primitive(ImpsTags.SendMessage_Request);
        primitive.addElement(ImpsTags.DeliveryReport,
                mConnection.getConfig().needDeliveryReport());
        PrimitiveElement msgInfo = primitive.addElement(ImpsTags.MessageInfo);
        PrimitiveElement recipient = msgInfo.addChild(ImpsTags.Recipient);
        recipient.addChild(((ImpsAddress)message.getTo()).toPrimitiveElement());
        PrimitiveElement sender = msgInfo.addChild(ImpsTags.Sender);
        sender.addChild(((ImpsAddress)message.getFrom()).toPrimitiveElement());
        msgInfo.addChild(ImpsTags.ContentType, "text/plain");
        String msgBody = message.getBody();
        msgInfo.addChild(ImpsTags.ContentSize, Integer.toString(msgBody.length()));
        primitive.addElement(ImpsTags.ContentData, msgBody);
        return primitive;
    }
    void processMessage(Message msg) {
        ImpsAddress from = (ImpsAddress) msg.getFrom();
        ImpsAddress to = (ImpsAddress) msg.getTo();
        ImpsAddress address = (to instanceof ImpsGroupAddress) ? to : from;
        synchronized (this) {
            ChatSession ses = findSession(address);
            if (ses == null) {
                ImEntity participant = address.getEntity(mConnection);
                if (participant != null) {
                    ses = createChatSession(address.getEntity(mConnection));
                } else {
                    ImpsLog.log("Message from unknown sender");
                    return;
                }
            }
            ses.onReceiveMessage(msg);
        }
    }
    private ChatSession findSession(Address address) {
        for(ChatSession session : mSessions) {
            ImEntity participant = session.getParticipant();
            if(participant.getAddress().equals(address)) {
                return session;
            }
        }
        return null;
    }
}
