import org.junit.jupiter.api.Test;

import javax.print.DocFlavor;
import javax.security.auth.login.CredentialException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CrackerTest {

    @Test
    public void testSimple0() throws InterruptedException {
        // tests no argument situation
        OutputStream st = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(st);
        System.setOut(ps);
        Cracker.main(new String[]{});
        String lines[] = st.toString().split("\\r?\\n");
        assertEquals("Args: password", lines[0]);
        assertEquals("or", lines[1]);
        assertEquals("Args: target length [workers]", lines[2]);
    }

    @Test
    public void testSimple1() throws InterruptedException {
        // tests one argument and a 0-thread situation too
        OutputStream st = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(st);
        System.setOut(ps);
        String outPassword = generateRandomPassword(1);
        Cracker.main(new String[]{ outPassword });
        String lines[] = st.toString().split("\\r?\\n");
        Cracker.main(new String[]{ lines[0], "4", "0" }); // 1
        lines = st.toString().split("\\r?\\n");
        assertEquals(outPassword, lines[1]);
    }

    /* generates randomly a password which should have length-length */
    private String generateRandomPassword(int length){
        String password = "";
        for(int i = 0; i < length; i++){
            int randomIndex = new Random().nextInt(Cracker.CHARS.length);
            password += Cracker.CHARS[randomIndex];
        }
        return password;
    }

    @Test
    public void testSimple2() throws InterruptedException {
        // simple example of using random word as a password and cracking it back
        // tests 2 argument situation in the main too
        OutputStream st = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(st);
        System.setOut(ps);
        String outPassword = generateRandomPassword(1);
        Cracker.main(new String[]{ outPassword });
        String lines[] = st.toString().split("\\r?\\n");
        Cracker.main(new String[]{ lines[0], "4" });
        lines = st.toString().split("\\r?\\n");
        assertEquals(outPassword, lines[1]);
    }

    @Test
    public void testSimple3() throws InterruptedException {
        Cracker cracker = new Cracker();
        String saveOld = cracker.algorithm;
        cracker.algorithm = "SSS";
        assertThrows(Exception.class, () -> {
            cracker.generateHashValue("mira");
        });
        cracker.algorithm = saveOld;
    }

    @Test
    public void testMedium0() throws InterruptedException {
        Cracker cr = new Cracker();
        // if generates every part of CHARS array
        for(int i = 0; i < Cracker.CHARS.length; i ++){
            String password = String.valueOf(cr.CHARS[i]) + cr.CHARS[cr.CHARS.length - 1 - i];
            String hashValue = cr.generateHashValue(password);
            assertEquals("[" + password + "]",
                    cr.recoverPassword(hashValue, 3, 7).toString());
        }
    }

    @Test // check diapason logic
    public void testMedium1() throws InterruptedException {
        Cracker cr = new Cracker();
        String hashValue = cr.generateHashValue("pas");
        cr.recoverPassword(hashValue, 3, 7);
        List<List<Integer>> diapasons = cr.returnDiapasons();
        for(int i = 0; i < diapasons.size() - 1; i ++)
            assertEquals(diapasons.get(i).get(0) + diapasons.get(i).get(1),
                    diapasons.get(i + 1).get(0));
    }

    @Test
    public void testHard0() throws InterruptedException {
        Cracker cracker = new Cracker();
        for(int i = 0; i <= 2; i ++) {
            String password = generateRandomPassword(i);
            String hashValue = cracker.generateHashValue(password);
            assertEquals("[" + password + "]", cracker.recoverPassword(hashValue, i,
                    new Random().nextInt(Cracker.CHARS.length)).toString());
        }
    }

    // ---------------- needs lots of time :))) 15 minutes :))) from here to the bottom

    /*
    public void testHard0() throws InterruptedException {
        // it is heavy process from here, the test creates 6 random words, but
        // with different length 0, 1, 2, 3, 4, 5, generates hash values and cracks them back
        Cracker cracker = new Cracker();
        for(int i = 0; i <= 5; i ++) {
            String password = generateRandomPassword(i);
            String hashValue = cracker.generateHashValue(password);
            assertEquals("[" + password + "]", cracker.recoverPassword(hashValue, i,
                    new Random().nextInt(Cracker.CHARS.length)).toString());
        }
    }

    @Test
    public void testHard1() throws InterruptedException {
        // the test creates 3 random words, but
        // with different length 0, 1, 2 generates their hash values and
        // cracks them back, but with the idea that
        // their length would be twice longer than they are
        Cracker cracker = new Cracker();
        for(int i = 0; i <= 2; i ++) {
            String password = generateRandomPassword(i);
            String hashValue = cracker.generateHashValue(password);
            assertEquals("[" + password + "]", cracker.recoverPassword(hashValue, i * 2,
                    new Random().nextInt(Cracker.CHARS.length)).toString());
        }
    }

    @Test
    public void testHard2() throws InterruptedException {
        // molly's length is 5, so it is hard to crack. need some time...
        // test use 4 thread so 40 character will be separated perfectly
        OutputStream st = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(st);
        System.setOut(ps);
        Cracker.main(new String[]{ "molly" });
        String lines[] = st.toString().split("\\r?\\n");
        assertEquals("4181eecbd7a755d19fdf73887c54837cbecf63fd", lines[0]);
        Cracker.main(new String[]{ lines[0], "5", "4" });
        lines = st.toString().split("\\r?\\n");
        assertEquals("molly", lines[1]);
    }

    @Test
    public void testHard3() throws InterruptedException {
        // molly's length is 5, so it is hard to crack. need some time...
        // test use 7 thread so 40 character could be separated not so good, see time results...
        // compare to the second hard2 test time result :)))))))
        OutputStream st = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(st);
        System.setOut(ps);
        Cracker.main(new String[]{ "molly" });
        String lines[] = st.toString().split("\\r?\\n");
        assertEquals("4181eecbd7a755d19fdf73887c54837cbecf63fd", lines[0]);
        Cracker.main(new String[]{ lines[0], "5", "7" });
        lines = st.toString().split("\\r?\\n");
        assertEquals("molly", lines[1]);
    }

    @Test
    public void testHard4() throws InterruptedException {
        // molly's length is 5, so it is hard to crack. need some time...
        // test use 11 thread so 40 character could be separated not so good, see time results...
        // compare to the second hard2 and hard3 test time results :)))))))
        OutputStream st = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(st);
        System.setOut(ps);
        Cracker.main(new String[]{ "molly" });
        String lines[] = st.toString().split("\\r?\\n");
        assertEquals("4181eecbd7a755d19fdf73887c54837cbecf63fd", lines[0]);
        Cracker.main(new String[]{ lines[0], "5", "11" });
        lines = st.toString().split("\\r?\\n");
        assertEquals("molly", lines[1]);
    }
    */
 }