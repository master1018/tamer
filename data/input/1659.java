public abstract class ModelAbstractChannelMixer implements ModelChannelMixer {
    public abstract boolean process(float[][] buffer, int offset, int len);
    public abstract void stop();
    public void allNotesOff() {
    }
    public void allSoundOff() {
    }
    public void controlChange(int controller, int value) {
    }
    public int getChannelPressure() {
        return 0;
    }
    public int getController(int controller) {
        return 0;
    }
    public boolean getMono() {
        return false;
    }
    public boolean getMute() {
        return false;
    }
    public boolean getOmni() {
        return false;
    }
    public int getPitchBend() {
        return 0;
    }
    public int getPolyPressure(int noteNumber) {
        return 0;
    }
    public int getProgram() {
        return 0;
    }
    public boolean getSolo() {
        return false;
    }
    public boolean localControl(boolean on) {
        return false;
    }
    public void noteOff(int noteNumber) {
    }
    public void noteOff(int noteNumber, int velocity) {
    }
    public void noteOn(int noteNumber, int velocity) {
    }
    public void programChange(int program) {
    }
    public void programChange(int bank, int program) {
    }
    public void resetAllControllers() {
    }
    public void setChannelPressure(int pressure) {
    }
    public void setMono(boolean on) {
    }
    public void setMute(boolean mute) {
    }
    public void setOmni(boolean on) {
    }
    public void setPitchBend(int bend) {
    }
    public void setPolyPressure(int noteNumber, int pressure) {
    }
    public void setSolo(boolean soloState) {
    }
}
