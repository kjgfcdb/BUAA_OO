import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class RequestQueue {
    private static int cnt;
    private BlockingQueue<Customer> customers;

    RequestQueue() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:
         *      \this.customers;
         * @EFFECTS:
         *      \this.customers = new LinkedBlockingQueue<>(300);
         */
        this.customers = new LinkedBlockingQueue<>(300);
    }

    synchronized Customer poll() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==customers.poll();
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        return customers.poll();
    }

    synchronized Customer peek() {
        /**
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==customers.peek();
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         */
        return customers.peek();
    }

    synchronized boolean isEmpty() {
        /**
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==customers.isEmpty();
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         *
         */
        return customers.isEmpty();
    }

    synchronized void offer(Customer customer) {
        /**
         * @REQUIRES:
         *      customer!=null;
         * @MODIFIES:
         *      customer;
         *      \this.cnt;
         *      \this.customers;
         * @EFFECTS:
         *      \all Customer c;c belongs to customers && !c.equals(customer);customers.offer(customer) && customer.setId(cnt++);
         *      \exists Customer c;c belongs to customers && c.equals(customer);return;
         * @THREAD_REQUIRES:
         *      \locked(\this);
         * @THREAD_EFFECTS:
         *      \locked();
         *
         */
        for (Customer cust : customers)
            if (cust.equals(customer))
                return;
        customers.offer(customer);
        customer.setId(cnt++);
    }

    void traverse(Taxi[] taxis) {
        /*
         * @REQUIRES:
         *      taxis!=null;
         * @MODIFIES:
         *      \this.customers;
         * @EFFECTS:
         *      遍历taxis中的出租车获取它们的当前快照，再遍历customers将距离合适的出租车
         *      添加到用户的考虑队列中，再将该用户添加到该出租车的用户队列中，如果是该用户
         *      第一次记录信息则记录该用户周围出租车状态。
         */
        if (taxis==null) return;
        Object[][] snapshot = new Object[TaxiSys.TAXINUM][5];
        //获取每辆车的快照
        for (int i = 0; i < TaxiSys.TAXINUM; i++) snapshot[i] = taxis[i].getSnapShot(0, false);
        for (Customer c : customers) {
            for (int i = 0; i < TaxiSys.TAXINUM; i++) {
                int taxiState = (int) snapshot[i][0];
                int taxiLoc = (int) snapshot[i][3];
                if (Math.abs(taxiLoc / TaxiSys.SIZE - c.getStart() / TaxiSys.SIZE) <= 2 &&
                        Math.abs(taxiLoc % TaxiSys.SIZE - c.getStart() % TaxiSys.SIZE) <= 2) {
                    taxis[i].addCredit(c);
                    if (!c.isLogged()) c.log(snapshot[i]);//请求发出时需记录
                    if (taxiState == State.READY) //向用户添加一辆出租车
                        c.addTaxi(taxis[i]);
                }
            }
            c.setLogged();
        }
    }
}
