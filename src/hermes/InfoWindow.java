package hermes;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

    /**
     * Graphical informations display
     * @author winston2968
     * @version 1.0
     */

public class InfoWindow extends JFrame implements MouseListener {
    
    /**
     * InfoWindow constructor
     * @param messageType
     * @param message
     */
    public InfoWindow (String messageType, String message) {
        super(messageType);
        this.add(new JLabel(message), BorderLayout.CENTER);
        JButton button = new JButton("ok");
        this.add(button, BorderLayout.SOUTH);
        button.addMouseListener(this);
        this.setSize(200,400);
        this.setVisible(true);



    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.setVisible(false);
        this.dispose();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Unimplemented method 'mousePressed'");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Unimplemented method 'mouseReleased'");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Unimplemented method 'mouseEntered'");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Unimplemented method 'mouseExited'");
    }
}
