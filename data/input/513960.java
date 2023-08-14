public final class EventLogParser {
    private final static String EVENT_TAG_MAP_FILE = "/system/etc/event-log-tags"; 
    private final static int EVENT_TYPE_INT      = 0;
    private final static int EVENT_TYPE_LONG     = 1;
    private final static int EVENT_TYPE_STRING   = 2;
    private final static int EVENT_TYPE_LIST     = 3;
    private final static Pattern PATTERN_SIMPLE_TAG = Pattern.compile(
    "^(\\d+)\\s+([A-Za-z0-9_]+)\\s*$"); 
    private final static Pattern PATTERN_TAG_WITH_DESC = Pattern.compile(
            "^(\\d+)\\s+([A-Za-z0-9_]+)\\s*(.*)\\s*$"); 
    private final static Pattern PATTERN_DESCRIPTION = Pattern.compile(
            "\\(([A-Za-z0-9_\\s]+)\\|(\\d+)(\\|\\d+){0,1}\\)"); 
    private final static Pattern TEXT_LOG_LINE = Pattern.compile(
            "(\\d\\d)-(\\d\\d)\\s(\\d\\d):(\\d\\d):(\\d\\d).(\\d{3})\\s+I/([a-zA-Z0-9_]+)\\s*\\(\\s*(\\d+)\\):\\s+(.*)"); 
    private final TreeMap<Integer, String> mTagMap = new TreeMap<Integer, String>();
    private final TreeMap<Integer, EventValueDescription[]> mValueDescriptionMap =
        new TreeMap<Integer, EventValueDescription[]>();
    public EventLogParser() {
    }
    public boolean init(IDevice device) {
        try {
            device.executeShellCommand("cat " + EVENT_TAG_MAP_FILE, 
                    new MultiLineReceiver() {
                @Override
                public void processNewLines(String[] lines) {
                    for (String line : lines) {
                        processTagLine(line);
                    }
                }
                public boolean isCancelled() {
                    return false;
                }
            });
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public boolean init(String[] tagFileContent) {
        for (String line : tagFileContent) {
            processTagLine(line);
        }
        return true;
    }
    public boolean init(String filePath)  {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = null;
            do {
                line = reader.readLine();
                if (line != null) {
                    processTagLine(line);
                }
            } while (line != null);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    private void processTagLine(String line) {
        if (line.length() > 0 && line.charAt(0) != '#') {
            Matcher m = PATTERN_TAG_WITH_DESC.matcher(line);
            if (m.matches()) {
                try {
                    int value = Integer.parseInt(m.group(1));
                    String name = m.group(2);
                    if (name != null && mTagMap.get(value) == null) {
                        mTagMap.put(value, name);
                    }
                    if (value == GcEventContainer.GC_EVENT_TAG) {
                        mValueDescriptionMap.put(value,
                            GcEventContainer.getValueDescriptions());
                    } else {
                        String description = m.group(3);
                        if (description != null && description.length() > 0) {
                            EventValueDescription[] desc =
                                processDescription(description);
                            if (desc != null) {
                                mValueDescriptionMap.put(value, desc);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                }
            } else {
                m = PATTERN_SIMPLE_TAG.matcher(line);
                if (m.matches()) {
                    int value = Integer.parseInt(m.group(1));
                    String name = m.group(2);
                    if (name != null && mTagMap.get(value) == null) {
                        mTagMap.put(value, name);
                    }
                }
            }
        }
    }
    private EventValueDescription[] processDescription(String description) {
        String[] descriptions = description.split("\\s*,\\s*"); 
        ArrayList<EventValueDescription> list = new ArrayList<EventValueDescription>();
        for (String desc : descriptions) {
            Matcher m = PATTERN_DESCRIPTION.matcher(desc);
            if (m.matches()) {
                try {
                    String name = m.group(1);
                    String typeString = m.group(2);
                    int typeValue = Integer.parseInt(typeString);
                    EventValueType eventValueType = EventValueType.getEventValueType(typeValue);
                    if (eventValueType == null) {
                    }
                    typeString = m.group(3);
                    if (typeString != null && typeString.length() > 0) {
                        typeString = typeString.substring(1);
                        typeValue = Integer.parseInt(typeString);
                        ValueType valueType = ValueType.getValueType(typeValue);
                        list.add(new EventValueDescription(name, eventValueType, valueType));
                    } else {
                        list.add(new EventValueDescription(name, eventValueType));
                    }
                } catch (NumberFormatException nfe) {
                } catch (InvalidValueTypeException e) {
                }
            } else {
                Log.e("EventLogParser",  
                    String.format("Can't parse %1$s", description));  
            }
        }
        if (list.size() == 0) {
            return null;
        }
        return list.toArray(new EventValueDescription[list.size()]);
    }
    public EventContainer parse(LogEntry entry) {
        if (entry.len < 4) {
            return null;
        }
        int inOffset = 0;
        int tagValue = ArrayHelper.swap32bitFromArray(entry.data, inOffset);
        inOffset += 4;
        String tag = mTagMap.get(tagValue);
        if (tag == null) {
            Log.e("EventLogParser", String.format("unknown tag number: %1$d", tagValue));
        }
        ArrayList<Object> list = new ArrayList<Object>();
        if (parseBinaryEvent(entry.data, inOffset, list) == -1) {
            return null;
        }
        Object data;
        if (list.size() == 1) {
            data = list.get(0);
        } else{
            data = list.toArray();
        }
        EventContainer event = null;
        if (tagValue == GcEventContainer.GC_EVENT_TAG) {
            event = new GcEventContainer(entry, tagValue, data);
        } else {
            event = new EventContainer(entry, tagValue, data);
        }
        return event;
    }
    public EventContainer parse(String textLogLine) {
        if (textLogLine.length() == 0) {
            return null;
        }
        Matcher m = TEXT_LOG_LINE.matcher(textLogLine);
        if (m.matches()) {
            try {
                int month = Integer.parseInt(m.group(1));
                int day = Integer.parseInt(m.group(2));
                int hours = Integer.parseInt(m.group(3));
                int minutes = Integer.parseInt(m.group(4));
                int seconds = Integer.parseInt(m.group(5));
                int milliseconds = Integer.parseInt(m.group(6));
                Calendar cal = Calendar.getInstance();
                cal.set(cal.get(Calendar.YEAR), month-1, day, hours, minutes, seconds);
                int sec = (int)Math.floor(cal.getTimeInMillis()/1000);
                int nsec = milliseconds * 1000000;
                String tag = m.group(7);
                int tagValue = -1;
                Set<Entry<Integer, String>> tagSet = mTagMap.entrySet();
                for (Entry<Integer, String> entry : tagSet) {
                    if (tag.equals(entry.getValue())) {
                        tagValue = entry.getKey();
                        break;
                    }
                }
                if (tagValue == -1) {
                    return null;
                }
                int pid = Integer.parseInt(m.group(8));
                Object data = parseTextData(m.group(9), tagValue);
                if (data == null) {
                    return null;
                }
                EventContainer event = null;
                if (tagValue == GcEventContainer.GC_EVENT_TAG) {
                    event = new GcEventContainer(tagValue, pid, -1 , sec, nsec, data);
                } else {
                    event = new EventContainer(tagValue, pid, -1 , sec, nsec, data);
                }
                return event;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    public Map<Integer, String> getTagMap() {
        return mTagMap;
    }
    public Map<Integer, EventValueDescription[]> getEventInfoMap() {
        return mValueDescriptionMap;
    }
    private static int parseBinaryEvent(byte[] eventData, int dataOffset, ArrayList<Object> list) {
        if (eventData.length - dataOffset < 1)
            return -1;
        int offset = dataOffset;
        int type = eventData[offset++];
        switch (type) {
        case EVENT_TYPE_INT: { 
                int ival;
                if (eventData.length - offset < 4)
                    return -1;
                ival = ArrayHelper.swap32bitFromArray(eventData, offset);
                offset += 4;
                list.add(new Integer(ival));
            }
            break;
        case EVENT_TYPE_LONG: { 
                long lval;
                if (eventData.length - offset < 8)
                    return -1;
                lval = ArrayHelper.swap64bitFromArray(eventData, offset);
                offset += 8;
                list.add(new Long(lval));
            }
            break;
        case EVENT_TYPE_STRING: { 
                int strLen;
                if (eventData.length - offset < 4)
                    return -1;
                strLen = ArrayHelper.swap32bitFromArray(eventData, offset);
                offset += 4;
                if (eventData.length - offset < strLen)
                    return -1;
                try {
                    String str = new String(eventData, offset, strLen, "UTF-8"); 
                    list.add(str);
                } catch (UnsupportedEncodingException e) {
                }
                offset += strLen;
                break;
            }
        case EVENT_TYPE_LIST: { 
                if (eventData.length - offset < 1)
                    return -1;
                int count = eventData[offset++];
                ArrayList<Object> subList = new ArrayList<Object>();
                for (int i = 0; i < count; i++) {
                    int result = parseBinaryEvent(eventData, offset, subList);
                    if (result == -1) {
                        return result;
                    }
                    offset += result;
                }
                list.add(subList.toArray());
            }
            break;
        default:
            Log.e("EventLogParser",  
                    String.format("Unknown binary event type %1$d", type));  
            return -1;
        }
        return offset - dataOffset;
    }
    private Object parseTextData(String data, int tagValue) {
        EventValueDescription[] desc = mValueDescriptionMap.get(tagValue);
        if (desc == null) {
            return null;
        }
        if (desc.length == 1) {
            return getObjectFromString(data, desc[0].getEventValueType());
        } else if (data.startsWith("[") && data.endsWith("]")) {
            data = data.substring(1, data.length() - 1);
            String[] values = data.split(",");
            if (tagValue == GcEventContainer.GC_EVENT_TAG) {
                Object[] objects = new Object[2];
                objects[0] = getObjectFromString(values[0], EventValueType.LONG);
                objects[1] = getObjectFromString(values[1], EventValueType.LONG);
                return objects;
            } else {
                if (values.length != desc.length) {
                    return null;
                }
                Object[] objects = new Object[values.length];
                for (int i = 0 ; i < desc.length ; i++) {
                    Object obj = getObjectFromString(values[i], desc[i].getEventValueType());
                    if (obj == null) {
                        return null;
                    }
                    objects[i] = obj;
                }
                return objects;
            }
        }
        return null;
    }
    private Object getObjectFromString(String value, EventValueType type) {
        try {
            switch (type) {
                case INT:
                    return Integer.valueOf(value);
                case LONG:
                    return Long.valueOf(value);
                case STRING:
                    return value;
            }
        } catch (NumberFormatException e) {
        }
        return null;
    }
    public void saveTags(String filePath) throws IOException {
        File destFile = new File(filePath);
        destFile.createNewFile();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destFile);
            for (Integer key : mTagMap.keySet()) {
                String tagName = mTagMap.get(key);
                EventValueDescription[] descriptors = mValueDescriptionMap.get(key);
                String line = null;
                if (descriptors != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%1$d %2$s", key, tagName)); 
                    boolean first = true;
                    for (EventValueDescription evd : descriptors) {
                        if (first) {
                            sb.append(" ("); 
                            first = false;
                        } else {
                            sb.append(",("); 
                        }
                        sb.append(evd.getName());
                        sb.append("|"); 
                        sb.append(evd.getEventValueType().getValue());
                        sb.append("|"); 
                        sb.append(evd.getValueType().getValue());
                        sb.append("|)"); 
                    }
                    sb.append("\n"); 
                    line = sb.toString();
                } else {
                    line = String.format("%1$d %2$s\n", key, tagName); 
                }
                byte[] buffer = line.getBytes();
                fos.write(buffer);
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
}
