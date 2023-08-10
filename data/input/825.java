public class AntiliaDateTimeField extends FormComponentPanel<Date> {
    private static class AM_PM extends EnumeratedType {
        private static final long serialVersionUID = 1L;
        static final AM_PM AM = new AM_PM("AM");
        static final AM_PM PM = new AM_PM("PM");
        public static AM_PM[] values() {
            return new AM_PM[] { AM, PM };
        }
        private AM_PM(final String name) {
            super(name);
        }
    }
    private static final IConverter MINUTES_CONVERTER = new ZeroPaddingIntegerConverter(2);
    private static final long serialVersionUID = 1L;
    private AM_PM amOrPm = AM_PM.AM;
    private DropDownChoice<AM_PM> amOrPmChoice;
    private MutableDateTime date;
    private DateTextField dateField;
    private Integer hours;
    private TextField<Integer> hoursField;
    private Integer minutes;
    private TextField<Integer> minutesField;
    public AntiliaDateTimeField(String id) {
        this(id, null);
    }
    @SuppressWarnings("unchecked")
    public AntiliaDateTimeField(String id, IModel<Date> model) {
        super(id, model);
        setType(Date.class);
        PropertyModel<Date> dateFieldModel = new PropertyModel<Date>(this, "date");
        add(dateField = newDateTextField("date", dateFieldModel));
        dateField.add(new AntiliaDatePicker() {
            private static final long serialVersionUID = 1L;
            protected void configure(Map<String, Object> widgetProperties) {
                super.configure(widgetProperties);
                AntiliaDateTimeField.this.configure(widgetProperties);
            }
        });
        add(hoursField = new TextField<Integer>("hours", new PropertyModel<Integer>(this, "hours"), Integer.class));
        hoursField.add(new HoursValidator());
        hoursField.setLabel(new Model<String>("hours"));
        add(minutesField = new TextField<Integer>("minutes", new PropertyModel<Integer>(this, "minutes"), Integer.class) {
            private static final long serialVersionUID = 1L;
            @Override
            public IConverter getConverter(Class type) {
                return MINUTES_CONVERTER;
            }
        });
        minutesField.add(new RangeValidator<Integer>(0, 59));
        minutesField.setLabel(new Model<String>("minutes"));
        add(amOrPmChoice = new DropDownChoice<AM_PM>("amOrPmChoice", new PropertyModel<AM_PM>(this, "amOrPm"), Arrays.asList(AM_PM.values())));
    }
    public AM_PM getAmOrPm() {
        return amOrPm;
    }
    public Date getDate() {
        return (date != null) ? date.toDate() : null;
    }
    public Integer getHours() {
        return hours;
    }
    protected void configure(Map<String, Object> widgetProperties) {
    }
    @Override
    public String getInput() {
        return dateField.getInput() + ", " + hoursField.getInput() + ":" + minutesField.getInput();
    }
    public Integer getMinutes() {
        return minutes;
    }
    public void setAmOrPm(AM_PM amOrPm) {
        this.amOrPm = amOrPm;
    }
    public void setDate(Date date) {
        this.date = (date != null) ? new MutableDateTime(date) : null;
        setDefaultModelObject(date);
    }
    public void setHours(Integer hours) {
        this.hours = hours;
    }
    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }
    protected TimeZone getClientTimeZone() {
        ClientInfo info = Session.get().getClientInfo();
        if (info instanceof WebClientInfo) {
            return ((WebClientInfo) info).getProperties().getTimeZone();
        }
        return null;
    }
    @Override
    protected void convertInput() {
        Object dateFieldInput = dateField.getConvertedInput();
        if (dateFieldInput != null) {
            MutableDateTime date = new MutableDateTime(dateFieldInput);
            Integer hours = (Integer) hoursField.getConvertedInput();
            Integer minutes = (Integer) minutesField.getConvertedInput();
            AM_PM amOrPm = amOrPmChoice.getConvertedInput();
            try {
                boolean use12HourFormat = use12HourFormat();
                if (hours != null) {
                    date.set(DateTimeFieldType.hourOfDay(), hours.intValue() % getMaximumHours(use12HourFormat));
                    date.setMinuteOfHour((minutes != null) ? minutes.intValue() : 0);
                }
                if (use12HourFormat) {
                    date.set(DateTimeFieldType.halfdayOfDay(), amOrPm == AM_PM.PM ? 1 : 0);
                }
                TimeZone zone = getClientTimeZone();
                if (zone != null) {
                    date.setMillis(DateTimeZone.getDefault().getMillisKeepLocal(DateTimeZone.forTimeZone(zone), date.getMillis()));
                }
                setConvertedInput(date.toDate());
            } catch (RuntimeException e) {
                AntiliaDateTimeField.this.error(e.getMessage());
                invalid();
            }
        } else {
            setConvertedInput(null);
        }
    }
    protected DateTextField newDateTextField(String id, PropertyModel<Date> dateFieldModel) {
        return new DateTextField(id, dateFieldModel, new StyleDateConverter(false));
    }
    @Override
    protected void onBeforeRender() {
        dateField.setRequired(isRequired());
        hoursField.setRequired(isRequired());
        minutesField.setRequired(isRequired());
        dateField.setEnabled(isEnabled() && isEnableAllowed());
        hoursField.setEnabled(isEnabled() && isEnableAllowed());
        minutesField.setEnabled(isEnabled() && isEnableAllowed());
        amOrPmChoice.setEnabled(isEnabled() && isEnableAllowed());
        boolean use12HourFormat = use12HourFormat();
        amOrPmChoice.setVisible(use12HourFormat);
        Date d = (Date) getDefaultModelObject();
        if (d != null) {
            date = new MutableDateTime(d);
        } else {
            date = null;
        }
        if (date != null) {
            TimeZone zone = getClientTimeZone();
            if (zone != null) {
                date.setZone(DateTimeZone.forTimeZone(zone));
            }
            if (use12HourFormat) {
                int hourOfHalfDay = date.get(DateTimeFieldType.hourOfHalfday());
                hours = new Integer(hourOfHalfDay == 0 ? 12 : hourOfHalfDay);
            } else {
                hours = new Integer(date.get(DateTimeFieldType.hourOfDay()));
            }
            amOrPm = (date.get(DateTimeFieldType.halfdayOfDay()) == 0) ? AM_PM.AM : AM_PM.PM;
            minutes = new Integer(date.getMinuteOfHour());
        }
        super.onBeforeRender();
    }
    protected boolean use12HourFormat() {
        String pattern = DateTimeFormat.patternForStyle("-S", getLocale());
        return pattern.indexOf('a') != -1 || pattern.indexOf('h') != -1 || pattern.indexOf('K') != -1;
    }
    private int getMaximumHours() {
        return getMaximumHours(use12HourFormat());
    }
    private int getMaximumHours(boolean use12HourFormat) {
        return use12HourFormat ? 12 : 24;
    }
    private class HoursValidator extends RangeValidator<Integer> {
        private static final long serialVersionUID = 1L;
        public HoursValidator() {
            if (getMaximumHours() == 24) {
                setRange(0, 23);
            } else {
                setRange(1, 12);
            }
        }
    }
}
