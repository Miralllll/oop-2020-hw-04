// Bank.java

/*
 Creates a bunch of accounts and uses threads
 to post transactions to the accounts concurrently.
*/

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Bank {
	public static final int ACCOUNTS = 20;	 // number of accounts
	public static final int STARTING_AMOUNT = 1000;
	public static final int QUEUE_SIZE = 10;

	/* It is a thread class. Its objects works on the transaction list */
	protected class Worker extends Thread {
		@Override
		public void run() {
			while (true){
				try {
					Transaction currTran = toDoList.take();
					if(currTran == nullTrans)
						break; // if actual list is empty
					currTran.makeTransaction(accounts);
				} catch (InterruptedException e) {
					// e.printStackTrace();
				}
			}
			latch.countDown();
		}
	}

	/* contains initializations:
	   ArrayBlockingQueue for communicating between the main bank thread and workers.
	   accounts (LinkedList), bank should have 20 accounts for default
	   nullTrans for stopping workers after finishing work */
	public Bank() {
		toDoList = new ArrayBlockingQueue<>(QUEUE_SIZE);
		accounts = new ArrayList<>();
		for(int i = 0; i < ACCOUNTS; i ++)
			accounts.add(new Account(this, i, STARTING_AMOUNT));
		nullTrans = new Transaction(-1,0,0);
	}

	/*
	 Reads transaction data (from/to/amt) from a file for processing.
	 (provided code)
	 */
	public void readFile(String file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			// Use stream tokenizer to get successive words from file
			StreamTokenizer tokenizer = new StreamTokenizer(reader);
			while (true) {
				int read = tokenizer.nextToken();
				if (read == StreamTokenizer.TT_EOF) break;  // detect EOF
				int from = (int)tokenizer.nval;
				tokenizer.nextToken();
				int to = (int)tokenizer.nval;
				tokenizer.nextToken();
				int amount = (int)tokenizer.nval;
				toDoList.put(new Transaction(from, to, amount));
			}
			reader.close();
		}
		catch (Exception e) {
			// e.printStackTrace(); // System.exit(1);
		}
	}

	/*
	 Processes one file of transaction data
	 -fork off workers
	 -read file into the buffer
	 -wait for the workers to finish
	*/
	public void processFile(String file, int numWorkers) throws InterruptedException {
		if(numWorkers <= 0) numWorkers = 1;
		latch = new CountDownLatch(numWorkers);
		runWorkers(numWorkers);
		readFile(file);
		for(int i = 0; i < numWorkers; i ++)
			toDoList.put(nullTrans);
		latch.await();
		for(int i = 0; i < ACCOUNTS; i ++)
			System.out.println(accounts.get(i).toString());
	}

	/* run all workers */
	public void runWorkers(int numWorkers){
		for(int i = 0; i < numWorkers; i ++)
			new Worker().start();
	}

	/* makes a deep copy of accounts, it's useful for testing */
	public List<Account> getAccounts(){
		List<Account> copy = new ArrayList<>();
		for(int i = 0; i < accounts.size(); i ++)
			copy.add(new Account(accounts.get(i)));
		return copy;
	}

	/*
	 Looks at commandline args and calls Bank processing.
	*/
	public static void main(String[] args) throws InterruptedException {
		// deal with command-lines args
		if (args.length == 0) {
			System.out.println("Args: transaction-file [num-workers [limit]]");
			// System.exit(1);
			return;
		}
		String file = args[0];
		int numWorkers = 1;
		if (args.length >= 2) {
			numWorkers = Integer.parseInt(args[1]);
		}
		Bank bank = new Bank();
		bank.processFile(file, numWorkers);
	}

	public boolean allJobDone(){
		return toDoList.isEmpty();
	}

	public int workersNumFinished(){
		return (int) latch.getCount();
	}

	private BlockingQueue<Transaction> toDoList;
	private List<Account> accounts;
	private CountDownLatch latch;
	private final Transaction nullTrans;
}

