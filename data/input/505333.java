public class EventTargetImpl implements EventTarget {
    private static final String TAG = "EventTargetImpl";
    private ArrayList<EventListenerEntry> mListenerEntries;
    private EventTarget mNodeTarget;
    static class EventListenerEntry
    {
        final String mType;
        final EventListener mListener;
        final boolean mUseCapture;
        EventListenerEntry(String type, EventListener listener, boolean useCapture)
        {
            mType = type;
            mListener = listener;
            mUseCapture = useCapture;
        }
    }
    public EventTargetImpl(EventTarget target) {
        mNodeTarget = target;
    }
    public void addEventListener(String type, EventListener listener, boolean useCapture) {
        if ((type == null) || type.equals("") || (listener == null)) {
            return;
        }
        removeEventListener(type, listener, useCapture);
        if (mListenerEntries == null) {
            mListenerEntries = new ArrayList<EventListenerEntry>();
        }
        mListenerEntries.add(new EventListenerEntry(type, listener, useCapture));
    }
    public boolean dispatchEvent(Event evt) throws EventException {
        EventImpl eventImpl = (EventImpl)evt;
        if (!eventImpl.isInitialized()) {
            throw new EventException(EventException.UNSPECIFIED_EVENT_TYPE_ERR,
                    "Event not initialized");
        } else if ((eventImpl.getType() == null) || eventImpl.getType().equals("")) {
            throw new EventException(EventException.UNSPECIFIED_EVENT_TYPE_ERR,
                    "Unspecified even type");
        }
        eventImpl.setTarget(mNodeTarget);
        eventImpl.setEventPhase(Event.AT_TARGET);
        eventImpl.setCurrentTarget(mNodeTarget);
        if (!eventImpl.isPropogationStopped() && (mListenerEntries != null)) {
            for (int i = 0; i < mListenerEntries.size(); i++) {
                EventListenerEntry listenerEntry = mListenerEntries.get(i);
                if (!listenerEntry.mUseCapture
                        && listenerEntry.mType.equals(eventImpl.getType())) {
                    try {
                        listenerEntry.mListener.handleEvent(eventImpl);
                    }
                    catch (Exception e) {
                        Log.w(TAG, "Catched EventListener exception", e);
                    }
                }
            }
        }
        if (eventImpl.getBubbles()) {
        }
        return eventImpl.isPreventDefault();
    }
    public void removeEventListener(String type, EventListener listener,
            boolean useCapture) {
        if (null == mListenerEntries) {
            return;
        }
        for (int i = 0; i < mListenerEntries.size(); i ++) {
            EventListenerEntry listenerEntry = mListenerEntries.get(i);
            if ((listenerEntry.mUseCapture == useCapture)
                    && (listenerEntry.mListener == listener)
                    && listenerEntry.mType.equals(type)) {
                mListenerEntries.remove(i);
                break;
            }
        }
    }
}
