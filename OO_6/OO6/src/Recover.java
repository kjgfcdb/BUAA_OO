import java.io.*;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class Recover {
    public synchronized void recover(boolean isRenamed,boolean isPathChanged,
                                     LinkedList<FileMap> renamed,
                                     LinkedList<FileMap> pathChanged) {
        if (!isRenamed && !isPathChanged) return;
        LinkedBlockingQueue<FileMap> total = new LinkedBlockingQueue<>();//获取总的链表
        if (isRenamed) {
            for (FileMap fileMap:renamed) total.offer(fileMap);
        }
        if (isPathChanged) {
            for (FileMap fileMap:pathChanged) total.offer(fileMap);
        }
        while (!total.isEmpty()) {
            FileMap fileMap = total.poll();
            ObjFile newFile = fileMap.getValue();
            ObjFile oldFile = fileMap.getKey();
            //开始恢复
            copy(new File(newFile.getAbsolutePath()),new File(oldFile.getAbsolutePath()));
            try {
                new File(oldFile.getAbsolutePath()).setLastModified(oldFile.getLastModifiedTime());
            } catch (Exception e) {
            }
            System.out.println("Recovery completed.");
            boolean find = false;
            for (FileMap newFileMap:total) {
                if (newFileMap.getValue().getAbsolutePath().equals(newFile.getAbsolutePath())) find = true;
            }
            if (!find) newFile.suicide();
        }
    }
    public static void copy(File src,File dst) {
        int length=1048576;
        try {
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(dst);
            byte[] buffer = new byte[length];
            while (true) {
                int ins = in.read(buffer);
                if (ins == -1) {
                    in.close();
                    out.flush();
                    out.close();
                    break;
                } else out.write(buffer, 0, ins);
            }
        } catch (Exception e) {
        }
    }
}
