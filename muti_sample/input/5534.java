public class Timer extends NotificationBroadcasterSupport
        implements TimerMBean, MBeanRegistration {
    public static final long ONE_SECOND = 1000;
    public static final long ONE_MINUTE = 60*ONE_SECOND;
    public static final long ONE_HOUR   = 60*ONE_MINUTE;
    public static final long ONE_DAY    = 24*ONE_HOUR;
    public static final long ONE_WEEK   = 7*ONE_DAY;
    private Map<Integer,Object[]> timerTable =
        new Hashtable<Integer,Object[]>();
    private boolean sendPastNotifications = false;
    private transient boolean isActive = false;
    private transient long sequenceNumber = 0;
    private static final int TIMER_NOTIF_INDEX     = 0;
    private static final int TIMER_DATE_INDEX      = 1;
    private static final int TIMER_PERIOD_INDEX    = 2;
    private static final int TIMER_NB_OCCUR_INDEX  = 3;
    private static final int ALARM_CLOCK_INDEX     = 4;
    private static final int FIXED_RATE_INDEX      = 5;
    private int counterID = 0;
    private java.util.Timer timer;
    public Timer() {
    }
    public ObjectName preRegister(MBeanServer server, ObjectName name)
        throws java.lang.Exception {
        return name;
    }
    public void postRegister (Boolean registrationDone) {
    }
    public void preDeregister() throws java.lang.Exception {
        TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                "preDeregister", "stop the timer");
        stop();
    }
    public void postDeregister() {
    }
    public synchronized MBeanNotificationInfo[] getNotificationInfo() {
        Set<String> notifTypes = new TreeSet<String>();
        for (Object[] entry : timerTable.values()) {
            TimerNotification notif = (TimerNotification)
                entry[TIMER_NOTIF_INDEX];
            notifTypes.add(notif.getType());
        }
        String[] notifTypesArray =
            notifTypes.toArray(new String[0]);
        return new MBeanNotificationInfo[] {
            new MBeanNotificationInfo(notifTypesArray,
                                      TimerNotification.class.getName(),
                                      "Notification sent by Timer MBean")
        };
    }
    public synchronized void start() {
        TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                "start", "starting the timer");
        if (isActive == false) {
            timer = new java.util.Timer();
            TimerAlarmClock alarmClock;
            Date date;
            Date currentDate = new Date();
            sendPastNotifications(currentDate, sendPastNotifications);
            for (Object[] obj : timerTable.values()) {
                date = (Date)obj[TIMER_DATE_INDEX];
                boolean fixedRate = ((Boolean)obj[FIXED_RATE_INDEX]).booleanValue();
                if (fixedRate)
                {
                  alarmClock = new TimerAlarmClock(this, date);
                  obj[ALARM_CLOCK_INDEX] = (Object)alarmClock;
                  timer.schedule(alarmClock, alarmClock.next);
                }
                else
                {
                  alarmClock = new TimerAlarmClock(this, (date.getTime() - currentDate.getTime()));
                  obj[ALARM_CLOCK_INDEX] = (Object)alarmClock;
                  timer.schedule(alarmClock, alarmClock.timeout);
                }
            }
            isActive = true;
            TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                    "start", "timer started");
        } else {
            TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                    "start", "the timer is already activated");
        }
    }
    public synchronized void stop() {
        TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                "stop", "stopping the timer");
        if (isActive == true) {
            for (Object[] obj : timerTable.values()) {
                TimerAlarmClock alarmClock = (TimerAlarmClock)obj[ALARM_CLOCK_INDEX];
                if (alarmClock != null) {
                    alarmClock.cancel();
                }
            }
            timer.cancel();
            isActive = false;
            TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                    "stop", "timer stopped");
        } else {
            TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                    "stop", "the timer is already deactivated");
        }
    }
    public synchronized Integer addNotification(String type, String message, Object userData,
                                                Date date, long period, long nbOccurences, boolean fixedRate)
        throws java.lang.IllegalArgumentException {
        if (date == null) {
            throw new java.lang.IllegalArgumentException("Timer notification date cannot be null.");
        }
        if ((period < 0) || (nbOccurences < 0)) {
            throw new java.lang.IllegalArgumentException("Negative values for the periodicity");
        }
        Date currentDate = new Date();
        if (currentDate.after(date)) {
            date.setTime(currentDate.getTime());
            if (TIMER_LOGGER.isLoggable(Level.FINER)) {
                TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                        "addNotification",
                        "update timer notification to add with:" +
                        "\n\tNotification date = " + date);
            }
        }
        Integer notifID = Integer.valueOf(++counterID);
        TimerNotification notif = new TimerNotification(type, this, 0, 0, message, notifID);
        notif.setUserData(userData);
        Object[] obj = new Object[6];
        TimerAlarmClock alarmClock;
        if (fixedRate)
        {
          alarmClock = new TimerAlarmClock(this, date);
        }
        else
        {
          alarmClock = new TimerAlarmClock(this, (date.getTime() - currentDate.getTime()));
        }
        Date d = new Date(date.getTime());
        obj[TIMER_NOTIF_INDEX] = (Object)notif;
        obj[TIMER_DATE_INDEX] = (Object)d;
        obj[TIMER_PERIOD_INDEX] = (Object) period;
        obj[TIMER_NB_OCCUR_INDEX] = (Object) nbOccurences;
        obj[ALARM_CLOCK_INDEX] = (Object)alarmClock;
        obj[FIXED_RATE_INDEX] = Boolean.valueOf(fixedRate);
        if (TIMER_LOGGER.isLoggable(Level.FINER)) {
            StringBuilder strb = new StringBuilder()
            .append("adding timer notification:\n\t")
            .append("Notification source = ")
            .append(notif.getSource())
            .append("\n\tNotification type = ")
            .append(notif.getType())
            .append("\n\tNotification ID = ")
            .append(notifID)
            .append("\n\tNotification date = ")
            .append(d)
            .append("\n\tNotification period = ")
            .append(period)
            .append("\n\tNotification nb of occurrences = ")
            .append(nbOccurences)
            .append("\n\tNotification executes at fixed rate = ")
            .append(fixedRate);
            TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                    "addNotification", strb.toString());
        }
        timerTable.put(notifID, obj);
        if (isActive == true) {
          if (fixedRate)
          {
            timer.schedule(alarmClock, alarmClock.next);
          }
          else
          {
            timer.schedule(alarmClock, alarmClock.timeout);
          }
        }
        TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                "addNotification", "timer notification added");
        return notifID;
    }
    public synchronized Integer addNotification(String type, String message, Object userData,
                                                Date date, long period, long nbOccurences)
        throws java.lang.IllegalArgumentException {
      return addNotification(type, message, userData, date, period, nbOccurences, false);
    }
    public synchronized Integer addNotification(String type, String message, Object userData,
                                                Date date, long period)
        throws java.lang.IllegalArgumentException {
        return (addNotification(type, message, userData, date, period, 0));
    }
    public synchronized Integer addNotification(String type, String message, Object userData, Date date)
        throws java.lang.IllegalArgumentException {
        return (addNotification(type, message, userData, date, 0, 0));
    }
    public synchronized void removeNotification(Integer id) throws InstanceNotFoundException {
        if (timerTable.containsKey(id) == false) {
            throw new InstanceNotFoundException("Timer notification to remove not in the list of notifications");
        }
        Object[] obj = timerTable.get(id);
        TimerAlarmClock alarmClock = (TimerAlarmClock)obj[ALARM_CLOCK_INDEX];
        if (alarmClock != null) {
            alarmClock.cancel();
        }
        if (TIMER_LOGGER.isLoggable(Level.FINER)) {
            StringBuilder strb = new StringBuilder()
            .append("removing timer notification:")
            .append("\n\tNotification source = ")
            .append(((TimerNotification)obj[TIMER_NOTIF_INDEX]).getSource())
            .append("\n\tNotification type = ")
            .append(((TimerNotification)obj[TIMER_NOTIF_INDEX]).getType())
            .append("\n\tNotification ID = ")
            .append(((TimerNotification)obj[TIMER_NOTIF_INDEX]).getNotificationID())
            .append("\n\tNotification date = ")
            .append(obj[TIMER_DATE_INDEX])
            .append("\n\tNotification period = ")
            .append(obj[TIMER_PERIOD_INDEX])
            .append("\n\tNotification nb of occurrences = ")
            .append(obj[TIMER_NB_OCCUR_INDEX])
            .append("\n\tNotification executes at fixed rate = ")
            .append(obj[FIXED_RATE_INDEX]);
            TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                    "removeNotification", strb.toString());
        }
        timerTable.remove(id);
        TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                "removeNotification", "timer notification removed");
    }
    public synchronized void removeNotifications(String type) throws InstanceNotFoundException {
        Vector<Integer> v = getNotificationIDs(type);
        if (v.isEmpty())
            throw new InstanceNotFoundException("Timer notifications to remove not in the list of notifications");
        for (Integer i : v)
            removeNotification(i);
    }
    public synchronized void removeAllNotifications() {
        TimerAlarmClock alarmClock;
        for (Object[] obj : timerTable.values()) {
            alarmClock = (TimerAlarmClock)obj[ALARM_CLOCK_INDEX];
            alarmClock.cancel();
        }
        TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                "removeAllNotifications", "removing all timer notifications");
        timerTable.clear();
        TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                "removeAllNotifications", "all timer notifications removed");
        counterID = 0;
        TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                "removeAllNotifications", "timer notification counter ID reset");
    }
    public int getNbNotifications() {
        return timerTable.size();
    }
    public synchronized Vector<Integer> getAllNotificationIDs() {
        return new Vector<Integer>(timerTable.keySet());
    }
    public synchronized Vector<Integer> getNotificationIDs(String type) {
        String s;
        Vector<Integer> v = new Vector<Integer>();
        for (Map.Entry<Integer,Object[]> entry : timerTable.entrySet()) {
            Object[] obj = entry.getValue();
            s = ((TimerNotification)obj[TIMER_NOTIF_INDEX]).getType();
            if ((type == null) ? s == null : type.equals(s))
                v.addElement(entry.getKey());
        }
        return v;
    }
    public String getNotificationType(Integer id) {
        Object[] obj = timerTable.get(id);
        if (obj != null) {
            return ( ((TimerNotification)obj[TIMER_NOTIF_INDEX]).getType() );
        }
        return null;
    }
    public String getNotificationMessage(Integer id) {
        Object[] obj = timerTable.get(id);
        if (obj != null) {
            return ( ((TimerNotification)obj[TIMER_NOTIF_INDEX]).getMessage() );
        }
        return null;
    }
    public Object getNotificationUserData(Integer id) {
        Object[] obj = timerTable.get(id);
        if (obj != null) {
            return ( ((TimerNotification)obj[TIMER_NOTIF_INDEX]).getUserData() );
        }
        return null;
    }
    public Date getDate(Integer id) {
        Object[] obj = timerTable.get(id);
        if (obj != null) {
            Date date = (Date)obj[TIMER_DATE_INDEX];
            return (new Date(date.getTime()));
        }
        return null;
    }
    public Long getPeriod(Integer id) {
        Object[] obj = timerTable.get(id);
        if (obj != null) {
            return (Long)obj[TIMER_PERIOD_INDEX];
        }
        return null;
    }
    public Long getNbOccurences(Integer id) {
        Object[] obj = timerTable.get(id);
        if (obj != null) {
            return (Long)obj[TIMER_NB_OCCUR_INDEX];
        }
        return null;
    }
    public Boolean getFixedRate(Integer id) {
      Object[] obj = timerTable.get(id);
      if (obj != null) {
        Boolean fixedRate = (Boolean)obj[FIXED_RATE_INDEX];
        return (Boolean.valueOf(fixedRate.booleanValue()));
      }
      return null;
    }
    public boolean getSendPastNotifications() {
        return sendPastNotifications;
    }
    public void setSendPastNotifications(boolean value) {
        sendPastNotifications = value;
    }
    public boolean isActive() {
        return isActive;
    }
    public boolean isEmpty() {
        return (timerTable.isEmpty());
    }
    private synchronized void sendPastNotifications(Date currentDate, boolean currentFlag) {
        TimerNotification notif;
        Integer notifID;
        Date date;
        ArrayList<Object[]> values =
            new ArrayList<Object[]>(timerTable.values());
        for (Object[] obj : values) {
            notif = (TimerNotification)obj[TIMER_NOTIF_INDEX];
            notifID = notif.getNotificationID();
            date = (Date)obj[TIMER_DATE_INDEX];
            while ( (currentDate.after(date)) && (timerTable.containsKey(notifID)) ) {
                if (currentFlag == true) {
                    if (TIMER_LOGGER.isLoggable(Level.FINER)) {
                        StringBuilder strb = new StringBuilder()
                        .append("sending past timer notification:")
                        .append("\n\tNotification source = ")
                        .append(notif.getSource())
                        .append("\n\tNotification type = ")
                        .append(notif.getType())
                        .append("\n\tNotification ID = ")
                        .append(notif.getNotificationID())
                        .append("\n\tNotification date = ")
                        .append(date)
                        .append("\n\tNotification period = ")
                        .append(obj[TIMER_PERIOD_INDEX])
                        .append("\n\tNotification nb of occurrences = ")
                        .append(obj[TIMER_NB_OCCUR_INDEX])
                        .append("\n\tNotification executes at fixed rate = ")
                        .append(obj[FIXED_RATE_INDEX]);
                        TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                                "sendPastNotifications", strb.toString());
                    }
                    sendNotification(date, notif);
                    TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                            "sendPastNotifications", "past timer notification sent");
                }
                updateTimerTable(notif.getNotificationID());
            }
        }
    }
    private synchronized void updateTimerTable(Integer notifID) {
        Object[] obj = timerTable.get(notifID);
        Date date = (Date)obj[TIMER_DATE_INDEX];
        Long period = (Long)obj[TIMER_PERIOD_INDEX];
        Long nbOccurences = (Long)obj[TIMER_NB_OCCUR_INDEX];
        Boolean fixedRate = (Boolean)obj[FIXED_RATE_INDEX];
        TimerAlarmClock alarmClock = (TimerAlarmClock)obj[ALARM_CLOCK_INDEX];
        if (period.longValue() != 0) {
            if ((nbOccurences.longValue() == 0) || (nbOccurences.longValue() > 1)) {
                date.setTime(date.getTime() + period.longValue());
                obj[TIMER_NB_OCCUR_INDEX] = Long.valueOf(java.lang.Math.max(0L, (nbOccurences.longValue() - 1)));
                nbOccurences = (Long)obj[TIMER_NB_OCCUR_INDEX];
                if (isActive == true) {
                  if (fixedRate.booleanValue())
                  {
                    alarmClock = new TimerAlarmClock(this, date);
                    obj[ALARM_CLOCK_INDEX] = (Object)alarmClock;
                    timer.schedule(alarmClock, alarmClock.next);
                  }
                  else
                  {
                    alarmClock = new TimerAlarmClock(this, period.longValue());
                    obj[ALARM_CLOCK_INDEX] = (Object)alarmClock;
                    timer.schedule(alarmClock, alarmClock.timeout);
                  }
                }
                if (TIMER_LOGGER.isLoggable(Level.FINER)) {
                    TimerNotification notif = (TimerNotification)obj[TIMER_NOTIF_INDEX];
                    StringBuilder strb = new StringBuilder()
                    .append("update timer notification with:")
                    .append("\n\tNotification source = ")
                    .append(notif.getSource())
                    .append("\n\tNotification type = ")
                    .append(notif.getType())
                    .append("\n\tNotification ID = ")
                    .append(notifID)
                    .append("\n\tNotification date = ")
                    .append(date)
                    .append("\n\tNotification period = ")
                    .append(period)
                    .append("\n\tNotification nb of occurrences = ")
                    .append(nbOccurences)
                    .append("\n\tNotification executes at fixed rate = ")
                    .append(fixedRate);
                    TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                            "updateTimerTable", strb.toString());
                }
            }
            else {
                if (alarmClock != null) {
                    alarmClock.cancel();
                }
                timerTable.remove(notifID);
            }
        }
        else {
            if (alarmClock != null) {
                   alarmClock.cancel();
            }
            timerTable.remove(notifID);
        }
    }
    @SuppressWarnings("deprecation")
    void notifyAlarmClock(TimerAlarmClockNotification notification) {
        TimerNotification timerNotification = null;
        Date timerDate = null;
        TimerAlarmClock alarmClock = (TimerAlarmClock)notification.getSource();
        for (Object[] obj : timerTable.values()) {
            if (obj[ALARM_CLOCK_INDEX] == alarmClock) {
                timerNotification = (TimerNotification)obj[TIMER_NOTIF_INDEX];
                timerDate = (Date)obj[TIMER_DATE_INDEX];
                break;
            }
        }
        sendNotification(timerDate, timerNotification);
        updateTimerTable(timerNotification.getNotificationID());
    }
    void sendNotification(Date timeStamp, TimerNotification notification) {
        if (TIMER_LOGGER.isLoggable(Level.FINER)) {
            StringBuilder strb = new StringBuilder()
            .append("sending timer notification:")
            .append("\n\tNotification source = ")
            .append(notification.getSource())
            .append("\n\tNotification type = ")
            .append(notification.getType())
            .append("\n\tNotification ID = ")
            .append(notification.getNotificationID())
            .append("\n\tNotification date = ")
            .append(timeStamp);
            TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                    "sendNotification", strb.toString());
        }
        long curSeqNumber;
        synchronized(this) {
            sequenceNumber = sequenceNumber + 1;
            curSeqNumber = sequenceNumber;
        }
        synchronized (notification) {
            notification.setTimeStamp(timeStamp.getTime());
            notification.setSequenceNumber(curSeqNumber);
            this.sendNotification((TimerNotification)notification.cloneTimerNotification());
        }
        TIMER_LOGGER.logp(Level.FINER, Timer.class.getName(),
                "sendNotification", "timer notification sent");
    }
}
class TimerAlarmClock extends java.util.TimerTask {
    Timer listener = null;
    long timeout = 10000;
    Date next = null;
    public TimerAlarmClock(Timer listener, long timeout) {
        this.listener = listener;
        this.timeout = Math.max(0L, timeout);
    }
    public TimerAlarmClock(Timer listener, Date next) {
        this.listener = listener;
        this.next = next;
    }
    public void run() {
        try {
            TimerAlarmClockNotification notif = new TimerAlarmClockNotification(this);
            listener.notifyAlarmClock(notif);
        } catch (Exception e) {
            TIMER_LOGGER.logp(Level.FINEST, Timer.class.getName(), "run",
                    "Got unexpected exception when sending a notification", e);
        }
    }
}
