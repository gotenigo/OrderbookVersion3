package com.diy.orderbookmanager;

import com.diy.Side.Side;
import com.diy.domain.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/** All functions in this class should throw if given null parameters */
public interface OrderBookManager {



    /**
     *
     *
     *   Update the order theOrderBook each time the exchange a new OrderBook
     *
     *
     * @param order
     * @return boolean
     */
    boolean updateOrder(Order order);





    /**
     * Get the best price  (the Top Level) for the instrument and side.
     *    *
     *    * <p>For buy orders - the highest price
     *       *    For sell orders - the lowest price
     *
     * @param instrument identifier of an instrument
     * @param side either buy or sell
     * @return Optional<BigDecimal> the best price, or Optional.empty() if there're no orders for the instrument on this
     *     side
     */
    Optional<BigDecimal> getBestPrice(String instrument, Side side);

    /**
     * Get all orders for the instrument on given side with given price
     *
     * <p>Result should contain orders in the same order as they arrive
     *
     * @param instrument identifier of an instrument
     * @param side either buy or sell
     * @param price requested price level
     * @return List<Order> all orders, or empty list if there are no orders for the instrument on this side with
     *     this price
     */
    List<Order> getOrdersAtLevel(String instrument, Side side, BigDecimal price);





    /**
     *
     * @param instrument
     * @param side
     * @param level
     * @return Map<BigDecimal, Set<Order>>
     */
    Map<BigDecimal, Set<Order>> getOrdersUpToLevel(String instrument, Side side, int level);

    /**
     *
     * @param instrument
     * @param side
     * @param level
     * @return Double
     */
    BigDecimal getAveragePriceOverLevel(String instrument, Side side, int level);


    /**
     *
     * @param instrument
     * @param side
     * @param level
     * @return Double
     */
    BigDecimal getTotalQtyOverLevel(String instrument, Side side, int level);



    /**
     *
     * @param instrument
     * @param side
     * @param level
     * @return  Map<BigDecimal,List<Double>>
     */
    Map<BigDecimal,List<Number>> getVolumeWeightedPriceOverLevel(String instrument, Side side, int level);


}
