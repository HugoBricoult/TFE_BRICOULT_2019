package be.hugobricoult.boiteacolistfe;


import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //déclaration variables adresse d'enregistrements paramètres
    private static final String prefs = "PREFS_BOITE_A_COLIS";
    private static final String pswd = "BOITE_A_COLIS_TFE_PASSWORD";

    //Objet de paramètre interne au système d'exploitation
    SharedPreferences sharedPreferences;

    //mot de passe de base (par défaut)
    private String basePswd = "1234";

    //Objets de la vue principale
    //champs mot de passe
    EditText mdp;
    //bouton sauvegarder
    Button btn;
    //texte de status
    TextView stats;


    @Override //A la creation de la vue
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //charger les élements de la vue en le trouvant grace à leurs ID
        mdp = (EditText) findViewById(R.id.BACmdp);
        stats = (TextView) findViewById(R.id.statusMdp);
        btn = (Button) findViewById(R.id.btnSave);

        //sélectionner les paramètres
        sharedPreferences = getBaseContext().getSharedPreferences(prefs,MODE_PRIVATE);

        //si les paramètre contienne l'option pswd, changer le contenu du edittext
        if(sharedPreferences.contains(pswd)){
            basePswd = sharedPreferences.getString(pswd,"1234");
            mdp.setText(basePswd);
        }
        //sinon créer l'option avec la valeur de basepswd
        else{
            sharedPreferences.edit().putString(pswd,basePswd).apply();
        }

        //ajouter une action au lick sur le bouton
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //changer le mot de passe des paramètre par celui écrit dans l'edittext
                sharedPreferences.edit().putString(pswd,mdp.getText().toString()).apply();
                stats.setText("Mot de passe enregistré : "+mdp.getText().toString());

            }
        });
    }
}
