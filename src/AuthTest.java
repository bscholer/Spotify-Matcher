import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author ibrabelware
 */
public class AuthTest extends JFrame {
    private JPanel pan;
    private JLabel website;
    /**
     * Creates new form JLabelLink
     */
    public AuthTest() {
        this.setTitle("Authentication");
        this.setSize(400, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        pan = new JPanel();
        website = new JLabel();

        website.setText("<html> Website : <a href=\"\">http://www.google.com/</a></html>");
        website.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pan.add(website);
        this.setContentPane(pan);
        this.setVisible(true);
        goWebsite(website);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Create and display the form
         */
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new AuthTest().setVisible(true);
            }
        });
    }

    private void goWebsite(JLabel website) {
        website.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("http://www.google.com/webhp?nomo=1&hl=fr"));
                } catch (URISyntaxException | IOException ex) {
                    //It looks like there's a problem
                }
            }
        });
    }
}