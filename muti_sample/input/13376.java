public class SnmpTableEntryNotification extends Notification {
    SnmpTableEntryNotification(String type, Object source,
                               long sequenceNumber, long timeStamp,
                               Object entry, ObjectName entryName) {
        super(type, source, sequenceNumber, timeStamp);
        this.entry = entry;
        this.name  = entryName;
    }
    public Object getEntry() {
        return entry;
    }
    public ObjectName getEntryName() {
        return name;
    }
    public static final String SNMP_ENTRY_ADDED =
        "jmx.snmp.table.entry.added";
    public static final String SNMP_ENTRY_REMOVED =
        "jmx.snmp.table.entry.removed";
    private final Object entry;
    private final ObjectName name;
    private static final long serialVersionUID = 5832592016227890252L;
}
