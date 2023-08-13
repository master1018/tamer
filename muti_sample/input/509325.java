public class PhoneUtils {
    private static final String LOG_TAG = "PhoneUtils";
    private static final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);
    private static final boolean DBG_SETAUDIOMODE_STACK = false;
    static final String ADD_CALL_MODE_KEY = "add_call_mode";
    static final int CALL_STATUS_DIALED = 0;  
    static final int CALL_STATUS_DIALED_MMI = 1;  
    static final int CALL_STATUS_FAILED = 2;  
    static final int AUDIO_IDLE = 0;  
    static final int AUDIO_RINGING = 1;  
    static final int AUDIO_OFFHOOK = 2;  
    private static int sAudioBehaviourState = AUDIO_IDLE;
    private static boolean sIsSpeakerEnabled = false;
    private static Hashtable<Connection, Boolean> sConnectionMuteTable =
        new Hashtable<Connection, Boolean>();
    private static ConnectionHandler mConnectionHandler;
    private static final int PHONE_STATE_CHANGED = -1;
    private static final int CNAP_SPECIAL_CASE_NO = -1;
    private static IExtendedNetworkService mNwService = null;
    private static Message mMmiTimeoutCbMsg = null;
    private static boolean sIsNoiseSuppressionEnabled = true;
    private static class ConnectionHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            switch (msg.what) {
                case PHONE_STATE_CHANGED:
                    if (DBG) log("ConnectionHandler: updating mute state for each connection");
                    Phone phone = (Phone) ar.userObj;
                    List<Connection> fgConnections = phone.getForegroundCall().getConnections();
                    for (Connection cn : fgConnections) {
                        if (sConnectionMuteTable.get(cn) == null) {
                            sConnectionMuteTable.put(cn, Boolean.FALSE);
                        }
                    }
                    List<Connection> bgConnections = phone.getBackgroundCall().getConnections();
                    for (Connection cn : bgConnections) {
                        if (sConnectionMuteTable.get(cn) == null) {
                            sConnectionMuteTable.put(cn, Boolean.FALSE);
                        }
                    }
                    Connection cn;
                    for (Iterator<Connection> cnlist = sConnectionMuteTable.keySet().iterator();
                            cnlist.hasNext();) {
                        cn = cnlist.next();
                        if (!fgConnections.contains(cn) && !bgConnections.contains(cn)) {
                            if (DBG) log("connection: " + cn + "not accounted for, removing.");
                            cnlist.remove();
                        }
                    }
                    if (phone.getState() != Phone.State.IDLE) {
                        restoreMuteState(phone);
                    } else {
                        setMuteInternal(phone, false);
                    }
                    break;
            }
        }
    }
    private static ServiceConnection ExtendedNetworkServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            if (DBG) log("Extended NW onServiceConnected");
            mNwService = IExtendedNetworkService.Stub.asInterface(iBinder);
        }
        public void onServiceDisconnected(ComponentName arg0) {
            if (DBG) log("Extended NW onServiceDisconnected");
            mNwService = null;
        }
    };
    public static void initializeConnectionHandler(Phone phone) {
        if (mConnectionHandler == null) {
            mConnectionHandler = new ConnectionHandler();
        }
        phone.registerForPreciseCallStateChanged(mConnectionHandler, PHONE_STATE_CHANGED, phone);
        Intent intent = new Intent("com.android.ussd.IExtendedNetworkService");
        phone.getContext().bindService(intent,
                ExtendedNetworkServiceConnection, Context.BIND_AUTO_CREATE);
        if (DBG) log("Extended NW bindService IExtendedNetworkService");
    }
    private PhoneUtils() {
    }
    static void setAudioControlState(int newState) {
        sAudioBehaviourState = newState;
    }
    static boolean answerCall(Phone phone) {
        if (DBG) log("answerCall()...");
        PhoneApp.getInstance().getRinger().stopRing();
        PhoneUtils.setAudioControlState(PhoneUtils.AUDIO_OFFHOOK);
        boolean answered = false;
        Call call = phone.getRingingCall();
        PhoneApp app = PhoneApp.getInstance();
        boolean phoneIsCdma = (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA);
        BluetoothHandsfree bthf = null;
        if (phoneIsCdma) {
            if (call.getState() == Call.State.WAITING) {
                final CallNotifier notifier = app.notifier;
                notifier.stopSignalInfoTone();
            }
        }
        if (call != null && call.isRinging()) {
            if (DBG) log("answerCall: call state = " + call.getState());
            try {
                if (phoneIsCdma) {
                    if (app.cdmaPhoneCallState.getCurrentCallState()
                            == CdmaPhoneCallState.PhoneCallState.IDLE) {
                        app.cdmaPhoneCallState.setCurrentCallState(
                                CdmaPhoneCallState.PhoneCallState.SINGLE_ACTIVE);
                    } else {
                        app.cdmaPhoneCallState.setCurrentCallState(
                                CdmaPhoneCallState.PhoneCallState.CONF_CALL);
                        app.cdmaPhoneCallState.setAddCallMenuStateAfterCallWaiting(true);
                        bthf = app.getBluetoothHandsfree();
                        if (bthf != null) {
                            bthf.cdmaSetSecondCallState(true);
                        }
                    }
                }
                phone.acceptCall();
                answered = true;
                if (phoneIsCdma) {
                    setMute(phone, false);
                }
                setAudioMode(phone.getContext(), AudioManager.MODE_IN_CALL);
                activateSpeakerIfDocked(phone);
            } catch (CallStateException ex) {
                Log.w(LOG_TAG, "answerCall: caught " + ex, ex);
                if (phoneIsCdma) {
                    app.cdmaPhoneCallState.setCurrentCallState(
                            app.cdmaPhoneCallState.getPreviousCallState());
                    if (bthf != null) {
                        bthf.cdmaSetSecondCallState(false);
                    }
                }
            }
        }
        return answered;
    }
    static boolean hangup(Phone phone) {
        boolean hungup = false;
        Call ringing = phone.getRingingCall();
        Call fg = phone.getForegroundCall();
        Call bg = phone.getBackgroundCall();
        if (!ringing.isIdle()) {
            if (DBG) log("HANGUP ringing call");
            hungup = hangupRingingCall(phone);
        } else if (!fg.isIdle()) {
            if (DBG) log("HANGUP foreground call");
            hungup = hangup(fg);
        } else if (!bg.isIdle()) {
            if (DBG) log("HANGUP background call");
            hungup = hangup(bg);
        }
        if (DBG) log("hungup=" + hungup);
        return hungup;
    }
    static boolean hangupRingingCall(Phone phone) {
        if (DBG) log("hangup ringing call");
        Call ringing = phone.getRingingCall();
        int phoneType = phone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            Call.State state = ringing.getState();
            if (state == Call.State.INCOMING) {
                if (DBG) log("hangup ringing call");
                return hangup(ringing);
            } else if (state == Call.State.WAITING) {
                if (DBG) log("hangup Call waiting call");
                final CallNotifier notifier = PhoneApp.getInstance().notifier;
                notifier.sendCdmaCallWaitingReject();
                return true;
            } else {
                if (DBG) log("No Ringing call to hangup");
                return false;
            }
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            if (DBG) log("hangup ringing call");
            return hangup(ringing);
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
    }
    static boolean hangupActiveCall(Phone phone) {
        if (DBG) log("hangup active call");
        return hangup(phone.getForegroundCall());
    }
    static boolean hangupHoldingCall(Phone phone) {
        if (DBG) log("hangup holding call");
        return hangup(phone.getBackgroundCall());
    }
    static boolean hangupRingingAndActive(Phone phone) {
        boolean hungUpRingingCall = false;
        boolean hungUpFgCall = false;
        Call ringingCall = phone.getRingingCall();
        Call fgCall = phone.getForegroundCall();
        if (!ringingCall.isIdle()) {
            if (DBG) log("endCallInternal: Hang up Ringing Call");
            hungUpRingingCall = hangupRingingCall(phone);
        }
        if (!fgCall.isIdle()) {
            if (DBG) log("endCallInternal: Hang up Foreground Call");
            hungUpFgCall = hangupActiveCall(phone);
        }
        return hungUpRingingCall || hungUpFgCall;
    }
    static boolean hangup(Call call) {
        try {
            call.hangup();
            return true;
        } catch (CallStateException ex) {
            Log.e(LOG_TAG, "Call hangup: caught " + ex, ex);
        }
        return false;
    }
    static void hangup(Connection c) {
        try {
            if (c != null) {
                c.hangup();
            }
        } catch (CallStateException ex) {
            Log.w(LOG_TAG, "Connection hangup: caught " + ex, ex);
        }
    }
    static boolean answerAndEndHolding(Phone phone) {
        if (DBG) log("end holding & answer waiting: 1");
        if (!hangupHoldingCall(phone)) {
            Log.e(LOG_TAG, "end holding failed!");
            return false;
        }
        if (DBG) log("end holding & answer waiting: 2");
        return answerCall(phone);
    }
    static boolean answerAndEndActive(Phone phone) {
        if (DBG) log("answerAndEndActive()...");
        return hangupActiveCall(phone);
    }
    static private void updateCdmaCallStateOnNewOutgoingCall(PhoneApp app) {
        if (app.cdmaPhoneCallState.getCurrentCallState() ==
            CdmaPhoneCallState.PhoneCallState.IDLE) {
            app.cdmaPhoneCallState.setCurrentCallState(
                CdmaPhoneCallState.PhoneCallState.SINGLE_ACTIVE);
        } else {
            app.cdmaPhoneCallState.setCurrentCallState(
                CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE);
        }
    }
    static int placeCall(Phone phone, String number, Uri contactRef) {
        int status = CALL_STATUS_DIALED;
        try {
            if (DBG) log("placeCall: '" + number + "'...");
            Connection cn = phone.dial(number);
            if (DBG) log("===> phone.dial() returned: " + cn);
            int phoneType = phone.getPhoneType();
            if (cn == null) {
                if (phoneType == Phone.PHONE_TYPE_GSM) {
                    if (DBG) log("dialed MMI code: " + number);
                    status = CALL_STATUS_DIALED_MMI;
                    if (mNwService != null) {
                        try {
                            mNwService.setMmiString(number);
                            if (DBG) log("Extended NW bindService setUssdString (" + number + ")");
                        } catch (RemoteException e) {
                            mNwService = null;
                        }
                    }
                } else {
                    status = PhoneUtils.CALL_STATUS_FAILED;
                }
            } else {
                PhoneApp app = PhoneApp.getInstance();
                if (phoneType == Phone.PHONE_TYPE_CDMA) {
                    updateCdmaCallStateOnNewOutgoingCall(app);
                }
                PhoneUtils.setAudioControlState(PhoneUtils.AUDIO_OFFHOOK);
                String content = phone.getContext().getContentResolver().SCHEME_CONTENT;
                if ((contactRef != null) && (contactRef.getScheme().equals(content))) {
                    Object userDataObject = cn.getUserData();
                    if (userDataObject == null) {
                        cn.setUserData(contactRef);
                    } else {
                        if (userDataObject instanceof CallerInfo) {
                            ((CallerInfo) userDataObject).contactRefUri = contactRef;
                        } else {
                            ((CallerInfoToken) userDataObject).currentInfo.contactRefUri =
                                contactRef;
                        }
                    }
                }
                setAudioMode(phone.getContext(), AudioManager.MODE_IN_CALL);
                activateSpeakerIfDocked(phone);
            }
        } catch (CallStateException ex) {
            Log.w(LOG_TAG, "Exception from phone.dial()", ex);
            status = CALL_STATUS_FAILED;
        }
        return status;
    }
    static int placeCallVia(Context context, Phone phone,
                            String number, Uri contactRef, Uri gatewayUri) {
        if (DBG) log("placeCallVia: '" + number + "' GW:'" + gatewayUri + "'");
        if (null == gatewayUri || !"tel".equals(gatewayUri.getScheme())) {
            Log.e(LOG_TAG, "Unsupported URL:" + gatewayUri);
            return CALL_STATUS_FAILED;
        }
        String gatewayNumber = gatewayUri.getSchemeSpecificPart();
        Connection connection;
        try {
            connection = phone.dial(gatewayNumber);
        } catch (CallStateException ex) {
            Log.e(LOG_TAG, "Exception dialing gateway", ex);
            connection = null;
        }
        if (null == connection) {
            Log.e(LOG_TAG, "Got null connection.");
            return CALL_STATUS_FAILED;
        }
        PhoneApp app = PhoneApp.getInstance();
        boolean phoneIsCdma = (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA);
        if (phoneIsCdma) {
            updateCdmaCallStateOnNewOutgoingCall(app);
        }
        PhoneUtils.setAudioControlState(PhoneUtils.AUDIO_OFFHOOK);
        if (phoneIsCdma) {
            number = CdmaConnection.formatDialString(number);
        }
        number = PhoneNumberUtils.extractNetworkPortion(number);
        number = PhoneNumberUtils.convertKeypadLettersToDigits(number);
        number = PhoneNumberUtils.formatNumber(number);
        CallerInfo info = null;
        if (ContentResolver.SCHEME_CONTENT.equals(contactRef.getScheme())) {
            info = CallerInfo.getCallerInfo(context, contactRef);
        }
        if (null == info) {
            info = CallerInfo.getCallerInfo(context, number);
        }
        info.phoneNumber = number;
        connection.setUserData(info);
        setAudioMode(phone.getContext(), AudioManager.MODE_IN_CALL);
        return CALL_STATUS_DIALED;
    }
    static void sendEmptyFlash(Phone phone) {
        if (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            Call fgCall = phone.getForegroundCall();
            if (fgCall.getState() == Call.State.ACTIVE) {
                if (DBG) Log.d(LOG_TAG, "onReceive: (CDMA) sending empty flash to network");
                switchHoldingAndActive(phone);
            }
        }
    }
    static void switchHoldingAndActive(Phone phone) {
        try {
            if (DBG) log("switchHoldingAndActive");
            phone.switchHoldingAndActive();
        } catch (CallStateException ex) {
            Log.w(LOG_TAG, "switchHoldingAndActive: caught " + ex, ex);
        }
    }
    static Boolean restoreMuteState(Phone phone) {
        Connection c = phone.getForegroundCall().getEarliestConnection();
        if (c != null) {
            int phoneType = phone.getPhoneType();
            Boolean shouldMute = null;
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                shouldMute = sConnectionMuteTable.get(
                        phone.getForegroundCall().getLatestConnection());
            } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                shouldMute = sConnectionMuteTable.get(
                        phone.getForegroundCall().getEarliestConnection());
            }
            if (shouldMute == null) {
                if (DBG) log("problem retrieving mute value for this connection.");
                shouldMute = Boolean.FALSE;
            }
            setMute (phone, shouldMute.booleanValue());
            return shouldMute;
        }
        return Boolean.valueOf(getMute (phone));
    }
    static void mergeCalls(Phone phone) {
        int phoneType = phone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_GSM) {
            try {
                if (DBG) log("mergeCalls");
                phone.conference();
            } catch (CallStateException ex) {
                Log.w(LOG_TAG, "mergeCalls: caught " + ex, ex);
            }
        } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
            if (DBG) log("mergeCalls");
            PhoneApp app = PhoneApp.getInstance();
            if (app.cdmaPhoneCallState.getCurrentCallState()
                    == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE) {
                app.cdmaPhoneCallState.setCurrentCallState(
                        CdmaPhoneCallState.PhoneCallState.CONF_CALL);
                switchHoldingAndActive(phone);
            }
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
    }
    static void separateCall(Connection c) {
        try {
            if (DBG) log("separateCall: " + c.getAddress());
            c.separate();
        } catch (CallStateException ex) {
            Log.w(LOG_TAG, "separateCall: caught " + ex, ex);
        }
    }
    static Dialog displayMMIInitiate(Context context,
                                          MmiCode mmiCode,
                                          Message buttonCallbackMessage,
                                          Dialog previousAlert) {
        if (DBG) log("displayMMIInitiate: " + mmiCode);
        if (previousAlert != null) {
            previousAlert.dismiss();
        }
        if (mNwService != null) {
            if (DBG) log("running USSD code, displaying indeterminate progress.");
            ProgressDialog pd = new ProgressDialog(context);
            CharSequence textmsg = "";
            try {
                textmsg = mNwService.getMmiRunningText();
            } catch (RemoteException e) {
                mNwService = null;
                textmsg = context.getText(R.string.ussdRunning);
            }
            if (DBG) log("Extended NW displayMMIInitiate (" + textmsg+ ")");
            pd.setMessage(textmsg);
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            pd.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
            pd.show();
            mMmiTimeoutCbMsg = buttonCallbackMessage;
            try {
                mMmiTimeoutCbMsg.getTarget().sendMessageDelayed(buttonCallbackMessage, 15000);
            } catch(NullPointerException e) {
                mMmiTimeoutCbMsg = null;
            }
            return pd;
        }
        boolean isCancelable = (mmiCode != null) && mmiCode.isCancelable();
        if (!isCancelable) {
            if (DBG) log("not a USSD code, displaying status toast.");
            CharSequence text = context.getText(R.string.mmiStarted);
            Toast.makeText(context, text, Toast.LENGTH_SHORT)
                .show();
            return null;
        } else {
            if (DBG) log("running USSD code, displaying indeterminate progress.");
            ProgressDialog pd = new ProgressDialog(context);
            pd.setMessage(context.getText(R.string.ussdRunning));
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            pd.show();
            return pd;
        }
    }
    static void displayMMIComplete(final Phone phone, Context context, final MmiCode mmiCode,
            Message dismissCallbackMessage,
            AlertDialog previousAlert) {
        CharSequence text;
        int title = 0;  
        MmiCode.State state = mmiCode.getState();
        if (DBG) log("displayMMIComplete: state=" + state);
        if(mMmiTimeoutCbMsg != null) {
            try{
                mMmiTimeoutCbMsg.getTarget().removeMessages(mMmiTimeoutCbMsg.what);
                if (DBG) log("Extended NW displayMMIComplete removeMsg");
            } catch (NullPointerException e) {
            }
            mMmiTimeoutCbMsg = null;
        }
        switch (state) {
            case PENDING:
                text = mmiCode.getMessage();
                if (DBG) log("- using text from PENDING MMI message: '" + text + "'");
                break;
            case CANCELLED:
                text = context.getText(R.string.mmiCancelled);
                break;
            case COMPLETE:
                if (PhoneApp.getInstance().getPUKEntryActivity() != null) {
                    title = com.android.internal.R.string.PinMmi;
                    text = context.getText(R.string.puk_unlocked);
                    break;
                }
            case FAILED:
                text = mmiCode.getMessage();
                if (DBG) log("- using text from MMI message: '" + text + "'");
                break;
            default:
                throw new IllegalStateException("Unexpected MmiCode state: " + state);
        }
        if (previousAlert != null) {
            previousAlert.dismiss();
        }
        PhoneApp app = PhoneApp.getInstance();
        if ((app.getPUKEntryActivity() != null) && (state == MmiCode.State.COMPLETE)) {
            if (DBG) log("displaying PUK unblocking progress dialog.");
            ProgressDialog pd = new ProgressDialog(app);
            pd.setTitle(title);
            pd.setMessage(text);
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
            pd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            pd.show();
            app.setPukEntryProgressDialog(pd);
        } else {
            if (app.getPUKEntryActivity() != null) {
                app.setPukEntryActivity(null);
            }
            if (state != MmiCode.State.PENDING) {
                if (DBG) log("MMI code has finished running.");
                if (mNwService != null) {
                    try {
                        text = mNwService.getUserMessage(text);
                    } catch (RemoteException e) {
                        mNwService = null;
                    }
                    if (DBG) log("Extended NW displayMMIInitiate (" + text + ")");
                    if (text == null || text.length() == 0)
                        return;
                }
                AlertDialog newDialog = new AlertDialog.Builder(context)
                        .setMessage(text)
                        .setPositiveButton(R.string.ok, null)
                        .setCancelable(true)
                        .create();
                newDialog.getWindow().setType(
                        WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
                newDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                newDialog.show();
            } else {
                if (DBG) log("USSD code has requested user input. Constructing input dialog.");
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.dialog_ussd_response, null);
                final EditText inputText = (EditText) dialogView.findViewById(R.id.input_field);
                final DialogInterface.OnClickListener mUSSDDialogListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            switch (whichButton) {
                                case DialogInterface.BUTTON1:
                                    phone.sendUssdResponse(inputText.getText().toString());
                                    break;
                                case DialogInterface.BUTTON2:
                                    if (mmiCode.isCancelable()) {
                                        mmiCode.cancel();
                                    }
                                    break;
                            }
                        }
                    };
                final AlertDialog newDialog = new AlertDialog.Builder(context)
                        .setMessage(text)
                        .setView(dialogView)
                        .setPositiveButton(R.string.send_button, mUSSDDialogListener)
                        .setNegativeButton(R.string.cancel, mUSSDDialogListener)
                        .setCancelable(false)
                        .create();
                final View.OnKeyListener mUSSDDialogInputListener =
                    new View.OnKeyListener() {
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            switch (keyCode) {
                                case KeyEvent.KEYCODE_CALL:
                                case KeyEvent.KEYCODE_ENTER:
                                    phone.sendUssdResponse(inputText.getText().toString());
                                    newDialog.dismiss();
                                    return true;
                            }
                            return false;
                        }
                    };
                inputText.setOnKeyListener(mUSSDDialogInputListener);
                inputText.requestFocus();
                newDialog.getWindow().setType(
                        WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
                newDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                newDialog.show();
            }
        }
    }
    static boolean cancelMmiCode(Phone phone) {
        List<? extends MmiCode> pendingMmis = phone.getPendingMmiCodes();
        int count = pendingMmis.size();
        if (DBG) log("cancelMmiCode: num pending MMIs = " + count);
        boolean canceled = false;
        if (count > 0) {
            MmiCode mmiCode = pendingMmis.get(0);
            if (mmiCode.isCancelable()) {
                mmiCode.cancel();
                canceled = true;
            }
        }
        if (mNwService != null) {
            try {
                mNwService.clearMmiString();
            } catch (RemoteException e) {
                mNwService = null;
            }
        }
        if (mMmiTimeoutCbMsg != null) {
            mMmiTimeoutCbMsg = null;
        }
        return canceled;
    }
    public static class VoiceMailNumberMissingException extends Exception {
        VoiceMailNumberMissingException() {
            super();
        }
        VoiceMailNumberMissingException(String msg) {
            super(msg);
        }
    }
    static String getNumberFromIntent(Context context, Phone phone, Intent intent)
            throws VoiceMailNumberMissingException {
        final String number = PhoneNumberUtils.getNumberFromIntent(intent, context);
        if (intent.getData().getScheme().equals("voicemail") &&
                (number == null || TextUtils.isEmpty(number)))
            throw new VoiceMailNumberMissingException();
        return number;
    }
    static CallerInfo getCallerInfo(Context context, Connection c) {
        CallerInfo info = null;
        if (c != null) {
            Object userDataObject = c.getUserData();
            if (userDataObject instanceof Uri) {
                info = CallerInfo.getCallerInfo(context, (Uri) userDataObject);
                if (info != null) {
                    c.setUserData(info);
                }
            } else {
                if (userDataObject instanceof CallerInfoToken) {
                    info = ((CallerInfoToken) userDataObject).currentInfo;
                } else {
                    info = (CallerInfo) userDataObject;
                }
                if (info == null) {
                    String number = c.getAddress();
                    if (DBG) log("getCallerInfo: number = " + number);
                    if (!TextUtils.isEmpty(number)) {
                        info = CallerInfo.getCallerInfo(context, number);
                        if (info != null) {
                            c.setUserData(info);
                        }
                    }
                }
            }
        }
        return info;
    }
    public static class CallerInfoToken {
        public boolean isFinal;
        public CallerInfo currentInfo;
        public CallerInfoAsyncQuery asyncQuery;
    }
    static CallerInfoToken startGetCallerInfo(Context context, Call call,
            CallerInfoAsyncQuery.OnQueryCompleteListener listener, Object cookie) {
        PhoneApp app = PhoneApp.getInstance();
        Connection conn = null;
        int phoneType = app.phone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            conn = call.getLatestConnection();
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            conn = call.getEarliestConnection();
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
        return startGetCallerInfo(context, conn, listener, cookie);
    }
    static CallerInfoToken startGetCallerInfo(Context context, Connection c,
            CallerInfoAsyncQuery.OnQueryCompleteListener listener, Object cookie) {
        CallerInfoToken cit;
        if (c == null) {
            cit = new CallerInfoToken();
            cit.asyncQuery = null;
            return cit;
        }
        Object userDataObject = c.getUserData();
        if (userDataObject instanceof Uri) {
            cit = new CallerInfoToken();
            cit.currentInfo = new CallerInfo();
            cit.asyncQuery = CallerInfoAsyncQuery.startQuery(QUERY_TOKEN, context,
                    (Uri) userDataObject, sCallerInfoQueryListener, c);
            cit.asyncQuery.addQueryListener(QUERY_TOKEN, listener, cookie);
            cit.isFinal = false;
            c.setUserData(cit);
            if (DBG) log("startGetCallerInfo: query based on Uri: " + userDataObject);
        } else if (userDataObject == null) {
            String number = c.getAddress();
            cit = new CallerInfoToken();
            cit.currentInfo = new CallerInfo();
            cit.currentInfo.cnapName =  c.getCnapName();
            cit.currentInfo.name = cit.currentInfo.cnapName; 
            cit.currentInfo.numberPresentation = c.getNumberPresentation();
            cit.currentInfo.namePresentation = c.getCnapNamePresentation();
            if (DBG) {
                log("startGetCallerInfo: number = " + number);
                log("startGetCallerInfo: CNAP Info from FW(1): name="
                    + cit.currentInfo.cnapName
                    + ", Name/Number Pres=" + cit.currentInfo.numberPresentation);
            }
            if (!TextUtils.isEmpty(number)) {
                number = modifyForSpecialCnapCases(context, cit.currentInfo, number,
                        cit.currentInfo.numberPresentation);
                cit.currentInfo.phoneNumber = number;
                if (cit.currentInfo.numberPresentation != Connection.PRESENTATION_ALLOWED) {
                    cit.isFinal = true;
                } else {
                    cit.asyncQuery = CallerInfoAsyncQuery.startQuery(QUERY_TOKEN, context,
                            number, sCallerInfoQueryListener, c);
                    cit.asyncQuery.addQueryListener(QUERY_TOKEN, listener, cookie);
                    cit.isFinal = false;
                }
            } else {
                if (DBG) log("startGetCallerInfo: No query to start, send trivial reply.");
                cit.isFinal = true; 
            }
            c.setUserData(cit);
            if (DBG) log("startGetCallerInfo: query based on number: " + number);
        } else if (userDataObject instanceof CallerInfoToken) {
            cit = (CallerInfoToken) userDataObject;
            if (cit.asyncQuery != null) {
                cit.asyncQuery.addQueryListener(QUERY_TOKEN, listener, cookie);
                if (DBG) log("startGetCallerInfo: query already running, adding listener: " +
                        listener.getClass().toString());
            } else {
                String updatedNumber = c.getAddress();
                if (DBG) log("startGetCallerInfo: updatedNumber initially = " + updatedNumber);
                if (!TextUtils.isEmpty(updatedNumber)) {
                    cit.currentInfo.cnapName =  c.getCnapName();
                    cit.currentInfo.name = cit.currentInfo.cnapName;
                    cit.currentInfo.numberPresentation = c.getNumberPresentation();
                    cit.currentInfo.namePresentation = c.getCnapNamePresentation();
                    updatedNumber = modifyForSpecialCnapCases(context, cit.currentInfo,
                            updatedNumber, cit.currentInfo.numberPresentation);
                    cit.currentInfo.phoneNumber = updatedNumber;
                    if (DBG) log("startGetCallerInfo: updatedNumber=" + updatedNumber);
                    if (DBG) log("startGetCallerInfo: CNAP Info from FW(2): name="
                            + cit.currentInfo.cnapName
                            + ", Name/Number Pres=" + cit.currentInfo.numberPresentation);
                    if (cit.currentInfo.numberPresentation != Connection.PRESENTATION_ALLOWED) {
                        cit.isFinal = true;
                    } else {
                        cit.asyncQuery = CallerInfoAsyncQuery.startQuery(QUERY_TOKEN, context,
                                updatedNumber, sCallerInfoQueryListener, c);
                        cit.asyncQuery.addQueryListener(QUERY_TOKEN, listener, cookie);
                        cit.isFinal = false;
                    }
                } else {
                    if (DBG) log("startGetCallerInfo: No query to attach to, send trivial reply.");
                    if (cit.currentInfo == null) {
                        cit.currentInfo = new CallerInfo();
                    }
                    cit.currentInfo.cnapName = c.getCnapName();  
                    cit.currentInfo.name = cit.currentInfo.cnapName;
                    cit.currentInfo.numberPresentation = c.getNumberPresentation();
                    cit.currentInfo.namePresentation = c.getCnapNamePresentation();
                    if (DBG) log("startGetCallerInfo: CNAP Info from FW(3): name="
                            + cit.currentInfo.cnapName
                            + ", Name/Number Pres=" + cit.currentInfo.numberPresentation);
                    cit.isFinal = true; 
                }
            }
        } else {
            cit = new CallerInfoToken();
            cit.currentInfo = (CallerInfo) userDataObject;
            cit.asyncQuery = null;
            cit.isFinal = true;
            if (DBG) log("startGetCallerInfo: query already done, returning CallerInfo");
        }
        return cit;
    }
    private static final int QUERY_TOKEN = -1;
    static CallerInfoAsyncQuery.OnQueryCompleteListener sCallerInfoQueryListener =
        new CallerInfoAsyncQuery.OnQueryCompleteListener () {
            public void onQueryComplete(int token, Object cookie, CallerInfo ci) {
                if (DBG) log("query complete, updating connection.userdata");
                Connection conn = (Connection) cookie;
                if (DBG) log("- onQueryComplete: CallerInfo:" + ci);
                if (ci.contactExists || ci.isEmergencyNumber() || ci.isVoiceMailNumber()) {
                    if (0 == ci.numberPresentation) {
                        ci.numberPresentation = conn.getNumberPresentation();
                    }
                } else {
                    CallerInfo newCi = getCallerInfo(null, conn);
                    if (newCi != null) {
                        newCi.phoneNumber = ci.phoneNumber; 
                        ci = newCi;
                    }
                }
                conn.setUserData(ci);
            }
        };
    static String getCompactNameFromCallerInfo(CallerInfo ci, Context context) {
        if (DBG) log("getCompactNameFromCallerInfo: info = " + ci);
        String compactName = null;
        if (ci != null) {
            if (TextUtils.isEmpty(ci.name)) {
                compactName = modifyForSpecialCnapCases(context, ci, ci.phoneNumber,
                                                        ci.numberPresentation);
            } else {
                compactName = ci.name;
            }
        }
        if ((compactName == null) || (TextUtils.isEmpty(compactName))) {
            if (ci != null && ci.numberPresentation == Connection.PRESENTATION_RESTRICTED) {
                compactName = context.getString(R.string.private_num);
            } else if (ci != null && ci.numberPresentation == Connection.PRESENTATION_PAYPHONE) {
                compactName = context.getString(R.string.payphone);
            } else {
                compactName = context.getString(R.string.unknown);
            }
        }
        if (DBG) log("getCompactNameFromCallerInfo: compactName=" + compactName);
        return compactName;
    }
    static boolean isConferenceCall(Call call) {
        PhoneApp app = PhoneApp.getInstance();
        int phoneType = app.phone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            CdmaPhoneCallState.PhoneCallState state = app.cdmaPhoneCallState.getCurrentCallState();
            if ((state == CdmaPhoneCallState.PhoneCallState.CONF_CALL)
                    || ((state == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE)
                    && !app.cdmaPhoneCallState.IsThreeWayCallOrigStateDialing())) {
                return true;
            }
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            List<Connection> connections = call.getConnections();
            if (connections != null && connections.size() > 1) {
                return true;
            }
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
        return false;
    }
    static void startNewCall(final Phone phone) {
        if (!okToAddCall(phone)) {
            Log.w(LOG_TAG, "startNewCall: can't add a new call in the current state");
            dumpCallState(phone);
            return;
        }
        if (!phone.getForegroundCall().isIdle()) {
            setMuteInternal(phone, true);
            PhoneApp.getInstance().setRestoreMuteOnInCallResume(true);
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ADD_CALL_MODE_KEY, true);
        PhoneApp.getInstance().startActivity(intent);
    }
    static void showIncomingCallUi() {
        if (DBG) log("showIncomingCallUi()...");
        PhoneApp app = PhoneApp.getInstance();
        try {
            ActivityManagerNative.getDefault().closeSystemDialogs("call");
        } catch (RemoteException e) {
        }
        app.preventScreenOn(true);
        app.requestWakeState(PhoneApp.WakeState.FULL);
        app.displayCallScreen();
    }
    static void turnOnSpeaker(Context context, boolean flag, boolean store) {
        if (DBG) log("turnOnSpeaker(flag=" + flag + ", store=" + store + ")...");
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(flag);
        if (store) {
            sIsSpeakerEnabled = flag;
        }
        if (flag) {
            NotificationMgr.getDefault().notifySpeakerphone();
        } else {
            NotificationMgr.getDefault().cancelSpeakerphone();
        }
        PhoneApp app = PhoneApp.getInstance();
        app.updateWakeState();
        app.updateProximitySensorMode(app.phone.getState());
    }
    static void restoreSpeakerMode(Context context) {
        if (DBG) log("restoreSpeakerMode, restoring to: " + sIsSpeakerEnabled);
        if (isSpeakerOn(context) != sIsSpeakerEnabled) {
            turnOnSpeaker(context, sIsSpeakerEnabled, false);
        }
    }
    static boolean isSpeakerOn(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isSpeakerphoneOn();
    }
    static void turnOnNoiseSuppression(Context context, boolean flag, boolean store) {
        if (DBG) log("turnOnNoiseSuppression: " + flag);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (!context.getResources().getBoolean(R.bool.has_in_call_noise_suppression)) {
            return;
        }
        if (flag) {
            audioManager.setParameters("noise_suppression=auto");
        } else {
            audioManager.setParameters("noise_suppression=off");
        }
        if (store) {
            sIsNoiseSuppressionEnabled = flag;
        }
    }
    static void restoreNoiseSuppression(Context context) {
        if (DBG) log("restoreNoiseSuppression, restoring to: " + sIsNoiseSuppressionEnabled);
        if (!context.getResources().getBoolean(R.bool.has_in_call_noise_suppression)) {
            return;
        }
        if (isNoiseSuppressionOn(context) != sIsNoiseSuppressionEnabled) {
            turnOnNoiseSuppression(context, sIsNoiseSuppressionEnabled, false);
        }
    }
    static boolean isNoiseSuppressionOn(Context context) {
        if (!context.getResources().getBoolean(R.bool.has_in_call_noise_suppression)) {
            return false;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String noiseSuppression = audioManager.getParameters("noise_suppression");
        if (DBG) log("isNoiseSuppressionOn: " + noiseSuppression);
        if (noiseSuppression.contains("off")) {
            return false;
        } else {
            return true;
        }
    }
    static void setMute(Phone phone, boolean muted) {
        setMuteInternal(phone, muted);
        for (Connection cn : phone.getForegroundCall().getConnections()) {
            if (sConnectionMuteTable.get(cn) == null) {
                if (DBG) log("problem retrieving mute value for this connection.");
            }
            sConnectionMuteTable.put(cn, Boolean.valueOf(muted));
        }
    }
    static void setMuteInternal(Phone phone, boolean muted) {
        if (DBG) log("setMute: " + muted);
        Context context = phone.getContext();
        boolean routeToAudioManager =
            context.getResources().getBoolean(R.bool.send_mic_mute_to_AudioManager);
        if (routeToAudioManager) {
            AudioManager audioManager =
                (AudioManager) phone.getContext().getSystemService(Context.AUDIO_SERVICE);
            if (DBG) log(" setMicrophoneMute: " + muted);
            audioManager.setMicrophoneMute(muted);
        } else {
            phone.setMute(muted);
        }
        if (muted) {
            NotificationMgr.getDefault().notifyMute();
        } else {
            NotificationMgr.getDefault().cancelMute();
        }
    }
    static boolean getMute(Phone phone) {
        Context context = phone.getContext();
        boolean routeToAudioManager =
            context.getResources().getBoolean(R.bool.send_mic_mute_to_AudioManager);
        if (routeToAudioManager) {
            AudioManager audioManager =
                (AudioManager) phone.getContext().getSystemService(Context.AUDIO_SERVICE);
            return audioManager.isMicrophoneMute();
        } else {
            return phone.getMute();
        }
    }
     static void setAudioMode(Context context, int mode) {
        if (DBG) Log.d(LOG_TAG, "setAudioMode(" + audioModeToString(mode) + ")...");
        boolean ignore = false;
        switch (sAudioBehaviourState) {
            case AUDIO_RINGING:
                ignore = ((mode == AudioManager.MODE_NORMAL) || (mode == AudioManager.MODE_IN_CALL));
                break;
            case AUDIO_OFFHOOK:
                ignore = ((mode == AudioManager.MODE_NORMAL) || (mode == AudioManager.MODE_RINGTONE));
                break;
            case AUDIO_IDLE:
            default:
                ignore = (mode == AudioManager.MODE_IN_CALL);
                break;
        }
        if (!ignore) {
            AudioManager audioManager =
                    (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (DBG_SETAUDIOMODE_STACK) Log.d(LOG_TAG, "Stack:", new Throwable("stack dump"));
            audioManager.setMode(mode);
        } else {
            if (DBG) Log.d(LOG_TAG, "setAudioMode(), state is " + sAudioBehaviourState +
                    " ignoring " + audioModeToString(mode) + " request");
        }
    }
    private static String audioModeToString(int mode) {
        switch (mode) {
            case AudioManager.MODE_INVALID: return "MODE_INVALID";
            case AudioManager.MODE_CURRENT: return "MODE_CURRENT";
            case AudioManager.MODE_NORMAL: return "MODE_NORMAL";
            case AudioManager.MODE_RINGTONE: return "MODE_RINGTONE";
            case AudioManager.MODE_IN_CALL: return "MODE_IN_CALL";
            default: return String.valueOf(mode);
        }
    }
     static boolean handleHeadsetHook(Phone phone, KeyEvent event) {
        if (DBG) log("handleHeadsetHook()..." + event.getAction() + " " + event.getRepeatCount());
        if (phone.getState() == Phone.State.IDLE) {
            return false;
        }
        final boolean hasRingingCall = !phone.getRingingCall().isIdle();
        final boolean hasActiveCall = !phone.getForegroundCall().isIdle();
        final boolean hasHoldingCall = !phone.getBackgroundCall().isIdle();
        if (hasRingingCall &&
            event.getRepeatCount() == 0 &&
            event.getAction() == KeyEvent.ACTION_UP) {
            int phoneType = phone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                answerCall(phone);
            } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                if (hasActiveCall && hasHoldingCall) {
                    if (DBG) log("handleHeadsetHook: ringing (both lines in use) ==> answer!");
                    answerAndEndActive(phone);
                } else {
                    if (DBG) log("handleHeadsetHook: ringing ==> answer!");
                    answerCall(phone);  
                }
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
        } else {
            if (event.isLongPress()) {
                if (DBG) log("handleHeadsetHook: longpress -> hangup");
                hangup(phone);
            }
            else if (event.getAction() == KeyEvent.ACTION_UP &&
                     event.getRepeatCount() == 0) {
                Connection c = phone.getForegroundCall().getLatestConnection();
                if (c != null && !PhoneNumberUtils.isEmergencyNumber(c.getAddress())) {
                    if (getMute(phone)) {
                        if (DBG) log("handleHeadsetHook: UNmuting...");
                        setMute(phone, false);
                    } else {
                        if (DBG) log("handleHeadsetHook: muting...");
                        setMute(phone, true);
                    }
                }
            }
        }
        return true;
    }
     static boolean hasDisconnectedConnections(Phone phone) {
        return hasDisconnectedConnections(phone.getForegroundCall()) ||
                hasDisconnectedConnections(phone.getBackgroundCall()) ||
                hasDisconnectedConnections(phone.getRingingCall());
    }
    private static final boolean hasDisconnectedConnections(Call call) {
        for (Connection c : call.getConnections()) {
            if (!c.isAlive()) {
                return true;
            }
        }
        return false;
    }
     static boolean okToSwapCalls(Phone phone) {
        int phoneType = phone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            PhoneApp app = PhoneApp.getInstance();
            return (app.cdmaPhoneCallState.getCurrentCallState()
                    == CdmaPhoneCallState.PhoneCallState.CONF_CALL);
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            return phone.getRingingCall().isIdle()
                    && (phone.getForegroundCall().getState() == Call.State.ACTIVE)
                    && (phone.getBackgroundCall().getState() == Call.State.HOLDING);
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
    }
     static boolean okToMergeCalls(Phone phone) {
        int phoneType = phone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            PhoneApp app = PhoneApp.getInstance();
            return ((app.cdmaPhoneCallState.getCurrentCallState()
                    == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE)
                    && !app.cdmaPhoneCallState.IsThreeWayCallOrigStateDialing());
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            return phone.getRingingCall().isIdle() && phone.canConference();
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
    }
     static boolean okToAddCall(Phone phone) {
        int phoneType = phone.getPhoneType();
        final Call.State fgCallState = phone.getForegroundCall().getState();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            PhoneApp app = PhoneApp.getInstance();
            return ((fgCallState == Call.State.ACTIVE)
                    && (app.cdmaPhoneCallState.getAddCallMenuStateAfterCallWaiting()));
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            final boolean hasRingingCall = !phone.getRingingCall().isIdle();
            final boolean hasActiveCall = !phone.getForegroundCall().isIdle();
            final boolean hasHoldingCall = !phone.getBackgroundCall().isIdle();
            final boolean allLinesTaken = hasActiveCall && hasHoldingCall;
            return !hasRingingCall
                    && !allLinesTaken
                    && ((fgCallState == Call.State.ACTIVE)
                        || (fgCallState == Call.State.IDLE)
                        || (fgCallState == Call.State.DISCONNECTED));
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
    }
    private static int checkCnapSpecialCases(String n) {
        if (n.equals("PRIVATE") ||
                n.equals("P") ||
                n.equals("RES")) {
            if (DBG) log("checkCnapSpecialCases, PRIVATE string: " + n);
            return Connection.PRESENTATION_RESTRICTED;
        } else if (n.equals("UNAVAILABLE") ||
                n.equals("UNKNOWN") ||
                n.equals("UNA") ||
                n.equals("U")) {
            if (DBG) log("checkCnapSpecialCases, UNKNOWN string: " + n);
            return Connection.PRESENTATION_UNKNOWN;
        } else {
            if (DBG) log("checkCnapSpecialCases, normal str. number: " + n);
            return CNAP_SPECIAL_CASE_NO;
        }
    }
     static String modifyForSpecialCnapCases(Context context, CallerInfo ci,
            String number, int presentation) {
        if (ci == null || number == null) return number;
        if (DBG) log("modifyForSpecialCnapCases: initially, number=" + number
                + ", presentation=" + presentation + " ci " + ci);
        if (number.equals(context.getString(R.string.absent_num))
                && presentation == Connection.PRESENTATION_ALLOWED) {
            number = context.getString(R.string.unknown);
            ci.numberPresentation = Connection.PRESENTATION_UNKNOWN;
        }
        if (ci.numberPresentation == Connection.PRESENTATION_ALLOWED
                || (ci.numberPresentation != presentation
                        && presentation == Connection.PRESENTATION_ALLOWED)) {
            int cnapSpecialCase = checkCnapSpecialCases(number);
            if (cnapSpecialCase != CNAP_SPECIAL_CASE_NO) {
                if (cnapSpecialCase == Connection.PRESENTATION_RESTRICTED) {
                    number = context.getString(R.string.private_num);
                } else if (cnapSpecialCase == Connection.PRESENTATION_UNKNOWN) {
                    number = context.getString(R.string.unknown);
                }
                if (DBG) log("SpecialCnap: number=" + number
                        + "; presentation now=" + cnapSpecialCase);
                ci.numberPresentation = cnapSpecialCase;
            }
        }
        if (DBG) log("modifyForSpecialCnapCases: returning number string=" + number);
        return number;
    }
     static boolean hasPhoneProviderExtras(Intent intent) {
        if (null == intent) {
            return false;
        }
        final String name = intent.getStringExtra(InCallScreen.EXTRA_GATEWAY_PROVIDER_PACKAGE);
        final String gatewayUri = intent.getStringExtra(InCallScreen.EXTRA_GATEWAY_URI);
        return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(gatewayUri);
    }
     static void checkAndCopyPhoneProviderExtras(Intent src, Intent dst) {
        if (!hasPhoneProviderExtras(src)) {
            Log.d(LOG_TAG, "checkAndCopyPhoneProviderExtras: some or all extras are missing.");
            return;
        }
        dst.putExtra(InCallScreen.EXTRA_GATEWAY_PROVIDER_PACKAGE,
                     src.getStringExtra(InCallScreen.EXTRA_GATEWAY_PROVIDER_PACKAGE));
        dst.putExtra(InCallScreen.EXTRA_GATEWAY_URI,
                     src.getStringExtra(InCallScreen.EXTRA_GATEWAY_URI));
    }
     static CharSequence getProviderLabel(Context context, Intent intent) {
        String packageName = intent.getStringExtra(InCallScreen.EXTRA_GATEWAY_PROVIDER_PACKAGE);
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(info);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
     static Drawable getProviderIcon(Context context, Intent intent) {
        String packageName = intent.getStringExtra(InCallScreen.EXTRA_GATEWAY_PROVIDER_PACKAGE);
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
     static Uri getProviderGatewayUri(Intent intent) {
        String uri = intent.getStringExtra(InCallScreen.EXTRA_GATEWAY_URI);
        return TextUtils.isEmpty(uri) ? null : Uri.parse(uri);
    }
     static String formatProviderUri(Uri uri) {
        if (null != uri) {
            if ("tel".equals(uri.getScheme())) {
                return PhoneNumberUtils.formatNumber(uri.getSchemeSpecificPart());
            } else {
                return uri.toString();
            }
        }
        return null;
    }
     static boolean isRoutableViaGateway(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        number = PhoneNumberUtils.stripSeparators(number);
        if (!number.equals(PhoneNumberUtils.convertKeypadLettersToDigits(number))) {
            return false;
        }
        number = PhoneNumberUtils.extractNetworkPortion(number);
        return PhoneNumberUtils.isGlobalPhoneNumber(number);
    }
    private static void activateSpeakerIfDocked(Phone phone) {
        if (DBG) log("activateSpeakerIfDocked()...");
        if (PhoneApp.mDockState == Intent.EXTRA_DOCK_STATE_DESK ||
                PhoneApp.mDockState == Intent.EXTRA_DOCK_STATE_CAR) {
            if (DBG) log("activateSpeakerIfDocked(): Phone in a dock -> may need to turn on speaker.");
            PhoneApp app = PhoneApp.getInstance();
            BluetoothHandsfree bthf = app.getBluetoothHandsfree();
            if (!app.isHeadsetPlugged() && !(bthf != null && bthf.isAudioOn())) {
                turnOnSpeaker(phone.getContext(), true, true);
            }
        }
    }
     static boolean isPhoneInEcm(Phone phone) {
        boolean phoneInEcm = false;
        if ((phone != null) && (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA)) {
            String ecmMode =
                    SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE);
            if (ecmMode != null) {
                phoneInEcm = ecmMode.equals("true");
            }
        }
        return phoneInEcm;
    }
     static void dumpCallState(Phone phone) {
        PhoneApp app = PhoneApp.getInstance();
        Log.d(LOG_TAG, "dumpCallState():");
        Log.d(LOG_TAG, "- Phone: " + phone + ", name = " + phone.getPhoneName()
              + ", state = " + phone.getState());
        StringBuilder b = new StringBuilder(128);
        Call call = phone.getForegroundCall();
        b.setLength(0);
        b.append("  - FG call: ").append(call.getState());
        b.append(" isAlive ").append(call.getState().isAlive());
        b.append(" isRinging ").append(call.getState().isRinging());
        b.append(" isDialing ").append(call.getState().isDialing());
        b.append(" isIdle ").append(call.isIdle());
        b.append(" hasConnections ").append(call.hasConnections());
        Log.d(LOG_TAG, b.toString());
        call = phone.getBackgroundCall();
        b.setLength(0);
        b.append("  - BG call: ").append(call.getState());
        b.append(" isAlive ").append(call.getState().isAlive());
        b.append(" isRinging ").append(call.getState().isRinging());
        b.append(" isDialing ").append(call.getState().isDialing());
        b.append(" isIdle ").append(call.isIdle());
        b.append(" hasConnections ").append(call.hasConnections());
        Log.d(LOG_TAG, b.toString());
        call = phone.getRingingCall();
        b.setLength(0);
        b.append("  - RINGING call: ").append(call.getState());
        b.append(" isAlive ").append(call.getState().isAlive());
        b.append(" isRinging ").append(call.getState().isRinging());
        b.append(" isDialing ").append(call.getState().isDialing());
        b.append(" isIdle ").append(call.isIdle());
        b.append(" hasConnections ").append(call.hasConnections());
        Log.d(LOG_TAG, b.toString());
        final boolean hasRingingCall = !phone.getRingingCall().isIdle();
        final boolean hasActiveCall = !phone.getForegroundCall().isIdle();
        final boolean hasHoldingCall = !phone.getBackgroundCall().isIdle();
        final boolean allLinesTaken = hasActiveCall && hasHoldingCall;
        b.setLength(0);
        b.append("  - hasRingingCall ").append(hasRingingCall);
        b.append(" hasActiveCall ").append(hasActiveCall);
        b.append(" hasHoldingCall ").append(hasHoldingCall);
        b.append(" allLinesTaken ").append(allLinesTaken);
        Log.d(LOG_TAG, b.toString());
        if (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            if (app.cdmaPhoneCallState != null) {
                Log.d(LOG_TAG, "  - CDMA call state: "
                      + app.cdmaPhoneCallState.getCurrentCallState());
            } else {
                Log.d(LOG_TAG, "  - CDMA device, but null cdmaPhoneCallState!");
            }
        }
        boolean ringing = app.getRinger().isRinging();
        Log.d(LOG_TAG, "  - Ringer state: " + ringing);
    }
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
