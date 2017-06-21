import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

class Detail {
    private PrintWriter printWriter;
    private LinkedBlockingQueue<String> content;
    Detail() {
        try {
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter("detail.txt")));
        } catch (Exception e) {
            System.out.println("无法创建detail.txt!");
        }
        content = new LinkedBlockingQueue<>();
    }

    public synchronized void register(int i,Object o) {
        if (i==0) {//renamed
            for (FileMap fileMap:((LinkedList<FileMap>)o)) {
                content.offer(new Date().toString()+" : "+fileMap.getKey().getAbsolutePath()+
                        " was renamed to "+fileMap.getValue().getAbsolutePath());
                System.out.println(new Date().toString()+" : "+fileMap.getKey().getAbsolutePath()+
                        " was renamed to "+fileMap.getValue().getAbsolutePath());
            }
        } else if (i==1) {//modified
            for (ObjFile objFile:((LinkedBlockingQueue<ObjFile>)o)) {
                if (objFile.getLastModifiedTime()!=objFile.getModifiedTime()) {
                    Date d1 = new Date(objFile.getLastModifiedTime());
                    Date d2 = new Date(objFile.getModifiedTime());
                    content.offer(new Date().toString() + " : " + objFile +
                            " was modified from " + d1 + " to " + d2);
                    System.out.println(new Date().toString() + " : " + objFile +
                            " was modified from " + d1 + " to " + d2);
                }
            }
        } else if (i==2) {//path-changed
            for (FileMap fileMap:((LinkedList<FileMap>)o)) {
                content.offer(new Date().toString()+" : "+fileMap.getKey().getAbsolutePath()+
                        " was pathChanged to "+fileMap.getValue().getAbsolutePath());
                System.out.println(new Date().toString()+" : "+fileMap.getKey().getAbsolutePath()+
                        " was pathChanged to "+fileMap.getValue().getAbsolutePath());
            }
        } else {//size-changed
            for (ObjFile objFile:((LinkedBlockingQueue<ObjFile>)o)) {
                content.offer(new Date().toString()+" : "+objFile+
                        " was sizeChanged from "+objFile.getLastLength()+" bytes to "+
                        objFile.getLength()+" bytes.");
                System.out.println(new Date().toString()+" : "+objFile+
                        " was sizeChanged from "+objFile.getLastLength()+" bytes to "+
                        objFile.getLength()+" bytes.");
            }
        }
    }
    public synchronized void print() {
        while (!content.isEmpty()) {
            printWriter.println(content.poll());
            printWriter.flush();
        }
    }
    public synchronized void close() {
        printWriter.close();
    }
}
