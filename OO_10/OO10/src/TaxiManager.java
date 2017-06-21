import java.util.ArrayList;

class TaxiManager {
    /**
     * @Overview:出租车管理器提供查询任意出租车的能力
     */
    private Taxi[] taxis;
    public boolean repOK() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==(taxis!=null)&&(taxis[i]!=null for all 0<=i<taxis.length);
         */
        if (taxis==null) return false;
        for (int i=0;i<taxis.length;i++) {
            if (taxis[i] == null) return false;
        }
        return true;
    }

    TaxiManager(Taxi[] taxis) {
        /*
         * @REQUIRES:
         *      taxis!=null;
         * @MODIFIES:
         *      \this.taxis;
         * @EFFECTS:
         *      \this.taxis == taxis;
         */
        this.taxis = taxis;
    }

    ArrayList<Taxi> queryTaxi(int _state) {
        /*
         * @REQUIRES:
         *      0<=_state<=3;
         * @MODIFIES:None;
         * @EFFECTS:
         *      ArrayList<Taxi> temp = new ArrayList<>();
         *      \all int i;0<=i<TaxiSys.TAXINUM && taxis[i].getState()==_state;temp.add(taxis[i]);
         *      \result==temp;
         */
        ArrayList<Taxi> temp = new ArrayList<>();
        for (int i = 0; i < TaxiSys.TAXINUM; i++) {
            if (taxis[i].getState() == _state)
                temp.add(taxis[i]);
        }
        return temp;
    }

    //获取编号为i的出租车的状态
    int getState(int i) {
        /*
         * @REQUIRES:
         *      0<=i<TaxiSys.TAXINUM;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==taxis[i].getState();
         */
        return taxis[i].getState();
    }
}
