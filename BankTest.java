import org.junit.Test;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BankTest {
    private int workerNums[] = new int[]{ 1, 2, 3, 4, 5, 10, 20, 50, 100 };
    private static final String FILE_SMALL = "small.txt";
    private static final String FILE_5K = "5k.txt";
    private static final String FILE_100K = "100k.txt";

    @Test
    public void testSimple0() throws InterruptedException {
        // get text from output and check it, this test is for "error message"
        OutputStream st = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(st);
        System.setOut(ps);
        Bank.main(new String[]{});
        String lines[] = st.toString().split("\\r?\\n");
        assertEquals("Args: transaction-file [num-workers [limit]]", lines[0]);
    }

    @Test
    public void testSimple1() throws InterruptedException {
        // giving incorrect argument
        Bank.main(new String[]{ "Mari.txt" });
        // giving more than 2 argument (for testing main better :)))
        Bank.main(new String[]{ FILE_SMALL , "1", "sadd"});
        // if a thread interrupted, worker should catch interruption-exception
        new Bank(){
            @Override
            public void processFile(String file, int numWorkers) throws InterruptedException {
                super.processFile(file, numWorkers);
                readFile(FILE_SMALL);
                Worker wk = new Worker();
                wk.start();
                wk.interrupt();
            }
        }.processFile(FILE_SMALL, 10);
    }

    @Test
    public void testSimple2() throws InterruptedException {
        // get text from output and check it, for small-file :)
        OutputStream st = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(st);
        System.setOut(ps);
        Bank.main(new String[]{ FILE_SMALL });
        String lines[] = st.toString().split("\\r?\\n");
        for(int i = 0; i < lines.length; i += 2){
            assertTrue(lines[i + 1].contains("1001"));
            assertTrue(lines[i].contains("999"));
        }
    }

    @Test // tests program on a small list.
    public void testMedium0() throws InterruptedException {
        testForDiffWorkersNum(FILE_SMALL, 1001, 999);
    }

    @Test // tests program on a 5k-list.
    public void testMedium1() throws InterruptedException {
        testForDiffWorkersNum(FILE_5K, 1000, 1000);
    }

    @Test //tests program on a 100k-list.
    public void testMedium2() throws InterruptedException {
        testForDiffWorkersNum(FILE_100K, 1000, 1000);
    }

    @Test // counts worker threads
    public void testMedium3() throws InterruptedException {
        Bank bank = new Bank();
        bank.runWorkers(10);
        assertEquals(12, Thread.activeCount());
    }

    public void testForDiffWorkersNum(String fileName, int odd, int even) throws InterruptedException {
        for(int i = 0; i < workerNums.length; i ++){
            Bank bank = new Bank();
            bank.processFile(fileName, workerNums[i]); // processes file
            assertFalse(checkOddAccounts(bank.getAccounts(), odd + 1));
            assertTrue(checkOddAccounts(bank.getAccounts(), odd));
            assertFalse(checkEvenAccounts(bank.getAccounts(), even + 3));
            assertTrue(checkEvenAccounts(bank.getAccounts(), even));
        }
    }

    /* checks if odd place accounts contain balances which are equal to amount */
    private boolean checkOddAccounts(List<Account> accounts, int amount) {
        for (int i = 1; i < accounts.size(); i += 2) {
            if (accounts.get(i).getBalance() != amount) return false;
        }
        return true;
    }

    /* checks if even place accounts contain balances which are equal to amount */
    private boolean checkEvenAccounts(List<Account> accounts, int amount) {
        for (int i = 0; i < accounts.size(); i += 2) {
            if (accounts.get(i).getBalance() != amount) return false;
        }
        return true;
    }

    @Test // transaction checking
    public void testMedium4() throws IOException, InterruptedException {
        int transArr[] = readTransactions();
        Bank bank = new Bank();
        bank.processFile(FILE_SMALL, 4);
        for (int i = 1; i < 20; i ++){
            String p = bank.getAccounts().get(i).toString();
            System.out.println(p);
            int acct = p.indexOf(":");
            int bal = p.indexOf(":",acct + 1);
            int trans = p.indexOf(":",bal + 1);
            assertEquals(transArr[i],Integer.parseInt(p.substring(trans + 1)));
        }
    }

    @Test // checks if job done, and every worker finished job
    public void testMedium5() throws InterruptedException {
        Bank bank = new Bank();
        bank.processFile(FILE_SMALL, 4);
        assertTrue(bank.allJobDone());
        assertEquals(0, bank.workersNumFinished());
    }

    private int[] readTransactions() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(FILE_SMALL));
        StreamTokenizer tokenizer = new StreamTokenizer(bufferedReader);
        int arr[] = new int [20];
        while (true){
            if (tokenizer.nextToken() == StreamTokenizer.TT_EOF) break;
            int from, to;
            from = (int)tokenizer.nval;
            tokenizer.nextToken();
            to = (int) tokenizer.nval;
            tokenizer.nextToken();
            arr[from] ++;
            arr[to] ++;
        }
        return arr;
    }

    @Test
    public void testHard0() throws InterruptedException {
        // reuses a bank for working on the lists.
        Bank bank = new Bank();
        for(int i = 0; i < 10; i ++){
            bank.processFile(FILE_5K, i * 2);
            assertTrue(checkOddAccounts(bank.getAccounts(), 1000));
            assertTrue(checkEvenAccounts(bank.getAccounts(), 1000));
        }
    }

    @Test
    public void testHard1() throws InterruptedException {
        // reuses a bank for working on the lists.
        Bank bank = new Bank();
        for(int i = 0; i < 3; i ++){
            bank.processFile(FILE_100K, i * 2);
            assertTrue(checkOddAccounts(bank.getAccounts(), 1000));
            assertTrue(checkEvenAccounts(bank.getAccounts(), 1000));
        }
    }
}