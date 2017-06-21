package OO15;

class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    /*
     * @Overview:捕捉线程产生的异常。
     */

    public boolean repOK() {
        /*
         * @REQUIRES:None;
         * @MODIFIES:None;
         * @EFFECTS:
         *      \result==true;
         */
        return true;
    }

    public void uncaughtException(Thread t, Throwable e) {
        /*
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
