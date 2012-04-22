Arduino-Communicator
====================

Very simple Android application for communicating with Arduino Uno (with Atmega16U2 or Atmega8U2 programmed as a USB-to-serial converter).

No need for extra Host Shield or Bluetooth. All you need is a Micro USB OTG to USB Adapter.

Send data from your Arduino with Serial.println(), Serial.print() or Serial.write() in 9600 baud rate. Receive data with Serial.read(). (See below how to send data from your Android app)

Please note that this app will not work with Arduino boards with the FTDI USB-to-serial driver chip.

To let your own Android app receive data from Arduino, listen to the "primavera.arduino.intent.action.DATA_RECEIVED" intent. This intent will contain the "primavera.arduino.intent.extra.DATA" byte array with the received data. Call getByteArrayExtra("primavera.arduino.intent.extra.DATA") to retreive the data.
To send data to Arduino from your app, broadcast an intent with action "primavera.arduino.intent.action.SEND_DATA". Add the data to be sent as byte array extra "primavera.arduino.intent.extra.DATA".
