class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{
    public void uncaughtException(Thread t,Throwable e) {
        System.out.println("Exception Caught by MyUncaughtException Handler");
    }
}
