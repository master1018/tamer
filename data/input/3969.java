public class SoftChannelProxy implements MidiChannel {
    private MidiChannel channel = null;
    public MidiChannel getChannel() {
        return channel;
    }
    public void setChannel(MidiChannel channel) {
        this.channel = channel;
    }
    public void allNotesOff() {
        if (channel == null)
            return;
        channel.allNotesOff();
    }
    public void allSoundOff() {
        if (channel == null)
            return;
        channel.allSoundOff();
    }
    public void controlChange(int controller, int value) {
        if (channel == null)
            return;
        channel.controlChange(controller, value);
    }
    public int getChannelPressure() {
        if (channel == null)
            return 0;
        return channel.getChannelPressure();
    }
    public int getController(int controller) {
        if (channel == null)
            return 0;
        return channel.getController(controller);
    }
    public boolean getMono() {
        if (channel == null)
            return false;
        return channel.getMono();
    }
    public boolean getMute() {
        if (channel == null)
            return false;
        return channel.getMute();
    }
    public boolean getOmni() {
        if (channel == null)
            return false;
        return channel.getOmni();
    }
    public int getPitchBend() {
        if (channel == null)
            return 8192;
        return channel.getPitchBend();
    }
    public int getPolyPressure(int noteNumber) {
        if (channel == null)
            return 0;
        return channel.getPolyPressure(noteNumber);
    }
    public int getProgram() {
        if (channel == null)
            return 0;
        return channel.getProgram();
    }
    public boolean getSolo() {
        if (channel == null)
            return false;
        return channel.getSolo();
    }
    public boolean localControl(boolean on) {
        if (channel == null)
            return false;
        return channel.localControl(on);
    }
    public void noteOff(int noteNumber) {
        if (channel == null)
            return;
        channel.noteOff(noteNumber);
    }
    public void noteOff(int noteNumber, int velocity) {
        if (channel == null)
            return;
        channel.noteOff(noteNumber, velocity);
    }
    public void noteOn(int noteNumber, int velocity) {
        if (channel == null)
            return;
        channel.noteOn(noteNumber, velocity);
    }
    public void programChange(int program) {
        if (channel == null)
            return;
        channel.programChange(program);
    }
    public void programChange(int bank, int program) {
        if (channel == null)
            return;
        channel.programChange(bank, program);
    }
    public void resetAllControllers() {
        if (channel == null)
            return;
        channel.resetAllControllers();
    }
    public void setChannelPressure(int pressure) {
        if (channel == null)
            return;
        channel.setChannelPressure(pressure);
    }
    public void setMono(boolean on) {
        if (channel == null)
            return;
        channel.setMono(on);
    }
    public void setMute(boolean mute) {
        if (channel == null)
            return;
        channel.setMute(mute);
    }
    public void setOmni(boolean on) {
        if (channel == null)
            return;
        channel.setOmni(on);
    }
    public void setPitchBend(int bend) {
        if (channel == null)
            return;
        channel.setPitchBend(bend);
    }
    public void setPolyPressure(int noteNumber, int pressure) {
        if (channel == null)
            return;
        channel.setPolyPressure(noteNumber, pressure);
    }
    public void setSolo(boolean soloState) {
        if (channel == null)
            return;
        channel.setSolo(soloState);
    }
}
