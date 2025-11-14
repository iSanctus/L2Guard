package net.sf.l2j.commons.logging.formatter;

import java.util.logging.LogRecord;

import net.sf.l2j.commons.logging.MasterFormatter;

public class FileLogFormatter extends MasterFormatter
{
	@Override
	public String format(LogRecord logRecord)
	{
		return "[" + getFormatedDate(logRecord.getMillis()) + "]" + SPACE + logRecord.getLevel().getName() + SPACE + logRecord.getMessage() + CRLF;
	}
}