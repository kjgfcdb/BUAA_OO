import java.util.LinkedList;

public class FileManager {
    private static LinkedList<ObjFile> history = new LinkedList<>();
    public static ObjFile getNewFile(String path) {
        ObjFile temp = new ObjFile(path);
        for (ObjFile old:history) {
            if (old.getAbsolutePath().equals(temp.getAbsolutePath())) {
                return old;
            }
        }
        if (!temp.getAbsolutePath().equals("")) history.offer(temp);
        return temp;
    }
}
