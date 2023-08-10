public abstract class DurationCalculator {
    public static long calculateDuration(Reservation[] reservations) {
        long duration = 0;
        for (Reservation reservation : reservations) {
            duration += calculateDuration(reservation);
        }
        return duration;
    }
    public static long calculateDuration(Reservation reservation) {
        return calculateDuration(reservation.getAppointments());
    }
    public static long calculateDuration(Appointment[] appointments) {
        long duration = 0;
        for (Appointment appointment : appointments) {
            duration += calculateDuration(appointment);
        }
        return duration;
    }
    public static long calculateDuration(Appointment appointment) {
        long duration = 0;
        if (appointment.getRepeating() != null && appointment.getRepeating().getEnd() == null) {
            return 999999;
        }
        if (appointment.getRepeating() == null) {
            duration = DateTools.countMinutes(appointment.getStart(), appointment.getEnd());
        } else {
            List<AppointmentBlock> splits = new ArrayList<AppointmentBlock>();
            appointment.createBlocks(appointment.getStart(), DateTools.fillDate(appointment.getMaxEnd()), splits);
            for (AppointmentBlock block : splits) {
                duration += DateTools.countMinutes(block.getStart(), block.getEnd());
            }
        }
        return duration;
    }
}
