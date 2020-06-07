import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class WebWorker extends Thread {
    private WebFrame.WebLauncher view;
    private String urlString; // for downloading
    private String status; // for updating view
    private int rowInd; // for updating view --> status

    public WebWorker(String url, int rowInd, WebFrame.WebLauncher view){
        urlString = url;
        this.rowInd = rowInd;
        this.view = view;
    }

    @Override
    public void run(){
        webDownload();
        view.updateResults(status, rowInd);
    }

    /* code from pdf with little changes */
    public void webDownload() {
        InputStream input = null;
        StringBuilder contents = null;
		try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            // Set connect() to throw an IOException
            // if connection does not succeed in this many msecs.
            if(isInterrupted()) throw new InterruptedException();
            connection.setConnectTimeout(5000);
            connection.connect();
            input = connection.getInputStream();
            if(isInterrupted()) throw new InterruptedException();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            char[] array = new char[1000];
            int len;
            contents = new StringBuilder(1000);
            while ((len = reader.read(array, 0, array.length)) > 0) {
                // If a worker was interrupted checks ...
                if(isInterrupted()) throw new InterruptedException();
                contents.append(array, 0, len);
                Thread.sleep(100);
            }
            if(isInterrupted()) throw new InterruptedException();
            // Successful download if we get here
            String pattern = "HH:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            // use the number of chars, although in reality
            // the number of bytes could be larger :)))
            status = date + " " + contents.length() + "bytes";
        } // Otherwise control jumps to a catch...
		catch(MalformedURLException ignored) { status = "err"; }
		// deal with interruption ?
		catch(InterruptedException exception) { status = "interrupted"; }
		catch(IOException ignored) { status = "err"; }
        // "finally" clause, to close the input stream... in any case
		finally {
            try {
                if (input != null) input.close();
            } catch (IOException ignored) { }
            if(isInterrupted()) status = "interrupted";
        } // Check one more time, this part could be commented, but I wrote it for better performance

    }
}
