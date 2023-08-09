public class MidiUtils {
    public final static int DEFAULT_TEMPO_MPQ = 500000; 
    public final static int META_END_OF_TRACK_TYPE = 0x2F;
    public final static int META_TEMPO_TYPE = 0x51;
    public static boolean isMetaEndOfTrack(MidiMessage midiMsg) {
        if (midiMsg.getLength() != 3
            || midiMsg.getStatus() != MetaMessage.META) {
            return false;
        }
        byte[] msg = midiMsg.getMessage();
        return ((msg[1] & 0xFF) == META_END_OF_TRACK_TYPE) && (msg[2] == 0);
    }
    public static boolean isMetaTempo(MidiMessage midiMsg) {
        if (midiMsg.getLength() != 6
            || midiMsg.getStatus() != MetaMessage.META) {
            return false;
        }
        byte[] msg = midiMsg.getMessage();
        return ((msg[1] & 0xFF) == META_TEMPO_TYPE) && (msg[2] == 3);
    }
    public static int getTempoMPQ(MidiMessage midiMsg) {
        if (midiMsg.getLength() != 6
            || midiMsg.getStatus() != MetaMessage.META) {
            return -1;
        }
        byte[] msg = midiMsg.getMessage();
        if (((msg[1] & 0xFF) != META_TEMPO_TYPE) || (msg[2] != 3)) {
            return -1;
        }
        int tempo =    (msg[5] & 0xFF)
                    | ((msg[4] & 0xFF) << 8)
                    | ((msg[3] & 0xFF) << 16);
        return tempo;
    }
    public static double convertTempo(double tempo) {
        if (tempo <= 0) {
            tempo = 1;
        }
        return ((double) 60000000l) / tempo;
    }
    public static long ticks2microsec(long tick, double tempoMPQ, int resolution) {
        return (long) (((double) tick) * tempoMPQ / resolution);
    }
    public static long microsec2ticks(long us, double tempoMPQ, int resolution) {
        return (long) ((((double)us) * resolution) / tempoMPQ);
    }
    public static long tick2microsecond(Sequence seq, long tick, TempoCache cache) {
        if (seq.getDivisionType() != Sequence.PPQ ) {
            double seconds = ((double)tick / (double)(seq.getDivisionType() * seq.getResolution()));
            return (long) (1000000 * seconds);
        }
        if (cache == null) {
            cache = new TempoCache(seq);
        }
        int resolution = seq.getResolution();
        long[] ticks = cache.ticks;
        int[] tempos = cache.tempos; 
        int cacheCount = tempos.length;
        int snapshotIndex = cache.snapshotIndex;
        int snapshotMicro = cache.snapshotMicro;
        long us = 0; 
        if (snapshotIndex <= 0
            || snapshotIndex >= cacheCount
            || ticks[snapshotIndex] > tick) {
            snapshotMicro = 0;
            snapshotIndex = 0;
        }
        if (cacheCount > 0) {
            int i = snapshotIndex + 1;
            while (i < cacheCount && ticks[i] <= tick) {
                snapshotMicro += ticks2microsec(ticks[i] - ticks[i - 1], tempos[i - 1], resolution);
                snapshotIndex = i;
                i++;
            }
            us = snapshotMicro
                + ticks2microsec(tick - ticks[snapshotIndex],
                                 tempos[snapshotIndex],
                                 resolution);
        }
        cache.snapshotIndex = snapshotIndex;
        cache.snapshotMicro = snapshotMicro;
        return us;
    }
    public static long microsecond2tick(Sequence seq, long micros, TempoCache cache) {
        if (seq.getDivisionType() != Sequence.PPQ ) {
            double dTick = ( ((double) micros)
                           * ((double) seq.getDivisionType())
                           * ((double) seq.getResolution()))
                           / ((double) 1000000);
            long tick = (long) dTick;
            if (cache != null) {
                cache.currTempo = (int) cache.getTempoMPQAt(tick);
            }
            return tick;
        }
        if (cache == null) {
            cache = new TempoCache(seq);
        }
        long[] ticks = cache.ticks;
        int[] tempos = cache.tempos; 
        int cacheCount = tempos.length;
        int resolution = seq.getResolution();
        long us = 0; long tick = 0; int newReadPos = 0; int i = 1;
        if (micros > 0 && cacheCount > 0) {
            while (i < cacheCount) {
                long nextTime = us + ticks2microsec(ticks[i] - ticks[i - 1],
                                                    tempos[i - 1], resolution);
                if (nextTime > micros) {
                    break;
                }
                us = nextTime;
                i++;
            }
            tick = ticks[i - 1] + microsec2ticks(micros - us, tempos[i - 1], resolution);
            if (Printer.debug) Printer.debug("microsecond2tick(" + (micros / 1000)+") = "+tick+" ticks.");
        }
        cache.currTempo = tempos[i - 1];
        return tick;
    }
    public static int tick2index(Track track, long tick) {
        int ret = 0;
        if (tick > 0) {
            int low = 0;
            int high = track.size() - 1;
            while (low < high) {
                ret = (low + high) >> 1;
                long t = track.get(ret).getTick();
                if (t == tick) {
                    break;
                } else if (t < tick) {
                    if (low == high - 1) {
                        ret++;
                        break;
                    }
                    low = ret;
                } else { 
                    high = ret;
                }
            }
        }
        return ret;
    }
    public static class TempoCache {
        long[] ticks;
        int[] tempos; 
        int snapshotIndex = 0;
        int snapshotMicro = 0;
        int currTempo; 
        private boolean firstTempoIsFake = false;
        public TempoCache() {
            ticks = new long[1];
            tempos = new int[1];
            tempos[0] = DEFAULT_TEMPO_MPQ;
            snapshotIndex = 0;
            snapshotMicro = 0;
        }
        public TempoCache(Sequence seq) {
            this();
            refresh(seq);
        }
        public synchronized void refresh(Sequence seq) {
            ArrayList list = new ArrayList();
            Track[] tracks = seq.getTracks();
            if (tracks.length > 0) {
                Track track = tracks[0];
                int c = track.size();
                for (int i = 0; i < c; i++) {
                    MidiEvent ev = track.get(i);
                    MidiMessage msg = ev.getMessage();
                    if (isMetaTempo(msg)) {
                        list.add(ev);
                    }
                }
            }
            int size = list.size() + 1;
            firstTempoIsFake = true;
            if ((size > 1)
                && (((MidiEvent) list.get(0)).getTick() == 0)) {
                size--;
                firstTempoIsFake = false;
            }
            ticks  = new long[size];
            tempos = new int[size];
            int e = 0;
            if (firstTempoIsFake) {
                ticks[0] = 0;
                tempos[0] = DEFAULT_TEMPO_MPQ;
                e++;
            }
            for (int i = 0; i < list.size(); i++, e++) {
                MidiEvent evt = (MidiEvent) list.get(i);
                ticks[e] = evt.getTick();
                tempos[e] = getTempoMPQ(evt.getMessage());
            }
            snapshotIndex = 0;
            snapshotMicro = 0;
        }
        public int getCurrTempoMPQ() {
            return currTempo;
        }
        float getTempoMPQAt(long tick) {
            return getTempoMPQAt(tick, -1.0f);
        }
        synchronized float getTempoMPQAt(long tick, float startTempoMPQ) {
            for (int i = 0; i < ticks.length; i++) {
                if (ticks[i] > tick) {
                    if (i > 0) i--;
                    if (startTempoMPQ > 0 && i == 0 && firstTempoIsFake) {
                        return startTempoMPQ;
                    }
                    return (float) tempos[i];
                }
            }
            return tempos[tempos.length - 1];
        }
    }
}
