package net.sf.librefundraiser.guiswing.forms;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 9/15/13
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class DonorEdit extends JXTaskPaneContainer {
    public DonorEdit() {
        super();
        JXTaskPane basicInfoGroup = new JXTaskPane("Basic Info");
        basicInfoGroup.add(new DonorBasicInfo().getMainPanel());
        this.add(basicInfoGroup);
        JXTaskPane giftGroup = new JXTaskPane("Gifts");
        giftGroup.add(new GiftList().getMainPanel());
        this.add(giftGroup);
    }
}
