package dam.romsanbryan.paint;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Clase que carga las preferencias
 *
 * Created by romsanbryan on 8/03/18.
 */

public class SettingsActivity extends PreferenceActivity {

    /**
     * Creamos la vista y la configuramos
     *
     * @param savedInstanceState
     * @return Devolvemos la vista
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias); // Cargamos las preferencias a la vista

    }
}
