import com.diy.Side.Side;
import com.diy.domain.Order;
import com.diy.orderbookmanager.OrderBook;
import com.diy.orderbookmanager.OrderBookList;
import com.diy.orderbookmanager.OrderBookManager;
import org.junit.Test;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
    @Test
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


    }






    /*******************************
     *
     *      Action : Test Functionality : Duplicate ,  getBestPrice ,
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

        assertEquals( 2, orderList.size());  //something under SELL

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

        int nbrLoop = 100;

        List<Double []> listQtyPrice = new ArrayList<>();
        double numerator=0;
        double denominator=0;

        //1. prepare the value : Qty & Price
        for(int i=0; i<nbrLoop; i++){

            double price =randomPrice().doubleValue();
            double qty =randomQty().doubleValue();

            numerator= numerator + (price * qty);
            denominator=denominator+qty;
            listQtyPrice.add(new Double[]{qty, price});

        }

        double averagePrice=numerator/denominator;
        System.out.println("=>averagePrice="+averagePrice);

        //2. Inject Order
        OrderBookManager orderBookManager = new OrderBookList();

        for(Double[] e : listQtyPrice){
           assertTrue( orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p="+e[1]+"|q="+e[0]+"|s=s")));
        }

        double getAveragePriceOverLevel = orderBookManager.getAveragePriceOverLevel("BTCUSD",Side.SELL, nbrLoop);

        System.out.println("=>getAveragePriceOverLevel="+getAveragePriceOverLevel);

        Map<BigDecimal, Set<Order>> getOrdersUpToLevelSell=orderBookManager.getOrdersUpToLevel("BTCUSD",Side.SELL,nbrLoop);

        System.out.println("getOrdersUpToLevelSell="+getOrdersUpToLevelSell);

        assertEquals(averagePrice,getAveragePriceOverLevel,0.001);

    }









    @Test
    public void TestGetTotalQtyOverLevel(){

        System.out.println("________________ TestGetTotalQtyOverLevel ");

        int nbrLoop = 50;

        List<Double []> listQtyPrice = new ArrayList<>();
        double totalQty=0;

        //1. prepare the value : Qty & Price
        for(int i=0; i<nbrLoop; i++){

            double price =randomPrice().doubleValue();
            double qty =randomQty().doubleValue();

            totalQty=totalQty+qty;
            listQtyPrice.add(new Double[]{qty, price});

        }

        System.out.println("=>totalQty="+totalQty);

        //2. Inject Order
        OrderBookManager orderBookManager = new OrderBookList();

        for(Double[] e : listQtyPrice){
            assertTrue( orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p="+e[1]+"|q="+e[0]+"|s=b")));
        }

        double getTotalQtyOverLevel = orderBookManager.getTotalQtyOverLevel("BTCUSD",Side.BUY, nbrLoop);

        System.out.println("=>getTotalQtyOverLevelSell="+getTotalQtyOverLevel);

        Map<BigDecimal, Set<Order>> getOrdersUpToLevelSell=orderBookManager.getOrdersUpToLevel("BTCUSD",Side.BUY,nbrLoop);

        System.out.println("getTotalQtyOverLevelSell="+getOrdersUpToLevelSell);

        assertEquals(totalQty,getTotalQtyOverLevel,0.001);

    }







    @Test
    public void TestGetVolumeWeightedPriceOverLevel(){

        System.out.println("________________ TestGetVolumeWeightedPriceOverLevel ");


        int nbrLoop = 100; // Number of Order created
        int grpPerPrice=5; // number of Order under the same price

        List<Double []> listQtyPrice = new ArrayList<>();

        double price=randomPrice().doubleValue();
        double qty=randomQty().doubleValue();
        long timestamp = 1638848595;

        //1. prepare the value : Qty & Price
        for(int i=0; i<nbrLoop; i++){

            if (i%grpPerPrice==0) {
                price = randomPrice().doubleValue();
                qty = randomQty().doubleValue();
            }

            listQtyPrice.add(new Double[]{qty, price, });

        }


        //2. Inject Order
        OrderBookManager orderBookManager = new OrderBookList();

        int cmpt=0;
        for(Double[] e : listQtyPrice){
            assertTrue( orderBookManager.updateOrder(toOrder("t="+timestamp+cmpt+"|i=BTCUSD|p="+e[1]+"|q="+e[0]+"|s=b")));
            cmpt++;
        }

        Map<BigDecimal, Set<Order>> getOrdersUpToLevelSell=orderBookManager.getOrdersUpToLevel("BTCUSD",Side.BUY,nbrLoop);
        System.out.println("getTotalQtyOverLevelSell="+getOrdersUpToLevelSell);


        Map<BigDecimal,List<Double>>  getVolumeWeightedPriceOverLevel = orderBookManager.getVolumeWeightedPriceOverLevel("BTCUSD",Side.BUY,nbrLoop);
        System.out.println("getVolumeWeightedPriceOverLevel="+getVolumeWeightedPriceOverLevel);


        assertEquals((nbrLoop/grpPerPrice), getVolumeWeightedPriceOverLevel.size());

        for(Double[] e : listQtyPrice){
            assertEquals( grpPerPrice , orderBookManager.getOrdersAtLevel("BTCUSD", Side.BUY, StringToBigDecimal(e[1]+"")).size());
            cmpt++;
        }

    }













    @Test
    public void TestConcurrentAddDeleteHeavyLoad() throws InterruptedException {

        System.out.println("________________ TestConcurrentAddDeleteHeavyLoad ");

        int numberOfJob = 100_000; // Number of Order created
        int grpPerPrice=5; // number of Order under the same price

        List<Double []> listQtyPrice = new ArrayList<>();


        double price=randomPrice().doubleValue();
        double qty=randomQty().doubleValue();
        long timestamp = 1638848595;


        //1. prepare the value : Qty & Price
        for(int i=0; i<numberOfJob; i++){

            if (i%grpPerPrice==0) {
                price = randomPrice().doubleValue();
                qty = randomQty().doubleValue();
            }
            listQtyPrice.add(new Double[]{qty, price, });
        }

        OrderBookManager orderBookManager = new OrderBookList();


        ExecutorService service = Executors.newFixedThreadPool(10);
        //Define the Latch
        CountDownLatch latchAdd = new CountDownLatch(numberOfJob);
        CountDownLatch latchDelete = new CountDownLatch(numberOfJob);


        //Heavy Add
        int cmpt=0;
        for(Double[] e : listQtyPrice){

            int finalCmpt = cmpt;

            service.execute(() -> {
                orderBookManager.updateOrder(toOrder("t="+timestamp+ finalCmpt +"|i=BTCUSD|p="+e[1]+"|q="+e[0]+"|s=b"));
                latchAdd.countDown();
            });

            cmpt++;
        }
        System.out.println("cmpt="+cmpt);

        cmpt=0;
        for(Double[] e : listQtyPrice){

            int finalCmpt = cmpt;

            service.execute(() -> {
                orderBookManager.updateOrder(toOrder("t="+timestamp+ finalCmpt +"|i=BTCUSD|p="+e[1]+"|q=0|s=b"));
                latchDelete.countDown();
            });

            cmpt++;
        }


        latchAdd.await();
        latchDelete.await();


        for(Double[] e : listQtyPrice){
            assertEquals( Collections.emptyList() , orderBookManager.getOrdersAtLevel("BTCUSD", Side.BUY, StringToBigDecimal(e[1]+"")));
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


    @Test
    public void TestDuplicatedNotAllowed() {
        System.out.println("________________ TestDuplicatedNotAllowed ");
        Order order = toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=b");
        OrderBookList orderBookList = new OrderBookList();
        orderBookList.updateOrder(new Order(order));
        order = toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=b");
        orderBookList.updateOrder(new Order(order));
        List<Order> orderList = orderBookList.getOrdersAtLevel("BTCUSD", Side.BUY, new BigDecimal("32.99"));

        assertTrue(orderList.size() == 1);

    }









}
