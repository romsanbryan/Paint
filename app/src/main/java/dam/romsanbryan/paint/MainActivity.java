package dam.romsanbryan.paint;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Aplicación de un Paint simple para dispositivos Android
 *
 * @author romsanbryan
 * @see AppCompatActivity
 * @see View.OnClickListener
 * @since 1.0.5  2018-03-09
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Metodos
        // Privados
    private CanvasSufaceView canvas; // Objeto de la clase LienzoDibujo
    private DataBaseHelper myDB; // Objeto de la base de datos
    private Button bt_verde, bt_rojo, bt_ama, bt_azul, bt_mor, bt_ne, bt_bor; // Botones de colores
    private Button bt_mas, bt_menos; // Botones de tamaño del pincel
    private Button  bt_new, bt_pref, bt_reciente, bt_save, bt_foto, bt_open; // Otros botones
    private String mCurrentPhotoPath; // Variable para guardar la ruta de la takePhoto realizada
    private SharedPreferences preferences; // Objeto de la clase de Preferencias
    private int c; // Variable para el color del pincel
    private int tam; // Variable para el tamaño del pincel
    private Uri uri = null; // Rutas URI

    // Variables staticas y finales
    public static final int ACTION_TAKE_PHOTO_B = 1;
    public static final String PNG_FILE_SUFFIX = ".png";
    private static final int READ_REQUEST_CODE = 42;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        canvas = new CanvasSufaceView(this); // Declaracion del objeto de canvas
        myDB = new DataBaseHelper(this); // Declaraicon del objeto de base de datos
        preferences = PreferenceManager.getDefaultSharedPreferences(this); // Declaramos el objeto de las preferencias


        // Deficiones de los botones de colores (y goma) y activamos acciones
        bt_verde = findViewById(R.id.bt_color1); // Definicion
        bt_verde.setOnClickListener(this); // Accion
        bt_ama = findViewById(R.id.bt_color2); // Definicion
        bt_ama.setOnClickListener(this); // Accion
        bt_rojo = findViewById(R.id.bt_color3); // Definicion
        bt_rojo.setOnClickListener(this); // Accion
        bt_azul = findViewById(R.id.bt_color4); // Definicion
        bt_azul.setOnClickListener(this); // Accion
        bt_mor = findViewById(R.id.bt_color5); // Definicion
        bt_mor.setOnClickListener(this); // Accion
        bt_ne = findViewById(R.id.bt_color6); // Definicion
        bt_ne.setOnClickListener(this); // Accion



        bt_bor = findViewById(R.id.goma); // Definicion
        bt_bor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c = Color.WHITE;
            }
        }); // Accion

        bt_mas = findViewById(R.id.mas); // Definicion
        bt_mas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tam = Integer.parseInt(preferences.getString("tamaño", ""))+5; // Accion: Aumentar a 5 el tamaño del lapiz
                preferences.edit().putString("tamaño", String.valueOf(tam)).commit(); // Actualizamos preferencias

            }
        });
        bt_menos = findViewById(R.id.menos); // Definicion
        bt_menos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tam = Integer.parseInt(preferences.getString("tamaño", ""))-5; // Accion: Disminuimos a 5 el tamaño del lapiz
                preferences.edit().putString("tamaño", String.valueOf(tam)).commit(); // Actualizamos preferencias
            }
        });

        bt_new = findViewById(R.id.bt_new); // Definicion
        bt_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Limpiamos Canvas
            }
        });

        bt_pref = findViewById(R.id.pref); // Definicion
        bt_pref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), SettingsActivity.class);
                startActivity(i);
            }
        });

        bt_save = findViewById(R.id.bt_save); // Definicion
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // Guardamos el bitmap en la base de datos
                // Generamos nombre
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String folderName = "IMG_" + timeStamp+PNG_FILE_SUFFIX;

                canvas.saveBitmap(folderName); // Llamamos al metodo de guardar

                // Añadimos a la base de datos
                boolean  isInserted = myDB.insertData(folderName, canvas.outPath+"/"+folderName);
                if (isInserted == true)
                    Toast.makeText(MainActivity.this, "Data Inserted", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(MainActivity.this, "Data not Inserted", Toast.LENGTH_LONG).show();
            }
        });

        bt_reciente = findViewById(R.id.bt_reciente); // Definicion
        bt_reciente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // Consultamos la base de datos
                Cursor res = myDB.getAllData();
                if (res.getCount() == 0){ // Si no tiene filas, cargamos mensaje de error y salimos
                    showMessage("Error", "Nothing found");
                    return;
                }
                // Si tiene cargamo un buffer con los datos y lo cargamos
                StringBuffer buffer = new StringBuffer();
                while (res.moveToNext()){
                    buffer.append("Name: "+res.getString(1)+"\n");
                    buffer.append("Ruta: "+res.getString(2)+"\n\n");

                }
                showMessage("Recientes", buffer.toString());
            }
        });

        bt_foto = findViewById(R.id.bt_camara);
        bt_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        bt_open = findViewById(R.id.bt_open);
        bt_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFileSearch();
            }
        });
    }

    /**
     * Hacemos foto, la guardamos y ponemos en el camvas
     */
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;
        try {
            f = createImageFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (Exception e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }
        startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO_B);
    }

    /**
     * Abre un intent para seleccionar el fichero de imagen
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT intent para seleccionar el fichero desde
        // el explorador de ficheros del sistema
        //Cuando la aplicación envía la intent ACTION_OPEN_DOCUMENT,
        //lanza un selector que muestra todos los proveedores de documentos coincidentes.
        Intent intent = new Intent("android.intent.action.GET_CONTENT");//new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Al agregar la categoría CATEGORY_OPENABLE a la intent filtra
        // los resultados para mostrar solo documentos que se pueden abrir, como archivos de imagen
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // sólo nos interesan las imágenes
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    /**
     * Creamos el nombre de la foto realizada
     * @return Nombre de la foto
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp;
        File cameraImage = getImageDir();
        File imageF = File.createTempFile(imageFileName, PNG_FILE_SUFFIX, cameraImage);
        return imageF;
    }

    /**
     * Creamos el directorio de la foto
     * @return Ruta de la ubicacion de la foto
     */
    private File getImageDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir =  new File (canvas.outPath + "/paint");
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("Camera Sample", "Error al crear el directorio");
                        return null;
                    }
                }
            }
        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }

    /**
     * Seleccionamos foto para el Canvas
     */
    private void setPictureToCanvas() {

		// Establecemos alto y ancho
        int targetW = canvas.getWidth();
        int targetH = canvas.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Realizamos el escalado de la foto
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
            if(scaleFactor % 2 != 0) scaleFactor++;
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		// Decodificamos el bitMap
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		// Asociamos bitmap al canvas
        canvas.setBitmap(bitmap);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode){
                case READ_REQUEST_CODE:
                    // The document selected by the user won't be returned in the intent.
                    // Instead, a URI to that document will be contained in the return intent
                    // provided to this method as a parameter.
                    // Pull that URI using resultData.getData().
                    if (resultData != null) {
                        uri = resultData.getData();

                        Log.i("SimplyDrawing", "Uri: " + uri.toString());
                        try {
                            InputStream file = getContentResolver().openInputStream(uri);
                            canvas.setBitmap(file,uri.getPath(),getExternalFilesDir("images"));
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Mensaje de AlertDialog para mostrar datos de la consulta
     * @param title Titulo de la ventana, si no hay datos nos mostrara "error" y si los hay "data"
     * @param message Mensaje de la ventana, muestra los datos de la base de datos si hubiera
     */
    private void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    /**
     * Acciones de botones de colores
     * Seleccionamos un color determinado según que color pulsemo
     *
     * @param view Vista de la aplicacion
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_color1:
                c = Color.GREEN;
                break;
            case R.id.bt_color2:
                c = Color.YELLOW;
                break;
            case R.id.bt_color3:
                c = Color.RED;
                break;
            case R.id.bt_color4:
                c = Color.BLUE;
                break;
            case R.id.bt_color5:
                c = Color.MAGENTA;
                break;
            case R.id.bt_color6:
                c = Color.BLACK;
                break;
            default:
                break;
        }
        preferences.edit().putString("color", String.valueOf(c)).commit(); // Actualizamos preferencias
    }
}