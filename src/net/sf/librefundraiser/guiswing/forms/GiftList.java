package net.sf.librefundraiser.guiswing.forms;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 9/15/13
 * Time: 7:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class GiftList {
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JTable tblGifts;
    private JPanel pnlGifts;

    public JPanel getMainPanel() {
        return pnlGifts;
    }

    private void createUIComponents() {
        this.tblGifts = new JTable(new String[][]{
                {"57","$50.00","2004-01-28","false","2004-01-31","Mailing",""},
                {"57","$50.00","2004-01-28","false","2004-01-31","Mailing",""},
                {"57","$50.00","2004-01-28","false","2004-01-31","Mailing",""},
                {"57","$50.00","2004-01-28","false","2004-01-31","Mailing",""},
                {"57","$50.00","2004-01-28","false","2004-01-31","Mailing",""}
        }, new String[]{"Record Number","Amount","Date Given","Letter","Entry Date","Source","Note"});
    }
}
