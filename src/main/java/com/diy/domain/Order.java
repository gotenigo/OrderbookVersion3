package com.diy.domain;


import com.diy.Side.Side;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.sql.Timestamp;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class Order {


    /** identifier of an instrument */
    private String instrument;

    /** either buy or sell */
    private Side side;

    /** limit price for the order, always positive */
    private BigDecimal price;   // memory usage :  36 + Ceiling(log2(n)/8.0) bytes //https://stackoverflow.com/questions/2501176/java-bigdecimal-memory-usage

    /** required quantity, always positive */
    private Double quantity;  // memory usage :  36 + Ceiling(log2(n)/8.0) bytes

    private Timestamp timestamp;


    /** Hide the default ctor */
    private Order() {}





    /**
     * Copying ctor
     *
     * @param order an order to make copy from
     */
    public Order(Order order) {
        this( order.instrument, order.side, order.price, order.quantity, order.timestamp);

    }

    /**
     * All-values ctor
     *
     * @param instrument identifier of an instrument
     * @param side either buy or sell
     * @param price limit price for the order, always positive
     * @param quantity required quantity, always positive
     */
    public Order( String instrument, Side side, BigDecimal price, Double quantity, Timestamp timestamp) {

        requireNonNull(instrument);
        checkArgument(price.signum() > 0, "price must be positive");
        checkArgument(quantity > -1, "quantity must be positive");
        requireNonNull(timestamp);
        this.instrument = instrument;
        this.side = side;
        this.price = price.setScale(2, RoundingMode.DOWN);
        this.quantity = BigDecimal.valueOf(quantity).setScale(2, RoundingMode.DOWN).doubleValue();
        this.timestamp=timestamp;
    }



    public String getInstrument() {
        return instrument;
    }
    public Side getSide() {
        return side;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public double getQuantity() {
        return quantity;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (! (price.compareTo(order.price)==0)) return false; // same price
        if (!instrument.equals(order.instrument)) return false;// same instrument
        if (timestamp != null ? !timestamp.equals(order.timestamp) : order.timestamp != null) return false; // null or same timestamp
        return side == order.side; // same side
    }


    @Override
    public int hashCode() {

        int result = (instrument != null ? instrument.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (side != null ? side.hashCode() : 0);
        result = 31 * result + (int) (price.longValue() ^ (price.longValue()>>> 32));
        return result;
    }


    @Override
    public String toString() {
        return "Order{" +
                "instrument='" + instrument + '\'' +
                ", side=" + side +
                ", price=" + price +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                '}';
    }


}