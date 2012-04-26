Arduino-Communicator
====================

Very simple Android application for communicating with Arduino Uno (with Atmega16U2 or Atmega8U2 programmed as a USB-to-serial converter).

No need for extra Host Shield or Bluetooth. All you need is a Micro USB OTG to USB Adapter.

Send data from your Arduino with Serial.println(), Serial.print() or Serial.write() in 9600 baud rate. Receive data with Serial.read().

Toggle between hex and ascii by clicking on received/sent data.

Let your own Android application receive data from Arduino by listening to the "primavera.arduino.intent.action.DATA_RECEIVED" intent. This intent will contain the "primavera.arduino.intent.extra.DATA" byte array with the received data. Call getByteArrayExtra("primavera.arduino.intent.extra.DATA") to retreive the data.
Send data to Arduino from your application by broadcasting an intent with action "primavera.arduino.intent.action.SEND_DATA". Add the data to be sent as byte array extra "primavera.arduino.intent.extra.DATA".

Please note that this app will not work with Arduino boards with the FTDI USB-to-serial driver chip.

Source code at: https://github.com/jeppsson/Arduino-Communicator
