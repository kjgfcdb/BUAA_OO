package OO15;

class Scheduler implements Runnable {
    /**
     * @Overview:调度器类处理过期用户请求，并记录未过期请求的周围车辆情况。
     */
    private RequestQueue customers;
    private Taxi[] taxis;
    private Thread t;

    Scheduler(RequestQueue customerRequest,Taxi [] taxis) {
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
         *      \this.t == new Thread(this,"Scheduler);
         *      \this.t.setUncaughtExceptionHandler(new MyExceptionHandler());
         */
        this.customers = customerRequest;
        this.taxis = taxis;
        t = new Thread(this,"Scheduler");
        t.setUncaughtExceptionHandler(new MyExceptionHandler());
    }

    public boolean repOK() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result == (customers!=null) && (taxiGUI!=null) && (taxis!=null) && (t!=null) &&
         *                 (taxis[i]!=null for all 0<=i<TaxiSys.TAXINUM);
         */
        if (taxis==null) return false;
        for (int i=0;i<TaxiSys.TAXINUM;i++)
            if (taxis[i]==null) return false;
        return (customers!=null) && (t!=null);
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
            long t = gv.getTime();
            while (!customers.isEmpty() && customers.peek().getTime() + 3000 <= t) {
                Customer c = customers.poll();
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
