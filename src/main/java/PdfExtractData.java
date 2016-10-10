import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.snowtide.PDF;
import com.snowtide.pdf.Document;
import com.snowtide.pdf.OutputTarget;
import com.snowtide.pdf.Page;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Created by Nick Fytros on 19/9/2016.
 */
public class PdfExtractData extends JFrame {

    private JPanel panel;
    private JLabel pdfLabel, txtLabel;
    private JButton pdfBtn, txtBtn;
    private JTextArea comparisonResults;
    private ImageIcon iconopentxt = new ImageIcon(getClass().getResource("/images/text.png"));
    private ImageIcon iconopenpdf = new ImageIcon(getClass().getResource("/images/pdf.png"));
    private ImageIcon iconcompare = new ImageIcon(getClass().getResource("/images/compare.png"));
    private ImageIcon iconexit = new ImageIcon(getClass().getResource("/images/exit.png"));
    private ImageIcon iconinfo = new ImageIcon(getClass().getResource("/images/info.png"));
    private JProgressBar progBar;

    private String txtFilePath = "";
    private java.util.ArrayList<String> pdfFilePaths = new ArrayList<String>();
    private java.util.ArrayList<String[]> txtFileLines = new ArrayList<String[]>();

    public PdfExtractData() {
        initUI();
    }

