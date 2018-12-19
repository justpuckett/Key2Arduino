package com.key2arduino;

import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArduinoWriter {

    SerialPort serialPort;
    /**
     * The port we're normally going to use.
     */
    private static final String PORT_NAMES[] = {
        "/dev/tty.usbserial-A9007UX1", // Mac OS X
        "/dev/ttyACM0", // Raspberry Pi
        "/dev/ttyUSB0", // Linux
        "COM3", // Windows
    };
    /**
     * A BufferedReader which will be fed by a InputStreamReader converting the
     * bytes into characters making the displayed results codepage independent
     */
    private BufferedReader input;
    /**
     * The output stream to the port
     */
    private OutputStream output;
    /**
     * Milliseconds to block while waiting for port open
     */
    private static final int TIME_OUT = 2000;
    /**
     * Default bits per second for COM port.
     */
    private static final int DATA_RATE = 9600;

    public void initialize() {
        // the next line is for Raspberry Pi and 
        // gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
        //System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

        //CommPortIdentifier portId = null;
        //Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            System.out.println("Port: " + port.getDescriptivePortName() + "\t " + port.getPortDescription() + " \t " + port.getSystemPortName());
        }
        serialPort = SerialPort.getCommPort("COM4");
        serialPort.setBaudRate(DATA_RATE);
        serialPort.openPort();

        PrintWriter output = new PrintWriter(serialPort.getOutputStream());
        for (int i = 0; i < 10; i++) {
            output.append("<T0>");

            output.flush();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(ArduinoWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
            byte[] readBuffer = new byte[serialPort.bytesAvailable()];
            int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
            if (numRead > 0) {
                for (int k = 0; k < numRead; k++) {
                    System.out.print((char) readBuffer[k]);
                }
                System.out.println();

            }

            // output.append("<Test2>");
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(ArduinoWriter.class.getName()).log(Level.SEVERE, null, ex);
//            }
            //    output.flush();
        }
        serialPort.closePort();
//        //First, Find an instance of serial port as set in PORT_NAMES.
//        while (portEnum.hasMoreElements()) {
//            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
//            for (String portName : PORT_NAMES) {
//                if (currPortId.getName().equals(portName)) {
//                    portId = currPortId;
//                    break;
//                }
//            }
//        }
//        if (portId == null) {
//            System.out.println("Could not find COM port.");
//            return;
//        }
//
//        try {
//            // open serial port, and use class name for the appName.
//            serialPort = (SerialPort) portId.open(this.getClass().getName(),
//                    TIME_OUT);
//
//            // set port parameters
//            serialPort.setSerialPortParams(DATA_RATE,
//                    SerialPort.DATABITS_8,
//                    SerialPort.STOPBITS_1,
//                    SerialPort.PARITY_NONE);
//
//            // open the streams
//            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
//            output = serialPort.getOutputStream();
//
//            // add event listeners
//            serialPort.addEventListener(this);
//            serialPort.notifyOnDataAvailable(true);
//        } catch (Exception e) {
//            System.err.println(e.toString());
//        }
    }

    /**
     * This should be called when you stop using the port. This will prevent
     * port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.closePort();
        }
    }

//    /**
//     * Handle an event on the serial port. Read the data and print it.
//     */
//    public synchronized void serialEvent(SerialPortEvent oEvent) {
//        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
//            try {
//                String inputLine = input.readLine();
//                System.out.println(inputLine);
//            } catch (Exception e) {
//                System.err.println(e.toString());
//            }
//        }
//        // Ignore all the other eventTypes, but you should consider the other ones.
//    }
    public static void main(String[] args) throws Exception {
        ArduinoWriter main = new ArduinoWriter();
        main.initialize();
        Thread t = new Thread() {
            public void run() {
                //the following line will keep this app alive for 1000 seconds,
                //waiting for events to occur and responding to them (printing incoming messages to console).
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException ie) {
                }
            }
        };
        t.start();
        System.out.println("Started");
    }

    /**
     * Crude Arduino detection that requires response from Arduino firmware
     *
     * @return
     */
    public static SerialPort findArduinoPort() {
        SerialPort[] ports = SerialPort.getCommPorts();

        boolean[] connected = new boolean[ports.length];
        for (int i = ports.length - 1; i >= 0; i--) {
            SerialPort port = ports[i];
            port.setBaudRate(DATA_RATE);
            boolean success = false;
            port.openPort();
            OutputStream os = port.getOutputStream();
            if (os == null || !port.isOpen()) {
                System.out.println(port.getSystemPortName() + " is busy.");
            } else {
                PrintWriter output = new PrintWriter(os);
                InputStream in = port.getInputStream();
                for (int m = 0; m < 50; m++) {
                    output.write("<?>\n");
                    output.flush();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ArduinoWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    byte[] readBuffer = new byte[port.bytesAvailable()];
                    int numRead = port.readBytes(readBuffer, readBuffer.length);
                    if (numRead > 0) {
                        for (int k = 0; k < numRead; k++) {
                            System.out.print((char) readBuffer[k]);
                        }
                        System.out.println();
                        success = true;
                        break;
                    }
                }
                try {
                    in.close();
                } catch (IOException ex) {
                    Logger.getLogger(ArduinoWriter.class.getName()).log(Level.SEVERE, null, ex);
                }
                port.closePort();
                System.out.println(success + " on: Port: " + port.getDescriptivePortName() + "\t " + port.getPortDescription() + " \t " + port.getSystemPortName());
                if (success) {
                    return port;
                }
            }
            connected[i] = success;// && numRead > 0;

        }
        for (int i = 0; i < ports.length; i++) {
            if (connected[i]) {
                return ports[i];
            }
        }
        return null;
    }

    public static void writeToPort(SerialPort port, String[] outputLines) {
        port.setBaudRate(DATA_RATE);
        
        port.openPort();
        //flushRead(port, true);
        for (int i = 0; i < outputLines.length; i++) {
            //flushRead(port, true);
            writeToOpenPort(port, outputLines[i]);
        }
        //flushRead(port, true);

        port.closePort();
    }

    public static void writeToOpenPort(SerialPort port, String outputText) {
        if (port != null && port.isOpen()) {
            port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
            OutputStream os = port.getOutputStream();
            PrintWriter output = new PrintWriter(os);
            System.out.println("Writing: '" + outputText + "'");
            output.write(outputText);
            output.flush();
            System.out.println();
        }
    }

    private static void flushRead(SerialPort port, boolean delay) {
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        long timeout = 1000;
        long startTime = System.currentTimeMillis();
        boolean readStarted = false;
        do {

            byte[] readBuffer = new byte[port.bytesAvailable()];
            int numRead = port.readBytes(readBuffer, readBuffer.length);
            if (numRead > 0) {
                for (int k = 0; k < numRead; k++) {
                    System.out.print((char) readBuffer[k]);
                }
                readStarted = true;
            }
            try {
                Thread.sleep(5);
            } catch (Exception e) {
            }

        } while ((port.bytesAvailable() > 0 || !readStarted) && ((System.currentTimeMillis()-startTime) < timeout));//|| 
    }
}
