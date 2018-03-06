package dam.romsanbryan.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by rafa on 4/3/18.
 */

public class LienzoMapaBits extends SurfaceView implements SurfaceHolder.Callback{
    private Context context;
    private Bitmap bitMap;
    private HiloImagen hiloImagen;
    private SurfaceHolder holder;
    private File outPath;
    private String sourceFileName;

    public LienzoMapaBits(Context context, AttributeSet attrs) {
        super(context, attrs);
        // hilo para dibujar
        holder = getHolder();
        setDrawingCacheEnabled(true);
        hiloImagen = new HiloImagen(holder);
    }

    public  void setBitmap(InputStream file, String fileName, File path){
        try {
            this.outPath = path;

            this.sourceFileName = fileName;

            this.bitMap = BitmapFactory.decodeStream(file);
            file.close();

            if(this.bitMap!=null) {
                this.bitMap.setWidth(getWidth());
                this.bitMap.setHeight(getHeight());
                hiloImagen.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveBitmap(){
        try {
            FileOutputStream outFile = new FileOutputStream(new File(outPath,"copia1.png"));

            bitMap.compress(Bitmap.CompressFormat.PNG,100,
                    outFile);

            outFile.flush();
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        this.finalizarHiloImagen();
    }

    private void finalizarHiloImagen(){
        boolean retry = true;
        while (retry) {
            try {
                hiloImagen.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class HiloImagen extends Thread{
        // soporte de la superficie de dibujo
        private SurfaceHolder holder;
        // lienzo, la superficie de dibujo
        private Canvas canvas;
        // brocha para pintar
        private Paint drawPaint = new Paint();

        public HiloImagen(SurfaceHolder holder){
            this.holder = holder;
        }

        @Override
        public void run() {
            boolean retry=true;

            if (holder.getSurface().isValid()) {
                try {
                    canvas = holder.lockCanvas(null);
                    canvas.drawBitmap(bitMap, 0, 0, null);
                    drawPaint.setColor(Color.WHITE);
                    drawPaint.setAntiAlias(true);
                    drawPaint.setStrokeWidth(20);
                    drawPaint.setStyle(Paint.Style.STROKE);
                    drawPaint.setStrokeJoin(Paint.Join.ROUND);
                    drawPaint.setStrokeCap(Paint.Cap.ROUND);
                    canvas.drawLine(0,0,100,100,drawPaint);
                    bitMap = getDrawingCache();
                } finally {
                    if (canvas != null)
                        holder.unlockCanvasAndPost(canvas);
                }
            }

            while (retry) {
                try {
                    this.join();
                    retry = false;
                } catch (InterruptedException e) {

                }
            }
        }
    }
}

