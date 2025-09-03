package simulation; // // پکیج simulation

import infrastructure.CityMap; // // نقشه شهر
import infrastructure.Intersection; // // تقاطع‌ها
import infrastructure.Road; // // جاده‌ها
import infrastructure.Lane; // // لِین‌ها
import core.Route; // // مسیر
import core.Direction; // // جهت‌ها (برای انتخاب لِین)
import java.util.*; // // ساختارهای داده استاندارد (Map, List, PriorityQueue و ...)

public class PathFinder { // // یابنده مسیر با دیکسترا روی تقاطع‌ها
    private final CityMap map; // // مرجع به نقشه

    public PathFinder(CityMap map) { // // سازنده
        this.map = map; // // ذخیره نقشه
    }

    public Route findBestRoute(Lane start, Lane end) { // // ورودی: لِین شروع و پایان
        if (start == null || end == null) return new Route(start, end, Collections.<Lane>emptyList()); // // ورودی نامعتبر

        // گره‌ها = تقاطع‌ها. مبدا دیکسترا: تقاطعِ ابتدای جادهٔ start با توجه به جهت آن //
        Intersection startNode = getNodeForLaneAsSource(start); // // نود مبدا
        Intersection goalNode  = getNodeForLaneAsTarget(end);   // // نود مقصد

        if (startNode == null || goalNode == null) { // // بررسی ایمنی
            return new Route(start, end, Collections.<Lane>emptyList()); // // مسیر تهی
        }

        // ---------- اجرای دیکسترا روی گراف تقاطع‌ها ----------
        // ساخت نگاشت‌ها //
        Map<Intersection, Double> dist = new HashMap<Intersection, Double>(); // // فاصله از مبدا
        Map<Intersection, Intersection> prev = new HashMap<Intersection, Intersection>(); // // والد برای بازسازی مسیر

        // مقداردهی اولیه //
        List<Intersection> nodes = map.getIntersections(); // // همه تقاطع‌ها
        for (int i = 0; i < nodes.size(); i++) { // // پیمایش
            dist.put(nodes.get(i), Double.POSITIVE_INFINITY); // // فاصله بی‌نهایت
            prev.put(nodes.get(i), null); // // والد نامشخص
        }
        dist.put(startNode, 0.0); // // فاصله مبدا = ۰

        // صف اولویت‌دار بر اساس کمترین فاصله //
        PriorityQueue<Intersection> pq = new PriorityQueue<Intersection>(nodes.size(), new Comparator<Intersection>() { // // مقایسه‌گر سفارشی
            public int compare(Intersection a, Intersection b) { // // مقایسه دو گره
                double da = dist.get(a); // // فاصله a
                double db = dist.get(b); // // فاصله b
                if (da < db) return -1; // // کوچکتر
                if (da > db) return 1;  // // بزرگتر
                return 0; // // برابر
            }
        });
        pq.add(startNode); // // افزودن مبدا به صف

        // الگوریتم دیکسترا //
        while (!pq.isEmpty()) { // // تا زمانی که صف خالی نشده
            Intersection u = pq.poll(); // // کمترین فاصله
            if (u == goalNode) break; // // به مقصد رسیدیم

            List<Road> adjRoads = map.getAdjacentRoads(u); // // راه‌های متصل به u
            for (int i = 0; i < adjRoads.size(); i++) { // // پیمایش راه‌های مجاور
                Road r = adjRoads.get(i); // // یک راه
                Intersection v = r.getOtherEnd(u); // // تقاطع طرف مقابل
                if (v == null) continue; // // ایمنی
                double w = r.getLength(); // // وزن یال = طول راه (می‌تونی ترافیک/محدودیت هم لحاظ کنی)
                double alt = dist.get(u) + w; // // مسیر جایگزین
                if (alt < dist.get(v)) { // // اگر بهتر است
                    dist.put(v, alt); // // به‌روزرسانی فاصله
                    prev.put(v, u); // // ثبت والد
                    // چون PriorityQueue جاوا decrease-key ندارد، دوباره اضافه می‌کنیم //
                    pq.add(v); // // افزودن مجدد برای اولویت جدید
                }
            }
        }

        // ---------- بازسازی لیست تقاطع‌ها از goal به start ----------
        List<Intersection> pathNodes = new ArrayList<Intersection>(); // // لیست گره‌ها
        Intersection cur = goalNode; // // از مقصد شروع کن
        while (cur != null) { // // تا زمانی که والد داریم
            pathNodes.add(0, cur); // // درج در ابتدای لیست
            cur = prev.get(cur); // // حرکت به والد
        }
        if (pathNodes.isEmpty() || pathNodes.get(0) != startNode) { // // اگر مسیر معتبر نشد
            return new Route(start, end, Collections.<Lane>emptyList()); // // مسیر تهی
        }

        // ---------- تبدیل مسیر تقاطع‌ها به لیست لِین‌ها ----------
        List<Lane> lanes = new ArrayList<Lane>(); // // خروجی لِین‌ها
        // لِین اول باید همان start باشد //
        lanes.add(start); // // افزودن لِین شروع

        for (int i = 0; i < pathNodes.size() - 1; i++) { // // از هر تقاطع به بعدی
            Intersection a = pathNodes.get(i);     // // مبدا
            Intersection b = pathNodes.get(i + 1); // // مقصد
            Road r = map.getRoadBetween(a, b);     // // راه بین a و b
            if (r == null) continue; // // اگر نبود، رد شو

            // تعیین جهت حرکت روی این Road //
            boolean forward = (r.getStartIntersection() == a); // // اگر از start→end می‌رویم
            Lane chosen; // // لِین انتخابی
            if (forward) {
                // از بین لِین‌های رفت یک را انتخاب می‌کنیم (پیشنهاد: لِین چپ [index 0]) //
                chosen = r.getForwardLanes().isEmpty() ? null : r.getForwardLanes().get(0); // // لِین چپ رفت
            } else {
                chosen = r.getBackwardLanes().isEmpty() ? null : r.getBackwardLanes().get(0); // // لِین چپ برگشت
            }
            if (chosen != null) {
                // اگر همین Segment همان start نبود، اضافه کن //
                if (chosen != start) lanes.add(chosen); // // افزودن لِین این Segment
            }
        }

        // اگر مقصد در همان Road بود، آخرین لِین end را هم مطمئن شو هست //
        if (!lanes.isEmpty()) {
            Lane last = lanes.get(lanes.size() - 1); // // آخرین لِین
            if (end != null && last != end) { // // اگر متفاوت است
                lanes.add(end); // // افزودن انتهایی
            }
        }

        return new Route(start, end, lanes); // // ساخت Route نهایی
    }

    // انتخاب نود مبدا براساس جهت لِین شروع //
    private Intersection getNodeForLaneAsSource(Lane lane) { // // نود مبدا
        Road r = lane.getParentRoad(); // // جادهٔ لِین
        Direction d = lane.getDirection(); // // جهت لِین
        // اگر جهت لِین EAST یا SOUTH باشد یعنی از start به end می‌رویم //
        if (d == Direction.EAST || d == Direction.SOUTH) {
            return r.getStartIntersection(); // // مبدا = start
        } else {
            return r.getEndIntersection();   // // مبدا = end (چون WEST/NORTH یعنی از end به start)
        }
    }

    // انتخاب نود مقصد براساس جهت لِین مقصد //
    private Intersection getNodeForLaneAsTarget(Lane lane) { // // نود مقصد
        Road r = lane.getParentRoad(); // // جادهٔ لِین مقصد
        Direction d = lane.getDirection(); // // جهت لِین مقصد
        if (d == Direction.EAST || d == Direction.SOUTH) {
            return r.getEndIntersection(); // // مقصد = end
        } else {
            return r.getStartIntersection(); // // مقصد = start
        }
    }
}

