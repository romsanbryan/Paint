package dam.romsanbryan.paint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


/**
 * Clase que permite ajustar el lienzo y poder pintar sobre el
 *
 * @author romsanbryan
 * @see SurfaceView
 * @see SurfaceHolder.Callback
 * @since 1.0.5  2018-03-09
 */
@SuppressLint("AppCompatCustomView")
public class CanvasSufaceView extends SurfaceView implements SurfaceHolder.Callback {

    // Variables
        // Privadas
    private DataBaseHelper myDB; // Objeto de la clase de base de datos
    private HiloDibujo hiloDibujo; // Objeto del hilo dibujo
    private String bitMapFile=""; // Ficero del mapa de bits
    private float touched_x; // Eje x de contacto
    private float touched_y; // Eje y de contacto
    private Path drawPath; // Ruta dibujada con el dedo
    private Paint drawPaint; // Ruta pintada
    private Bitmap bitMap; // Mapa de bits
    private SharedPreferences preferences; // Objeto de preferencias
    private File savePhoto;
    private String fileName;
    private File outPath;
    private String sourceFileName;

    // Constantes
    public static final File SAVE_PATH = new File("/sdcard/DCIM/"); // Ruta donde guardaremos las imagenes

    // Constructores

    /**
     * Constructor por defecto, llama al contructor real
     *
     * @param context Contexto de la aplicacion
     */
    public CanvasSufaceView(Context context){
        this(context, null);
    }

