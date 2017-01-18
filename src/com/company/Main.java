package com.company;

import java.io.*;
import javax.swing.SwingUtilities;


public class Main{

    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {

                    new DartLoader();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}