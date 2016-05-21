package tw.kid7.BannerMaker.util;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import tw.kid7.BannerMaker.BannerMaker;
import tw.kid7.BannerMaker.State;
import tw.kid7.BannerMaker.configuration.ConfigManager;
import tw.kid7.BannerMaker.configuration.Language;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static tw.kid7.BannerMaker.State.MAIN_MENU;

public class InventoryMenuUtil {

    static public void openMenu(Player player) {
        //取得玩家狀態
        State state = MAIN_MENU;
        if (BannerMaker.getInstance().stateMap.containsKey(player.getName())) {
            state = BannerMaker.getInstance().stateMap.get(player.getName());
        }
        //根據狀態決定行為
        switch (state) {
            case CREATE_BANNER:
                openCreateBanner(player);
                break;
            case CREATE_ALPHABET:
                openCreateAlphabet(player);
                break;
            case BANNER_INFO:
                openBannerInfo(player);
                break;
            case MAIN_MENU:
            default:
                openMainMenu(player);
        }

    }

    private static void openMainMenu(Player player) {
        //建立選單
        Inventory menu = Bukkit.createInventory(null, 54, MessageUtil.format("&b&m&r" + Language.get("gui.prefix") + Language.get("gui.main-menu")));
        //當前頁數
        int currentBannerPage = 1;
        if (BannerMaker.getInstance().currentBannerPage.containsKey(player.getName())) {
            currentBannerPage = BannerMaker.getInstance().currentBannerPage.get(player.getName());
        } else {
            BannerMaker.getInstance().currentBannerPage.put(player.getName(), 1);
        }
        //顯示現有旗幟
        List<ItemStack> bannerList = IOUtil.loadBannerList(player, currentBannerPage);
        for (int i = 0; i < bannerList.size() && i < 45; i++) {
            ItemStack banner = bannerList.get(i);
            menu.setItem(i, banner);
        }
        //總頁數
        int totalPage = (int) Math.ceil(IOUtil.getBannerCount(player) / 45.0);
        //新增按鈕
        //換頁按鈕
        //上一頁
        if (currentBannerPage > 1) {
            ItemStack prevPage = new ItemBuilder(Material.ARROW).amount(currentBannerPage - 1).name(MessageUtil.format("&a" + Language.get("gui.prev-page"))).build();
            menu.setItem(45, prevPage);
        }
        //下一頁
        if (currentBannerPage < totalPage) {
            ItemStack nextPage = new ItemBuilder(Material.ARROW).amount(currentBannerPage + 1).name(MessageUtil.format("&a" + Language.get("gui.next-page"))).build();
            menu.setItem(53, nextPage);
        }
        //Create banner
        ItemStack btnCreateBanner = new ItemBuilder(Material.WOOL).amount(1).durability(5).name(MessageUtil.format("&a" + Language.get("gui.create-banner"))).build();
        menu.setItem(49, btnCreateBanner);
        //建立字母
        ItemStack btnCreateAlphabet = AlphabetBanner.get("A");
        ItemMeta btnCreateAlphabetItemMeta = btnCreateAlphabet.getItemMeta();
        btnCreateAlphabetItemMeta.setDisplayName(MessageUtil.format("&a" + Language.get("gui.alphabet-and-number")));
        btnCreateAlphabet.setItemMeta(btnCreateAlphabetItemMeta);
        menu.setItem(51, btnCreateAlphabet);
        //開啟選單
        player.openInventory(menu);
    }

