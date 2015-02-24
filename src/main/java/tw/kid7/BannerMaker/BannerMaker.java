package tw.kid7.BannerMaker;

import com.google.common.collect.Maps;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import tw.kid7.BannerMaker.listener.InventoryClickEventListener;

import java.util.HashMap;

public class BannerMaker extends JavaPlugin {
    private static BannerMaker instance = null;
    public HashMap<String, State> stateMap = Maps.newHashMap();
    public HashMap<String, Integer> selectedColor = Maps.newHashMap();
    public HashMap<String, ItemStack> currentBanner = Maps.newHashMap();
    public HashMap<String, Boolean> morePatterns = Maps.newHashMap();
    public HashMap<String, Integer> selectedIndex = Maps.newHashMap();
    public HashMap<String, Integer> currentRecipePage = Maps.newHashMap();

    @Override
    public void onEnable() {
        instance = this;
        //指令
        this.getCommand("BannerMaker").setExecutor(new BannerMakerCommandExecutor());
        //Listener
        this.getServer().getPluginManager().registerEvents(new InventoryClickEventListener(), this);
    }

    @Override
    public void onDisable() {

    }

    public static BannerMaker getInstance() {
        return instance;
    }
}