    static class Option implements JavacOption {
        OptionName name;
        String argsNameKey;
        String descrKey;
        boolean hasSuffix;
        ChoiceKind choiceKind;
        Map<String,Boolean> choices;
        Option(OptionName name, String argsNameKey, String descrKey) {
            this.name = name;
            this.argsNameKey = argsNameKey;
            this.descrKey = descrKey;
            char lastChar = name.optionName.charAt(name.optionName.length()-1);
            hasSuffix = lastChar == ':' || lastChar == '=';
        }
        Option(OptionName name, String descrKey) {
            this(name, null, descrKey);
        }
        Option(OptionName name, String descrKey, ChoiceKind choiceKind, String... choices) {
            this(name, descrKey, choiceKind, createChoices(choices));
        }
        private static Map<String,Boolean> createChoices(String... choices) {
            Map<String,Boolean> map = new LinkedHashMap<String,Boolean>();
            for (String c: choices)
                map.put(c, false);
            return map;
        }
        Option(OptionName name, String descrKey, ChoiceKind choiceKind,
                Map<String,Boolean> choices) {
            this(name, null, descrKey);
            if (choiceKind == null || choices == null)
                throw new NullPointerException();
            this.choiceKind = choiceKind;
            this.choices = choices;
        }
        @Override
        public String toString() {
            return name.optionName;
        }
        public boolean hasArg() {
            return argsNameKey != null && !hasSuffix;
        }
        public boolean matches(String option) {
            if (!hasSuffix)
                return option.equals(name.optionName);
            if (!option.startsWith(name.optionName))
                return false;
            if (choices != null) {
                String arg = option.substring(name.optionName.length());
                if (choiceKind == ChoiceKind.ONEOF)
                    return choices.keySet().contains(arg);
                else {
                    for (String a: arg.split(",+")) {
                        if (!choices.keySet().contains(a))
                            return false;
                    }
                }
            }
            return true;
        }
        void help(PrintWriter out) {
            String s = "  " + helpSynopsis();
            out.print(s);
            for (int j = Math.min(s.length(), 28); j < 29; j++) out.print(" ");
            Log.printLines(out, Main.getLocalizedString(descrKey));
        }
        String helpSynopsis() {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            if (argsNameKey == null) {
                if (choices != null) {
                    String sep = "{";
                    for (Map.Entry<String,Boolean> e: choices.entrySet()) {
                        if (!e.getValue()) {
                            sb.append(sep);
                            sb.append(e.getKey());
                            sep = ",";
                        }
                    }
                    sb.append("}");
                }
            } else {
                if (!hasSuffix)
                    sb.append(" ");
                sb.append(Main.getLocalizedString(argsNameKey));
            }
            return sb.toString();
        }
        void xhelp(PrintWriter out) {}
        public boolean process(Options options, String option, String arg) {
            if (options != null) {
                if (choices != null) {
                    if (choiceKind == ChoiceKind.ONEOF) {
                        for (String s: choices.keySet())
                            options.remove(option + s);
                        String opt = option + arg;
                        options.put(opt, opt);
                        String nm = option.substring(0, option.length() - 1);
                        options.put(nm, arg);
                    } else {
                        for (String a: arg.split(",+")) {
                            String opt = option + a;
                            options.put(opt, opt);
                        }
                    }
                }
                options.put(option, arg);
            }
            return false;
        }
        public boolean process(Options options, String option) {
            if (hasSuffix)
                return process(options, name.optionName, option.substring(name.optionName.length()));
            else
                return process(options, option, option);
        }
        public OptionKind getKind() { return OptionKind.NORMAL; }
        public OptionName getName() { return name; }
    };
    static class XOption extends Option {
        XOption(OptionName name, String argsNameKey, String descrKey) {
            super(name, argsNameKey, descrKey);
        }
        XOption(OptionName name, String descrKey) {
            this(name, null, descrKey);
        }
        XOption(OptionName name, String descrKey, ChoiceKind kind, String... choices) {
            super(name, descrKey, kind, choices);
        }
        XOption(OptionName name, String descrKey, ChoiceKind kind, Map<String,Boolean> choices) {
            super(name, descrKey, kind, choices);
        }
        @Override
        void help(PrintWriter out) {}
        @Override
        void xhelp(PrintWriter out) { super.help(out); }
        @Override
        public OptionKind getKind() { return OptionKind.EXTENDED; }
    };
    static class HiddenOption extends Option {
        HiddenOption(OptionName name) {
            super(name, null, null);
        }
        HiddenOption(OptionName name, String argsNameKey) {
            super(name, argsNameKey, null);
        }
        @Override
        void help(PrintWriter out) {}
        @Override
        void xhelp(PrintWriter out) {}
        @Override
        public OptionKind getKind() { return OptionKind.HIDDEN; }
    };
}
