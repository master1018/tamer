public class Bug4527203 {
    public static void main(String[] args) {
        Calendar huCalendar = Calendar.getInstance(new Locale("hu","HU"));
        int hufirstDayOfWeek = huCalendar.getFirstDayOfWeek();
        if (hufirstDayOfWeek != Calendar.MONDAY) {
            throw new RuntimeException();
        }
        Calendar ukCalendar = Calendar.getInstance(new Locale("uk","UA"));
        int ukfirstDayOfWeek = ukCalendar.getFirstDayOfWeek();
        if (ukfirstDayOfWeek != Calendar.MONDAY) {
            throw new RuntimeException();
        }
    }
}
