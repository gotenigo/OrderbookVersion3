package com.diy.orderbookmanager;

import com.diy.Side.Side;
import com.diy.domain.Order;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static com.google.common.collect.MoreCollectors.onlyElement;

@Slf4j
public final class OrderBookList implements OrderBookManager  {


    private final Map<String, OrderBook> orderBookMap;



    public OrderBookList() {
        this.orderBookMap = new ConcurrentHashMap<>();   //O(log n) pretty fast, your collection size has to be
                                                         // very extreme to notice a performance impact
    }


    /**
     * Add new order  in Atomic way
     *
     * <p>Orders for the same instrument, on the same side, with the same price should be kept in the
     * order as they arrive
     *
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   23/01/2022
     *
     * @param order new order to add <br>
     * @see Order
     */
     private boolean addOrder(Order order) { // this Method uses Atomic processes

        //log.debug("=>addOrder order called for ="+order);
        boolean success=false;

        String instrument = order.getInstrument();

         OrderBook orderBookPresent = orderBookMap.computeIfPresent(instrument, (k,v)->{  // do we have already an Orderbook for that Instrument ?

             OrderBook orderBook = orderBookMap.get(instrument);
             orderBook.addOrder(order);
             return orderBook;

         });


         OrderBook orderBookAbsent = orderBookMap.computeIfAbsent(instrument , k->{  // if no, then we create an OrderBook for that specific Instrument

             OrderBook orderBook = new OrderBook(instrument);
             orderBook.addOrder(order);
             return orderBook;
         });


         if (orderBookPresent!=null || orderBookAbsent!=null ){
             success=true;
         }

         //log.debug("orderBookMap =" + orderBookMap);
        //log.debug("Add completed : successful =" + success);

        return success;
    }








    /**
     *
     *  Delete an existing order  in Atomic way
     *
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   23/01/2022
     *
     * Delete an existing order . Returns false if no such order exists
     *
     * @param order unique identifier of existing order
     * @return boolean True if the order was successfully deleted, false otherwise
     */
    private boolean deleteOrder(Order order) {

        log.debug("=>Delete order called for ="+order);
        boolean success = false;


        String instrument = order.getInstrument();


        OrderBook orderBookPresent = orderBookMap.computeIfPresent(instrument, (k,v)->{  // do we have already an Orderbook for that Instrument ?

            OrderBook orderBook = orderBookMap.get(instrument);
            orderBook.deleteOrder(order);
            log.debug(" ! State of the orderBook before finalize the Delete ="+orderBook);
            return orderBook;

        });


        if (orderBookPresent.isEmpty()) { // if there is no OrderBook left


            if (orderBookMap.remove(instrument) == null) { // then we delete the key (price)
                log.error(" - orderBookList -Internal Error : key  " + instrument + " could not be deleted successfully !");
                success = false;
            }
            success = true;
            log.info("Delete Order + key removal completed : success =" + success);

        }else if (orderBookPresent == null){
            success = false;
            log.info("I cant find any orderBook with key="+instrument+" for order ="+order);
        }else{
            success = true;
            log.info("Delete Order completed : success =" + success);
        }

        return success;
    }







