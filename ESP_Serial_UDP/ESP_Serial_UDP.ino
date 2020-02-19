#include <SoftwareSerial.h>

#include <ESP8266WiFi.h>
#include <WiFiUdp.h>

#ifndef STASSID
#define STASSID "davidhome"
#define STAPSK  "davidhome"
#endif

const char MYAPP[] = "ESP Serial UDP:";
const char MYVERSION[] = "1.1.0.2";
const char chBoard[] = "WeMos D1 R1";
String strRS = String("RS>");
String strTS = String("TS>");
String strRU = String("RU>");
String strTU = String("TU>");

WiFiUDP Udp;
const int icSoftRX = 5;
const int icSoftTX = 4;
SoftwareSerial mySerial(icSoftRX,icSoftTX);
unsigned int uiBoudRate = 19200;

unsigned int serverPort = 1600;   // port to listen
unsigned int clientPort = 2600;   // port to send
IPAddress ipRemote(192,168,17,129);



// buffers for receiving and sending data
char packetBuffer[UDP_TX_PACKET_MAX_SIZE + 1]; //buffer to hold incoming packet,
char  ReplyBuffer[] = "acknowledged\r\n";       // a string to send back
char recvBuffer[2048];
char sendBuffer[2048];

char FirstMsg[] = "On Loop Wait fot instuctions...";
/*
 Commands - 1 - Led Off - E-001 / A001
            2 - Led On  - E-002 / A002
 */
void setup() 
{
  // Serial Setup
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH); // Set to OFF at start
  Serial.begin(uiBoudRate);
  Serial.println("");
  Serial.print(MYAPP);
  Serial.println(MYVERSION);
  pinMode(icSoftRX, INPUT); // RX
  pinMode(icSoftTX, OUTPUT); // TX
  Serial.printf("Led=%d\n",LED_BUILTIN);
  mySerial.begin(uiBoudRate);
  mySerial.println("ESP Started");
  // UDP Setup
  WiFi.mode(WIFI_STA);
  WiFi.begin(STASSID, STAPSK);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print('.');
    delay(500);
  }
  Serial.print("\nServer Connected! IP address: ");
  Serial.print(WiFi.localIP());
  Serial.printf(": %d\n", serverPort);
  Udp.begin(serverPort);
  Serial.print("Remote Client iP :");
  Serial.print(ipRemote);
  Serial.print(": ");
  Serial.print(serverPort);
  Serial.println("");
}
String strMsg = String("ESP UDP Start!");
String strMsgFromSoft = String("");
String strMsgFromUDP = String("");
int iGot = 0; 
int iRes = 0;
void loop()
{
  // Message to/From SR1
  String strToSoft;
  String strToUDP;
  String strMsgFromSerial;
  int iC = 0;
  strMsgFromSerial = ReciveSendSerial(strMsg,true);
  strMsgFromSerial = STRRemoveCRNL(strMsgFromSerial);
  // Send to Soft Serial
  strToSoft = strMsgFromSerial;
  if (strMsgFromSerial.length() <= 0)
  {
    strToSoft = strMsg;
  }
  if (strToSoft.length() >0)
  {
    Serial.printf("\n*** Message to SoftSerial [%s] ***\n",strToSoft.c_str());
  }
  strMsgFromSoft = ReciveSendSoftSerial(strToSoft);
  strMsgFromSoft = STRRemoveCRNL(strMsgFromSoft);
  strMsg = "";
  
  if (strMsgFromSerial.length() > 0)
  {
   // Send to UDP
   Serial.print("MSG to UDP>");
   Serial.println(strMsgFromSerial);
    MySendPacketString(iGot,strMsgFromSerial);
  }
  if (strMsgFromSoft.length() > 0)
  {
   // Send to UDP
   Serial.print("MSG to UDP>");
   Serial.println(strMsgFromSoft);
    MySendPacketString(iGot,strMsgFromSoft);
  }
  // UDP Messages
  strMsgFromUDP = UDPSendReceiveString(strMsgFromSoft);
  strMsgFromUDP = STRRemoveCRNL(strMsgFromUDP);
  if (strMsgFromUDP.length() > 0)
  {
     Serial.printf("UDM msg[%s]\n",strMsgFromUDP.c_str());
     if (strMsgFromUDP[0] == 'U')
     {
      strMsgFromSoft = ReciveSendSoftSerial(strMsgFromUDP);
     }
     else if(strMsgFromUDP[0] == 'E')
     {
      iC = ParseCommand(strMsgFromUDP);
      ExecuteCommand(iC);      
     }
     else if(strMsgFromUDP[0] == 'A')
     {
      iC = ParseCommand(strMsgFromUDP);
      ExecuteCommand(iC);     
      strMsgFromSoft = ReciveSendSoftSerial(strMsgFromUDP);
     }
     
  }
}

