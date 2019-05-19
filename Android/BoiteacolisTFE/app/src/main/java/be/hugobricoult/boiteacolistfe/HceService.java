package be.hugobricoult.boiteacolistfe;

import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by hugo on 3/05/2019.
 */

public class HceService extends HostApduService{

    //SELECT AID = {0x00, (byte) 0xA4, 0x04,0x00}
    //taille de l'AID = 0x07
    //AID est un identifiant en hexadecimal commencant par la clef 0x0F, ici FRUGAL
    //AID = {0xF0, 0x54, 0x46, 0x45, 0x33, 0x31, 0x39}.

    private static final byte[] SELECT_AID = {0x00, (byte) 0xA4, 0x04,0x00,0x07,(byte) 0xF0, 0x54, 0x46, 0x45, 0x33, 0x31, 0x39, 0x00};
    private static byte[] MY_UID = {0x31, 0x32, 0x33, 0x34};
    private static final byte[] MY_ERROR = {0x65, 0x72, 0x72, 0x65, 0x75, 0x72};

    private static final String pref = "PREFS_BOITE_A_COLIS";
    private static final String password = "BOITE_A_COLIS_TFE_PASSWORD";

    private String mdpStr = "";
    SharedPreferences sharedPreferences;

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {

        //si le message reçu est égale a ce que nous attendons :
        if (Arrays.equals(SELECT_AID, apdu)) {

            //récupèrer le répertoir de paramètres.
            sharedPreferences = getBaseContext().getSharedPreferences(pref,MODE_PRIVATE);

            //récupèrer le mot de passe (si il  n'existe pas prendre la valeur 1234)
            mdpStr = sharedPreferences.getString(password,"1234");

            //convertire le mot de passe en tableau de byte
            MY_UID = hexStringToByteArray(toHex(mdpStr));

            //retourne notre mot de passe
            return MY_UID;
        }
        else {
            //sinon renvoie de l'erreur
            return MY_ERROR;
        }

    }

    @Override
    public void onDeactivated(int i) {

    }
    //fonction pour convertir un string hexadecimal en tableau de Byte
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    //fonction pour convertir un string en hexadecimal
    public String toHex(String arg)  {
        return String.format("%02x", new BigInteger(1, arg.getBytes(Charset.defaultCharset())));
    }
}

