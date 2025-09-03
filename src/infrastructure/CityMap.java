
package infrastructure; // // پکیج زیرساخت

import simulation.PathFinder; // // برای صدا زدن دیکسترا
import core.Route; // // نوع Route
import java.util.ArrayList; // // لیست
import java.util.List; // // اینترفیس

public class CityMap { // // نقشه شهر (راه‌ها و تقاطع‌ها)
    private final List<Intersection> intersections; // // لیست تقاطع‌ها
    private final List<Road> roads; // // لیست راه‌ها

    public CityMap() { // // سازنده
        this.intersections = new ArrayList<Intersection>(); // // لیست خالی تقاطع‌ها
        this.roads = new ArrayList<Road>(); // // لیست خالی راه‌ها
    }

    public void addIntersection(Intersection i) { // // افزودن تقاطع
        intersections.add(i); // // اضافه به لیست
    }

    public void addRoad(Road r) { // // افزودن راه
        roads.add(r); // // اضافه به لیست
    }

    public List<Intersection> getIntersections() { // // گتر لیست تقاطع‌ها
        return intersections; // // خروجی
    }

    public List<Road> getRoads() { // // گتر لیست راه‌ها
        return roads; // // خروجی
    }

    // همه راه‌های متصل به یک تقاطع //
    public List<Road> getAdjacentRoads(Intersection i) { // // همسایه‌های راه برای تقاطع i
        List<Road> res = new ArrayList<Road>(); // // خروجی
        for (int k = 0; k < roads.size(); k++) { // // پیمایش راه‌ها
            Road r = roads.get(k); // // یک راه
            if (r.getStartIntersection() == i || r.getEndIntersection() == i) { // // اگر به i وصل است
                res.add(r); // // افزودن
            }
        }
        return res; // // خروجی
    }

    // گرفتن راه بین دو تقاطع (اگر وجود داشته باشد) //
    public Road getRoadBetween(Intersection a, Intersection b) { // // راه بین a و b
        for (int k = 0; k < roads.size(); k++) { // // پیمایش
            Road r = roads.get(k); // // یک راه
            if ((r.getStartIntersection() == a && r.getEndIntersection() == b) ||
                    (r.getStartIntersection() == b && r.getEndIntersection() == a)) { // // دو جهت
                return r; // // یافت شد
            }
        }
        return null; // // نبود
    }

    // متد راحت برای پیدا کردن مسیر بین دو لِین //
    public Route findRoute(Lane start, Lane end) { // // ساخت Route بین start و end
        PathFinder pf = new PathFinder(this); // // ساخت یابنده مسیر
        return pf.findBestRoute(start, end); // // اجرای دیکسترا و برگرداندن Route
    }
}
































//package infrastructure;
//
//import core.Point;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CityMap {
//    private final List<Intersection> intersections = new ArrayList<>();
//    private final List<Road> roads = new ArrayList<>();
//
//    public List<Intersection> getIntersections() { return intersections; }
//    public List<Road> getRoads() { return roads; }
//
//    // سازنده‌ی ساده‌ی اینترسکشن (برای DemoMaps)
//    public Intersection newIntersection(String id, int x, int y) {
//        Intersection it = new Intersection(id, new Point(x, y));
//        intersections.add(it);
//        return it;
//    }
//
//    public Road addRoad(String id, Intersection a, Intersection b, boolean twoWay) {
//        Road r = new Road(id, a, b, twoWay);
//        roads.add(r);
//        return r;
//    }
//
//    public void addForwardLane(Road road, Lane lane) {
//        if (road != null && lane != null) road.addForwardLane(lane);
//    }
//
//    public void addBackwardLane(Road road, Lane lane) {
//        if (road != null && lane != null) road.addBackwardLane(lane);
//    }
//}



























