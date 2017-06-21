import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class RequestQueue {
    private BlockingQueue<Customer> customers;
    RequestQueue() {
        this.customers = new LinkedBlockingQueue<>();
    }
    synchronized Customer poll() {
        return customers.poll();
    }
    synchronized Customer peek() {
        return customers.peek();
    }
    synchronized boolean isEmpty() {
        return customers.isEmpty();
    }
    synchronized void offer(Customer customer) {
        customers.offer(customer);
    }
    void traverse(Taxi[] taxis) {
        Object[][] snapshot = new Object[TaxiSys.TAXINUM][5];
        //获取每辆车的快照
        for (int i = 0; i < TaxiSys.TAXINUM; i++) snapshot[i] = taxis[i].getSnapShot(0,false);
        for (Customer c:customers) {
            for (int i = 0; i < TaxiSys.TAXINUM; i++) {
                int taxiState = (int) snapshot[i][0];
                int taxiLoc = (int) snapshot[i][3];
                if (Math.abs(taxiLoc / TaxiSys.SIZE - c.getStart() / TaxiSys.SIZE) <= 2 &&
                        Math.abs(taxiLoc % TaxiSys.SIZE - c.getStart() % TaxiSys.SIZE) <= 2) {
                    if (!c.isLogged()) c.log(snapshot[i]);//请求发出时需记录
                    if (taxiState==State.READY) //向用户添加一辆出租车
                        c.addTaxi(taxis[i]);
                }
            }
            c.setLogged();
        }
    }
}
