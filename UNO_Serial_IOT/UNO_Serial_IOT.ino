#include <SoftwareSerial.h>

const char MYAPP[] = "UNO Serial:";
const char MYVERSION[] = "1.1.0.0";

String strR = String("R>");
String strT = String("T>");

const int icSoftRX = 2;
const int icSoftTX = 3;
SoftwareSerial mySerial(icSoftRX,icSoftTX);

unsigned int uiBoudRate = 19200;
/*
 Commands - 1 - Led Off - U-001 / A001
            2 - Led On  - U-002 / A002
 */

void setup()
{
  pinMode(LED_BUILTIN, OUTPUT);
  Serial.begin(uiBoudRate);
  Serial.print(MYAPP);
  Serial.println(MYVERSION);
  pinMode(icSoftRX, INPUT); // RX
  pinMode(icSoftTX, OUTPUT); // TX
  mySerial.begin(uiBoudRate);
  mySerial.println("Uno Started");
}

String strMsg = String("UNO Start!");
String strMsgFromSoft = String("");
void loop()
{
  int iC = 0;
  // Message to/From SR1
  strMsg = ReciveSendSerial(strMsg, true);
  strMsgFromSoft = ReciveSendSoftSerial(strMsg);
  if (strMsgFromSoft.length() > 0)
  {
    iC = ParseCommand(strMsgFromSoft);
    ExecuteCommand(iC);
    strMsg = strR + strMsgFromSoft;
    if (iC == 0)
    {
      Serial.print("MSG:"); 
      Serial.println(strMsg); 
    }
  }
  strMsg = ""; //strMsgFromSoft;
}

String ReciveSendSerial(String strToSend, boolean bEcho)
{
  String retString;
  if (Serial.available()) {
    retString = Serial.readString();
    if (bEcho) Serial.println(strT + retString);
  }
  if (strToSend.length() > 0) {
    Serial.println(strToSend);
  }
  return retString;
}
String ReciveSendSoftSerial(String strToSend)
{
  String retString;
  if (mySerial.available()) {
    retString = mySerial.readString();
  }
  if (strToSend.length() > 0) {
    mySerial.print(strToSend);
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
int ParseCommand(String strMsg)
{
  // Command string:U:XXX:YYYY
  // U- Uno command
  // XXX - 3 digit number of command
  // YYYY - 4 character
  String strCmdNum;
  int iC = 0;
  String strCmd = STRRemoveCRNL(strMsg);
  if (strCmd[0] == 'U' || strCmd[0] == 'A')
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
      digitalWrite(LED_BUILTIN, LOW); 
      Serial.println("Led Off"); 
    break;
    case 2: // Led On
      digitalWrite(LED_BUILTIN, HIGH);
      Serial.println("Led On"); 
    break;
  }
}
