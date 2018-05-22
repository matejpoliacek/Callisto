package com.chocolateam.galileopvt;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GnssLogger {
    /**
     *  Class that logs NMEA
     */
    private static final Date mDate = new Date();
    private static final String PREFIX = "gnss_log";
    private final Object mFileLock = new Object();
    private BufferedWriter mFileWriter;
    private File baseDirectory;


    public void startNewLog(String constellation){
        synchronized (mFileLock) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                baseDirectory = new File(Environment.getExternalStorageDirectory(), PREFIX);
                if (!baseDirectory.exists()){
                    baseDirectory.mkdirs();
                }
                Log.e("LOG SUCCESS", "Can create the directory");
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                Log.e("LOG ERROR", "Cannot write to external storage.");
                return;
            } else {
                Log.e("LOG ERROR", "Cannot read external storage.");
                return;
            }

            //SimpleDateFormat format = new SimpleDateFormat("hhmmss.SSS");
            //Date now = new Date();
            String filename = String.format("%s.log", PREFIX + "_" + constellation);
            File currentFile = new File(baseDirectory, filename);
            String currentFilePath = currentFile.getAbsolutePath();
            BufferedWriter currentFileWriter;

            try {
                FileWriter writer = new FileWriter(currentFile);
                writer.append("a");
                writer.flush();
                writer.close();
            }catch (IOException e){
                Log.e("LOG ERROR", "e");
            }

            try {
                currentFileWriter = new BufferedWriter(new FileWriter(currentFile));
            } catch (IOException e) {
                Log.e("LOG ERROR", "Could not open file: " + currentFilePath, e);
                return;
            }
            try {
                currentFileWriter.write("Test file log");
            } catch (IOException e) {
                Log.e("LOG ERROR", "Could not initialise file");
            }
            if (mFileWriter != null) {
                try {
                    mFileWriter.close();
                } catch (IOException e) {
                    Log.e("LOG ERROR", "Unable to close all file streams");
                }
            }
        }
    }

    public void appendLog(String constellation, double longitude, double latitude, double alt, int satnumber, int hdop){

        synchronized (mFileLock){
            File currentFile = new File(baseDirectory,"gnss_log_" + constellation);
            try {
                BufferedWriter buf = new BufferedWriter(new FileWriter(currentFile, true));
                // We don't have the HDOP calculated so I'll just skip it for now
                buf.append("Constellation: ").append(constellation).append(",").append("Lon").append(String.valueOf(longitude)).append(",").append("Lat").append(String.valueOf(latitude)).append(",").append("Alt").append(String.valueOf(alt)).append(",").append("SatNumber").append(String.valueOf(satnumber));
                buf.newLine();
                buf.close();
                Log.e("LOG SUCCESS", "Wrote log");
            }catch (IOException e){
                Log.e("LOG ERROR", "Could not write in file");
            }
        }
    }
}
