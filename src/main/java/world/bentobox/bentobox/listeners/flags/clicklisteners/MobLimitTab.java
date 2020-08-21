/**
 *
 */
package world.bentobox.bentobox.listeners.flags.clicklisteners;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.ClickType;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelItem.ClickHandler;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.Tab;
import world.bentobox.bentobox.api.panels.TabbedPanel;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
public class MobLimitTab implements Tab, ClickHandler {

    private BentoBox plugin = BentoBox.getInstance();
    private User user;

    /**
     * @param user
     */
    public MobLimitTab(User user) {
        super();
        this.user = user;
    }

    /**
     * A list of all living entity types, minus some
     */
    private static final List<EntityType> LIVING_ENTITY_TYPES = Collections.unmodifiableList(Arrays.stream(EntityType.values())
            .filter(EntityType::isAlive)
            .filter(t -> !(t.equals(EntityType.PLAYER) || t.equals(EntityType.GIANT) || t.equals(EntityType.ARMOR_STAND)))
            .sorted(Comparator.comparing(EntityType::name))
            .collect(Collectors.toList()));

    @Override
    public boolean onClick(Panel panel, User user, ClickType clickType, int slot) {
        // This is a click on the mob limit panel
        // Case panel to Tabbed Panel to get the active page
        TabbedPanel tp = (TabbedPanel)panel;
        // Conver the slot and active page to an index
        int index = tp.getActivePage() * 36 + slot - 9;
        EntityType c = LIVING_ENTITY_TYPES.get(index);
        if (plugin.getIWM().getMobLimitSettings(user.getWorld()).contains(c.name())) {
            plugin.getIWM().getMobLimitSettings(user.getWorld()).remove(c.name());
        } else {
            plugin.getIWM().getMobLimitSettings(user.getWorld()).add(c.name());
        }
        // Apply change to panel
        panel.getInventory().setItem(slot, getPanelItem(c, user).getItem());
        // Save settings
        plugin.getIWM().getAddon(Util.getWorld(user.getWorld())).ifPresent(GameModeAddon::saveWorldSettings);
        return true;
    }

    @Override
    public PanelItem getIcon() {
        return new PanelItemBuilder().icon(Material.IRON_BOOTS).name(user.getTranslation("protection.flags.LIMIT_MOBS.name")).build();

    }

    @Override
    public String getName() {
        return user.getTranslation("protection.flags.LIMIT_MOBS.name");
    }

    @Override
    public List<@Nullable PanelItem> getPanelItems() {
        // Make panel items
        return LIVING_ENTITY_TYPES.stream().map(c -> getPanelItem(c, user)).collect(Collectors.toList());
    }

    @Override
    public String getPermission() {
        return "";
    }

    private PanelItem getPanelItem(EntityType c, User user) {
        PanelItemBuilder pib = new PanelItemBuilder();
        pib.name(Util.prettifyText(c.toString()));
        pib.clickHandler(this);
        if (!BentoBox.getInstance().getIWM().getMobLimitSettings(user.getWorld()).contains(c.name())) {
            pib.icon(Material.GREEN_SHULKER_BOX);
            pib.description(user.getTranslation("protection.flags.LIMIT_MOBS.can"));
        } else {
            pib.icon(Material.RED_SHULKER_BOX);
            pib.description(user.getTranslation("protection.flags.LIMIT_MOBS.cannot"));
        }
        return pib.build();
    }

}
