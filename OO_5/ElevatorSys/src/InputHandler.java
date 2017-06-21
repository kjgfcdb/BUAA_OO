import java.util.regex.Matcher;
import java.util.regex.Pattern;

class InputHandler {
    public static boolean parseString(String s) {
        Pattern p1 = Pattern.compile("^\\(FR,\\+?\\d+,(UP|DOWN)\\)$");//(FR,1,UP)
        Pattern p2 = Pattern.compile("^\\(ER,#\\+?\\d+,\\+?\\d+\\)$");//(ER,#1,3)
        try {
            Matcher m1 = p1.matcher(s);
            Matcher m2 = p2.matcher(s);
            if (!m1.find() && !m2.find()) return false;
            String[] strs = s.split("[,()]");
            if (strs.length!=4) return false;
            if (strs[1].equals("FR")) {
                if (Integer.parseInt(strs[2])>=Floor.getMinFloor() && Integer.parseInt(strs[2])<=Floor.getMaxFloor() &&(
                        strs[3].equals("UP") || strs[3].equals("DOWN"))) {
                    if ((Integer.parseInt(strs[2])==1 && strs[3].equals("DOWN") ) ||
                            (Integer.parseInt(strs[2])==20 && strs[3].equals("UP") )) return false;
                    return true;
                }
            }
            if (strs[1].equals("ER")) {
                if (Integer.parseInt(strs[3])>=Floor.getMinFloor() && Integer.parseInt(strs[3])<=Floor.getMaxFloor() &&
                        Integer.parseInt(strs[2].substring(1))>0 && Integer.parseInt(strs[2].substring(1))<4) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
