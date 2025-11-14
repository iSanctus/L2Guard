package net.sf.l2j.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import net.sf.l2j.commons.logging.CLogger;

public class HwidProtectionConfig
{
    private static final CLogger LOGGER = new CLogger(HwidProtectionConfig.class.getName());
    private static final Properties _props = new Properties();

    static
    {
        load();
    }

    public static void load()
    {
        try (FileInputStream fis = new FileInputStream(new File("./config/hwid.properties")))
        {
            _props.load(fis);
            LOGGER.info("HWID Protection: hwid.properties loaded successfully.");
        }
        catch (Exception e)
        {
            LOGGER.error("HWID Protection: failed to load hwid.properties, defaults will be used.", e);
        }
    }

    public static String getProperty(String key, String defaultValue)
    {
        return _props.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key, int defaultValue)
    {
        try
        {
            return Integer.parseInt(_props.getProperty(key, String.valueOf(defaultValue)));
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue)
    {
        try
        {
            return Boolean.parseBoolean(_props.getProperty(key, String.valueOf(defaultValue)));
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }
}
