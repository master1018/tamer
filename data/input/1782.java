public class test {
    private void jRadioButton9ItemStateChanged(ItemEvent evt) {
        if (getInitProvider() == null || getInitProvider().isInitiating() || evt.getStateChange() != ItemEvent.SELECTED) {
            return;
        }
        MidiThread.getInstance().emitParamChange(this, getChannelNumber(), RackAttack.DRY_OUT, 8);
    }
}
