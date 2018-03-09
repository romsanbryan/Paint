package dam.romsanbryan.paint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


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
    private Bitmap bitMap; // Mapa de bits
    private SharedPreferences preferences; // Objeto de preferencias
    private File ruta;
    private String sourceFileName;
        // Variables staticas y final
    public static final File outPath = new File("/sdcard/DCIM/"); // Ruta donde guardaremos las imagenes;

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
        bitMap = Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);
    }

    /**
     * Si el lienzo es destruido acabamos con todos los hilos
     *
     * @param surfaceHolder Lienzo
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
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
     * Implementamos el bitmap de la carga
     * @param file Archivo destino
     * @param fileName Nombr del archivo
     * @param path Ruta
     */
    public  void setBitmap(InputStream file, String fileName, File path){
        try {
            this.ruta = path;

            this.sourceFileName = fileName;

            this.bitMap = BitmapFactory.decodeStream(file);

            if(this.bitMap!=null) {
                if (Build.VERSION.SDK_INT>=19) {

                    this.bitMap.setWidth(getWidth());
                    this.bitMap.setHeight(getHeight());
                }
                hiloDibujo.start();
            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Guardamos imagen del bitMap
     */
    public void saveBitmap(String nombre){
        try {
            FileOutputStream outFile = new FileOutputStream(new File(outPath,nombre));

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

            if (holder.getSurface().isValid()) { // Si el lienzo padre es valido
                try {
                    canvas = holder.lockCanvas(null); // Bloqueamos canvas

                    canvas.drawBitmap(Bitmap.createBitmap (CanvasSufaceView.this.getWidth()
                            , CanvasSufaceView.this.getHeight(), Bitmap.Config.ARGB_8888), 0,0,null); // Cargamos nuestro bitmap
                    // Propiedades del pincel
                    drawPaint.setColor(Integer.parseInt(preferences.getString("color", "-16777216"))); // Color
                    drawPaint.setAntiAlias(true); // Configura Flags
                    drawPaint.setStrokeWidth(Integer.parseInt(preferences.getString("tamaño", "20"))); // Tamaño
                    drawPaint.setStyle(Paint.Style.STROKE); // Estilo
                    drawPaint.setStrokeJoin(Paint.Join.ROUND); // Punta
                    drawPaint.setStrokeCap(Paint.Cap.ROUND); // Punta del acabado

                    canvas.drawPath(drawPath, drawPaint); // Dibujamos en canvas


                } catch(Exception e) { // Recogemos todas las excepciones
                    e.printStackTrace();
                }finally { // Finalmente...
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                        bitMap = getDrawingCache(); // Guardamos el cache en el bitmap
                    }
                }
            }
        }
    }
}
