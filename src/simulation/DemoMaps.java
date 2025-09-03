




package simulation; // // پکیج simulation

import core.Direction; // // جهت‌ها
import core.Point; // // مختصات
import infrastructure.CityMap; // // نقشه
import infrastructure.Intersection; // // تقاطع
import infrastructure.Road; // // جاده
import infrastructure.Lane; // // لِین

import java.util.Random; // // نامنظمی کوچک برای طبیعی‌تر شدن

public final class DemoMaps { // // کلاس کمکی ساخت نقشه‌ی دمو
    private DemoMaps() {} // // جلوگیری از نمونه‌سازی

    public static CityMap irregularGrid(int rows, int cols, int blockW, int blockH, int gapX, int gapY) { // // گرید نامنظم
        CityMap map = new CityMap(); // // نقشه خالی
        Random rnd = new Random(); // // رندوم

        // --- ۱) ساخت تقاطع‌ها روی گره‌های شبکه ---
        Intersection[][] I = new Intersection[rows + 1][cols + 1]; // // ماتریس تقاطع‌ها
        for (int r = 0; r <= rows; r++) { // // سطرها
            for (int c = 0; c <= cols; c++) { // // ستون‌ها
                int jx = rnd.nextInt(11) - 5; // // جابه‌جایی کوچک افقی
                int jy = rnd.nextInt(11) - 5; // // جابه‌جایی کوچک عمودی
                int x = c * (blockW + gapX) + jx; // // X نهایی
                int y = r * (blockH + gapY) + jy; // // Y نهایی
                Intersection inter = new Intersection("I-" + r + "-" + c, new Point(x, y)); // // ساخت تقاطع
                map.addIntersection(inter); // // افزودن به نقشه
                I[r][c] = inter; // // ذخیره
            }
        }

        // --- ۲) جاده‌های افقی (دوطرفه، دو لِین در هر جهت) ---
        for (int r = 0; r <= rows; r++) { // // سطر
            for (int c = 0; c < cols; c++) { // // بین ستون‌های مجاور
                Intersection A = I[r][c]; // // مبدأ
                Intersection B = I[r][c + 1]; // // مقصد
                Road R = new Road("RH-" + r + "-" + c, A, B, true); // // جاده دوطرفه
                // رفت به شرق
                Lane f1 = new Lane("L-H-F1-" + r + "-" + c, Direction.EAST, R); // // لِین رفت ۱
                Lane f2 = new Lane("L-H-F2-" + r + "-" + c, Direction.EAST, R); // // لِین رفت ۲
                f1.setOffsetIndex(-1); // // کمی چپِ خط مرکزی (نسبت به جهت EAST)
                f2.setOffsetIndex(+1); // // کمی راستِ خط مرکزی
                R.addForwardLane(f1); // // افزودن به forward
                R.addForwardLane(f2); // // افزودن به forward
                // برگشت به غرب
                Lane b1 = new Lane("L-H-B1-" + r + "-" + c, Direction.WEST, R); // // لِین برگشت ۱
                Lane b2 = new Lane("L-H-B2-" + r + "-" + c, Direction.WEST, R); // // لِین برگشت ۲
                b1.setOffsetIndex(-1); // // چپ (نسبت به WEST)
                b2.setOffsetIndex(+1); // // راست
                R.addBackwardLane(b1); // // افزودن به backward
                R.addBackwardLane(b2); // // افزودن به backward
                map.addRoad(R); // // ثبت جاده
            }
        }

        // --- ۳) جاده‌های عمودی (دوطرفه، دو لِین در هر جهت) ---
        for (int c = 0; c <= cols; c++) { // // ستون
            for (int r = 0; r < rows; r++) { // // بین سطرهای مجاور
                Intersection A = I[r][c]; // // مبدأ
                Intersection B = I[r + 1][c]; // // مقصد
                Road R = new Road("RV-" + r + "-" + c, A, B, true); // // جاده دوطرفه
                // رفت به جنوب
                Lane f1 = new Lane("L-V-F1-" + r + "-" + c, Direction.SOUTH, R); // // لِین رفت ۱
                Lane f2 = new Lane("L-V-F2-" + r + "-" + c, Direction.SOUTH, R); // // لِین رفت ۲
                f1.setOffsetIndex(-1); // // چپ نسبت به SOUTH
                f2.setOffsetIndex(+1); // // راست
                R.addForwardLane(f1); // // افزودن
                R.addForwardLane(f2); // // افزودن
                // برگشت به شمال
                Lane b1 = new Lane("L-V-B1-" + r + "-" + c, Direction.NORTH, R); // // لِین برگشت ۱
                Lane b2 = new Lane("L-V-B2-" + r + "-" + c, Direction.NORTH, R); // // لِین برگشت ۲
                b1.setOffsetIndex(-1); // // چپ
                b2.setOffsetIndex(+1); // // راست
                R.addBackwardLane(b1); // // افزودن
                R.addBackwardLane(b2); // // افزودن
                map.addRoad(R); // // ثبت جاده
            }
        }

        return map; // // نقشه نهایی
    }
}







































