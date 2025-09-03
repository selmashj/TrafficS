
package ui;

import core.Vehicle;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;

public class VehicleInfoPanel extends JPanel {
    private final JLabel idLbl = new JLabel("-");
    private final JLabel typeLbl = new JLabel("-");
    private final JLabel speedLbl = new JLabel("-");
    private final JLabel stateLbl = new JLabel("-");

    public VehicleInfoPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JLabel("Vehicle Info"));
        add(idLbl);
        add(typeLbl);
        add(speedLbl);
        add(stateLbl);
    }

    public void showVehicleDetails(Vehicle v) {
        if (v == null) {
            idLbl.setText("No vehicle selected");
            return;
        }
        idLbl.setText("ID: " + v.getId());
        typeLbl.setText("Type: " + v.getType());
        speedLbl.setText("Speed: " + v.getSpeed());
        stateLbl.setText("State: " + v.getState());
        repaint();
    }
}































//package ui;
//
//import core.Vehicle;
//
//import javax.swing.JPanel;
//import javax.swing.JLabel;
//import javax.swing.BoxLayout;
//
//public class VehicleInfoPanel extends JPanel {
//    private final JLabel idLbl = new JLabel("-");
//    private final JLabel typeLbl = new JLabel("-");
//    private final JLabel speedLbl = new JLabel("-");
//    private final JLabel stateLbl = new JLabel("-");
//
//    public VehicleInfoPanel() {
//        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//        add(new JLabel("Vehicle Info"));
//        add(idLbl);
//        add(typeLbl);
//        add(speedLbl);
//        add(stateLbl);
//    }
//
//    public void showVehicleDetails(Vehicle v) {
//        if (v == null) {
//            idLbl.setText("No vehicle selected");
//            return;
//        }
//        idLbl.setText("ID: " + v.getId());
//        typeLbl.setText("Type: " + v.getType());
//        speedLbl.setText("Speed: " + v.getSpeed());
//        stateLbl.setText("State: " + v.getState());
//        repaint();
//    }
//}
