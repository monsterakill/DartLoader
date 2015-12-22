package com.company;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.text.DefaultEditorKit;
import java.util.List;

/**
 * Created by Admin on 28.10.2015.
 */
public class DartLoader extends JFrame {
    private JPanel mainForm;
    private JButton directorySelect;
    private JTextField usrName;
    private JTextField galleryLinkField;
    private JTextArea inputInfoTextArea;
    private JPasswordField usrPassword;
    private JTextArea trueLog;
    private JTextArea imgName;
    private JButton startDownload;
    private JProgressBar progressBar;
    private JTextArea galleryFoldersInfo;
    private JScrollPane scrollPane;
    private JLabel gLink;
    private JLabel uName;
    private JLabel uPassword;
    private JLabel gFoldersInfo;
    private JLabel Animation;

    JFileChooser chooser = new JFileChooser();


    public DartLoader() throws IOException{
        super("DartLoader alpha 0.2.0");
        setContentPane(mainForm);
        pack();


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);

        //Set Background Img && Background Animation FIX
        setLayout(new BorderLayout());
        JLabel background=new JLabel(new ImageIcon("C:\\Users\\Admin\\IdeaProjects\\Downloader\\src\\FFF.png"));
        add(background);
        background.setLayout(new FlowLayout());
        Animation.setVisible(false);



