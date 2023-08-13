public class PtsCodes {
    private PtsCodes() {
    }
    private static HashMap<String, String> sCodeToTransaction = new HashMap<String, String>();
    private static HashMap<String, String> sCodeToElement = new HashMap<String, String>();
    private static HashMap<String, String> sCodeToCapElement = new HashMap<String, String>();
    private static HashMap<String, String> sCodeToCapValue= new HashMap<String, String>();
    private static HashMap<String, String> sCodeToServiceTree = new HashMap<String, String>();
    private static HashMap<String, String> sCodeToPresenceAttribute = new HashMap<String, String>();
    private static HashMap<String, String> sTransactionToCode = new HashMap<String, String>();
    private static HashMap<String, String> sElementToCode = new HashMap<String, String>();
     static HashMap<String, String> sCapElementToCode = new HashMap<String, String>();
     static HashMap<String, String> sCapValueToCode= new HashMap<String, String>();
    private static HashMap<String, String> sServiceTreeToCode = new HashMap<String, String>();
    static HashMap<String, String> sContactListPropsToCode = new HashMap<String, String>();
    private static HashMap<String, String> sPresenceAttributeToCode = new HashMap<String, String>();
    private static ArrayList<String> sServerRequestTransactionCode = new ArrayList<String>();
    private static HashMap<String, String> sCodeToPAValue = new HashMap<String, String>();
    private static HashMap<String, String> sPAValueToCode = new HashMap<String, String>();
    public static String getTransaction(String type) {
        return sCodeToTransaction.get(type.toUpperCase());
    }
    public static String getElement(String type) {
        return sCodeToElement.get(type.toUpperCase());
    }
    public static String getCapElement(String type) {
        return sCodeToCapElement.get(type.toUpperCase());
    }
    public static String getCapValue(String type) {
        return sCodeToCapValue.get(type.toUpperCase()) == null ?
                type : sCodeToCapValue.get(type.toUpperCase());
    }
    public static String getServiceTreeValue(String type) {
        return sCodeToServiceTree.get(type.toUpperCase());
    }
    public static String getTxCode(String txType) {
        return sTransactionToCode.get(txType);
    }
    public static String getElementCode(String elemType, String transactionType) {
        if (ImpsTags.PresenceSubList.equals(elemType)) {
            return ImpsTags.UpdatePresence_Request.equals(transactionType) ? "UV" : "PS";
        }
        return sElementToCode.get(elemType);
    }
    public static String getCapElementCode(String elemType) {
        return sCapElementToCode.get(elemType);
    }
    public static String getCapValueCode(String value) {
        return sCapValueToCode.get(value);
    }
    public static String getServiceTreeCode(String elemType) {
        return sServiceTreeToCode.get(elemType);
    }
    public static String getPresenceAttributeCode(String elemType){
        return sPresenceAttributeToCode.get(elemType);
    }
    public static String getPresenceAttributeElement(String code) {
        return sCodeToPresenceAttribute.get(code.toUpperCase());
    }
    public static String getPAValue(String code) {
        String value = sCodeToPAValue.get(code.toUpperCase());
        return value == null ? code : value;
    }
    public static String getPAValueCode(String value) {
        String code = sPAValueToCode.get(value.toUpperCase());
        return code == null ? value : code;
    }
    public static boolean isServerRequestCode(String type) {
        for (String code : sServerRequestTransactionCode) {
            if (code.equals(type)) {
                return true;
            }
        }
        return false;
    }
    public static final String ClientCapability_Response    = "PC";
    public static final String Disconnect                   = "DI";
    public static final String GetList_Response             = "LG";
    public static final String KeepAlive_Response           = "AK";
    public static final String Login_Response               = "RL";
    public static final String Service_Response             = "QS";
    public static final String Status                       = "ST";   
    public static final String ListManage_Response          = "ML";
    public static final String PresenceNotification         = "PN";
    public static final String GetPresence_Response         = "PG";
    public static final String AllFunctions          = "AF";
    public static final String CapabilityList        = "CA";
    public static final String AgreedCapabilityList  = "AP";
    public static final String CapabilityRequest     = "CR";
    public static final String ClientID              = "CI";
    public static final String ContactList           = "CL";
    public static final String DefaultContactList    = "DC";
    public static final String DigestSchema          = "DI";
    public static final String Nonce                 = "NO";
    public static final String NotAvailableFunctions = "NF";
    public static final String KeepAliveTime         = "KA";
    public static final String SessionID             = "SI";
    public static final String DefaultList           = "DL";
    public static final String UserNickList          = "UN";
    public static final String ContactListProps      = "CP";
    public static final String Presence              = "PR";
    public static final String DisplayName = "DN";
    public static final String Default     = "DE";
    static {
        sTransactionToCode.put(ImpsTags.Login_Request, "LR");
        sTransactionToCode.put(ImpsTags.ClientCapability_Request, "CP");
        sTransactionToCode.put(ImpsTags.Service_Request, "SQ");
        sTransactionToCode.put(ImpsTags.Logout_Request, "OR");
        sTransactionToCode.put(ImpsTags.Status, "ST");
        sTransactionToCode.put(ImpsTags.GetList_Request, "GL");
        sTransactionToCode.put(ImpsTags.CreateList_Request, "CL");
        sTransactionToCode.put(ImpsTags.DeleteList_Request, "DL");
        sTransactionToCode.put(ImpsTags.ListManage_Request, "LM");
        sTransactionToCode.put(ImpsTags.GetPresence_Request, "GP");
        sTransactionToCode.put(ImpsTags.SubscribePresence_Request, "SB");
        sTransactionToCode.put(ImpsTags.GetBlockedList_Request, "GB");
        sTransactionToCode.put(ImpsTags.CreateAttributeList_Request, "CA");
        sTransactionToCode.put(ImpsTags.Polling_Request, "PO");
        sTransactionToCode.put(ImpsTags.UpdatePresence_Request, "UP");
        sElementToCode.put(ImpsTags.ClientID, ClientID);
        sElementToCode.put(ImpsTags.DigestSchema, "SH");
        sElementToCode.put(ImpsTags.DigestBytes, "DB");
        sElementToCode.put(ImpsTags.Password, "PW");
        sElementToCode.put(ImpsTags.SessionCookie, "SC");
        sElementToCode.put(ImpsTags.TimeToLive, "TL");
        sElementToCode.put(ImpsTags.UserID, "UI");
        sElementToCode.put(ImpsTags.CapabilityList, CapabilityList);
        sElementToCode.put(ImpsTags.Functions, "RF");
        sElementToCode.put(ImpsTags.AllFunctionsRequest, "AR");
        sElementToCode.put(ImpsTags.Result, "ST");
        sElementToCode.put(ImpsTags.ContactList, "CL");
        sElementToCode.put(ImpsTags.NickList, "UN");
        sElementToCode.put(ImpsTags.AddNickList, "AN");
        sElementToCode.put(ImpsTags.RemoveNickList, "RN");
        sElementToCode.put(ImpsTags.ContactListProperties, "CP");
        sElementToCode.put(ImpsTags.ReceiveList, "RL");
        sElementToCode.put(ImpsTags.PresenceSubList, "PS");
        sElementToCode.put(ImpsTags.DefaultList, DefaultList);
        sElementToCode.put(ImpsTags.AutoSubscribe, "AS");
        sCodeToTransaction.put(ClientCapability_Response, ImpsTags.ClientCapability_Response);
        sCodeToTransaction.put(Disconnect, ImpsTags.Disconnect);
        sCodeToTransaction.put(GetList_Response, ImpsTags.GetList_Response);
        sCodeToTransaction.put(KeepAlive_Response, ImpsTags.KeepAlive_Response);
        sCodeToTransaction.put(Login_Response, ImpsTags.Login_Response);
        sCodeToTransaction.put(Service_Response, ImpsTags.Service_Response);
        sCodeToTransaction.put(Status, ImpsTags.Status);
        sCodeToTransaction.put(ListManage_Response, ImpsTags.ListManage_Response);
        sCodeToTransaction.put(PresenceNotification, ImpsTags.PresenceNotification_Request);
        sCodeToTransaction.put(GetPresence_Response, ImpsTags.GetPresence_Response);
        sCodeToElement.put(AllFunctions, ImpsTags.AllFunctions);
        sCodeToElement.put(CapabilityList, ImpsTags.CapabilityList);
        sCodeToElement.put(CapabilityRequest, ImpsTags.CapabilityRequest);
        sCodeToElement.put(ClientID, ImpsTags.ClientID);
        sCodeToElement.put(ContactList, ImpsTags.ContactList);
        sCodeToElement.put(DigestSchema, ImpsTags.DigestSchema);
        sCodeToElement.put(Nonce, ImpsTags.Nonce);
        sCodeToElement.put(NotAvailableFunctions, ImpsTags.NotAvailableFunctions);
        sCodeToElement.put(KeepAliveTime, ImpsTags.KeepAliveTime);
        sCodeToElement.put(SessionID, ImpsTags.SessionID);
        sCodeToElement.put(Status, ImpsTags.Status);
        sCodeToElement.put(DefaultList, ImpsTags.DefaultContactList);
        sCodeToElement.put(UserNickList, ImpsTags.NickList);
        sCodeToElement.put(Presence, ImpsTags.Presence);
        sCodeToCapElement.put("CT", ImpsTags.ClientType);
        sCodeToCapElement.put("CI", ImpsTags.CIRHTTPAddress);
        sCodeToCapElement.put("DL", ImpsTags.DefaultLanguage);
        sCodeToCapElement.put("ID", ImpsTags.InitialDeliveryMethod);
        sCodeToCapElement.put("MT", ImpsTags.MultiTrans);
        sCodeToCapElement.put("PS", ImpsTags.ParserSize);
        sCodeToCapElement.put("PM", ImpsTags.ServerPollMin);
        sCodeToCapElement.put("SB", ImpsTags.SupportedBearer);
        sCodeToCapElement.put("SC", ImpsTags.SupportedCIRMethod);
        sCodeToCapElement.put("TA", ImpsTags.TCPAddress);
        sCodeToCapElement.put("TP", ImpsTags.TCPPort);
        sCodeToCapElement.put("UP", ImpsTags.UDPPort);
        for (Entry<String, String> e : sCodeToCapElement.entrySet()) {
            sCapElementToCode.put(e.getValue(), e.getKey());
        }
        sCodeToCapValue.put("SS", "SSMS");
        sCodeToCapValue.put("ST", "STCP");
        sCodeToCapValue.put("SU", "SUDP");
        sCodeToCapValue.put("WS", "WAPSMS");
        sCodeToCapValue.put("WU", "WAPUDP");
        sCodeToCapValue.put("MP", ImpsConstants.PRESENCE_MOBILE_PHONE);
        sCodeToCapValue.put("CO", ImpsConstants.PRESENCE_COMPUTER);
        sCodeToCapValue.put("PD", ImpsConstants.PRESENCE_PDA);
        sCodeToCapValue.put("CL", ImpsConstants.PRESENCE_CLI);
        sCodeToCapValue.put("OT", ImpsConstants.PRESENCE_OTHER);
        for (Entry<String, String> e : sCodeToCapValue.entrySet()) {
            sCapValueToCode.put(e.getValue(), e.getKey());
        }
        sCodeToServiceTree.put("WV", ImpsTags.WVCSPFeat);
        sCodeToServiceTree.put("FF", ImpsTags.FundamentalFeat);
        sCodeToServiceTree.put("PF", ImpsTags.PresenceFeat);
        sCodeToServiceTree.put("IF", ImpsTags.IMFeat);
        sCodeToServiceTree.put("GE", ImpsTags.GroupFeat);
        for (Entry<String, String> e : sCodeToServiceTree.entrySet()) {
            sServiceTreeToCode.put(e.getValue(), e.getKey());
        }
        sContactListPropsToCode.put(ImpsConstants.DisplayName, "DN");
        sContactListPropsToCode.put(ImpsConstants.Default, "DE");
        sCodeToPresenceAttribute.put("OS", ImpsTags.OnlineStatus);
        sCodeToPresenceAttribute.put("CF", ImpsTags.ClientInfo);
        sCodeToPresenceAttribute.put("UA", ImpsTags.UserAvailability);
        sCodeToPresenceAttribute.put("ST", ImpsTags.StatusText);
        sCodeToPresenceAttribute.put("SC", ImpsTags.StatusContent);
        sCodeToPresenceAttribute.put("CT", ImpsTags.ClientType);
        sCodeToPresenceAttribute.put("CP", ImpsTags.ClientProducer);
        sCodeToPresenceAttribute.put("CV", ImpsTags.ClientVersion);
        sCodeToPresenceAttribute.put("RC", ImpsTags.ReferredContent);
        sCodeToPresenceAttribute.put("CY", ImpsTags.ContentType);
        for (Entry<String, String> e : sCodeToPresenceAttribute.entrySet()) {
            sPresenceAttributeToCode.put(e.getValue(), e.getKey());
        }
        sServerRequestTransactionCode.add(PresenceNotification);
        sCodeToPAValue.put("AV", ImpsConstants.PRESENCE_AVAILABLE);
        sCodeToPAValue.put("DI", ImpsConstants.PRESENCE_DISCREET);
        sCodeToPAValue.put("NA", ImpsConstants.PRESENCE_NOT_AVAILABLE);
        sCodeToPAValue.put("MP", ImpsConstants.PRESENCE_MOBILE_PHONE);
        sCodeToPAValue.put("CO", ImpsConstants.PRESENCE_COMPUTER);
        sCodeToPAValue.put("PD", ImpsConstants.PRESENCE_PDA);
        sCodeToPAValue.put("CL", ImpsConstants.PRESENCE_CLI);
        sCodeToPAValue.put("OT", ImpsConstants.PRESENCE_OTHER);
        for (Entry<String, String> e : sCodeToPAValue.entrySet()) {
            sPAValueToCode.put(e.getValue(), e.getKey());
        }
    }
}
