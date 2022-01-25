package com.diy.domain;

import com.diy.Side.Side;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public final class Order {


    /** identifier of an instrument */
    private final String instrument;

    /** either buy or sell */
    private final Side side;

    /** limit price for the order, always positive */
    private final BigDecimal price;   // memory usage :  36 + Ceiling(log2(n)/8.0) bytes
                                // BigDecimal hit the performance a bit, but it's very accurate in calculation.

    /** required quantity, always positive */
    private final BigDecimal quantity;  // memory usage :  36 + Ceiling(log2(n)/8.0) bytes
                                 // BigDecimal hit the performance a bit, but it's very accurate in calculation.

    private final Timestamp timestamp;



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
    public Order( String instrument, Side side, BigDecimal price, BigDecimal quantity, Timestamp timestamp) {

        requireNonNull(instrument);
        checkArgument(price.signum() > 0, "price must be positive");
        checkArgument(quantity.signum() >= 0 /*> -1*/, "quantity cant be negative");
        requireNonNull(timestamp);
        this.instrument = instrument;
        this.side = side;
        this.price = price.setScale(2, RoundingMode.DOWN);
        this.quantity = quantity.setScale(2, RoundingMode.DOWN);//BigDecimal.valueOf(quantity).setScale(2, RoundingMode.DOWN).doubleValue();
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
    public BigDecimal getQuantity() {
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