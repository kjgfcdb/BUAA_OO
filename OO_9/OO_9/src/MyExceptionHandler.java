class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    public void uncaughtException(Thread t, Throwable e) {
        /**
         * @REQUIRES:
         *      t!=null;
         *      e!=null;
         * @MODIFIES:None;
         * @EFFECTS:
         *      Catch the exception caused by threads.
         */
        System.out.println("Exception Caught by MyUncaughtException Handler");
    }
}
