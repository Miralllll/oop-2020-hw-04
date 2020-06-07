import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {
    private Bank bank = new Bank();

    @Test
    public void testSimple0(){
        // tests simple get-type methods
        Account[] accounts = new Account[20];
        for (int i = 0; i < accounts.length; i ++) {
            accounts[i] = new Account(bank, i, i * 10);
        }
        for (int i = 0; i < accounts.length; i ++){
            assertEquals(i, accounts[i].getId());
            assertEquals(bank, accounts[i].getBank());
            assertEquals(i * 10, accounts[i].getBalance());
            assertEquals(0, accounts[0].getTransactions());
        }
    }

    @Test
    public void testSimple1(){
        // tests a deposit method
        Account[] accounts = new Account[20];
        for (int i = 0; i < accounts.length; i ++) {
            accounts[i] = new Account(bank, i, i * 10);
        }
        for (int i = 0; i < accounts.length; i ++){
            int oldBalance = accounts[i].getBalance();
            accounts[i].deposit(100);
            assertEquals(oldBalance + 100, accounts[i].getBalance());
        }
    }

    @Test
    public void testSimple2(){
        // tests a withdraw method
        Account[] accounts = new Account[20];
        for (int i = 0; i < accounts.length; i ++) {
            accounts[i] = new Account(bank, i, i * 20);
        }
        for (int i = accounts.length / 2; i < accounts.length; i ++){
            int oldBalance = accounts[i].getBalance();
            accounts[i].withdraw(10 * i);
            assertEquals(10 * i, accounts[i].getBalance());
            assertEquals("acct:" + i + " bal:" + 10 * i + " trans:"
                    + 1 , accounts[i].toString());
        }
    }

    @Test
    public void testSimple3(){
        // checks both type constructors and get methods too.
        Account[] accounts = new Account[20];
        for (int i = 0; i < accounts.length; i ++) {
            accounts[i] = new Account(bank, i, i * 20);
        }
        for (int i = 0; i < accounts.length; i ++) {
            assertEquals(accounts[i].getId(), new Account(accounts[i]).getId());
            assertEquals(accounts[i].getBank(), new Account(accounts[i]).getBank());
            assertEquals(accounts[i].getBalance(), new Account(accounts[i]).getBalance());
        }
    }
}