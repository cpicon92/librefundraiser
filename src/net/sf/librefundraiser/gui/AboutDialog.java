package net.sf.librefundraiser.gui;

import java.net.MalformedURLException;
import java.net.URL;

import net.sf.librefundraiser.Main;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

public class AboutDialog extends Dialog {

	protected Object result;
	protected Shell shell;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public AboutDialog(Shell parent, int style) {
		super(parent, style);
		setText("About LibreFundraiser");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(getText());
		GridLayout gl_shell = new GridLayout(1, false);
		gl_shell.marginHeight = 0;
		gl_shell.marginWidth = 0;
		gl_shell.verticalSpacing = 0;
		shell.setLayout(gl_shell);
		
		SelectionAdapter linkAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					Program.launch(new URL(e.text).toString());
				} catch (MalformedURLException e1) {
				}
			}
		};
		
		Composite compositeBanner = new Composite(shell, SWT.NONE);
		compositeBanner.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		compositeBanner.setBackgroundMode(SWT.INHERIT_DEFAULT);
		compositeBanner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout gl_compositeBanner = new GridLayout(2, false);
		gl_compositeBanner.marginLeft = 10;
		compositeBanner.setLayout(gl_compositeBanner);
		
		Label lblLibrefundraiser = new Label(compositeBanner, SWT.NONE);
		lblLibrefundraiser.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		lblLibrefundraiser.setFont(SWTResourceManager.getFont("Segoe UI", 26, SWT.NORMAL));
		lblLibrefundraiser.setText("LibreFundraiser");
		
		Label lblLogo = new Label(compositeBanner, SWT.NONE);
		lblLogo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 3));
		lblLogo.setImage(SWTResourceManager.getImage(AboutDialog.class, "/net/sf/librefundraiser/logo/balloon128.png"));
		
		Label lblVersion = new Label(compositeBanner, SWT.NONE);
		lblVersion.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		lblVersion.setText(Main.version);
		
		Link linkSite = new Link(compositeBanner, SWT.NONE);
		linkSite.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, true, 1, 1));
		linkSite.setText("Please visit our website: <a href=\"http://librefundraiser.sf.net/\">librefundraiser.sf.net</a>");
		linkSite.addSelectionListener(linkAdapter);
		
		Label lblSep = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		lblSep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblSep.setText("sep");
		
		Composite compositeInfo = new Composite(shell, SWT.NONE);
		compositeInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_compositeInfo = new GridLayout(3, false);
		gl_compositeInfo.verticalSpacing = 10;
		gl_compositeInfo.marginWidth = 10;
		gl_compositeInfo.marginHeight = 10;
		compositeInfo.setLayout(gl_compositeInfo);
		
		Label lblCopyright = new Label(compositeInfo, SWT.NONE);
		lblCopyright.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		lblCopyright.setText("Copyright © 2012 The LibreFundraiser developers. All rights reserved. ");
		
		Label lblGpl = new Label(compositeInfo, SWT.NONE);
		lblGpl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblGpl.setImage(SWTResourceManager.getImage(AboutDialog.class, "/net/sf/librefundraiser/icons/gpl.png"));
		
		Label lblGplNotice = new Label(compositeInfo, SWT.WRAP);
		lblGplNotice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		lblGplNotice.setText("LibreFundraiser is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. ");
		
		Label lblWarranty = new Label(compositeInfo, SWT.WRAP);
		lblWarranty.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		lblWarranty.setText("LibreFundraiser is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.");
		
		Link linkGNU = new Link(compositeInfo, SWT.NONE);
		linkGNU.addSelectionListener(linkAdapter);
		linkGNU.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		linkGNU.setText("You should have received a copy of the GNU General Public License along with LibreFundraiser.  If not, see <a href=\"http://www.gnu.org/licenses/\">http://www.gnu.org/licenses/</a>.");
		
		Link linkIcon = new Link(compositeInfo, SWT.NONE);
		linkIcon.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 1));
		linkIcon.setText("Balloon icon by <a href=\"http://dapinographics.com/\">Pien Duijverman</a>, \r\nlicensed under <a href=\"http://dapinographics.com/license-info/#thethe-togle-content-7981\">CC Attribution-Noncommercial 3.0.</a>");
		linkIcon.addSelectionListener(linkAdapter);
		
		Button btnOk = new Button(compositeInfo, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		GridData gd_btnOk = new GridData(SWT.RIGHT, SWT.BOTTOM, false, true, 1, 1);
		gd_btnOk.widthHint = 70;
		btnOk.setLayoutData(gd_btnOk);
		btnOk.setText("OK");
		compositeInfo.setTabList(new Control[]{btnOk, linkGNU, linkIcon});
		shell.setTabList(new Control[]{compositeInfo, compositeBanner});
		
		shell.setSize(shell.computeSize(455, -1));

	}

}
