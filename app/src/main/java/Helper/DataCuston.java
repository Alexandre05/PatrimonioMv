package Helper;

import java.text.SimpleDateFormat;

public class DataCuston {
    public static String dataAtual() {

        long data = System.currentTimeMillis();

        SimpleDateFormat simpleDateFormate = new SimpleDateFormat("dd/MM/yyyy");

        String dataString = simpleDateFormate.format(data);

        return dataString;


    }
}