    /**
     *
     *   Update the order theOrderBook each time the exchange a new OrderBook
     *
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   23/01/2022
     *
     *
     * @param order
     * @return boolean Successful or not
     */
    @Override
    public boolean updateOrder(Order order) {

        String instrument=order.getInstrument();
        Side side = order.getSide();
        BigDecimal price = order.getPrice();

        List<Order> orderList=getOrdersAtLevel(instrument, side, price);

        try {
                                                // we pick up the order we are looking for
            Order vOrder = orderList.stream()  // return 1 Element  ( Expected behaviour )  // return 2 Element => IllegalArgumentException // return 0 Element => NoSuchElementException
                    .filter(x -> x.equals(order))
                    .collect(onlyElement());

            if (vOrder.getQuantity().compareTo(order.getQuantity())==0) {
                log.info("Transaction Ignored, nothing has changed on that order!"); // save time and improve perf
                return false;
            }


            if (order.getQuantity().signum() > 0) { //•	If the quantity is not zero it means order book level at specified price needs to be updated (or inserted if it was not there)
                if (!deleteOrder(order)) return false;
                if (!addOrder(order)) return false;
            } else if (order.getQuantity().compareTo(BigDecimal.ZERO) == 0) { //•	If the quantity is zero it means the order book level at specified price must be removed from the book
                if (!deleteOrder(order)) return false;
            }

        } catch (IllegalArgumentException  e) { //two or more elements
            log.error(" ! Internal Error, you cant have duplicate 'unique' Element in the orderBookMap :"+ orderBookMap);
            throw e;
        } catch (NoSuchElementException  e) {  //stream is empty

            if(order.getQuantity().signum() >0) {
                if (!addOrder(order)) return false; //  We add order if it does not exist
            }else{
                log.info("ignored. You cant add an order with Qty<=0 (you sent '"+order.getQuantity()+"')");
                return false;
            }

        }

        return true;
    }








    /**
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   23/01/2022
     *
     *
     * @param instrument
     * @return boolean
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
     * @since   23/01/2022
     *
     *
     * @param instrument identifier of an instrument
     * @param side       either buy or sell
     * @return the best price Optional<BigDecimal>, or Optional.empty() if there're no orders for the instrument on this
     * side
     */
    @Override
    public Optional<BigDecimal> getBestPrice(String instrument, Side side) {

        Optional<BigDecimal> bestPrice = Optional.empty();

        if (this.hasInstrument(instrument)){
            bestPrice=orderBookMap.get(instrument).getBestPrice(instrument,side);
        }
        log.debug("BestPrice="+bestPrice);
        return bestPrice;
    }










    /**
     *
     * @param instrument
     * @param side
     * @param level
     * @return Map<BigDecimal, Set<Order>> or Collections.EMPTY_MAP
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
     * @return Double
     */
    @Override
    public BigDecimal getAveragePriceOverLevel(String instrument, Side side, int level) {

        BigDecimal averagePrice=BigDecimal.ZERO;
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
     * @return Double
     */
    @Override
    public BigDecimal getTotalQtyOverLevel(String instrument, Side side, int level) {

        BigDecimal totalQtyOverLevel=BigDecimal.ZERO;
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
     * @return Map<BigDecimal, List<Double>> or Collections.EMPTY_MAP
     */
    @Override
    public Map<BigDecimal,List<Number>> getVolumeWeightedPriceOverLevel(String instrument, Side side, int level) {

        Map<BigDecimal, List<Number>> volumeWeightedPrice = Collections.EMPTY_MAP;

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
     * @since   23/01/2022
     *
     * @param instrument identifier of an instrument
     * @param side       either buy or sell
     * @param price      requested price level
     * @return List<Order>, or empty list if there are no orders for the instrument on this side with
     * this price
     */
    @Override
    public List<Order> getOrdersAtLevel(String instrument, Side side, BigDecimal price) {

        List<Order> orderList=new ArrayList<>();

        if (this.hasInstrument(instrument)){
            orderList=orderBookMap.get(instrument).getOrdersAtLevel(instrument,side,price);
        }

        return orderList;
    }

    /********************
     *
     * getFullOrderBook
     *
     * @return Map<BigDecimal, Set < Order>> []
     */
    @Override
    public Map< String , List<Map <BigDecimal,Set<Order>>> >  getFullOrderBook() {

        Map< String , List<Map <BigDecimal,Set<Order>>> >  orderBookList = new HashMap<>();

        orderBookMap.forEach((k,v)->{

            Map<BigDecimal, Set<Order>> orderBookBid =v.getOrderBookBySide(Side.BUY);
            Map<BigDecimal, Set<Order>> orderBookAsk=v.getOrderBookBySide(Side.SELL);

            orderBookList.put(k,Arrays.asList(orderBookBid,orderBookAsk) );

        });

        log.debug("FullOrderBook="+orderBookList);

        return orderBookList;

    }


    @Override
    public String toString() {
        return "OrderBookList{" +
                "orderBookMap=" + orderBookMap +
                '}';
    }



}
