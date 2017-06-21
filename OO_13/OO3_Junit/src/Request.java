class Request{
    private String _type;
    private int _floor;
    private int _upDown;//upDown=1表示上，upDown=0表示下
    private long _T;//时刻
    private int len;
    private int alreadyOut;//已经输出
    private int duplicatedOut;//重复输出
    private int invalidOut;//无效输出
    private int order;
    Request(String type,int floor,long T) {//电梯内请求
        this._type = type;
        this._floor = floor;
        this._T = T;
        len = 3;
    }
    Request(String type,int floor,int upDown,long T){//楼层请求
        this._type = type;
        this._floor = floor;
        this._upDown = upDown;
        this._T = T;
        len = 4;
    }
    int getOrder() {return order;}
    void setOrder(int o) {order = o;}
    int getLen() {//返回请求长度，相当于返回请求种类
        return len;
    }
    int getFloor() {
        return this._floor;
    }
    long getTime() {
        return this._T;
    }
    int getUpDown() {
        return _upDown;
    }
    int getAlreadyOut() {return alreadyOut;}
    void setAlreadyOut(int x) {alreadyOut = x;}
    int getDuplicatedOut() {return duplicatedOut;}
    void setDuplicatedOut(int x) {duplicatedOut = x;}
    int getInvalidOut() {return invalidOut;}
    void setInvalidOut(int x) {invalidOut = x;}
    String getType() {return _type;}
    public String toString() {
        if (len==4) {
            return "["+_type+","+_floor+","+(_upDown==1?"UP":"DOWN")+","+_T+"]";
        } else return "["+_type+","+_floor+","+_T+"]";
    }
}
