import java.util.ArrayList;

class ReqQue{
    private ArrayList<Request> que;
    ReqQue() {
        /**
         * @REQUIRES:None;
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
         * @REQUIRES:r!=null;
         * @MODIFIES:\this.que;
         * @EFFECTS:
         *      \this.que.size() = \old(\this).que.size()+1 &&
         *      \this.que.contains(r);
         */
        que.add(r);
    }

    int size() {
        /**
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==\this.que.size();
         */
        return que.size();
    }

    Request get(int i) {
        /**
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      (i<0 || i>=que.size()) ==> \result==null;
         *      (0<=i&&i<que.size()) ==> \result==que.get(i);
         */
        if (i<0 || i>=que.size()) return null;
        return que.get(i);
    }
}
