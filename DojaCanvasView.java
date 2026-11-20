package id.doja.kitkat.emulator.doja;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class DojaCanvasView extends View {

    private DojaRuntime runtime;
    private final Paint blitPaint = new Paint();
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    public DojaCanvasView(Context context) {
        super(context);
        init();
    }

    public DojaCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        blitPaint.setFilterBitmap(false); // Ketajaman piksel LCD FOMA asli
        blitPaint.setAntiAlias(false);
    }

    public void attachRuntime(DojaRuntime rt) {
        this.runtime = rt;
        this.runtime.setRepaintCallback(this::postInvalidate);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (runtime == null) return;
        Bitmap frame = runtime.renderFrame();
        if (frame == null) return;

        int vw = getWidth();
        int vh = getHeight();
        int bw = frame.getWidth();
        int bh = frame.getHeight();

        float scale = Math.min((float) vw / bw, (float) vh / bh);
        int drawW = Math.round(bw * scale);
        int drawH = Math.round(bh * scale);
        int left = (vw - drawW) / 2;
        int top = (vh - drawH) / 2;

        srcRect.set(0, 0, bw, bh);
        dstRect.set(left, top, left + drawW, top + drawH);
        canvas.drawBitmap(frame, srcRect, dstRect, blitPaint);
    }
}