//package infrastructure;
//
//import java.util.*;
//
///**
// * CityMap with helpers required by World/DemoMaps:
// *  - getRandomEntryLane()
// *  - pickRandomCrossings(...)
// *  - addPedestrianCrossing(...)
// *  - getLanePosition(...)
// * و alias های لازم.
// */
//public class CityMap {
//    private final List<Intersection> intersections = new ArrayList<>();
//    private final List<Road> roads = new ArrayList<>();
//    private final List<pedestrian.PedestrianCrossing> crossings = new ArrayList<>();
//    private final Random rnd = new Random();
//
//    public List<Intersection> getIntersections(){ return intersections; }
//    public List<Road> getRoads(){ return roads; }
//
//    public void addIntersection(Intersection i){ if(i!=null) intersections.add(i); }
//    public void addRoad(Road r){ if(r!=null) roads.add(r); }
//
//    // ---------- مورد استفاده DemoMaps/World ----------
//    /** یک لاین ورودی تصادفی (برای seed کردن خودروها). */
//    public Lane getRandomEntryLane(){
//        if(roads.isEmpty()) return null;
//        // ترجیحاً یک لاین forward با جهت EAST/NORTH/SOUTH/WEST بدون توجه به roundabout
//        Road r = roads.get(rnd.nextInt(roads.size()));
//        List<Lane> candidates = r.getForwardLanes();
//        if(candidates.isEmpty() && r.isTwoWay()) candidates = r.getBackwardLanes();
//        return candidates.isEmpty() ? null : candidates.get(rnd.nextInt(candidates.size()));
//    }
//
//    /** برگرداندن چند گذرگاه عابر به‌صورت تصادفی. */
//    public List<pedestrian.PedestrianCrossing> pickRandomCrossings(int count, boolean awayFromRoundabout){
//        List<pedestrian.PedestrianCrossing> pool = new ArrayList<>(crossings);
//        // اگر لازم بود می‌توان awayFromRoundabout را اینجا فیلتر کرد
//        Collections.shuffle(pool, rnd);
//        if(count <= 0) return Collections.emptyList();
//        return pool.subList(0, Math.min(count, pool.size()));
//    }
//
//    /** ثبت گذرگاه عابر پیاده در نقشه (برای DemoMaps). */
//    public void addPedestrianCrossing(pedestrian.PedestrianCrossing pc){
//        if(pc != null) crossings.add(pc);
//    }
//
//    /** همهٔ گذرگاه‌ها؛ برای World.getCrossings() قدیمی. */
//    public List<pedestrian.PedestrianCrossing> getCrossings(){ return Collections.unmodifiableList(crossings); }
//
//    /** سازگاری: موقعیت طولی لاین (می‌تواند برای رندر/محاسبه فاصله استفاده شود). */
//    public double getLanePosition(Lane ln){
//        return (ln == null) ? 0.0 : ln.getLength()*0.5; // نقطهٔ میان لاین به‌عنوان پیش‌فرض
//    }
//
//    public Intersection newIntersection(String n, int i, int i1) {
//        return null;
//    }
//
//    public Lane pickRandomEntryLane() {
//        return null;
//    }
//
//    // اگر در جایی Route نیاز شد، بعداً اضافه می‌کنیم.
//}





