        directorySelect.addActionListener(e -> {
            try {
                SaveFolderChoose();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        startDownload.addActionListener(e -> LogIn());
    }


    public void SaveFolderChoose() throws IOException {

        //Save Folder Choose
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("choosertitle");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        //Direction Correct Check
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
        } else System.out.println("No Selection ");
            trueLog.setForeground(Color.green);
            trueLog.setText("Selected Directory: " + "\n" + chooser.getSelectedFile());
        if(chooser.getSelectedFile() != null){
            startDownload.setEnabled(true);
        }else{
            trueLog.setForeground(Color.red);
            trueLog.setText("Selected Directory: Not Correct." + "\n" + "Please choose some active save Folder.");
        }
    }

    public void LogIn(){
        SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {

            @Override
            protected Boolean doInBackground() throws Exception {


                float FinalOffset = 0;
                int currentntmAmount = 0;
                boolean doneCheck = false;
                boolean loggedIn = false;
                //Small Internet Connection CHeck
                try {
                    Animation.setVisible(true);
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
                    Object objData = urlConnect.getContent();
                } catch (IOException e) {
                    trueLog.setForeground(Color.red);
                    trueLog.setText("Check your Internet Connection");
                    Animation.setVisible(false);
                }
                Connection.Response resp = Jsoup.connect("https://www.deviantart.com/users/login")
                        .timeout(10 * 1000)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0")
                        .execute();
                Document doc1 = resp.parse();
                progressBar.setValue(30);
                //Take Request of Current Session Token and SiteKey
                Element eltoken = doc1.getElementsByAttributeValueContaining("name", "validate_token").first();
                Element elkey = doc1.getElementsByAttributeValueContaining("name", "validate_key").first();
                String token = eltoken.attr("value");
                String key = elkey.attr("value");
                //Input Current Session Token and SiteKey
                inputInfoTextArea.setText("SiteToken: " + token + "\n" + "SiteKey: " + key + "\n");
                progressBar.setValue(50);
                Map<String, String> cookies1 = resp.cookies();
                String strPassword = new String(usrPassword.getPassword());
                Connection.Response res = Jsoup
                        .connect("https://www.deviantart.com/users/login")
                        .cookies(cookies1)
                        .data("validate_token", token)
                        .data("validate_key", key)
                        .data("username", usrName.getText())
                        .data("password",  strPassword)
                        .data("remember_me", "0")
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0")
                        .method(Connection.Method.POST)
                        .execute();
                Map<String, String> cookies = res.cookies();
                progressBar.setValue(70);
                //Check Account Settings Identity
                Document doc = Jsoup.connect("https://www.deviantart.com/settings/identity").cookies(cookies).get();
                if (doc.getElementById("signature") == null) {
                    progressBar.setValue(100);
                    trueLog.setForeground(Color.red);
                    trueLog.setText("Error");
                } else if (doc.getElementById("signature").hasAttr("id")) {
                    progressBar.setValue(100);
                    trueLog.setForeground(Color.green);
                    trueLog.setText("Logged In");
                    loggedIn = true;
                }
                String line =  galleryLinkField.getText();

                if (loggedIn) {
                    if (line.contains("http://") && line.contains(".deviantart.com") && line.contains("/gallery/"))
                        try {
                            //Gallery Size Scan
                            System.out.println(line);
                            Document gallerySize = Jsoup.connect(line).cookies(cookies).get();
                            Elements galleryFolders = gallerySize.select("div[class=label]").select("a[href*=/gallery/]");
                            //Gallery Include Folders
                            for (Element elOff : galleryFolders) {
                                System.out.println("pageFolders : " + elOff.attr("abs:href"));
                                galleryFoldersInfo.setForeground(Color.green);
                                galleryFoldersInfo.append(elOff.attr("abs:href") + "\n");
                            }

                            //Take Page Offset
                            Elements offSet = gallerySize.select("a[gmi-offset]");
                            for (Element elOff : offSet) {
                                String pageOffsets = elOff.attr("data-offset");
                                if (FinalOffset < Float.parseFloat(pageOffsets)) {
                                    FinalOffset = Float.parseFloat(pageOffsets);
                                }
                                System.out.println("data-offset : " + pageOffsets);
                            }
                            for (int y = 0; y < (FinalOffset / 24) + 1; y++) {
                                Document docGallery = Jsoup.connect(line + "?offset=" + 24 * y).cookies(cookies).get();
                                //Remove Gallery Folders
                                for (Element element : docGallery.select("div.gr-body")) {
                                    element.remove();
                                }

                                //Find images in HTML code
                                Elements img = docGallery.select("a[data-super-img~=(?i)\\.(png|jpe?g|gif)], a[data-super-full-img~=(?i)\\.(png|jpe?g|gif)]");

                                int ntmAmount = img.size(); //Number of images Found in current page

                                //Actual Progress Bar
                                //Not fully correct solution
                                if(FinalOffset != 0){
                                    int FinalOffestint = Math.round(FinalOffset);
                                    ntmAmount = ((FinalOffestint / 24) + 2) * ntmAmount;
                                    progressBar.setMaximum(ntmAmount);
                                }else{
                                    progressBar.setMaximum(ntmAmount);
                                }

                                for (Element el : img) {

                                    String src = el.absUrl("data-super-img");
                                    String srcFull = el.absUrl("data-super-full-img"); //Check Full size IMG
                                    if (srcFull == "") {
                                        srcFull = src;
                                    }

                                    System.out.println("Image Found!");
                                    doneCheck = true;
                                    System.out.println("Preview Img : " + src);
                                    System.out.println("Full Img : " + srcFull);
                                    int indexname = srcFull.lastIndexOf("/");
                                    if (indexname == srcFull.length()) {
                                        srcFull = srcFull.substring(1, indexname);
                                    }

                                    String name = srcFull.substring(indexname, srcFull.length());
                                    imgName.setText(name + "\n");
                                    System.out.println("--------------------------");

                                    try {

                                        System.out.println(ntmAmount);
                                        //Download Image from URL
                                        URL url = new URL(srcFull);
                                        InputStream in = new BufferedInputStream(url.openStream());
                                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                                        byte[] buf = new byte[1024];
                                        int n = 0;
                                        while (-1!=(n=in.read(buf))) {
                                            out.write(buf, 0, n);
                                        }
                                        //Actual Progress bar Value
                                        progressBar.setValue(currentntmAmount += 1);

                                        out.close();
                                        in.close();
                                        byte[] response = out.toByteArray();


                                        // Save image to User Folder
                                        FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile() + name);
                                        fos.write(response);
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (IOException ex) {

                            System.err.println("There was an error");
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    else {
                        Animation.setVisible(false);
                        trueLog.setForeground(Color.red);
                        trueLog.setText("Please Enter Correct Gallery Link!" + "\n" + "For Example: http://tophwei.deviantart.com/gallery/31938578/Me");
                    }
                } else {
                    Animation.setVisible(false);
                    trueLog.setForeground(Color.red);
                    trueLog.setText("Login Error" + "\n" + "Please check your input data and Try Again");
                }
                if(doneCheck) {
                    Animation.setVisible(false);
                    trueLog.setForeground(Color.green);
                    trueLog.setText("Done");
                    //small progress bar fix
                    progressBar.setValue(progressBar.getMaximum());
                }

                return false;
            }
            @Override
            // This will be called if you call publish() from doInBackground()
            // Can safely update the GUI here.
            protected void process(List<Integer> chunks) {





            }
            @Override
            // This is called when the thread finishes.
            // Can safely update GUI here.
            protected void done() {

            }
        };
        worker.execute();
    }
}