    private static void openCreateBanner(Player player) {
        //建立選單
        Inventory menu = Bukkit.createInventory(null, 54, MessageUtil.format("&b&m&r" + Language.get("gui.prefix") + Language.get("gui.create-banner")));
        //取得當前編輯中的旗幟
        ItemStack currentBanner = BannerMaker.getInstance().currentBanner.get(player.getName());
        if (currentBanner == null) {
            //剛開始編輯，先選擇底色
            for (int i = 0; i < 16; i++) {
                ItemStack banner = new ItemStack(Material.BANNER, 1, (short) i);
                menu.setItem(i + 1 + (i / 8), banner);
            }
        } else {
            //新增按鈕
            //當前旗幟
            menu.setItem(0, currentBanner);
            //patterns過多的警告
            if (currentBanner.hasItemMeta() && ((BannerMeta) currentBanner.getItemMeta()).numberOfPatterns() > 6) {
                ItemStack warning = new ItemBuilder(Material.SIGN).amount(1).name(MessageUtil.format("&c" + Language.get("gui.uncraftable-warning")))
                    .lore(Language.get("gui.more-than-6-patterns")).build();
                menu.setItem(9, warning);
            }
            //顏色
            for (int i = 0; i < 16; i++) {
                ItemStack dye = new ItemBuilder(Material.INK_SACK).amount(1).durability(i).build();
                menu.setItem(i + 1 + (i / 8), dye);
            }
            //Pattern
            //選擇的顏色
            int selectedColor = 0;
            if (BannerMaker.getInstance().selectedColor.containsKey(player.getName())) {
                selectedColor = BannerMaker.getInstance().selectedColor.get(player.getName());
            }
            for (int i = 0; i < 24; i++) {
                int patternIndex = i;
                if (BannerMaker.getInstance().morePatterns.containsKey(player.getName())) {
                    if (BannerMaker.getInstance().morePatterns.get(player.getName())) {
                        patternIndex += 24;
                    }
                }
                if (patternIndex >= getPatternTypeList().size()) {
                    break;
                }
                //預覽旗幟
                ItemStack banner = new ItemStack(Material.BANNER, 1, currentBanner.getDurability());
                BannerMeta bm = (BannerMeta) banner.getItemMeta();
                PatternType patternType = getPatternTypeList().get(patternIndex);
                bm.addPattern(new Pattern(DyeColor.getByDyeData((byte) selectedColor), patternType));
                banner.setItemMeta(bm);

                menu.setItem(i + 19 + (i / 8), banner);
            }
            //更多Pattern
            ItemStack btnMorePattern = new ItemBuilder(Material.NETHER_STAR).amount(1).name(MessageUtil.format("&a" + Language.get("gui.more-patterns"))).build();
            menu.setItem(51, btnMorePattern);
        }
        //返回
        ItemStack btnBackToMenu = new ItemBuilder(Material.WOOL).amount(1).durability(14).name(MessageUtil.format("&c" + Language.get("gui.back"))).build();
        menu.setItem(45, btnBackToMenu);
        if (currentBanner != null) {
            //建立旗幟
            ItemStack btnCreate = new ItemBuilder(Material.WOOL).amount(1).durability(5).name(MessageUtil.format("&a" + Language.get("gui.create"))).build();
            menu.setItem(53, btnCreate);
            //刪除
            ItemStack btnDelete = new ItemBuilder(Material.BARRIER).amount(1).name(MessageUtil.format("&c" + Language.get("gui.delete"))).build();
            menu.setItem(47, btnDelete);
            if (currentBanner.hasItemMeta() && ((BannerMeta) currentBanner.getItemMeta()).numberOfPatterns() > 0) {
                //移除Pattern
                ItemStack btnRemovePattern = new ItemBuilder(Material.BARRIER).amount(1).name(MessageUtil.format("&c" + Language.get("gui.remove-last-pattern"))).build();
                menu.setItem(49, btnRemovePattern);
            }
        }
        //開啟選單
        player.openInventory(menu);
    }

