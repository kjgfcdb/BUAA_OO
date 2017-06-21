import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
enum State{
    IDLE,SERVING,RUNNING,STILL
}
enum Direction{
    UP,DOWN,STILL
}

public class ElevatorSys {
    private static long startTime;//系统开始时间
    public static long getStartTime() {return startTime;}
    public static void main(String args[]) {
        try {
            Scanner input = new Scanner(System.in);
            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(new BufferedWriter(new FileWriter("result.txt")));
            } catch (Exception e) {
                System.exit(0);//如果发生IO异常，则强行退出程序
            }
            ReqQue reqQue = new ReqQue(printWriter);
            Floor floor = new Floor();
            Elv elv1 = new Elv(1, printWriter,floor);
            Elv elv2 = new Elv(2, printWriter,floor);
            Elv elv3 = new Elv(3, printWriter,floor);
            DecimalFormat df = new DecimalFormat("0.0");
            Schedule schedule = new Schedule(reqQue, elv1, elv2, elv3,floor,printWriter);
            boolean firstTime = true;
            while (true) {
                String str = input.nextLine();
                if (str.equals("q")) break;
                str = str.replaceAll(" ", "");
                String[] strs = str.split("[;]", -1);
                long curTime = 0;
                if (firstTime) {
                	startTime = new Date().getTime();
                	firstTime = false;
                }
                else curTime = new Date().getTime() - startTime;
                reqQue.append(strs,curTime);
            }
            input.close();
            printWriter.close();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Terrible Input");
            System.exit(0);
        }
    }
}