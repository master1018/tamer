public class PVRect extends PDisplayBase implements iGObject {
    vrect me;
    public PVRect(GCore core, String name, vrect primitive, iGObject parent) {
        this.me = primitive;
        this.Initialize(this, core, name);
    }
    @Override
    public void Draw(PApplet handler, DisplayMode mode) {
        super.Draw(handler, mode);
        handler.pushMatrix();
        handler.translate(me.p1.x, -me.z, -me.p1.y);
        handler.rotateY(me.angle);
        handler.rectMode(handler.CORNER);
        handler.rect(0, 0, me.distance, -me.height);
        handler.popMatrix();
    }
    @Override
    public Object GetPrimitive(Class type) {
        if (type == me.getClass()) return me; else return null;
    }
}