    private static void openCreateAlphabet(Player player) {
        //建立選單
        Inventory menu = Bukkit.createInventory(null, 54, MessageUtil.format("&b&m&r" + Language.get("gui.prefix") + Language.get("gui.alphabet-and-number")));
        //取得當前編輯中的字母
        AlphabetBanner currentAlphabetBanner = BannerMaker.getInstance().currentAlphabetBanner.get(player.getName());
        //邊框切換按鈕
        ItemStack btnBorderedBanner = new ItemStack(Material.BANNER, 1, (short) 15);
        BannerMeta borderedBannerMeta = (BannerMeta) btnBorderedBanner.getItemMeta();
        borderedBannerMeta.setDisplayName(MessageUtil.format("&a" + Language.get("gui.toggle-border")));
        borderedBannerMeta.addPattern(new Pattern(DyeColor.BLACK, PatternType.BORDER));
        btnBorderedBanner.setItemMeta(borderedBannerMeta);
        if (currentAlphabetBanner == null) {
            //選擇字母
            boolean alphabetBorder = true;
            if (BannerMaker.getInstance().alphabetBorder.containsKey(player.getName())) {
                alphabetBorder = BannerMaker.getInstance().alphabetBorder.get(player.getName());
            }
            char[] alphabetArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789?!.".toCharArray();
            for (int i = 0; i < alphabetArray.length && i < 54; i++) {
                char alphabet = alphabetArray[i];
                ItemStack alphabetItem = AlphabetBanner.get(String.valueOf(alphabet), DyeColor.WHITE, DyeColor.BLACK, alphabetBorder);
                menu.setItem(i, alphabetItem);
            }
            //切換有無邊框
            menu.setItem(49, btnBorderedBanner);
        } else {
            //選擇顏色
            menu.setItem(0, currentAlphabetBanner.toItemStack());
            //選擇底色
            for (int i = 0; i < 16; i++) {
                ItemStack banner = new ItemStack(Material.BANNER, 1, (short) i);
                menu.setItem(i + 1 + (i / 8), banner);
            }
            //選擇主要顏色
            for (int i = 0; i < 16; i++) {
                ItemStack dye = new ItemBuilder(Material.INK_SACK).amount(1).durability(i).build();
                menu.setItem(18 + i + 1 + (i / 8), dye);
            }
            //切換有無邊框
            menu.setItem(37, btnBorderedBanner);
            //檢視旗幟資訊按鈕
            ItemStack btnBannerInfo = new ItemBuilder(Material.WOOL).amount(1).durability(5).name(MessageUtil.format("&a" + Language.get("gui.banner-info"))).build();
            menu.setItem(49, btnBannerInfo);
        }
        //返回
        ItemStack btnBackToMenu = new ItemBuilder(Material.WOOL).amount(1).durability(14).name(MessageUtil.format("&c" + Language.get("gui.back"))).build();
        menu.setItem(45, btnBackToMenu);
        //開啟選單
        player.openInventory(menu);
    }

    private static void openBannerInfo(Player player) {
        //取得欲查看旗幟
        ItemStack banner = BannerMaker.getInstance().viewInfoBanner.get(player.getName());
        //僅限旗幟
        if (!BannerUtil.isBanner(banner)) {
            //回到主選單
            BannerMaker.getInstance().stateMap.put(player.getName(), State.MAIN_MENU);
            //重新開啟選單
            InventoryMenuUtil.openMenu(player);
            return;
        }
        //建立選單
        Inventory menu = Bukkit.createInventory(null, 54, MessageUtil.format("&b&m&r" + Language.get("gui.prefix") + Language.get("gui.banner-info")));
        menu.setItem(0, banner);
        //patterns數量
        int patternCount = ((BannerMeta) banner.getItemMeta()).numberOfPatterns();
        String patternCountStr = "";
        if (patternCount > 0) {
            patternCountStr = patternCount + " " + Language.get("gui.pattern-s");
        } else {
            patternCountStr = Language.get("gui.no-patterns");
        }
        ItemStack signPatternCount;
        if (patternCount <= 6) {
            signPatternCount = new ItemBuilder(Material.SIGN).amount(1).name(MessageUtil.format("&a" + patternCountStr)).build();
        } else {
            signPatternCount = new ItemBuilder(Material.SIGN).amount(1).name(MessageUtil.format("&a" + patternCountStr)).lore(MessageUtil.format("&c" + Language.get("gui.uncraftable"))).build();
        }
        menu.setItem(1, signPatternCount);
        if (patternCount <= 6) {
            //材料清單
            List<Integer> materialPosition = Arrays.asList(9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39);
            List<ItemStack> materialList = BannerUtil.getMaterials(banner);
            for (int i = 0; i < materialList.size() && i < materialPosition.size(); i++) {
                ItemStack materialItem = materialList.get(i);
                int position = materialPosition.get(i);
                menu.setItem(position, materialItem);
            }

            //合成表
            //當前頁數
            int currentRecipePage = BannerMaker.getInstance().currentRecipePage.get(player.getName());
            //總頁數
            int totalPage = patternCount + 1;
            //外框
            ItemStack workbench = new ItemBuilder(Material.WORKBENCH).amount(currentRecipePage).name(MessageUtil.format("&a" + Language.get("gui.craft-recipe")))
                .lore(MessageUtil.format("(" + currentRecipePage + "/" + totalPage + ")")).build();
            menu.setItem(6, workbench);
            ItemStack border = new ItemBuilder(Material.STAINED_GLASS_PANE).amount(1).durability(12).name(" ").build();
            List<Integer> borderPosition = Arrays.asList(4, 5, 7, 8, 13, 17, 22, 26, 31, 35, 40, 41, 42, 43, 44);
            for (int i : borderPosition) {
                menu.setItem(i, border.clone());
            }
            //換頁按鈕
            //上一頁
            if (currentRecipePage > 1) {
                ItemStack prevPage = new ItemBuilder(Material.ARROW).amount(currentRecipePage - 1).name(MessageUtil.format("&a" + Language.get("gui.prev-page"))).build();
                menu.setItem(22, prevPage);
            }
            //下一頁
            if (currentRecipePage < totalPage) {
                ItemStack nextPage = new ItemBuilder(Material.ARROW).amount(currentRecipePage + 1).name(MessageUtil.format("&a" + Language.get("gui.next-page"))).build();
                menu.setItem(26, nextPage);
            }
            //合成表
            HashMap<Integer, ItemStack> patternRecipe = getPatternRecipe(banner, currentRecipePage);
            List<Integer> craftPosition = Arrays.asList(14, 15, 16, 23, 24, 25, 32, 33, 34, 42);
            for (int i = 0; i < 10; i++) {
                int position = craftPosition.get(i);
                ItemStack itemStack = patternRecipe.get(i);
                menu.setItem(position, itemStack);
            }
        }
        //新增按鈕
        //嘗試取德key
        String key = BannerUtil.getKey(banner);
        //刪除
        if (key != null) {
            //有KEY時（儲存於玩家資料時），才顯示刪除按鈕
            ItemStack btnDelete = new ItemBuilder(Material.BARRIER).amount(1).name(MessageUtil.format("&c" + Language.get("gui.delete"))).build();
            menu.setItem(47, btnDelete);
        }
        //取得旗幟
        if (player.hasPermission("BannerMaker.getBanner")) {
            //檢查是否啟用經濟
            ItemBuilder btnGetBannerBuilder = new ItemBuilder(Material.WOOL).amount(1).durability(5).name(MessageUtil.format("&a" + Language.get("gui.get-this-banner")));
            if (BannerMaker.econ != null) {
                FileConfiguration config = ConfigManager.get("config.yml");
                Double price = config.getDouble("Economy.Price", 100);
                //FIXME 可能造成 IndexOutOfBoundsException: No group 1
                btnGetBannerBuilder.lore(MessageUtil.format("&a" + Language.get("gui.price", BannerMaker.econ.format(price))));
            }
            ItemStack btnGetBanner = btnGetBannerBuilder.build();
            menu.setItem(49, btnGetBanner);
        }
        //複製並編輯
        ItemStack btnCloneAndEdit = new ItemBuilder(Material.BOOK_AND_QUILL).amount(1).name(MessageUtil.format("&9" + Language.get("gui.clone-and-edit"))).build();
        menu.setItem(51, btnCloneAndEdit);

        //TODO 產生指令
        //返回
        ItemStack btnBackToMenu = new ItemBuilder(Material.WOOL).amount(1).durability(14).name(MessageUtil.format("&c" + Language.get("gui.back"))).build();
        menu.setItem(45, btnBackToMenu);
        //開啟選單
        player.openInventory(menu);
    }

