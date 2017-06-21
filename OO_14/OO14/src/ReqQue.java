import java.util.ArrayList;

class ReqQue{
    /**
     * Overview:请求队列类，用于存放请求
     * AF(c) = {que} where c.que = que;
     */
    public ArrayList<Request> que;
    ReqQue() {
        /**
         * @MODIFIES:\this.que;
         * @EFFECTS:\this.que = new ArrayList<Request>();
         */
        que = new ArrayList<Request>();
    }

    public boolean repOK() {
        /**
         * @invariant:
         *      que!=null;
         * @EFFECTS:
         *      \result==que!=null;
         */
        return que!=null;
    }

    void addReq(Request r) {
        /**
         * @MODIFIES:\this.que;
         * @EFFECTS:
         *      \this.que.size() = \old(\this).que.size()+1 &&
         *      \this.que.contains(r);
         */
        que.add(r);
    }

    int size() {
        /**
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==\this.que.size();
         */
        return que.size();
    }

    Request get(int i) {
        /**
         * @MODIFIES:None;
         * @EFFECTS:
         *      (i<0 || i>=que.size()) ==> \result==null;
         *      (0<=i&&i<que.size()) ==> \result==que.get(i);
         */
        if (i<0 || i>=que.size()) return null;
        return que.get(i);
    }
}
