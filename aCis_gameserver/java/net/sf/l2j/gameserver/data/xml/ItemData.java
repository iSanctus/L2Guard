package net.sf.l2j.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.logging.CLogger;

import net.sf.l2j.gameserver.data.DocumentItem;
import net.sf.l2j.gameserver.model.item.kind.Armor;
import net.sf.l2j.gameserver.model.item.kind.EtcItem;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;

/**
 * This class loads and stores all {@link Item} templates.
 * Supports Weapons, Armors, Accessories, Shields, Tattos, SpecialItems subfolders.
 */
public class ItemData
{
    private static final CLogger LOGGER = new CLogger(ItemData.class.getName());

    private Item[] _templates;

    protected ItemData()
    {
        load();
    }

    public void reload()
    {
        load();
    }

    private void load()
    {
        final File rootDir = new File("./data/xml/items");

        final Map<Integer, Armor> armors = new HashMap<>();
        final Map<Integer, Weapon> weapons = new HashMap<>();
        final Map<Integer, EtcItem> etcItems = new HashMap<>();

        int filesLoaded = loadFromFolder(rootDir, armors, etcItems, weapons);

        // Descobre o maior itemId
        int highest = 0;
        for (Item item : armors.values()) if (item.getItemId() > highest) highest = item.getItemId();
        for (Item item : weapons.values()) if (item.getItemId() > highest) highest = item.getItemId();
        for (Item item : etcItems.values()) if (item.getItemId() > highest) highest = item.getItemId();

        _templates = new Item[highest + 1];

        for (Armor item : armors.values()) _templates[item.getItemId()] = item;
        for (Weapon item : weapons.values()) _templates[item.getItemId()] = item;
        for (EtcItem item : etcItems.values()) _templates[item.getItemId()] = item;

        LOGGER.info("===========================================");
        LOGGER.info("Loaded items summary:");
        LOGGER.info("  • Armors:       " + armors.size());
        LOGGER.info("  • Weapons:      " + weapons.size());
        LOGGER.info("  • EtcItems:     " + etcItems.size());
        LOGGER.info("  • XML Files:    " + filesLoaded);
        LOGGER.info("-------------------------------------------");
        LOGGER.info("  TOTAL:          " + (armors.size() + weapons.size() + etcItems.size()) + " items.");
        LOGGER.info("===========================================");
    }

    private int loadFromFolder(File folder, Map<Integer, Armor> armors, Map<Integer, EtcItem> etcItems, Map<Integer, Weapon> weapons)
    {
        if (!folder.exists() || !folder.isDirectory())
            return 0;

        int loadedFiles = 0;

        for (File file : folder.listFiles())
        {
            if (file.isDirectory())
            {
                loadedFiles += loadFromFolder(file, armors, etcItems, weapons);
            }
            else if (file.isFile() && file.getName().endsWith(".xml"))
            {
                try
                {
                    DocumentItem document = new DocumentItem(file);
                    document.parse();

                    for (Item item : document.getItemList())
                    {
                        if (item instanceof EtcItem) etcItems.put(item.getItemId(), (EtcItem) item);
                        else if (item instanceof Armor) armors.put(item.getItemId(), (Armor) item);
                        else if (item instanceof Weapon) weapons.put(item.getItemId(), (Weapon) item);
                    }

                    loadedFiles++;
                }
                catch (Exception e)
                {
                    LOGGER.warn("Failed to load file: " + file.getPath() + " -> " + e.getMessage());
                }
            }
        }

        return loadedFiles;
    }

    public Item getTemplate(int id)
    {
        return (id >= _templates.length) ? null : _templates[id];
    }

    public Item[] getTemplates()
    {
        return _templates;
    }

    public static ItemData getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder
    {
        protected static final ItemData INSTANCE = new ItemData();
    }
}
