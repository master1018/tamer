public abstract class OptionHandlerAbstract implements OptionHandler {
    public OptionHandlerAbstract() {
    }
    protected StringBuffer availableInstallers(final Object[] factories) {
        final StringBuffer types = new StringBuffer();
        for (int i = 0; i < factories.length; i++) {
            if (i > 0) {
                types.append("; ");
            }
            types.append(((Installer) factories[i]).getName());
        }
        return types;
    }
    protected List<String> getOptionValues(CommandLine commandLine, String opt) {
        List<String> list = new ArrayList<String>();
        String[] optionValues = commandLine.getOptionValues(opt);
        if (optionValues != null) {
            for (String optionValue : optionValues) {
                ListUtils.appendDelimitedStringToList(optionValue, list);
            }
        }
        return list;
    }
}
