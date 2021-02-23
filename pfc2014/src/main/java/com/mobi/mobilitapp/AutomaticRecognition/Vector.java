package com.mobi.mobilitapp.AutomaticRecognition;

import android.os.Environment;
import android.util.Log;

import com.google.common.primitives.Doubles;
//import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import static java.lang.Math.abs;
import org.jtransforms.fft.DoubleFFT_1D;

import com.mobi.mobilitapp.Capture.CSVFile;
import com.mobi.mobilitapp.Capture.SensorSample;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;

/**
 * Created by Fran Medina on 18/09/17.
 */

public class Vector {

    final static double GRAVITY = 9.80665;
    final static int SAMPLING_RATE = 100;
    final static int WINDOW_SIZE = 1000;        // size of window in ms
    final static int WINDOW_STEPS = 250;        // overlap window step in ms
    final static int WINDOW_NUM_SAMPLES = 100;  // num of samples per window
    private double[] vector;

    public Vector() {
        String[] className = {"stationary", "walk", "run", "bicycle", "motorbike", "car", "bus", "metro", "train", "tram"};
        double acc_std, mag_std, acc_p_srd, acc_h_std, acc_p_e_b1, acc_p_e_b2, acc_p_e_b3, acc_p_e_b4,
                mag_norm_e_b1, mag_norm_e_b2, mag_norm_e_b3, mag_norm_e_b4, acc_p_sc, acc_p_ss, mag_sc, mag_ss,
                acc_normg_zcr, min_p, max_p;
    }

    private double[] norm(double[] x, double[] y, double[] z) {

        double[] norm = new double[x.length];

        for (int i = 0; i < x.length; i++) {
            norm[i] = Math.sqrt(x[i]*x[i] + y[i]*y[i] + z[i]*z[i]);
        }

        return norm;
    }

    private double[] accelerometerOrientationEstimationP(double[] x, double[] y, double[] z) {

        double vx, vy, vz;
        double[] dx, dy, dz, p, px, py, pz, pp;
        vx = vy = vz = 0;
        dx = dy = dz = p = px = py = pz = new double[x.length];

        for (double i : x)
            vx += i;
        vx = vx/x.length;
        for (int i = 0; i < x.length; i++)
            dx[i] = x[i] - vx;

        for (double i : y)
            vy += i;
        vy = vy/y.length;
        for (int i = 0; i < y.length; i++)
            dy[i] = y[i] - vy;

        for (double i : z)
            vz += i;
        vz = vz/z.length;
        for (int i = 0; i < z.length; i++)
            dz[i] = z[i] - vz;

        // Vertical & Horizontal Component of the dynamic acceleration vector d
        for (int i = 0; i < x.length; i++) {
            p[i] = (dx[i] * vx + dy[i] * vy + dz[i] * vz) / (vx * vx + vy * vy + vz * vz);
            px[i] = p[i] * vx;
            py[i] = p[i] * vy;
            pz[i] = p[i] * vz;
        }

        pp = norm(px, py, pz);

        return pp;
    }

    private double[] accelerometerOrientationEstimationH(double[] x, double[] y, double[] z) {

        double vx, vy, vz;
        double[] dx, dy, dz, p, px, py, pz, hx, hy, hz, hh;
        vx = vy = vz = 0;
        dx = dy = dz = p = px = py = pz = hx = hy = hz = new double[x.length];

        for (double i : x)
            vx += i;
        vx = vx / x.length;
        for (int i = 0; i < x.length; i++)
            dx[i] = x[i] - vx;

        for (double i : y)
            vy += i;
        vy = vy / y.length;
        for (int i = 0; i < y.length; i++)
            dy[i] = y[i] - vy;

        for (double i : z)
            vz += i;
        vz = vz / z.length;
        for (int i = 0; i < z.length; i++)
            dz[i] = z[i] - vz;

        // Vertical & Horizontal Component of the dynamic acceleration vector d
        for (int i = 0; i < x.length; i++) {
            p[i] = (dx[i] * vx + dy[i] * vy + dz[i] * vz) / (vx * vx + vy * vy + vz * vz);
            px[i] = p[i] * vx;
            py[i] = p[i] * vy;
            pz[i] = p[i] * vz;

            hx[i] = dx[i] - px[i];
            hy[i] = dy[i] - py[i];
            hz[i] = dz[i] - pz[i];
        }

        hh = norm(hx, hy, hz);

        return hh;
    }

    // SENSOR MEAN???

    // STANDARD DEVIATION
    private double sensorStd(double[] x, double[] y, double[] z) {

        double std = 0.0;
        double[] v = new double[x.length * 3];

        v = Doubles.concat(x,y,z);

        //DescriptiveStatistics dS = new DescriptiveStatistics(v);
        //std = dS.getStandardDeviation();

        return std;
    }

    // ENERGY
    private double energy(double[] x) {

        double e = 0.0;
        double[] x2 = new double[x.length];

        for (int i = 0; i < x.length; i++) {
            x[i] = abs(x[i]);
            x2[i] = x[i] * x[i];
            e += x2[i];
        }
        return e;
    }

    // SPECTRAL CENTROID
    private double spectralCentroid(double[] x) {

        double c, sum;
        c = sum = 0.0;

        for (int i = 0; i < x.length; i++) {
            c += x[i] * i;
            sum += x[i];
        }
        return c/sum;
    }

    // SPECTRAL SPREAD
    private double spectralSpread(double[] x) {

        double c = spectralCentroid(x);
        double s, sum;
        s = sum = 0;

        for (int i = 0; i < x.length; i++) {
            s += (i - (c * c)) * x[i];
            sum += x[i];
        }
        return s/sum;
    }