//package simulation;
//
//import core.Direction;
//import core.Point;
//import infrastructure.*;
//import pedestrian.PedestrianCrossing;
//
//public class DemoMaps {
//
//    /** نمونه: چهارراه + یک فلکه وسط */
//    public static CityMap buildFourWayRoundabout() {
//        CityMap map = new CityMap();
//
//        // چهار تقاطع اصلی
//        Intersection n = map.newIntersection("N", 400, 100);
//        Intersection s = map.newIntersection("S", 400, 700);
//        Intersection w = map.newIntersection("W", 100, 400);
//        Intersection e = map.newIntersection("E", 700, 400);
//
//        // جاده‌ها
//        Road rn = new Road("N-S", n, s, true);
//        Road rw = new Road("W-E", w, e, true);
//
//        // علامت‌گذاری نزدیک فلکه (برای isNearRoundabout)
//        rn.setNearRoundabout(true);
//        rw.setNearRoundabout(true);
//
//        // لِین‌ها (دو تا رفت، دو تا برگشت نمونه)
//        Lane n_s_0 = new Lane("n_s_0", Direction.SOUTH, rn); n_s_0.setOffsetIndex(-1);
//        Lane n_s_1 = new Lane("n_s_1", Direction.SOUTH, rn); n_s_1.setOffsetIndex(+1);
//        rn.addForwardLane(n_s_0);
//        rn.addForwardLane(n_s_1);
//
//        Lane s_n_0 = new Lane("s_n_0", Direction.NORTH, rn); s_n_0.setOffsetIndex(-1);
//        Lane s_n_1 = new Lane("s_n_1", Direction.NORTH, rn); s_n_1.setOffsetIndex(+1);
//        rn.addBackwardLane(s_n_0);
//        rn.addBackwardLane(s_n_1);
//
//        Lane w_e_0 = new Lane("w_e_0", Direction.EAST, rw); w_e_0.setOffsetIndex(-1);
//        Lane w_e_1 = new Lane("w_e_1", Direction.EAST, rw); w_e_1.setOffsetIndex(+1);
//        rw.addForwardLane(w_e_0);
//        rw.addForwardLane(w_e_1);
//
//        Lane e_w_0 = new Lane("e_w_0", Direction.WEST, rw); e_w_0.setOffsetIndex(-1);
//        Lane e_w_1 = new Lane("e_w_1", Direction.WEST, rw); e_w_1.setOffsetIndex(+1);
//        rw.addBackwardLane(e_w_0);
//        rw.addBackwardLane(e_w_1);
//
//        map.addRoad(rn);
//        map.addRoad(rw);
//
//        // چند گذرگاه عابر — دور از فلکه، تصادفی/ثابت
//        map.addPedestrianCrossing(new PedestrianCrossing("pc1", n, Direction.SOUTH, false));
//        map.addPedestrianCrossing(new PedestrianCrossing("pc2", e, Direction.WEST,  false));
//        map.addPedestrianCrossing(new PedestrianCrossing("pc3", s, Direction.NORTH, false));
//        map.addPedestrianCrossing(new PedestrianCrossing("pc4", w, Direction.EAST,  false));
//
//        return map;
//    }
//
//    /** نمونه‌ی گرید نامنظم با فلکه مرکزی (برای MainWindow قبلی) */
//    public static CityMap irregularGridWithRoundabout(int rows, int cols,
//                                                      int cellW, int cellH,
//                                                      int cx, int cy) {
//        // برای الان یک نقشه‌ی کوچک برمی‌گردانیم تا خطاها رفع شود
//        return buildFourWayRoundabout();
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////package simulation;
////
////import core.*;
////import infrastructure.*;
////import pedestrian.PedestrianCrossing;
////
////import java.util.Random;
////
/////** نقشه‌های دمو — نسخه‌ی هماهنگ با کلاس‌های جدید */
////public class DemoMaps {
////
////    private static final Random RNG = new Random();
////
////    /** گرید نامنظم + یک فلکه در مرکز */
////    public static CityMap irregularGridWithRoundabout(int rows, int cols,
////                                                      int cellW, int cellH,
////                                                      int cx, int cy) {
////        CityMap map = new CityMap();
////
////        // ساخت تقاطع‌ها
////        Intersection[][] I = new Intersection[rows][cols];
////        for (int r = 0; r < rows; r++) {
////            for (int c = 0; c < cols; c++) {
////                Point p = new Point(c * cellW + RNG.nextInt(15), r * cellH + RNG.nextInt(15));
////                I[r][c] = new Intersection("I" + r + "_" + c, p);
////                map.addIntersection(I[r][c]);
////            }
////        }
////
////        // ساخت جاده‌ها (افقی/عمودی)
////        for (int r = 0; r < rows; r++) {
////            for (int c = 0; c + 1 < cols; c++) {
////                Road rd = new Road("R_h_" + r + "_" + c, I[r][c], I[r][c+1], true, 2, 2);
////                map.addRoad(rd);
////            }
////        }
////        for (int c = 0; c < cols; c++) {
////            for (int r = 0; r + 1 < rows; r++) {
////                Road rd = new Road("R_v_" + r + "_" + c, I[r][c], I[r+1][c], true, 2, 2);
////                map.addRoad(rd);
////            }
////        }
////
////        // یک فلکه‌ ساده در مرکز (با فلگ روی Roadهای اطراف)
////        int mr = rows / 2, mc = cols / 2;
////        Intersection center = I[mr][mc];
////        for (Road rd : map.getRoads()) {
////            if (rd.getStartIntersection() == center || rd.getEndIntersection() == center) {
////                rd.setNearRoundabout(true);
////            }
////        }
////
////        // چند عابرگذر نمونه (دور از فلکه)
////        for (int k = 0; k < 4; k++) {
////            int rr = RNG.nextInt(rows);
////            int cc = RNG.nextInt(cols);
////            if (Math.abs(rr - mr) + Math.abs(cc - mc) <= 1) { k--; continue; } // کنار فلکه نباشد
////            PedestrianCrossing pc = new PedestrianCrossing(
////                    "pc" + k, I[rr][cc],
////                    (k % 2 == 0) ? Direction.EAST : Direction.SOUTH,
////                    false
////            );
////            map.addPedestrianCrossing(pc);
////        }
////
////        return map;
////    }
////}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////package simulation;
////
////import java.util.*;
////import core.*;
////import infrastructure.*;
////import pedestrian.PedestrianCrossing;
////
/////**
//// * ساخت نقشه‌های نمایشی (Demo) شامل شبکه‌ٔ نامنظم و فلکهٔ چهار‌راهیِ راست‌گرد.
//// *
//// * نکته: در این پیاده‌سازی فرض شده CityMap دارای addIntersection/addRoad است و
//// * Road با سازنده‌ی (id, start, end, twoWay) ساخته می‌شود.
//// * اگر امضای کلاس‌های شما متفاوت است، فقط داخل متدهای addI/addR مطابق امضای واقعی تغییر دهید.
//// */
////public final class DemoMaps {
////
////    private DemoMaps() {}
////
////    /* ========================= API عمومی ========================= */
////
////    /** نسخهٔ ساده با مقادیر پیش‌فرض برای شعاع و تعداد سگمنت‌های فلکه */
////    public static CityMap irregularGrid(int rows, int cols, int minLen, int maxLen) {
////        // شعاع بیرونی فلکه ~120px و 16 سگمنت برای دایره‌ای نرم
////        return irregularGridWithRoundabout(rows, cols, minLen, maxLen, 120, 16);
////    }
////
////    /**
////     * شبکهٔ نامنظم + فلکهٔ چهارراهی در مرکز.
////     * @param rows تعداد ردیف‌ها
////     * @param cols تعداد ستون‌ها
////     * @param minLen حداقل فاصلهٔ بین تقاطع‌ها (پیکسل)
////     * @param maxLen حداکثر فاصلهٔ بین تقاطع‌ها (پیکسل)
////     * @param outerRadius شعاع بیرونی فلکه (پیکسل)
////     * @param circleSegments تعداد سگمنت‌های حلقهٔ فلکه برای رندر نرم (۸ تا ۲۰ پیشنهاد می‌شود)
////     */
////    public static CityMap irregularGridWithRoundabout(
////            int rows, int cols, int minLen, int maxLen,
////            int outerRadius, int circleSegments) {
////
////        CityMap map = new CityMap();
////
////        // 1) تولید نقاط نامنظم شبکه
////        int[] xs = jitteredAxis(cols, minLen, maxLen);
////        int[] ys = jitteredAxis(rows, minLen, maxLen);
////
////        // 2) ساخت تقاطع‌های شبکه
////        Intersection[][] grid = new Intersection[rows][cols];
////        for (int r = 0; r < rows; r++) {
////            for (int c = 0; c < cols; c++) {
////                String id = "i_" + r + "_" + c;
////                grid[r][c] = addI(map, id, xs[c], ys[r]);
////            }
////        }
////
////        // 3) ساخت خیابان‌های افقی/عمودی با طول‌های متفاوت و 2طرفه
////        for (int r = 0; r < rows; r++) {
////            for (int c = 0; c + 1 < cols; c++) {
////                addTwoWayRoad(map, "h_" + r + "_" + c, grid[r][c], grid[r][c + 1]);
////            }
////        }
////        for (int c = 0; c < cols; c++) {
////            for (int r = 0; r + 1 < rows; r++) {
////                addTwoWayRoad(map, "v_" + r + "_" + c, grid[r][c], grid[r + 1][c]);
////            }
////        }
////
////        // 4) تعیین مرکز شبکه و ساخت فلکهٔ چهارراهی
////        int centerR = rows / 2;
////        int centerC = cols / 2;
////        Point center = grid[centerR][centerC].getPosition(); // (حل خطای قبلی: Point از طریق getPosition)
////
////        RoundaboutBundle rb = buildRoundabout(map, center, outerRadius, circleSegments);
////
////        // 5) اتصال ۴ بازوی اصلی خیابان‌ها به فلکه (شمال/جنوب/شرق/غرب)
////        //    هر بازو یک جفت مسیر entry/exit (یک‌طرفه) دارد.
////        connectArm(map, grid[centerR - 1][centerC], rb, Direction.SOUTH, "arm_N");
////        connectArm(map, grid[centerR + 1][centerC], rb, Direction.NORTH, "arm_S");
////        connectArm(map, grid[centerR][centerC + 1], rb, Direction.WEST,  "arm_E");
////        connectArm(map, grid[centerR][centerC - 1], rb, Direction.EAST,  "arm_W");
////
////        // 6) افزودن ۴ گذرگاه عابر به‌صورت تصادفی اما دور از فلکه
////        addRandomCrossingsAwayFrom(map, rb.ringNodes, 4, outerRadius * 2);
////
////        return map;
////    }
////
////    /* ========================= جزئیات پیاده‌سازی ========================= */
////
////    /** تولید مختصات نامنظم روی یک محور (برای شکستن قرینگی) */
////    private static int[] jitteredAxis(int n, int min, int max) {
////        Random rnd = new Random();
////        int[] a = new int[n];
////        int cur = 80; // کمی حاشیه
////        for (int i = 0; i < n; i++) {
////            int step = min + rnd.nextInt(Math.max(1, max - min + 1));
////            cur += step;
////            a[i] = cur;
////        }
////        return a;
////    }
////
////    /** اضافه‌کردن تقاطع به نقشه (در صورت تفاوت امضا، اینجا را تطبیق دهید) */
////    private static Intersection addI(CityMap map, String id, int x, int y) {
////        Intersection it = new Intersection(id, new Point(x, y));
////        map.addIntersection(it);
////        return it;
////    }
////
////    /** خیابان دوطرفه ساده بین دو تقاطع (اگر Road سازندهٔ دیگری دارد، اینجا را هماهنگ کنید) */
////    private static Road addTwoWayRoad(CityMap map, String id, Intersection a, Intersection b) {
////        Road r = new Road(id, a, b, true); // true => دوطرفه
////        map.addRoad(r);
////        return r;
////    }
////
////    /** خیابان یک‌طرفه (forward فقط)؛ برای سگمنت‌های حلقه و ورودی/خروجی‌ها */
////    private static Road addOneWayRoad(CityMap map, String id, Intersection from, Intersection to) {
////        // اگر کلاس Road پرچم twoWay می‌گیرد، آن را false بگذاریم؛ جهت حرکت from→to
////        Road r = new Road(id, from, to, false);
////        map.addRoad(r);
////        return r;
////    }
////
////    /** محاسبه نقطه‌ی قطبی روی دایره */
////    private static Point polar(Point center, double radius, double angleRad) {
////        int x = (int)Math.round(center.getX() + radius * Math.cos(angleRad));
////        int y = (int)Math.round(center.getY() + radius * Math.sin(angleRad));
////        return new Point(x, y);
////    }
////
////    /** بستهٔ اطلاعات فلکه برای اتصال بازوها */
////    private static final class RoundaboutBundle {
////        final List<Intersection> ringNodes; // گره‌های حلقه (ساعتگرد)
////        final double radius;
////        final Point center;
////
////        RoundaboutBundle(List<Intersection> ringNodes, double radius, Point center) {
////            this.ringNodes = ringNodes;
////            this.radius = radius;
////            this.center = center;
////        }
////    }
////
////    /**
////     * ساخت حلقهٔ راست‌گرد از چند سگمنت یک‌طرفه (ساعتگرد).
////     * هر سگمنت: Road یک‌طرفه از گره i به i+1.
////     */
////    private static RoundaboutBundle buildRoundabout(
////            CityMap map, Point center, double outerRadius, int segments) {
////
////        segments = Math.max(8, segments); // حداقل ۸ برای دایره پذیرفتنی
////        List<Intersection> ring = new ArrayList<>(segments);
////
////        // گره‌ها
////        for (int i = 0; i < segments; i++) {
////            double ang = (2 * Math.PI * i) / segments;
////            Point p = polar(center, outerRadius, ang);
////            Intersection it = new Intersection("rb_i" + i, p);
////            map.addIntersection(it);
////            ring.add(it);
////        }
////        // سگمنت‌های یک‌طرفه (ساعتگرد)
////        for (int i = 0; i < segments; i++) {
////            Intersection a = ring.get(i);
////            Intersection b = ring.get((i + 1) % segments);
////            addOneWayRoad(map, "rb_r" + i, a, b); // جهت a→b (ساعتگرد)
////        }
////        return new RoundaboutBundle(ring, outerRadius, center);
////    }
////
////    /**
////     * اتصال یک بازوی اصلی (entry/exit) به حلقه:
////     * - entry: خیابان یک‌طرفه از خیابان بیرونی به نزدیک‌ترین گره حلقه (ورود به فلکه).
////     * - exit : خیابان یک‌طرفه از گره بعدیِ حلقه به خیابان بیرونی (خروج از فلکه).
////     *
////     * @param outerNode یک تقاطع شبکه که بازو از آن می‌آید
////     * @param rb        بستهٔ فلکه
////     * @param dirToCenter جهت تقریبی از بیرون به مرکز (برای انتخاب نزدیک‌ترین گره)
////     * @param idPrefix  پیشوند شناسه‌ها
////     */
////    private static void connectArm(
////            CityMap map, Intersection outerNode, RoundaboutBundle rb,
////            Direction dirToCenter, String idPrefix) {
////
////        // نزدیک‌ترین گره حلقه به outerNode
////        int nearestIdx = nearestRingIndex(outerNode.getPosition(), rb.ringNodes);
////        int exitIdx    = (nearestIdx + 1) % rb.ringNodes.size(); // یک گره جلوتر برای خروج
////
////        Intersection entryOnRing = rb.ringNodes.get(nearestIdx);
////        Intersection exitOnRing  = rb.ringNodes.get(exitIdx);
////
////        // Entry: outer -> entryOnRing (یک‌طرفه به سمت فلکه)
////        addOneWayRoad(map, idPrefix + "_in", outerNode, entryOnRing);
////
////        // Exit: exitOnRing -> outer (یک‌طرفه از فلکه بیرون)
////        addOneWayRoad(map, idPrefix + "_out", exitOnRing, outerNode);
////    }
////
////    /** پیدا کردن نزدیک‌ترین گره حلقه به یک نقطه */
////    private static int nearestRingIndex(Point p, List<Intersection> ring) {
////        int best = 0;
////        long bestD2 = Long.MAX_VALUE;
////        for (int i = 0; i < ring.size(); i++) {
////            Point q = ring.get(i).getPosition();
////            long dx = (long)q.getX() - p.getX();
////            long dy = (long)q.getY() - p.getY();
////            long d2 = dx * dx + dy * dy;
////            if (d2 < bestD2) { bestD2 = d2; best = i; }
////        }
////        return best;
////    }
////
////    /**
////     * افزودن N گذرگاه عابر به‌صورت تصادفی که به اندازهٔ minDist از همهٔ گره‌های حلقه دور باشند.
////     * (برای سادگی، روی تقاطع‌های موجود ساخته می‌شود و نزدیک فلکه نیست.)
////     */
////    private static void addRandomCrossingsAwayFrom(
////            CityMap map, List<Intersection> ring, int count, double minDist) {
////
////        Random rnd = new Random();
////        List<Intersection> all = new ArrayList<>(map.getIntersections());
////        // حذف گره‌های خیلی نزدیک به حلقه
////        Iterator<Intersection> it = all.iterator();
////        while (it.hasNext()) {
////            Intersection x = it.next();
////            if (isNearAny(x.getPosition(), ring, minDist)) it.remove();
////        }
////        // اگر تعداد کافی نبود، رها
////        if (all.isEmpty()) return;
////
////        for (int i = 0; i < count; i++) {
////            Intersection pick = all.get(rnd.nextInt(all.size()));
////            // جهت تصادفی برای گذرگاه
////            Direction d = Direction.values()[rnd.nextInt(Direction.values().length)];
////            // بدون چراغ (hasSignal=false) طبق نیاز شما؛ اگر چراغ لازم شد بعداً تغییر دهید
////            PedestrianCrossing pc = new PedestrianCrossing("pc_" + System.nanoTime(), pick, d, false);
////            map.addPedestrianCrossing(pc);
////        }
////    }
////
////    private static boolean isNearAny(Point p, List<Intersection> ring, double minDist) {
////        double minD2 = minDist * minDist;
////        for (Intersection i : ring) {
////            Point q = i.getPosition();
////            double dx = q.getX() - p.getX();
////            double dy = q.getY() - p.getY();
////            double d2 = dx * dx + dy * dy;
////            if (d2 < minD2) return true;
////        }
////        return false;
////    }
////}
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
////
//////
//////package simulation;
//////
//////import java.util.*;
//////import core.*;
//////import infrastructure.*;
//////import trafficcontrol.*;
//////import pedestrian.*;
//////
//////public final class DemoMaps {
//////
//////    private DemoMaps(){}
//////
//////    public static CityMap buildFourWayRoundabout(){
//////        CityMap map = new CityMap();
//////
//////        // چهار تقاطع ورودی با فاصله‌های نامساوی
//////        Intersection N = new Intersection("N", new Point(0, -220));
//////        Intersection S = new Intersection("S", new Point(0,  260));
//////        Intersection E = new Intersection("E", new Point(300, 0));
//////        Intersection W = new Intersection("W", new Point(-280, 0));
//////
//////        // مرکز فلکه
//////        Intersection C = new Intersection("C", new Point(0, 0));
//////        C.markAsRoundabout(true);
//////
//////        // چهار جادهٔ شعاعی با طول‌های متفاوت
//////        Road rNC = new Road("rNC", N, C, 220);
//////        Road rSC = new Road("rSC", S, C, 260);
//////        Road rEC = new Road("rEC", E, C, 300);
//////        Road rWC = new Road("rWC", W, C, 280);
//////
//////        // هر شعاع: دوطرفه (ورود/خروج)
//////        // Forward: به سمت C (مثلا SOUTH/EAST/WEST/NORTH نسبت به هندسه)
//////        Lane n_in  = new Lane("n_in",  Direction.SOUTH, rNC); n_in.setOffsetIndex(+1);
//////        Lane n_out = new Lane("n_out", Direction.NORTH, rNC); n_out.setOffsetIndex(-1);
//////        rNC.addForwardLane(n_in); rNC.addBackwardLane(n_out);
//////
//////        Lane s_in  = new Lane("s_in",  Direction.NORTH, rSC); s_in.setOffsetIndex(+1);
//////        Lane s_out = new Lane("s_out", Direction.SOUTH, rSC); s_out.setOffsetIndex(-1);
//////        rSC.addForwardLane(s_in); rSC.addBackwardLane(s_out);
//////
//////        Lane e_in  = new Lane("e_in",  Direction.WEST,  rEC); e_in.setOffsetIndex(+1);
//////        Lane e_out = new Lane("e_out", Direction.EAST,  rEC); e_out.setOffsetIndex(-1);
//////        rEC.addForwardLane(e_in); rEC.addBackwardLane(e_out);
//////
//////        Lane w_in  = new Lane("w_in",  Direction.EAST,  rWC); w_in.setOffsetIndex(+1);
//////        Lane w_out = new Lane("w_out", Direction.WEST,  rWC); w_out.setOffsetIndex(-1);
//////        rWC.addForwardLane(w_in); rWC.addBackwardLane(w_out);
//////
//////        // حلقهٔ فلکه: دو لاین راست‌گرد (هر دو با جهت ساعت‌گرد)
//////        Road ring = new Road("ring", C, C, 2*Math.PI*120 /*محیط تقریبی*/);
//////        Lane ringOuter = new Lane("ringOuter", Direction.EAST, ring); ringOuter.setOffsetIndex(+1);
//////        Lane ringInner = new Lane("ringInner", Direction.EAST, ring); ringInner.setOffsetIndex(+2);
//////        ring.addForwardLane(ringOuter); ring.addForwardLane(ringInner);
//////        ring.setTwoWay(false);
//////
//////        // معرفی لاین‌های حلقه به تقاطع مرکزی
//////        C.setRingLanes(Arrays.asList(ringOuter, ringInner));
//////
//////        // محل merge تخمینی برای فاصله‌سنجی
//////        C.setRingMergeS(Direction.SOUTH, 0.0);
//////        C.setRingMergeS(Direction.NORTH, ring.getLengthMeters()*0.50);
//////        C.setRingMergeS(Direction.EAST,  ring.getLengthMeters()*0.25);
//////        C.setRingMergeS(Direction.WEST,  ring.getLengthMeters()*0.75);
//////
//////        // ورودی‌های فلکه Yield (فقط برای رندر؛ World خودش چراغ را نادیده می‌گیرد)
//////        C.setControl(Direction.NORTH, new YieldSign("y_n", Direction.NORTH));
//////        C.setControl(Direction.SOUTH, new YieldSign("y_s", Direction.SOUTH));
//////        C.setControl(Direction.EAST,  new YieldSign("y_e", Direction.EAST));
//////        C.setControl(Direction.WEST,  new YieldSign("y_w", Direction.WEST));
//////
//////        // چند سرعت‌گیر و محدودیت محلی نمونه
//////        rNC.setLocalSpeedLimit(10.0); // m/s ≈ 36 km/h
//////        rNC.addSpeedBump(new SpeedBump(120, 126, 5.0));
//////        rSC.addSpeedBump(new SpeedBump(150, 156, 6.0));
//////
//////        // گذرگاه‌های عابر (۴ تا، دور از فلکه)
//////        rEC.addCrossing(new PedestrianCrossing("pc1", E, Direction.WEST, false));
//////        rWC.addCrossing(new PedestrianCrossing("pc2", W, Direction.EAST, false));
//////        rNC.addCrossing(new PedestrianCrossing("pc3", N, Direction.SOUTH, false));
//////        rSC.addCrossing(new PedestrianCrossing("pc4", S, Direction.NORTH, false));
//////
//////        // ثبت روی نقشه
//////        map.addIntersection(N); map.addIntersection(S); map.addIntersection(E);
//////        map.addIntersection(W); map.addIntersection(C);
//////        map.addRoad(rNC); map.addRoad(rSC); map.addRoad(rEC); map.addRoad(rWC); map.addRoad(ring);
//////
//////        return map;
//////    }
//////}
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
////////
////////package simulation; // // پکیج simulation
////////
////////import core.Direction; // // جهت‌ها
////////import core.Point; // // مختصات
////////import infrastructure.*; // // CityMap/Intersection/Road/Lane
////////
////////import java.util.Random; // // رندوم
////////import java.util.List;   // // لیست
////////
////////public final class DemoMaps { // // کلاس کمکی ساخت نقشه‌ی دمو
////////    private DemoMaps() {} // // جلوگیری از نمونه‌سازی
////////
////////    // ---------------- گرید نامنظم + فلکه دایره‌ای ۲ لِین در مرکز ----------------
////////    public static CityMap irregularGridWithRoundabout(int rows, int cols, int blockW, int blockH, int gapX, int gapY) { // // گرید + فلکه
////////        CityMap map = new CityMap(); // // نقشه خالی
////////        Random rnd = new Random(); // // رندوم
////////
////////        // 1) ساخت تقاطع‌های گرید
////////        Intersection[][] I = new Intersection[rows + 1][cols + 1]; // // ماتریس تقاطع‌ها
////////        for (int r = 0; r <= rows; r++) { // // سطرها
////////            for (int c = 0; c <= cols; c++) { // // ستون‌ها
////////                int jx = rnd.nextInt(11) - 5; // // جابه‌جایی کوچک افقی
////////                int jy = rnd.nextInt(11) - 5; // // جابه‌جایی کوچک عمودی
////////                int x = c * (blockW + gapX) + jx; // // X نهایی
////////                int y = r * (blockH + gapY) + jy; // // Y نهایی
////////                Intersection inter = new Intersection("I-" + r + "-" + c, new Point(x, y)); // // ساخت تقاطع
////////                map.addIntersection(inter); // // افزودن به نقشه
////////                I[r][c] = inter; // // ذخیره
////////            }
////////        }
////////
////////        // 2) جاده‌های افقی/عمودی پایه (هر جهت ۱ لِین) – فلکه بعداً اضافه می‌شود
////////        for (int r = 0; r <= rows; r++) { // // افقی‌ها
////////            for (int c = 0; c < cols; c++) {
////////                Road R = new Road("RH-" + r + "-" + c, I[r][c], I[r][c + 1], true); // // دوطرفه
////////                Lane f = new Lane("L-H-F-" + r + "-" + c, Direction.EAST, R); f.setOffsetIndex(0); R.addForwardLane(f); // // رفت
////////                Lane b = new Lane("L-H-B-" + r + "-" + c, Direction.WEST, R); b.setOffsetIndex(0); R.addBackwardLane(b); // // برگشت
////////                map.addRoad(R); // // ثبت
////////            }
////////        }
////////        for (int c = 0; c <= cols; c++) { // // عمودی‌ها
////////            for (int r = 0; r < rows; r++) {
////////                Road R = new Road("RV-" + r + "-" + c, I[r][c], I[r + 1][c], true); // // دوطرفه
////////                Lane f = new Lane("L-V-F-" + r + "-" + c, Direction.SOUTH, R); f.setOffsetIndex(0); R.addForwardLane(f); // // رفت
////////                Lane b = new Lane("L-V-B-" + r + "-" + c, Direction.NORTH, R); b.setOffsetIndex(0); R.addBackwardLane(b); // // برگشت
////////                map.addRoad(R); // // ثبت
////////            }
////////        }
////////
////////        // 3) فلکه دایره‌ای ۲ لِین با ۴ بازوی ورودی/خروجی ۴ لِین
////////        int rc = rows / 2; // // سطر وسط
////////        int cc = cols / 2; // // ستون وسط
////////        Point center = I[rc][cc].getPosition(); // // مرکز تقریبی
////////        buildRoundaboutCircular2Lanes(map, center, 70, 12, 150); // // شعاع، تعداد قطعه، طول بازوها
////////
////////        return map; // // نقشه نهایی
////////    }
////////
////////    // ---------------- فلکه دایره‌ای ۲ لِین (حلقه یکطرفه ساعت‌گرد) ----------------
////////    private static void buildRoundaboutCircular2Lanes(CityMap map, Point c, int radius, int arcSegments, int armLen) { // // ساخت فلکه
////////        // نقاط روی دایره (arcSegments عدد گره) با نام RND-C-k
////////        Intersection[] ring = new Intersection[arcSegments]; // // گره‌های حلقه
////////        double step = 2.0 * Math.PI / arcSegments; // // گام زاویه
////////        for (int k = 0; k < arcSegments; k++) { // // ایجاد گره‌ها
////////            double ang = k * step; // // زاویه
////////            int x = c.getX() + (int) Math.round(radius * Math.cos(ang)); // // X
////////            int y = c.getY() + (int) Math.round(radius * Math.sin(ang)); // // Y
////////            Intersection node = new Intersection("RND-C-" + k, new Point(x, y)); // // گره حلقه
////////            map.addIntersection(node); // // ثبت
////////            ring[k] = node; // // ذخیره
////////        }
////////
////////        // جاده‌های حلقه: یکطرفه و ۲ لِین هم‌جهت (clockwise)
////////        for (int k = 0; k < arcSegments; k++) { // // هر قطعه
////////            Intersection A = ring[k]; // // شروع
////////            Intersection B = ring[(k + 1) % arcSegments]; // // بعدی
////////            Road R = new Road("RND-ARC-" + k, A, B, false); // // یکطرفه
////////            // جهت تقریبی این قطعه (برای نمایش صحیح خودرو)
////////            Direction dir = segmentDirection(A.getPosition(), B.getPosition()); // // جهت
////////            Lane l1 = new Lane("RND-ARC-" + k + "-L1", dir, R); l1.setOffsetIndex(-1); R.addForwardLane(l1); // // لِین داخلی
////////            Lane l2 = new Lane("RND-ARC-" + k + "-L2", dir, R); l2.setOffsetIndex(+1); R.addForwardLane(l2); // // لِین بیرونی
////////            map.addRoad(R); // // ثبت
////////        }
////////
////////        // چهار بازو: N/E/S/W – هر کدام ۴ لِین (۲ رفت به فلکه + ۲ برگشت)
////////        // انتخاب نزدیک‌ترین گره به هر جهت
////////        int iN = nearestIndex(ring, new Point(c.getX(), c.getY() - radius)); // // نزدیک بالا
////////        int iE = nearestIndex(ring, new Point(c.getX() + radius, c.getY())); // // نزدیک راست
////////        int iS = nearestIndex(ring, new Point(c.getX(), c.getY() + radius)); // // نزدیک پایین
////////        int iW = nearestIndex(ring, new Point(c.getX() - radius, c.getY())); // // نزدیک چپ
////////
////////        // بساز بازوها
////////        Intersection nFar = new Intersection("RND-NF", new Point(c.getX(), c.getY() - radius - armLen)); map.addIntersection(nFar); // // بیرون بالا
////////        Intersection eFar = new Intersection("RND-EF", new Point(c.getX() + radius + armLen, c.getY())); map.addIntersection(eFar); // // بیرون راست
////////        Intersection sFar = new Intersection("RND-SF", new Point(c.getX(), c.getY() + radius + armLen)); map.addIntersection(sFar); // // بیرون پایین
////////        Intersection wFar = new Intersection("RND-WF", new Point(c.getX() - radius - armLen, c.getY())); map.addIntersection(wFar); // // بیرون چپ
////////
////////        // هر بازو: جادهٔ اصلی ۴ لِین دوطرفه تا نزدیک حلقه + دو «اسلیپ» کوتاه یکطرفه ورود/خروج
////////        buildFourLaneBidirectional(map, nFar, ring[iN], Direction.SOUTH, Direction.NORTH, "RN-"); // // بازوی شمال
////////        buildSlipPair(map, ring[iN], Direction.EAST, "RNS"); // // اسلیپ ورود/خروج به حلقه سمت راست بازو
////////
////////        buildFourLaneBidirectional(map, eFar, ring[iE], Direction.WEST, Direction.EAST, "RE-");   // // بازوی شرق
////////        buildSlipPair(map, ring[iE], Direction.SOUTH, "RES"); // // اسلیپ‌ها
////////
////////        buildFourLaneBidirectional(map, sFar, ring[iS], Direction.NORTH, Direction.SOUTH, "RS-"); // // بازوی جنوب
////////        buildSlipPair(map, ring[iS], Direction.WEST, "RSS");  // // اسلیپ‌ها
////////
////////        buildFourLaneBidirectional(map, wFar, ring[iW], Direction.EAST, Direction.WEST, "RW-");   // // بازوی غرب
////////        buildSlipPair(map, ring[iW], Direction.NORTH, "RWS"); // // اسلیپ‌ها
////////    }
////////
////////    // ---------------- کمک: ساخت جادهٔ ۴ لِین دوطرفه بین A و B ----------------
////////    private static void buildFourLaneBidirectional(CityMap map,
////////                                                   Intersection A, Intersection B,
////////                                                   Direction toBForwardDir, Direction toABackDir,
////////                                                   String idPrefix) { // // سازنده جاده ۴ لِین بین دو تقاطع
////////        Road R = new Road(idPrefix + "MAIN", A, B, true); // // دوطرفه
////////        // دو لِین به سمت B (forward)
////////        Lane f1 = new Lane(idPrefix + "F1", toBForwardDir, R); f1.setOffsetIndex(-1); R.addForwardLane(f1); // // فوروارد ۱
////////        Lane f2 = new Lane(idPrefix + "F2", toBForwardDir, R); f2.setOffsetIndex(+1); R.addForwardLane(f2); // // فوروارد ۲
////////        // دو لِین به سمت A (backward)
////////        Lane b1 = new Lane(idPrefix + "B1", toABackDir, R); b1.setOffsetIndex(-1); R.addBackwardLane(b1); // // بک‌وارد ۱
////////        Lane b2 = new Lane(idPrefix + "B2", toABackDir, R); b2.setOffsetIndex(+1); R.addBackwardLane(b2); // // بک‌وارد ۲
////////        map.addRoad(R); // // ثبت
////////    }
////////
////////    // ---------------- کمک: دو اسلیپ کوتاه ورود/خروج یکطرفه کنار حلقه ----------------
////////    private static void buildSlipPair(CityMap map, Intersection nearOnRing, Direction flowDir, String tag) { // // اسلیپ‌های ورودی/خروجی
////////        // اسلیپ ورودی: از یک نقطه کمی دورتر به نزدیک‌ترین گره حلقه (یکطرفه به داخل)
////////        Point p = nearOnRing.getPosition(); // // مختصات گره حلقه
////////        Point inP = shiftPoint(p, flowDir, 35); // // نقطه شروع اسلیپ (کمی بیرون)
////////        Intersection inI = new Intersection(tag + "-IN", inP); map.addIntersection(inI); // // تقاطع مبدأ اسلیپ
////////        Road inR = new Road(tag + "-INR", inI, nearOnRing, false); // // یکطرفه به حلقه
////////        Lane inL = new Lane(tag + "-INL", flowDir, inR); inL.setOffsetIndex(0); inR.addForwardLane(inL); // // یک لِین
////////        map.addRoad(inR); // // ثبت
////////
////////        // اسلیپ خروجی: از گره حلقه به بیرون با جهت عمود مناسب
////////        Direction outDir = perpendicularCW(flowDir); // // جهت خروج
////////        Point outP = shiftPoint(p, outDir, 35); // // نقطه بیرونی
////////        Intersection outI = new Intersection(tag + "-OUT", outP); map.addIntersection(outI); // // تقاطع خروج
////////        Road outR = new Road(tag + "-OUTR", nearOnRing, outI, false); // // یکطرفه به بیرون
////////        Lane outL = new Lane(tag + "-OUTL", outDir, outR); outL.setOffsetIndex(0); outR.addForwardLane(outL); // // یک لِین
////////        map.addRoad(outR); // // ثبت
////////    }
////////
////////    // ---------------- یوتیلیتی‌های هندسی ساده ----------------
////////    private static Direction segmentDirection(Point a, Point b) { // // جهت تقریبی بردار AB
////////        int dx = b.getX() - a.getX(); // // Δx
////////        int dy = b.getY() - a.getY(); // // Δy
////////        if (Math.abs(dx) >= Math.abs(dy)) { // // افقی غالب
////////            return dx >= 0 ? Direction.EAST : Direction.WEST; // // شرق/غرب
////////        } else {
////////            return dy >= 0 ? Direction.SOUTH : Direction.NORTH; // // جنوب/شمال
////////        }
////////    }
////////
////////    private static int nearestIndex(Intersection[] arr, Point target) { // // نزدیک‌ترین گره به نقطه
////////        int best = 0; // // بهترین اندیس
////////        long bestD = Long.MAX_VALUE; // // بهترین فاصله
////////        for (int i = 0; i < arr.length; i++) {
////////            Point p = arr[i].getPosition(); // // مختصات
////////            long dx = p.getX() - target.getX(); long dy = p.getY() - target.getY(); // // Δ
////////            long d2 = dx * dx + dy * dy; // // فاصله مربع
////////            if (d2 < bestD) { bestD = d2; best = i; } // // به‌روزرسانی
////////        }
////////        return best; // // اندیس
////////    }
////////
////////    private static Point shiftPoint(Point p, Direction d, int dist) { // // انتقال نقطه در جهت d
////////        int x = p.getX(); int y = p.getY(); // // مبدا
////////        if (d == Direction.NORTH) y -= dist; // // بالا
////////        else if (d == Direction.SOUTH) y += dist; // // پایین
////////        else if (d == Direction.EAST)  x += dist; // // راست
////////        else if (d == Direction.WEST)  x -= dist; // // چپ
////////        return new Point(x, y); // // خروجی
////////    }
////////
////////    private static Direction perpendicularCW(Direction d) { // // عمود ساعت‌گرد
////////        if (d == Direction.NORTH) return Direction.EAST;  // // N→E
////////        if (d == Direction.EAST)  return Direction.SOUTH; // // E→S
////////        if (d == Direction.SOUTH) return Direction.WEST;  // // S→W
////////        return Direction.NORTH; // // W→N
////////    }
////////}
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
//////////package simulation; // // پکیج simulation
//////////
//////////import core.Direction; // // جهت‌ها
//////////import core.Point; // // مختصات
//////////import infrastructure.CityMap; // // نقشه
//////////import infrastructure.Intersection; // // تقاطع
//////////import infrastructure.Road; // // جاده
//////////import infrastructure.Lane; // // لِین
//////////
//////////import java.util.Random; // // نامنظمی کوچک
//////////
//////////public final class DemoMaps { // // نقشه‌های دمو
//////////    private DemoMaps() {} // // عدم نمونه‌سازی
//////////
//////////    public static CityMap irregularGrid(int rows, int cols, int blockW, int blockH, int gapX, int gapY) { // // گرید نامنظم
//////////        CityMap map = new CityMap(); // // نقشه
//////////        Random rnd = new Random(); // // رندوم
//////////
//////////        // ۱) ساخت تقاطع‌ها
//////////        Intersection[][] I = new Intersection[rows + 1][cols + 1]; // // ماتریس
//////////        for (int r = 0; r <= rows; r++) { // // سطر
//////////            for (int c = 0; c <= cols; c++) { // // ستون
//////////                int jx = rnd.nextInt(11) - 5; // // جابه‌جایی کوچک X
//////////                int jy = rnd.nextInt(11) - 5; // // جابه‌جایی کوچک Y
//////////                int x = c * (blockW + gapX) + jx; // // X
//////////                int y = r * (blockH + gapY) + jy; // // Y
//////////                Intersection inter = new Intersection("I-" + r + "-" + c, new Point(x, y)); // // ساخت
//////////                map.addIntersection(inter); // // افزودن
//////////                I[r][c] = inter; // // ذخیره
//////////            }
//////////        }
//////////
//////////        int majorRow = rows / 2; // // خیابان عریض افقی (ردیف وسط)
//////////
//////////        // ۲) جاده‌های افقی
//////////        for (int r = 0; r <= rows; r++) { // // سطر
//////////            for (int c = 0; c < cols; c++) { // // بین ستون‌های مجاور
//////////                Intersection A = I[r][c]; // // مبدأ
//////////                Intersection B = I[r][c + 1]; // // مقصد
//////////                Road R = new Road("RH-" + r + "-" + c, A, B, true); // // جاده دوطرفه
//////////
//////////                if (r == majorRow) { // // خیابان عریض: ۴ لِین (۲ رفت + ۲ برگشت)
//////////                    // قاعدهٔ مهم: لِین‌های «رفت» همه سمت + ، لِین‌های «برگشت» همه سمت -
//////////                    Lane f1 = new Lane("L-H-F1-" + r + "-" + c, Direction.EAST, R); f1.setOffsetIndex(+1); // // رفت۱ سمت +
//////////                    Lane f2 = new Lane("L-H-F2-" + r + "-" + c, Direction.EAST, R); f2.setOffsetIndex(+2); // // رفت۲ سمت +
//////////                    Lane b1 = new Lane("L-H-B1-" + r + "-" + c, Direction.WEST, R); b1.setOffsetIndex(-1); // // برگشت۱ سمت -
//////////                    Lane b2 = new Lane("L-H-B2-" + r + "-" + c, Direction.WEST, R); b2.setOffsetIndex(-2); // // برگشت۲ سمت -
//////////                    R.addForwardLane(f1); R.addForwardLane(f2); // // ثبت forward
//////////                    R.addBackwardLane(b1); R.addBackwardLane(b2); // // ثبت backward
//////////                } else { // // خیابان معمولی: ۲ لِین (۱ رفت + ۱ برگشت)
//////////                    Lane f = new Lane("L-H-F-" + r + "-" + c, Direction.EAST, R);  f.setOffsetIndex(+1); // // رفت سمت +
//////////                    Lane b = new Lane("L-H-B-" + r + "-" + c, Direction.WEST, R);  b.setOffsetIndex(-1); // // برگشت سمت -
//////////                    R.addForwardLane(f); R.addBackwardLane(b); // // ثبت
//////////                }
//////////                map.addRoad(R); // // افزودن جاده
//////////            }
//////////        }
//////////
//////////        // ۳) جاده‌های عمودی (در این نسخه همه معمولی: ۱ رفت + ۱ برگشت)
//////////        for (int c = 0; c <= cols; c++) { // // ستون
//////////            for (int r = 0; r < rows; r++) { // // بین سطرهای مجاور
//////////                Intersection A = I[r][c]; // // مبدأ
//////////                Intersection B = I[r + 1][c]; // // مقصد
//////////                Road R = new Road("RV-" + r + "-" + c, A, B, true); // // جاده
//////////                Lane f = new Lane("L-V-F-" + r + "-" + c, Direction.SOUTH, R); f.setOffsetIndex(+1); // // رفت سمت +
//////////                Lane b = new Lane("L-V-B-" + r + "-" + c, Direction.NORTH, R); b.setOffsetIndex(-1); // // برگشت سمت -
//////////                R.addForwardLane(f); R.addBackwardLane(b); // // ثبت
//////////                map.addRoad(R); // // افزودن
//////////            }
//////////        }
//////////
//////////        return map; // // خروجی
//////////    }
//////////}
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
////////
//////////package simulation; // // پکیج simulation
//////////
//////////import core.Direction; // // جهت‌ها
//////////import core.Point; // // مختصات
//////////import infrastructure.CityMap; // // نقشه
//////////import infrastructure.Intersection; // // تقاطع
//////////import infrastructure.Road; // // جاده
//////////import infrastructure.Lane; // // لِین
//////////
//////////import java.util.Random; // // نامنظمی کوچک برای طبیعی‌تر شدن
//////////
//////////public final class DemoMaps { // // کلاس کمکی ساخت نقشه‌ی دمو
//////////    private DemoMaps() {} // // جلوگیری از نمونه‌سازی
//////////
//////////    public static CityMap irregularGrid(int rows, int cols, int blockW, int blockH, int gapX, int gapY) { // // گرید نامنظم
//////////        CityMap map = new CityMap(); // // نقشه خالی
//////////        Random rnd = new Random(); // // رندوم
//////////
//////////        // --- ۱) ساخت تقاطع‌ها روی گره‌های شبکه ---
//////////        Intersection[][] I = new Intersection[rows + 1][cols + 1]; // // ماتریس تقاطع‌ها
//////////        for (int r = 0; r <= rows; r++) { // // سطرها
//////////            for (int c = 0; c <= cols; c++) { // // ستون‌ها
//////////                int jx = rnd.nextInt(11) - 5; // // جابه‌جایی کوچک افقی
//////////                int jy = rnd.nextInt(11) - 5; // // جابه‌جایی کوچک عمودی
//////////                int x = c * (blockW + gapX) + jx; // // X نهایی
//////////                int y = r * (blockH + gapY) + jy; // // Y نهایی
//////////                Intersection inter = new Intersection("I-" + r + "-" + c, new Point(x, y)); // // ساخت تقاطع
//////////                map.addIntersection(inter); // // افزودن به نقشه
//////////                I[r][c] = inter; // // ذخیره
//////////            }
//////////        }
//////////
//////////        // --- ۲) جاده‌های افقی (دوطرفه، دو لِین در هر جهت) ---
//////////        for (int r = 0; r <= rows; r++) { // // سطر
//////////            for (int c = 0; c < cols; c++) { // // بین ستون‌های مجاور
//////////                Intersection A = I[r][c]; // // مبدأ
//////////                Intersection B = I[r][c + 1]; // // مقصد
//////////                Road R = new Road("RH-" + r + "-" + c, A, B, true); // // جاده دوطرفه
//////////                // رفت به شرق
//////////                Lane f1 = new Lane("L-H-F1-" + r + "-" + c, Direction.EAST, R); // // لِین رفت ۱
//////////                Lane f2 = new Lane("L-H-F2-" + r + "-" + c, Direction.EAST, R); // // لِین رفت ۲
//////////                f1.setOffsetIndex(-1); // // کمی چپِ خط مرکزی (نسبت به جهت EAST)
//////////                f2.setOffsetIndex(+1); // // کمی راستِ خط مرکزی
//////////                R.addForwardLane(f1); // // افزودن به forward
//////////                R.addForwardLane(f2); // // افزودن به forward
//////////                // برگشت به غرب
//////////                Lane b1 = new Lane("L-H-B1-" + r + "-" + c, Direction.WEST, R); // // لِین برگشت ۱
//////////                Lane b2 = new Lane("L-H-B2-" + r + "-" + c, Direction.WEST, R); // // لِین برگشت ۲
//////////                b1.setOffsetIndex(-1); // // چپ (نسبت به WEST)
//////////                b2.setOffsetIndex(+1); // // راست
//////////                R.addBackwardLane(b1); // // افزودن به backward
//////////                R.addBackwardLane(b2); // // افزودن به backward
//////////                map.addRoad(R); // // ثبت جاده
//////////            }
//////////        }
//////////
//////////        // --- ۳) جاده‌های عمودی (دوطرفه، دو لِین در هر جهت) ---
//////////        for (int c = 0; c <= cols; c++) { // // ستون
//////////            for (int r = 0; r < rows; r++) { // // بین سطرهای مجاور
//////////                Intersection A = I[r][c]; // // مبدأ
//////////                Intersection B = I[r + 1][c]; // // مقصد
//////////                Road R = new Road("RV-" + r + "-" + c, A, B, true); // // جاده دوطرفه
//////////                // رفت به جنوب
//////////                Lane f1 = new Lane("L-V-F1-" + r + "-" + c, Direction.SOUTH, R); // // لِین رفت ۱
//////////                Lane f2 = new Lane("L-V-F2-" + r + "-" + c, Direction.SOUTH, R); // // لِین رفت ۲
//////////                f1.setOffsetIndex(-1); // // چپ نسبت به SOUTH
//////////                f2.setOffsetIndex(+1); // // راست
//////////                R.addForwardLane(f1); // // افزودن
//////////                R.addForwardLane(f2); // // افزودن
//////////                // برگشت به شمال
//////////                Lane b1 = new Lane("L-V-B1-" + r + "-" + c, Direction.NORTH, R); // // لِین برگشت ۱
//////////                Lane b2 = new Lane("L-V-B2-" + r + "-" + c, Direction.NORTH, R); // // لِین برگشت ۲
//////////                b1.setOffsetIndex(-1); // // چپ
//////////                b2.setOffsetIndex(+1); // // راست
//////////                R.addBackwardLane(b1); // // افزودن
//////////                R.addBackwardLane(b2); // // افزودن
//////////                map.addRoad(R); // // ثبت جاده
//////////            }
//////////        }
//////////
//////////        return map; // // نقشه نهایی
//////////    }
//////////}
