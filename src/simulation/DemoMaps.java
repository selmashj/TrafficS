package simulation; // // پکیج شبیه‌سازی

import infrastructure.CityMap;         // // نقشهٔ شهر
import infrastructure.Intersection;    // // تقاطع
import infrastructure.Road;            // // جاده
import infrastructure.Lane;            // // لِین
import core.Direction;                 // // جهت‌ها
import core.Point;                     // // مختصات صحیح

import java.util.ArrayList;            // // لیست
import java.util.List;                 // // لیست
import java.util.Random;               // // تصادفی

/**
 * تولیدکنندهٔ نقشهٔ شبکه‌ای «کاملاً مستقیم» با طول بلوک‌های متفاوت.
 * سه روش:
 *  1) variableGrid(...)      ← بازهٔ رندومی پیکسلی
 *  2) variableGridUnits(...) ← آرایهٔ واحدها × یک ضریب پیکسلی ثابت
 *  3) gridByRatios(...)      ← آرایهٔ نسبت‌ها + عرض/ارتفاع کل
 *
 * نکته: حداقل طول بلوک‌ها با SimulationConfig.MIN_BLOCK_PX کنترل می‌شود.
 */
public final class DemoMaps { // // یوتیلیتی تولید نقشه
    private DemoMaps() {} // // جلوگیری از نمونه‌سازی

    // روش ۱: رندومی پیکسلی (کاملاً مستقیم)
    public static CityMap variableGrid(int rows, int cols,
                                       int minBlockW, int maxBlockW,
                                       int minBlockH, int maxBlockH,
                                       int lanesPerDir) {
        if (rows < 1) rows = 1; if (cols < 1) cols = 1; // // حداقل‌ها
        if (lanesPerDir < 1) lanesPerDir = 1; if (lanesPerDir > 3) lanesPerDir = 3; // // محدودسازی

        Random rnd = new Random(); // // تصادفی
        CityMap map = new CityMap(); // // نقشه

        // // حداقل طول مجاز بلوک‌ها از تنظیمات (برای جلوگیری از چسبیدن گذرگاه‌ها)
        final int MINB = SimulationConfig.MIN_BLOCK_PX; // // حداقل طول بلوک

        int[] widths  = new int[cols]; // // عرض بلوک‌ها
        int[] heights = new int[rows]; // // ارتفاع بلوک‌ها
        int i; // // شمارنده
        for (i = 0; i < cols; i++) widths[i]  = rndBetween(rnd, Math.max(MINB, minBlockW), Math.max(MINB, maxBlockW)); // // عرض‌ها
        for (i = 0; i < rows; i++) heights[i] = rndBetween(rnd, Math.max(MINB, minBlockH), Math.max(MINB, maxBlockH)); // // ارتفاع‌ها

        int[] xs = accumFrom(100, widths); // // مختصات X ستون‌ها
        int[] ys = accumFrom(100, heights); // // مختصات Y ردیف‌ها

        buildGrid(map, xs, ys, lanesPerDir); // // ساخت شبکهٔ راست
        return map; // // خروجی
    }

    // روش ۲: واحدها × ضریب پیکسلی ثابت (کاملاً مستقیم)
    public static CityMap variableGridUnits(int[] rowUnits, int[] colUnits,
                                            int unitPx, int lanesPerDir) {
        if (rowUnits == null || rowUnits.length < 1) rowUnits = new int[]{10};  // // پیش‌فرض
        if (colUnits == null || colUnits.length < 1) colUnits = new int[]{10};  // // پیش‌فرض
        if (unitPx < 10) unitPx = 10;                                           // // حداقل
        if (lanesPerDir < 1) lanesPerDir = 1; if (lanesPerDir > 3) lanesPerDir = 3;

        final int MINB = SimulationConfig.MIN_BLOCK_PX; // // حداقل طول بلوک

        int[] heights = new int[rowUnits.length]; // // ارتفاع‌ها
        int[] widths  = new int[colUnits.length]; // // عرض‌ها
        int i; // // شمارنده
        for (i=0;i<rowUnits.length;i++){ int u = Math.max(1, rowUnits[i]); heights[i] = Math.max(MINB, u * unitPx); } // // تبدیل ردیف‌ها
        for (i=0;i<colUnits.length;i++){ int u = Math.max(1, colUnits[i]); widths[i]  = Math.max(MINB, u * unitPx); } // // تبدیل ستون‌ها

        CityMap map = new CityMap();                // // نقشه
        int[] xs = accumFrom(100, widths);          // // مختصات X
        int[] ys = accumFrom(100, heights);         // // مختصات Y
        buildGrid(map, xs, ys, lanesPerDir);        // // ساخت شبکهٔ راست
        return map;                                  // // خروجی
    }