    /**
     * Constructor real, hace las asignaciones de los objetos/variables
     *
     * @param context Contexto de la aplicacion
     * @param attrs Atributos
     */
    public CanvasSufaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!bitMapFile.isEmpty()) { // Comprueba que el fichero de bitmap NO esta vacio
            bitMap = BitmapFactory.decodeFile(bitMapFile); // Decodifica el fichero en el bitmap
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext()); // Asignamos el objeto de preferencias
        drawPaint = new Paint(); // Pintada
        getHolder().addCallback(this); //suscribir la instancia de la clase al callback del holder
        drawPath = new Path(); // Trazos del para dibujar
        hiloDibujo = new HiloDibujo(getHolder()); // Hilo de dibujo
    }

    /**
     * Al crear el lienzo de dibujo
     *
     * @param surfaceHolder Lienzo de dibujo
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // Comienza la ejecución del thread
    }

    /**
     * Si el lienzo sufre cambios lo restauramos
     *
     * @param surfaceHolder Lienzo
     * @param i
     * @param width Anchura del lienzo
     * @param height Altura del lienzo
     */
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int width, int height) {
        // Crea un bitmap con las dimensiones del view
//        bitMap = Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);
        if (bitMap == null) {
            bitMap = Bitmap.createBitmap(CanvasSufaceView.this.getWidth(),
                    CanvasSufaceView.this.getHeight(), Bitmap.Config.ARGB_4444);
        }
    }

    /**
     * Si el lienzo es destruido acabamos con todos los hilos
     *
     * @param surfaceHolder Lienzo
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
    }

    /**
     * Acciones segun el movimiento en el lienzo
     * @param event Tipo de movimiento
     * @return Si se realizo una accion devuelve: TRUE, en caso contrario: FALSE
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touched_x = event.getX(); // Eje X
        touched_y = event.getY(); // Eje Y

        int action = event.getAction();
        switch(action){
            case MotionEvent.ACTION_DOWN: // Accion de tocarla pantalla
                drawPath.moveTo(touched_x, touched_y); // Movemos los ejes
                break;
            case MotionEvent.ACTION_MOVE: // Accion de mover
                drawPath.lineTo(touched_x, touched_y); // Hacemos una linea por los ejes
                hiloDibujo = new HiloDibujo(getHolder()); // Hilo de dibujo
                hiloDibujo.start(); // Llamamos al hilo para que pinte
                try {
                    hiloDibujo.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case MotionEvent.ACTION_UP:
                drawPath.reset();
                drawPaint.reset();
                break;
            default:
                return false;
        }
        return true;
    }


    /**
     * Implementamos el bitMap de la foto
     *
     * @param bitMap
     */
    public void setBitmap(Bitmap bitMap){
        if(bitMap!=null) {
            this.bitMap = bitMap;
            if (Build.VERSION.SDK_INT>=19) {
                this.bitMap.setWidth(getWidth());
                this.bitMap.setHeight(getHeight());
            }
            hiloDibujo.start();
            try {
                hiloDibujo.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Modificamos al bitmap
     *
     * @param file Archivo destino
     * @param fileName Nombr del archivo
     * @param path Ruta
     *
     */
    public void setBitmap(InputStream file, String fileName, File path) {
        try {
            outPath = path;

            sourceFileName = fileName;
            bitMap = BitmapFactory.decodeStream(file);
            file.close();
            bitMap.copy(bitMap.getConfig(),true);

            if (bitMap != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    bitMap.setWidth(CanvasSufaceView.this.getWidth());

                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    bitMap.setHeight(CanvasSufaceView.this.getHeight());
                    hiloDibujo.join();

                }

                hiloDibujo.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap devulveBitmap() {
        return bitMap;
    }

    public static Bitmap convertToMutable(Bitmap imgIn, String FileName) {
        try {
            //this is the file going to use temporally to save the bytes.
            // This file will not be a image, it will store the raw image data.
            File file = new File(FileName + File.separator + "temp.tmp");

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // get the width and height of the source bitmap.
            int width = imgIn.getWidth();
            int height = imgIn.getHeight();
            Bitmap.Config type = imgIn.getConfig();

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes() * height);
            imgIn.copyPixelsToBuffer(map);
            //recycle the source bitmap, this will be no longer used.
            imgIn.recycle();
            System.gc();// try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available.
            imgIn = Bitmap.createBitmap(width, height, type);
            map.position(0);
            //load it back from temporary
            imgIn.copyPixelsFromBuffer(map);
            //close the temporary file and channel , then delete that also
            channel.close();
            randomAccessFile.close();

            // delete the temp file
            file.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imgIn;
    }

    public void cl(){

    }

    /**
     * Guardamos imagen del bitMap
     */
    public void saveBitmap(String nombre){
        try {
            FileOutputStream outFile = new FileOutputStream(new File(SAVE_PATH,nombre));

            bitMap.compress(Bitmap.CompressFormat.PNG,100, outFile);

            outFile.flush();
            outFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clase interna
     * Ejecuta un hilo cada vez que pasamos el dedo y este lo pinta
     *
     * @see Thread Hilo
     */
    class HiloDibujo extends Thread{
        // Variables
        private SurfaceHolder holder; // Superficie del dibujo
        private Canvas canvas; // Lienzo del dibujo
        private Paint drawPaint = new Paint(); // Pincel


        // Constructor
        /**
         * Constructor que asigna el lienzo y el color
         * @param holder Lienzo padre
         */
        public HiloDibujo(SurfaceHolder holder){
            this.holder = holder;
        }

        /**
         * Run de la clase, ejecuta la pintada
         */
        @Override
        public void run() {

            boolean retry = true;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (holder.getSurface().isValid()) {


                try {
                    canvas = holder.lockCanvas(null);
                    canvas.drawBitmap(bitMap, 0, 0, null);
                    drawPaint.setColor(Integer.parseInt(preferences.getString("color", "-16777216"))); // Color
                    drawPaint.setAntiAlias(true);
                    drawPaint.setStrokeWidth(Integer.parseInt(preferences.getString("tamaño", "20"))); // Tamaño
                    drawPaint.setStyle(Paint.Style.STROKE);
                    drawPaint.setStrokeJoin(Paint.Join.ROUND);
                    drawPaint.setStrokeCap(Paint.Cap.ROUND);
                    canvas.drawPath(drawPath, drawPaint);



                } finally {
                    if (canvas != null)
                        holder.unlockCanvasAndPost(canvas);
                }
            }

        }
    }
}
