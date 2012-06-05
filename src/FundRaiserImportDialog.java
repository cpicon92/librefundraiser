import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;


public class FundRaiserImportDialog extends Dialog {

	protected Object result;
	protected Shell shlImportingFromFundraiser;
	private ProgressBar progressBar;
	private Composite compositeProgress;
	private ProgressBar progressBarIndeterminate;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public FundRaiserImportDialog(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlImportingFromFundraiser.open();
		shlImportingFromFundraiser.layout();
		Display display = getParent().getDisplay();
		while (!shlImportingFromFundraiser.isDisposed()) {
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
		shlImportingFromFundraiser = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shlImportingFromFundraiser.setSize(319, 130);
		shlImportingFromFundraiser.setText("Importing from FundRaiser Basic...");
		GridLayout gl_shlImportingFromFundraiser = new GridLayout(1, false);
		gl_shlImportingFromFundraiser.verticalSpacing = 10;
		shlImportingFromFundraiser.setLayout(gl_shlImportingFromFundraiser);

		Label lblImporting = new Label(shlImportingFromFundraiser, SWT.NONE);
		lblImporting.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblImporting.setText("Importing");

		compositeProgress = new Composite(shlImportingFromFundraiser, SWT.NONE);
		compositeProgress.setLayout(new StackLayout());
		compositeProgress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		progressBarIndeterminate = new ProgressBar(compositeProgress, SWT.INDETERMINATE);

		progressBarIndeterminate.setVisible(true);
		((StackLayout)compositeProgress.getLayout()).topControl = progressBarIndeterminate;

		progressBar = new ProgressBar(compositeProgress, SWT.SMOOTH);
		progressBar.setMaximum(100);
		progressBar.setVisible(false);


		Button btnCancel = new Button(shlImportingFromFundraiser, SWT.NONE);
		btnCancel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		btnCancel.setText("Cancel");

	}

	public void setProgress(int percentage) {
		if (!progressBar.getVisible()) {
			progressBarIndeterminate.setVisible(false);
			progressBar.setVisible(true);
			((StackLayout)compositeProgress.getLayout()).topControl = progressBar;
		}
		progressBar.setSelection(percentage);
	}

	public void dispose() {
		shlImportingFromFundraiser.dispose();
	}

}