    // روش ۳: براساس نسبت‌ها + اندازهٔ کل (کاملاً مستقیم)
    public static CityMap gridByRatios(int[] rowRatios, int[] colRatios,
                                       int totalHeightPx, int totalWidthPx,
                                       int lanesPerDir){
        if (rowRatios == null || rowRatios.length < 1) rowRatios = new int[]{1}; // // پیش‌فرض
        if (colRatios == null || colRatios.length < 1) colRatios = new int[]{1}; // // پیش‌فرض
        if (lanesPerDir < 1) lanesPerDir = 1; if (lanesPerDir > 3) lanesPerDir = 3; // // محدودسازی

        // // مقیاس نقشه (۲×) از تنظیمات
        int H = Math.max(200, totalHeightPx) * SimulationConfig.MAP_SCALE; // // ارتفاع کل × مقیاس
        int W = Math.max(200, totalWidthPx)  * SimulationConfig.MAP_SCALE; // // عرض کل × مقیاس

        final int MINB = SimulationConfig.MIN_BLOCK_PX; // // حداقل طول بلوک

        int rSum = sumPositive(rowRatios); // // مجموع نسبت‌های ردیف
        int cSum = sumPositive(colRatios); // // مجموع نسبت‌های ستون

        int[] heights = new int[rowRatios.length]; // // ارتفاع هر بلوک
        int[] widths  = new int[colRatios.length]; // // عرض هر بلوک
        int i; // // شمارنده
        for (i=0;i<rowRatios.length;i++){ // // محاسبه ارتفاع بلوک‌ها با حداقل
            double frac = (double)Math.max(1,rowRatios[i])/(double)rSum; // // کسر نسبت
            heights[i] = Math.max(MINB, (int)Math.round(frac * H));      // // اعمال حداقل
        }
        for (i=0;i<colRatios.length;i++){ // // محاسبه عرض بلوک‌ها با حداقل
            double frac = (double)Math.max(1,colRatios[i])/(double)cSum;  // // کسر نسبت
            widths[i]  = Math.max(MINB, (int)Math.round(frac * W));       // // اعمال حداقل
        }

        CityMap map = new CityMap();                // // نقشه
        int[] xs = accumFrom(100, widths);          // // مختصات X
        int[] ys = accumFrom(100, heights);         // // مختصات Y
        buildGrid(map, xs, ys, lanesPerDir);        // // ساخت شبکهٔ راست
        return map;                                 // // خروجی
    }

    // ساخت شبکه از روی مختصات تجمعی (کاملاً مستقیم)
    private static void buildGrid(CityMap map, int[] xs, int[] ys, int lanesPerDir){
        int rows = ys.length - 1; // // تعداد بلوک عمودی
        int cols = xs.length - 1; // // تعداد بلوک افقی

        Intersection[][] grid = new Intersection[rows + 1][cols + 1]; // // ماتریس گره‌ها
        int gid = 0; // // شمارندهٔ تقاطع‌ها
        int r, c;   // // ایندکس‌ها

        // ساخت همهٔ تقاطع‌ها
        for (r = 0; r <= rows; r++) {
            for (c = 0; c <= cols; c++) {
                Point p = new Point(xs[c], ys[r]);                 // // مختصات
                Intersection it = new Intersection("I" + (gid++), p); // // تقاطع
                map.addIntersection(it);                           // // افزودن
                grid[r][c] = it;                                   // // ذخیره
            }
        }

        // یال‌های افقی (چپ↔راست)
        for (r = 0; r <= rows; r++) {
            for (c = 0; c < cols; c++) {
                Intersection A = grid[r][c];      // // ابتدا
                Intersection B = grid[r][c + 1];  // // انتها
                Road road = new Road("RH-" + r + "-" + c, A, B, true); // // جادهٔ مستقیم دوطرفه
                addLanesForTwoWay(road, lanesPerDir, true);            // // لِین‌های افقی
                map.addRoad(road);                                     // // ثبت
            }
        }
        // یال‌های عمودی (بالا↕پایین)
        for (r = 0; r < rows; r++) {
            for (c = 0; c <= cols; c++) {
                Intersection A = grid[r][c];      // // ابتدا
                Intersection B = grid[r + 1][c];  // // انتها
                Road road = new Road("RV-" + r + "-" + c, A, B, true); // // جادهٔ مستقیم دوطرفه
                addLanesForTwoWay(road, lanesPerDir, false);           // // لِین‌های عمودی
                map.addRoad(road);                                     // // ثبت
            }
        }
    }

