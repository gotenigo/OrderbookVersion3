import com.diy.Side.Side;
import com.diy.Utils.Utils;
import com.diy.domain.Order;
import com.diy.domain.orderbookmanager.OrderBook;
import com.diy.domain.orderbookmanager.OrderBookList;
import com.diy.domain.orderbookmanager.OrderBookManager;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static com.diy.Utils.Utils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class OrderbookServiceAppTest {


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

        /*
            BigDecimal pricecompare = new BigDecimal(32.9).setScale(2, RoundingMode.DOWN);
            System.out.print("pricecompare="+pricecompare);

            assertTrue(orderBook.getVolumeWeightedPriceOverLevel("BTCUSD",Side.BUY,5).containsKey( pricecompare  ));
            assertEquals("BTCUSD", orderBook.getInstrument());
        */




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
    public void TestGetAveragePriceOverLevel(){

        System.out.println("________________ TestGetAveragePriceOverLevel ");

        double start = 0;
        double end = 999;
        Double random = new Random().nextDouble();
        double result = start + (random * (end - start));

        Double truncatedDouble = BigDecimal.valueOf(result)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();


        System.out.println("random="+truncatedDouble);


        OrderBookManager orderBookManager = new OrderBookList();

        List<Integer[]> qtyPrice = new ArrayList<>();


        orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p=19.99|q=2|s=s"));
        orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p=19.99|q=4|s=s"));
        orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p=21.25|q=16|s=s"));
        orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p=21.25|q=1|s=s"));
        orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p=22.25|q=7|s=s"));
        orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p=15.33|q=10|s=b"));
        orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p=15.33|q=20|s=b"));
        orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p=10.11|q=13|s=b"));
        orderBookManager.updateOrder(toOrder("t=1642817574433|i=BTCUSD|p=10.11|q=13|s=b"));

        Double ret = orderBookManager.getAveragePriceOverLevel("BTCUSD",Side.SELL, 3);

        System.out.println("ret="+ret);

    }




    @Test
    public void TestGetTotalQtyOverLevel(){

        System.out.println("________________ TestGetTotalQtyOverLevel ");


        OrderBookManager orderBookManager = new OrderBookList();
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=19.99|q=2|s=s"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=19.99|q=4|s=s"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=21.25|q=16|s=s"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=21.25|q=1|s=s"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=22.25|q=7|s=s"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=15.33|q=10|s=b"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=15.33|q=20|s=b"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=10.11|q=13|s=b"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=10.11|q=13|s=b"));

        Double ret = orderBookManager.getTotalQtyOverLevel("BTCUSD",Side.SELL, 3);

        System.out.println("ret="+ret);

    }




    @Test
    public void TestGetVolumeWeightedPriceOverLevel(){

        System.out.println("________________ TestGetTotalQtyOverLevel ");


        OrderBookManager orderBookManager = new OrderBookList();
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=19.99|q=2|s=s"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=19.99|q=4|s=s"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=21.25|q=16|s=s"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=21.25|q=1|s=b"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=22.25|q=7|s=s"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=15.33|q=10|s=b"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=15.33|q=20|s=b"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=10.11|q=13|s=b"));
        orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=10.11|q=13|s=b"));

        Map<BigDecimal, List<Double>> ret = orderBookManager.getVolumeWeightedPriceOverLevel("BTCUSD",Side.SELL, 3);

        System.out.println("ret="+ret);

    }













    @Test
    public void TestConcurrentAddDeleteHeavyLoad() throws InterruptedException {

        System.out.println("________________ TestConcurrentAddDeleteHeavyLoad ");

        Random rand = new Random();
        OrderBookManager orderBookManager= new OrderBookList();

        int numberOfJob = 10_000;
        int priceRange=500;

        ExecutorService service = Executors.newFixedThreadPool(10);
        //Define the Latch
        CountDownLatch latchAdd = new CountDownLatch(numberOfJob);
        CountDownLatch latchDelete = new CountDownLatch(numberOfJob);

        //Heavy Add
        for (int i = 0; i < numberOfJob; i++) {
            int finalI = i;
            service.execute(() -> {

                //long qty = rand.nextInt(500)+1;
                long price = rand.nextInt(priceRange)+1;
                orderBookManager.updateOrder(toOrder("t=1638848595|i=ETHUSD|p=15.3"+finalI+"|q="+finalI+1+"|s=s"));
                latchAdd.countDown();
            });
        }

        // Heavy delete
        for (int i = 0; i < numberOfJob; i++) {
            int finalI = i;
            service.execute(() -> {

                orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=15.3"+finalI+"|q=2|s=s"));
                latchDelete.countDown();

            });
        }

        latchAdd.await();
        latchDelete.await();

        for (int i=0; i<(priceRange+1); i++) {
            long price=i+1;
            List<Order> orderList = orderBookManager.getOrdersAtLevel("ETHUSD", Side.SELL, new BigDecimal(price));
            assertTrue(orderList.isEmpty());
        }

    }





    @Test
    public void TestConcurrentAddWithRandomMatchLoad() throws InterruptedException {

        System.out.println("________________ TestConcurrentAddWithRandomMatchLoad ");

        Random rand = new Random();
        OrderBookManager orderBookManager= new OrderBookList();

        int numberOfJob = 20_000;
        //double priceRange=10000;

        ExecutorService service = Executors.newFixedThreadPool(10);
        //Define the Latch
        CountDownLatch latchAddBuy = new CountDownLatch(numberOfJob);
        CountDownLatch latchAddSell = new CountDownLatch(numberOfJob);

        //Heavy Add SELL
        for (int i = 0; i < numberOfJob; i++) {
            int finalI = i;
            service.execute(() -> {

                //long qty = rand.nextInt(500)+1;
                double price = ThreadLocalRandom.current().nextDouble(0.01, 999.99);
                orderBookManager.updateOrder(toOrder("t=1638848595|i=ETHUSD|p="+price+"|q="+finalI+1+"|s=s"));
                latchAddBuy.countDown();
            });
        }

        //Heavy Add BUY
        for (int i = numberOfJob; i < 2*numberOfJob; i++) {
            int finalI = i;
            service.execute(() -> {

                //long qty = rand.nextInt(500)+1;
                double price = ThreadLocalRandom.current().nextDouble(0.01, 999.99);
                orderBookManager.updateOrder(toOrder("t=1638848595|i=ETHUSD|p="+price+"|q="+finalI+1+"|s=b"));
                latchAddSell.countDown();
            });
        }

        latchAddBuy.await();
        latchAddSell.await();

        int cmptDeleteSucess=0;
        int cmptDeleteFailure=0;

        for (int i=0; i<(1000); i++) {
            long price=i+1;
            List<Order> orderList = orderBookManager.getOrdersAtLevel("ETHUSD", Side.SELL, new BigDecimal(price));

            for (Order o : orderList) {
                boolean vRet =orderBookManager.updateOrder(toOrder("t=1638848595|i=BTCUSD|p=19.99|q=2|s=s"));
                if(vRet)
                    cmptDeleteSucess++;
                else
                    cmptDeleteFailure++;
            }
        }


        for (int i=0; i<(1000); i++) {
            long price = i + 1;
            List<Order> orderList = orderBookManager.getOrdersAtLevel("ETHUSD", Side.SELL, new BigDecimal(price));
            assertTrue(!orderList.isEmpty());
        }

        System.out.println("cmptSucess="+cmptDeleteSucess);
        System.out.println("cmptFailure="+cmptDeleteFailure);

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
