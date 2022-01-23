package com.diy.orderbookmanager;

import com.diy.Side.Side;
import com.diy.domain.Order;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;


@Slf4j
public class OrderBook {


    private Map<BigDecimal, Set<Order>> orderbookBid;
    private Map<BigDecimal,Set<Order>> orderbookAsk;
    private final String Instrument;



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

        orderbookBid = new ConcurrentSkipListMap<>(ascendingPrice);  //O(log n)
        orderbookAsk = new ConcurrentSkipListMap<>(descendingPrice);  // O(log n)
        this.Instrument = product;

        log.info("new OrderBook created for "+product+" : "+this+" !");
    }





    public boolean isEmpty(){
        if (!orderbookBid.isEmpty() || !orderbookAsk.isEmpty())
            return false;

        return true;
    }





    /****
     *
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   16/01/2022
     *
     * @param order
     *
     ******/
    public boolean addOrder(Order order)
    {
        boolean success;

        BigDecimal price = order.getPrice();
        Side side = order.getSide();


        Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);

        if (orderBook.containsKey(price)) {
            success=orderBook.get(price).add(order);
            //log.debug("OrderBook ADD with existing price="+price+" => order "+order);
        } else {
            orderBook.put(price, Collections.newSetFromMap(new ConcurrentHashMap<>())  ); // O(log(n)) - the same thread-safe and performance guarantees as the map passed as argument
            success =orderBook.get(price).add(order);                                      //You typically use this method to create a concurrent set from a concurrent map, because there is no ConcurrentHashSet in the API.
            //log.debug("OrderBook ADD with new price="+price+" => order "+order);
        }

        return success;
    }







    /**
     *
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   16/01/2022
     *
     *
     * @param order
     */
    public boolean deleteOrder(Order order) {

        BigDecimal price = order.getPrice();
        Side side = order.getSide();
        boolean success = false;


        Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);

        if (orderBook.containsKey(price)) {

                success=orderBook.get(price).remove(order);

                if(orderBook.get(price).isEmpty()){ // if there is no Order left
                    if(orderBook.remove(price)==null){ // then we delete the key (price)
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
     * @return
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
     * @return
     */
    public Double getAveragePriceOverLevel( Side side, int level) {

        double averagePrice=0;
        double numerator=0;
        double denominator=0;

        Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);
        Iterator<Map.Entry<BigDecimal, Set<Order>>> iterator = orderBook.entrySet().iterator();

        for( int cmpt=1;iterator.hasNext();cmpt++){

            Map.Entry<BigDecimal, Set<Order>> entry = iterator.next();

            double sumQty =  entry.getValue().stream().mapToDouble(x ->   x.getQuantity()).sum();
            double sumPrQty =  sumQty * entry.getKey().doubleValue();
            numerator =numerator+sumPrQty;
            denominator=denominator+sumQty;


            if(cmpt==level) {
                averagePrice=numerator/denominator;
                break;
            }
        }

        return averagePrice ;
    }


    /**
     *
     * @param side
     * @param level
     * @return
     */
    public Double getTotalQtyOverLevel( Side side, int level) {

        double totalQtyOverLevel=0;

        Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);
        Iterator<Map.Entry<BigDecimal, Set<Order>>> iterator = orderBook.entrySet().iterator();

        for( int cmpt=1;iterator.hasNext();cmpt++){

            Map.Entry<BigDecimal, Set<Order>> entry = iterator.next();
            double sumQty =  entry.getValue().stream().mapToDouble(x ->   x.getQuantity()).sum();
            totalQtyOverLevel =totalQtyOverLevel+sumQty;

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
     * @return
     */
    public Map<BigDecimal, List<Double> > getVolumeWeightedPriceOverLevel( Side side, int level) {



        Map<BigDecimal, List<Double>> volumeWeightedPrice= new LinkedHashMap<>();

        Map<BigDecimal, Set<Order>> orderBook = getOrderBookBySide(side);
        Iterator<Map.Entry<BigDecimal, Set<Order>>> iterator = orderBook.entrySet().iterator();

        for( int cmpt=1;iterator.hasNext();cmpt++){

            Map.Entry<BigDecimal, Set<Order>> entry = iterator.next();
            double sum =  entry.getValue().stream().mapToDouble(x ->   x.getQuantity()).sum();
            double count =entry.getValue().size();
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
     * @since   16/01/2022
     *
     *
     * @param side
     * @return
     */
    private Map<BigDecimal,Set<Order> > getOrderBookBySide(Side side) {

        switch(side) {
            case BUY : return orderbookBid;
            case SELL: return orderbookAsk;
            default: throw new IllegalArgumentException();
        }
    }






    /**
     *
     *
     * @author  Gothard GOTENI
     * @version 1.0
     * @since   16/01/2022
     *
     * @param instrument
     * @param side
     * @return
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
     * @since   16/01/2022
     *
     *
     * @param instrument
     * @param side
     * @param price
     * @return
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
                "orderbookBid=" + orderbookBid +
                ", orderbookAsk=" + orderbookAsk +
                ", Instrument='" + Instrument + '\'' +
                '}';
    }




}
