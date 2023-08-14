public class CK_TOKEN_INFO {
    public char[] label;           
    public char[] manufacturerID;  
    public char[] model;           
    public char[] serialNumber;    
    public long flags;               
    public long ulMaxSessionCount;     
    public long ulSessionCount;        
    public long ulMaxRwSessionCount;   
    public long ulRwSessionCount;      
    public long ulMaxPinLen;           
    public long ulMinPinLen;           
    public long ulTotalPublicMemory;   
    public long ulFreePublicMemory;    
    public long ulTotalPrivateMemory;  
    public long ulFreePrivateMemory;   
    public CK_VERSION    hardwareVersion;       
    public CK_VERSION    firmwareVersion;       
    public char[] utcTime;           
    public CK_TOKEN_INFO(char[] label, char[] vendor, char[] model,
                         char[] serialNo, long flags,
                         long sessionMax, long session,
                         long rwSessionMax, long rwSession,
                         long pinLenMax, long pinLenMin,
                         long totalPubMem, long freePubMem,
                         long totalPrivMem, long freePrivMem,
                         CK_VERSION hwVer, CK_VERSION fwVer, char[] utcTime) {
        this.label = label;
        this.manufacturerID = vendor;
        this.model = model;
        this.serialNumber = serialNo;
        this.flags = flags;
        this.ulMaxSessionCount = sessionMax;
        this.ulSessionCount = session;
        this.ulMaxRwSessionCount = rwSessionMax;
        this.ulRwSessionCount = rwSession;
        this.ulMaxPinLen = pinLenMax;
        this.ulMinPinLen = pinLenMin;
        this.ulTotalPublicMemory = totalPubMem;
        this.ulFreePublicMemory = freePubMem;
        this.ulTotalPrivateMemory = totalPrivMem;
        this.ulFreePrivateMemory = freePrivMem;
        this.hardwareVersion = hwVer;
        this.firmwareVersion = fwVer;
        this.utcTime = utcTime;
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(Constants.INDENT);
        buffer.append("label: ");
        buffer.append(new String(label));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("manufacturerID: ");
        buffer.append(new String(manufacturerID));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("model: ");
        buffer.append(new String(model));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("serialNumber: ");
        buffer.append(new String(serialNumber));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("flags: ");
        buffer.append(Functions.tokenInfoFlagsToString(flags));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulMaxSessionCount: ");
        buffer.append((ulMaxSessionCount == PKCS11Constants.CK_EFFECTIVELY_INFINITE)
                  ? "CK_EFFECTIVELY_INFINITE"
                  : (ulMaxSessionCount == PKCS11Constants.CK_UNAVAILABLE_INFORMATION)
                    ? "CK_UNAVAILABLE_INFORMATION"
                    : String.valueOf(ulMaxSessionCount));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulSessionCount: ");
        buffer.append((ulSessionCount == PKCS11Constants.CK_UNAVAILABLE_INFORMATION)
                  ? "CK_UNAVAILABLE_INFORMATION"
                  : String.valueOf(ulSessionCount));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulMaxRwSessionCount: ");
        buffer.append((ulMaxRwSessionCount == PKCS11Constants.CK_EFFECTIVELY_INFINITE)
                  ? "CK_EFFECTIVELY_INFINITE"
                  : (ulMaxRwSessionCount == PKCS11Constants.CK_UNAVAILABLE_INFORMATION)
                    ? "CK_UNAVAILABLE_INFORMATION"
                    : String.valueOf(ulMaxRwSessionCount));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulRwSessionCount: ");
        buffer.append((ulRwSessionCount == PKCS11Constants.CK_UNAVAILABLE_INFORMATION)
                  ? "CK_UNAVAILABLE_INFORMATION"
                  : String.valueOf(ulRwSessionCount));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulMaxPinLen: ");
        buffer.append(String.valueOf(ulMaxPinLen));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulMinPinLen: ");
        buffer.append(String.valueOf(ulMinPinLen));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulTotalPublicMemory: ");
        buffer.append((ulTotalPublicMemory == PKCS11Constants.CK_UNAVAILABLE_INFORMATION)
                  ? "CK_UNAVAILABLE_INFORMATION"
                  : String.valueOf(ulTotalPublicMemory));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulFreePublicMemory: ");
        buffer.append((ulFreePublicMemory == PKCS11Constants.CK_UNAVAILABLE_INFORMATION)
                  ? "CK_UNAVAILABLE_INFORMATION"
                  : String.valueOf(ulFreePublicMemory));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulTotalPrivateMemory: ");
        buffer.append((ulTotalPrivateMemory == PKCS11Constants.CK_UNAVAILABLE_INFORMATION)
                  ? "CK_UNAVAILABLE_INFORMATION"
                  : String.valueOf(ulTotalPrivateMemory));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("ulFreePrivateMemory: ");
        buffer.append((ulFreePrivateMemory == PKCS11Constants.CK_UNAVAILABLE_INFORMATION)
                  ? "CK_UNAVAILABLE_INFORMATION"
                  : String.valueOf(ulFreePrivateMemory));
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("hardwareVersion: ");
        buffer.append(hardwareVersion.toString());
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("firmwareVersion: ");
        buffer.append(firmwareVersion.toString());
        buffer.append(Constants.NEWLINE);
        buffer.append(Constants.INDENT);
        buffer.append("utcTime: ");
        buffer.append(new String(utcTime));
        return buffer.toString() ;
    }
}
