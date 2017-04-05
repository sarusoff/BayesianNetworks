package bn.util;


import java.math.BigDecimal;
import java.math.RoundingMode;

public class Utils {

    public static double round(double num, int places){
        BigDecimal bd = new BigDecimal(num);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
