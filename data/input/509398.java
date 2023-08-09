class MediaRecorderStateErrors {
    public static enum MediaRecorderState {
        INITIAL,
        INITIAL_AFTER_RESET,
        INITIAL_AFTER_STOP,
        INITIALIZED,
        DATASOURCECONFIGURED,
        PREPARED,
        RECORDING,
        ERROR,
    }
    public boolean errorInInitialState = false;
    public boolean errorInInitialStateAfterReset = false;
    public boolean errorInInitialStateAfterStop = false;
    public boolean errorInInitializedState = false;
    public boolean errorInDataSourceConfiguredState = false;
    public boolean errorInPreparedState = false;
    public boolean errorInRecordingState = false;
    public boolean errorInErrorState = false;
}
