public class DefaultPresenceMapping implements PresenceMapping {
    public Map<String, Object> getExtra(int status) {
        return null;
    }
    public boolean getOnlineStatus(int status) {
        return status != ImPluginConstants.PRESENCE_OFFLINE;
    }
    public int getPresenceStatus(boolean onlineStatus, String userAvailability,
            Map<String, Object> allValues) {
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
    public int[] getSupportedPresenceStatus() {
        return new int[] {
                ImPluginConstants.PRESENCE_AVAILABLE,
                ImPluginConstants.PRESENCE_DO_NOT_DISTURB,
                ImPluginConstants.PRESENCE_AWAY
        };
    }
    public String getUserAvaibility(int status) {
        switch (status) {
            case ImPluginConstants.PRESENCE_AVAILABLE:
                return ImPluginConstants.PA_AVAILABLE;
            case ImPluginConstants.PRESENCE_AWAY:
                return ImPluginConstants.PA_NOT_AVAILABLE;
            case ImPluginConstants.PRESENCE_DO_NOT_DISTURB:
                return ImPluginConstants.PA_DISCREET;
            default:
                return null;
        }
    }
    public boolean requireAllPresenceValues() {
        return false;
    }
}
