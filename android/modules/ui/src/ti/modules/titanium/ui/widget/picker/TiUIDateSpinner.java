/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package ti.modules.titanium.ui.widget.picker;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kankan.wheel.widget.WheelView;

import org.appcelerator.titanium.TiDict;
import org.appcelerator.titanium.TiProxy;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import android.widget.LinearLayout;

public class TiUIDateSpinner extends TiUIView
		implements WheelView.OnItemSelectedListener
{
	private static final String LCAT = "TiUIDateSpinner";
	private WheelView monthWheel;
	private WheelView dayWheel;
	private WheelView yearWheel;
	
	private FormatNumericWheelAdapter monthAdapter;
	private FormatNumericWheelAdapter dayAdapter;
	private FormatNumericWheelAdapter yearAdapter;
	
	private boolean suppressChangeEvent = false;
	private boolean ignoreItemSelection = false;

	private Date maxDate, minDate;
	private Locale locale = Locale.getDefault();
	private boolean dayBeforeMonth = false;
	private boolean numericMonths = false;
	
	private Calendar calendar = Calendar.getInstance();
	
	public TiUIDateSpinner(TiViewProxy proxy)
	{
		super(proxy);
		createNativeView();
	}
	
	private void createNativeView()
	{
		// defaults
		maxDate = new Date(calendar.get(Calendar.YEAR) + 100, 11, 31);
		minDate = new Date(calendar.get(Calendar.YEAR) - 100, 0, 31);
		
		monthWheel = new WheelView(proxy.getContext());
		dayWheel = new WheelView(proxy.getContext());
		yearWheel = new WheelView(proxy.getContext());
		
		monthWheel.setTextSize(20);
		dayWheel.setTextSize(monthWheel.getTextSize());
		yearWheel.setTextSize(monthWheel.getTextSize());
		
    	monthWheel.setItemSelectedListener(this);
    	dayWheel.setItemSelectedListener(this);
    	yearWheel.setItemSelectedListener(this);
        
		LinearLayout layout = new LinearLayout(proxy.getContext());
		layout.setOrientation(LinearLayout.HORIZONTAL);
		
		if (proxy.hasDynamicValue("dayBeforeMonth")) {
			dayBeforeMonth = TiConvert.toBoolean(proxy.getDynamicProperties(), "dayBeforeMonth");
		}
		
		if (dayBeforeMonth) {
			layout.addView(dayWheel);
			layout.addView(monthWheel);
		} else {
			layout.addView(monthWheel);
			layout.addView(dayWheel);
		}
		
		layout.addView(yearWheel);
		setNativeView(layout);
		
	}
	
	@Override
	public void processProperties(TiDict d) {
		super.processProperties(d);
		
		boolean valueExistsInProxy = false;
	    
        if (d.containsKey("value")) {
            calendar.setTime((Date)d.get("value"));
            valueExistsInProxy = true;
        }   
        
        if (d.containsKey("minDate")) {
        	Calendar c = Calendar.getInstance();
        	minDate = TiConvert.toDate(d, "minDate");
        	c.setTime(minDate);
        } 
        
        if (d.containsKey("maxDate")) {
        	Calendar c = Calendar.getInstance();
        	maxDate = TiConvert.toDate(d, "maxDate");
        	c.setTime(maxDate);
        } 
        
        if (d.containsKey("locale")) {
        	setLocale(TiConvert.toString(d, "locale"));
        }
        
        if (d.containsKey("dayBeforeMonth")) {
        	dayBeforeMonth = TiConvert.toBoolean(d, "dayBeforeMonth");
        }
        
        if (d.containsKey("numericMonths")) {
        	numericMonths = TiConvert.toBoolean(d, "numericMonths");
        }
        
        if (maxDate.before(minDate)) {
        	maxDate = minDate;
        }
        
        // If initial value is out-of-bounds, set date to nearest bound
        if (calendar.getTime().after(maxDate)) {
        	calendar.setTime(maxDate);
        } else if (calendar.getTime().before(minDate)) {
        	calendar.setTime(minDate);
        }
        
        setValue(calendar.getTimeInMillis() , true);
        
        if (!valueExistsInProxy) {
        	proxy.internalSetDynamicValue("value", calendar.getTime(), false);
        }
      
	}
	
	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue,
			TiProxy proxy)
	{
		if ("value".equals(key)) {
			Date date = (Date)newValue;
			setValue(date.getTime());
		} else if ("locale".equals(key)) {
			setLocale(TiConvert.toString(newValue));
		}
		super.propertyChanged(key, oldValue, newValue, proxy);
	}
	
	
	private void setAdapters()
	{
		setYearAdapter();
		setMonthAdapter();
		setDayAdapter();
		
	}

	private void setYearAdapter()
	{
		int minYear = minDate.getYear() + 1900;
		int maxYear = maxDate.getYear() + 1900;
		if (yearAdapter != null && yearAdapter.getMinValue() == minYear && yearAdapter.getMaxValue() == maxYear) {
			return;
		}
		yearAdapter = new FormatNumericWheelAdapter(minYear, maxYear, new DecimalFormat("0000"), 4);
		
		ignoreItemSelection = true;
		yearWheel.setAdapter(yearAdapter);
		ignoreItemSelection = false;
	}
	
	private void setMonthAdapter()
	{
		setMonthAdapter(false);
	}
	
	private void setMonthAdapter(boolean forceUpdate)
	{
		int setMinMonth = 1;
		int setMaxMonth = 12;
		
		int currentMin = -1, currentMax = -1;
		if (monthAdapter != null) {
			currentMin = monthAdapter.getMinValue();
			currentMax = monthAdapter.getMaxValue();
		}

		int maxYear = maxDate.getYear() + 1900;
		int minYear = minDate.getYear() + 1900;
		int selYear = getSelectedYear();
		
		if (selYear == maxYear) {
			setMaxMonth = maxDate.getMonth() + 1;
		}
		
		if (selYear == minYear) {
			setMinMonth = minDate.getMonth() + 1;
		}
		
		if (currentMin != setMinMonth || currentMax != setMaxMonth || forceUpdate) {
			NumberFormat format;
			int width = 4;
			if (numericMonths) {
				format = new DecimalFormat("00");
			} else {
				format = new MonthFormat(this.locale);
				width = ((MonthFormat)format).getLongestMonthName();
			}
			monthAdapter = new FormatNumericWheelAdapter(setMinMonth, setMaxMonth, format, width);
			ignoreItemSelection = true;
			monthWheel.setAdapter(monthAdapter);
			ignoreItemSelection = false;
		}
		
	}
	
	private void setDayAdapter()
	{
		int setMinDay = 1;
		int setMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		int currentMin = -1, currentMax = -1;
		if (dayAdapter != null) {
			currentMin = dayAdapter.getMinValue();
			currentMax = dayAdapter.getMaxValue();
		}

		int maxYear = maxDate.getYear() + 1900;
		int minYear = minDate.getYear() + 1900;
		int selYear = getSelectedYear();
		int maxMonth = maxDate.getMonth() + 1;
		int minMonth = minDate.getMonth() + 1;
		int selMonth = getSelectedMonth();
		
		if (selYear == maxYear && selMonth == maxMonth) {
			setMaxDay = maxDate.getDate();
		}
		
		if (selYear == minYear && selMonth == minMonth) {
			setMinDay = minDate.getDate();
		}
		
		if (currentMin != setMinDay || currentMax != setMaxDay) {
			dayAdapter = new FormatNumericWheelAdapter(setMinDay, setMaxDay, new DecimalFormat("00"), 4);
			ignoreItemSelection = true;
			dayWheel.setAdapter(dayAdapter);
			ignoreItemSelection = false;
		}
	}
	
	private void syncWheels()
	{
		ignoreItemSelection = true;
		yearWheel.setCurrentItem(yearAdapter.getIndex(calendar.get(Calendar.YEAR)));
		monthWheel.setCurrentItem(monthAdapter.getIndex(calendar.get(Calendar.MONTH) + 1));
		dayWheel.setCurrentItem(dayAdapter.getIndex(calendar.get(Calendar.DAY_OF_MONTH)));
		ignoreItemSelection = false;
	}
	
	public void setValue(long value)
	{
		setValue(value, false);
	}
	
	public void setValue(long value, boolean suppressEvent)
	{
		Date oldVal, newVal;
		oldVal = calendar.getTime();
		
		setCalendar(value);
		newVal = calendar.getTime();
		if (newVal.after(maxDate)) {
			newVal = maxDate;
			setCalendar(newVal);
		} else if (newVal.before(minDate)) {
			newVal = minDate;
			setCalendar(newVal);
		}
		
		boolean isChanged = (!newVal.equals(oldVal));
		
		setAdapters();
		
		syncWheels();
		proxy.internalSetDynamicValue("value", newVal, false);
		
		if (isChanged && !suppressEvent) {
			if (!suppressChangeEvent) {
				TiDict data = new TiDict();
				data.put("value", newVal);
				proxy.fireEvent("change", data);
			}

		}
	}
	
	public void setValue(Date value, boolean suppressEvent)
	{
		long millis = value.getTime();
		setValue(millis, suppressEvent);
	}
	
	public void setValue(Date value)
	{
		setValue(value, false);
	}
	
	public void setValue()
	{
		setValue(getSelectedDate());
	}
	
	private void setLocale(String localeString) 
	{
		Locale locale = Locale.getDefault();
		if (localeString != null && localeString.length() > 1) {
			String stripped = localeString.replaceAll("-", "").replaceAll("_", "");
			if (stripped.length() == 2) {
				locale = new Locale(stripped);
			} else if (stripped.length() >= 4) {
				String language = stripped.substring(0, 2);
				String country = stripped.substring(2, 4);
				if (stripped.length() > 4) {
					locale = new Locale(language, country, stripped.substring(4));
				} else {
					locale = new Locale(language, country);
				}
			} else {
				Log.w(LCAT, "Locale string '" + localeString + "' not understood.  Using default locale.");
			}
		}

		if (!this.locale.equals(locale)) {
			this.locale = locale;
			setMonthAdapter(true);
			syncWheels();
		}
	}
	
	private void setCalendar(long millis)
	{
		calendar.setTimeInMillis(millis);
	}
	
	private void setCalendar(Date date)
	{
		calendar.setTime(date);
	}
	
	private int getSelectedYear()
	{
		return yearAdapter.getValue(yearWheel.getCurrentItem());
	}
	
	private int getSelectedMonth()
	{
		return monthAdapter.getValue(monthWheel.getCurrentItem());
	}
	
	private int getSelectedDay()
	{
		return dayAdapter.getValue(dayWheel.getCurrentItem());
	}
	
	private Date getSelectedDate()
	{
		return new Date(getSelectedYear() - 1900, getSelectedMonth() - 1, getSelectedDay());
	}
	
	@Override
	public void onItemSelected(WheelView view, int index)
	{
		if (ignoreItemSelection) {
			return;
		}
		setValue();
		
	}
	
	class MonthFormat extends NumberFormat
	{
		private static final long serialVersionUID = 1L;
		private DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
		
		public MonthFormat(Locale locale)
		{
			super();
			setLocale(locale);
		}
		
		@Override
		public StringBuffer format(double value, StringBuffer buffer,
				FieldPosition position)
		{
			return format((long) value, buffer, position);
		}

		@Override
		public StringBuffer format(long value, StringBuffer buffer,
				FieldPosition position)
		{
			buffer.append(symbols.getMonths()[((int)value) - 1]);
			return buffer;
		}

		@Override
		public Number parse(String value, ParsePosition position)
		{
			String[] months = symbols.getMonths();
			for (int i = 0; i < months.length; i++) {
				if (months[i].equals(value)) {
					return new Long(i + 1);
				}
			}
			return null;
		}
		
		public void setLocale(Locale locale)
		{
			symbols = new DateFormatSymbols(locale);
		}
		
		public int getLongestMonthName()
		{
			int max = 0;
			for (String month : symbols.getMonths()) {
				max = (month.length() > max) ? month.length() : max;
			}
			return max;
		}
		
	}

}
