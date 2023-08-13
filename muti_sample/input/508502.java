public class ImpsPresenceUtils {
    private ImpsPresenceUtils() {
    }
    public static Presence extractPresence(PrimitiveElement presenceListElem,
            PresenceMapping mapping){
        int status = extractPresenceStatus(presenceListElem, mapping);
        String statusText = extractStatusText(presenceListElem);
        byte[] avatarData = extractAvatarBytes(presenceListElem);
        String avatarType = extractAvatarType(presenceListElem);
        int clientType = Presence.CLIENT_TYPE_DEFAULT;
        HashMap<String, String> clientInfo = extractClientInfo(presenceListElem);
        if (ImpsConstants.PRESENCE_MOBILE_PHONE.equals(clientInfo.get(ImpsTags.ClientType))) {
            clientType = Presence.CLIENT_TYPE_MOBILE;
        }
        return new Presence(status, statusText, avatarData, avatarType, clientType);
    }
    public static ArrayList<PrimitiveElement> buildUpdatePresenceElems(
            Presence oldPresence, Presence newPresence, PresenceMapping mapping) {
        int status = newPresence.getStatus();
        ArrayList<PrimitiveElement> elems = new ArrayList<PrimitiveElement>();
        boolean newOnlineStatus = mapping.getOnlineStatus(status);
        PrimitiveElement onlineElem = new PrimitiveElement(ImpsTags.OnlineStatus);
        onlineElem.addChild(ImpsTags.Qualifier, true);
        onlineElem.addChild(ImpsTags.PresenceValue, newOnlineStatus);
        elems.add(onlineElem);
        String newUserAvailablity = mapping.getUserAvaibility(status);
        PrimitiveElement availElem = new PrimitiveElement(ImpsTags.UserAvailability);
        availElem.addChild(ImpsTags.Qualifier, true);
        availElem.addChild(ImpsTags.PresenceValue, newUserAvailablity);
        elems.add(availElem);
        Map<String, Object> extra = mapping.getExtra(status);
        if (extra != null) {
            mapToPrimitives(extra, elems);
        }
        String statusText = newPresence.getStatusText();
        if (statusText == null) {
            statusText = "";
        }
        if (!statusText.equals(oldPresence.getStatusText())) {
            PrimitiveElement statusElem = new PrimitiveElement(ImpsTags.StatusText);
            statusElem.addChild(ImpsTags.Qualifier, true);
            statusElem.addChild(ImpsTags.PresenceValue, statusText);
            elems.add(statusElem);
        }
        byte[] avatar = newPresence.getAvatarData();
        if (avatar != null && !Arrays.equals(avatar, oldPresence.getAvatarData())) {
            String base64Avatar = Base64.encodeToString(avatar, Base64.NO_WRAP);
            PrimitiveElement statusContent = new PrimitiveElement(ImpsTags.StatusContent);
            statusContent.addChild(ImpsTags.Qualifier, true);
            statusContent.addChild(ImpsTags.DirectContent, base64Avatar);
            statusContent.addChild(ImpsTags.ContentType, newPresence.getAvatarType());
            elems.add(statusContent);
        }
        return elems;
    }
    private static int extractPresenceStatus(PrimitiveElement presenceListElem,
            PresenceMapping mapping) {
        PrimitiveElement onlineStatusElem = presenceListElem.getChild(ImpsTags.OnlineStatus);
        boolean onlineStatus = ImpsUtils.isQualifiedPresence(onlineStatusElem)
            && ImpsUtils.isTrue(onlineStatusElem.getChildContents(ImpsTags.PresenceValue));
        PrimitiveElement availabilityElem = presenceListElem.getChild(ImpsTags.UserAvailability);
        String userAvailability = ImpsUtils.isQualifiedPresence(availabilityElem) ?
                availabilityElem.getChildContents(ImpsTags.PresenceValue) : null;
        HashMap<String, Object> all = null;
        if (mapping.requireAllPresenceValues()) {
            all = new HashMap<String, Object>();
            primitivetoMap(presenceListElem, all);
        }
        return mapping.getPresenceStatus(onlineStatus, userAvailability, all);
    }
    private static void primitivetoMap(PrimitiveElement elem, HashMap<String, Object> map) {
        String key = elem.getTagName();
        int childrenCount = elem.getChildCount();
        if (childrenCount > 0) {
            HashMap<String, Object> childrenMap = new HashMap<String, Object>();
            for (PrimitiveElement child : elem.getChildren()) {
                primitivetoMap(child, childrenMap);
            }
            map.put(key, childrenMap);
        } else {
            map.put(key, elem.getContents());
        }
    }
    private static void mapToPrimitives(Map<String, Object> map, ArrayList<PrimitiveElement> elems) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String tag = entry.getKey();
            Object value = entry.getValue();
            PrimitiveElement elem = new PrimitiveElement(tag);
            if (value instanceof String) {
                elem.setContents((String)value);
            } else if (value instanceof Map) {
                mapToPrimitives((Map<String, Object>)value, elem.getChildren());
            }
            elems.add(elem);
        }
    }
    private static HashMap<String, String> extractClientInfo(PrimitiveElement presenceListElem) {
        HashMap<String, String> clientInfo = new HashMap<String, String>();
        PrimitiveElement clientInfoElem = presenceListElem.getChild(ImpsTags.ClientInfo);
        if (ImpsUtils.isQualifiedPresence(clientInfoElem)) {
            String clientType = clientInfoElem.getChildContents(ImpsTags.ClientType);
            if (clientType != null) {
                clientInfo.put(ImpsTags.ClientType, clientType);
            }
            String clientProducer = clientInfoElem.getChildContents(ImpsTags.ClientProducer);
            if (clientProducer != null) {
                clientInfo.put(ImpsTags.ClientProducer, clientProducer);
            }
            String clientVersion = clientInfoElem.getChildContents(ImpsTags.ClientVersion);
            if (clientVersion != null) {
                clientInfo.put(ImpsTags.ClientVersion, clientVersion);
            }
        }
        return clientInfo;
    }
    private static String extractStatusText(PrimitiveElement presenceListElem) {
        String statusText = null;
        PrimitiveElement statusTextElem = presenceListElem.getChild(ImpsTags.StatusText);
        if (ImpsUtils.isQualifiedPresence(statusTextElem)) {
            statusText = statusTextElem.getChildContents(ImpsTags.PresenceValue);
        }
        return statusText;
    }
    private static byte[] extractAvatarBytes(PrimitiveElement presenceListElem) {
        PrimitiveElement statusContentElem = presenceListElem.getChild(ImpsTags.StatusContent);
        if(ImpsUtils.isQualifiedPresence(statusContentElem)) {
            String avatarStr = statusContentElem.getChildContents(ImpsTags.DirectContent);
            if (avatarStr != null){
                return Base64.decode(avatarStr, Base64.DEFAULT);
            }
        }
        return null;
    }
    private static String extractAvatarType(PrimitiveElement presenceListElem) {
        PrimitiveElement statusContentElem = presenceListElem.getChild(ImpsTags.StatusContent);
        if(ImpsUtils.isQualifiedPresence(statusContentElem)) {
            return statusContentElem.getChildContents(ImpsTags.ContentType);
        }
        return null;
    }
}
