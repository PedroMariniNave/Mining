package com.zpedroo.mining.utils.item;

import org.apache.commons.lang3.Validate;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class ItemUtils {

    private Map<Inventory, HashMap<Integer, Runnable>> actions = new HashMap<>(54);

    public void setItemAction(Inventory inventory, int slot, Runnable action) {
        Validate.notNull(action, "Item action cannot be null!");

        HashMap<Integer, Runnable> invActions;
        if (getInventoryActions(inventory) == null) {
            invActions = new HashMap<>();
        } else {
            invActions = getInventoryActions(inventory);
        }

        invActions.put(slot, action);
        getActions().put(inventory, invActions);
    }

    public HashMap<Integer, Runnable> getInventoryActions(Inventory inventory) {
        return actions.get(inventory);
    }

    public Runnable getAction(Inventory inventory, int slot) {
        return getInventoryActions(inventory).get(slot);
    }

    private Map<Inventory, HashMap<Integer, Runnable>> getActions() {
        return actions;
    }
}