    static private List<PatternType> getPatternTypeList() {
        List<PatternType> list = Arrays.asList(
            PatternType.BORDER,
            PatternType.BRICKS,
            PatternType.CIRCLE_MIDDLE,
            PatternType.CREEPER,
            PatternType.CROSS,
            PatternType.CURLY_BORDER,
            PatternType.DIAGONAL_LEFT,
            PatternType.DIAGONAL_LEFT_MIRROR,
            PatternType.DIAGONAL_RIGHT,
            PatternType.DIAGONAL_RIGHT_MIRROR,
            PatternType.FLOWER,
            PatternType.GRADIENT,
            PatternType.GRADIENT_UP,
            PatternType.HALF_HORIZONTAL,
            PatternType.HALF_HORIZONTAL_MIRROR,
            PatternType.HALF_VERTICAL,
            PatternType.HALF_VERTICAL_MIRROR,
            PatternType.MOJANG,
            PatternType.RHOMBUS_MIDDLE,
            PatternType.SKULL,
            PatternType.SQUARE_BOTTOM_LEFT,
            PatternType.SQUARE_BOTTOM_RIGHT,
            PatternType.SQUARE_TOP_LEFT,
            PatternType.SQUARE_TOP_RIGHT,
            PatternType.STRAIGHT_CROSS,
            PatternType.STRIPE_BOTTOM,
            PatternType.STRIPE_CENTER,
            PatternType.STRIPE_DOWNLEFT,
            PatternType.STRIPE_DOWNRIGHT,
            PatternType.STRIPE_LEFT,
            PatternType.STRIPE_MIDDLE,
            PatternType.STRIPE_RIGHT,
            PatternType.STRIPE_SMALL,
            PatternType.STRIPE_TOP,
            PatternType.TRIANGLE_BOTTOM,
            PatternType.TRIANGLE_TOP,
            PatternType.TRIANGLES_BOTTOM,
            PatternType.TRIANGLES_TOP
        );
        return list;
    }

