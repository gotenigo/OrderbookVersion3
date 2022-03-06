package com.diy.orderbookmanager;

import com.diy.Side.Side;
import com.diy.domain.Order;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;


@Slf4j
public final class OrderBook {

    private final Map<BigDecimal, Set<Order>> orderBookBid;  // BigDecimal hit the performance a bit, but it's very accurate in calculation.
    private final Map<BigDecimal,Set<Order>> orderBookAsk;   // BTC being very expensive, then accuracy matters
    private final String Instrument;                   // in Crypto price, we have often compounding decimal point operations.



    public OrderBook(String product) {


        Comparator<BigDecimal> ascendingPrice = (p1, p2) -> {
            if (p1.compareTo(p2)==0) return 0;
            if (p1.compareTo(p2)<0) return 1;
            return -1;
        };

        Comparator<BigDecimal> descendingPrice = (p1,p2) -> {
            if (p1.compareTo(p2)==0) return 0;
            if (p1.compareTo(p2)>0) return 1;
            return -1;
        };

        orderBookBid = new ConcurrentSkipListMap<>(ascendingPrice);  //O(log n)  // Treemap concurrent variant
        orderBookAsk = new ConcurrentSkipListMap<>(descendingPrice);  // O(log n) // Treemap concurrent variant
        this.Instrument = product;

        log.info("new OrderBook created for "+product+" : "+this+" !");
    }



    /**
     *    Test if empty
     *
     * @return
     */
    public boolean isEmpty(){
        if (!orderBookBid.isEmpty() || !orderBookAsk.isEmpty()) {
            return false;
        }

        return true;
    }





    /****
     *
     *    => Add new Order
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   16/01/2022
     *
     * @param order
     *
     * @return boolean Successful or Not
     *
     ******/
    public boolean addOrder(Order order){ // this Method uses Atomic processes
        boolean success=false;

        BigDecimal price = order.getPrice();
        Side side = order.getSide();

        Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);


        Set<Order> OrdersPresent = orderBook.computeIfPresent(price , (k,v) ->{   //Atomic Add to be Thread safe

            //log.debug("computeIfPresent => OrderBook ADD with existing price="+price+" => order "+order);
            Set<Order> orderSet = orderBook.get(price);
            orderSet.add(order);
            return orderSet;
        }) ;


        Set<Order> OrdersAbsent = orderBook.computeIfAbsent(price , k ->{   //Atomic Add to be Thread safe

            //log.debug("computeIfAbsent => OrderBook ADD with new price="+price+" => order "+order);
            Set<Order> orderSet = Collections.newSetFromMap(new ConcurrentHashMap<>()); //  (Equivalent to HashSet) O(log(n)) - the same thread-safe and performance guarantees as the map passed as argument
            orderSet.add(order);                                            //You typically use this method to create a concurrent set from a concurrent map, because there is no ConcurrentHashSet in the API.
            return orderSet;
        }) ;


        if (OrdersAbsent!=null || OrdersPresent!=null ){
                success=true;
        }