    // ZERO CROSSING RATE
    private int zeroCrossingRate(double[] x) {

        int sum = 0;

        for (int i = 1; i < x.length; i++) {
            if(x[i] * x[i-1] <= 0)
                sum++;
        }
        return sum;
    }

    // FUNCTION THAT BUILDS THE FEATURE VECTOR
    public double[] featureVector(double[] ax, double[] ay, double[] az,
                                   double[] mx, double[] my, double[] mz) {

        double[] feat = new double[19];
        double[] p = new double[ax.length];
        double[] h = new double[ax.length];
        double[] acc_normg, mag_norm, sig;
        double[] BAND1 = new double[3];
        double[] BAND2 = new double[4];
        double[] BAND3 = new double[9];
        double[] BAND4 = new double[25];
        int sig_len = 0;
        double max, min;
        max = min = 0;

        p = accelerometerOrientationEstimationP(ax, ay, az);
        double[] fftPx, fftMx;
        fftPx = fftMx = new double[p.length/2];

        h = accelerometerOrientationEstimationH(ax, ay, az);
        acc_normg = norm(ax, ay, az);
        mag_norm = norm(mx, my, mz);


        for (int i = 0; i < ax.length; i++) {
            acc_normg[i] -= GRAVITY;

        }

        sig = p;
        sig_len = sig.length;
        DoubleFFT_1D pFFT = new DoubleFFT_1D(sig_len);
        pFFT.complexForward(sig);
        // Normalize the FFT
        for (int i = 0; i < sig_len/2; i++)
            fftPx[i] = abs(sig[i]);

        sig = mag_norm;
        sig_len = sig.length;
        DoubleFFT_1D mFFT = new DoubleFFT_1D(sig_len);
        mFFT.complexForward(sig);
        // Normalize the FFT
        for (int i = 0; i < sig_len/2; i++)
            fftMx[i] = abs(sig[i]);

        vector[0] = sensorStd(ax, ay, az);
        vector[1] = sensorStd(ax, ay, az);
        //DescriptiveStatistics dSp = new DescriptiveStatistics(p);
        //vector[2] = dSp.getStandardDeviation();
        //DescriptiveStatistics dSh = new DescriptiveStatistics(h);
        //vector[3] = dSh.getStandardDeviation();
        // BAND1 - BAND4 for accelerometer
        for (int i = 1; i < 4; i++)
            BAND1[i-1] = fftPx[i];
        vector[4] = energy(BAND1);
        for (int i = 5; i < 9; i++)
            BAND2[i-5] = fftPx[i];
        vector[5] = energy(BAND2);
        for (int i = 10; i < 19; i++)
            BAND3[i-10] = fftPx[i];
        vector[6] = energy(BAND3);
        for (int i = 20; i < 45; i++)
            BAND4[i-20] = fftPx[i];
        vector[7] = energy(BAND4);
        // BAND1 - BAND4 for magnetic field
        for (int i = 1; i < 4; i++)
            BAND1[i-1] = fftMx[i];
        vector[8] = energy(BAND1);
        for (int i = 5; i < 9; i++)
            BAND2[i-5] = fftMx[i];
        vector[9] = energy(BAND2);
        for (int i = 10; i < 19; i++)
            BAND3[i-10] = fftMx[i];
        vector[10] = energy(BAND3);
        for (int i = 20; i < 45; i++)
            BAND4[i-20] = fftMx[i];
        vector[11] = energy(BAND4);
        vector[12] = spectralCentroid(p);
        vector[13] = spectralSpread(p);
        vector[14] = spectralCentroid(fftMx);
        vector[15] = spectralSpread(fftMx);
        vector[16] = zeroCrossingRate(acc_normg);

        for (int i = 0; i < p.length; i ++) {
            if (min > p[i])
                min = p[i];
            if (max < p[i])
                max = p[i];
        }
        vector[17] = min;
        vector[18] = max;

        return vector;
    }


    // CSV Reader and return a entire vector column in doubles
    public static double[] read(String dir, int column) {

        CSVReader reader;
        int size = -1;
        double[] x = null;
        int i = 0;
        Boolean first = true;

        try{
            reader = new CSVReader(new FileReader(dir));
            String[] line;
            LineNumberReader lnr = new LineNumberReader(new FileReader(dir));
            while (lnr.readLine() != null) {
                size++;
            }
            lnr.close();
            x = new double[size];
            while ((line = reader.readNext()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                x[i] = Float.valueOf(line[column]);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return x;

    }

    public void vectorCSV () {

        String FILE_STORE_DIR = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath() + "/Fran/sensor";
        String fileAcc = FILE_STORE_DIR + "/ACCELEROMETER.csv";
        String fileMag = FILE_STORE_DIR + "/MAGNETICFIELD.csv";
        double[] ax, ay, az, mx, my, mz, vector;

        ax = read(fileAcc, 1);
        ay = read(fileAcc, 2);
        az = read(fileAcc, 3);

        mx = read(fileMag, 1);
        my = read(fileMag, 2);
        mz = read(fileMag, 3);

        Log.d("VECTOR", "Starting...");
        vector = featureVector(ax, ay, az, mx, my, mz);
        Log.d("VECTOR", "Feature vector created...");

        String filename = "VECTOR" + ".csv";
        CSVFile csv = new CSVFile(FILE_STORE_DIR, filename);
        csv.open();
        for (int i = 0; i < vector.length; i++) {
            csv.writeLine(String.valueOf(vector[i]));
        }
        csv.close();

    }


}
