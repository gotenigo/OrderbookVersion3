import com.diy.Side.Side;
import com.diy.domain.Order;
import com.diy.orderbookmanager.OrderBook;
import com.diy.orderbookmanager.OrderBookList;
import com.diy.orderbookmanager.OrderBookManager;
import org.junit.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


import static com.diy.Utils.Utils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class OrderBookServiceAppTest {


    /*******************************
     *
     *      Object Creation
     *
     ************************************/


    @Test
    public void TestOrderBookIsCreatedOK() {

        System.out.println("________________ TestOrderBookIsCreatedOK");

        OrderBook orderBook = new OrderBook("BTCUSD"); // OrderBook is created

        assertTrue(orderBook instanceof OrderBook);
        assertTrue(orderBook.toString() instanceof String);
        assertTrue(orderBook.isEmpty());
        assertNotNull(orderBook);

    }





    @Test
    public void TestOrderBookListIsCreatedOK() {

        System.out.println("________________ TestOrderBookListIsCreatedOK");

        OrderBookList orderBookList = new OrderBookList();

        assertTrue(orderBookList instanceof OrderBookList);
        assertTrue(orderBookList.toString() instanceof String);
        assertNotNull(orderBookList);

    }


    @Test
    public void TestEqualityAndHashcode(){

        System.out.println("________________ TestEqualityAndHashcode");

        //1. Test Price and Qty scale do not affect equality
        Order order1 = toOrder("t=1638848595|i=BTCUSD|p=32.9|q=100.10|s=s");
        Order order2 = toOrder("t=1638848595|i=BTCUSD|p=32.90|q=100.1|s=s");

        assertEquals(order1.hashCode(),order2.hashCode());
        assertTrue(order1.equals(order2));

        //2. Test TimeStamp affects equality
        order1 = toOrder("t=1638848595|i=BTCUSD|p=32.9|q=100.10|s=s");
        order2 = toOrder("t=1638848596|i=BTCUSD|p=32.90|q=100.1|s=s");

        assertNotEquals(order1.hashCode(),order2.hashCode());
        assertTrue(!order1.equals(order2));

        //3. Test Qty does not affect equality
        order1 = toOrder("t=1638848595|i=BTCUSD|p=32.9|q=100.10|s=s");
        order2 = toOrder("t=1638848595|i=BTCUSD|p=32.90|q=55|s=s");

        assertEquals(order1.hashCode(),order2.hashCode());
        assertTrue(order1.equals(order2));

        //4. Test Price affect equality
        order1 = toOrder("t=1638848595|i=BTCUSD|p=55.9|q=100.10|s=s");
        order2 = toOrder("t=1638848595|i=BTCUSD|p=32.90|q=55|s=s");

        assertNotEquals(order1.hashCode(),order2.hashCode());
        assertTrue(!order1.equals(order2));

    }



    /*******************************
     *
     *      Action : ADD, DELETE, Update
     *
     ************************************/

    @Test
    public void TestOrderBookManagerSimpleAdd() {

        System.out.println("________________ TestOrderBookManagerSimpleAdd");

        OrderBookManager orderBookManager = new OrderBookList();
        Order order = toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=b"); // create an Order
        boolean successState = orderBookManager.updateOrder(order);  // perform ADD

        assertTrue(successState);

    }


    @Test
    public void TestOrderBookManagerSimpleAddWithZeroQty() {

        System.out.println("________________ TestOrderBookManagerSimpleAdd");

        OrderBookManager orderBookManager = new OrderBookList();
        Order order = toOrder("t=1638848595|i=BTCUSD|p=32.99|q=0|s=b"); // create an Order
        boolean successState = orderBookManager.updateOrder(order);  // perform ADD

        System.out.println("orderBookManager.orderBookManager() ="+orderBookManager.getFullOrderBook());

        assertTrue(!successState);

    }



    @Test
    public void TestOrderBookManagerSimpleDelete() {

        System.out.println("________________ TestOrderBookManagerSimpleDelete");

        OrderBookManager orderBookManager = new OrderBookList();
        Order order = toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=b"); // create an Order
        boolean successState = orderBookManager.updateOrder(order);  // perform ADD

        assertTrue(successState);

        order = toOrder("t=1638848595|i=BTCUSD|p=32.99|q=0|s=b"); // create an Order
        successState = orderBookManager.updateOrder(order);  // perform Delete as Qty=0

        assertTrue(successState);

    }



    @Test
    public void TestOrderBookManagerSimpleUpdate() {

        System.out.println("________________ TestOrderBookManagerSimpleUpdate");

        OrderBookManager orderBookManager = new OrderBookList();
        Order order = toOrder("t=1638848595|i=BTCUSD|p=32.90|q=100|s=b"); // create an Order
        boolean successState = orderBookManager.updateOrder(order);  // perform ADD

        assertTrue(successState);

        order = toOrder("t=1638848595|i=BTCUSD|p=32.90|q=55.23|s=b"); // create an Order
        successState = orderBookManager.updateOrder(order);  // perform Update as Qty has changed

        assertTrue(successState);

    }




    //Mock Test

   /* @Test
    public void TestMockOrderBookListUpdateOrder() {

        System.out.println("________________ TestMockOrderBookListAddOrderExecuted");

        OrderBookList mockOrderBookList = mock(OrderBookList.class);
        doReturn(true).when(mockOrderBookList).updateOrder(isA(Order.class));
        Order order = toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=b");
        boolean ret1 =mockOrderBookList.updateOrder(order);
        assertTrue(ret1);
        verify(mockOrderBookList, times(1)).updateOrder(order);


        doReturn(false).when(mockOrderBookList).updateOrder(isA(Order.class));
        boolean ret2 = mockOrderBookList.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=19.99|q=2|s=s"));
        assertFalse(ret2);
        verify( mockOrderBookList, times(1)).updateOrder(toOrder("t=1638848595|i=BTCUSD|p=19.99|q=2|s=s"));


    }*/






    /*******************************
     *
     *      Action : Test Functionality : Duplicate ,  getBestPrice , getOrdersUpToLevel,
     *                                       getAveragePriceOverLevel,  getTotalQtyOverLevel
     *                                            getVolumeWeightedPriceOverLevel, getOrdersAtLevel
     *
     ************************************/


    @Test
    public void TestOrderBookManagerSimpleDuplicate() {

        System.out.println("________________ TestOrderBookManagerSimpleDuplicate");

        OrderBookManager orderBookManager = new OrderBookList();
        Order order = toOrder("t=1638848595|i=BTCUSD|p=32.90|q=100.50|s=b"); // create an Order
        boolean successState = orderBookManager.updateOrder(order);  // perform ADD

        assertTrue(successState);

        order = toOrder("t=1638848595|i=BTCUSD|p=32.90|q=100.5|s=b"); // create duplicate Order as per the spec
        successState = orderBookManager.updateOrder(order);  // perform Update as Qty has changed

        assertTrue(!successState);

    }





    @Test
    public void TestOrderBookManagerBestPrice() {

        System.out.println("________________ TestOrderBookManagerBestPrice");

        OrderBookManager orderBookManager = new OrderBookList();

        Order order = toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=b");
        boolean successState = orderBookManager.updateOrder(order);

        assertTrue(successState);

        Optional<BigDecimal> bestPrice = orderBookManager.getBestPrice("BTCUSD", Side.BUY);

        assertTrue(bestPrice.isPresent());
        BigDecimal bestPriceVal = bestPrice.get();

        assertTrue(bestPrice.isPresent());
        assertEquals("32.99", bestPriceVal.toString());

    }


    @Test
    public void TestOrderBookManagerGetOrdersAtLevel() {

        System.out.println("________________ TestOrderBookManagerGetOrdersAtLevel");

        OrderBookManager orderBookManager = new OrderBookList();
        Order order = toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=s");
        assertTrue(orderBookManager.updateOrder(order));// Add order

        List<Order> orderList = orderBookManager.getOrdersAtLevel("BTCUSD", Side.BUY, StringToBigDecimal("32.99"));

        assertTrue(orderList instanceof List);
        assertEquals(orderList.size() , 0); // nothing under BUY

        order = toOrder("t=1638848596|i=BTCUSD|p=32.99|q=100|s=s");
        assertTrue(orderBookManager.updateOrder(order)); // Add order
        orderList = orderBookManager.getOrdersAtLevel("BTCUSD", Side.SELL, StringToBigDecimal("32.99"));

        assertEquals( 2, orderList.size());  // we expect data under SELL

        orderBookManager.getFullOrderBook();


    }


    @Test
    public void TestGetOrdersUpToLevel() {

        System.out.println("________________ TestGetOrdersUpToLevel");

        OrderBookManager orderBookManager = new OrderBookList();

        Order order = toOrder("t=1638848596|i=BTCUSD|p=32.99|q=100|s=s");
        assertTrue(orderBookManager.updateOrder(order));// Add order

        order = toOrder("t=1638848596|i=BTCUSD|p=50.19|q=100|s=s");
        assertTrue(orderBookManager.updateOrder(order));// Add order

        order = toOrder("t=1638848596|i=BTCUSD|p=11.99|q=100|s=b");
        assertTrue(orderBookManager.updateOrder(order));// Add order

        order = toOrder("t=1638848596|i=BTCUSD|p=19.73|q=100|s=b");
        assertTrue(orderBookManager.updateOrder(order));// Add order

        Map<BigDecimal, Set<Order>> getOrdersUpToLevelSell=orderBookManager.getOrdersUpToLevel("BTCUSD",Side.BUY,10);
        Map<BigDecimal, Set<Order>> getOrdersUpToLevelBuy=orderBookManager.getOrdersUpToLevel("BTCUSD",Side.SELL,1);

        assertEquals(2,getOrdersUpToLevelSell.size());
        assertEquals(1,getOrdersUpToLevelBuy.size());

    }



    @Test
    public void TestGetAveragePriceOverLevel() {

        System.out.println("________________ TestGetAveragePriceOverLevel ");

        int nbrLoop = 100_000; // number of Order to create

        Map<BigDecimal,BigDecimal> mapQtyPrice = new HashMap<>();
        BigDecimal numerator=BigDecimal.ZERO;
        BigDecimal denominator=BigDecimal.ZERO;

        //1. prepare the value : Qty & Price
        for(int i=0; i<nbrLoop; i++){

            BigDecimal price =randomPrice();
            BigDecimal qty =randomQty();
            mapQtyPrice.put(price,qty); // need because Random can generate a dupe on high iteration number
        }


        //2. Inject Order  + Calculate average Price
        OrderBookManager orderBookManager = new OrderBookList();

        Iterator<Map.Entry<BigDecimal,BigDecimal>> iterator = mapQtyPrice.entrySet().iterator();
        while(iterator.hasNext()){

            Map.Entry<BigDecimal,BigDecimal> e = iterator.next();
            BigDecimal price= e.getKey();
            BigDecimal qty=e.getValue();

            assertTrue( orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p="+price+"|q="+qty+"|s=s")));

            numerator= numerator .add (price.multiply(qty));
            denominator=denominator.add(qty);
        }

        BigDecimal averagePrice=numerator.divide(denominator, RoundingMode.DOWN);

        BigDecimal getAveragePriceOverLevel = orderBookManager.getAveragePriceOverLevel("BTCUSD",Side.SELL, nbrLoop);

        assertEquals(averagePrice,getAveragePriceOverLevel);

    }









    @Test
    public void TestGetTotalQtyOverLevel(){

        System.out.println("________________ TestGetTotalQtyOverLevel ");

        int nbrLoop = 100_000; // number of Order to create


        //1. prepare the value : Qty & Price
        Map<BigDecimal,BigDecimal> mapQtyPrice = new HashMap<>();
        BigDecimal totalQty=BigDecimal.ZERO;

        for(int i=0; i<nbrLoop; i++){

            BigDecimal price =randomPrice();
            BigDecimal qty =randomQty();
            mapQtyPrice.put(price,qty); // need because Random can generate a dupe on high iteration number
        }


        //2. Inject Order
        OrderBookManager orderBookManager = new OrderBookList();

        Iterator<Map.Entry<BigDecimal,BigDecimal>> iterator = mapQtyPrice.entrySet().iterator();
        while(iterator.hasNext()){

            Map.Entry<BigDecimal,BigDecimal> e = iterator.next();
            BigDecimal price= e.getKey();
            BigDecimal qty=e.getValue();

            assertTrue( orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p="+price+"|q="+qty+"|s=b")));

            totalQty=totalQty.add(qty);
        }

        BigDecimal getTotalQtyOverLevel = orderBookManager.getTotalQtyOverLevel("BTCUSD",Side.BUY, nbrLoop);

        assertEquals(totalQty,getTotalQtyOverLevel);

    }







    @Test
    public void TestGetVolumeWeightedPriceOverLevel(){

        System.out.println("________________ TestGetVolumeWeightedPriceOverLevel ");


        int nbrLoop = 10_000; // // number of Order to create
        int grpPerPrice=5; // number of Order under the same price

        // using native object for easier handling
        class Position{

            BigDecimal qty;
            BigDecimal price;

            Position(BigDecimal qty, BigDecimal price){
                this.qty=qty;
                this.price=price;
            }
            BigDecimal getQty(){return qty;}
            BigDecimal getPrice(){return price;}

            @Override
            public String toString() {
                return "Position{" +
                        "qty=" + qty +
                        ", price=" + price +
                        '}';
            }
        }

        List<Position> listPosition = new ArrayList<>();

        BigDecimal price=randomPrice();
        BigDecimal qty=randomQty();
        long timestamp = 1638848595;

        //1. prepare the value : Qty & Price
        for(int i=0; i<nbrLoop; i++){

            if (i%grpPerPrice==0) {
                price = randomPrice();
                qty = randomQty();
            }
            listPosition.add(new Position(qty,price));
        }


        //2. Inject Order
        OrderBookManager orderBookManager = new OrderBookList();

        int cmpt=0;
        for(Position e : listPosition){
            assertTrue( orderBookManager.updateOrder(toOrder("t="+timestamp+cmpt+"|i=BTCUSD|p="+e.getPrice()+"|q="+e.getQty()+"|s=b")));
            cmpt++;
        }


        // We need to Group by price as random can generate duplicate Price
        Map<BigDecimal, Long> listPositionGrpByPrice = listPosition.stream()
                .collect(Collectors.groupingByConcurrent(Position::getPrice, Collectors.counting()));


        Map<BigDecimal,List<Number>>  getVolumeWeightedPriceOverLevel = orderBookManager.getVolumeWeightedPriceOverLevel("BTCUSD",Side.BUY,nbrLoop);

        assertEquals(listPositionGrpByPrice.size(), getVolumeWeightedPriceOverLevel.size());


    }









    @Test
    public void TestConcurrentAddReadHeavyLoad() throws InterruptedException {

        System.out.println("________________ TestConcurrentAddReadHeavyLoad ");

        int numberOfJob = 10_000; // Number of Order to create => numberOfJob will be concurrent Read & Write
        int grpPerPrice=5; // number of Order under the same price

        // using native object for easier handling
        class Position{
            BigDecimal qty;
            BigDecimal price;

            Position(BigDecimal qty, BigDecimal price){
                this.qty=qty;
                this.price=price;
            }
            BigDecimal getQty(){return qty;}
            BigDecimal getPrice(){return price;}
        }

        List<Position> listPosition = new ArrayList<>();

        BigDecimal price=randomPrice();
        BigDecimal qty=randomQty();
        long timestamp = 1638848595;

        //1. prepare the value : Qty & Price
        for(int i=0; i<numberOfJob; i++){

            if (i%grpPerPrice==0) {
                price = randomPrice();
                qty = randomQty();
            }
            listPosition.add(new Position(qty,price));
        }

        OrderBookManager orderBookManager = new OrderBookList();

        ExecutorService service = Executors.newFixedThreadPool(10);
        //Define the Latch
        CountDownLatch latchAdd = new CountDownLatch(numberOfJob);
        CountDownLatch latchRead = new CountDownLatch(numberOfJob);


        //Heavy Add
        service.execute(() -> {

            int cmpt=0;
            for(Position e : listPosition){

                System.out.println(cmpt+" : Writing data.....");

                int finalCmpt = cmpt;
                orderBookManager.updateOrder(toOrder("t="+timestamp+ finalCmpt +"|i=BTCUSD|p="+e.getPrice()+"|q="+e.getQty()+"|s=b"));
                latchAdd.countDown();
                //System.out.println("latchAdd ="+latchAdd.getCount());

                /*try {
                    Thread.sleep(2);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }*/

                cmpt++;
            }
        });


        //Heavy Read
        service.execute(() -> {
            List<Map> vList= new ArrayList<>();
            int cmpt=0;
            for(Position e : listPosition){

               System.out.println(cmpt+" : Reading data.....");

                vList.add(orderBookManager.getVolumeWeightedPriceOverLevel("BTCUSD",Side.BUY,numberOfJob)); // read everything
                latchRead.countDown();
                //System.out.println("latchRead ="+latchRead.getCount());

                //we slow down the reading a bit as we have similar space
                /*try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }*/

                cmpt++;
            }
        });

        latchAdd.await();
        latchRead.await();

        // workout the Map pair  GroupByPrice Vs Count
        Map<BigDecimal, Long> listPositionGrpByPrice = listPosition.stream()
                .collect(Collectors.groupingByConcurrent(Position::getPrice, Collectors.counting()));
        //System.out.println("listPositionGrpByPrice="+listPositionGrpByPrice);

        //make sure the Data added matches the Data read
        for(Position e : listPosition){
            assertEquals( listPositionGrpByPrice.get(e.getPrice()).intValue() , orderBookManager.getOrdersAtLevel("BTCUSD", Side.BUY, e.getPrice()).size());
            //cmpt++;
        }

    }






    /*******************************
     *
     *      ERROR HANDLING
     *
     ************************************/

    @Test(expected = NullPointerException.class)
    public void TestOrderBookManagerNullArg()  {

        System.out.println("________________ TestOrderBookManagerNullArg ");
        OrderBookManager orderBookManager = new OrderBookList();
        orderBookManager.updateOrder(null);

    }


    @Test(expected = NullPointerException.class)
    public void TestOrderBookManagerGetBestPriceNullArg()  {

        System.out.println("________________ TestOrderBookManagerGetBestPriceNullArg ");
        OrderBookManager orderBookManager = new OrderBookList();
        orderBookManager.getBestPrice(null,null);

    }


    @Test(expected = NullPointerException.class)
    public void TestOrderBookManagerGetOrdersAtLevelNullArg()  {

        System.out.println("________________ TestOrderBookManagerGetOrdersAtLevelNullArg ");
        OrderBookManager orderBookManager = new OrderBookList();
        orderBookManager.getOrdersAtLevel(null,null, new BigDecimal("0"));

    }



    @Test(expected = NumberFormatException.class)
    public void TestOrderNullPointerException() {
        System.out.println("________________ TestOrderWrongArgument ");
        Order order = toOrder("t=|i=BTCUSD|p=32.88|q=123|s=s");
    }


    @Test(expected = IllegalArgumentException.class)
    public void TestIllegalArgumentException() {
        System.out.println("________________ TestIllegalArgumentException ");
        Order order = toOrder("t=1638848595|i=BTCUSD|p=32.99|q=-1|s=s");
    }









}
