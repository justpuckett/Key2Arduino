#define relay1 7
#define relay2 6
#define relay3 5
#define relay4 4
int led = 13;
int val = LOW;
const byte numChars = 32;
char receivedChars[numChars];
boolean newData = false;
boolean writing = false;

// receivedChars command: T/F pin# : Ex: T5  F4

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(led, OUTPUT);
  pinMode(relay1,OUTPUT);
  pinMode(relay2,OUTPUT);
  pinMode(relay3,OUTPUT);
  pinMode(relay4,OUTPUT);
  digitalWrite(7,LOW);
  digitalWrite(6,LOW);
  digitalWrite(5,LOW);
  digitalWrite(4,LOW);
}

void loop() {
    recvWithStartEndMarkers();
    processNewData();
}

void recvWithStartEndMarkers() {
    static boolean recvInProgress = false;
    static byte ndx = 0;
    char startMarker = '<';
    char endMarker = '>';
    char rc;
 
 // if (Serial.available() > 0) {
    while (Serial.available() > 0 && newData == false) {
        rc = Serial.read();

        if (recvInProgress == true) {
            if (rc != endMarker) {
                receivedChars[ndx] = rc;
                ndx++;
                if (ndx >= numChars) {
                    ndx = numChars - 1;
                }
            }
            else {
                receivedChars[ndx] = '\0'; // terminate the string
                recvInProgress = false;
                ndx = 0;
                newData = true;
            }
        }

        else if (rc == startMarker) {
            recvInProgress = true;
        }
    }
    //Serial.write(".");
}

void processNewData() {
    if (newData == true) {
       if (receivedChars[0] == "?") {
         Serial.print("<!>");
         return;
       } else if (receivedChars[0] == "!") {
         return;
       }
      
        //Serial.println(receivedChars);
        int pin = 0;
        if (isdigit(receivedChars[1])) {
          pin = receivedChars[1] - '0';
        }
        int pinVal = LOW;
        if (receivedChars[0] == 't' || receivedChars[0] == 'T') {
          pinVal = HIGH;
        } else if (receivedChars[0] == 'f' || receivedChars[0] == 'F'){
          pinVal = LOW;
        } 
        //Serial.print("<ACK ");
        //Serial.print(pin);
        //Serial.print(receivedChars[2]);
        //Serial.println(">");
        if (pin > 0 && pin < 10) {
          digitalWrite(pin,pinVal);
        }
        newData = false;
    } 
}
