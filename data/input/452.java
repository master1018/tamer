public class PlayerPosition2dPositionPidReq implements PlayerConstants {
    private float kp;
    private float ki;
    private float kd;
    public synchronized float getKp() {
        return this.kp;
    }
    public synchronized void setKp(float newKp) {
        this.kp = newKp;
    }
    public synchronized float getKi() {
        return this.ki;
    }
    public synchronized void setKi(float newKi) {
        this.ki = newKi;
    }
    public synchronized float getKd() {
        return this.kd;
    }
    public synchronized void setKd(float newKd) {
        this.kd = newKd;
    }
}
