import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

class Customer{
    private int start;
    private int end;
    private long time;
    private static int cnt;
    private int id;
    private boolean logged;//控制首次记录
    private PrintWriter out;
    private Set<Taxi> taxis = new LinkedHashSet<>();

    Customer(int start,int end,long time) {
        this.id = cnt++;
        this.start = start;
        this.end = end;
        this.time = time;
        this.logged = false;
        try{
            out = new PrintWriter(new BufferedWriter(new FileWriter("log"+id+".txt")),true);
        } catch (Exception e) {
            System.out.println("IO Exception occured!");
        }
        out.println(this+" First Logging :");
    }

    public String toString() {
        return "Customer#"+id;
    }
    int getStart() {
        return start;
    }
    long getTime() {
        return time;
    }
    int getEnd() {
        return end;
    }
    boolean equals(Customer c) {
        return this.time==c.time &&
                this.start == c.start &&
                this.end == c.end;
    }
    synchronized void addTaxi(Taxi taxi) {
        taxis.add(taxi);
    }
    boolean tryToGetOn() {//是否能上车
        Taxi result = null;
        Object[] save = new Object[5];
        out.println("Taxis tried to grap "+this+"'s order: ");
        int format = 0;
        for (Taxi t:taxis) {
            out.print(t+"\t");
            format++;
            if (format==5) {
                out.print("\n");
                format = 0;
            }
            Object[] objects = t.getSnapShot(start,true);//0:state,1:credit,2:path
            if ((int)(objects[0])==State.READY) {
                if (result==null) {
                    result = t;
                    for (int l=0;l<3;l++) save[l] = objects[l];
                }
                else {
                    if ((int)(objects[1])>(int)(save[1])) {
                        result = t;
                        for (int l=0;l<3;l++) save[l] = objects[l];
                    }
                    else if (objects[2].toString().length()<
                            save[2].toString().length()) {
                        result = t;
                        for (int l=0;l<3;l++) save[l] = objects[l];
                    }
                }
            }
        }
        if (result!=null) {
            String path = result.addCust(this);
            out.println("\nTaxi chosen by "+this+" : "+result);
            out.println("Path found for "+this+" :");
            int x = start/TaxiSys.SIZE+1;
            int y = start%TaxiSys.SIZE+1;
            format = 1;
            out.printf(">>>>(%d,%d)\t",x,y);
            for (int i=0;i<path.length();i++) {
                if (path.charAt(i)=='U') out.printf("--->(%d,%d)\t",--x,y);
                if (path.charAt(i)=='D') out.printf("--->(%d,%d)\t",++x,y);
                if (path.charAt(i)=='L') out.printf("--->(%d,%d)\t",x,--y);
                if (path.charAt(i)=='R') out.printf("--->(%d,%d)\t",x,++y);
                if (format==4) out.println();
                format = (format+1)%5;
            }
            out.close();
            return true;
        }
        else return false;
    }
    boolean isLogged() {
        return logged;
    }
    void setLogged() {
        if (logged) return;
        out.println();
        logged = true;
    }
    void log(Object[] snapshot) {
        String state = (int)snapshot[0]==State.IDLE? "IDLE":
                (int)snapshot[0]==State.TOCUS? "TOCUS":
                        (int)snapshot[0]==State.TODES? "TODES":
                                "READY";
        int credit = (int)snapshot[1];
        int loc = (int)snapshot[3];
        int id = (int)snapshot[4];
        out.println(//请求发出时周围车辆状态
                 "Taxi#" +id +"\tState : "+ state +
                         "\tLocation : ("+(loc/TaxiSys.SIZE+1)
                        +","+(loc%TaxiSys.SIZE+1)+")"
                        +"\tCredit : "+credit
        );
        out.flush();
    }
}
