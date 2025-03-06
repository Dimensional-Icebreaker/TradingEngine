import random
import threading
# Improvements: the atomic counter doesn’t fully meet the requirement of being lock-free. Ideally, a lock-free solution would not require any locks (e.g., using atomic operations provided by specific libraries).
class AtomicCounter:
    """ Non-ideal Lock-free atomic counter using a simple increment. """
    def __init__(self, initial=0):
        self.value = initial
        self.lock = threading.Lock() # Lockfree solution would not require any locks and python requires third party libraries to implement atomic operations

    def increment(self):
        """ Atomic increment function. """
        with self.lock:
            self.value += 1
            return self.value

class Order:
    """ Represents an order in the order book. """
    def __init__(self, order_type, ticker, quantity, price, order_id):
        self.order_type = order_type  
        self.ticker = ticker
        self.quantity = quantity
        self.price = price
        self.order_id = order_id  # Unique identifier for the order
        self.next = None  # Pointer for  list

class OrderList:
    """ Lock-free singly linked list for storing orders. """
    def __init__(self):
        self.head = None

    def add_order(self, new_order):
        """ Add the new order into the linked list in sorted order. """
        while True:
            current_head = self.head
            if not current_head or (new_order.order_type == 'Buy' and new_order.price > current_head.price) or (new_order.order_type == 'Sell' and new_order.price < current_head.price):
                new_order.next = current_head
                if self._compare_and_swap_head(current_head, new_order):
                    break
            else:
                current = current_head
                while current.next and ((new_order.order_type == 'Buy' and current.next.price >= new_order.price) or (new_order.order_type == 'Sell' and current.next.price <= new_order.price)):
                    current = current.next
                new_order.next = current.next
                if self._compare_and_swap_next(current, new_order):
                    break

    def pop_head(self):
        """ Remove and return the first order in the list (highest priority). """
        while True:
            current_head = self.head
            if not current_head:
                return None
            new_head = current_head.next
            if self._compare_and_swap_head(current_head, new_head):
                return current_head

    def _compare_and_swap_head(self, expected, new):
        """ Simulated atomic compare-and-swap operation for the head of the linked list. """
        if self.head == expected:
            self.head = new
            return True
        return False

    def _compare_and_swap_next(self, node, new):
        """ Simulated atomic compare-and-swap operation for the next pointer of a node. """
        if node.next == new.next:
            node.next = new
            return True
        return False

class OrderBook:
    """ Order book for each stock ticker, handling buy and sell orders. """
    def __init__(self):
        self.buy_orders = OrderList()
        self.sell_orders = OrderList()

    def add_order(self, new_order):
        """ Add order to the correct order list and match orders. """
        if new_order.order_type == 'Buy':
            self.buy_orders.add_order(new_order)
        else:
            self.sell_orders.add_order(new_order)
        self.match_orders()

    def match_orders(self):
        """ Match buy and sell orders while maintaining O(n) complexity. """
        while self.buy_orders.head and self.sell_orders.head and self.buy_orders.head.price >= self.sell_orders.head.price:
            buy_order = self.buy_orders.head
            sell_order = self.sell_orders.head
            traded_quantity = min(buy_order.quantity, sell_order.quantity)
            trade_price = sell_order.price
            print(f'Matched Order: {buy_order.ticker} | Price: {trade_price} | Quantity: {traded_quantity}')
            
            buy_order.quantity -= traded_quantity
            sell_order.quantity -= traded_quantity
            
            if buy_order.quantity == 0:
                self.buy_orders.pop_head()
            if sell_order.quantity == 0:
                self.sell_orders.pop_head()

class TradingEngine:
    """ Trading engine to manage stock orders and simulate real-time trading. """
    def __init__(self):
        self.order_books = [OrderBook() for _ in range(1024)]  # 1,024 tickers (stocks)
        self.order_id_counter = AtomicCounter(0)  # Custom atomic counter
    
    def add_order(self, order_type, ticker_index, quantity, price):
        """ Generate an order and add it to the order book for the ticker. """
        order = Order(order_type, 'STOCK' + str(ticker_index), quantity, price, self.order_id_counter.increment())
        self.order_books[ticker_index - 1].add_order(order)
    
    def simulate_trading(self, num_orders=100):
        """ Simulate trading with random order generation. """
        for _ in range(num_orders):
            order_type = random.choice(['Buy', 'Sell'])
            ticker_index = random.randint(1, 1024)
            quantity = random.randint(1, 100)
            price = random.randint(10, 500)
            self.add_order(order_type, ticker_index, quantity, price)

if __name__ == "__main__":
    engine = TradingEngine()
    engine.simulate_trading(200)  # Simulate 200 orders
