package dam.romsanbryan.paint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by romsanbryan on 5/3/18.
 */
@SuppressLint("AppCompatCustomView")
public class LienzoDibujo extends SurfaceView implements SurfaceHolder.Callback {

    //Propiedades recomendadas
    //  Hilo de dibujo
    private HiloDibujo hiloDibujo;
    //  Mapa de bits
    private static Bitmap bitMap;
    //  Fichero del mapa de bits
    private String bitMapFile="";
    //  Posición contacto en X
    private float touched_x;
    //  Posicion contacto en y
    private float touched_y;
    // Dibuja ruta con el dedo
    private Path drawPath;


    public LienzoDibujo(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!bitMapFile.isEmpty()) {
            bitMap = BitmapFactory.decodeFile(bitMapFile);

        }

        //suscribir la instancia de la clase al callback del holder
        getHolder().addCallback(this);
        // trazos para el dibujo
        drawPath = new Path();
        // hilo para dibujar
        hiloDibujo = new HiloDibujo(getHolder(), EditorActivity.c);
        hiloDibujo.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // Comienza la ejecución del thread

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int width, int height) {
        // Crea un bitmap con las dimensiones del view
        //bitMap = Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
       this.finalizarHiloDibujo();
    }
    private void finalizarHiloDibujo(){
        boolean retry = true;
        while (retry) {
            try {
                hiloDibujo.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touched_x = event.getX();
        touched_y = event.getY();

        int action = event.getAction();
        switch(action){
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touched_x, touched_y);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touched_x, touched_y);
                hiloDibujo = new HiloDibujo(getHolder(), EditorActivity.c);
                hiloDibujo.start();
                break;
            default:
                return false;
        }
        return true;
    }

    class HiloDibujo extends Thread{
        // soporte de la superficie de dibujo
        private SurfaceHolder holder;
        // lienzo, la superficie de dibujo
        private Canvas canvas;
        // brocha para pintar
        private Paint drawPaint = new Paint();;
        // Color para pintar
        private int color;

        public HiloDibujo(SurfaceHolder holder, int c){
            this.holder = holder;
            this.color = c;
        }

        @Override
        public void run() {

            boolean retry=true;

            if (holder.getSurface().isValid()) {
                try {
                    canvas = holder.lockCanvas(null);

                    canvas.drawBitmap(Bitmap.createBitmap (LienzoDibujo.this.getWidth()
                            , LienzoDibujo.this.getHeight(), Bitmap.Config.ARGB_8888), 0,0,null);
                    drawPaint.setColor(color);
                    drawPaint.setAntiAlias(true);
                    drawPaint.setStrokeWidth(EditorActivity.tam);
                    drawPaint.setStyle(Paint.Style.STROKE);
                    drawPaint.setStrokeJoin(Paint.Join.ROUND);
                    drawPaint.setStrokeCap(Paint.Cap.ROUND);
                    canvas.drawPath(drawPath, drawPaint);
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
