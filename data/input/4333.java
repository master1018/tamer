public class ListTimeZones{
  public static void main(String[] args){
    Date date = new Date();
    String TimeZoneIds[] = TimeZone.getAvailableIDs();
    for(int i = 0; i < TimeZoneIds.length; i++){
      TimeZone tz = TimeZone.getTimeZone(TimeZoneIds[i]);
      Calendar calendar = new GregorianCalendar(tz);
      String calString = calendar.toString();
    }
  }
}
