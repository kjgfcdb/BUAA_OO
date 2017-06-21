import java.util.*;
import java.util.regex.*;
public class ElevatorSchedule {
    public static void main(String [] args) {
        Scanner input = new Scanner(System.in);
        ALSScheduler sch = new ALSScheduler();
        Pattern p1 = Pattern.compile("^\\(FR,\\+?\\d+,UP,\\+?\\d+\\)$");
        Pattern p2 = Pattern.compile("^\\(FR,\\+?\\d+,DOWN,\\+?\\d+\\)$");
        Pattern p3 = Pattern.compile("^\\(ER,\\+?\\d+,\\+?\\d+\\)$");
        Matcher m1,m2,m3;
        int instrNum = 0;
        try {
            while (true) {
                String s = input.nextLine();
                s = s.replaceAll(" ", "");

                if (s.equals("q") || instrNum==100000)//超出100000条指令即自动截断
                    break;
                instrNum++;
                if (illegalStr(s)) {
                    System.out.println("Invalid ["+s+"]");
                    continue;
                }
                m1 = p1.matcher(s);//强行匹配，匹配不到就算错
                m2 = p2.matcher(s);
                m3 = p3.matcher(s);
                boolean hasfound = (m1.find()||m2.find()||m3.find());
                String[] strs = s.split("[,()]");
                if (hasfound && strs.length == 5 && strs[0].equals("")) {//可能是楼层命令
                    if (sch.checkFloor(strs[1], strs[2], strs[3], strs[4]) == 0)
                        System.out.println("INVALID ["+s+"]");

                } else if (hasfound && strs.length == 4 && strs[0].equals("")) {//可能是电梯命令
                    if (sch.checkElevator(strs[1], strs[2], strs[3]) == 0) {
                        System.out.println("INVALID ["+s+"]");
                    }
                } else {
                    System.out.println("INVALID ["+s+"]");
                }

            }
            sch.compute();
        } catch (Exception e) {
            System.out.println("Terrible Input");
        }
        input.close();
    }
    private static boolean illegalStr(String s) {
        String eng = "EFRUPDOWN";
        int flag=0;
        for (int i=0;i<s.length();i++) {
            for (int j=0;j<eng.length();j++) {
                if (s.charAt(i)==eng.charAt(j)) {
                    flag=1;
                }
            }
            if (!((s.charAt(i)>='0' && s.charAt(i)<='9')||
                    s.charAt(i)=='(' || s.charAt(i)==')'||
                    s.charAt(i)==',' || s.charAt(i)=='+'||
                    flag==1)) {
                return true;
            }
        }
        return false;
    }
}

