import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;


public class IFTTT {
    public static void main(String args[]) {
        /*
        //A simple example for testing.
        ObjFile folder = FileManager.getNewFile("D:/folder");
        folder.createNewFolder();
        ObjFile test = FileManager.getNewFile("D:/folder/test.txt");
        test.createNewFile();
        test.rewrite("Hello world\r\ndlrow olleH");
        test.renameTo("D:/folder/hello.txt");
        //test.suicide();
        //IF,D:/folder/hello.txt,modified,THEN,record-detail
        */


        Scanner input = new Scanner(System.in);
        LinkedBlockingQueue<Req> reqs = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<FileSystemMonitor> monitors = new LinkedBlockingQueue<>();
        Summary summary = new Summary();
        Detail detail = new Detail();
        Recover recover = new Recover();

        try {
            while (true) {
                String str_in = input.nextLine();
                if (str_in.equals("run")) break;
                String[] commands = str_in.trim().split(";", -1);
                for (String command : commands) {
                    if (!InputHandler.parseInput(command, reqs))
                        System.out.println("Invalid request -> " + command);
                }
            }
            for (Req req : reqs) {
                FileSystemMonitor curMonitor = null;
                for (FileSystemMonitor monitor : monitors) {
                    if (monitor.getPath().equals(req.getFilePath())) {
                        curMonitor = monitor;
                        break;
                    }
                }
                if (curMonitor == null) {
                    monitors.offer(new FileSystemMonitor(req, summary, detail, recover));
                } else {
                    curMonitor.addTask(req);
                }
            }
            boolean sum = false;
            for (FileSystemMonitor monitor : monitors) {
                if (monitor.hasSummary()) sum = true;
                monitor.start();
            }
            if (sum) summary.start();//有任务要求才会Summary
            while (true) {
                String end = input.nextLine();
                if (end.equals("end")) {
                    summary.close();
                    detail.close();
                    input.close();
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            System.out.println("Monitor exploded.");
            System.exit(0);
        }
    }
}
