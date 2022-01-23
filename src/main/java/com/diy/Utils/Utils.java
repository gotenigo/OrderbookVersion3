package com.diy.Utils;

import com.diy.Side.Side;
import com.diy.domain.Order;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;


@Slf4j
public class Utils {


    /**
     *
     * @param order
     * @return
     */
    public static Order toOrder(String order){


        String[] arg = order.split("\\|");

        requireNonNull(arg[0].substring(2)); // timestamp
        String instrument=requireNonNull(arg[1].substring(2)); // Instrument

        BigDecimal price=new BigDecimal(arg[2].substring(2));  // price
        checkArgument(price.signum()> 0, "price must be positive");

        Double quantity=Double.parseDouble((arg[3].substring(2)));  // Qty
        checkArgument(quantity > -1, "quantity must be positive");

        requireNonNull(arg[4]); // Side

        Side side = null;
        switch(arg[4].substring(2).toLowerCase(Locale.ROOT)) {

            case "b":
                side=Side.BUY;
                break;

            case "s":
                side=Side.SELL;
                break;
        }

        //DateFormat simple = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");

        String timestampStr = arg[0].substring(2);
        long time=Long.valueOf(timestampStr).longValue();
        Timestamp timestamp= new Timestamp(time);

        Order vOrder = new Order( instrument, side, price, quantity,timestamp);
        //log.info("vOrder="+vOrder);

        return vOrder;

    }





    public static BigDecimal StringToBigDecimal(String price){

        BigDecimal pricecompare = new BigDecimal(price).setScale(2, RoundingMode.DOWN);
        return pricecompare;

    }


    public static Double randomQty(){

        double start = 0;
        double end = 10737418;
        Double random = new Random().nextDouble();

        double result = start + (random * (end - start));

        Double truncatedDouble = BigDecimal.valueOf(result)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        return truncatedDouble;
    }



    public static BigDecimal randomPrice(){

        BigDecimal min= new BigDecimal("0");
        BigDecimal max= new BigDecimal("1000");
        BigDecimal randomBigDecimal = min.add(new BigDecimal(Math.random()).multiply(max.subtract(min)));
        return randomBigDecimal.setScale(2,RoundingMode.DOWN);

    }


    public static Timestamp timestamp(){

        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        long millis = cal.getTimeInMillis();

        return new Timestamp(millis);
    }

}
