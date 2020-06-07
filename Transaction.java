import java.util.List;

// Transaction.java
/*
 (provided code)
 Transaction is just a dumb struct to hold
 one transaction. Supports toString.
*/
public class Transaction {
	public int from;
	public int to;
	public int amount;

	/* common-type constructor with arguments:
	   from - is an id of an account from which the
	   money (amount) should be transferred.
	   to - is an id of an account to which the
	   money (amount) should be transferred.
	 */
   	public Transaction(int from, int to, int amount) {
		this.from = from;
		this.to = to;
		this.amount = amount;
	}

	/* returns from - is an id of an account from which the
	   money (amount) has be transferred */
	public int getFrom(){ return from; }

	/* returns to - is an id of an account to which the
	   money (amount) has be transferred */
	public int getTo(){ return to; }

	/* returns amount - is an amount of the
	   money should be transferred */
	public int getAmount(){ return amount; }

	/* makes transaction if it is possible */
	public void makeTransaction(List<Account> accounts){
		if(from == to) return;
   		if(accounts.get(from).withdraw(amount))
   			accounts.get(to).deposit(amount);
	}

	@Override
	public String toString() {
		return("from:" + from + " to:" + to + " amt:" + amount);
	}
}
