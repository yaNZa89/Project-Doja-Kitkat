package id.doja.kitkat.emulator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import id.doja.kitkat.emulator.doja.DojaCanvasView;
import id.doja.kitkat.emulator.doja.DojaRuntime;
import id.doja.kitkat.emulator.doja.JamDescriptor;
import id.doja.kitkat.emulator.ui.KeitaiKeypadView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {

    private static final int REQ_PICK_DOJA_FILE = 1901;

    private DojaCanvasView canvasView;
    private DojaRuntime runtime;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = (TextView) findViewById(R.id.txt_status);
        canvasView = (DojaCanvasView) findViewById(R.id.doja_canvas);
        KeitaiKeypadView keypad = (KeitaiKeypadView) findViewById(R.id.keitai_keypad);
        Button btnLoad = (Button) findViewById(R.id.btn_load_jar);
        Button btnReset = (Button) findViewById(R.id.btn_reset);

        runtime = new DojaRuntime(this, 240, 240, 256 * 1024);
        canvasView.attachRuntime(runtime);

        keypad.setKeyListener(new KeitaiKeypadView.OnDojaKeyListener() {
            @Override
            public void onDojaKeyDown(int keyCode) {
                runtime.dispatchKeyPressed(keyCode);
            }

            @Override
            public void onDojaKeyUp(int keyCode) {
                runtime.dispatchKeyReleased(keyCode);
            }
        });

        btnLoad.setOnClickListener(v -> openFileChooserKitKat());
        btnReset.setOnClickListener(v -> {
            runtime.resetDemo();
            txtStatus.setText("Demo DoJa direset (" + 240 + "x" + 240 + ")");
        });

        runtime.startDemoApp();
    }

    private void openFileChooserKitKat() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Pilih file .jam atau .jar"), REQ_PICK_DOJA_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_DOJA_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                loadSelectedUri(uri);
            }
        }
    }

    private void loadSelectedUri(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            is.close();

            byte[] raw = bos.toByteArray();
            String path = uri.toString().toLowerCase();
            if (path.endsWith(".jam")) {
                JamDescriptor jam = JamDescriptor.parse(raw);
                txtStatus.setText("JAM: " + jam.appName + " (" + jam.drawArea + ")");
                runtime.applyJamDescriptor(jam);
            } else {
                String summary = runtime.inspectJarArchive(raw);
                txtStatus.setText(summary);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Gagal membaca berkas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (runtime != null) {
            runtime.shutdown();
        }
    }
}
