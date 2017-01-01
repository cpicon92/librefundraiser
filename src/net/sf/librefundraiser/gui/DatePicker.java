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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.librefundraiser.ResourceManager;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DatePicker extends Composite {
	private Text text;
	private CalendarPopup calendarPopup = null;
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
		text.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					if (tabRight()) {
						e.doit = false;
					}
				}
			}
		});
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.ARROW_DOWN: 
					incrementSelection(text, -1);
					break;
				case SWT.ARROW_UP:
					incrementSelection(text, +1);
					break;
				case SWT.TAB:
				case SWT.ESC:
					if (calendarPopup != null) {
						calendarPopup.close();
					}
					break;
				}
			}
		});
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
			@Override
			public void focusLost(FocusEvent e) {
				DatePicker.this.getDisplay().timerExec(10, new Runnable() {
					@Override
					public void run() {
						if (DatePicker.this.calendarPopup != null &&!DatePicker.this.calendarPopup.isFocused()) {
							DatePicker.this.calendarPopup.close();
						}
					}
				});
			}
		});
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Label btnDropDown = new Label(this, SWT.NONE);
		btnDropDown.setImage(ResourceManager.getIcon("calendar.png"));
		btnDropDown.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				Date initial = null;
				try {
					initial = dateFormat.parse(text.getText());
				} catch (Exception e1) {
				}
				if (initial == null) {
					initial = new Date();
				}
				text.setFocus();
				if (calendarPopup != null) {
					calendarPopup.close();
				}
				calendarPopup = new CalendarPopup(DatePicker.this.getShell(), DatePicker.this, initial);
				calendarPopup.open();
			}
		});
		btnDropDown.addMouseTrackListener(new MouseTrackListener() {
			@Override
			public void mouseEnter(MouseEvent e) {
				btnDropDown.setImage(ResourceManager.getIcon("calendar_hover.png"));
			}
			@Override
			public void mouseExit(MouseEvent e) {
				btnDropDown.setImage(ResourceManager.getIcon("calendar.png"));
			}
			@Override
			public void mouseHover(MouseEvent e) {
			}
		});
		btnDropDown.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		setTabList(new Control[]{text});


	}
	
	private boolean tabRight() {
		selectText(text);
		Integer[] indexes = getDateIndexes(this.getDate(), this.dateFormat);
		boolean changed = false;
		for (Integer i : indexes) {
			if (i > text.getSelection().y) {
				text.setSelection(i);
				selectText(text);
				changed = true;
				break;
			}
		}
		return changed;
	}
	
	private void incrementSelection(Text text, int i) {
		try {
			selectText(text);
			String selection = text.getSelectionText();
			String newInt = String
					.format("%d", Integer.parseInt(selection) + i);
			Point selectionIndexes = text.getSelection();
			String contents = text.getText();
			String prefix = contents.substring(0, selectionIndexes.x);
			String suffix = contents.substring(selectionIndexes.y);
			this.setDate(dateFormat.parse(prefix + newInt + suffix));
			text.setSelection(selectionIndexes.x,
					selectionIndexes.x + newInt.length());
		} catch (Exception e) {
		}
	}

	private static Integer[] getDateIndexes(Date date, DateFormat dateFormat) {
		ArrayList<Integer> indexes = new ArrayList<>();
		AttributedCharacterIterator charIterator = dateFormat.formatToCharacterIterator(date);
		Field previous = null;
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
			charIterator.next();
		}
		indexes.add(charIterator.getEndIndex());
		return indexes.toArray(new Integer[]{});
	}
	
	private static void selectNumbers(Text text, int indexStart, int indexEnd) {
		String selection = text.getText().substring(indexStart, indexEnd);
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(selection); 
		if (m.find()) {
		   indexStart += m.start();
		   indexEnd -= selection.length() - m.end();
		}
		text.setSelection(indexStart, indexEnd);
	}
	
	protected void selectText(final Text text) {
		text.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					Integer[] indexes = getDateIndexes(DatePicker.this.getDate(), dateFormat);
					int currentSelection = text.getSelection().x;
					boolean selectedSomething = false;
					for (int i = 1; i < indexes.length; i++) {
						if (currentSelection < indexes[i]) {
							selectNumbers(text, indexes[i-1], indexes[i]);
							selectedSomething = true;
							break;
						}
					}
					if (!selectedSomething) {
						selectNumbers(text, indexes[indexes.length - 2], indexes[indexes.length - 1]);
					}
				} catch (Exception e) {
//					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
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

	private static class CalendarPopup extends PopupDialog {
		private DatePicker sibling;
		DateTime calendar;
		private final Date initial;
		private boolean focused = false;
		public CalendarPopup(Shell parent, DatePicker sibling, Date initial) {
			super(parent, PopupDialog.HOVER_SHELLSTYLE, false, false, false, false, false, null, null);
			this.setSibling(sibling);
			this.initial = initial;
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			FillLayout layout = new FillLayout(SWT.HORIZONTAL);
			layout.marginHeight = layout.marginWidth = 1;
			composite.setLayout(layout);
			calendar = new DateTime(composite, SWT.CALENDAR);
			calendar.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					sibling.setDate(CalendarPopup.this.getDate());
				}
			});
			calendar.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
//					CalendarPopup.this.close();
				}
				@Override
				public void mouseDown(MouseEvent e) {
					CalendarPopup.this.focused = true;
				}
			});
			calendar.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					CalendarPopup.this.focused = false;
					CalendarPopup.this.close();
				}
				@Override
				public void focusGained(FocusEvent e) {
					CalendarPopup.this.focused = true;
				}
			});
			this.setDate(initial);
			final Color colorBorder = composite.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
			composite.setBackground(colorBorder);
			composite.pack();
			return composite;
		}
//		@Override
//		public boolean close() {
//			if (this.getShell() == null) return true;
//			this.getShell().getDisplay().timerExec(100, new Runnable() {
//				@Override
//				public void run() {
//					CalendarPopup.super.close();
//				}
//			});
//			return false;
//		}
		@Override
		protected void adjustBounds() {
			Point siblingPosition = this.getSibling().toDisplay(0, 0);
			Rectangle siblingBounds = this.getSibling().getBounds();
			this.getShell().setBounds(siblingPosition.x, siblingPosition.y + siblingBounds.height, 220, 180);
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
		public boolean isFocused() {
			return focused;
		}
	}
}
