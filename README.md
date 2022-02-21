# OrderbookVersion3 - Crypto Currency -  Bitcoin and Ethereum

<br><br>Core Technology - Task 
Order book is a collection of levels representing asset price and aggregated quantity sorted by the best price. It consists of 2 sides
<br>•	bids which contain the buy orders
<br>•	asks which contain the sell orders


<br><br>For example
10: 100 12.34 | 12.44 400
21: 220 12.24 | 12.54 500
32: 150 12.14 | 12.64 300
<br>In the above example the best bid (at level 0) is 100 units @ 12.34 and the best ask (at level 0) is 400 units @ 12.44
Exchange will send data in the following format (encoded as ASCII).
t=1638848595|i=BTCUSD|p=32.99|q=100|s=b


<br><br>where:
<br>t - UNIX timestamp (milliseconds since epoch)
<br>i - instrument
<br>p - price
<br>q - quantity
<br>s - side (can be 'b' for 'buy' or 's' for 'sell')



<br><br>The order book follows a set of update rules
<br>•	If the quantity is not zero it means order book level at specified price needs to be updated (or inserted if it was not there)
<br>•	If the quantity is zero it means the order book level at specified price must be removed from the book


<br><br>Assumptions about incoming market data
<br>•	The instrument universe is BTCUSD, ETHUSD, SOLUSD
<br>•	The price is in the range between “0.01” and “999.99”
<br>•	The quantity is in the range between “0” and “10737418.23”
<br>•	The scale for price and quantity is in the range between 0 and 2
<br>•	Prices and quantities can have different scale for the same instrument, e.g. it is possible to receive updates with price of “12.3” (scale equals 1) and “12.30” (scale equals 
<br>2) which would refer to the same level in the order book
<br>•	The exchange will only send valid market data, i.e. no crossed books, no out of order levels, no message field re-ordering 


<br><br>Write a program that will consume incoming stream of order book updates (add/change/delete) and reconstruct the order book. It's perfectly fine for the market data to be read from a file.
At any point the user should be able to perform any of the operations for the given instrument and side




