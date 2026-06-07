package com.bytedance.myapplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MockData {

    private static int currentPage = 1;
    private static final int PAGE_SIZE = 6;

    public static List<Ad> getAdsByCategory(String category) {//得到Tab标签所有广告
        List<Ad> allAds = getAllAds();
        List<Ad> filteredAds = new ArrayList<>();
        
        for (Ad ad : allAds) {
            if ("精选".equals(category)) {
                filteredAds.add(ad);
            } else if (ad.getTags().contains(category)) {
                filteredAds.add(ad);
            }
        }
//  随机打乱 filteredAds 这个列表（List）中元素的顺序
        Collections.shuffle(filteredAds);
        return filteredAds;
    }
    public static List<Ad> getAdsByCategorypage(String category) {//得到Tab标签所有广告
        List<Ad> allAds = getAllAds();
        List<Ad> filteredAds = new ArrayList<>();

        for (Ad ad : allAds) {
            if ("精选".equals(category)) {
                filteredAds.add(ad);
            } else if (ad.getTags().contains(category)) {
                filteredAds.add(ad);
            }
        }

        return filteredAds;
    }

    public static List<Ad> getAdsByPage(String category, int page) {

        List<Ad> allAds = getAdsByCategory(category);
        List<Ad> pageAds = new ArrayList<>();
        
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allAds.size());
        
        for (int i = start; i < end; i++) {
            pageAds.add(allAds.get(i));
        }
        
        return pageAds;
    }
    public static List<Ad> getAdsByPage1(String category, int page) {

        List<Ad> allAds = getAdsByCategory(category);
        List<Ad> pageAds = new ArrayList<>();

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allAds.size());

        for (int i = start; i < end; i++) {
            pageAds.add(allAds.get(i));
        }

        return pageAds;
    }

    public static List<Ad> refreshAds(String category) {
        currentPage = 1;
        return getAdsByPage(category, currentPage);
    }
    public static List<Ad> refreshAds1(String category) {
        currentPage = 1;
        return getAdsByPage1(category, currentPage);
    }
    public static List<Ad> loadMoreAds(String category) {
        currentPage++;
        return getAdsByPage(category, currentPage);
    }

    public static int getTotalPages(String category) {
        List<Ad> allAds = getAdsByCategory(category);
        return (int) Math.ceil((double) allAds.size() / PAGE_SIZE);
    }

    public static void resetPage() {
        currentPage = 1;
    }

    public static List<Ad> getAllAds() {
        List<Ad> ads = new ArrayList<>();

        ads.add(new Ad(
                "1",
                "本周限定「燕麦拿铁」：轻甜不腻，满满幸福感ff",
                "燕麦拿铁限时特惠，口感顺滑，低糖更好喝",
                "",
                "拾光咖啡",
                "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400&h=300&fit=crop",
                "big",
                Arrays.asList("美食", "本地探店", "潮流"),
                1280,
                320,
                false,
                false,
                5000,
                890
        ));

        ads.add(new Ad(
                "2",
                "小众轻奢项链：通勤显气质，送礼也不踩雷",
                "精致设计，百搭款式，适合各种场合",
                "",
                "轻奢饰品",
                "drawable://xianglian",
                "small",
                Arrays.asList("轻奢", "通勤", "潮流"),
                856,
                245,
                false,
                false,
                3200,
                567
        ));

        ads.add(new Ad(
                "3",
                "新人体检课：30分钟燃脂训练",
                "科学训练方法，快速燃脂，塑造身材",
                "",
                "城南健身",
                "https://neeko-copilot.bytedance.net/api/text2image?prompt=fitness%20training%20class%20people%20doing%20exercises%20in%20modern%20gym&image_size=portrait_4_3",
                "video",
                "android.resource://com.bytedance.myapplication/raw/gym_video",
                "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&h=300&fit=crop",
                148,
                Arrays.asList("运动", "健身", "本地"),
                2150,
                480,
                false,
                false,
                8000,
                1200
        ));

        ads.add(new Ad(
                "4",
                "低脂轻食碗：高蛋白不饿肚，适合健身餐",
                "营养均衡，健康美味，低卡高纤维",
                "",
                "城市轻食",
                "drawable://jianzhi",
                "big",
                Arrays.asList("美食", "运动", "健康"),
                1560,
                380,
                false,
                false,
                4500,
                780
        ));

        ads.add(new Ad(
                "5",
                "夏季新款连衣裙：优雅气质，显瘦显白",
                "轻薄透气面料，时尚设计，百搭单品",
                "",
                "时尚女装",
                "drawable://lianyiqun",
                "big",
                Arrays.asList("电商", "潮流", "女装"),
                3420,
                890,
                false,
                false,
                12000,
                2100
        ));

        ads.add(new Ad(
                "6",
                "智能手表：运动健康监测，时尚外观",
                "多功能运动模式，心率血氧监测，长续航",
                "",
                "数码好物",
                "drawable://watch",
                "small",
                Arrays.asList("电商", "数码", "运动"),
                2890,
                650,
                false,
                false,
                9500,
                1560
        ));

        ads.add(new Ad(
                "7",
                "网红奶茶店开业：第二杯半价",
                "精选原料，匠心制作，多种口味",
                "",
                "茶颜悦色",
                "drawable://naicha",
                "big",
                Arrays.asList("美食", "本地探店", "优惠"),
                1890,
                420,
                false,
                false,
                6800,
                1050
        ));

        ads.add(new Ad(
                "8",
                "瑜伽课程体验：舒缓身心，提升气质",
                "专业教练指导，零基础可学",
                "",
                "静心瑜伽",
                "https://neeko-copilot.bytedance.net/api/text2image?prompt=yoga%20class%20people%20doing%20yoga%20poses%20in%20bright%20studio&image_size=portrait_4_3",
                "video",
                "android.resource://com.bytedance.myapplication/raw/yoga_video",
                "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=400&h=300&fit=crop",
                125,
                Arrays.asList("运动", "健康", "本地"),
                980,
                230,
                false,
                false,
                3800,
                590
        ));

        ads.add(new Ad(
                "9",
                "无线蓝牙耳机：降噪沉浸，超长续航",
                "主动降噪技术，Hi-Fi音质，30小时续航",
                "",
                "数码好物",
                "drawable://lanya",
                "small",
                Arrays.asList("电商", "数码", "潮流"),
                3280,
                720,
                false,
                false,
                15000,
                2300
        ));

        ads.add(new Ad(
                "10",
                "手工甜品蛋糕：生日聚会首选",
                "新鲜食材，精致工艺，多种口味",
                "",
                "甜蜜时光",
                "drawable://cake",
                "big",
                Arrays.asList("美食", "本地探店", "甜点"),
                2150,
                450,
                false,
                false,
                7200,
                1100
        ));

        ads.add(new Ad(
                "11",
                "休闲皮鞋：舒适百搭，商务休闲",
                "真皮材质，透气舒适，经典款式",
                "",
                "鞋行",
                "drawable://nvxie",
                "small",
                Arrays.asList("电商", "女装", "通勤"),
                1890,
                380,
                false,
                false,
                6500,
                980
        ));

        ads.add(new Ad(
                "12",
                "亲子乐园门票：周末遛娃好去处",
                "多种游乐设施，安全环境，亲子互动",
                "",
                "欢乐天地",
                "https://neeko-copilot.bytedance.net/api/text2image?prompt=children%20amusement%20park%20colorful%20playground%20with%20happy%20kids&image_size=portrait_4_3",
                "video",
                "android.resource://com.bytedance.myapplication/raw/playground_video",
                "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=300&fit=crop",
                96,
                Arrays.asList("本地", "亲子", "娱乐"),
                3560,
                890,
                false,
                false,
                12000,
                1800
        ));

        ads.add(new Ad(
                "13",
                "防晒喷雾：清爽不油腻，持久防护",
                "SPF50+高倍防晒，清爽质地，防水防汗",
                "",
                "美妆优选",
                "drawable://fangshai",
                "small",
                Arrays.asList("电商", "美妆", "夏季"),
                1250,
                280,
                false,
                false,
                8900,
                1400
        ));

        ads.add(new Ad(
                "14",
                "日式料理自助：无限畅吃，新鲜食材",
                "刺身、寿司、铁板烧，应有尽有",
                "",
                "和风料理",
                "drawable://rishi",
                "big",
                Arrays.asList("美食", "本地探店", "日料"),
                4280,
                950,
                false,
                false,
                15000,
                2300
        ));

        ads.add(new Ad(
                "15",
                "便携充电宝：超大容量，轻薄便携",
                "20000mAh大容量，支持快充，小巧便携",
                "",
                "数码好物",
                "drawable://chongdian",
                "small",
                Arrays.asList("电商", "数码", "出行"),
                1560,
                340,
                false,
                false,
                7800,
                1200
        ));

        ads.add(new Ad(
                "16",
                "街舞体验课：释放活力，展现自我",
                "专业导师，零基础教学，氛围热烈",
                "",
                "舞力全开",
                "https://neeko-copilot.bytedance.net/api/text2image?prompt=hip%20hop%20dance%20class%20young%20people%20dancing%20in%20studio&image_size=portrait_4_3",
                "video",
                "android.resource://com.bytedance.myapplication/raw/dance_video",
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400&h=300&fit=crop",
                156,
                Arrays.asList("运动", "本地", "潮流"),
                1680,
                390,
                false,
                false,
                5600,
                850
        ));

        ads.add(new Ad(
                "17",
                "复古墨镜：时尚百搭，防晒必备",
                "经典款式，UV400防护，多种颜色",
                "",
                "时尚配饰",
                "drawable://mojing",
                "small",
                Arrays.asList("电商", "潮流", "夏季"),
                980,
                220,
                false,
                false,
                6200,
                950
        ));

        ads.add(new Ad(
                "18",
                "海鲜大排档：新鲜直送，实惠美味",
                "当日海鲜，现点现做，价格亲民",
                "",
                "渔人码头",
                "drawable://haixian",
                "big",
                Arrays.asList("美食", "本地探店", "海鲜"),
                2890,
                650,
                false,
                false,
                9800,
                1500
        ));

        ads.add(new Ad(
                "19",
                "家用空气净化器：静音高效，智能控制",
                "HEPA滤网，智能感应，低噪音运行",
                "",
                "智能家居",
                "drawable://jinghua",
                "big",
                Arrays.asList("电商", "家居", "健康"),
                5680,
                1200,
                false,
                false,
                18000,
                2700
        ));

        ads.add(new Ad(
                "20",
                "户外帐篷30：防风防雨，轻便易携",
                "防水面料，快速搭建，四季可用",
                "",
                "户外装备",
                "drawable://zhangpeng",
                "big",
                Arrays.asList("电商", "户外", "运动"),
                2350,
                520,
                false,
                false,
                8500,
                1300
        ));

        ads.add(new Ad(
                "21",
                "护肤套装：补水保湿，提亮肤色",
                "多种肤质适用，温和不刺激，效果明显",
                "",
                "美妆优选",
                "drawable://hufu",
                "small",
                Arrays.asList("电商", "美妆", "护肤"),
                3180,
                720,
                false,
                false,
                12000,
                1800
        ));

        ads.add(new Ad(
                "22",
                "宠物美容服务：专业护理，爱心呵护",
                "洗澡美容，毛发护理，健康检查",
                "",
                "萌宠乐园",
                "https://neeko-copilot.bytedance.net/api/text2image?prompt=cute%20dog%20being%20groomed%20at%20pet%20salon%20professional%20care&image_size=portrait_4_3",
                "video",
                "android.resource://com.bytedance.myapplication/raw/pet_video",
                "https://images.unsplash.com/photo-1518717758536-85ae29004428?w=400&h=300&fit=crop",
                112,
                Arrays.asList("本地", "宠物", "服务"),
                1680,
                380,
                false,
                false,
                4500,
                680
        ));

        ads.add(new Ad(
                "23",
                "有机水果礼盒：新鲜直达，健康美味",
                "精选有机水果，产地直发，品质保证",
                "",
                "鲜果速递",
                "drawable://fruit",
                "big",
                Arrays.asList("美食", "健康", "电商"),
                1980,
                450,
                false,
                false,
                6500,
                980
        ));

        ads.add(new Ad(
                "24",
                "便携式咖啡机：随时随地享受现磨咖啡",
                "一键萃取，小巧便携，15bar高压",
                "",
                "咖啡器具",
                "drawable://coffe",
                "small",
                Arrays.asList("电商", "数码", "美食"),
                2680,
                620,
                false,
                false,
                8900,
                1350
        ));

        return ads;
    }

    public static List<Ad> searchAds(String query) {
        return searchAds(query, "精选");
    }

    public static List<Ad> searchAds(String query, String category) {
        List<Ad> allAds = getAllAds();
        List<Ad> results = new ArrayList<>();
        
        query = query.toLowerCase();
        
        for (Ad ad : allAds) {
            boolean categoryMatches = "精选".equals(category) || ad.getTags().contains(category);
            
            boolean keywordMatches = ad.getTitle().toLowerCase().contains(query)
                    || ad.getDescription().toLowerCase().contains(query)
                    || ad.getAiSummary().toLowerCase().contains(query)
                    || ad.getSource().toLowerCase().contains(query);
            
            for (String tag : ad.getTags()) {
                if (tag.toLowerCase().contains(query)) {
                    keywordMatches = true;
                    break;
                }
            }
            
            if (categoryMatches && keywordMatches) {
                results.add(ad);
            }
        }
        
        return results;
    }
}
