public abstract class CellLocation {
    public static void requestLocationUpdate() {
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
            if (phone != null) {
                phone.updateServiceLocation();
            }
        } catch (RemoteException ex) {
        }
    }
    public static CellLocation newFromBundle(Bundle bundle) {
        switch(TelephonyManager.getDefault().getPhoneType()) {
        case Phone.PHONE_TYPE_CDMA:
            return new CdmaCellLocation(bundle);
        case Phone.PHONE_TYPE_GSM:
            return new GsmCellLocation(bundle);
        default:
            return null;
        }
    }
    public abstract void fillInNotifierBundle(Bundle bundle);
    public abstract boolean isEmpty();
    public static CellLocation getEmpty() {
        switch(TelephonyManager.getDefault().getPhoneType()) {
        case Phone.PHONE_TYPE_CDMA:
            return new CdmaCellLocation();
        case Phone.PHONE_TYPE_GSM:
            return new GsmCellLocation();
        default:
            return null;
        }
    }
}
