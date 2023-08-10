public class DateConverter extends StrutsTypeConverter {
    public static final String FORMAT_DATA = "yyyy-MM-dd";
    public static final String FORMAT_TIME = "yyyy-MM-dd HH:mm:ss";
    @SuppressWarnings("rawtypes")
    @Override
    public Object convertFromString(Map context, String[] values, Class toClass) {
        if (values == null || values.length == 0) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_TIME);
        Date date = null;
        String dateString = values[0];
        if (dateString != null) {
            try {
                date = sdf.parse(dateString);
            } catch (ParseException e) {
                date = null;
            }
            if (date == null) {
                sdf = new SimpleDateFormat(FORMAT_DATA);
                try {
                    date = sdf.parse(dateString);
                } catch (ParseException e) {
                    date = null;
                }
            }
        }
        return date;
    }
    @SuppressWarnings("rawtypes")
    @Override
    public String convertToString(Map context, Object o) {
        if (o instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_TIME);
            return sdf.format((Date) o);
        }
        return "";
    }
}
