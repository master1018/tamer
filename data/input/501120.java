public class CarrierLogo {
    private CarrierLogo() {
    }
    private static Map<String, Integer> sLogoMap = null;
    private static Map<String, Integer> getLogoMap() {
        if (sLogoMap == null) {
            sLogoMap = new HashMap<String, Integer>();
        }
        return sLogoMap;
    }
    public static int getLogo(String name) {
        Integer res = getLogoMap().get(name);
        if (res != null) {
            return res.intValue();
        }
        return -1;
    }
}
