public class DemoPresenceMapping implements PresenceMapping {
    public int[] getSupportedPresenceStatus() {
        return new int[] {
                ImPluginConstants.PRESENCE_AVAILABLE,
                ImPluginConstants.PRESENCE_DO_NOT_DISTURB,
                ImPluginConstants.PRESENCE_OFFLINE
        };
    }
    public boolean getOnlineStatus(int status) {
        return status != ImPluginConstants.PRESENCE_OFFLINE;
    }
    public String getUserAvaibility(int status) {
        switch (status) {
            case ImPluginConstants.PRESENCE_AVAILABLE:
                return ImPluginConstants.PA_AVAILABLE;
            case ImPluginConstants.PRESENCE_DO_NOT_DISTURB:
                return ImPluginConstants.PA_DISCREET;
            case ImPluginConstants.PRESENCE_OFFLINE:
                return ImPluginConstants.PA_NOT_AVAILABLE;
            default:
                return null;
        }
    }
    public Map<String, Object> getExtra(int status) {
        return null;
    }
    public boolean requireAllPresenceValues() {
        return false;
    }
    public int getPresenceStatus(boolean onlineStatus, String userAvailability,
            Map allValues) {
        if (!onlineStatus) {
            return ImPluginConstants.PRESENCE_OFFLINE;
        }
        if (ImPluginConstants.PA_NOT_AVAILABLE.equals(userAvailability)) {
            return ImPluginConstants.PRESENCE_AWAY;
        } else if (ImPluginConstants.PA_DISCREET.equals(userAvailability)) {
            return ImPluginConstants.PRESENCE_DO_NOT_DISTURB;
        } else {
            return ImPluginConstants.PRESENCE_AVAILABLE;
        }
    }
}
