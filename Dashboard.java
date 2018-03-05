
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.net.*;

public class Dashboard extends JFrame implements Runnable {
    public static final long serialVersionUID = 2L;
    public static void main ( String[] args ) throws SocketException {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() { new Dashboard(); }
        } );
    }

    JTextField setting;
    JTextField speed;
    JTextField throttle;
    JProgressBar speedo;
    DatagramPanel receive = new DatagramPanel();
    public Dashboard() {
        super("Dashboard");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel content = new JPanel( );
        content.setLayout( new BoxLayout( content, BoxLayout.Y_AXIS) );

        try {
            receive.setAddress(InetAddress.getLocalHost().getHostAddress(), false);
           receive.setPort(65201, false);
        }catch(UnknownHostException e){
            System.err.println(e.getMessage());
        }
        content.add(receive);

        JPanel debug = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        debug.add(new JLabel("setting"));
        setting = new JTextField("99900.##mph");
        setting.setEditable(false);
        debug.add(setting);
        debug.add(new JLabel("throttle"));
        throttle = new JTextField("###.##%");
        throttle.setEditable(false);
        debug.add(throttle);
        debug.add(new JLabel("Speed"));
        speed = new JTextField("99900.##mph");
        speed.setEditable(false);
        debug.add(speed);
        content.add(debug);

        speedo = new JProgressBar(0,110);
        content.add(speedo);
        this.setContentPane(content);
        this.pack();
        this.setVisible(true);

        /* start thread that handles comminications */
        (new Thread(this)).start();
    }


    public void run() {
        try{
        /* set up socket for reception */
            SocketAddress address = receive.getSocketAddress();
            DatagramSocket socket = new DatagramSocket(address);

            while(true) {
                try{
                    /* start with fresh datagram packet */
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive( packet );
                    /* extract message and pick appart into
                       lines and key:value pairs
                    */
                    String message = new String(packet.getData());
                    String[] lines = message.trim().split("\n");
                    for(String l : lines) {
                        String[] pair  = l.split(":");
                        switch( pair[0] ) {/*<-- Java now lets you do switches on strings :-) */
                            case "setting":
                                double set = Double.parseDouble(pair[1]);
                                setting.setText(String.format("%5.2fmph",set));
                            break;
                            case "speed":
                                double mph = Double.parseDouble(pair[1]);
                                speed.setText(String.format("%5.2fmph",mph));
                                speedo.setValue((int)mph);
                            break;
                            case "throttle":
                                double th = Double.parseDouble(pair[1]);
                                throttle.setText(String.format("%5.2f%%",th*100));
                            break;
                        }
                    }
                }catch(IOException e){
                    System.err.println(e.getMessage());
                }
            }
        }catch(SocketException e){
            System.err.println(e.getMessage());
        }
    }


}
