class Scheduler implements Runnable{
    private RequestQueue customers;
    private Taxi[] taxis;
    private Thread t;
    Scheduler(RequestQueue customerRequest,Taxi[] taxis) {
        this.customers = customerRequest;
        this.taxis =taxis;
        t = new Thread(this);
        t.setUncaughtExceptionHandler(new MyExceptionHandler());
    }
    public void run() {
        while (true) {
            long t = System.currentTimeMillis();
            while (!customers.isEmpty() && customers.peek().getTime()+3000<=t) {
                Customer c = customers.poll();
                if (c.tryToGetOn()) System.out.println(c+" successfully got on a taxi.");
                else System.out.println(c+" failed to get on a taxi!");
            }
            //对所有时间未到的用户请求来说，现在还处于窗口期，可以继续加出租车进去
            customers.traverse(taxis);
        }
    }
    public void start() {
        t.start();
    }
}
