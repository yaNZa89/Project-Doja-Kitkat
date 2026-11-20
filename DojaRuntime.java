package id.doja.kitkat.emulator.doja;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Runtime DoJa (com.nttdocomo.ui) ringan yang dioptimalkan untuk Android 4.4 KitKat (API 19).
 */
public class DojaRuntime {

    public static final int KEY_0 = 0x00;
    public static final int KEY_1 = 0x01;
    public static final int KEY_2 = 0x02;
    public static final int KEY_3 = 0x03;
    public static final int KEY_4 = 0x04;
    public static final int KEY_5 = 0x05;
    public static final int KEY_6 = 0x06;
    public static final int KEY_7 = 0x07;
    public static final int KEY_8 = 0x08;
    public static final int KEY_9 = 0x09;
    public static final int KEY_ASTERISK = 0x0A;
    public static final int KEY_POUND = 0x0B;
    public static final int KEY_UP = 0x10;
    public static final int KEY_LEFT = 0x11;
    public static final int KEY_RIGHT = 0x12;
    public static final int KEY_DOWN = 0x13;
    public static final int KEY_SELECT = 0x14;
    public static final int KEY_SOFT1 = 0x15;
    public static final int KEY_SOFT2 = 0x16;
    public static final int KEY_CLEAR = 0x19;

    private final Context context;
    private final int lcdWidth;
    private final int lcdHeight;
    private final byte[] scratchPad;
    private final Bitmap backBuffer;
    private final Canvas lcdCanvas;
    private final Paint paint = new Paint();

    private Runnable repaintCallback;
    private volatile boolean running = false;
    private Thread loopThread;

    private int playerX;
    private int playerY;
    private int lastKeyCode = -1;
    private int score = 0;
    private String activeLabel = "DOJA KITKAT VM";
    private int loadedClassCount = 0;

    public DojaRuntime(Context context, int width, int height, int spMaxBytes) {
        this.context = context;
        this.lcdWidth = width;
        this.lcdHeight = height;
        this.scratchPad = new byte[Math.max(256, spMaxBytes)];
        this.backBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        this.lcdCanvas = new Canvas(this.backBuffer);
        this.playerX = width / 2;
        this.playerY = height / 2;
        loadScratchPad();
    }

    public void setRepaintCallback(Runnable cb) {
        this.repaintCallback = cb;
    }

    public void startDemoApp() {
        running = true;
        loopThread = new Thread(() -> {
            while (running) {
                if (repaintCallback != null) {
                    repaintCallback.run();
                }
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        loopThread.start();
    }

    public void resetDemo() {
        playerX = lcdWidth / 2;
        playerY = lcdHeight / 2;
        score = 0;
        lastKeyCode = -1;
    }

    public void applyJamDescriptor(JamDescriptor jam) {
        this.activeLabel = jam.appName;
        if (repaintCallback != null) repaintCallback.run();
    }

    public String inspectJarArchive(byte[] jarBytes) {
        List<String> classes = new ArrayList<String>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(jarBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    classes.add(entry.getName());
                }
            }
        } catch (Exception e) {
            return "Gagal membaca JAR: " + e.getMessage();
        }
        this.loadedClassCount = classes.size();
        this.activeLabel = "JAR (" + loadedClassCount + " class)";
        return "Terdeteksi " + loadedClassCount + " berkas .class DoJa";
    }

    public void dispatchKeyPressed(int keyCode) {
        this.lastKeyCode = keyCode;
        int step = 10;
        if (keyCode == KEY_UP || keyCode == KEY_2) playerY = Math.max(36, playerY - step);
        else if (keyCode == KEY_DOWN || keyCode == KEY_8) playerY = Math.min(lcdHeight - 16, playerY + step);
        else if (keyCode == KEY_LEFT || keyCode == KEY_4) playerX = Math.max(12, playerX - step);
        else if (keyCode == KEY_RIGHT || keyCode == KEY_6) playerX = Math.min(lcdWidth - 12, playerX + step);
        else if (keyCode == KEY_SELECT || keyCode == KEY_5) {
            score += 10;
            playBeep(660, 45);
            saveScratchPadInt(0, score);
        }
        if (repaintCallback != null) repaintCallback.run();
    }

    public void dispatchKeyReleased(int keyCode) {
        // Hook untuk Canvas.processEvent KEY_RELEASED_EVENT
    }

    public Bitmap renderFrame() {
        lcdCanvas.drawColor(Color.rgb(6, 11, 22));

        paint.setColor(Color.rgb(15, 23, 42));
        lcdCanvas.drawRect(0, 0, lcdWidth, 24, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(11f);
        lcdCanvas.drawText(activeLabel, 8, 16, paint);

        paint.setColor(Color.rgb(56, 189, 248));
        lcdCanvas.drawCircle(playerX, playerY, 8f, paint);

        paint.setColor(Color.rgb(148, 163, 184));
        paint.setTextSize(10f);
        lcdCanvas.drawText("Skor ScratchPad: " + score, 8, lcdHeight - 24, paint);
        lcdCanvas.drawText("KeyCode: " + (lastKeyCode >= 0 ? "0x" + Integer.toHexString(lastKeyCode) : "-"), 8, lcdHeight - 10, paint);

        return backBuffer;
    }

    private void loadScratchPad() {
        SharedPreferences sp = context.getSharedPreferences("doja_scratchpad", Context.MODE_PRIVATE);
        String encoded = sp.getString("sp_data", null);
        if (encoded != null) {
            byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
            System.arraycopy(decoded, 0, scratchPad, 0, Math.min(decoded.length, scratchPad.length));
            score = ((scratchPad[0] & 0xFF) << 8) | (scratchPad[1] & 0xFF);
        }
    }

    private void saveScratchPadInt(int offset, int val) {
        scratchPad[offset] = (byte) ((val >> 8) & 0xFF);
        scratchPad[offset + 1] = (byte) (val & 0xFF);
        SharedPreferences sp = context.getSharedPreferences("doja_scratchpad", Context.MODE_PRIVATE);
        sp.edit().putString("sp_data", Base64.encodeToString(scratchPad, Base64.DEFAULT)).apply();
    }

    private void playBeep(final int freqHz, final int durationMs) {
        new Thread(() -> {
            int sampleRate = 8000;
            int numSamples = (durationMs * sampleRate) / 1000;
            short[] buffer = new short[numSamples];
            for (int i = 0; i < numSamples; i++) {
                buffer[i] = (short) (Math.sin(2 * Math.PI * i * freqHz / sampleRate) * 12000);
            }
            AudioTrack track = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    numSamples * 2,
                    AudioTrack.MODE_STATIC
            );
            track.write(buffer, 0, numSamples);
            track.play();
        }).start();
    }

    public void shutdown() {
        running = false;
        if (loopThread != null) {
            loopThread.interrupt();
        }
    }
}
