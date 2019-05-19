//ajout des bibliothèques du PN532 en mode SPI
  #include <SPI.h>
  #include <PN532_SPI.h>
  #include "PN532.h"

//création d'un objet de communication nfc en base SPI
  PN532_SPI pn532spi(SPI, 10);
  PN532 nfc(pn532spi);

//déclaration des variables
  boolean isLock = false;
  boolean isEmpty = true;
  String password = "";
  const String passwordCompare = "bac987654";
  const byte lockPinRly = 3;
  const byte btnPin = 4;
  const byte cptDistTrig = 5;
  const byte cptDistEcho = 6;
  const int boxLenght = 30; //cm
  float distance_mm;
  float distance_cm;

//temps maximal en micro seconde d'attente de retour de signal pour le capteur de distance
  const unsigned long MEASURE_TIMEOUT  = 25000; 

//Vitesse du son en mm/s
  const float SOUND_SPEED = 340.0/1000;

void setup() {
  Serial.begin(115200);
  while(!Serial){;}

  pinMode(lockPinRly, OUTPUT);
  pinMode(btnPin, INPUT);
  pinMode(cptDistTrig,OUTPUT);
  digitalWrite(cptDistTrig, LOW);
  pinMode(cptDistEcho, INPUT);

  
  Serial.println("----TFE Boite à Colis----");

  nfc.begin();

  //récupèrer la version du firmware installé sur la carte pn532
  uint32_t versiondata = nfc.getFirmwareVersion();

  //si la version ne renvoie rien => on ne trouve pas la carte pn532
  if (! versiondata) {
      Serial.print("Aucune carte PN532 n'as été trouvée");
      while (1); //on arrête le script
    }
  //si la carte est trouvée afficher ses informations
  Serial.print("Puce trouvée : PN5"); Serial.println((versiondata>>24) & 0xFF, HEX); 
  Serial.print("Firmware ver. "); Serial.print((versiondata>>16) & 0xFF, DEC); 
  Serial.print('.'); Serial.println((versiondata>>8) & 0xFF, DEC);

  //configuration de la carte pour pouvoir lire des cartes
  nfc.SAMConfig();
}

void loop() {
  //-------------------PARTIE RECUPERATION MDP NFC-----------------------
  //boolean succès réception/envoie message
  boolean success;

  //Longeur du message
  uint8_t responseLength = 32;

  //si une carte est détéctée, renvoie un true
  success = nfc.inListPassiveTarget();

  if(success){
    Serial.println("Quelque chose à été trouvé !");
    uint8_t selectAid[] = { 0x00,
                             0xA4,
                             0x04,
                             0x00,
                             0x07, //logueur de l'AID
                             0xF0, 0x54, 0x46, 0x45, 0x43, 0x31, 0x39, //AID (F0 + TFE319 en hex)
                             0x00 };
   uint8_t response[32];

   success = nfc.inDataExchange(selectAid, sizeof(selectAid), response, &responseLength);

   if(success){

  
    
    for (uint8_t i = 0; i < responseLength; i++) {
        char c = response[i];
        if (c <= 0x1f || c > 0x7f) {
            Serial.print('.');
        } else {
            Serial.print(c);
            password += c ;
        }
    }
    Serial.println("");
    }
    }
//-----------------------------------PARTIE CAPTEUR DE PROXIMITE---------------------------------------------

    //on envoie une pulse de 10 microseconde au capteur 
    digitalWrite(cptDistTrig, HIGH);
    delayMicroseconds(10);
    digitalWrite(cptDistTrig, LOW);

    //mesure du temps entre l'envoie et la réception de la pulse (max définit par timeout
    long measure = pulseIn(cptDistEcho, HIGH, MEASURE_TIMEOUT); 

    //calcul de la longeur en mm (/2 car aller et retour)
    distance_mm = measure / 2.0 * SOUND_SPEED;

    //convertion en cm
    distance_cm = distance_mm / 10.0;

//------------------------------------DETECTION COLIS--------------------------------------------------------

    //si distance est inférieur à la longeur de la boite => présence colis
    if(distance_cm < boxLenght){
      isEmpty = false;
      }
    else{
      isEmpty = true;
      }

//-----------------------------------STATUS DU VERROU---------------------------------------------------------

    //si isEmpty est false et appui boutton=> verrouillage
    if(!isEmpty && digitalRead(btnPin) == HIGH){
        isLock = true;
      }

    //si mot depasse correct => déverouillage et remise string password à ""
    if(password.equals(passwordCompare)){
        isLock = false;
        password = "";
      }
//------------------------------------VERROUILLAGE------------------------------------------------------------


    //verouillage si islock = true
    if(isLock){
      digitalWrite(lockPinRly,HIGH);
      }
    else{
      digitalWrite(lockPinRly,LOW);
      }

//attente de 0.5 second pour éviter une surcharge
    delay(500);

}
