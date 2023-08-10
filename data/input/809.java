public class UploadController extends AppController {
    public void index() {
    }
    @POST
    public void save() throws IOException {
        Iterator<FormItem> iterator = uploadedFiles();
        String fileContent = "", fileName = "";
        String fieldContent = "", fieldName = "";
        while (iterator.hasNext()) {
            FormItem item = iterator.next();
            if (item.isFile()) {
                fileName = item.getFileName();
                fileContent = Util.read(item.getInputStream());
            } else {
                fieldName = item.getFieldName();
                fieldContent = Util.read(item.getInputStream());
            }
        }
        flash("file_name", fileName);
        flash("file_content", fileContent);
        flash("field_name", fieldName);
        flash("field_content", fieldContent);
        redirect(UploadController.class);
    }
}