    private void initUI() {
        // set the image to display on top left corner
        ImageIcon webIcon = new ImageIcon(getClass().getResource("/images/taxlisis_logo.png"));
        setIconImage(webIcon.getImage());

        createMenuBar();

        panel = (JPanel) getContentPane();

        // buttons for pdf and text file picker
        pdfBtn = new JButton("Επιλογή PDF", iconopenpdf);
        pdfBtn.addActionListener(new OpenPDFFileAction());
        pdfLabel = new JLabel("Δεν έχουν επιλεγεί PDF αρχεία");
        pdfLabel.setForeground(Color.red);

        txtBtn = new JButton("Επιλογή TXT", iconopentxt);
        txtBtn.addActionListener(new OpenTextFileAction());
        txtLabel = new JLabel("Δεν έχει επιλεγεί το αρχείο TXT");
        txtLabel.setForeground(Color.red);

        // result text area and label
        JScrollPane pane = new JScrollPane();
        JLabel comparisonLabel = new JLabel("Αποτελέσματα σύγκρισης:");
        comparisonResults = new JTextArea();
        comparisonResults.setLineWrap(true);
        comparisonResults.setWrapStyleWord(true);
        comparisonResults.setEditable(false);
        comparisonResults.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pane.getViewport().add(comparisonResults);

        // progress bar
        progBar = new JProgressBar();
        progBar.setVisible(false);
        progBar.setStringPainted(true);

        // allow copy paste on resultsTextArea
        comparisonResults.addMouseListener(new ContextMenuMouseListener());

        // compare button
        JButton startButton = new JButton("Σύγκριση", iconcompare);
        startButton.addActionListener((ActionEvent event) -> {
            // check if pdf and txt files are chosen
            if (pdfFilePaths.size() > 0 && pdfFilePaths.size() <= 400 && !txtFilePath.isEmpty()) {
                // reset before run
                progBar.setMaximum(pdfFilePaths.size());
                progBar.setValue(0);
                progBar.setVisible(true);
                comparisonResults.setText("");
                // start the proccess in a thread
                Runnable compare = () -> {
                    for (String pdfPath: pdfFilePaths) {
                        extractDataAndCompare(pdfPath);
                    }
                    System.gc();
                };
                Thread thread = new Thread(compare);
                thread.start();
            }else {
                JOptionPane.showMessageDialog(panel, "Πρέπει να επιλέξετε τα αρχεία PDF (μέχρι 400) και το αρχείο TXT για να ξεκινήσει η σύγκριση",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // set the button on the pane
        createLayout(pdfBtn, pdfLabel, txtBtn, txtLabel, startButton, comparisonLabel, pane, progBar);

        setTitle("Εφαρμογή σύγκρισης αρχείων PDF");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void createMenuBar() {

        JMenuBar menubar = new JMenuBar();

        JMenu file = new JMenu("Αρχείο");
        file.setMnemonic(KeyEvent.VK_F);
        JMenu info = new JMenu("Πληροφορίες");

        JMenuItem eMenuItemopenpdf = new JMenuItem("Επιλογή PDF", iconopenpdf);
        JMenuItem eMenuItemopentxt = new JMenuItem("Επιλογή TXT", iconopentxt);
        JMenuItem eMenuIteminfo = new JMenuItem("Πληροφορίες", iconinfo);
        JMenuItem eMenuItemexit = new JMenuItem("Έξοδος", iconexit);
        eMenuItemexit.setMnemonic(KeyEvent.VK_E);
        eMenuItemexit.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });
        eMenuItemopenpdf.addActionListener(new OpenPDFFileAction());
        eMenuItemopentxt.addActionListener(new OpenTextFileAction());
        eMenuIteminfo.addActionListener((ActionEvent event) -> {
            JOptionPane.showMessageDialog(panel, "Version 1.0.0 \nDeveloped by Nick Fytros for LEONTIOS IKE & ASSOCIATES",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        });

        file.add(eMenuItemopenpdf);
        file.add(eMenuItemopentxt);
        file.add(eMenuItemexit);
        info.add(eMenuIteminfo);

        menubar.add(file);
        menubar.add(info);

        setJMenuBar(menubar);
    }

    private void createLayout(JComponent... arg) {

        Container pane = getContentPane();
        GroupLayout gl = new GroupLayout(pane);
        pane.setLayout(gl);

        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addGroup(gl.createParallelGroup()
                        .addComponent(arg[0])
                        .addComponent(arg[1])
                        .addComponent(arg[2])
                        .addComponent(arg[3])
                        .addComponent(arg[4])
                        .addComponent(arg[7]))
                .addGap(30)
                .addGroup(gl.createParallelGroup()
                        .addComponent(arg[5])
                        .addComponent(arg[6]))
        );

        gl.setVerticalGroup(gl.createParallelGroup()
                .addGroup(gl.createSequentialGroup()
                        .addComponent(arg[0])
                        .addComponent(arg[1])
                        .addGap(30)
                        .addComponent(arg[2])
                        .addComponent(arg[3])
                        .addGap(30)
                        .addComponent(arg[4])
                        .addGap(10)
                        .addComponent(arg[7]))
                .addGroup(gl.createSequentialGroup()
                        .addComponent(arg[5])
                        .addGap(10)
                        .addComponent(arg[6]))
        );
    }

    private class OpenPDFFileAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {

            JFileChooser fdia = new JFileChooser();
            fdia.setMultiSelectionEnabled(true);
            FileFilter filter = new FileNameExtensionFilter("Αρχεία .pdf",
                    "pdf");
            fdia.setFileFilter(filter);

            int ret = fdia.showDialog(panel, "Επιλογή αρχείων");

            if (ret == JFileChooser.APPROVE_OPTION) {
                File[] files = fdia.getSelectedFiles();
                pdfFilePaths.clear();
                for (File file : files) {
                    pdfFilePaths.add(file.getAbsolutePath());
                }
                pdfLabel.setText("Επιλέχθηκαν " + files.length + " αρχεία PDF");
                pdfLabel.setForeground(new Color(21, 193, 198));
            }
        }
    }

    private class OpenTextFileAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {

            JFileChooser fdia = new JFileChooser();
            FileFilter filter = new FileNameExtensionFilter("Αρχεία .txt",
                    "txt");
            fdia.setFileFilter(filter);

            int ret = fdia.showDialog(panel, "Επιλογή αρχείου");

            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = fdia.getSelectedFile();
                txtFilePath = file.getAbsolutePath();
                txtLabel.setText("Επιλέχθηκε το αρχείο: " + file.getName());
                //read file into stream, try-with-resources
                txtFileLines.clear();
                try (Stream<String> stream = Files.lines(Paths.get(txtFilePath),StandardCharsets.ISO_8859_1)) {
                    // add the txt file lines to the arraylist
                    stream.forEach((line) -> {txtFileLines.add(line.split(" +"));});
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                txtLabel.setForeground(new Color(21, 193, 198));
            }
        }
    }

    private void extractDataAndCompare(String pdfFileLocation) {
        String filteredDataFirstPage;
        String filteredDataLastPage;
        Hashtable<String, String> extractedData = new Hashtable<String, String>();
        String[] stoixeiaParastatikou, posa;
        // variable to see if the current pdf is resolved
        boolean pdfResolved = false;

        // DEVELOPED USING WITH SNOWTIDE PDF LIBRARY

        Document pdf = PDF.open(pdfFileLocation);
        // get the last page
        Page firstPage = pdf.getPage(0);
        Page lastPage = pdf.getPage(pdf.getPages().size()-1);
        StringBuilder firstPageText = new StringBuilder(1024);
        StringBuilder lastPageText = new StringBuilder(1024);
        firstPage.pipe(new OutputTarget(firstPageText));
        lastPage.pipe(new OutputTarget(lastPageText));
        try {
            pdf.close();

            // replaces all the whitespaces with single space
            filteredDataFirstPage = firstPageText.toString().replaceAll("\\s+", "~");
            filteredDataLastPage = lastPageText.toString().replaceAll("\\s+", "~");
//            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("C:/Users/fitro/Downloads/export.txt"))) {
//                writer.write(filteredDataFirstPage);
//            }

            stoixeiaParastatikou = getParastatikoData(filteredDataFirstPage).split("~");
            posa = getPosa(filteredDataLastPage).split("~");
            // fill the hashtable with the results
            // if he is ΙΔΙΩΤΗΣ dont search for afm
            if (getValue("ΕΠΩΝΥΜΙΑ~:", filteredDataFirstPage).equals("Ι∆ΙΩΤΗΣ")){
                extractedData.put("afm", "000000000");
            }else extractedData.put("afm", getValue("Α.Φ.Μ.:", filteredDataFirstPage));
            extractedData.put("eidos_parastatikou", getEidosParastatikou(filteredDataFirstPage));
            extractedData.put("seira", stoixeiaParastatikou[0]);
            extractedData.put("number", stoixeiaParastatikou[1]);
            extractedData.put("date", stoixeiaParastatikou[2]);
            // catch the case in which 'ΠΛΗΡΩΤΕΟ ΠΟΣΟ' πινακάκι is empty
            if (posa.length < 3){
                // write the error on results TextArea
                comparisonResults.append("* Το " + new File(pdfFileLocation).getName() + " δεν έχει το πινακάκι 'ΠΛΗΡΩΤΕΟ ΠΟΣΟ' συμπληρωμένο\n");
                extractedData.put("synolo_aksias_ypok_fpa", "");
                extractedData.put("synolo_fpa", "");
                extractedData.put("ammount_to_pay", "");
                pdfResolved = true;
            }else{
                extractedData.put("synolo_aksias_ypok_fpa", posa[0]);
                extractedData.put("synolo_fpa", posa[1]);
                extractedData.put("ammount_to_pay", posa[2]);
            }

            // TODO implement comparison method
            progBar.setValue(progBar.getValue() + 1);
            double total = 0.00;
//            txtFileLines.forEach(lineArray -> {
//                if (lineArray.length > 4) {
//                    System.out.println(lineArray[4] + "  ------------  " + extractedData.get("afm") + " contains: " + lineArray[4].contains(extractedData.get("afm")));
//                }
//                System.out.println(extractedData.get("afm").equals("000000000") || (lineArray[4].length() >= 9 && lineArray[4].contains(extractedData.get("afm"))));
//            });
            ArrayList<String[]> dataExported = (ArrayList<String[]>) txtFileLines.parallelStream().filter(lineArray -> lineArray.length > 4
                    &&(lineArray[1]+lineArray[2].substring(0, 1)).equals(extractedData.get("seira"))
                    &&(lineArray[3].equals(extractedData.get("number")) || lineArray[4].equals(extractedData.get("number")))
                    /* missing the date clause*/
            ).collect(Collectors.toList());
            for ( String[] lineArray: dataExported){
                if ((extractedData.get("afm").equals("000000000") || (lineArray[4].length() >= 9 && lineArray[4].contains(extractedData.get("afm"))))) {
                    total += Double.valueOf(lineArray[lineArray.length - 1].replace(",", "."));
                }else{
                    comparisonResults.append("* Το " + new File(pdfFileLocation).getName() + " δεν έχει το ΑΦΜ του στο txt\n");
                    pdfResolved = true;
                    break;
                }
            }
            total = Double.valueOf(new DecimalFormat("##.##").format(total).replace(",","."));
            if (!extractedData.get("ammount_to_pay").isEmpty() && total > 0.00 && !pdfResolved) {
                if (total != Double.valueOf(extractedData.get("ammount_to_pay").replace(",", "."))) {
                    comparisonResults.append("* Το " + new File(pdfFileLocation).getName() + " διαφέρει κατά " + new DecimalFormat("##.##").format(Math.abs(total - Double.valueOf(extractedData.get("ammount_to_pay").replace(",", ".")))) + " ευρώ\n");
                }
            }else if (!pdfResolved){
                comparisonResults.append("* Το " + new File(pdfFileLocation).getName() + " δεν είναι καταχωρημένο\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getValue (String key, String pdfData){
        String value = "";
        // going to the result of the key and getting the data
        int dataIndexStart = pdfData.indexOf(key)+key.length()+1;
        while(!String.valueOf(pdfData.charAt(dataIndexStart)).equals("~")){
            value += String.valueOf(pdfData.charAt(dataIndexStart));
            dataIndexStart ++;
        }
        return value;
    }

    private String getPosa (String pdfData){
        // key to start index searching on '~' separated document data
        String key = "ΠΛΗΡ~ΤΕΟ~ΠΟΣΟ";
        String value = "";
        // going to the result of the key and getting the data
        if (pdfData.indexOf(key) == -1){
            key = "ΠΛΗΡΩΤΕΟ~ΠΟΣΟ";
        }
        int dataIndexStart = pdfData.indexOf(key)+key.length()+1;
        // stop when you find '~' and the next value is not a comma or a number
        while(!String.valueOf(pdfData.charAt(dataIndexStart)).equals("~") || String.valueOf(pdfData.charAt(dataIndexStart+1)).matches("[0-9,]")){
            value += String.valueOf(pdfData.charAt(dataIndexStart));
            dataIndexStart ++;
        }
        return value;
    }

    private String getEidosParastatikou (String pdfData){
        // key to start index searching on '~' separated document data
        String key = "ΗΜΕΡΟΜΗΝΙΑ";
        String value = "";
        // going to the result of the key and getting the data
        int dataIndexStart = pdfData.indexOf(key)+key.length()+1;
        // stop when you find '~' and the next value is not a comma or a number
        while(!String.valueOf(pdfData.charAt(dataIndexStart)).equals("~") || !String.valueOf(pdfData.charAt(dataIndexStart+1)).matches("[0-9]")){
            value += String.valueOf(pdfData.charAt(dataIndexStart));
            dataIndexStart ++;
        }
        return value.replace("~"," ");
    }

    private String getParastatikoData (String pdfData){
        // key to start index searching on '~' separated document data
        String key = "ΣΚΟΠΟΣ~∆ΙΑΚΙΝΗΣΗΣ";
        String value = "";
        // going to the result of the key and getting the data
        int dataIndexStart = pdfData.indexOf(key)-2;
        // stop when you find '~' and the next value is not a comma or a number
        while(!String.valueOf(pdfData.charAt(dataIndexStart)).equals("~") || String.valueOf(pdfData.charAt(dataIndexStart-1)).matches("[0-9,/]")){
            value += String.valueOf(pdfData.charAt(dataIndexStart));
            dataIndexStart --;
        }
        return new StringBuffer(value).reverse().toString();
    }

    public static void main(String args[]) {
        EventQueue.invokeLater(() -> {
            PdfExtractData ex = new PdfExtractData();
            ex.setVisible(true);
        });
    }
}