    // افزودن لِین‌های رفت/برگشت برای جاده‌های دوطرفه
    private static void addLanesForTwoWay(Road road, int lanesPerDir, boolean horizontal) {
        Direction fwd = horizontal ? Direction.EAST : Direction.SOUTH; // // جهت رفت
        Direction bwd = horizontal ? Direction.WEST : Direction.NORTH; // // جهت برگشت
        List<Lane> fwdList = new ArrayList<Lane>(); // // لِین‌های رفت
        int i; // // شمارنده
        for (i = 0; i < lanesPerDir; i++) { // // ساخت لِین‌های رفت
            Lane ln = new Lane(road.getId() + "-F" + i, fwd, road); // // لِین
            ln.setOffsetIndex(i);                                   // // اندیس افست جانبی
            if (!fwdList.isEmpty()) { Lane left = fwdList.get(fwdList.size() - 1); ln.setLeftAdjacentLane(left); left.setRightAdjacentLane(ln); } // // لینک همسایه
            road.addForwardLane(ln); fwdList.add(ln);               // // افزودن
        }
        List<Lane> bwdList = new ArrayList<Lane>(); // // لِین‌های برگشت
        for (i = 0; i < lanesPerDir; i++) { // // ساخت لِین‌های برگشت
            Lane ln = new Lane(road.getId() + "-B" + i, bwd, road); // // لِین
            ln.setOffsetIndex(i);                                   // // اندیس
            if (!bwdList.isEmpty()) { Lane left = bwdList.get(bwdList.size() - 1); ln.setLeftAdjacentLane(left); left.setRightAdjacentLane(ln); } // // لینک
            road.addBackwardLane(ln); bwdList.add(ln);              // // افزودن
        }
    }

    // کمکی‌ها
    private static int[] accumFrom(int start, int[] deltas){ int[] out = new int[deltas.length + 1]; out[0] = start; int i; for (i=1;i<out.length;i++) out[i] = out[i-1] + deltas[i-1]; return out; } // // مختصات تجمعی
    private static int rndBetween(Random rnd, int a, int b) { if (a == b) return a; if (a > b) { int t = a; a = b; b = t; } return a + rnd.nextInt(b - a + 1); } // // رندوم
    private static int sumPositive(int[] arr){ int s = 0, i; for (i=0;i<arr.length;i++){ int v = arr[i]; if (v > 0) s += v; } return (s>0)? s : arr.length; } // // جمع مثبت‌ها
}




























