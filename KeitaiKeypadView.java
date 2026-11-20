package id.doja.kitkat.emulator.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import id.doja.kitkat.emulator.doja.DojaRuntime;

public class KeitaiKeypadView extends LinearLayout {

    public interface OnDojaKeyListener {
        void onDojaKeyDown(int keyCode);
        void onDojaKeyUp(int keyCode);
    }

    private OnDojaKeyListener listener;

    public KeitaiKeypadView(Context context) {
        super(context);
        buildKeypad(context);
    }

    public KeitaiKeypadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        buildKeypad(context);
    }

    public void setKeyListener(OnDojaKeyListener listener) {
        this.listener = listener;
    }

    private void buildKeypad(Context context) {
        setOrientation(VERTICAL);

        // Baris Softkey & Navigasi
        LinearLayout navRow = new LinearLayout(context);
        navRow.setOrientation(HORIZONTAL);
        navRow.addView(makeKeyButton(context, "SOFT1", DojaRuntime.KEY_SOFT1), weightedParams(1f));
        navRow.addView(makeKeyButton(context, "KIRI", DojaRuntime.KEY_LEFT), weightedParams(1f));
        navRow.addView(makeKeyButton(context, "ATAS", DojaRuntime.KEY_UP), weightedParams(1f));
        navRow.addView(makeKeyButton(context, "OK", DojaRuntime.KEY_SELECT), weightedParams(1f));
        navRow.addView(makeKeyButton(context, "BAWAH", DojaRuntime.KEY_DOWN), weightedParams(1f));
        navRow.addView(makeKeyButton(context, "KANAN", DojaRuntime.KEY_RIGHT), weightedParams(1f));
        navRow.addView(makeKeyButton(context, "SOFT2", DojaRuntime.KEY_SOFT2), weightedParams(1f));
        addView(navRow);

        // Grid Keypad Numerik 4x3 FOMA
        GridLayout grid = new GridLayout(context);
        grid.setColumnCount(3);
        grid.setRowCount(4);

        Object[][] keys = {
            {"1", DojaRuntime.KEY_1}, {"2 ABC", DojaRuntime.KEY_2}, {"3 DEF", DojaRuntime.KEY_3},
            {"4 GHI", DojaRuntime.KEY_4}, {"5 JKL", DojaRuntime.KEY_5}, {"6 MNO", DojaRuntime.KEY_6},
            {"7 PQRS", DojaRuntime.KEY_7}, {"8 TUV", DojaRuntime.KEY_8}, {"9 WXYZ", DojaRuntime.KEY_9},
            {"*", DojaRuntime.KEY_ASTERISK}, {"0", DojaRuntime.KEY_0}, {"#", DojaRuntime.KEY_POUND}
        };

        for (Object[] item : keys) {
            GridLayout.LayoutParams gp = new GridLayout.LayoutParams();
            gp.width = 0;
            gp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            grid.addView(makeKeyButton(context, (String) item[0], (Integer) item[1]), gp);
        }
        addView(grid);
    }

    private LayoutParams weightedParams(float weight) {
        return new LayoutParams(0, LayoutParams.WRAP_CONTENT, weight);
    }

    private Button makeKeyButton(Context context, String label, final int keyCode) {
        Button btn = new Button(context);
        btn.setText(label);
        btn.setTextSize(11f);
        btn.setTextColor(Color.parseColor("#F8FAFC"));
        btn.setBackgroundColor(Color.parseColor("#334155"));
        btn.setOnTouchListener((v, event) -> {
            if (listener == null) return false;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                listener.onDojaKeyDown(keyCode);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                listener.onDojaKeyUp(keyCode);
            }
            return false;
        });
        return btn;
    }
}
