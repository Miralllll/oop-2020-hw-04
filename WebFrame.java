import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class WebFrame extends JFrame {

    private ReentrantLock interr = new ReentrantLock();
    private JButton singleTh, multipleTh, stop;
    private JLabel running, completed, elapsed;
    private JPanel panel;
    private DefaultTableModel model;
    private JTable table;
    private JTextField txField;
    private JProgressBar progressBar;
    private long startTime, stopTime;
    private WebLauncher launch;

    public WebFrame(String file){
        super("WebLoader");
        initUpperSide(file);
        initThreadButtons();
        initThreadTxField();
        initLabels();
        initProgressBar();
        initStopButton();
        addListeners();
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /* adds listeners on buttons: single thread and concurrent fetch, stop */
    private void addListeners() {
        addSingleThListener();
        addMultipleThListener();
        addStopListener();
    }

    /* adds a listener on the stop-button */
    private void addStopListener() {
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                changeButtonsState(false);
                stopTime = System.currentTimeMillis();
                synchronized (interr) { // semaphore for better performance
                    launch.interrupt();
                    launch.interruptEveryWorker();
                }
                String elapsedSt = ELAPSED_DF + (stopTime - startTime);
                SwingUtilities.invokeLater(() -> {
                    elapsed.setText(elapsedSt);
                });
            }
        });
    }

    /* adds a listener on the concurrent fetch button */
    private void addMultipleThListener() {
        multipleTh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                preparation(); // change old view situation, refresh it
                int maxThreads = Integer.valueOf(txField.getText());
                if(maxThreads < SINGLE_NUM) maxThreads = SINGLE_NUM;
                launch = new WebLauncher(maxThreads);
                launch.start();
            }
        });
    }

    /* adds a listener on the single thread fetch button */
    private void addSingleThListener() {
        singleTh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                preparation();
                launch = new WebLauncher(SINGLE_NUM);
                launch.start();
            }
        });
    }

    /* change buttons to the oppositely conditions, progress bas as 0%
       label test as default ones and clears everything from status column */
    private void preparation() {
        changeButtonsState(true);
        // startTime = System.currentTimeMillis();
        progressBar.setValue(0);
        labelDefaultMode();
        clearStatusColumn();
    }

    /* clears everything from status column */
    private void clearStatusColumn() {
        for(int rowInd = 0; rowInd < model.getRowCount(); rowInd ++)
            model.setValueAt("", rowInd, 1);
    }

    /* sets default texts to the labels */
    private void labelDefaultMode() {
        elapsed.setText(ELAPSED_DF);
        running.setText(RUNNING_DF);
        completed.setText(COMPLETED_DF);
    }

    /* changes single-thread/multiple-thread buttons as enabled if !(process started now)
       changes stop button as enabled if process started now */
    private void changeButtonsState(boolean nowStarts) {
        singleTh.setEnabled(!nowStarts);
        multipleTh.setEnabled(!nowStarts);
        stop.setEnabled(nowStarts);
    }

    /* initializes a stop-button and adds it to the panel */
    private void initStopButton() {
        stop = new JButton(STOP);
        stop.setEnabled(false);
        panel.add(stop);
    }

    /* initializes a progress-bar, sets start value 0 and adds it to the panel */
    private void initProgressBar() {
        progressBar = new JProgressBar(0, model.getRowCount());
        // progressBar.setString("");
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        panel.add(progressBar);
    }

    /* initializes some labels and adds them to the panel */
    private void initLabels() {
        running = new JLabel(RUNNING_DF);
        panel.add(running);
        completed = new JLabel(COMPLETED_DF);
        panel.add(completed);
        elapsed = new JLabel(ELAPSED_DF);
        panel.add(elapsed);
    }


    /* initializes a text-field and adds it to the panel */
    private void initThreadTxField() {
        txField = new JTextField("4", TX_FIELD_SIZE);
        txField.setMaximumSize(txField.getPreferredSize());
        panel.add(txField);
    }

    /* initializes a progress-bar and adds it to the panel */
    private void initThreadButtons() {
        singleTh = new JButton(SINGLE_THREAD);
        multipleTh = new JButton(MULTIPLE_THREAD);
        panel.add(singleTh);
        panel.add(multipleTh);
    }

    /* initializes a panel, a model, a table, a scrollPanel */
    private void initUpperSide(String file) {
        // most of it is from pdf :))))))
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        model = new DefaultTableModel(new String[] { "url", "status"}, 0);
        updateModel(readFromFile(file));
        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600,300));
        panel.add(scrollPane);
        add(panel);
    }

    /* puts urls in the column of the urls */
    private void updateModel(LinkedList<String> urls) {
        for(int i = 0; i < urls.size(); i++)
            model.addRow(new Object[]{ urls.get(i), "" });
    }


    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            new WebFrame(LINKS2); // LINKS2
        });
    }

    /* reads every line from file and saves them in the list, then returns that list */
    public static LinkedList<String> readFromFile(String file){
        LinkedList<String> urls = new LinkedList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (true){
                String line = reader.readLine();
                if(line == null) break;
                urls.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return urls;
    }

    // ------------------------------------------------------------------------------------

    public class WebLauncher extends Thread {
        private AtomicInteger runningTh, completedTh;
        private Semaphore sem;
        private List<WebWorker> workersList;

        public WebLauncher(int limit){
            // only limit amount of threads together
            sem = new Semaphore(limit);
            runningTh = new AtomicInteger(0);
            completedTh = new AtomicInteger(0);
            workersList = new ArrayList<>();
        }

        @Override
        public void run(){
            // start time from the start of the run
            startTime = System.currentTimeMillis();
            runningTh.incrementAndGet(); // + launcher thread too
            process();
            String elapsedSt = ELAPSED_DF + (stopTime - startTime);
            SwingUtilities.invokeLater(() -> {
                // button change after finished everything
                changeButtonsState(false);
                elapsed.setText(elapsedSt);
                progressBar.setValue(0); // could be commented
            });
            runningTh.decrementAndGet(); // - launcher thread too
            updateView(); // last update of gui
        }

        /* if a preparation process here was interrupted or something else
           stop-process should not continue */
        private void process() {
            if(startProcess())
                stoppedProcess();
        }

        /* waits every worker to finish there jobs */
        private void stoppedProcess() {
            for(int i = 0; i < workersList.size(); i ++) {
                try {
                    workersList.get(i).join();
                } catch (InterruptedException e) {
                    // :)))))))))))))))))))))))))))))
                    //synchronized (interr) {
                    //   interruptEveryWorker(); // stop from here :))
                    //}
                    break;
                }
            }
        }

        /* starts workers from here, if semaphore is not 0... */
        private boolean startProcess() {
            for (int i = 0; i < model.getRowCount(); i++) {
                try {
                    sem.acquire();
                    workersList.add(new WebWorker((String) model.getValueAt(i, 0),
                                i, this));
                    runningTh.incrementAndGet();
                    synchronized (interr) {
                        if (isInterrupted()) throw new InterruptedException();
                        workersList.get(i).start();
                    }
                } catch(InterruptedException e){
                    //synchronized (interr) {
                    //    interruptEveryWorker();
                    //}
                    return false;
                }
            }
            return true;
        }

        /* interrupts every worker in the list*/
        private void interruptEveryWorker() {
            for(int i = 0; i < workersList.size(); i ++)
                if(workersList.get(i).isAlive())
                    workersList.get(i).interrupt();
        }

        /* aster finishing worker its job, launcher should update view,
           this part could be inside of worker... :))) */
        public void updateResults(String status, int rowInd) {
            sem.release(); // worker finished
            model.setValueAt(status, rowInd, 1);
            stopTime = System.currentTimeMillis();
            completedTh.incrementAndGet();
            runningTh.decrementAndGet();
            updateView(); // gui update here
        }

        /* every gui update and changes is here */
        private void updateView() {
            String runningSt = RUNNING_DF.substring(0, RUNNING_DF.length() - 1) + runningTh;
            String completedSt = COMPLETED_DF.substring(0, COMPLETED_DF.length() - 1) + completedTh;
            String elapsedSt = ELAPSED_DF + (stopTime - startTime);
            SwingUtilities.invokeLater(() -> {
                running.setText(runningSt);
                completed.setText(completedSt);
                progressBar.setValue(completedTh.intValue());
                // elapsed.setText(elapsedSt);
            });
        }
    }

    private static final int TX_FIELD_SIZE = 3, SINGLE_NUM = 1;
    private static final String RUNNING_DF = "Running:0", COMPLETED_DF = "Completed:0",
            ELAPSED_DF = "Elapsed:", STOP = "Stop";
    private static final String SINGLE_THREAD = "Single Thread Fetch";
    private static final String MULTIPLE_THREAD = "Concurrent Fetch";
    private static final String LINKS = "links.txt", LINKS2 = "links2.txt";
}
