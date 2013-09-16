package net.sf.librefundraiser.guiswing.forms;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 9/15/13
 * Time: 4:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class DonorList {
    private JTable tblDonorList;
    private JTextField txtFilter;
    private JPanel pnlDonorList;

    private void createUIComponents() {
        this.tblDonorList = new JTable(new String[][]{{"Capone","Anthony J."},{"Atwater","Patricia"}}, new String[]{"Last Name/Business", "First Name"});
    }

    public JPanel getMainPanel() {
        return pnlDonorList;
    }
}
