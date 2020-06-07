import java.lang.reflect.Array;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Cracker {
	// Array of chars used to produce strings
	public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789.,-!".toCharArray();

	/*
	 Given a byte[] array, produces a hex String,
	 such as "234a6f". with 2 chars for each byte in the array.
	 (provided code)
	*/
	public static String hexToString(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (int i=0; i<bytes.length; i++) {
			int val = bytes[i];
			val = val & 0xff;  // remove higher bits, sign
			if (val<16) buff.append('0'); // leading 0
			buff.append(Integer.toString(val, 16));
		}
		return buff.toString();
	}
	
	/*
	 Given a string of hex byte values such as "24a26f", creates
	 a byte[] array of those values, one byte value -128..127
	 for each 2 chars.
	 (provided code)
	*/
	public static byte[] hexToArray(String hex) {
		byte[] result = new byte[hex.length()/2];
		for (int i=0; i<hex.length(); i+=2) {
			result[i/2] = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
		}
		return result;
	}

	public static void main(String[] args) throws InterruptedException {
		if (args.length <= 0) {
			System.out.println("Args: password\nor\nArgs: target length [workers]");
			return;
		}
		Cracker cracker = new Cracker(); // for both situation
		if (args.length == 1)
			System.out.println(cracker.generateHashValue(args[0]));
		else {
			String targ = args[0];
			int len = Integer.parseInt(args[1]);
			int num = 1;
			if (args.length > 2)
				num = Integer.parseInt(args[2]);
			cracker.recoveryProcess(targ, len, num);
		}
	}

	/* This contains password-recovering and printing processes */
	public void  recoveryProcess(String targ, int len, int num) throws InterruptedException {
		List<String> passwords = recoverPassword(targ, len, num);
		for(int i = 0; i < passwords.size(); i ++)
			System.out.println(passwords.get(i));
		System.out.println("all done");
	}

	/* This contains a password-recovering process. */
	public List<String> recoverPassword(String targ, int len, int num) throws InterruptedException {
		this.target = hexToArray(targ);
		this.limit = len;
		if(num <= 0) num = 1;
		latch = new CountDownLatch(num);
		passwords =  new ArrayList<>();
		diapasons = new ArrayList<>();
		separateWorkingAreas(num);
		latch.await();
		return passwords;
	}

	/* This method divides alphabet as equal parts as possible.
	   These divided parts are useful for workers to build up words started
	   with all letters that are in them (These parts) */
	private void separateWorkingAreas(int num) {
		int charsNumForEach = CHARS.length / num;
		int remainder = CHARS.length % num;
		int start = 0;
		// spreads remained characters
		for(int i = 0; i < remainder; i ++, start+= (charsNumForEach + 1))
			new Worker(start, charsNumForEach + 1).start();
		for(int i = remainder; i < num; i ++, start += charsNumForEach)
			new Worker(start, charsNumForEach).start();
	}

	/* for generating hash value from a password - arg */
	public String generateHashValue(String arg) {
		return hexToString(generateHashValueBytes(arg));
	}

	/* for generating hash value as bytes array from a password - arg */
	private byte[] generateHashValueBytes(String arg){
		byte[] hashValue = null;
		try{
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.update(arg.getBytes());
			hashValue = md.digest();
		} catch (Exception e){
			// e.printStackTrace();
		}
		return hashValue;
	}

	public List<List<Integer>> returnDiapasons(){
		List<List<Integer>> copy = new ArrayList<>();
		for(int i = 0; i < diapasons.size(); i++)
			copy.add(new ArrayList<>(diapasons.get(i)));
		return copy;
	}

	////////////////////////////////////////////////////////////////////

	private class Worker extends Thread {
		private int start, length; // start and end of the part

		public Worker(int start, int length){
			this.start = start;
			this.length = length;
			List<Integer> ls = new ArrayList<>();
			ls.add(start);
			ls.add(length);
			diapasons.add(ls);
		}

		@Override
		public void run(){
			// iterates all characters from start to end (length + start)
			if(limit != 0)
				for(int i = start; i < length + start; i ++)
					wordsStartWith(String.valueOf(CHARS[i]), limit);
				//else ?  // for sending signal, that one more thread finished its work
			latch.countDown();
		}

		/* generates all words which are started with currWord's first (start)
		   letter which are shorter than limit or equal to it */
		private void wordsStartWith(String currWord, int limit) {
			synchronized (passwords) {
				if (Arrays.equals(generateHashValueBytes(currWord), target))
					passwords.add(currWord);
			}
			if(currWord.length() >= limit) return;
			for(int i = 0; i < CHARS.length; i++)
				wordsStartWith(currWord + CHARS[i], limit);
		}
	}

	private List<List<Integer>> diapasons;
	private CountDownLatch latch;
	private byte[] target;
	private int limit;
	private List<String> passwords;
	protected String algorithm = "SHA";
}
