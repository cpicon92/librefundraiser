package net.sf.librefundraiser;

import net.sf.librefundraiser.db.FileDBASE;
import net.sf.librefundraiser.gui.MainWindow;
import net.sf.librefundraiser.guiswing.frames.MainFrameFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import java.io.File;
import java.io.IOException;

import static net.sf.librefundraiser.Util.fileCreationPossible;
import static net.sf.librefundraiser.Util.loadSettings;

//TODO: fix fixed-width dialogs to render properly on high-dpi displays
public class SwingMain {
	public static final String version = "(Development Snapshot)";
	public static void main(String args[]) {
		if (args.length > 1) {
			System.err.println("Syntax: librefundraiser [filename]");
		}
        File db = null;
		if (args.length == 1) {
			db = new File(args[0]);
		}
        loadSettings();
        new MainFrameFactory()
                .setDb(db)
                .makeMainFrame();
	}

    //TODO Clean this stuff out
	public static String newDbFilePrompt(Shell shell) {
		FileDialog fileDialog = new FileDialog(shell,SWT.SAVE);
		fileDialog.setFilterExtensions(new String[]{"*.lfd","*.*"});
		fileDialog.setFilterNames(new String[]{"LibreFundraiser Database (*.lfd)","All Files"});
		String path = "";
		boolean goodPath = false;
		while (!goodPath) {
			try {
				do {
					path = fileDialog.open();
				} while(!fileCreationPossible(path));
				goodPath = true;
			} catch (IOException e) {
				File file = new File(path);
				MessageBox verify = new MessageBox(shell,SWT.YES | SWT.NO | SWT.ICON_WARNING);
				verify.setMessage(file.getName() + " already exists. Do you want to overwrite it?");
				verify.setText("LibreFundraiser Warning");
				goodPath = verify.open() == SWT.YES;
			}
		}
		return path;
	}
	
	public static void importFromFRBW(final Display display, final Shell parent, final MainWindow mainWindow, final String path) {
		if (path == null) return;
		new Thread(new Runnable() {
			public void run() {
				FileDBASE db = new FileDBASE(path);
				Donor[] importedDonors = new Donor[] {};
				try {
					importedDonors = db.importFRBW();
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox error = new MessageBox(parent,SWT.ICON_ERROR);
					error.setText("LibreFundraiser Error");
					error.setMessage("Could not load donors. This probably isn't a FundRaiser basic installation folder...");
				}
                //commented out because of calls to deprecated method
//				SwingMain.getDonorDB().saveDonors(importedDonors);
//				SwingMain.getWindow().refresh(true, true);
			}
		}).start();
	}
}