        return success;
    }



    /**
     *
     *      => Delete Order
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   16/01/2022
     *
     * @param order
     * @return boolean Successful or Not
     */
    public boolean deleteOrder(Order order) {

        BigDecimal price = order.getPrice();
        Side side = order.getSide();
        boolean success = false;

        Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);

        if (orderBook.containsKey(price)) {

            success=orderBook.get(price).remove(order);

            if(orderBook.get(price).isEmpty()){ // if there is no Order left
                if(orderBook.remove(price)==null){ // then we delete the key (price) to free memory
                    log.error(" - orderBook -Internal Error : key  "+price+" could not be deleted successfully !");
                    success=false;
                };
            }

        } else {
            log.info("this record order "+order+" does not exist !");
        }

        return success;
    }






    /**
     *
     * @param instrument
     * @param side
     * @param level
     * @return Map<BigDecimal, Set<Order>> or empty Map
     */
    public Map<BigDecimal, Set<Order>> getOrdersUpToLevel(String instrument, Side side, int level) {

        Map<BigDecimal, Set<Order>> ordersUpToLeve = new HashMap<>(); //simple hashMap is good enough and it boosts perf
        Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);
        Iterator<Map.Entry<BigDecimal, Set<Order>>> iterator = orderBook.entrySet().iterator();

        for( int cmpt=1;iterator.hasNext();cmpt++){

            Map.Entry<BigDecimal, Set<Order>> entry = iterator.next();
            ordersUpToLeve.put(entry.getKey(),entry.getValue());

            if(cmpt==level)
                break;
        }

        return ordersUpToLeve ;
    }


    /**
     *
     * @param side
     * @param level
     * @return Double
     */
    public BigDecimal getAveragePriceOverLevel( Side side, int level) {

        BigDecimal averagePrice=BigDecimal.ZERO;
        BigDecimal numerator=BigDecimal.ZERO;
        BigDecimal denominator=BigDecimal.ZERO;

        Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);
        Iterator<Map.Entry<BigDecimal, Set<Order>>> iterator = orderBook.entrySet().iterator();

        for( int cmpt=1;iterator.hasNext();cmpt++){

            Map.Entry<BigDecimal, Set<Order>> entry = iterator.next();

            BigDecimal sumQty =  entry.getValue().stream().map(x ->   x.getQuantity()).reduce((e1,e2)->e1.add(e2)).get();
            BigDecimal sumPrQty =  sumQty.multiply(entry.getKey()) ;
            numerator =numerator.add(sumPrQty);
            denominator=denominator.add(sumQty);

            if(cmpt==level || cmpt==orderBook.size()) {
                return numerator.divide(denominator, RoundingMode.DOWN);
            }
        }

        return averagePrice ;
    }


    /**
     *
     * @param side
     * @param level
     * @return Double
     */
    public BigDecimal getTotalQtyOverLevel( Side side, int level) {

        BigDecimal totalQtyOverLevel = BigDecimal.ZERO;

        Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);
        Iterator<Map.Entry<BigDecimal, Set<Order>>> iterator = orderBook.entrySet().iterator();

        for( int cmpt=1;iterator.hasNext();cmpt++){

            Map.Entry<BigDecimal, Set<Order>> entry = iterator.next();
            BigDecimal sumQty =  entry.getValue().stream().map(x ->   x.getQuantity()).reduce((e1,e2)->e1.add(e2)).get();
            totalQtyOverLevel =totalQtyOverLevel.add(sumQty);

            if(cmpt==level) {
                break;
            }
        }

        return totalQtyOverLevel ;
    }






    /**
     *
     * @param side
     * @param level
     * @return Map<BigDecimal, List<Double>>
     */
    public Map<BigDecimal, List<Number>> getVolumeWeightedPriceOverLevel( Side side, int level) {

        Map<BigDecimal, List<Number>> volumeWeightedPrice= new LinkedHashMap<>();
        Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);
        Iterator<Map.Entry<BigDecimal, Set<Order>>> iterator = orderBook.entrySet().iterator();

        for( int cmpt=1;iterator.hasNext();cmpt++){

            Map.Entry<BigDecimal, Set<Order>> entry = iterator.next();
            Optional<BigDecimal> sumOpt =  entry.getValue().stream().map(x ->   x.getQuantity()).reduce((e1,e2)->e1.add(e2));

            BigDecimal sum = sumOpt.orElse(BigDecimal.ZERO);

            int count =entry.getValue().size();
            volumeWeightedPrice.put(entry.getKey(), Arrays.asList(count,sum));

            if(cmpt==level) {
                break;
            }
        }
        return volumeWeightedPrice ;
    }





    /**
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   23/01/2022
     *
     *
     * @param side
     * @return Map<BigDecimal,Set<Order> > : orderbookBid or orderbookAsk
     */
    private Map<BigDecimal,Set<Order> > getOrderBookBySide(Side side) {

        switch(side) {
            case BUY : return orderBookBid;
            case SELL: return orderBookAsk;
            default: throw new IllegalArgumentException();
        }
    }






    /**
     *
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   23/01/2022
     *
     * @param instrument
     * @param side
     * @return Optional<BigDecimal>
     **/
    Optional<BigDecimal> getBestPrice(String instrument, Side side){

        Optional<BigDecimal> bestPrice = Optional.empty();

        if(instrument.equals(this.Instrument)) {

            Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);
            Optional<BigDecimal> firstKey =orderBook.keySet().stream().findFirst();

            if(firstKey .isPresent()){
                bestPrice=Optional.of(firstKey.get());
            }

        }else{
            log.info("order for product "+instrument+" ignored ! This OrderBook is set for "+this.Instrument);
        }
        return bestPrice;
    }




    /**
     *
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   23/01/2022
     *
     *
     * @param instrument
     * @param side
     * @param price
     * @return Collections.emptyList() or List<Order>
     */
    List<Order> getOrdersAtLevel(String instrument, Side side, BigDecimal price) {

        List<Order> orderList=Collections.emptyList();
        if(instrument.equals(this.Instrument)) {

            orderList = new ArrayList<>();
            Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);

            if (orderBook.containsKey(price)) {
                orderList = orderBook.get(price).stream().collect(Collectors.toList());
            }else{
                log.info("No order at Price level "+price+" under Side="+side+" and Instrument="+Instrument);
            }
        }else{
            log.info("order for product "+instrument+" ignored ! This OrderBook is set for "+this.Instrument);
        }

        return orderList;
    }





    @Override
    public String toString() {
        return "OrderBook{" +
                "orderBookBid=" + orderBookBid +
                ", orderBookAsk=" + orderBookAsk +
                ", Instrument='" + Instrument + '\'' +
                '}';
    }




}
