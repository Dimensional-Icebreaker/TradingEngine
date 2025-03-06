# Stock Trading Engine

## Overview
This project implements a real-time stock trading engine capable of matching buy and sell orders in a simplified stock market environment. The engine supports up to 1,024 stock tickers and provides functionality for placing buy and sell orders while handling concurrent transactions from multiple brokers.

The primary goal of this engine is to simulate real-time trading by processing buy and sell orders and matching them based on price and quantity. This project aims to address challenges in multi-threaded environments, such as race conditions and lock-free data structures.

## Features

### Order Matching:
- Buy orders are matched with sell orders based on price, with buy price greater than or equal to the sell price.
- Orders are matched in O(n) time complexity, where 'n' is the number of orders in the order book.

### Concurrency Handling:
- Lock-free data structures are used for order management to avoid race conditions when multiple threads modify the order book.
- The code avoids using dictionaries, maps, or similar data structures, adhering to the constraints of the problem.

### Atomic Operations:
- Atomic counters are used for generating unique order IDs and for safely incrementing counters across multiple threads.

### Order Simulation:
- A wrapper is implemented to simulate random order transactions by multiple brokers (simulating active stock transactions).

## Project Structure
The project consists of the following key components:
- **Order Class**: Defines the structure of an order (buy or sell).
- **OrderBook Class**: Maintains the order book for each ticker, responsible for adding orders and matching them.
- **TradingEngine Class**: Manages the stock order books and handles the simulation of real-time trading.
- **AtomicCounter Class**: Simulates an atomic counter for generating unique order IDs (note: this solution is not fully lock-free).

## Requirements
- **Java Version**: 1.8 or higher (for the Java implementation).
- **Python Version**: 3.7 or higher (for the Python implementation).

## Improvements & Drawbacks

### 1. Atomic Counter:
- **Drawback**: The current implementation of the atomic counter is not truly lock-free. While Python's threading.Lock is used to ensure safe increments, it introduces blocking, which contradicts the lock-free requirement.
- **Improvement**: Implement a true lock-free counter using specialized libraries or lower-level atomic operations. In languages such as Java, `AtomicInteger` from `java.util.concurrent.atomic` can be used for better atomicity.

### 2. Order Book Management:
- **Drawback**: The use of `AtomicReferenceArray` in the Java version to store buy and sell orders is a lock-free solution but lacks true efficiency for real-world applications with high concurrency.
- **Improvement**: Implement a more advanced lock-free data structure, such as a concurrent skip list, which would offer better performance and scalability for managing orders in a highly concurrent environment.

### 3. Order Matching Algorithm:
- **Drawback**: The `matchOrders` function has a time complexity of O(n), which is acceptable for smaller systems but may become a bottleneck as the number of orders increases.
- **Improvement**: Consider implementing a more efficient order matching algorithm, such as a priority queue or binary search tree, to reduce time complexity when the number of orders grows.

### 4. Concurrency and Race Condition Handling:
- **Drawback**: While lock-free structures are used to some extent, there are still scenarios where multiple threads may modify the order book simultaneously, which can lead to potential race conditions.
- **Improvement**: Implement proper thread synchronization using more advanced techniques like CAS (Compare-And-Swap) for concurrent updates, and test thoroughly for edge cases.

### 5. Order Simulation:
- **Drawback**: The simulation of brokers placing orders is a simplified random process, which may not accurately reflect real-world trading patterns or order flow.
- **Improvement**: Enhance the simulation by introducing more realistic market behaviors, such as market volatility, order book depth, and the impact of larger trades on the price.

### 6. Data Structure Constraints:
- **Drawback**: The problem statement restricts the use of maps, dictionaries, and other built-in data structures that can make implementation more efficient.
- **Improvement**: While this is a design constraint, future versions could relax this requirement and use more appropriate data structures to improve scalability and performance.

## Future Enhancements
1. **Distributed System**: In the future, the trading engine can be extended to a distributed system to handle large-scale trading scenarios, with load balancing and fault tolerance.
2. **Advanced Market Features**: Features such as limit orders, market orders, and order cancellations can be added to simulate a more realistic trading environment.
3. **Real-Time Price Feed**: Integrate real-time market data (price, volume, etc.) to influence order matching and enhance the simulation.

## Running the Project

### Java Implementation
1. Compile the Java code using:
   ```bash
   javac TradingEngine.java
2. Run the Java program
   ```bash
   java TradingEngine
### Python Implementation
1. **Install required dependencies**:
   ```bash
      pip install threading

2. Run the Python program
   ```bash
   python trading_engine.py

#Conclusion
This project provides a simplified yet effective model for a stock trading engine, emphasizing real-time order matching and concurrent transaction handling. With improvements in lock-free data structures, order matching algorithms, and concurrency management, this engine can be further optimized for high-performance use in real-world scenarios.

   

