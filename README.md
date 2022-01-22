# OrderbookVersion3

Core Technology - Task 
Order book is a collection of levels representing asset price and aggregated quantity sorted by the best price. It consists of 2 sides
•	bids which contain the buy orders
•	asks which contain the sell orders
For example
10: 100 12.34 | 12.44 400
21: 220 12.24 | 12.54 500
32: 150 12.14 | 12.64 300
In the above example the best bid (at level 0) is 100 units @ 12.34 and the best ask (at level 0) is 400 units @ 12.44
Exchange will send data in the following format (encoded as ASCII).
t=1638848595|i=BTCUSD|p=32.99|q=100|s=b
where:
t - UNIX timestamp (milliseconds since epoch)
i - instrument
p - price
q - quantity
s - side (can be 'b' for 'buy' or 's' for 'sell')
The order book follows a set of update rules
•	If the quantity is not zero it means order book level at specified price needs to be updated (or inserted if it was not there)
•	If the quantity is zero it means the order book level at specified price must be removed from the book
Assumptions about incoming market data
•	The instrument universe is BTCUSD, ETHUSD, SOLUSD
•	The price is in the range between “0.01” and “999.99”
•	The quantity is in the range between “0” and “10737418.23”
•	The scale for price and quantity is in the range between 0 and 2
•	Prices and quantities can have different scale for the same instrument, e.g. it is possible to receive updates with price of “12.3” (scale equals 1) and “12.30” (scale equals 2) which would refer to the same level in the order book
•	The exchange will only send valid market data, i.e. no crossed books, no out of order levels, no message field re-ordering 
Write a program that will consume incoming stream of order book updates (add/change/delete) and reconstruct the order book. It's perfectly fine for the market data to be read from a file.
At any point the user should be able to perform any of the operations for the given instrument and side
•	What is the top level
•	Iterate over the levels in the ascending (asks) and descending (bids) price order
•	Average price over N top levels
•	Total quantity over N top levels
•	Volume weighted price over N top levels
Make sure your solution delivers high performance and scales well. Your code should be well tested, clean and easy for others to understand. Write your code as if you were writing production code.
FAQ
1.	Does the test reflect types of problems I will be solving in my job?
o	Absolutely!
2.	What is the time limit?
o	Ideally the task should be completed over the weekend.
3.	What is the submission format?
o	Either zip the solution and email to us or share a link to the git repository.
4.	What is the implementation language?
o	Java or Rust
5.	Which Java version can I use?
o	JDK11
6.	Which Java libraries can I use in non-test code
o	None
7.	Which Rust version can I use?
o	Only stable tool chain min version 1.57
8.	Which Rust crates can I use in non-test code?
o	You should rely on what's in the standard library but do let us know if there is a create you feel should be used so that we can review and approve
9.	Can I make any additional assumptions
o	Yes, as long as you document them

