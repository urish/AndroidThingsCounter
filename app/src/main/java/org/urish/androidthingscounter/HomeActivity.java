package org.urish.androidthingscounter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends Activity {
    private static final String TAG = "HomeActivity";

    private static final String I2C_BUS_NAME = "I2C2";
    private static final int I2C_DEVICE_ADDRESS = 0x71;
    private static final int CLEAR_SCREEN_COMMAND = 0x76;

    private long counter = 0;
    private I2cDevice sevenSegmentDevice;
    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            sevenSegmentDevice = service.openI2cDevice(I2C_BUS_NAME, I2C_DEVICE_ADDRESS);
        } catch (IOException e) {
            Log.e(TAG, "Failed opening I2C device", e);
        }

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    writeNumber(counter);
                } catch (IOException e) {
                    Log.e(TAG, "Failed writings to I2C device", e);
                }
                counter++;
            }
        }, 2000, 250);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.sevenSegmentDevice != null) {
            try {
                this.sevenSegmentDevice.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    }

    private void writeNumber(long number) throws IOException {
        writeString(String.valueOf(number));
    }

    private void writeString(String s) throws IOException {
        byte[] bytes = s.getBytes();
        sevenSegmentDevice.write(new byte[]{CLEAR_SCREEN_COMMAND}, 1);
        sevenSegmentDevice.write(bytes, bytes.length);
    }
}
