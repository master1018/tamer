public class GUI_Settings {
    private int frame_x = 50;
    private int frame_y = 50;
    private int frame_width = 700;
    private int slot_height = 20;
    private int decor_height = 20;
    private long number_of_rendered_hours = 7 * 24;
    private String gui_settings_file_name = "";
    private boolean ext_view = false;
    private boolean show_row_labels = false;
    public GUI_Settings() {
    }
    public GUI_Settings(int x, int y, int width, int sh, int dh, long nor_hours, boolean eview, boolean rl) {
        frame_x = x;
        frame_y = y;
        frame_width = width;
        slot_height = sh;
        decor_height = dh;
        number_of_rendered_hours = nor_hours;
        ext_view = eview;
        show_row_labels = rl;
    }
    public int get_frame_x() {
        return frame_x;
    }
    public void set_frame_x(int x) {
        frame_x = x;
        return;
    }
    public int get_frame_y() {
        return frame_y;
    }
    public void set_frame_y(int y) {
        frame_y = y;
        return;
    }
    public int get_frame_width() {
        return frame_width;
    }
    public void set_frame_width(int width) {
        frame_width = width;
        return;
    }
    public int get_slot_height() {
        return slot_height;
    }
    public void set_slot_height(int height) {
        slot_height = height;
        return;
    }
    public int get_decor_height() {
        return decor_height;
    }
    public void set_decor_height(int height) {
        decor_height = height;
        return;
    }
    public long get_number_of_rendered_hours() {
        return number_of_rendered_hours;
    }
    public void set_number_of_rendered_hours(long hours) {
        number_of_rendered_hours = hours;
        return;
    }
    public boolean get_ext_view() {
        return ext_view;
    }
    public void set_ext_view(boolean mode) {
        ext_view = mode;
        return;
    }
    public boolean get_show_row_labels() {
        return show_row_labels;
    }
    public void set_show_row_labels(boolean show) {
        show_row_labels = show;
        return;
    }
}
