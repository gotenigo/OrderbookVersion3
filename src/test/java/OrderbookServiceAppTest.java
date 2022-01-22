import com.diy.Side.Side;
import com.diy.Utils.Utils;
import com.diy.domain.Order;
import com.diy.orderbookmanager.OrderBook;
import com.diy.orderbookmanager.OrderBookList;
import com.diy.orderbookmanager.OrderBookManager;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class OrderbookServiceAppTest {




    @Test
    public void TestOrderBookIsCreatedOK() {

        System.out.println("________________ TestOrderBookIsCreatedOK");


        OrderBook orderBook = new OrderBook("BTCUSD"); // OrderBook is created

        assertTrue(orderBook instanceof OrderBook);
        assertTrue(orderBook.toString() instanceof String);
        assertTrue(orderBook.getVolumeWeightedPriceOverLevel("BTCUSD",Side.SELL,2).isEmpty());

        boolean successState = orderBook.addOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=32.9|q=100|s=b"));  // an Order is added
        boolean successState2 =orderBook.addOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=32.90|q=100|s=b")); // Duplicate Order is added as well

        assertTrue(successState);
        assertTrue(!successState2);
        assertNotNull(orderBook);
        assertTrue(!orderBook.getOrderbookBid().isEmpty()); //confirm order is present in the OrderBook
        assertTrue(orderBook.getOrderbookAsk().isEmpty()); //confirm order the wrong side remain empty


/*
        BigDecimal pricecompare = new BigDecimal(32.9).setScale(2, RoundingMode.HALF_UP);
        System.out.print("pricecompare="+pricecompare);

        assertTrue(orderBook.getVolumeWeightedPriceOverLevel("BTCUSD",Side.BUY,5).containsKey( pricecompare  ));
        assertEquals("BTCUSD", orderBook.getInstrument());
  */
    }




    @Test
    public void TestOrderBookListIsCreatedOK() {

        System.out.println("________________ TestOrderBookListIsCreatedOK");

        OrderBookList orderBookList = new OrderBookList();

        assertTrue(orderBookList instanceof OrderBookList);
        assertTrue(orderBookList.toString() instanceof String);
        assertNotNull(orderBookList);
    }



    @Test(expected = NullPointerException.class)
    public void TestOrderBookManagerNullArgAdd()  {

        System.out.println("________________ TestOrderBookManagerNullArgAdd ");

        OrderBookManager orderBookManager = new OrderBookList();
        orderBookManager.updateOrder(null);

    }





    @Test(expected = NullPointerException.class)
    public void TestOrderBookManagerGetBestPrice()  {

        System.out.println("________________ TestOrderBookManagerGetBestPrice ");

        OrderBookManager orderBookManager = new OrderBookList();
        orderBookManager.getBestPrice(null,null);

    }



    @Test(expected = NullPointerException.class)
    public void TestOrderBookManagerGetOrdersAtLevel()  {

        System.out.println("________________ TestOrderBookManagerGetOrdersAtLevel ");

        OrderBookManager orderBookManager = new OrderBookList();
        orderBookManager.getOrdersAtLevel(null,null, new BigDecimal("0"));

    }



    @Test
    public void TestOrderBookManagerAddOrderAndBestPrice() {

        System.out.println("________________ TestOrderBookManagerAddOrderAndBestPrice");

        OrderBookManager orderBookManager = new OrderBookList();

        Order order = Utils.toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=b");
        boolean successState = orderBookManager.updateOrder(order);


        Optional<BigDecimal> bestPrice = orderBookManager.getBestPrice("BTCUSD", Side.BUY);

        BigDecimal bestPriceVal=BigDecimal.ZERO; // To check with mentor
        if(bestPrice.isPresent()) {
            bestPriceVal = bestPrice.get();
        }

        assertTrue(bestPrice.isPresent());
        assertEquals("32.99", bestPriceVal.toString());

    }


    @Test
    public void TestOrderBookManagerAddOrderAndOrderAtLevel() {

        System.out.println("________________ TestOrderBookManagerAddOrderAndOrderAtLevel");

        OrderBookManager orderBookManager = new OrderBookList();
        Order order = Utils.toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=s");

        List<Order> orderList = orderBookManager.getOrdersAtLevel("BTCUSD", Side.SELL, new BigDecimal("32.99"));
        assertTrue(orderList.size() == 0);

        orderBookManager.updateOrder(order);
        orderList = orderBookManager.getOrdersAtLevel("BTCUSD", Side.SELL, new BigDecimal("32.99"));

        assertTrue(orderList instanceof List);
        assertTrue(orderList.size() > 0);

    }


    @Test
    public void TestOrderBookManagerAddOrderAndDeleteOrder() {

        System.out.println("________________ TestOrderBookManagerAddOrderAndDeleteOrder");

        OrderBookManager orderBookManager = new OrderBookList();

        Order order = Utils.toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=b");
        Order order2 = Utils.toOrder("t=1638848595|i=BTCUSD|p=32.99|q=0|s=b");

        orderBookManager.updateOrder(order);
        orderBookManager.updateOrder(order2);

        List<Order> orderList = orderBookManager.getOrdersAtLevel("BTCUSD", Side.BUY, new BigDecimal("32.99"));
        assertTrue(orderList.isEmpty());

    }


    @Test
    public void TestMockOrderBookListAddOrderExecuted() {

        System.out.println("________________ TestMockOrderBookListAddOrderExecuted");

        OrderBookList orderBookList = mock(OrderBookList.class);
        doReturn(true).when(orderBookList).updateOrder(isA(Order.class));
        Order order = Utils.toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=b");
        orderBookList.updateOrder(order);
        verify(orderBookList, times(1)).updateOrder(order);

    }


    @Test
    public void TestMockOrderBookListUpdateOrder() {

        System.out.println("________________ TestMockOrderBookListDeleteOrderExecuted");

        OrderBookList mockOrderBookList = mock(OrderBookList.class);
        doReturn(true).when(mockOrderBookList).updateOrder(isA(Order.class));
        boolean ret = mockOrderBookList.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=19.99|q=2|s=s"));
        assertTrue(ret);
        verify( mockOrderBookList, times(1)).updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=19.99|q=2|s=s"));

    }






    @Test(expected = NumberFormatException.class)
    public void TestOrderNullPointerException() {
        System.out.println("________________ TestOrderWrongArgument ");
        Order order = Utils.toOrder("t=|i=BTCUSD|p=32.88|q=123|s=s");
    }


    @Test(expected = IllegalArgumentException.class)
    public void TestIllegalArgumentException() {
        System.out.println("________________ TestIllegalArgumentException ");
        Order order = Utils.toOrder("t=1638848595|i=BTCUSD|p=32.99|q=-1|s=s");
    }


    @Test
    public void TestDuplicatedNotAllowed() {
        System.out.println("________________ TestDuplicatedNotAllowed ");
        Order order = Utils.toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=b");
        OrderBookList orderBookList = new OrderBookList();
        orderBookList.updateOrder(new Order(order));
        order = Utils.toOrder("t=1638848595|i=BTCUSD|p=32.99|q=100|s=b");
        orderBookList.updateOrder(new Order(order));
        List<Order> orderList = orderBookList.getOrdersAtLevel("BTCUSD", Side.BUY, new BigDecimal("32.99"));

        assertTrue(orderList.size() == 1);

    }


    @Test
    public void Test(){


        double a = 0.02;
        double b = 0.01;
        double c = b / a;
        System.out.println(c);

        BigDecimal _a = new BigDecimal("0.02");
        BigDecimal _b = new BigDecimal("0.01");
        BigDecimal _c = _b.divide(_a);
        System.out.println(_c);

    }

    @Test
    public void TestGetOrdersUpToLevel(){

        System.out.println("________________ TestGetOrdersUpToLevel ");


        OrderBookManager orderBookManager = new OrderBookList();
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=19.99|q=2|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=19.99|q=4|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=21.25|q=16|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=21.25|q=1|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=22.25|q=7|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=15.33|q=10|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=15.33|q=20|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=10.11|q=13|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=10.11|q=13|s=s"));

        Map<BigDecimal, Set<Order>> retSell = orderBookManager.getOrdersUpToLevel("BTCUSD",Side.SELL, 3);
        Map<BigDecimal, Set<Order>> ret2Buy = orderBookManager.getOrdersUpToLevel("BTCUSD",Side.BUY, 3);

        assertTrue(retSell.size()==3);
        assertTrue(ret2Buy.size()==3);

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


        orderBookManager.updateOrder(Utils.toOrder("t=1642817574433|i=BTCUSD|p=19.99|q=2|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1642817574433|i=BTCUSD|p=19.99|q=4|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1642817574433|i=BTCUSD|p=21.25|q=16|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1642817574433|i=BTCUSD|p=21.25|q=1|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1642817574433|i=BTCUSD|p=22.25|q=7|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1642817574433|i=BTCUSD|p=15.33|q=10|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1642817574433|i=BTCUSD|p=15.33|q=20|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1642817574433|i=BTCUSD|p=10.11|q=13|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1642817574433|i=BTCUSD|p=10.11|q=13|s=b"));

        Double ret = orderBookManager.getAveragePriceOverLevel("BTCUSD",Side.SELL, 3);

        System.out.println("ret="+ret);

    }




    @Test
    public void TestGetTotalQtyOverLevel(){

        System.out.println("________________ TestGetTotalQtyOverLevel ");


        OrderBookManager orderBookManager = new OrderBookList();
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=19.99|q=2|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=19.99|q=4|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=21.25|q=16|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=21.25|q=1|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=22.25|q=7|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=15.33|q=10|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=15.33|q=20|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=10.11|q=13|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=10.11|q=13|s=b"));

        Double ret = orderBookManager.getTotalQtyOverLevel("BTCUSD",Side.SELL, 3);

        System.out.println("ret="+ret);

    }




    @Test
    public void TestGetVolumeWeightedPriceOverLevel(){

        System.out.println("________________ TestGetTotalQtyOverLevel ");


        OrderBookManager orderBookManager = new OrderBookList();
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=19.99|q=2|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=19.99|q=4|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=21.25|q=16|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=21.25|q=1|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=22.25|q=7|s=s"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=15.33|q=10|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=15.33|q=20|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=10.11|q=13|s=b"));
        orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=10.11|q=13|s=b"));

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
                orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=ETHUSD|p=15.3"+finalI+"|q="+finalI+1+"|s=s"));
                latchAdd.countDown();
            });
        }

        // Heavy delete
        for (int i = 0; i < numberOfJob; i++) {
            int finalI = i;
            service.execute(() -> {

                orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=15.3"+finalI+"|q=2|s=s"));
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
                orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=ETHUSD|p="+price+"|q="+finalI+1+"|s=s"));
                latchAddBuy.countDown();
            });
        }

        //Heavy Add BUY
        for (int i = numberOfJob; i < 2*numberOfJob; i++) {
            int finalI = i;
            service.execute(() -> {

                //long qty = rand.nextInt(500)+1;
                double price = ThreadLocalRandom.current().nextDouble(0.01, 999.99);
                orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=ETHUSD|p="+price+"|q="+finalI+1+"|s=b"));
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
                boolean vRet =orderBookManager.updateOrder(Utils.toOrder("t=1638848595|i=BTCUSD|p=19.99|q=2|s=s"));
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


}