// Local Functions
String ReciveSendSerial(String strToSend, boolean bEcho)
{
  String retString;
  if (Serial.available()) {
    retString = Serial.readString();
    if (bEcho) Serial.print(strTS + retString);
  }
  if (strToSend.length() > 0){
    Serial.print(strToSend);
  }
  return retString;
}
String STRRemoveCRNL(String strMsg)
{
  String strRet = strMsg;
  int iL = strRet.length();
  int i = 0;
  for (i = 0 ; i < 2 ; i++)
  {
    if (iL > 0)
    {
      if (strRet[iL-1] == '\r' || strRet[iL-1] == '\n')
      {
        strRet[iL-1] = 0;
        iL--;
      }
    }
    else
    {
      break;
    }
  }
  return strRet;
}
String ReciveSendSoftSerial(String strToSend)
{
  String retString;
  if (mySerial.available()) {
    retString = mySerial.readString();
  }
  if (strToSend.length() > 0){
    mySerial.print(strToSend);
     if (strToSend.length() >0)
      {
        Serial.printf("\n*** Sent to SoftSerial [%s] ***\n",strToSend.c_str());
      }
  }
  return retString;
}
void MySendPacketString(int iN,String stMsgSnd)
{
    // send a reply, to the IP address and port that sent us the packet we received
    char mmm[128]= {""};
    String strLine;
    if (stMsgSnd.length() > 0)
    {
      Udp.beginPacket(ipRemote, clientPort);
      strcpy(mmm, stMsgSnd.c_str());
      Udp.write(mmm);
      Udp.endPacket();
    }
}
String UDPSendReceiveString(String strSend)
{
  String strReceive;
    int packetSize = Udp.parsePacket();

    if (packetSize) {
        Serial.print("Received packet of size ");
        Serial.println(packetSize);

        Serial.print("From ");
        IPAddress remoteIp = Udp.remoteIP();
        Serial.print(remoteIp);
        Serial.print(", port ");
        Serial.println(Udp.remotePort());

        // read the packet into recvBuffer
        int len = Udp.read(recvBuffer, 2048);
        if (len > 0) {
            strReceive = String(recvBuffer);
            Udp.beginPacket(Udp.remoteIP(), 1600);
            strcpy(sendBuffer, strSend.c_str());
            Udp.write(sendBuffer);
            Udp.endPacket();
        }
        else {
            Serial.println("Read 0 bytes.");
        }
    }
    return strReceive;
}
int ParseCommand(String strMsg)
{
  // Command string:U:XXX:YYYY
  // U- Uno command
  // XXX - 3 digit number of command
  // YYYY - 4 character
  String strCmdNum;
  int iC = 0;
  String strCmd = STRRemoveCRNL(strMsg);
  if (strCmd[0] == 'E' || strCmd[0] == 'A')
  {
    if (strCmd.length() > 2)
    {
      strCmdNum = strCmd.substring(2,2+3);
      iC = strCmdNum.toInt();
    }
  }
  return iC;
}
int ExecuteCommand(int iC)
{
  switch (iC)
  {
    case 1: // Led Off
      digitalWrite(LED_BUILTIN, HIGH); 
      Serial.println("Led Off"); 
    break;
    case 2: // Led On
      digitalWrite(LED_BUILTIN, LOW);
      Serial.println("Led On"); 
    break;
  }
}
