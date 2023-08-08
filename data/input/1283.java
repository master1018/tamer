public class Median3D extends VtkImageFilter {
    private final vtkImageMedian3D filter;
    private static final String HELP_STRING = "This is a wrapper for vtkImageMedian3D filter.\n" + "vtkImageMedian3D a Median filter that replaces each pixel with the median " + "value from a rectangular neighborhood around that pixel. Neighborhoods " + "can be no more than 3 dimensional. Setting one axis of the neighborhood " + "  kernelSize to 1 changes the filter into a 2D median.";
    public Median3D() {
        filter = new vtkImageMedian3D();
    }
    public void setKernelSize(final int dx, final int dy, final int dz) {
        filter.SetKernelSize(dx, dy, dz);
    }
    public int[] getKernelSize() {
        return filter.GetKernelSize();
    }
    public void update() throws VTKException {
        final vtkImageData inputImageData = transferToVTK(inputImage);
        filter.SetInput(inputImageData);
        filter.Update();
        outputImage = transferFromVTK(filter.GetOutput());
    }
    public String getHelpString() {
        return HELP_STRING;
    }
}
