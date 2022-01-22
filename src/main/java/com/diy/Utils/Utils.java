package com.diy.Utils;

import com.diy.Side.Side;
import com.diy.domain.Order;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Locale;

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
        requireNonNull(arg[4]);

        Side side=null;
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




    /**
     *
     * @param price
     * @return
     */
    public static BigDecimal intToBigDecimal(int price){

        BigDecimal pricecompare = new BigDecimal(price).setScale(2, RoundingMode.DOWN);
        //System.out.print("pricecompare="+pricecompare);

        return pricecompare;

    }



    public static BigDecimal StringToBigDecimal(String price){

        BigDecimal pricecompare = new BigDecimal(price).setScale(2, RoundingMode.DOWN);
        //System.out.print("pricecompare="+pricecompare);

        return pricecompare;

    }




}
