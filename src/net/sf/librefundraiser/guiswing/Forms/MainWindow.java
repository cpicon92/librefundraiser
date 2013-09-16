package net.sf.librefundraiser.guiswing.forms;

import org.jdesktop.swingx.JXTitledPanel;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 9/15/13
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainWindow {
    private JTable tblDonorList;
    private JButton btnNew;
    private JButton btnSave;
    private JButton btnProperties;
    private JPanel mainPanel;
    private JTextField txtFilter;
    private JXTitledPanel ttlDonorList;
    private JXTitledPanel ttlDonorEdit;


    public JPanel getMainPanel() {
        return mainPanel;
    }


    public JTable getDonorTable() {
        return tblDonorList;
    }

    private void createUIComponents() {
        this.tblDonorList = new JTable(new String[][]{{"Capone","Anthony J."},{"Atwater","Patricia"}}, new String[]{"Last Name/Business", "First Name"});
        this.txtFilter = new JTextField();
//        PromptSupport.setPrompt("Filter", this.txtFilter);
        this.ttlDonorList = new JXTitledPanel("Donors", new DonorList().getMainPanel());
        this.ttlDonorEdit = new JXTitledPanel("Anthony J. Capone", new JScrollPane(new DonorEdit()));
    }
}
