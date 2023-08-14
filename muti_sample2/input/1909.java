public class test {
	public static String convertDateFormat(String date, String srcFormat, String destFormat) throws ParseException {
		DateFormat sFormat = new SimpleDateFormat(srcFormat);
		Date srcDate = sFormat.parse(date);
		DateFormat dFormat = new SimpleDateFormat(destFormat);
		String retval = dFormat.format(srcDate);
		return retval;
	}
}
