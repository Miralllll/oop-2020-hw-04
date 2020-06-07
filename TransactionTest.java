import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    public void testSimple0(){
        // checks only get-type methods and constructor
        for (int i = 0; i < 20; i ++) {
            Transaction trans = new Transaction(i + 1, i, i);
            assertEquals(i + 1, trans.getFrom());
            assertEquals(i, trans.getTo());
            assertEquals(i, trans.getAmount());
        }
    }

    @Test
    public void testSimple1(){
        // checks the constructor and toString method too
        for (int i = 0; i < 20; i ++) {
            Transaction trans = new Transaction(i + 1, i, i * 2);
            assertTrue(trans.toString().contains(Integer.toString(i + 1)));
            assertTrue(trans.toString().contains(Integer.toString(i)));
            assertTrue(trans.toString().contains(Integer.toString(i * 2)));
        }
    }

    @Test
    public void testSimple2(){
        Bank bank = new Bank();
        List<Account> accounts = new ArrayList<>();
        // checks situation when transaction should fail
        for(int i = 0; i < 2; i ++)
            accounts.add(new Account(bank, i, 1000) {
                @Override
                public boolean withdraw(int amount){
                    if(getBalance() < amount) return false;
                    return super.withdraw(amount);
                }
            });
        // 1100  is not on the account, so nothing should be changed
        Transaction trans1 = new Transaction(1, 0, 1100);
        trans1.makeTransaction(accounts);
        assertEquals(1000, accounts.get(1).getBalance());
        assertEquals(1000, accounts.get(0).getBalance());
        // 100  is on the account, so a transaction should be successful.
        Transaction trans2 = new Transaction(1, 0, 100);
        trans2.makeTransaction(accounts);
        assertEquals(900, accounts.get(1).getBalance());
        assertEquals(1100, accounts.get(0).getBalance());
    }

    @Test
    public void testSimple3(){
        Bank bank = new Bank();
        // tests makeTransaction method...
        List<Account> accounts = new ArrayList<>();
        for(int i = 0; i < 20; i ++)
            accounts.add(new Account(bank, i, 1000));
        for (int i = 0; i < 20; i += 2) {
            Transaction trans = new Transaction(i + 1, i, i * 10);
            trans.makeTransaction(accounts);
            assertEquals(1000 + i * 10, accounts.get(i).getBalance());
            assertEquals(1000 - i * 10, accounts.get(i + 1).getBalance());
        }
    }

    @Test
    public void testSimple4(){
        Bank bank = new Bank();
        // tests when source and destination accounts are same
        List<Account> accounts = new ArrayList<>();
        for(int i = 0; i < 5; i ++)
            accounts.add(new Account(bank, i, 1000));
        for (int i = 0; i < 5; i ++) {
            Transaction trans = new Transaction(i , i, 10);
            trans.makeTransaction(accounts);
            assertEquals(0, accounts.get(i).getTransactions());
            assertEquals(1000, accounts.get(i).getBalance());
        }
    }
}