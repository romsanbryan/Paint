package dam.romsanbryan.paint;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Clase que carga las preferencias
 *
 * @author romsanbryan
 * @see PreferenceActivity
 * @since 1.02 on 2018-03-08
 */

public class SettingsActivity extends PreferenceActivity {

    /**
     * Creamos la vista y la configuramos
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias); // Cargamos las preferencias a la vista

    }
}
