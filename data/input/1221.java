public class JoglibCamera {
    private JoglibContext context;
    public JoglibCamera(JoglibContext context) {
        this.context = context;
    }
    public void setCameraBasic(float fieldOfViewAngleY, float distance) {
        final GL gl = context.getGL();
        final GLU glu = context.getGLU();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        if (0 == fieldOfViewAngleY) {
            fieldOfViewAngleY = 45;
        }
        final double widthHeightRatio = context.getCanvasWidthToHeightRatio();
        final double nearPlane = 0.1;
        final double farPlane = 10000000;
        glu.gluPerspective(fieldOfViewAngleY, widthHeightRatio, nearPlane, farPlane);
        double eyeX = 0;
        double eyeY = 0;
        double eyeZ = distance;
        double centerX = 0;
        double centerY = 0;
        double centerZ = 0;
        double upX = 0;
        double upY = 1;
        double upZ = 0;
        glu.gluLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    public void setCameraSphere(float fieldOfViewAngleY, float xAngle, float yAngle, float distance) {
        final GL gl = context.getGL();
        final GLU glu = context.getGLU();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        if (0 == fieldOfViewAngleY) {
            fieldOfViewAngleY = 45;
        }
        final double widthHeightRatio = context.getCanvasWidthToHeightRatio();
        final double nearPlane = 0.1;
        final double farPlane = 10000000;
        glu.gluPerspective(fieldOfViewAngleY, widthHeightRatio, nearPlane, farPlane);
        float phi = (float) toRadians(xAngle);
        float tau = (float) toRadians(yAngle);
        final float nin = (float) toRadians(90);
        double centerX = 0;
        double centerY = 0;
        double centerZ = 0;
        final double cosPhi = cos(phi);
        final double sinTau = sin(tau);
        final double sinPhi = sin(phi);
        final double cosTau = cos(tau);
        double eyeX = centerX + distance * cosPhi * sinTau;
        double eyeY = centerY + distance * sinPhi * sinTau;
        double eyeZ = centerZ + distance * cosTau;
        final double sinTauNin = sin(tau - nin);
        final double cosTauNin = cos(tau - nin);
        double upX = cosPhi * sinTauNin;
        double upY = sinPhi * sinTauNin;
        double upZ = cosTauNin;
        glu.gluLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
}
