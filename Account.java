// Account.java

/*
 Simple, thread-safe Account class encapsulates
 a balance and a transaction count.
*/
public class Account {
	private int id;
	private int balance;
	private int transactions;
	
	// It may work out to be handy for the account to
	// have a pointer to its Bank.
	// (a suggestion, not a requirement)
	private Bank bank;  

	public Account(Bank bank, int id, int balance, int transactions) {
		this.bank = bank;
		this.id = id;
		this.balance = balance;
		this.transactions = transactions;
	}

	/* Common constructor with bank, id and balance information */
	public Account(Bank bank, int id, int balance) {
		this(bank, id, balance, 0);
	}

	/* One more constructor for creating Account objects easier */
	public Account(Account other){
		this(other.getBank(), other.getId(), other.getBalance(), other.getTransactions());
	}

	/* Adds an amount (some money) to a current balance */
	public synchronized void deposit(int amount){
		balance += amount;
		transactions ++;
	}

	/* Minus an amount (some money) to a current balance */
	public synchronized boolean withdraw(int amount){
		// if we want that balance will be always positive,
		// next line should be uncommented.
		// if(balance < amount) return false;
		balance -= amount;
		transactions ++;
		return true;
	}

	/* returns account's id code */
	public int getId(){ return id; }

	/* returns bank where this account is located */
	public Bank getBank(){ return bank; }

	/* returns balance which is on the account currently */
	public synchronized int getBalance(){ return balance; }

	/* returns transactions numbers */
	public synchronized int getTransactions(){ return transactions; }

	@Override
	public String toString(){
		return "acct:" + id + " bal:" + balance + " trans:" + transactions;
	}
}
