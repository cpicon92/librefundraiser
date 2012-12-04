package net.sf.librefundraiser.gui;

import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.DateFormat;
import java.text.DateFormat.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DatePicker extends Composite {
	private Text text;
	private CalendarPopup calendarPopup;
	private final DatePicker datePicker = this;
	private final DateFormat dateFormat;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public DatePicker(DateFormat format, Composite parent, int style) {
		super(parent, style);
		this.dateFormat = format;
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		text = new Text(this, SWT.BORDER);
		text.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				selectText(text);
			}
		});
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				selectText(text);
			}
		});
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Button btnDropDown = new Button(this, SWT.ARROW | SWT.DOWN);
		btnDropDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Date initial = null;
				try {
					initial = dateFormat.parse(text.getText());
				} catch (Exception e1) {
				}
				if (initial == null) {
					initial = new Date();
				}
				calendarPopup = new CalendarPopup(datePicker.getShell(), datePicker, initial);
				calendarPopup.setVisible(true);
			}
		});
		btnDropDown.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		setTabList(new Control[]{text});


	}

	protected void selectText(final Text text) {
		text.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					ArrayList<Integer> indexes = new ArrayList<Integer>();
					AttributedCharacterIterator charIterator = dateFormat.formatToCharacterIterator(datePicker.getDate());
					Field previous = null;
					char c = charIterator.first();
					for (int i = charIterator.getBeginIndex(); i < charIterator.getEndIndex(); i++) {
						Field f = null;
						for (Entry<Attribute, Object> e : charIterator.getAttributes().entrySet()) {
							try {
								f = (Field) e.getValue();
								break;
							} catch (Exception e1) {
							}
						}
						try {
						if (previous == null || !f.equals(previous)) {
							indexes.add(i);
							previous = f;
						}
						} catch (Exception e1) {
						}
						c = charIterator.next();
					}
					indexes.add(charIterator.getEndIndex());
					int currentSelection = text.getSelection().x;
					boolean selectedSomething = false;
					for (int i = 1; i < indexes.size(); i++) {
						if (currentSelection < indexes.get(i)) {
							text.setSelection(indexes.get(i-1), indexes.get(i));
							selectedSomething = true;
							break;
						}
					}
					if (!selectedSomething) {
						text.setSelection(indexes.get(indexes.size() - 2), indexes.get(indexes.size() - 1));
					}
//					if (currentSelection < 5) {
//						text.setSelection(0, 4);
//					} else if (currentSelection < 9) {
//						text.setSelection(5, 7);
//					} else if (currentSelection < 12) {
//						text.setSelection(8, 10);
//					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void openCalendar() {

	}

	public Date getDate() {
		try {
			return dateFormat.parse(text.getText());
		} catch (Exception e) {
			return null;
		}
	}

	public void setDate(Date date) {
		text.setText(dateFormat.format(date));
	}

	private class CalendarPopup extends Shell {
		private DatePicker sibling;
		private final CalendarPopup me = this;
		DateTime calendar;
		public CalendarPopup(Shell parent, DatePicker sibling, Date initial) {
			super(parent, SWT.NONE);
			this.setSibling(sibling);
			createContents();
			this.setDate(initial);
		}
		protected void createContents() {
			setLayout(new FillLayout(SWT.HORIZONTAL));
			calendar = new DateTime(this, SWT.BORDER | SWT.CALENDAR);
			calendar.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					sibling.setDate(me.getDate());
				}
			});
			this.addShellListener(new ShellListener() {
				public void shellActivated(ShellEvent e) {
				}
				public void shellClosed(ShellEvent e) {
				}
				public void shellDeactivated(ShellEvent e) {
					me.dispose();
				}
				public void shellDeiconified(ShellEvent e) {
				}
				public void shellIconified(ShellEvent e) {
				}
			});
		}
		protected void checkSubclass() {
			// Disable the check that prevents subclassing of SWT components
		}
		public void setVisible(boolean visible) {
			Point siblingPosition = this.getSibling().toDisplay(0, 0);
			Rectangle siblingBounds = this.getSibling().getBounds();
			this.setBounds(siblingPosition.x, siblingPosition.y + siblingBounds.height, 220, 180);
			super.setVisible(visible);
		}
		public DatePicker getSibling() {
			return sibling;
		}
		public void setSibling(DatePicker sibling) {
			this.sibling = sibling;
		}
		public Date getDate() {
			Calendar cal = new GregorianCalendar(calendar.getYear(),calendar.getMonth(),calendar.getDay());
			Date date = new Date(cal.getTimeInMillis());
			return date;
		}
		public void setDate(Date date) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			calendar.setYear(cal.get(Calendar.YEAR));
			calendar.setMonth(cal.get(Calendar.MONTH));
			calendar.setDay(cal.get(Calendar.DAY_OF_MONTH));
		}
	}
}
