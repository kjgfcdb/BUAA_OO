import java.awt.*;

class Scheduler implements Runnable {
    private RequestQueue customers;
    private TaxiGUI taxiGUI;
    private Taxi[] taxis;
    private Thread t;

    Scheduler(RequestQueue customerRequest, TaxiGUI taxiGUI,Taxi [] taxis) {
        /*
         * @REQUIRES:
         *      customerRequest!=null;
         *      taxiGUI!=null;
         *      taxis!=null;
         * @MODIFIES:
         *      \this.customers;
         *      \this.taxiGUI;
         *      \this.taxis;
         *      \this.t;
         * @EFFECTS:
         *      \this.customers == customerRequest;
         *      \this.taxiGUI == taxiGUI;
         *      \this.taxis == taxis;
         *      \this.t == new Thread(this);
         *      \this.t.setUncaughtExceptionHandler(new MyExceptionHandler());
         */
        this.customers = customerRequest;
        this.taxiGUI = taxiGUI;
        this.taxis = taxis;
        t = new Thread(this);
        t.setUncaughtExceptionHandler(new MyExceptionHandler());
    }

    public void run() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.customers;
         *      \this.taxis;
         * @EFFECTS:
         *      不断查询当前用户队列，从中取出过期的用户请求并执行它，用户请求可能成功或者失败，
         *      将成功的用户请求分配给出租车，将失败的用户请求进行失败记录。随后遍历用户队列中
         *      剩余的用户请求，继续记录它们周围的出租车状态。
         */
        while (true) {
            long t = System.currentTimeMillis();
            while (!customers.isEmpty() && customers.peek().getTime() + 3000 <= t) {
                Customer c = customers.poll();
                taxiGUI.RequestTaxi(new Point(c.getStart() / TaxiSys.SIZE, c.getStart() % TaxiSys.SIZE),
                        new Point(c.getEnd() / TaxiSys.SIZE, c.getEnd() % TaxiSys.SIZE));
                if (c.tryToGetOn()) System.out.println(c + " successfully got on a taxi.");
                else {
                    c.logFailure();
                    System.out.println(c + " failed to get on a taxi!");
                }
            }
            //对所有时间未到的用户请求来说，现在还处于窗口期，可以继续加出租车进去
            customers.traverse(taxis);
        }
    }

    void start() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.t;
         * @EFFECTS:
         *      \this.t.start();
         */
        t.start();
    }
}
