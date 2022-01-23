package com.diy.orderbookmanager;



import com.diy.Side.Side;
import com.diy.domain.Order;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.MoreCollectors.onlyElement;

@Slf4j
public class OrderBookList implements OrderBookManager  {


    private Map<String, OrderBook> orderBookMap;


    public OrderBookList() {
        this.orderBookMap = new ConcurrentHashMap<>();   //O(log n) pretty fast, your collection size has to be
                                                         // very extreme to notice a performance impact
    }



    /**
     * Add new order
     *
     * <p>Orders for the same instrument, on the same side, with the same price should be kept in the
     * order as they arrive
     *
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   16/01/2022
     *
     * @param order new order to add <br>
     * @see Order
     */
     private boolean addOrder(Order order) {

        log.info("=>addOrder order called for ="+order);
        boolean success=false;

        String instrument = order.getInstrument();

        if (this.hasInstrument(instrument)) {  // do we have already an Orderbook for that Instrument ?

            success = orderBookMap.get(instrument).addOrder(order);

        } else { // if no, then we create an Orderbook for that for specific Instrument

            OrderBook orderBook = new OrderBook(instrument);
            orderBook.addOrder(order);
            orderBookMap.put(instrument, orderBook);
            success=(orderBookMap.get(instrument)!=null)? true:false;

        }

        log.info("Add completed : successful =" + success);
        return success;
    }








    /**
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   16/01/2022
     *
     * Delete an existing order. Returns false if no such order exists
     *
     * @param order unique identifier of existing order
     * @return True if the order was successfully deleted, false otherwise
     */
    private boolean deleteOrder(Order order) {

        log.info("=>Delete order called for ="+order);
        boolean success = false;

        String instrument = order.getInstrument();

        if (this.hasInstrument(instrument)) {
            success = orderBookMap.get(instrument).deleteOrder(order);

            if(orderBookMap.get(instrument).isEmpty()){ // if there is no Orderbook left
                if(orderBookMap.remove(instrument)==null){ // then we delete the key (price)
                    log.error(" - orderBookList -Internal Error : key  "+instrument+" could not be deleted successfully !");
                    success=false;
                };
            }

        }else{
            log.info("I cant find any orderbook for Instrument under order ="+order);
        }

        log.info("Delete completed : success =" + success);
        return success;
    }







    /**
     *
     * @param order
     * @return
     */
    @Override
    public synchronized boolean updateOrder(Order order) {

        String instrument=order.getInstrument();
        Side side = order.getSide();
        BigDecimal price = order.getPrice();

        List<Order> orderList=getOrdersAtLevel(instrument, side, price);

        try {

            Order vOrder = orderList.stream()
                    .filter(x -> x.equals(order))
                    .collect(onlyElement());

            if (vOrder.getQuantity() == order.getQuantity()) {
                log.info("Transaction Ignored, nothing has changed on that order!"); // save time and improve perf
                return false;
            }

            if (order.getQuantity() > 0) { //•	If the quantity is not zero it means order book level at specified price needs to be updated (or inserted if it was not there)
                if (!deleteOrder(order)) return false;
                if (!addOrder(order)) return false;
            } else if (order.getQuantity() == 0) { //•	If the quantity is zero it means the order book level at specified price must be removed from the book
                if (!deleteOrder(order)) return false;
            }

        } catch (IllegalArgumentException  e) { //two or more elements
            log.error(" ! Internal Error, you cant have duplicate 'unique' Element in the orderBookMap :"+ orderBookMap);
            throw e;
        } catch (NoSuchElementException  e) {  //stream is empty
            if (!addOrder(order)) return false; //  We add order if it does not exist

        }

        return true;
    }








    /**
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   16/01/2022
     *
     *
     * @param instrument
     * @return
     */
    private boolean hasInstrument(String instrument){

        if (orderBookMap.containsKey(instrument)) {
            return true;
        }else{
            //log.info("This OrderBookList does not have any record for OrderBook "+instrument+" !");
            return false;
        }

    }






    /**
     * Get the best price for the instrument and side.
     * *
     * * <p>For buy orders - the highest price
     * *    For sell orders - the lowest price
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   16/01/2022
     *
     *
     * @param instrument identifier of an instrument
     * @param side       either buy or sell
     * @return the best price, or Optional.empty() if there're no orders for the instrument on this
     * side
     */
    @Override
    public Optional<BigDecimal> getBestPrice(String instrument, Side side) {

        Optional<BigDecimal> bestPrice = Optional.empty();

        if (this.hasInstrument(instrument)){
            bestPrice=orderBookMap.get(instrument).getBestPrice(instrument,side);
        }
        log.info("BestPrice="+bestPrice);
        return bestPrice;
    }










    /**
     *
     * @param instrument
     * @param side
     * @param level
     * @return
     */
    @Override
    public Map<BigDecimal, Set<Order>> getOrdersUpToLevel(String instrument, Side side, int level) {

        Map ordersUpToLeve = Collections.EMPTY_MAP;
        if (this.hasInstrument(instrument)){

            ordersUpToLeve=orderBookMap.get(instrument).getOrdersUpToLevel( instrument,  side,  level);
        }

        return ordersUpToLeve ;
    }





    /**
     *
     * @param instrument
     * @param side
     * @param level
     * @return
     */
    @Override
    public Double getAveragePriceOverLevel(String instrument, Side side, int level) {

        double averagePrice=0;
        if (this.hasInstrument(instrument)){

            averagePrice=orderBookMap.get(instrument).getAveragePriceOverLevel(   side,  level);
        }

        return averagePrice ;

    }




    /**
     *
     * @param instrument
     * @param side
     * @param level
     * @return
     */
    @Override
    public Double getTotalQtyOverLevel(String instrument, Side side, int level) {

        double totalQtyOverLevel=0;
        if (this.hasInstrument(instrument)){

            totalQtyOverLevel=orderBookMap.get(instrument).getTotalQtyOverLevel(   side,  level);
        }

        return totalQtyOverLevel ;
    }



    /**
     *
     * @param instrument
     * @param side
     * @param level
     * @return
     */
    @Override
    public Map<BigDecimal,List<Double>> getVolumeWeightedPriceOverLevel(String instrument, Side side, int level) {

        Map<BigDecimal, List<Double>> volumeWeightedPrice = Collections.EMPTY_MAP;

        if (this.hasInstrument(instrument)){

            volumeWeightedPrice=orderBookMap.get(instrument).getVolumeWeightedPriceOverLevel(   side,  level);
        }

        return volumeWeightedPrice ;
    }







    /**
     * Get all orders for the instrument on given side with given price
     *
     * <p>Result should contain orders in the same order as they arrive
     *
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   16/01/2022
     *
     * @param instrument identifier of an instrument
     * @param side       either buy or sell
     * @param price      requested price level
     * @return all orders, or empty list if there are no orders for the instrument on this side with
     * this price
     */
    @Override
    public List<Order> getOrdersAtLevel(String instrument, Side side, BigDecimal price) {

        List<Order> orderList=new ArrayList<>();

        if (this.hasInstrument(instrument)){
            orderList=orderBookMap.get(instrument).getOrdersAtLevel(instrument,side,price);

            if(orderList==null) orderList= Collections.emptyList(); // for extra security
        }

        return orderList;
    }





    @Override
    public String toString() {
        return "OrderBookList{" +
                "orderBookMap=" + orderBookMap +
                '}';
    }



}