//
//
//package infrastructure;
//
//import core.*;
//import pedestrian.PedestrianCrossing;
//
//import java.util.*;
//
///** نگه‌دارنده تقاطع‌ها/جاده‌ها + چند هِلپر مورد نیاز دمو/ورلد */
//public class CityMap {
//
//    private final List<Intersection> intersections = new ArrayList<>();
//    private final List<Road> roads = new ArrayList<>();
//    private final List<PedestrianCrossing> crossings = new ArrayList<>();
//
//    public List<Intersection> getIntersections() { return Collections.unmodifiableList(intersections); }
//    public List<Road> getRoads() { return Collections.unmodifiableList(roads); }
//
//    public void addIntersection(Intersection i) { if (i != null) intersections.add(i); }
//    public void addRoad(Road r) { if (r != null) roads.add(r); }
//
//    // --- برای DemoMaps: ثبت عابر‌گذر ---
//    public void addPedestrianCrossing(PedestrianCrossing pc) {
//        if (pc != null) {
//            crossings.add(pc);
//            // اگر لازم شد به نزدیک‌ترین Road هم وصلش کن
//            Road nearest = findNearestRoad(pc.getIntersection());
//            if (nearest != null) nearest.addCrossing(pc);
//        }
//    }
//
//    public List<PedestrianCrossing> getCrossings() { return Collections.unmodifiableList(crossings); }
//
//    // انتخاب تصادفی چند crossing (برای تزریق در World)
//    public List<PedestrianCrossing> pickRandomCrossings(int count, boolean excludeNearRoundabouts) {
//        ArrayList<PedestrianCrossing> list = new ArrayList<>(crossings);
//        if (excludeNearRoundabouts) {
//            list.removeIf(pc -> {
//                Road r = findNearestRoad(pc.getIntersection());
//                return r != null && r.isNearRoundabout();
//            });
//        }
//        Collections.shuffle(list);
//        if (count <= 0 || count >= list.size()) return list;
//        return list.subList(0, count);
//    }
//
//    // موقعیت تقریبیِ مرکز یک لِین (برای UI/World)
//    public Point getLanePosition(Lane ln) {
//        if (ln == null || ln.getParentRoad() == null) return new Point(0, 0);
//        Road rd = ln.getParentRoad();
//        Point a = rd.getStartIntersection().getPosition();
//        Point b = rd.getEndIntersection().getPosition();
//        // نقطه‌ی میانی + آفست جانبی ساده
//        int midx = (a.getX() + b.getX()) / 2;
//        int midy = (a.getY() + b.getY()) / 2;
//        return new Point(midx, midy);
//    }
//
//    // ساده‌ترین Route – اختیاراً بعداً PathFinder
//    public Route findRoute(Lane start, Lane end) {
//        Route r = new Route(start, end, new ArrayList<>());
//        r.getLanesInOrder().add(start);
//        if (end != null && end != start) r.getLanesInOrder().add(end);
//        return r;
//    }
//
//    private Road findNearestRoad(Intersection i) {
//        if (i == null) return null;
//        Road ans = null;
//        double best = Double.MAX_VALUE;
//        for (Road r : roads) {
//            Point a = r.getStartIntersection().getPosition();
//            Point b = r.getEndIntersection().getPosition();
//            double cx = (a.getX() + b.getX()) / 2.0;
//            double cy = (a.getY() + b.getY()) / 2.0;
//            double dx = i.getPosition().getX() - cx;
//            double dy = i.getPosition().getY() - cy;
//            double d2 = dx*dx + dy*dy;
//            if (d2 < best) { best = d2; ans = r; }
//        }
//        return ans;
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
//
//
//
//
//
////
////package infrastructure; // // پکیج زیرساخت
////
////import java.util.ArrayList; // // برای لیست‌ها
////import java.util.List; // // اینترفیس لیست
////import core.Route; // // برای امضای findRoute (در فاز بعدی)
////import core.Direction; // // اگر بعداً خواستیم استفاده کنیم
////import core.Point; // // اگر بعداً ساخت نقشه داخلی داشتیم
////import pedestrian.PedestrianCrossing;
////
////public class CityMap { // // کلاس نقشه شهر
////    private final List<Intersection> intersections; // // لیست تقاطع‌ها
////    private final List<Road> roads; // // لیست جاده‌ها
////
////    public CityMap() { // // سازندهٔ پیش‌فرض
////        this.intersections = new ArrayList<Intersection>(); // // ساخت لیست تقاطع‌ها
////        this.roads = new ArrayList<Road>(); // // ساخت لیست جاده‌ها
////    }
////
////    public List<Intersection> getIntersections() { // // گرفتن همهٔ تقاطع‌ها
////        return this.intersections; // // برگرداندن لیست
////    }
////
////    public List<Road> getRoads() { // // گرفتن همهٔ جاده‌ها
////        return this.roads; // // برگرداندن لیست
////    }
////
////    // ====== متدهای کمکی موردنیاز DemoMaps/DemoTraffic ======
////    public void addIntersection(Intersection it) { // // افزودن تقاطع
////        if (it != null) { this.intersections.add(it); } // // چک نال و افزودن
////    }
////
////    public void addRoad(Road r) { // // افزودن جاده
////        if (r != null) { this.roads.add(r); } // // چک نال و افزودن
////    }
////
////    // ====== متدهای موردنیاز PathFinder ======
////    public List<Road> getAdjacentRoads(Intersection at) { // // همهٔ جاده‌هایی که به این تقاطع وصل‌اند
////        ArrayList<Road> out = new ArrayList<Road>(); // // خروجی
////        if (at == null) { return out; } // // اگر ورودی نال بود
////        for (int i = 0; i < this.roads.size(); i++) { // // پیمایش همهٔ جاده‌ها
////            Road r = this.roads.get(i); // // جاده iام
////            if (r.getStart() == at || r.getEnd() == at) { // // اگر به این تقاطع متصل است
////                out.add(r); // // افزودن به خروجی
////            }
////        }
////        return out; // // برگرداندن لیست
////    }
////
////    public Road getRoadBetween(Intersection a, Intersection b) { // // پیدا کردن جاده بین دو تقاطع
////        if (a == null || b == null) { return null; } // // چک نال
////        for (int i = 0; i < this.roads.size(); i++) { // // پیمایش جاده‌ها
////            Road r = this.roads.get(i); // // جاده
////            Intersection s = r.getStart(); // // سر
////            Intersection e = r.getEnd(); // // ته
////            // بدون توجه به ترتیب (دو حالت) //
////            if ((s == a && e == b) || (s == b && e == a)) { // // اگر جاده مستقیماً این دو را وصل می‌کند
////                return r; // // همان را برگردان
////            }
////        }
////        return null; // // اگر نبود
////    }
////
////    // ====== جای‌نگه‌دار مسیر (در فاز بعدی پیاده‌سازی می‌شود) ======
////    public Route findRoute(Lane start, Lane end) { // // مسیردهی (فعلاً خالی)
////        return null; // // بعداً با دایکسترا تکمیل می‌شود
////    }
////
////
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
//////package infrastructure; // // پکیج زیرساخت
//////
//////import java.util.ArrayList; // // برای لیست‌ها
//////import java.util.List; // // اینترفیس لیست
//////import java.util.Random; // // در صورت نیاز به ساخت ارگانیک
//////import core.Point; // // مختصات
//////import core.Direction; // // جهت‌ها
//////import core.Route; // // برای امضای findRoute (در فاز بعدی تکمیل می‌شود)
//////
//////public class CityMap { // // کلاس نقشه شهر
//////    private final List<Intersection> intersections; // // لیست تقاطع‌ها
//////    private final List<Road> roads; // // لیست جاده‌ها
//////
//////    public CityMap() { // // سازندهٔ پیش‌فرض
//////        this.intersections = new ArrayList<Intersection>(); // // ساخت لیست تقاطع‌ها
//////        this.roads = new ArrayList<Road>(); // // ساخت لیست جاده‌ها
//////    }
//////
//////    public List<Intersection> getIntersections() { return this.intersections; } // // گرفتن تقاطع‌ها
//////    public List<Road> getRoads() { return this.roads; } // // گرفتن جاده‌ها
//////
//////    // ====== متدهای موردنیاز DemoMaps ======
//////    public void addIntersection(Intersection it) { // // افزودن تقاطع
//////        if (it != null) { this.intersections.add(it); } // // چک نال و افزودن
//////    }
//////
//////    public void addRoad(Road r) { // // افزودن جاده
//////        if (r != null) { this.roads.add(r); } // // چک نال و افزودن
//////    }
//////
//////    // ====== (اختیاری) ساخت گرید‌های آماده؛ اگر قبلاً داری می‌تونی نگه‌داری ======
//////    // ... (اگر نسخه قبلی buildGrid/buildOrganicGrid داری، همون‌ها کافیه)
//////
//////    public Route findRoute(Lane start, Lane end) { // // جای‌نگه‌دار مسیر
//////        return null; // // در فاز PathFinder تکمیل می‌شود
//////    }
//////}
//////
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
//////package infrastructure; // // پکیج زیرساخت
//////
//////import java.util.ArrayList; // // برای لیست‌ها
//////import java.util.List; // // اینترفیس لیست
//////import java.util.Random; // // برای پراکندگی کنترل‌شده
//////import core.Point; // // مختصات تقاطع‌ها
//////import core.Direction; // // جهت‌ها
//////import core.Route; // // امضا برای findRoute (بعداً تکمیل می‌شود)
//////
//////public class CityMap { // // کلاس نقشه شهر
//////    private final List<Intersection> intersections; // // لیست تقاطع‌ها
//////    private final List<Road> roads; // // لیست جاده‌ها
//////
//////    public CityMap() { // // سازندهٔ پیش‌فرض
//////        this.intersections = new ArrayList<Intersection>(); // // ساخت لیست تقاطع‌ها
//////        this.roads = new ArrayList<Road>(); // // ساخت لیست جاده‌ها
//////    }
//////
//////    public List<Intersection> getIntersections() { return this.intersections; } // // گرفتن تقاطع‌ها
//////    public List<Road> getRoads() { return this.roads; } // // گرفتن جاده‌ها
//////
//////    // ==============================
//////    // گرید منظم (قرینه) - در صورت نیاز
//////    // ==============================
//////    public void buildGrid(int rows, int cols, int blockSizePx, int lanesPerDirection, boolean twoWay) { // // ساخت گرید منظم
//////        this.intersections.clear(); // // پاک‌سازی قبلی
//////        this.roads.clear(); // // پاک‌سازی قبلی
//////
//////        Intersection[][] grid = new Intersection[rows][cols]; // // ماتریس تقاطع‌ها
//////        for (int r = 0; r < rows; r++) { // // حلقه سطرها
//////            for (int c = 0; c < cols; c++) { // // حلقه ستون‌ها
//////                String id = "I_" + r + "_" + c; // // شناسه یکتا
//////                Point pos = new Point(c * blockSizePx, r * blockSizePx); // // مختصات یکنواخت
//////                Intersection it = new Intersection(id, pos); // // ساخت تقاطع
//////                grid[r][c] = it; // // ذخیره در ماتریس
//////                this.intersections.add(it); // // افزودن به لیست
//////            }
//////        }
//////
//////        int rc = 0; // // شمارنده جاده‌ها
//////        for (int r = 0; r < rows; r++) { // // حلقه سطرها
//////            for (int c = 0; c < cols; c++) { // // حلقه ستون‌ها
//////                if (c + 1 < cols) { // // همسایه راست
//////                    addRoadWithLanes("R_H_" + (rc++), grid[r][c], grid[r][c + 1],
//////                            Direction.EAST, Direction.WEST, lanesPerDirection, twoWay); // // جاده افقی
//////                }
//////                if (r + 1 < rows) { // // همسایه پایین
//////                    addRoadWithLanes("R_V_" + (rc++), grid[r][c], grid[r + 1][c],
//////                            Direction.SOUTH, Direction.NORTH, lanesPerDirection, twoWay); // // جاده عمودی
//////                }
//////            }
//////        }
//////    }
//////
//////    // =========================================
//////    // گرید ارگانیک (غیرقرینه) - شبیه شهر واقعی
//////    // =========================================
//////    public void buildOrganicGrid(int rows, int cols, int baseBlockPx, int jitterPx,
//////                                 int lanesPerDirection, boolean twoWay, long seed) { // // ساخت گرید ارگانیک
//////        this.intersections.clear(); // // پاک‌سازی تقاطع‌ها
//////        this.roads.clear(); // // پاک‌سازی جاده‌ها
//////
//////        Random rng = new Random(seed); // // تصادفی با سید ثابت
//////
//////        int[] colStep = new int[cols]; // // فاصله‌های افقی
//////        int[] rowStep = new int[rows]; // // فاصله‌های عمودی
//////
//////        for (int c = 0; c < cols; c++) { // // تعیین فاصله هر ستون
//////            int jit = (jitterPx > 0) ? rng.nextInt(jitterPx + 1) : 0; // // پراکندگی 0..jitter
//////            int sign = rng.nextBoolean() ? 1 : -1; // // علامت پراکندگی
//////            colStep[c] = baseBlockPx + sign * jit; // // فاصله واقعی
//////            if (colStep[c] < baseBlockPx / 2) { colStep[c] = baseBlockPx / 2; } // // کف ایمنی
//////        }
//////        for (int r = 0; r < rows; r++) { // // تعیین فاصله هر سطر
//////            int jit = (jitterPx > 0) ? rng.nextInt(jitterPx + 1) : 0; // // پراکندگی 0..jitter
//////            int sign = rng.nextBoolean() ? 1 : -1; // // علامت
//////            rowStep[r] = baseBlockPx + sign * jit; // // فاصله واقعی
//////            if (rowStep[r] < baseBlockPx / 2) { rowStep[r] = baseBlockPx / 2; } // // کف ایمنی
//////        }
//////
//////        int[] colX = new int[cols]; // // X ستون‌ها (انباشت)
//////        int[] rowY = new int[rows]; // // Y سطرها (انباشت)
//////        int acc = 0; // // انباشتی
//////        for (int c = 0; c < cols; c++) { colX[c] = acc; acc += colStep[c]; } // // تولید مختصات X
//////        acc = 0; // // ریست
//////        for (int r = 0; r < rows; r++) { rowY[r] = acc; acc += rowStep[r]; } // // تولید مختصات Y
//////
//////        Intersection[][] grid = new Intersection[rows][cols]; // // ماتریس تقاطع
//////        for (int r = 0; r < rows; r++) { // // حلقه سطر
//////            for (int c = 0; c < cols; c++) { // // حلقه ستون
//////                String id = "I_" + r + "_" + c; // // شناسه یکتا
//////                Intersection it = new Intersection(id, new Point(colX[c], rowY[r])); // // ساخت تقاطع غیرقرینه
//////                grid[r][c] = it; // // ذخیره
//////                this.intersections.add(it); // // افزودن به لیست
//////            }
//////        }
//////
//////        int rc = 0; // // شمارنده جاده
//////        for (int r = 0; r < rows; r++) { // // حلقه سطر
//////            for (int c = 0; c < cols; c++) { // // حلقه ستون
//////                if (c + 1 < cols) { // // همسایه راست
//////                    addRoadWithLanes("R_H_" + (rc++), grid[r][c], grid[r][c + 1],
//////                            Direction.EAST, Direction.WEST, lanesPerDirection, twoWay); // // جاده افقی
//////                }
//////                if (r + 1 < rows) { // // همسایه پایین
//////                    addRoadWithLanes("R_V_" + (rc++), grid[r][c], grid[r + 1][c],
//////                            Direction.SOUTH, Direction.NORTH, lanesPerDirection, twoWay); // // جاده عمودی
//////                }
//////            }
//////        }
//////    }
//////
//////    private void addRoadWithLanes(String roadId, Intersection a, Intersection b,
//////                                  Direction forwardDir, Direction backwardDir,
//////                                  int lanesPerDirection, boolean twoWay) { // // کمکی ساخت جاده+لِین
//////        Road road = new Road(roadId, a, b, twoWay); // // ساخت جاده
//////        List<Lane> forward = new ArrayList<Lane>(); // // لیست لِین‌های forward
//////        List<Lane> backward = new ArrayList<Lane>(); // // لیست لِین‌های backward
//////        for (int i = 0; i < lanesPerDirection; i++) { // // ساخت n لِین
//////            String lidF = roadId + "_F_" + i; // // id لِین forward
//////            Lane lf = new Lane(lidF, forwardDir, road); // // لِین forward
//////            forward.add(lf); // // افزودن
//////            if (twoWay) { // // اگر دوطرفه
//////                String lidB = roadId + "_B_" + i; // // id لِین backward
//////                Lane lb = new Lane(lidB, backwardDir, road); // // لِین backward
//////                backward.add(lb); // // افزودن
//////            }
//////        }
//////        road.setForwardLanes(forward); // // ثبت لِین‌های forward در جاده
//////        road.setBackwardLanes(backward); // // ثبت لِین‌های backward در جاده
//////        this.roads.add(road); // // افزودن جاده به نقشه
//////    }
//////
//////    public Route findRoute(Lane start, Lane end) { // // جای‌نگه‌دار مسیر (فاز بعدی)
//////        return null; // // فعلاً خالی
//////    }
//////}
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
//////package infrastructure; // // پکیج زیرساخت
//////
//////import java.util.ArrayList; // // برای لیست‌ها
//////import java.util.List; // // اینترفیس لیست
//////import core.Point; // // مختصات تقاطع‌ها
//////import core.Direction; // // جهت لِین‌ها
//////import core.Route; // // برای امضای findRoute (فعلاً جای‌نگه‌دار)
//////
//////// توجه: اگر پکیج‌های پروژه‌ات متفاوت‌اند، فقط همین importها را با ساختار خودت هماهنگ کن.
//////
//////public class CityMap { // // کلاس نقشه شهر
//////    private final List<Intersection> intersections; // // لیست تقاطع‌ها
//////    private final List<Road> roads; // // لیست جاده‌ها
//////
//////    public CityMap() { // // سازنده پیش‌فرض
//////        this.intersections = new ArrayList<Intersection>(); // // ایجاد لیست تقاطع‌ها
//////        this.roads = new ArrayList<Road>(); // // ایجاد لیست جاده‌ها
//////    }
//////
//////    public List<Intersection> getIntersections() { // // گرفتن تقاطع‌ها
//////        return this.intersections; // // برگرداندن لیست
//////    }
//////
//////    public List<Road> getRoads() { // // گرفتن جاده‌ها
//////        return this.roads; // // برگرداندن لیست
//////    }
//////
//////    // =========================
//////    //  ساخت نقشه گرید ۷×۷
//////    // =========================
//////    public void buildGrid(int rows, int cols, int blockSizePx, int lanesPerDirection, boolean twoWay) { // // ساخت گرید
//////        this.intersections.clear(); // // پاک‌سازی قبلی تقاطع‌ها
//////        this.roads.clear(); // // پاک‌سازی قبلی جاده‌ها
//////
//////        Intersection[][] grid = new Intersection[rows][cols]; // // ماتریس تقاطع‌ها
//////
//////        // 1) ایجاد تقاطع‌ها
//////        for (int r = 0; r < rows; r++) { // // حلقه سطرها
//////            for (int c = 0; c < cols; c++) { // // حلقه ستون‌ها
//////                String id = "I_" + r + "_" + c; // // شناسه یکتا
//////                int x = c * blockSizePx; // // مختصات X
//////                int y = r * blockSizePx; // // مختصات Y
//////                Point pos = new Point(x, y); // // نقطه مکان
//////                Intersection it = new Intersection(id, pos); // // ساخت تقاطع
//////                grid[r][c] = it; // // قرار دادن در ماتریس
//////                this.intersections.add(it); // // افزودن به لیست
//////            }
//////        }
//////
//////        // 2) ایجاد جاده‌ها بین همسایه‌ها
//////        int rc = 0; // // شمارنده id جاده
//////        for (int r = 0; r < rows; r++) { // // حلقه سطرها
//////            for (int c = 0; c < cols; c++) { // // حلقه ستون‌ها
//////                if (c + 1 < cols) { // // اگر همسایه راست هست
//////                    Intersection a = grid[r][c]; // // تقاطع چپ
//////                    Intersection b = grid[r][c + 1]; // // تقاطع راست
//////                    String rid = "R_H_" + (rc++); // // id جاده افقی
//////                    Road road = new Road(rid, a, b, twoWay); // // ساخت جاده
//////                    List<Lane> forward = new ArrayList<Lane>(); // // لیست لِین‌های راست‌رو (EAST)
//////                    List<Lane> backward = new ArrayList<Lane>(); // // لیست لِین‌های چپ‌رو (WEST)
//////                    for (int i = 0; i < lanesPerDirection; i++) { // // ساخت n لِین
//////                        String lidF = rid + "_F_" + i; // // id لِین forward
//////                        Lane lf = new Lane(lidF, Direction.EAST, road); // // لِین به سمت شرق
//////                        forward.add(lf); // // افزودن
//////                        if (twoWay) { // // اگر دوطرفه
//////                            String lidB = rid + "_B_" + i; // // id لِین backward
//////                            Lane lb = new Lane(lidB, Direction.WEST, road); // // لِین به سمت غرب
//////                            backward.add(lb); // // افزودن
//////                        }
//////                    }
//////                    road.setForwardLanes(forward); // // ثبت لِین‌های forward در جاده
//////                    road.setBackwardLanes(backward); // // ثبت لِین‌های backward در جاده
//////                    this.roads.add(road); // // افزودن جاده به لیست
//////                }
//////
//////                if (r + 1 < rows) { // // اگر همسایه پایین هست
//////                    Intersection a = grid[r][c]; // // تقاطع بالا
//////                    Intersection b = grid[r + 1][c]; // // تقاطع پایین
//////                    String rid = "R_V_" + (rc++); // // id جاده عمودی
//////                    Road road = new Road(rid, a, b, twoWay); // // ساخت جاده
//////                    List<Lane> forward = new ArrayList<Lane>(); // // لِین‌های رو به جنوب
//////                    List<Lane> backward = new ArrayList<Lane>(); // // لِین‌های رو به شمال
//////                    for (int i = 0; i < lanesPerDirection; i++) { // // ساخت n لِین
//////                        String lidF = rid + "_F_" + i; // // id لِین forward
//////                        Lane lf = new Lane(lidF, Direction.SOUTH, road); // // لِین به سمت جنوب
//////                        forward.add(lf); // // افزودن
//////                        if (twoWay) { // // اگر دوطرفه
//////                            String lidB = rid + "_B_" + i; // // id لِین backward
//////                            Lane lb = new Lane(lidB, Direction.NORTH, road); // // لِین به سمت شمال
//////                            backward.add(lb); // // افزودن
//////                        }
//////                    }
//////                    road.setForwardLanes(forward); // // ثبت لِین‌های forward
//////                    road.setBackwardLanes(backward); // // ثبت لِین‌های backward
//////                    this.roads.add(road); // // افزودن جاده
//////                }
//////            }
//////        }
//////    }
//////
//////    // جای‌نگه‌دار مسیر؛ در مرحله‌ی PathFinder تکمیل می‌شود
//////    public Route findRoute(Lane start, Lane end) { // // امضای مسیریابی
//////        return null; // // فعلاً پیاده‌سازی نشده
//////    }
//////}
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
//////package infrastructure; // // پکیج زیرساخت نقشه/جاده/تقاطع
//////
//////import java.util.ArrayList; // // استفاده از لیست
//////import java.util.List; // // اینترفیس لیست
//////import java.util.HashMap; // // دیکشنری کمکی
//////import java.util.Map; // // اینترفیس مپ
//////import core.Direction; // // جهت‌ها
//////import core.Identifiable; // // برای سازگاری
//////import core.Vehicle; // // در صورت نیاز آینده
//////import trafficcontrol.TrafficControlDevice; // // کنترل تقاطع
//////import core.Point; // // نقطه دوبعدی
//////// توجه: اگر پکیج‌های پروژه‌ات فرق دارند، فقط importها را مطابق ساختار خودت اصلاح کن. //
//////
//////public class CityMap { // // کلاس نقشهٔ شهر
//////    private final List<Intersection> intersections; // // لیست تقاطع‌ها
//////    private final List<Road> roads; // // لیست جاده‌ها
//////
//////    public CityMap() { // // سازنده پیش‌فرض
//////        this.intersections = new ArrayList<Intersection>(); // // ساخت لیست تقاطع‌ها
//////        this.roads = new ArrayList<Road>(); // // ساخت لیست جاده‌ها
//////    }
//////
//////    public List<Intersection> getIntersections() { // // گرفتن همه تقاطع‌ها
//////        return this.intersections; // // برگرداندن لیست
//////    }
//////
//////    public List<Road> getRoads() { // // گرفتن همه جاده‌ها
//////        return this.roads; // // برگرداندن لیست
//////    }
//////
//////    // =========================
//////    //  سازندهٔ نقشهٔ گرید ۷×۷
//////    // =========================
//////    public void buildGrid(int rows, int cols, int blockSizePx, int lanesPerDirection, boolean twoWay) { // // ساخت گرید
//////        this.intersections.clear(); // // پاک‌سازی قبلی
//////        this.roads.clear(); // // پاک‌سازی قبلی
//////
//////        // --- 1) ساخت تقاطع‌ها روی گرید ---
//////        Intersection[][] grid = new Intersection[rows][cols]; // // ماتریس تقاطع‌ها
//////        for (int r = 0; r < rows; r++) { // // حلقه سطرها
//////            for (int c = 0; c < cols; c++) { // // حلقه ستون‌ها
//////                String id = "I_" + r + "_" + c; // // شناسه یکتا برای هر تقاطع
//////                int x = c * blockSizePx; // // مختصات X بر اساس اندازه بلوک
//////                int y = r * blockSizePx; // // مختصات Y بر اساس اندازه بلوک
//////                Point p = new Point(x, y); // // ساخت نقطهٔ مکان تقاطع
//////                Intersection it = new Intersection(id, p); // // فرض: سازنده (id, point) موجود است
//////                grid[r][c] = it; // // قرار دادن در ماتریس
//////                this.intersections.add(it); // // افزودن به لیست سراسری
//////            }
//////        }
//////
//////        // --- 2) ساخت جاده‌های افقی و عمودی بین تقاطع‌های همسایه ---
//////        int roadCounter = 0; // // شمارنده برای id جاده
//////        for (int r = 0; r < rows; r++) { // // حلقه سطرها
//////            for (int c = 0; c < cols; c++) { // // حلقه ستون‌ها
//////                if (c + 1 < cols) { // // اگر همسایهٔ راست وجود دارد
//////                    Intersection a = grid[r][c]; // // تقاطع چپ
//////                    Intersection b = grid[r][c + 1]; // // تقاطع راست
//////                    String rid = "R_H_" + (roadCounter++); // // id یکتا برای جاده افقی
//////                    Road road = new Road(rid, a, b, twoWay); // // فرض: سازنده (id, start, end, twoWay)
//////                    // لِین‌های رفت و برگشت
//////                    List<Lane> forward = new ArrayList<Lane>(); // // لیست لِین‌های رو به راست
//////                    List<Lane> backward = new ArrayList<Lane>(); // // لیست لِین‌های رو به چپ
//////                    for (int i = 0; i < lanesPerDirection; i++) { // // ساخت n لِین در هر جهت
//////                        String lidF = rid + "_F_" + i; // // id لِین رفت
//////                        Lane lf = new Lane(lidF, Direction.EAST, road); // // فرض: سازنده (id, direction, parentRoad)
//////                        forward.add(lf); // // افزودن لِین رفت
//////                        if (twoWay) { // // اگر جاده دوطرفه است
//////                            String lidB = rid + "_B_" + i; // // id لِین برگشت
//////                            Lane lb = new Lane(lidB, Direction.WEST, road); // // ساخت لِین برگشت
//////                            backward.add(lb); // // افزودن لِین برگشت
//////                        }
//////                    }
//////                    road.setForwardLanes(forward); // // ست لیست لِین‌های رو به راست
//////                    road.setBackwardLanes(backward); // // ست لیست لِین‌های رو به چپ
//////                    this.roads.add(road); // // افزودن جاده به لیست
//////                }
//////
//////                if (r + 1 < rows) { // // اگر همسایهٔ پایین وجود دارد
//////                    Intersection a = grid[r][c]; // // تقاطع بالا
//////                    Intersection b = grid[r + 1][c]; // // تقاطع پایین
//////                    String rid = "R_V_" + (roadCounter++); // // id یکتا برای جاده عمودی
//////                    Road road = new Road(rid, a, b, twoWay); // // ساخت جاده عمودی
//////                    // لِین‌های رفت و برگشت
//////                    List<Lane> forward = new ArrayList<Lane>(); // // لیست لِین‌های رو به پایین
//////                    List<Lane> backward = new ArrayList<Lane>(); // // لیست لِین‌های رو به بالا
//////                    for (int i = 0; i < lanesPerDirection; i++) { // // ساخت n لِین در هر جهت
//////                        String lidF = rid + "_F_" + i; // // id لِین رفت
//////                        Lane lf = new Lane(lidF, Direction.SOUTH, road); // // لِین به سمت جنوب
//////                        forward.add(lf); // // افزودن لِین رفت
//////                        if (twoWay) { // // اگر دوطرفه
//////                            String lidB = rid + "_B_" + i; // // id لِین برگشت
//////                            Lane lb = new Lane(lidB, Direction.NORTH, road); // // لِین به سمت شمال
//////                            backward.add(lb); // // افزودن لِین برگشت
//////                        }
//////                    }
//////                    road.setForwardLanes(forward); // // ست لِین‌های رو به پایین
//////                    road.setBackwardLanes(backward); // // ست لِین‌های رو به بالا
//////                    this.roads.add(road); // // افزودن جاده
//////                }
//////            }
//////        }
//////    }
//////
//////    // ============== جای‌نگه‌دار مسیر ==============
//////    public Route findRoute(Lane start, Lane end) { // // جای‌نگه‌دار مسیریابی
//////        // در مرحلهٔ بعد (PathFinder) تکمیل می‌کنیم. فعلاً null برمی‌گردانیم.
//////        return null; // // هنوز پیاده‌سازی نشده
//////    }
//////}
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
//////package infrastructure; // // پکیج زیرساخت
//////
//////import simulation.PathFinder; // // برای صدا زدن دیکسترا
//////import core.Route; // // نوع Route
//////import java.util.ArrayList; // // لیست
//////import java.util.List; // // اینترفیس
//////
//////public class CityMap { // // نقشه شهر (راه‌ها و تقاطع‌ها)
//////    private final List<Intersection> intersections; // // لیست تقاطع‌ها
//////    private final List<Road> roads; // // لیست راه‌ها
//////
//////    public CityMap() { // // سازنده
//////        this.intersections = new ArrayList<Intersection>(); // // لیست خالی تقاطع‌ها
//////        this.roads = new ArrayList<Road>(); // // لیست خالی راه‌ها
//////    }
//////
//////    public void addIntersection(Intersection i) { // // افزودن تقاطع
//////        intersections.add(i); // // اضافه به لیست
//////    }
//////
//////    public void addRoad(Road r) { // // افزودن راه
//////        roads.add(r); // // اضافه به لیست
//////    }
//////
//////    public List<Intersection> getIntersections() { // // گتر لیست تقاطع‌ها
//////        return intersections; // // خروجی
//////    }
//////
//////    public List<Road> getRoads() { // // گتر لیست راه‌ها
//////        return roads; // // خروجی
//////    }
//////
//////    // همه راه‌های متصل به یک تقاطع //
//////    public List<Road> getAdjacentRoads(Intersection i) { // // همسایه‌های راه برای تقاطع i
//////        List<Road> res = new ArrayList<Road>(); // // خروجی
//////        for (int k = 0; k < roads.size(); k++) { // // پیمایش راه‌ها
//////            Road r = roads.get(k); // // یک راه
//////            if (r.getStartIntersection() == i || r.getEndIntersection() == i) { // // اگر به i وصل است
//////                res.add(r); // // افزودن
//////            }
//////        }
//////        return res; // // خروجی
//////    }
//////
//////    // گرفتن راه بین دو تقاطع (اگر وجود داشته باشد) //
//////    public Road getRoadBetween(Intersection a, Intersection b) { // // راه بین a و b
//////        for (int k = 0; k < roads.size(); k++) { // // پیمایش
//////            Road r = roads.get(k); // // یک راه
//////            if ((r.getStartIntersection() == a && r.getEndIntersection() == b) ||
//////                    (r.getStartIntersection() == b && r.getEndIntersection() == a)) { // // دو جهت
//////                return r; // // یافت شد
//////            }
//////        }
//////        return null; // // نبود
//////    }
//////
//////    // متد راحت برای پیدا کردن مسیر بین دو لِین //
//////    public Route findRoute(Lane start, Lane end) { // // ساخت Route بین start و end
//////        PathFinder pf = new PathFinder(this); // // ساخت یابنده مسیر
//////        return pf.findBestRoute(start, end); // // اجرای دیکسترا و برگرداندن Route
//////    }
//////}