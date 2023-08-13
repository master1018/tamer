class RilMessage {
    int mId;
    Object mData;
    ResultCode mResCode;
    RilMessage(int msgId, String rawData) {
        mId = msgId;
        mData = rawData;
    }
    RilMessage(RilMessage other) {
        this.mId = other.mId;
        this.mData = other.mData;
        this.mResCode = other.mResCode;
    }
}
public class StkService extends Handler implements AppInterface {
    private static SIMRecords mSimRecords;
    private static StkService sInstance;
    private CommandsInterface mCmdIf;
    private Context mContext;
    private StkCmdMessage mCurrntCmd = null;
    private StkCmdMessage mMenuCmd = null;
    private RilMessageDecoder mMsgDecoder = null;
    static final int MSG_ID_SESSION_END              = 1;
    static final int MSG_ID_PROACTIVE_COMMAND        = 2;
    static final int MSG_ID_EVENT_NOTIFY             = 3;
    static final int MSG_ID_CALL_SETUP               = 4;
    static final int MSG_ID_REFRESH                  = 5;
    static final int MSG_ID_RESPONSE                 = 6;
    static final int MSG_ID_RIL_MSG_DECODED          = 10;
    private static final int MSG_ID_SIM_LOADED       = 20;
    private static final int DEV_ID_KEYPAD      = 0x01;
    private static final int DEV_ID_DISPLAY     = 0x02;
    private static final int DEV_ID_EARPIECE    = 0x03;
    private static final int DEV_ID_UICC        = 0x81;
    private static final int DEV_ID_TERMINAL    = 0x82;
    private static final int DEV_ID_NETWORK     = 0x83;
    private StkService(CommandsInterface ci, SIMRecords sr, Context context,
            SIMFileHandler fh, SimCard sc) {
        if (ci == null || sr == null || context == null || fh == null
                || sc == null) {
            throw new NullPointerException(
                    "Service: Input parameters must not be null");
        }
        mCmdIf = ci;
        mContext = context;
        mMsgDecoder = RilMessageDecoder.getInstance(this, fh);
        mCmdIf.setOnStkSessionEnd(this, MSG_ID_SESSION_END, null);
        mCmdIf.setOnStkProactiveCmd(this, MSG_ID_PROACTIVE_COMMAND, null);
        mCmdIf.setOnStkEvent(this, MSG_ID_EVENT_NOTIFY, null);
        mCmdIf.setOnStkCallSetUp(this, MSG_ID_CALL_SETUP, null);
        mSimRecords = sr;
        mSimRecords.registerForRecordsLoaded(this, MSG_ID_SIM_LOADED, null);
        mCmdIf.reportStkServiceIsRunning(null);
        StkLog.d(this, "StkService: is running");
    }
    public void dispose() {
        mSimRecords.unregisterForRecordsLoaded(this);
        mCmdIf.unSetOnStkSessionEnd(this);
        mCmdIf.unSetOnStkProactiveCmd(this);
        mCmdIf.unSetOnStkEvent(this);
        mCmdIf.unSetOnStkCallSetUp(this);
        this.removeCallbacksAndMessages(null);
    }
    protected void finalize() {
        StkLog.d(this, "Service finalized");
    }
    private void handleRilMsg(RilMessage rilMsg) {
        if (rilMsg == null) {
            return;
        }
        CommandParams cmdParams = null;
        switch (rilMsg.mId) {
        case MSG_ID_EVENT_NOTIFY:
            if (rilMsg.mResCode == ResultCode.OK) {
                cmdParams = (CommandParams) rilMsg.mData;
                if (cmdParams != null) {
                    handleProactiveCommand(cmdParams);
                }
            }
            break;
        case MSG_ID_PROACTIVE_COMMAND:
            cmdParams = (CommandParams) rilMsg.mData;
            if (cmdParams != null) {
                if (rilMsg.mResCode == ResultCode.OK) {
                    handleProactiveCommand(cmdParams);
                } else {
                    sendTerminalResponse(cmdParams.cmdDet, rilMsg.mResCode,
                            false, 0, null);
                }
            }
            break;
        case MSG_ID_REFRESH:
            cmdParams = (CommandParams) rilMsg.mData;
            if (cmdParams != null) {
                handleProactiveCommand(cmdParams);
            }
            break;
        case MSG_ID_SESSION_END:
            handleSessionEnd();
            break;
        case MSG_ID_CALL_SETUP:
            break;
        }
    }
    private void handleProactiveCommand(CommandParams cmdParams) {
        StkLog.d(this, cmdParams.getCommandType().name());
        StkCmdMessage cmdMsg = new StkCmdMessage(cmdParams);
        switch (cmdParams.getCommandType()) {
        case SET_UP_MENU:
            if (removeMenu(cmdMsg.getMenu())) {
                mMenuCmd = null;
            } else {
                mMenuCmd = cmdMsg;
            }
            sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false, 0,
                    null);
            break;
        case DISPLAY_TEXT:
            if (!cmdMsg.geTextMessage().responseNeeded) {
                sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false,
                        0, null);
            }
            break;
        case REFRESH:
            cmdParams.cmdDet.typeOfCommand = CommandType.SET_UP_IDLE_MODE_TEXT
                    .value();
            break;
        case SET_UP_IDLE_MODE_TEXT:
            sendTerminalResponse(cmdParams.cmdDet, ResultCode.OK, false,
                    0, null);
            break;
        case LAUNCH_BROWSER:
        case SELECT_ITEM:
        case GET_INPUT:
        case GET_INKEY:
        case SEND_DTMF:
        case SEND_SMS:
        case SEND_SS:
        case SEND_USSD:
        case PLAY_TONE:
        case SET_UP_CALL:
            break;
        default:
            StkLog.d(this, "Unsupported command");
            return;
        }
        mCurrntCmd = cmdMsg;
        Intent intent = new Intent(AppInterface.STK_CMD_ACTION);
        intent.putExtra("STK CMD", cmdMsg);
        mContext.sendBroadcast(intent);
    }
    private void handleSessionEnd() {
        StkLog.d(this, "SESSION END");
        mCurrntCmd = mMenuCmd;
        Intent intent = new Intent(AppInterface.STK_SESSION_END_ACTION);
        mContext.sendBroadcast(intent);
    }
    private void sendTerminalResponse(CommandDetails cmdDet,
            ResultCode resultCode, boolean includeAdditionalInfo,
            int additionalInfo, ResponseData resp) {
        if (cmdDet == null) {
            return;
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int tag = ComprehensionTlvTag.COMMAND_DETAILS.value();
        if (cmdDet.compRequired) {
            tag |= 0x80;
        }
        buf.write(tag);
        buf.write(0x03); 
        buf.write(cmdDet.commandNumber);
        buf.write(cmdDet.typeOfCommand);
        buf.write(cmdDet.commandQualifier);
        tag = 0x80 | ComprehensionTlvTag.DEVICE_IDENTITIES.value();
        buf.write(tag);
        buf.write(0x02); 
        buf.write(DEV_ID_TERMINAL); 
        buf.write(DEV_ID_UICC); 
        tag = 0x80 | ComprehensionTlvTag.RESULT.value();
        buf.write(tag);
        int length = includeAdditionalInfo ? 2 : 1;
        buf.write(length);
        buf.write(resultCode.value());
        if (includeAdditionalInfo) {
            buf.write(additionalInfo);
        }
        if (resp != null) {
            resp.format(buf);
        }
        byte[] rawData = buf.toByteArray();
        String hexString = IccUtils.bytesToHexString(rawData);
        if (Config.LOGD) {
            StkLog.d(this, "TERMINAL RESPONSE: " + hexString);
        }
        mCmdIf.sendTerminalResponse(hexString, null);
    }
    private void sendMenuSelection(int menuId, boolean helpRequired) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int tag = BerTlv.BER_MENU_SELECTION_TAG;
        buf.write(tag);
        buf.write(0x00); 
        tag = 0x80 | ComprehensionTlvTag.DEVICE_IDENTITIES.value();
        buf.write(tag);
        buf.write(0x02); 
        buf.write(DEV_ID_KEYPAD); 
        buf.write(DEV_ID_UICC); 
        tag = 0x80 | ComprehensionTlvTag.ITEM_ID.value();
        buf.write(tag);
        buf.write(0x01); 
        buf.write(menuId); 
        if (helpRequired) {
            tag = ComprehensionTlvTag.HELP_REQUEST.value();
            buf.write(tag);
            buf.write(0x00); 
        }
        byte[] rawData = buf.toByteArray();
        int len = rawData.length - 2; 
        rawData[1] = (byte) len;
        String hexString = IccUtils.bytesToHexString(rawData);
        mCmdIf.sendEnvelope(hexString, null);
    }
    private void eventDownload(int event, int sourceId, int destinationId,
            byte[] additionalInfo, boolean oneShot) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int tag = BerTlv.BER_EVENT_DOWNLOAD_TAG;
        buf.write(tag);
        buf.write(0x00); 
        tag = 0x80 | ComprehensionTlvTag.EVENT_LIST.value();
        buf.write(tag);
        buf.write(0x01); 
        buf.write(event); 
        tag = 0x80 | ComprehensionTlvTag.DEVICE_IDENTITIES.value();
        buf.write(tag);
        buf.write(0x02); 
        buf.write(sourceId); 
        buf.write(destinationId); 
        if (additionalInfo != null) {
            for (byte b : additionalInfo) {
                buf.write(b);
            }
        }
        byte[] rawData = buf.toByteArray();
        int len = rawData.length - 2; 
        rawData[1] = (byte) len;
        String hexString = IccUtils.bytesToHexString(rawData);
        mCmdIf.sendEnvelope(hexString, null);
    }
    public static StkService getInstance(CommandsInterface ci, SIMRecords sr,
            Context context, SIMFileHandler fh, SimCard sc) {
        if (sInstance == null) {
            if (ci == null || sr == null || context == null || fh == null
                    || sc == null) {
                return null;
            }
            HandlerThread thread = new HandlerThread("Stk Telephony service");
            thread.start();
            sInstance = new StkService(ci, sr, context, fh, sc);
            StkLog.d(sInstance, "NEW sInstance");
        } else if ((sr != null) && (mSimRecords != sr)) {
            StkLog.d(sInstance, "Reinitialize the Service with SIMRecords");
            mSimRecords = sr;
            mSimRecords.registerForRecordsLoaded(sInstance, MSG_ID_SIM_LOADED, null);
            StkLog.d(sInstance, "sr changed reinitialize and return current sInstance");
        } else {
            StkLog.d(sInstance, "Return current sInstance");
        }
        return sInstance;
    }
    public static AppInterface getInstance() {
        return getInstance(null, null, null, null, null);
    }
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case MSG_ID_SESSION_END:
        case MSG_ID_PROACTIVE_COMMAND:
        case MSG_ID_EVENT_NOTIFY:
        case MSG_ID_REFRESH:
            StkLog.d(this, "ril message arrived");
            String data = null;
            if (msg.obj != null) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (ar != null && ar.result != null) {
                    try {
                        data = (String) ar.result;
                    } catch (ClassCastException e) {
                        break;
                    }
                }
            }
            mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, data));
            break;
        case MSG_ID_CALL_SETUP:
            mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, null));
            break;
        case MSG_ID_SIM_LOADED:
            break;
        case MSG_ID_RIL_MSG_DECODED:
            handleRilMsg((RilMessage) msg.obj);
            break;
        case MSG_ID_RESPONSE:
            handleCmdResponse((StkResponseMessage) msg.obj);
            break;
        default:
            throw new AssertionError("Unrecognized STK command: " + msg.what);
        }
    }
    public synchronized void onCmdResponse(StkResponseMessage resMsg) {
        if (resMsg == null) {
            return;
        }
        Message msg = this.obtainMessage(MSG_ID_RESPONSE, resMsg);
        msg.sendToTarget();
    }
    private boolean validateResponse(StkResponseMessage resMsg) {
        if (mCurrntCmd != null) {
            return (resMsg.cmdDet.compareTo(mCurrntCmd.mCmdDet));
        }
        return false;
    }
    private boolean removeMenu(Menu menu) {
        try {
            if (menu.items.size() == 1 && menu.items.get(0) == null) {
                return true;
            }
        } catch (NullPointerException e) {
            StkLog.d(this, "Unable to get Menu's items size");
            return true;
        }
        return false;
    }
    private void handleCmdResponse(StkResponseMessage resMsg) {
        if (!validateResponse(resMsg)) {
            return;
        }
        ResponseData resp = null;
        boolean helpRequired = false;
        CommandDetails cmdDet = resMsg.getCmdDetails();
        switch (resMsg.resCode) {
        case HELP_INFO_REQUIRED:
            helpRequired = true;
        case OK:
        case PRFRMD_WITH_PARTIAL_COMPREHENSION:
        case PRFRMD_WITH_MISSING_INFO:
        case PRFRMD_WITH_ADDITIONAL_EFS_READ:
        case PRFRMD_ICON_NOT_DISPLAYED:
        case PRFRMD_MODIFIED_BY_NAA:
        case PRFRMD_LIMITED_SERVICE:
        case PRFRMD_WITH_MODIFICATION:
        case PRFRMD_NAA_NOT_ACTIVE:
        case PRFRMD_TONE_NOT_PLAYED:
            switch (AppInterface.CommandType.fromInt(cmdDet.typeOfCommand)) {
            case SET_UP_MENU:
                helpRequired = resMsg.resCode == ResultCode.HELP_INFO_REQUIRED;
                sendMenuSelection(resMsg.usersMenuSelection, helpRequired);
                return;
            case SELECT_ITEM:
                resp = new SelectItemResponseData(resMsg.usersMenuSelection);
                break;
            case GET_INPUT:
            case GET_INKEY:
                Input input = mCurrntCmd.geInput();
                if (!input.yesNo) {
                    if (!helpRequired) {
                        resp = new GetInkeyInputResponseData(resMsg.usersInput,
                                input.ucs2, input.packed);
                    }
                } else {
                    resp = new GetInkeyInputResponseData(
                            resMsg.usersYesNoSelection);
                }
                break;
            case DISPLAY_TEXT:
            case LAUNCH_BROWSER:
                break;
            case SET_UP_CALL:
                mCmdIf.handleCallSetupRequestFromSim(resMsg.usersConfirm, null);
                mCurrntCmd = null;
                return;
            }
            break;
        case NO_RESPONSE_FROM_USER:
        case UICC_SESSION_TERM_BY_USER:
        case BACKWARD_MOVE_BY_USER:
            resp = null;
            break;
        default:
            return;
        }
        sendTerminalResponse(cmdDet, resMsg.resCode, false, 0, resp);
        mCurrntCmd = null;
    }
}
