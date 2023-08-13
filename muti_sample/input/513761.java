public class Track {
    private ArrayList<MidiEvent> events; 
    private MidiEvent badEvent; 
    private long tick; 
    Track() {
        events = new ArrayList<MidiEvent>();
        events.add(new MidiEvent(new MetaMessage(new byte[] {-1, 47, 0}), 0));
    }
    public boolean add(MidiEvent event) {
        if (event == null || event == badEvent) {
            return false;
        }
        if (event.getMessage().getMessage()[0] == -1 &&
                event.getMessage().getMessage()[1] == 47 &&
                event.getMessage().getMessage()[2] == 0 ) {
            if (events.size() == 0) {
                return events.add(event);
            }
            byte[] bt = events.get(events.size() - 1).getMessage().getMessage();
            if ((bt[0] != -1) && (bt[1] != 47) && (bt[2] != 0)) {
                return events.add(event);
            }         
            return true;
        }
        if (events.size() == 0) {
            events.add(new MidiEvent(new MetaMessage(new byte[] {-1, 47, 0}), 0));
            badEvent = event;
            throw new ArrayIndexOutOfBoundsException(Messages.getString("sound.01")); 
        }
        byte[] bt = events.get(events.size() - 1).getMessage().getMessage();
        if ((bt[0] != -1) && (bt[1] != 47) && (bt[2] != 0)) {
            events.add(new MidiEvent(new MetaMessage(new byte[] {-1, 47, 0}), 0));
        }
        if (events.contains(event)) {
            return false;
        } 
        if (events.size() == 1) {
            events.add(0, event);
        }
        for (int i = 0; i < events.size() - 1; i++ ) {
            if (events.get(i).getTick() <= event.getTick()) {
                continue;
            }
            events.add(i, event);
            break;
        }
        if (tick < event.getTick()) {
            tick = event.getTick();
        }
        return true;           
    }
    public MidiEvent get(int index) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= events.size()) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("sound.02", index, events.size()));  
        }
        return events.get(index);
    }
    public boolean remove(MidiEvent event) {
        if (event == badEvent) {
            badEvent = null;
            return false;
        }
        if (events.remove(event)) {
            if (events.size() == 0) {
                tick = 0;
            }
            return true;
        }
        return false;
    }
    public int size() {
        return events.size();
    }
    public long ticks() {
        return tick;
    }
}
