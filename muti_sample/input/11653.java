public class ReadTCPTimeoutsImpl implements ReadTimeouts
{
    private int initial_time_to_wait;
    private int max_time_to_wait;
    private int max_giop_header_time_to_wait;
    private double backoff_factor;
    public ReadTCPTimeoutsImpl(int initial_time,
                            int max_time,
                            int max_giop_header_time,
                            int backoff_percent) {
        this.initial_time_to_wait = initial_time;
        this.max_time_to_wait = max_time;
        this.max_giop_header_time_to_wait = max_giop_header_time;
        this.backoff_factor = 1 + (double)(backoff_percent)/100;
    }
    public int get_initial_time_to_wait() { return initial_time_to_wait; }
    public int get_max_time_to_wait() { return max_time_to_wait; }
    public double get_backoff_factor() { return backoff_factor; }
    public int get_max_giop_header_time_to_wait() {
        return max_giop_header_time_to_wait; }
}
