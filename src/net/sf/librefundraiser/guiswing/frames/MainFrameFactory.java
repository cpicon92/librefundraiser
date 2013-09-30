package net.sf.librefundraiser.guiswing.frames;

import net.sf.librefundraiser.db.FileLFD;
import net.sf.librefundraiser.db.NewerDbVersionException;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 9/29/13
 * Time: 9:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainFrameFactory {
    public MainFrameFactory setDb(File db) {
        this.db = db;
        return this;
    }
    private File db = null;
    public MainFrame makeMainFrame() {
        MainFrame mf = new MainFrame();
        if (db != null) mf.openFile(db);
        return mf;
    }
}
