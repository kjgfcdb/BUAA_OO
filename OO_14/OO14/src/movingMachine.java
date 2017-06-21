interface movingMachine{
    abstract void update(Request r);
    abstract int getCurPos();
    abstract double getComTime();
    abstract int getState(Request r);
}
