public class PyramidTest extends BaseExample {
    @Override
    public void initTest() {
        setTitle("Pyramid");
        int count = 20;
        {
            BodyDef bd = new BodyDef();
            Body ground = m_world.createBody(bd);
            PolygonShape shape = new PolygonShape();
            shape.setAsEdge(new Vec2(-40.0f, 0f), new Vec2(40.0f, 0f));
            ground.createFixture(shape, 0.0f);
        }
        {
            float a = .5f;
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(a, a);
            Vec2 x = new Vec2(-7.0f, 0.75f);
            Vec2 y = new Vec2();
            Vec2 deltaX = new Vec2(0.5625f, 1.25f);
            Vec2 deltaY = new Vec2(1.125f, 0.0f);
            for (int i = 0; i < count; ++i) {
                y.set(x);
                for (int j = i; j < count; ++j) {
                    BodyDef bd = new BodyDef();
                    bd.type = BodyType.DYNAMIC;
                    bd.position.set(y);
                    Body body = m_world.createBody(bd);
                    body.createFixture(shape, 5.0f);
                    y.addLocal(deltaY);
                }
                x.addLocal(deltaX);
            }
        }
    }
    @Override
    public String getTestName() {
        return "Pyramid";
    }
}
