import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

class Order {
    String orderType; 
    String ticker;
    int quantity;
    int price;
    int orderId;

    public Order(String orderType, String ticker, int quantity, int price, int orderId) {
        this.orderType = orderType;
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
        this.orderId = orderId;
    }
}

class OrderBook {
    // Lock-free implementation using AtomicReferenceArray
    AtomicReferenceArray<Order> buyOrders;  // Array for Buy orders
    AtomicReferenceArray<Order> sellOrders; // Array for Sell orders
    AtomicInteger buyIndex = new AtomicInteger(0); // Thread-safe index for buy orders
    AtomicInteger sellIndex = new AtomicInteger(0); // Thread-safe index for sell orders

    public OrderBook() {
        // Using an AtomicReferenceArray for lock-free access
        buyOrders = new AtomicReferenceArray<>(1024);
        sellOrders = new AtomicReferenceArray<>(1024);
    }

    // Add order to the respective order book
    public void addOrder(Order order) {
        if ("Buy".equals(order.orderType)) {
            addBuyOrder(order);
        } else {
            addSellOrder(order);
        }
        matchOrders();  // Attempt to match orders after adding
    }

    // Lock-free adding of buy order
    private void addBuyOrder(Order order) {
        int index = buyIndex.getAndIncrement();
        buyOrders.set(index, order);
    }

    // Lock-free adding of sell order
    private void addSellOrder(Order order) {
        int index = sellIndex.getAndIncrement();
        sellOrders.set(index, order);
    }

    // Match orders in O(n) time complexity
    public void matchOrders() {
        int i = 0, j = 0;
        while (i < buyIndex.get() && j < sellIndex.get()) {
            Order buyOrder = buyOrders.get(i);
            Order sellOrder = sellOrders.get(j);

            // Skip null orders (already matched and removed)
            if (buyOrder == null) {
                i++;
                continue;
            }
            if (sellOrder == null) {
                j++;
                continue;
            }

            // Check if the buy price is greater than or equal to the sell price
            if (buyOrder.price >= sellOrder.price) {
                // Determine the traded quantity
                int tradedQuantity = Math.min(buyOrder.quantity, sellOrder.quantity);
                int tradePrice = sellOrder.price;

                System.out.println("Matched Order: " + buyOrder.ticker + " | Price: " + tradePrice + " | Quantity: " + tradedQuantity);

                // Update the quantities after trade
                buyOrder.quantity -= tradedQuantity;
                sellOrder.quantity -= tradedQuantity;

                // Remove orders if fully matched
                if (buyOrder.quantity == 0) {
                    buyOrders.set(i, null);  // Remove the buy order from the list
                }
                if (sellOrder.quantity == 0) {
                    sellOrders.set(j, null);  // Remove the sell order from the list
                }

                // Move to the next order if the current one is fully matched
                if (buyOrder.quantity == 0) i++;
                if (sellOrder.quantity == 0) j++;
            } else {
                // Move to the next buy order if the current one cannot be matched
                i++;
            }
        }
    }
}

class TradingEngine {
    AtomicReferenceArray<OrderBook> orderBooks; // For 1,024 tickers
    AtomicInteger orderIdCounter; // Atomic counter for unique order IDs

    public TradingEngine() {
        orderBooks = new AtomicReferenceArray<>(1024);
        for (int i = 0; i < 1024; i++) {
            orderBooks.set(i, new OrderBook());
        }
        orderIdCounter = new AtomicInteger(0); // Atomic counter to generate unique order IDs
    }

    // Add order to the engine for a specific ticker
    public void addOrder(String orderType, int tickerIndex, int quantity, int price) {
        int orderId = orderIdCounter.incrementAndGet();
        String ticker = "STOCK" + tickerIndex;
        Order order = new Order(orderType, ticker, quantity, price, orderId);
        orderBooks.get(tickerIndex - 1).addOrder(order);
    }

    // Simulate trading with multiple brokers
    public void simulateTrading(int numOrders, int numBrokers) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(numBrokers);

        for (int i = 0; i < numBrokers; i++) {
            final int brokerId = i;
            executorService.submit(() -> {
                for (int j = 0; j < numOrders; j++) {
                    String orderType = new Random().nextBoolean() ? "Buy" : "Sell";
                    int tickerIndex = new Random().nextInt(1024) + 1;
                    int quantity = new Random().nextInt(100) + 1;
                    int price = new Random().nextInt(500) + 10;

                    // Simulate a broker placing an order
                    addOrder(orderType, tickerIndex, quantity, price);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }
}

public class javaTradingEngine {
    public static void main(String[] args) throws InterruptedException {
        TradingEngine engine = new TradingEngine();
        engine.simulateTrading(200, 10);  // Simulate 200 orders with 10 brokers
    }
}
