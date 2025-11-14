package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.item.kind.EtcItem;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.special.ItemClan;

public class ItemHandler extends AbstractHandler<Integer, IItemHandler>
{
    protected ItemHandler()
    {
        super(IItemHandler.class, "itemhandlers");

        // Registro de handlers custom
        registerHandler(new ItemClan());
    }

    @Override
    protected void registerHandler(IItemHandler handler)
    {
        if (handler == null)
            return;
        _entries.put(handler.getClass().getSimpleName().intern().hashCode(), handler);
    }

    @Override
    public IItemHandler getHandler(Object key)
    {
        if (!(key instanceof EtcItem etcItem))
            return null;

        String handlerName = etcItem.getHandlerName();
        if (handlerName == null)
            return null;

        return super.getHandler(handlerName.hashCode());
    }

    public static ItemHandler getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder
    {
        protected static final ItemHandler INSTANCE = new ItemHandler();
    }
}
