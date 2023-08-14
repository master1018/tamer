class EventDispatcher implements Runnable {
    private static final int AUTO_CLOSE_TIME = 5000;
    private ArrayList eventQueue = new ArrayList();
    private Thread thread = null;
    private ArrayList<ClipInfo> autoClosingClips = new ArrayList<ClipInfo>();
    private ArrayList<LineMonitor> lineMonitors = new ArrayList<LineMonitor>();
    static final int LINE_MONITOR_TIME = 400;
    synchronized void start() {
        if(thread == null) {
            thread = JSSecurityManager.createThread(this,
                                                    "Java Sound Event Dispatcher",   
                                                    true,  
                                                    -1,    
                                                    true); 
        }
    }
    protected void processEvent(EventInfo eventInfo) {
        int count = eventInfo.getListenerCount();
        if (eventInfo.getEvent() instanceof LineEvent) {
            LineEvent event = (LineEvent) eventInfo.getEvent();
            if (Printer.debug) Printer.debug("Sending "+event+" to "+count+" listeners");
            for (int i = 0; i < count; i++) {
                try {
                    ((LineListener) eventInfo.getListener(i)).update(event);
                } catch (Throwable t) {
                    if (Printer.err) t.printStackTrace();
                }
            }
            return;
        }
        if (eventInfo.getEvent() instanceof MetaMessage) {
            MetaMessage event = (MetaMessage)eventInfo.getEvent();
            for (int i = 0; i < count; i++) {
                try {
                    ((MetaEventListener) eventInfo.getListener(i)).meta(event);
                } catch (Throwable t) {
                    if (Printer.err) t.printStackTrace();
                }
            }
            return;
        }
        if (eventInfo.getEvent() instanceof ShortMessage) {
            ShortMessage event = (ShortMessage)eventInfo.getEvent();
            int status = event.getStatus();
            if ((status & 0xF0) == 0xB0) {
                for (int i = 0; i < count; i++) {
                    try {
                        ((ControllerEventListener) eventInfo.getListener(i)).controlChange(event);
                    } catch (Throwable t) {
                        if (Printer.err) t.printStackTrace();
                    }
                }
            }
            return;
        }
        Printer.err("Unknown event type: " + eventInfo.getEvent());
    }
    protected void dispatchEvents() {
        EventInfo eventInfo = null;
        synchronized (this) {
            try {
                if (eventQueue.size() == 0) {
                    if (autoClosingClips.size() > 0 || lineMonitors.size() > 0) {
                        int waitTime = AUTO_CLOSE_TIME;
                        if (lineMonitors.size() > 0) {
                            waitTime = LINE_MONITOR_TIME;
                        }
                        wait(waitTime);
                    } else {
                        wait();
                    }
                }
            } catch (InterruptedException e) {
            }
            if (eventQueue.size() > 0) {
                eventInfo = (EventInfo) eventQueue.remove(0);
            }
        } 
        if (eventInfo != null) {
            processEvent(eventInfo);
        } else {
            if (autoClosingClips.size() > 0) {
                closeAutoClosingClips();
            }
            if (lineMonitors.size() > 0) {
                monitorLines();
            }
        }
    }
    private synchronized void postEvent(EventInfo eventInfo) {
        eventQueue.add(eventInfo);
        notifyAll();
    }
    public void run() {
        while (true) {
            try {
                dispatchEvents();
            } catch (Throwable t) {
                if (Printer.err) t.printStackTrace();
            }
        }
    }
    void sendAudioEvents(Object event, List listeners) {
        if ((listeners == null)
            || (listeners.size() == 0)) {
            return;
        }
        start();
        EventInfo eventInfo = new EventInfo(event, listeners);
        postEvent(eventInfo);
    }
    private void closeAutoClosingClips() {
        synchronized(autoClosingClips) {
            if (Printer.debug)Printer.debug("> EventDispatcher.closeAutoClosingClips ("+autoClosingClips.size()+" clips)");
            long currTime = System.currentTimeMillis();
            for (int i = autoClosingClips.size()-1; i >= 0 ; i--) {
                ClipInfo info = autoClosingClips.get(i);
                if (info.isExpired(currTime)) {
                    AutoClosingClip clip = info.getClip();
                    if (!clip.isOpen() || !clip.isAutoClosing()) {
                        if (Printer.debug)Printer.debug("EventDispatcher: removing clip "+clip+"  isOpen:"+clip.isOpen());
                        autoClosingClips.remove(i);
                    }
                    else if (!clip.isRunning() && !clip.isActive() && clip.isAutoClosing()) {
                        if (Printer.debug)Printer.debug("EventDispatcher: closing clip "+clip);
                        clip.close();
                    } else {
                        if (Printer.debug)Printer.debug("Doing nothing with clip "+clip+":");
                        if (Printer.debug)Printer.debug("  open="+clip.isOpen()+", autoclosing="+clip.isAutoClosing());
                        if (Printer.debug)Printer.debug("  isRunning="+clip.isRunning()+", isActive="+clip.isActive());
                    }
                } else {
                    if (Printer.debug)Printer.debug("EventDispatcher: clip "+info.getClip()+" not yet expired");
                }
            }
        }
        if (Printer.debug)Printer.debug("< EventDispatcher.closeAutoClosingClips ("+autoClosingClips.size()+" clips)");
    }
    private int getAutoClosingClipIndex(AutoClosingClip clip) {
        synchronized(autoClosingClips) {
            for (int i = autoClosingClips.size()-1; i >= 0; i--) {
                if (clip.equals(autoClosingClips.get(i).getClip())) {
                    return i;
                }
            }
        }
        return -1;
    }
    void autoClosingClipOpened(AutoClosingClip clip) {
        if (Printer.debug)Printer.debug("> EventDispatcher.autoClosingClipOpened ");
        int index = 0;
        synchronized(autoClosingClips) {
            index = getAutoClosingClipIndex(clip);
            if (index == -1) {
                if (Printer.debug)Printer.debug("EventDispatcher: adding auto-closing clip "+clip);
                autoClosingClips.add(new ClipInfo(clip));
            }
        }
        if (index == -1) {
            synchronized (this) {
                notifyAll();
            }
        }
        if (Printer.debug)Printer.debug("< EventDispatcher.autoClosingClipOpened finished("+autoClosingClips.size()+" clips)");
    }
    void autoClosingClipClosed(AutoClosingClip clip) {
    }
    private void monitorLines() {
        synchronized(lineMonitors) {
            if (Printer.debug)Printer.debug("> EventDispatcher.monitorLines ("+lineMonitors.size()+" monitors)");
            for (int i = 0; i < lineMonitors.size(); i++) {
                lineMonitors.get(i).checkLine();
            }
        }
        if (Printer.debug)Printer.debug("< EventDispatcher.monitorLines("+lineMonitors.size()+" monitors)");
    }
    void addLineMonitor(LineMonitor lm) {
        if (Printer.trace)Printer.trace("> EventDispatcher.addLineMonitor("+lm+")");
        synchronized(lineMonitors) {
            if (lineMonitors.indexOf(lm) >= 0) {
                if (Printer.trace)Printer.trace("< EventDispatcher.addLineMonitor finished -- this monitor already exists!");
                return;
            }
            if (Printer.debug)Printer.debug("EventDispatcher: adding line monitor "+lm);
            lineMonitors.add(lm);
        }
        synchronized (this) {
            notifyAll();
        }
        if (Printer.debug)Printer.debug("< EventDispatcher.addLineMonitor finished -- now ("+lineMonitors.size()+" monitors)");
    }
    void removeLineMonitor(LineMonitor lm) {
        if (Printer.trace)Printer.trace("> EventDispatcher.removeLineMonitor("+lm+")");
        synchronized(lineMonitors) {
            if (lineMonitors.indexOf(lm) < 0) {
                if (Printer.trace)Printer.trace("< EventDispatcher.removeLineMonitor finished -- this monitor does not exist!");
                return;
            }
            if (Printer.debug)Printer.debug("EventDispatcher: removing line monitor "+lm);
            lineMonitors.remove(lm);
        }
        if (Printer.debug)Printer.debug("< EventDispatcher.removeLineMonitor finished -- now ("+lineMonitors.size()+" monitors)");
    }
    private class EventInfo {
        private Object event;
        private Object[] listeners;
        EventInfo(Object event, List listeners) {
            this.event = event;
            this.listeners = listeners.toArray();
        }
        Object getEvent() {
            return event;
        }
        int getListenerCount() {
            return listeners.length;
        }
        Object getListener(int index) {
            return listeners[index];
        }
    } 
    private class ClipInfo {
        private AutoClosingClip clip;
        private long expiration;
        ClipInfo(AutoClosingClip clip) {
            this.clip = clip;
            this.expiration = System.currentTimeMillis() + AUTO_CLOSE_TIME;
        }
        AutoClosingClip getClip() {
            return clip;
        }
        boolean isExpired(long currTime) {
            return currTime > expiration;
        }
    } 
    interface LineMonitor {
        public void checkLine();
    }
} 
