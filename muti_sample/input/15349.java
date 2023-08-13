public class Track {
    private ArrayList eventsList = new ArrayList();
    private HashSet set = new HashSet();
    private MidiEvent eotEvent;
    Track() {
        MetaMessage eot = new ImmutableEndOfTrack();
        eotEvent = new MidiEvent(eot, 0);
        eventsList.add(eotEvent);
        set.add(eotEvent);
    }
    public boolean add(MidiEvent event) {
        if (event == null) {
            return false;
        }
        synchronized(eventsList) {
            if (!set.contains(event)) {
                int eventsCount = eventsList.size();
                MidiEvent lastEvent = null;
                if (eventsCount > 0) {
                    lastEvent = (MidiEvent) eventsList.get(eventsCount - 1);
                }
                if (lastEvent != eotEvent) {
                    if (lastEvent != null) {
                        eotEvent.setTick(lastEvent.getTick());
                    } else {
                        eotEvent.setTick(0);
                    }
                    eventsList.add(eotEvent);
                    set.add(eotEvent);
                    eventsCount = eventsList.size();
                }
                if (MidiUtils.isMetaEndOfTrack(event.getMessage())) {
                    if (event.getTick() > eotEvent.getTick()) {
                        eotEvent.setTick(event.getTick());
                    }
                    return true;
                }
                set.add(event);
                int i = eventsCount;
                for ( ; i > 0; i--) {
                    if (event.getTick() >= ((MidiEvent)eventsList.get(i-1)).getTick()) {
                        break;
                    }
                }
                if (i == eventsCount) {
                    eventsList.set(eventsCount - 1, event);
                    if (eotEvent.getTick() < event.getTick()) {
                        eotEvent.setTick(event.getTick());
                    }
                    eventsList.add(eotEvent);
                } else {
                    eventsList.add(i, event);
                }
                return true;
            }
        }
        return false;
    }
    public boolean remove(MidiEvent event) {
        synchronized(eventsList) {
            if (set.remove(event)) {
                int i = eventsList.indexOf(event);
                if (i >= 0) {
                    eventsList.remove(i);
                    return true;
                }
            }
        }
        return false;
    }
    public MidiEvent get(int index) throws ArrayIndexOutOfBoundsException {
        try {
            synchronized(eventsList) {
                return (MidiEvent)eventsList.get(index);
            }
        } catch (IndexOutOfBoundsException ioobe) {
            throw new ArrayIndexOutOfBoundsException(ioobe.getMessage());
        }
    }
    public int size() {
        synchronized(eventsList) {
            return eventsList.size();
        }
    }
    public long ticks() {
        long ret = 0;
        synchronized (eventsList) {
            if (eventsList.size() > 0) {
                ret = ((MidiEvent)eventsList.get(eventsList.size() - 1)).getTick();
            }
        }
        return ret;
    }
    private static class ImmutableEndOfTrack extends MetaMessage {
        private ImmutableEndOfTrack() {
            super(new byte[3]);
            data[0] = (byte) META;
            data[1] = MidiUtils.META_END_OF_TRACK_TYPE;
            data[2] = 0;
        }
        public void setMessage(int type, byte[] data, int length) throws InvalidMidiDataException {
            throw new InvalidMidiDataException("cannot modify end of track message");
        }
    }
}
