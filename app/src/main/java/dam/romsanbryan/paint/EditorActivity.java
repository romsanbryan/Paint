package dam.romsanbryan.paint;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity {
    private LienzoDibujo lienzoDibujo;
    private Button bt_green, bt_rojo, bt_ama, bt_azul, bt_mor, bt_ne, bt_bor, bt_mas, bt_menos, bt_new;
    public static int c = Color.BLACK;
    public static int tam = 20;
    private static final int READ_REQUEST_CODE = 42;
    private Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);


        bt_rojo = findViewById(R.id.bt_color3);
        bt_rojo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c = Color.RED;
            }
        });

        bt_green = findViewById(R.id.bt_color1);
        bt_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               c = Color.GREEN;
            }
        });
        bt_ama = findViewById(R.id.bt_color2);
        bt_ama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c  = Color.YELLOW;
            }
        });
        bt_azul = findViewById(R.id.bt_color4);
        bt_azul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c  = Color.BLUE;
            }
        });
        bt_mor = findViewById(R.id.bt_color5);
        bt_mor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c  = Color.MAGENTA;
            }
        });
        bt_ne = findViewById(R.id.bt_color6);
        bt_ne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c  = Color.BLACK;
            }
        });
        bt_bor = findViewById(R.id.goma);
        bt_bor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c  = Color.WHITE;
            }
        });
        bt_mas = findViewById(R.id.mas);
        bt_mas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tam+=5;
            }
        });
        bt_menos = findViewById(R.id.menos);
        bt_menos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tam-=5;
            }
        });

        bt_new = findViewById(R.id.bt_new);
        bt_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Para limpiar el lienzo
            }
        });
    }

}
