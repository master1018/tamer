package net.sf.logdistiller.logtypes;

import java.io.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;
import net.sf.logdistiller.LogEvent;
import net.sf.logdistiller.LogType;
import net.sf.logdistiller.util.LogEventBuilder;
import net.sf.logdistiller.util.StringCutter;

/**
 * Log event for <a href="http://jboss.org/">JBoss</a>'s server logs. By default, the classification rules generated by
 * the GUI for this type of logs will sort events based on the following attributes: <code>level</code> then
 * <code>logger</code>.
 *
 * @since 0.9
 */
public class JBossLogEvent extends LogEvent implements Comparable<JBossLogEvent> {

    public static final String ID = "jboss";

    public final String timestamp;

    public final Date date;

    public final String level;

    public final String logger;

    public final String message;

    public final String throwable;

    public final String throwable_firstLine;

    public final String throwable_class;

    public static final LogType LOGTYPE = new LogType.Basic(ID);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    private static final String[] ATTRIBUTE_NAMES = { "logSource", "timestamp", "timestamp.date", "timestamp.time", "level", "logger", "message", "throwable", "throwable.firstline", "throwable.class" };

    public static final LogType.Description DESCRIPTION = new Description((LogType.Basic) LOGTYPE, ATTRIBUTE_NAMES);

    public JBossLogEvent(LogEvent.Factory factory, String firstLine, String secondLine, String throwable) throws ParseException {
        super(factory, (secondLine == null) ? firstLine : (firstLine + Factory.NEWLINE + throwable));
        timestamp = firstLine.substring(0, 23);
        date = DATE_FORMAT.parse(timestamp);
        level = firstLine.substring(24, 29).trim();
        StringCutter cutter = new StringCutter(firstLine.substring(31));
        logger = cutter.parseTo("] ");
        message = cutter.getRemaining();
        this.throwable = throwable;
        throwable_firstLine = (secondLine == null) ? "" : secondLine;
        int index = throwable_firstLine.indexOf(':');
        throwable_class = (index > 0) ? throwable.substring(0, index) : "";
        setAttributes(new String[] { factory.getLogSource(), timestamp, timestamp.substring(0, 10), timestamp.substring(11, 23), level, logger, message, throwable, throwable_firstLine, throwable_class });
    }

    public int compareTo(JBossLogEvent o) {
        return date.compareTo(o.date);
    }

    private static class Description extends LogType.Description {

        public Description(LogType.Basic logtype, String[] attributeNames) {
            super(logtype, attributeNames);
            logtype.setDescription(this);
        }

        public LogEvent.Factory newFactory(Reader reader, String logSource) throws IOException {
            return new Factory(this, reader, logSource);
        }

        public String getDefaultSpecificGroups() {
            return "  <group id=\"warn\">\n" + "    <description>WARN events</description>\n" + "    <condition>\n" + "      <match attribute=\"level\" type=\"equals\">WARN</match>\n" + "    </condition>\n" + "    <report publisher=\"file\"/>\n" + "    <plugin type=\"sampling\">\n" + "      <param name=\"attributes\">logger</param>\n" + "    </plugin>\n" + "  </group>\n" + "\n" + "  <group id=\"error\">\n" + "    <description>ERROR events</description>\n" + "    <condition>\n" + "      <match attribute=\"level\" type=\"equals\">ERROR</match>\n" + "    </condition>\n" + "    <report publisher=\"file\"/>\n" + "    <plugin type=\"sampling\">\n" + "      <param name=\"attributes\">logger</param>\n" + "    </plugin>\n" + "  </group>";
        }

        public String getDefaultSamplingAttributes() {
            return "level,logger";
        }
    }

    private static class Factory extends LogEvent.Factory {

        private final LineNumberReader reader;

        private String curLine;

        public Factory(Description description, Reader reader, String logSource) throws FileNotFoundException {
            super(description, logSource);
            this.reader = new LineNumberReader(reader);
        }

        private static final Pattern DATE_PATTERN = Pattern.compile("\\A\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3} ");

        /**
         * Detect the start of a new log event
         */
        protected boolean detectLogEventStart(String line) {
            return DATE_PATTERN.matcher(line).find();
        }

        protected LogEvent readNextEvent() throws IOException, ParseException {
            if (curLine == null) {
                curLine = reader.readLine();
                if (curLine == null) {
                    return null;
                }
            }
            String firstLine = curLine;
            int lineNumber = reader.getLineNumber();
            String secondLine = null;
            StringBuffer buffer = new StringBuffer();
            while (((curLine = reader.readLine()) != null) && (!detectLogEventStart(curLine))) {
                if (secondLine == null) {
                    secondLine = curLine;
                } else {
                    buffer.append(NEWLINE);
                }
                buffer.append(curLine);
            }
            return BUILDER.buildLogEvent(this, lineNumber, firstLine, secondLine, buffer.toString());
        }

        private static final LogEventBuilder BUILDER = new LogEventBuilder() {

            protected LogEvent newEvent(LogEvent.Factory factory, String curLine, Object... objects) throws ParseException {
                return new JBossLogEvent(factory, curLine, (String) objects[0], (String) objects[1]);
            }
        };
    }
}