    private static HashMap<Integer, ItemStack> getPatternRecipe(final ItemStack banner, int step) {
        HashMap<Integer, ItemStack> recipe = Maps.newHashMap();
        //填滿空氣
        for (int i = 0; i < 10; i++) {
            recipe.put(i, new ItemStack(Material.AIR));
        }
        //只處理旗幟
        if (banner != null && banner.getType().equals(Material.BANNER)) {
            BannerMeta bm = (BannerMeta) banner.getItemMeta();
            int totalStep = bm.numberOfPatterns() + 1;
            if (step == 1) {
                //第一步，旗幟合成
                //顏色
                int color = 15 - banner.getDurability();
                //羊毛
                ItemStack wool = new ItemStack(Material.WOOL, 1, (short) color);
                for (int i = 0; i < 6; i++) {
                    recipe.put(i, wool.clone());
                }
                //木棒
                ItemStack stick = new ItemStack(Material.STICK);
                recipe.put(7, stick);
            } else if (step <= totalStep) {
                //新增Pattern
                //當前banner
                ItemStack prevBanner = new ItemStack(Material.BANNER, 1, banner.getDurability());
                BannerMeta pbm = (BannerMeta) prevBanner.getItemMeta();
                //新增至目前的Pattern
                for (int i = 0; i < step - 2; i++) {
                    pbm.addPattern(bm.getPattern(i));
                }
                prevBanner.setItemMeta(pbm);
                //當前Pattern
                Pattern pattern = bm.getPattern(step - 2);
                //所需染料
                Dye dye = new Dye();
                dye.setColor(pattern.getColor());
                ItemStack dyeItem = dye.toItemStack(1);
                //旗幟位置
                int bannerPosition = 4;
                //染料位置
                List<Integer> dyePosition = Collections.emptyList();
                //根據Pattern決定位置
                switch (pattern.getPattern()) {
                    case SQUARE_BOTTOM_LEFT:
                        dyePosition = Collections.singletonList(6);
                        break;
                    case SQUARE_BOTTOM_RIGHT:
                        dyePosition = Collections.singletonList(8);
                        break;
                    case SQUARE_TOP_LEFT:
                        dyePosition = Collections.singletonList(0);
                        break;
                    case SQUARE_TOP_RIGHT:
                        dyePosition = Collections.singletonList(2);
                        break;
                    case STRIPE_BOTTOM:
                        dyePosition = Arrays.asList(6, 7, 8);
                        break;
                    case STRIPE_TOP:
                        dyePosition = Arrays.asList(0, 1, 2);
                        break;
                    case STRIPE_LEFT:
                        dyePosition = Arrays.asList(0, 3, 6);
                        break;
                    case STRIPE_RIGHT:
                        dyePosition = Arrays.asList(2, 5, 8);
                        break;
                    case STRIPE_CENTER:
                        bannerPosition = 3;
                        dyePosition = Arrays.asList(1, 4, 7);
                        break;
                    case STRIPE_MIDDLE:
                        bannerPosition = 1;
                        dyePosition = Arrays.asList(3, 4, 5);
                        break;
                    case STRIPE_DOWNRIGHT:
                        bannerPosition = 1;
                        dyePosition = Arrays.asList(0, 4, 8);
                        break;
                    case STRIPE_DOWNLEFT:
                        bannerPosition = 1;
                        dyePosition = Arrays.asList(2, 4, 6);
                        break;
                    case STRIPE_SMALL:
                        dyePosition = Arrays.asList(0, 2, 3, 5);
                        break;
                    case CROSS:
                        bannerPosition = 1;
                        dyePosition = Arrays.asList(0, 2, 4, 6, 8);
                        break;
                    case STRAIGHT_CROSS:
                        bannerPosition = 0;
                        dyePosition = Arrays.asList(1, 3, 4, 5, 7);
                        break;
                    case TRIANGLE_BOTTOM:
                        bannerPosition = 7;
                        dyePosition = Arrays.asList(4, 6, 8);
                        break;
                    case TRIANGLE_TOP:
                        bannerPosition = 1;
                        dyePosition = Arrays.asList(0, 2, 4);
                        break;
                    case TRIANGLES_BOTTOM:
                        dyePosition = Arrays.asList(3, 5, 7);
                        break;
                    case TRIANGLES_TOP:
                        dyePosition = Arrays.asList(1, 3, 5);
                        break;
                    case DIAGONAL_LEFT:
                        dyePosition = Arrays.asList(0, 1, 3);
                        break;
                    case DIAGONAL_RIGHT:
                        dyePosition = Arrays.asList(5, 7, 8);
                        break;
                    case DIAGONAL_LEFT_MIRROR:
                        dyePosition = Arrays.asList(3, 6, 7);
                        break;
                    case DIAGONAL_RIGHT_MIRROR:
                        dyePosition = Arrays.asList(1, 2, 5);
                        break;
                    case CIRCLE_MIDDLE:
                        bannerPosition = 1;
                        dyePosition = Collections.singletonList(4);
                        break;
                    case RHOMBUS_MIDDLE:
                        dyePosition = Arrays.asList(1, 3, 5, 7);
                        break;
                    case HALF_VERTICAL:
                        bannerPosition = 5;
                        dyePosition = Arrays.asList(0, 1, 3, 4, 6, 7);
                        break;
                    case HALF_HORIZONTAL:
                        bannerPosition = 7;
                        dyePosition = Arrays.asList(0, 1, 2, 3, 4, 5);
                        break;
                    case HALF_VERTICAL_MIRROR:
                        bannerPosition = 3;
                        dyePosition = Arrays.asList(1, 2, 4, 5, 7, 8);
                        break;
                    case HALF_HORIZONTAL_MIRROR:
                        bannerPosition = 1;
                        dyePosition = Arrays.asList(3, 4, 5, 6, 7, 8);
                        break;
                    case BORDER:
                        dyePosition = Arrays.asList(0, 1, 2, 3, 5, 6, 7, 8);
                        break;
                    case CURLY_BORDER:
                        recipe.put(1, new ItemStack(Material.VINE));
                        if (!pattern.getColor().equals(DyeColor.BLACK)) {
                            dyePosition = Collections.singletonList(7);
                        }
                        break;
                    case CREEPER:
                        recipe.put(1, new ItemStack(Material.SKULL_ITEM, 1, (short) 4));
                        if (!pattern.getColor().equals(DyeColor.BLACK)) {
                            dyePosition = Collections.singletonList(7);
                        }
                        break;
                    case GRADIENT:
                        bannerPosition = 1;
                        dyePosition = Arrays.asList(0, 2, 4, 7);
                        break;
                    case GRADIENT_UP:
                        bannerPosition = 7;
                        dyePosition = Arrays.asList(1, 4, 6, 8);
                        break;
                    case BRICKS:
                        recipe.put(1, new ItemStack(Material.BRICK));
                        if (!pattern.getColor().equals(DyeColor.BLACK)) {
                            dyePosition = Collections.singletonList(7);
                        }
                        break;
                    case SKULL:
                        recipe.put(1, new ItemStack(Material.SKULL_ITEM, 1, (short) 1));
                        if (!pattern.getColor().equals(DyeColor.BLACK)) {
                            dyePosition = Collections.singletonList(7);
                        }
                        break;
                    case FLOWER:
                        recipe.put(1, new ItemStack(Material.RED_ROSE, 1, (short) 8));
                        if (!pattern.getColor().equals(DyeColor.BLACK)) {
                            dyePosition = Collections.singletonList(7);
                        }
                        break;
                    case MOJANG:
                        recipe.put(1, new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1));
                        if (!pattern.getColor().equals(DyeColor.BLACK)) {
                            dyePosition = Collections.singletonList(7);
                        }
                        break;
                }
                //放置旗幟與染料
                recipe.put(bannerPosition, prevBanner);
                for (int i : dyePosition) {
                    recipe.put(i, dyeItem.clone());
                }
            }
            //合成結果
            //當前banner
            ItemStack currentBanner = new ItemStack(Material.BANNER, 1, banner.getDurability());
            BannerMeta cbm = (BannerMeta) currentBanner.getItemMeta();
            //新增至目前的Pattern
            for (int i = 0; i < step - 1; i++) {
                cbm.addPattern(bm.getPattern(i));
            }
            currentBanner.setItemMeta(cbm);
            recipe.put(9, currentBanner);
        }
        return recipe;
    }
}