final class ImpsClientCapability {
    private ImpsClientCapability() {
    }
    public static String getClientType() {
        return "MOBILE_PHONE";
    }
    public static int getParserSize() {
        return 256 * 1024;
    }
    public static int getAcceptedContentLength() {
        return 256 * 1024;
    }
    public static int getMultiTrans() {
        return 1;
    }
    public static int getMultiTransPerMessage() {
        return 1;
    }
    public static String getInitialDeliveryMethod() {
        return "P";
    }
    public static CirMethod[] getSupportedCirMethods() {
        return new CirMethod[] {
                CirMethod.STCP,
                CirMethod.SSMS,
                CirMethod.SHTTP,
        };
    }
    public static TransportType[] getSupportedBearers() {
        return new TransportType[] {
                TransportType.HTTP
        };
    }
    public static String[] getSupportedPresenceAttribs() {
        return new String[] {
                ImpsTags.OnlineStatus,
                ImpsTags.ClientInfo,
                ImpsTags.UserAvailability,
                ImpsTags.StatusText,
                ImpsTags.StatusContent,
        };
    };
    public static String[] getBasicPresenceAttributes() {
        return new String[] {
                ImpsTags.OnlineStatus,
                ImpsTags.ClientInfo,
                ImpsTags.UserAvailability,
        };
    }
}