//package simulation; // // پکیج شبیه‌سازی
//
//import infrastructure.CityMap;         // // نقشهٔ شهر
//import infrastructure.Intersection;    // // تقاطع
//import infrastructure.Road;            // // جاده
//import infrastructure.Lane;            // // لِین
//import core.Direction;                 // // جهت‌ها
//import core.Point;                     // // مختصات صحیح
//
//import java.util.ArrayList;            // // لیست پویا
//import java.util.List;                 // // لیست
//import java.util.Random;               // // تصادفی
//
///**
// * تولیدکنندهٔ نقشهٔ شبکه‌ای «کاملاً مستقیم» با طول بلوک‌های متفاوت.
// * سه روش:
// *  1) variableGrid(...)      ← بازهٔ رندومی پیکسلی
// *  2) variableGridUnits(...) ← آرایهٔ واحدها × یک ضریب پیکسلی ثابت
// *  3) gridByRatios(...)      ← آرایهٔ نسبت‌ها + عرض/ارتفاع کل (پیشنهادی)
// */
//public final class DemoMaps { // // یوتیلیتی تولید نقشه
//    private DemoMaps() {} // // جلوگیری از نمونه‌سازی
//
//    // روش ۱: رندومی پیکسلی (کاملاً مستقیم)
//    public static CityMap variableGrid(int rows, int cols,
//                                       int minBlockW, int maxBlockW,
//                                       int minBlockH, int maxBlockH,
//                                       int lanesPerDir) {
//        if (rows < 1) rows = 1; if (cols < 1) cols = 1; // // حداقل‌ها
//        if (lanesPerDir < 1) lanesPerDir = 1; if (lanesPerDir > 3) lanesPerDir = 3; // // محدودسازی
//
//        Random rnd = new Random(); // // تصادفی
//        CityMap map = new CityMap(); // // نقشه
//
//        int[] widths  = new int[cols]; // // عرض بلوک‌ها
//        int[] heights = new int[rows]; // // ارتفاع بلوک‌ها
//        int i; // // شمارنده
//        for (i = 0; i < cols; i++) widths[i]  = rndBetween(rnd, Math.max(60, minBlockW), Math.max(60, maxBlockW)); // // عرض‌ها
//        for (i = 0; i < rows; i++) heights[i] = rndBetween(rnd, Math.max(60, minBlockH), Math.max(60, maxBlockH)); // // ارتفاع‌ها
//
//        int[] xs = accumFrom(100, widths); // // مختصات X ستون‌ها
//        int[] ys = accumFrom(100, heights); // // مختصات Y ردیف‌ها
//
//        buildGrid(map, xs, ys, lanesPerDir); // // ساخت شبکهٔ راست
//        return map; // // خروجی
//    }
//
//    // روش ۲: واحدها × ضریب پیکسلی ثابت (کاملاً مستقیم)
//    public static CityMap variableGridUnits(int[] rowUnits, int[] colUnits,
//                                            int unitPx, int lanesPerDir) {
//        if (rowUnits == null || rowUnits.length < 1) rowUnits = new int[]{10};  // // پیش‌فرض
//        if (colUnits == null || colUnits.length < 1) colUnits = new int[]{10};  // // پیش‌فرض
//        if (unitPx < 10) unitPx = 10;                                           // // حداقل
//        if (lanesPerDir < 1) lanesPerDir = 1; if (lanesPerDir > 3) lanesPerDir = 3;
//
//        int[] heights = new int[rowUnits.length]; // // ارتفاع‌ها
//        int[] widths  = new int[colUnits.length]; // // عرض‌ها
//        int i; // // شمارنده
//        for (i=0;i<rowUnits.length;i++){ int u = Math.max(1, rowUnits[i]); heights[i] = Math.max(60, u * unitPx); } // // تبدیل ردیف‌ها
//        for (i=0;i<colUnits.length;i++){ int u = Math.max(1, colUnits[i]); widths[i]  = Math.max(60, u * unitPx); } // // تبدیل ستون‌ها
//
//        CityMap map = new CityMap();                // // نقشه
//        int[] xs = accumFrom(100, widths);          // // مختصات X
//        int[] ys = accumFrom(100, heights);         // // مختصات Y
//        buildGrid(map, xs, ys, lanesPerDir);        // // ساخت شبکهٔ راست
//        return map;                                  // // خروجی
//    }
//
//    // روش ۳: براساس نسبت‌ها (مثل 8,4,14,…) + اندازهٔ کل (کاملاً مستقیم)
//    public static CityMap gridByRatios(int[] rowRatios, int[] colRatios,
//                                       int totalHeightPx, int totalWidthPx,
//                                       int lanesPerDir){
//        if (rowRatios == null || rowRatios.length < 1) rowRatios = new int[]{1}; // // پیش‌فرض
//        if (colRatios == null || colRatios.length < 1) colRatios = new int[]{1}; // // پیش‌فرض
//        if (totalHeightPx < 200) totalHeightPx = 200; // // حداقل ارتفاع کل
//        if (totalWidthPx  < 200) totalWidthPx  = 200; // // حداقل عرض کل
//        if (lanesPerDir < 1) lanesPerDir = 1; if (lanesPerDir > 3) lanesPerDir = 3;
//
//        int rSum = sumPositive(rowRatios); // // مجموع نسبت‌های ردیف
//        int cSum = sumPositive(colRatios); // // مجموع نسبت‌های ستون
//
//        int[] heights = new int[rowRatios.length]; // // ارتفاع هر بلوک
//        int[] widths  = new int[colRatios.length]; // // عرض هر بلوک
//        int i; // // شمارنده
//        for (i=0;i<rowRatios.length;i++){ double frac = (double)Math.max(1,rowRatios[i])/(double)rSum; heights[i] = Math.max(60, (int)Math.round(frac * totalHeightPx)); } // // تبدیل
//        for (i=0;i<colRatios.length;i++){ double frac = (double)Math.max(1,colRatios[i])/(double)cSum; widths[i]  = Math.max(60, (int)Math.round(frac * totalWidthPx));  } // // تبدیل
//
//        CityMap map = new CityMap();                // // نقشه
//        int[] xs = accumFrom(100, widths);          // // مختصات X
//        int[] ys = accumFrom(100, heights);         // // مختصات Y
//        buildGrid(map, xs, ys, lanesPerDir);        // // ساخت شبکهٔ راست
//        return map;                                 // // خروجی
//    }
//
//    // ساخت شبکه از روی مختصات تجمعی (کاملاً مستقیم)
//    private static void buildGrid(CityMap map, int[] xs, int[] ys, int lanesPerDir){
//        int rows = ys.length - 1; // // تعداد بلوک عمودی
//        int cols = xs.length - 1; // // تعداد بلوک افقی
//
//        Intersection[][] grid = new Intersection[rows + 1][cols + 1]; // // ماتریس گره‌ها
//        int gid = 0; // // شمارندهٔ تقاطع‌ها
//        int r, c;   // // ایندکس‌ها
//
//        // ساخت همهٔ تقاطع‌ها
//        for (r = 0; r <= rows; r++) {
//            for (c = 0; c <= cols; c++) {
//                Point p = new Point(xs[c], ys[r]);                 // // مختصات
//                Intersection it = new Intersection("I" + (gid++), p); // // تقاطع
//                map.addIntersection(it);                           // // افزودن
//                grid[r][c] = it;                                   // // ذخیره
//            }
//        }
//
//        // یال‌های افقی (چپ↔راست)
//        for (r = 0; r <= rows; r++) {
//            for (c = 0; c < cols; c++) {
//                Intersection A = grid[r][c];      // // ابتدا
//                Intersection B = grid[r][c + 1];  // // انتها
//                Road road = new Road("RH-" + r + "-" + c, A, B, true); // // جادهٔ مستقیم دوطرفه
//                addLanesForTwoWay(road, lanesPerDir, true);            // // لِین‌های افقی
//                map.addRoad(road);                                     // // ثبت
//            }
//        }
//        // یال‌های عمودی (بالا↕پایین)
//        for (r = 0; r < rows; r++) {
//            for (c = 0; c <= cols; c++) {
//                Intersection A = grid[r][c];      // // ابتدا
//                Intersection B = grid[r + 1][c];  // // انتها
//                Road road = new Road("RV-" + r + "-" + c, A, B, true); // // جادهٔ مستقیم دوطرفه
//                addLanesForTwoWay(road, lanesPerDir, false);           // // لِین‌های عمودی
//                map.addRoad(road);                                     // // ثبت
//            }
//        }
//    }
//
//    // افزودن لِین‌های رفت/برگشت برای جاده‌های دوطرفه
//    private static void addLanesForTwoWay(Road road, int lanesPerDir, boolean horizontal) {
//        Direction fwd = horizontal ? Direction.EAST : Direction.SOUTH; // // جهت رفت
//        Direction bwd = horizontal ? Direction.WEST : Direction.NORTH; // // جهت برگشت
//        List<Lane> fwdList = new ArrayList<Lane>(); // // لِین‌های رفت
//        int i; // // شمارنده
//        for (i = 0; i < lanesPerDir; i++) { // // ساخت لِین‌های رفت
//            Lane ln = new Lane(road.getId() + "-F" + i, fwd, road); // // لِین
//            ln.setOffsetIndex(i);                                   // // اندیس افست جانبی
//            if (!fwdList.isEmpty()) { Lane left = fwdList.get(fwdList.size() - 1); ln.setLeftAdjacentLane(left); left.setRightAdjacentLane(ln); } // // لینک همسایه
//            road.addForwardLane(ln); fwdList.add(ln);               // // افزودن
//        }
//        List<Lane> bwdList = new ArrayList<Lane>(); // // لِین‌های برگشت
//        for (i = 0; i < lanesPerDir; i++) { // // ساخت لِین‌های برگشت
//            Lane ln = new Lane(road.getId() + "-B" + i, bwd, road); // // لِین
//            ln.setOffsetIndex(i);                                   // // اندیس
//            if (!bwdList.isEmpty()) { Lane left = bwdList.get(bwdList.size() - 1); ln.setLeftAdjacentLane(left); left.setRightAdjacentLane(ln); } // // لینک
//            road.addBackwardLane(ln); bwdList.add(ln);              // // افزودن
//        }
//    }
//
//    // کمکی‌ها
//    private static int[] accumFrom(int start, int[] deltas){ int[] out = new int[deltas.length + 1]; out[0] = start; int i; for (i=1;i<out.length;i++) out[i] = out[i-1] + deltas[i-1]; return out; } // // مختصات تجمعی
//    private static int rndBetween(Random rnd, int a, int b) { if (a == b) return a; if (a > b) { int t = a; a = b; b = t; } return a + rnd.nextInt(b - a + 1); } // // رندوم
//    private static int sumPositive(int[] arr){ int s = 0, i; for (i=0;i<arr.length;i++){ int v = arr[i]; if (v > 0) s += v; } return (s>0)? s : arr.length; } // // جمع مثبت‌ها
//}
//
