import java.util.LinkedHashSet;
import java.util.Set;

class Customer {
    private int start;
    private int end;
    private long time;
    private int id;
    private boolean logged;//控制首次记录
    private MyPrintWriter out;
    private StringBuilder buffer;
    private Set<Taxi> taxis = new LinkedHashSet<>();

    Customer(int start, int end, long time, MyPrintWriter out) {
        /**
         * @REQUIRES:0<=start<=6399 && 0<=end<=6399
         * @MODIFIES:
         *      \this.start;
         *      \this.end;
         *      \this.time;
         *      \this.logged;
         *      \this.out;
         *      \this.buffer;
         * @EFFECTS:
         *      \this.start==start;
         *      \this.end==end;
         *      \this.time==time-time%100;
         *      \this.logged==false;
         *      \this.out==out;
         *      \this.buffer==new StringBuilder();
         */
        this.start = start;
        this.end = end;
        this.time = time-time%100;
        this.logged = false;
        this.out = out;
        this.buffer = new StringBuilder();
    }

    public String toString() {
        /**
         * @REQUIRES:None
         * @MODIFIES:None
         * @EFFECTS:
         *      \result==("Customer#"+id);
         */
        return "Customer#" + id;
    }

    int getId() {
        /**
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==(id);
         */
        return id;
    }

    void setId(int id) {
        /**
         * @REQUIRES:None;
         * @MODIFIES:\this.id
         * @EFFECTS:
         *      \this.id==id;
         *      \this.buffer.append("Customer#"+id+" First Logging :\n");
         */
        this.id = id;
        String s = "Customer#"+id+" First Logging :\n";
        buffer.append(s);
    }

    int getStart() {
        /**
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==(\this.start);
         */
        return start;
    }

    long getTime() {
        /**
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==(\this.time);
         */
        return time;
    }

    int getEnd() {
        /**
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==(\this.end);
         */
        return end;
    }

    boolean equals(Customer c) {
        /**
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == (c!=null && \this.time == c.time &&
         *          \this.start == c.start && \this.end == c.end);
         */
        return c!=null &&
                this.time == c.time &&
                this.start == c.start &&
                this.end == c.end;
    }

    synchronized void addTaxi(Taxi taxi) {
        /**
         * @REQUIRES:taxi!=null
         * @MODIFIES:\this.taxis;
         * @EFFECTS:
         *      \this.taxis.add(taxi);
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        if (taxi!=null) taxis.add(taxi);
    }

    boolean tryToGetOn() {//是否能上车
        /**
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.buffer;
         * @EFFECTS:
         *      \this.taxis.length==0 ==> \result=false;
         *      \exists Taxi t;(t in taxis)&&(t.state == READY);\result==true;
         *      \all Taxi t;(t in taxis)&&(t.state != READY);\result==false;
         */
        Taxi result = null;
        Object[] save = new Object[5];
        buffer.append("Taxis tried to grap ");
        buffer.append(this);
        buffer.append("'s order: \n");
        int format = 0;
        for (Taxi t : taxis) {
            buffer.append(t);
            buffer.append("\t");
            format++;
            if (format == 5) {
                buffer.append("\n");
                format = 0;
            }
            Object[] objects = t.getSnapShot(start, true);//0:state,1:credit,2:path
            if ((int) (objects[0]) == State.READY) {
                if (result == null) {
                    result = t;
                    for (int l = 0; l < 3; l++) save[l] = objects[l];
                } else {
                    if ((int)(objects[1]) > (int)(save[1])) {
                        result = t;
                        for (int l = 0; l < 3; l++) save[l] = objects[l];
                    } else if (((int)(objects[1]) == (int)(save[1])) &&
                    		(objects[2].toString().length() < save[2].toString().length())) {
                        result = t;
                        for (int l = 0; l < 3; l++) save[l] = objects[l];
                    }
                }
            }
        }
        if (result != null) {
            String path = result.addCust(this);
            String appendix = "\nTaxi chosen by "+this+
                    " : "+result+"\nPath found for "+
                    this+" :\n";
            buffer.append(appendix);
            int x = start / TaxiSys.SIZE + 1;
            int y = start % TaxiSys.SIZE + 1;
            format = 1;
            printPath(x, y);
            for (int i = 0; i < path.length(); i++) {
                if (path.charAt(i) == 'U') printPath(--x, y);
                if (path.charAt(i) == 'D') printPath(++x, y);
                if (path.charAt(i) == 'L') printPath(x, --y);
                if (path.charAt(i) == 'R') printPath(x, ++y);
                if (format == 4) buffer.append("\n");
                format = (format + 1) % 5;
            }
            out.print(buffer.toString() + "\n-----------------------------------\n");
            return true;
        } else return false;
    }

    boolean isLogged() {
        /**
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==logged;
         */
        return logged;
    }

    void setLogged() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:\this.logged;
         * @EFFECTS:
         *      \this.logged ==> do nothing;
         *      !\this.logged ==> (\this.buffer.append("\n") && \this.logged==true);
         */
        if (logged) return;
        buffer.append("\n");
        logged = true;
    }

    private void printPath(int x, int y) {
        /**
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.buffer;
         * @EFFECTS:
         *      \this.buffer.append(">>>>("+x+","+y+")\t");
         */
        String s = ">>>>("+x+","+y+")\t";
        buffer.append(s);
    }

    void logFailure() {
        /**
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.out;
         * @EFFECTS:
         *      MyPrintWriter prints the content in the buffer and flushes;
         */
        out.print(buffer.toString() + "\n-----------------------------------\n");
    }

    void log(Object[] snapshot) {
        /**
         * @REQUIRES:snapshot!=null
         * @MODIFIES:\this.buffer
         * @EFFECTS:
         *      \this.buffer.append("Taxi#" + id + "\tState : " + state +
         *      "\tLocation : (" + (loc / TaxiSys.SIZE + 1)
         *      + "," + (loc % TaxiSys.SIZE + 1) + ")"
         *      + "\tCredit : " + credit + "\n");
         */
        if (snapshot==null) return;
        String state = (int) snapshot[0] == State.IDLE ? "IDLE" :
                (int) snapshot[0] == State.TOCUS ? "TOCUS" :
                        (int) snapshot[0] == State.TODES ? "TODES" :
                                "READY";
        int credit = (int) snapshot[1];
        int loc = (int) snapshot[3];
        int id = (int) snapshot[4];
        String output = "Taxi#" + id + "\tState : " + state +
                "\tLocation : (" + (loc / TaxiSys.SIZE + 1)
                + "," + (loc % TaxiSys.SIZE + 1) + ")"
                + "\tCredit : " + credit + "\n";
        buffer.append(output);
    }
}
