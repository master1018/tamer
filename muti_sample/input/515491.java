final class GetPropReceiver extends MultiLineReceiver {
    final static String GETPROP_COMMAND = "getprop"; 
    private final static Pattern GETPROP_PATTERN = Pattern.compile("^\\[([^]]+)\\]\\:\\s*\\[(.*)\\]$"); 
    private Device mDevice = null;
    public GetPropReceiver(Device device) {
        mDevice = device;
    }
    @Override
    public void processNewLines(String[] lines) {
        for (String line : lines) {
            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            Matcher m = GETPROP_PATTERN.matcher(line);
            if (m.matches()) {
                String label = m.group(1);
                String value = m.group(2);
                if (label.length() > 0) {
                    mDevice.addProperty(label, value);
                }
            }
        }
    }
    public boolean isCancelled() {
        return false;
    }
    @Override
    public void done() {
        mDevice.update(Device.CHANGE_BUILD_INFO);
    }
}
