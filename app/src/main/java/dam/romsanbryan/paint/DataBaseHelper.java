package dam.romsanbryan.paint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Clase operacional de la aplicacion.
 * Contiene los metodos necesarios para crear, annadir y borrar datos
 *
 * @author romsanbryan
 * @see SQLiteOpenHelper
 * @version 1.0.0  2018-03-08
 */

public class DataBaseHelper extends SQLiteOpenHelper {

    // Variables
    public static final String DATABASE_NAME = "Paint.db"; // DataBase Name
    public static final String TABLE_NAME = "recents"; // Table Name
    public static final String COL_1 = "ID"; // Colum 1
    public static final String COL_2 = "Name"; // Colum 2
    public static final String COL_3= "URL"; // Colum 3

    /**
     * Constructor utilizado por la clase SQLiteOpenHelper para crear la base de datos
     *
     * @param context Contexto de la aplicacion
     */
    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    /**
     * Crea la base de datos con el nombre de la tabla y los campos que necesitemos.
     *
     * @param db Base de datos
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME+" ("+COL_1+" INTEGER PRIMARY KEY AUTOINCREMENT,"
                +COL_2+" TEXT,"+COL_3+" TEXT)");
    }

    /**
     * Si la base de datos ya existe la borra y crea una nueva
     *
     * @param db Base de datos
     * @param i Version Anterior
     * @param i1 Version nueva
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    /**
     * Insertar datos en la tabla mediante un contenedor de valores (ContentView)
     *
     * @param name Nombre del archivo
     * @param paht Ruta del archivo
     * @return True si los datos se introduciron correctamente
     */
    public boolean insertData(String name, String paht){
        SQLiteDatabase db = this.getWritableDatabase(); // Activamos que podemos escribir en la base de datos
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, paht);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    /**
     * Recorre la base de datos  con sentencia SQL
     *
     * @return Datos de la base de datos
     */
    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM "+TABLE_NAME, null);
    }

}