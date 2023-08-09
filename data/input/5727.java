public class SetTransform{
    public static void main(String[] args) throws Exception {
        ModelDestination dest = new ModelDestination();
        ModelStandardTransform newtransform = new ModelStandardTransform();
        dest.setTransform(newtransform);
        if(dest.getTransform() != newtransform)
            throw new RuntimeException("dest.getTransform() is incorrect!");
    }
}
