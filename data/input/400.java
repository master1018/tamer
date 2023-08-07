public class DailyEnergy implements Command {
    protected static final String NAME = "daily-energy";
    protected static final String ARGS = "[tower | lounge] [date]";
    protected static final String DESCRIPTION = "Returns the energy in kWh used by the tower or lounge for the specified date (yyyy-mm-dd).";
    @Override
    public String execute(String... args) throws CommandFailedException {
        if (args.length < 2) {
            throw new InsufficientArgumentsException();
        }
        String location = args[0];
        String dateAsString = args[1];
        if ((!Server.isTower(location)) && (!Server.isLounge(location))) {
            throw new CommandFailedException(location + " is not the name of a tower or lounge.");
        }
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateAsString);
        } catch (ParseException e) {
            throw new CommandFailedException(dateAsString + " is not a valid date, or date format yyyy-MM-dd.", e);
        }
        try {
            XMLGregorianCalendar startOfDay = Tstamp.makeTimestamp(date.getTime());
            startOfDay.setTime(0, 0, 0);
            XMLGregorianCalendar endOfDay = Tstamp.incrementDays(Tstamp.makeTimestamp(date.getTime()), 1);
            endOfDay.setTime(0, 0, 0);
            double energy = this.getEnergyConsumed(location, startOfDay, endOfDay, 0);
            String output = location + "'s energy consumption for " + dateAsString + " was: ";
            output += String.format("%.1f kWh.\n", energy / 1000);
            return output;
        } catch (WattDepotClientException e) {
            throw new CommandFailedException("Could not gather needed data.", e);
        }
    }
    @Override
    public String getHelp() {
        return NAME + " " + ARGS + "\n\t " + DESCRIPTION + "\n";
    }
    @Override
    public String getName() {
        return NAME;
    }
    public double getEnergyConsumed(String location, XMLGregorianCalendar startOfDay, XMLGregorianCalendar endOfDay, int interval) throws WattDepotClientException {
        return Server.getClient().getEnergyConsumed(location, startOfDay, endOfDay, interval);
    }
}
