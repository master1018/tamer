public class ICalendar {
    private static final String TAG = "Sync";
    public static class FormatException extends Exception {
        public FormatException() {
            super();
        }
        public FormatException(String msg) {
            super(msg);
        }
        public FormatException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
    public static class Component {
        private static final String BEGIN = "BEGIN";
        private static final String END = "END";
        private static final String NEWLINE = "\n";
        public static final String VCALENDAR = "VCALENDAR";
        public static final String VEVENT = "VEVENT";
        public static final String VTODO = "VTODO";
        public static final String VJOURNAL = "VJOURNAL";
        public static final String VFREEBUSY = "VFREEBUSY";
        public static final String VTIMEZONE = "VTIMEZONE";
        public static final String VALARM = "VALARM";
        private final String mName;
        private final Component mParent; 
        private LinkedList<Component> mChildren = null;
        private final LinkedHashMap<String, ArrayList<Property>> mPropsMap =
                new LinkedHashMap<String, ArrayList<Property>>();
        public Component(String name, Component parent) {
            mName = name;
            mParent = parent;
        }
        public String getName() {
            return mName;
        }
        public Component getParent() {
            return mParent;
        }
        protected LinkedList<Component> getOrCreateChildren() {
            if (mChildren == null) {
                mChildren = new LinkedList<Component>();
            }
            return mChildren;
        }
        public void addChild(Component child) {
            getOrCreateChildren().add(child);
        }
        public List<Component> getComponents() {
            return mChildren;
        }
        public void addProperty(Property prop) {
            String name= prop.getName();
            ArrayList<Property> props = mPropsMap.get(name);
            if (props == null) {
                props = new ArrayList<Property>();
                mPropsMap.put(name, props);
            }
            props.add(prop);
        }
        public Set<String> getPropertyNames() {
            return mPropsMap.keySet();
        }
        public List<Property> getProperties(String name) {
            return mPropsMap.get(name);
        }
        public Property getFirstProperty(String name) {
            List<Property> props = mPropsMap.get(name);
            if (props == null || props.size() == 0) {
                return null;
            }
            return props.get(0);
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            sb.append(NEWLINE);
            return sb.toString();
        }
        public void toString(StringBuilder sb) {
            sb.append(BEGIN);
            sb.append(":");
            sb.append(mName);
            sb.append(NEWLINE);
            for (String propertyName : getPropertyNames()) {
                for (Property property : getProperties(propertyName)) {
                    property.toString(sb);
                    sb.append(NEWLINE);
                }
            }
            if (mChildren != null) {
                for (Component component : mChildren) {
                    component.toString(sb);
                    sb.append(NEWLINE);
                }
            }
            sb.append(END);
            sb.append(":");
            sb.append(mName);
        }
    }
    public static class Property {
        public static final String DTSTART = "DTSTART";
        public static final String DTEND = "DTEND";
        public static final String DURATION = "DURATION";
        public static final String RRULE = "RRULE";
        public static final String RDATE = "RDATE";
        public static final String EXRULE = "EXRULE";
        public static final String EXDATE = "EXDATE";
        private final String mName;
        private LinkedHashMap<String, ArrayList<Parameter>> mParamsMap =
                new LinkedHashMap<String, ArrayList<Parameter>>();
        private String mValue; 
        public Property(String name) {
            mName = name;
        }
        public Property(String name, String value) {
            mName = name;
            mValue = value;
        }
        public String getName() {
            return mName;
        }
        public String getValue() {
            return mValue;
        }
        public void setValue(String value) {
            mValue = value;
        }        
        public void addParameter(Parameter param) {
            ArrayList<Parameter> params = mParamsMap.get(param.name);
            if (params == null) {
                params = new ArrayList<Parameter>();
                mParamsMap.put(param.name, params);
            }
            params.add(param);
        }
        public Set<String> getParameterNames() {
            return mParamsMap.keySet();
        }
        public List<Parameter> getParameters(String name) {
            return mParamsMap.get(name);
        }
        public Parameter getFirstParameter(String name) {
            ArrayList<Parameter> params = mParamsMap.get(name);
            if (params == null || params.size() == 0) {
                return null;
            }
            return params.get(0);
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            return sb.toString();
        }
        public void toString(StringBuilder sb) {
            sb.append(mName);
            Set<String> parameterNames = getParameterNames();
            for (String parameterName : parameterNames) {
                for (Parameter param : getParameters(parameterName)) {
                    sb.append(";");
                    param.toString(sb);
                }
            }
            sb.append(":");
            sb.append(mValue);
        }
    }
    public static class Parameter {
        public String name;
        public String value;
        public Parameter() {
        }
        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            return sb.toString();
        }
        public void toString(StringBuilder sb) {
            sb.append(name);
            sb.append("=");
            sb.append(value);
        }
    }
    private static final class ParserState {
        public String line; 
        public int index;
    }
    private ICalendar() {
    }
    private static String normalizeText(String text) {
        text = text.replaceAll("\r\n", "\n");
        text = text.replaceAll("\r", "\n");
        text = text.replaceAll("\n ", "");
        return text;
    }
    private static Component parseComponentImpl(Component component,
                                                String text)
            throws FormatException {
        Component current = component;
        ParserState state = new ParserState();
        state.index = 0;
        String[] lines = text.split("\n");
        for (String line : lines) {
            try {
                current = parseLine(line, state, current);
                if (component == null) {
                    component = current;
                }
            } catch (FormatException fe) {
                if (Config.LOGV) {
                    Log.v(TAG, "Cannot parse " + line, fe);
                }
            }
            continue;
        }
        return component;
    }
    private static Component parseLine(String line, ParserState state,
                                       Component component)
            throws FormatException {
        state.line = line;
        int len = state.line.length();
        char c = 0;
        for (state.index = 0; state.index < len; ++state.index) {
            c = line.charAt(state.index);
            if (c == ';' || c == ':') {
                break;
            }
        }
        String name = line.substring(0, state.index);
        if (component == null) {
            if (!Component.BEGIN.equals(name)) {
                throw new FormatException("Expected BEGIN");
            }
        }
        Property property;
        if (Component.BEGIN.equals(name)) {
            String componentName = extractValue(state);
            Component child = new Component(componentName, component);
            if (component != null) {
                component.addChild(child);
            }
            return child;
        } else if (Component.END.equals(name)) {
            String componentName = extractValue(state);
            if (component == null ||
                    !componentName.equals(component.getName())) {
                throw new FormatException("Unexpected END " + componentName);
            }
            return component.getParent();
        } else {
            property = new Property(name);
        }
        if (c == ';') {
            Parameter parameter = null;
            while ((parameter = extractParameter(state)) != null) {
                property.addParameter(parameter);
            }
        }
        String value = extractValue(state);
        property.setValue(value);
        component.addProperty(property);
        return component;
    }
    private static String extractValue(ParserState state)
            throws FormatException {
        String line = state.line;
        if (state.index >= line.length() || line.charAt(state.index) != ':') {
            throw new FormatException("Expected ':' before end of line in "
                    + line);
        }
        String value = line.substring(state.index + 1);
        state.index = line.length() - 1;
        return value;
    }
    private static Parameter extractParameter(ParserState state)
            throws FormatException {
        String text = state.line;
        int len = text.length();
        Parameter parameter = null;
        int startIndex = -1;
        int equalIndex = -1;
        while (state.index < len) {
            char c = text.charAt(state.index);
            if (c == ':') {
                if (parameter != null) {
                    if (equalIndex == -1) {
                        throw new FormatException("Expected '=' within "
                                + "parameter in " + text);
                    }
                    parameter.value = text.substring(equalIndex + 1,
                                                     state.index);
                }
                return parameter; 
            } else if (c == ';') {
                if (parameter != null) {
                    if (equalIndex == -1) {
                        throw new FormatException("Expected '=' within "
                                + "parameter in " + text);
                    }
                    parameter.value = text.substring(equalIndex + 1,
                                                     state.index);
                    return parameter;
                } else {
                    parameter = new Parameter();
                    startIndex = state.index;
                }
            } else if (c == '=') {
                equalIndex = state.index;
                if ((parameter == null) || (startIndex == -1)) {
                    throw new FormatException("Expected ';' before '=' in "
                            + text);
                }
                parameter.name = text.substring(startIndex + 1, equalIndex);
            }
            ++state.index;
        }
        throw new FormatException("Expected ':' before end of line in " + text);
    }
    public static Component parseCalendar(String text) throws FormatException {
        Component calendar = parseComponent(null, text);
        if (calendar == null || !Component.VCALENDAR.equals(calendar.getName())) {
            throw new FormatException("Expected " + Component.VCALENDAR);
        }
        return calendar;
    }
    public static Component parseEvent(String text) throws FormatException {
        Component event = parseComponent(null, text);
        if (event == null || !Component.VEVENT.equals(event.getName())) {
            throw new FormatException("Expected " + Component.VEVENT);
        }
        return event;
    }
    public static Component parseComponent(String text) throws FormatException {
        return parseComponent(null, text);
    }
    public static Component parseComponent(Component component, String text)
        throws FormatException {
        text = normalizeText(text);
        return parseComponentImpl(component, text);
    }
}
