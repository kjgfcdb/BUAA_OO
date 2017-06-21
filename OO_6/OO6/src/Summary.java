import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class Summary implements Runnable{
    private PrintWriter printWriter;
    private int renamed;
    private int modified;
    private int pathChanged;
    private int sizeChanged;
    private Thread t;
    private boolean turnOn;
    Summary() {
        turnOn = true;
        t = new Thread(this);
        t.setUncaughtExceptionHandler(new MyExceptionHandler());
        try {
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter("summary.txt")));
        } catch (Exception e) {
            System.out.println("创建summary.txt文件失败！");
        }
    }
    public synchronized void register(int i,Object o) {
        if (o==null) return;;
        if (i==0) {//renamed
            for (FileMap fileMap : ((LinkedList<FileMap>) o)) renamed++;
        }
        if (i==1) {//modified
            for (ObjFile objFile:((LinkedBlockingQueue<ObjFile>)o)) modified++;
        }
        if (i==2) {//pathChanged
            for (FileMap fileMap:((LinkedList<FileMap>)o)) pathChanged++;
        }
        if (i==3) {//sizeChanged
            for (ObjFile objFile:((LinkedBlockingQueue<ObjFile>)o)) sizeChanged++;
        }
    }
    public void run() {
        while (true) {
            try{
                Thread.sleep(10000);
            } catch (Exception e) {}
            if (!turnOn) break;
            printWriter.println(new Date()+"\trenamed:\t"+renamed+
                    "\tmodified:\t"+modified+"\tpath-changed:\t"+pathChanged+
                    "\tsizeChanged:\t"+sizeChanged+"\n");
            printWriter.flush();
        }
    }
    public synchronized void close() {
        turnOn = false;
        printWriter.close();
    }
    public void start() {
        t.start();
    }
}
