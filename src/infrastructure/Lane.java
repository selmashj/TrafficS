
package infrastructure; // // پکیج زیرساخت

import core.Direction;         // // جهت حرکت لِین
import core.Identifiable;      // // اینترفیس شناسه یکتا
import core.Point;             // // نقطهٔ صحیح (X,Y) پروژه
import core.Vehicle;           // // برای ثبت خودروها و پیدا کردن لیدر
import ui.UIConstants;         // // ثوابت UI مثل عرض لِین

import java.awt.geom.Point2D;  // // نقطهٔ اعشاری برای محاسبات هندسی
import java.util.ArrayList;    // // لیست پویا
import java.util.Collections;  // // ابزار لیست‌های امن
import java.util.List;         // // اینترفیس لیست

public class Lane implements Identifiable { // // نمایندهٔ یک لِین یک‌طرفه روی یک جاده
    private final String id;            // // شناسه یکتا
    private final Direction direction;  // // جهت حرکت در این لِین
    private final Road parentRoad;      // // جادهٔ والد که این لِین روی آن قرار دارد

    private Lane leftAdjacentLane;      // // لِین همسایهٔ چپ (اگر وجود داشته باشد)
    private Lane rightAdjacentLane;     // // لِین همسایهٔ راست (اگر وجود داشته باشد)
    private int offsetIndex = 0;        // // اندیس این لِین داخل گروه همان جهت (۰،۱،۲…)

    // --- افزوده: نگهداری خودروهای داخل این لِین (thread-safe) ---
    private final List<Vehicle> occupants = Collections.synchronizedList(new ArrayList<Vehicle>()); // // خودروهای حاضر در لِین

    public Lane(String id, Direction direction, Road parentRoad) { // // سازندهٔ لِین
        this.id = id;                  // // مقداردهی شناسه
        this.direction = direction;    // // مقداردهی جهت
        this.parentRoad = parentRoad;  // // مقداردهی جادهٔ والد
    }

    @Override
    public String getId() {            // // پیاده‌سازی گتر شناسه
        return id;                     // // برگرداندن شناسه
    }

    public Direction getDirection() {  // // گتر جهت
        return direction;              // // برگرداندن جهت
    }

    public Road getParentRoad() {      // // گتر جادهٔ والد
        return parentRoad;             // // برگرداندن جاده
    }

    public Lane getLeftAdjacentLane() {    // // گتر لِین همسایهٔ چپ (برای OvertakingRules)
        return leftAdjacentLane;           // // بازگرداندن رفرنس چپ
    }

    public Lane getRightAdjacentLane() {   // // گتر لِین همسایهٔ راست (برای OvertakingRules)
        return rightAdjacentLane;          // // بازگرداندن رفرنس راست
    }

    public void setLeftAdjacentLane(Lane l) {  // // ستر همسایهٔ چپ
        this.leftAdjacentLane = l;             // // تنظیم مقدار
    }

    public void setRightAdjacentLane(Lane l) { // // ستر همسایهٔ راست
        this.rightAdjacentLane = l;            // // تنظیم مقدار
    }

    public void setOffsetIndex(int index) {    // // تنظیم اندیس افست داخل گروه همان جهت
        this.offsetIndex = index;              // // ذخیرهٔ اندیس
    }

    // ===================== مدیریت خودروها در لِین =====================

    public void registerVehicle(Vehicle v) {    // // افزودن خودرو به لِین
        if (v == null) return;                  // // محافظت از null
        if (v.getCurrentLane() != this) return; // // فقط اگر واقعاً در همین لِین است
        if (!occupants.contains(v)) {           // // جلوگیری از تکرار
            occupants.add(v);                   // // اضافه به لیست
        }
    }

    public void unregisterVehicle(Vehicle v) {  // // حذف خودرو از لِین
        occupants.remove(v);                    // // حذف اگر وجود داشته باشد
    }

    public List<Vehicle> getVehiclesView() {    // // نمای امن (کپی غیرقابل‌تغییر) از خودروهای لِین
        synchronized (occupants) {              // // قفل برای سازگاری چندنخی
            return Collections.unmodifiableList(new ArrayList<Vehicle>(occupants)); // // کپی امن
        }
    }

    /**
     * پیدا کردن نزدیک‌ترین خودروی جلو (Leader) در همین لِین برای «self».
     * اگر چیزی جلو نباشد، null برمی‌گردد.
     */
    public Vehicle findLeaderAhead(Vehicle self) {           // // جستجوی لیدر جلو
        if (self == null) return null;                       // // محافظت
        final double sSelf = self.getPositionInLane();       // // مکان طولی خودروی فعلی
        final boolean forward = (direction == Direction.EAST // // تشخیص جهت مثبت
                || direction == Direction.SOUTH);            // // برای EAST/SOUTH s بزرگ‌تر یعنی جلو

        Vehicle best = null;                                 // // بهترین کاندید
        double bestDelta = Double.POSITIVE_INFINITY;         // // فاصله طولی مینیمم

        synchronized (occupants) {                           // // قفل روی لیست
            for (int i = 0; i < occupants.size(); i++) {     // // پیمایش خودروها
                Vehicle v = occupants.get(i);                // // یک خودرو
                if (v == self) continue;                     // // خودِ ماشین را رد کن
                double s = v.getPositionInLane();            // // موقعیت آن خودرو

                if (forward) {                               // // اگر جهت رو به جلو (A→B)
                    if (s <= sSelf) continue;                // // فقط ماشین‌هایی که جلوترند
                    double gap = s - sSelf;                  // // فاصله طولی
                    if (gap < bestDelta) {                   // // اگر نزدیک‌تر است
                        bestDelta = gap;                     // // به‌روز رسانی فاصله
                        best = v;                            // // انتخاب لیدر
                    }
                } else {                                     // // جهت معکوس (B→A)
                    if (s >= sSelf) continue;                // // فقط ماشین‌هایی که جلوتر در معکوس‌اند (s کوچک‌تر)
                    double gap = sSelf - s;                  // // فاصله طولی
                    if (gap < bestDelta) {                   // // اگر نزدیک‌تر است
                        bestDelta = gap;                     // // به‌روزرسانی
                        best = v;                            // // انتخاب
                    }
                }
            }
        }
        return best;                                         // // لیدر (یا null)
    }

    // ===================== هندسهٔ مسیر/طول/زاویه (بدون تغییر) =====================

    public double getLength() {            // // طول تقریبی مسیر مرکزی لِین (براساس مسیر جاده)
        final int SAMPLES = 24;            // // تعداد نمونه برای تقریب
        double length = 0.0;               // // متغیر جمع طول
        Point2D prev = parentRoad.curvePoint(0.0); // // نقطهٔ آغاز مسیر (t=0)
        for (int i = 1; i <= SAMPLES; i++) {       // // پیمایش نمونه‌ها
            double t = (double) i / (double) SAMPLES; // // پارامتر t فعلی
            Point2D p = parentRoad.curvePoint(t);     // // نقطهٔ متناظر روی مسیر
            length += prev.distance(p);               // // افزودن فاصله قطعه
            prev = p;                                 // // بروزرسانی نقطهٔ قبلی
        }
        return length;                 // // بازگرداندن طول تقریبی
    }

    private double sToT(double s) {    // // نگاشت فاصلهٔ واقعی s روی مسیر به پارامتر t ∈ [0..1]
        final int S = 60;              // // دقت نمونه‌گیری برای جدول تجمعی
        double[] acc = new double[S + 1]; // // آرایهٔ طول تجمعی
        acc[0] = 0.0;                  // // مقدار اولیه
        Point2D prev = parentRoad.curvePoint(0.0); // // نقطهٔ آغاز
        for (int i = 1; i <= S; i++) {            // // محاسبهٔ تجمعی
            double t = (double) i / (double) S;   // // t فعلی
            Point2D p = parentRoad.curvePoint(t); // // نقطهٔ روی مسیر
            acc[i] = acc[i - 1] + prev.distance(p); // // طول تجمعی تا این نمونه
            prev = p;                              // // بروزرسانی prev
        }
        double total = acc[S];         // // طول کل مسیر
        if (s <= 0) return 0.0;        // // کلیپ پایین
        if (s >= total) return 1.0;    // // کلیپ بالا
        int lo = 0, hi = S;            // // جستجوی دودویی بین نمونه‌ها
        while (hi - lo > 1) {          // // حلقهٔ جستجو
            int mid = (lo + hi) >>> 1; // // اندیس میانی
            if (acc[mid] < s) lo = mid; else hi = mid; // // انتخاب بازه
        }
        double seg = acc[hi] - acc[lo];                 // // طول سگمنت یافت‌شده
        double ratio = (seg <= 1e-6) ? 0.0 : (s - acc[lo]) / seg; // // نسبت درون سگمنت
        return ((double) lo + ratio) / (double) S;      // // t متناظر با s
    }

    public Point getPositionAt(double positionInLane) { // // مرکز خودرو روی این لِین در فاصلهٔ داده‌شده
        double L = getLength();                         // // طول لِین
        if (L < 1e-6) {                                 // // محافظت از تقسیم بر صفر
            Point A = parentRoad.getStartIntersection().getPosition(); // // گرفتن نقطهٔ شروع
            return new Point(A.getX(), A.getY());       // // بازگرداندن همان نقطه
        }

        int sideSign = (direction == Direction.EAST || direction == Direction.SOUTH) ? (+1) : (-1); // // علامت سمت
        double s = positionInLane;                      // // فاصلهٔ روی مسیر
        if (sideSign < 0) s = L - s;                    // // معکوس کردن برای جهت مخالف

        double t = sToT(s);                             // // نگاشت s به t
        Point2D c = parentRoad.curvePoint(t);           // // نقطهٔ مرکزی مسیر
        Point2D tan = parentRoad.curveTangent(t);       // // بردار مماس مسیر
        double lenT = Math.hypot(tan.getX(), tan.getY()); // // طول بردار مماس
        if (lenT < 1e-6) lenT = 1.0;                    // // جلوگیری از صفر
        double nx = -tan.getY() / lenT;                 // // مؤلفهٔ X نرمال چپ مسیر
        double ny =  tan.getX() / lenT;                 // // مؤلفهٔ Y نرمال چپ مسیر

        int lanesF = parentRoad.getForwardLanes().size(); // // تعداد لِین‌های جهت رفت
        int lanesB = parentRoad.getBackwardLanes().size(); // // تعداد لِین‌های جهت برگشت
        int perSide = Math.max(lanesF, lanesB);           // // بیشینهٔ لِین برای هر سمت
        double groupW = perSide * UIConstants.LANE_WIDTH
                + Math.max(0, perSide - 1) * UIConstants.LANE_GAP; // // پهنای گروه یک سمت
        double sideCenter = UIConstants.LANE_GAP * 0.5 + groupW * 0.5; // // فاصلهٔ مرکز گروه از مرکز راه
        double perLaneOffset = (UIConstants.LANE_WIDTH * 0.5) + (UIConstants.LANE_GAP * 0.5); // // افست بین لِین‌ها

        double lateral = sideSign * sideCenter + (-offsetIndex * perLaneOffset * sideSign); // // افست جانبی نهایی

        int x = (int) Math.round(c.getX() + nx * lateral); // // مختصات X مرکز خودرو
        int y = (int) Math.round(c.getY() + ny * lateral); // // مختصات Y مرکز خودرو
        return new Point(x, y);                             // // بازگرداندن نقطهٔ نهایی
    }

    public double getAngleRadians() {                     // // زاویهٔ حرکت خودرو روی این لِین (رادیان)
        Point2D tan = parentRoad.curveTangent(0.5);       // // مماس در میانهٔ مسیر
        double a = Math.atan2(tan.getY(), tan.getX());    // // زاویهٔ A→B
        if (direction == Direction.WEST || direction == Direction.NORTH) a += Math.PI; // // تصحیح برای جهت معکوس
        return a;                                         // // بازگرداندن زاویه
    }
}




























//package infrastructure; // // پکیج زیرساخت
//
//import core.Direction;         // // جهت حرکت لِین
//import core.Identifiable;      // // اینترفیس شناسه یکتا
//import core.Point;             // // نقطهٔ صحیح (X,Y) پروژه
//import ui.UIConstants;         // // ثوابت UI مثل عرض لِین
//import java.awt.geom.Point2D;  // // نقطهٔ اعشاری برای محاسبات هندسی
//
//public class Lane implements Identifiable { // // نمایندهٔ یک لِین یک‌طرفه روی یک جاده
//    private final String id;            // // شناسه یکتا
//    private final Direction direction;  // // جهت حرکت در این لِین
//    private final Road parentRoad;      // // جادهٔ والد که این لِین روی آن قرار دارد
//
//    private Lane leftAdjacentLane;      // // لِین همسایهٔ چپ (اگر وجود داشته باشد)
//    private Lane rightAdjacentLane;     // // لِین همسایهٔ راست (اگر وجود داشته باشد)
//    private int offsetIndex = 0;        // // اندیس این لِین داخل گروه همان جهت (۰،۱،۲…)
//
//    public Lane(String id, Direction direction, Road parentRoad) { // // سازندهٔ لِین
//        this.id = id;                  // // مقداردهی شناسه
//        this.direction = direction;    // // مقداردهی جهت
//        this.parentRoad = parentRoad;  // // مقداردهی جادهٔ والد
//    }
//
//    @Override
//    public String getId() {            // // پیاده‌سازی گتر شناسه
//        return id;                     // // برگرداندن شناسه
//    }
//
//    public Direction getDirection() {  // // گتر جهت
//        return direction;              // // برگرداندن جهت
//    }
//
//    public Road getParentRoad() {      // // گتر جادهٔ والد
//        return parentRoad;             // // برگرداندن جاده
//    }
//
//    public Lane getLeftAdjacentLane() {    // // گتر لِین همسایهٔ چپ (برای OvertakingRules)
//        return leftAdjacentLane;           // // بازگرداندن رفرنس چپ
//    }
//
//    public Lane getRightAdjacentLane() {   // // گتر لِین همسایهٔ راست (برای OvertakingRules)
//        return rightAdjacentLane;          // // بازگرداندن رفرنس راست
//    }
//
//    public void setLeftAdjacentLane(Lane l) {  // // ستر همسایهٔ چپ
//        this.leftAdjacentLane = l;             // // تنظیم مقدار
//    }
//
//    public void setRightAdjacentLane(Lane l) { // // ستر همسایهٔ راست
//        this.rightAdjacentLane = l;            // // تنظیم مقدار
//    }
//
//    public void setOffsetIndex(int index) {    // // تنظیم اندیس افست داخل گروه همان جهت
//        this.offsetIndex = index;              // // ذخیرهٔ اندیس
//    }
//
//    public double getLength() {            // // طول تقریبی مسیر مرکزی لِین (براساس مسیر جاده)
//        final int SAMPLES = 24;            // // تعداد نمونه برای تقریب
//        double length = 0.0;               // // متغیر جمع طول
//        Point2D prev = parentRoad.curvePoint(0.0); // // نقطهٔ آغاز مسیر (t=0)
//        for (int i = 1; i <= SAMPLES; i++) {       // // پیمایش نمونه‌ها
//            double t = (double) i / (double) SAMPLES; // // پارامتر t فعلی
//            Point2D p = parentRoad.curvePoint(t);     // // نقطهٔ متناظر روی مسیر
//            length += prev.distance(p);               // // افزودن فاصله قطعه
//            prev = p;                                 // // بروزرسانی نقطهٔ قبلی
//        }
//        return length;                 // // بازگرداندن طول تقریبی
//    }
//
//    private double sToT(double s) {    // // نگاشت فاصلهٔ واقعی s روی مسیر به پارامتر t ∈ [0..1]
//        final int S = 60;              // // دقت نمونه‌گیری برای جدول تجمعی
//        double[] acc = new double[S + 1]; // // آرایهٔ طول تجمعی
//        acc[0] = 0.0;                  // // مقدار اولیه
//        Point2D prev = parentRoad.curvePoint(0.0); // // نقطهٔ آغاز
//        for (int i = 1; i <= S; i++) {            // // محاسبهٔ تجمعی
//            double t = (double) i / (double) S;   // // t فعلی
//            Point2D p = parentRoad.curvePoint(t); // // نقطهٔ روی مسیر
//            acc[i] = acc[i - 1] + prev.distance(p); // // طول تجمعی تا این نمونه
//            prev = p;                              // // بروزرسانی prev
//        }
//        double total = acc[S];         // // طول کل مسیر
//        if (s <= 0) return 0.0;        // // کلیپ پایین
//        if (s >= total) return 1.0;    // // کلیپ بالا
//        int lo = 0, hi = S;            // // جستجوی دودویی بین نمونه‌ها
//        while (hi - lo > 1) {          // // حلقهٔ جستجو
//            int mid = (lo + hi) >>> 1; // // اندیس میانی
//            if (acc[mid] < s) lo = mid; else hi = mid; // // انتخاب بازه
//        }
//        double seg = acc[hi] - acc[lo];                 // // طول سگمنت یافت‌شده
//        double ratio = (seg <= 1e-6) ? 0.0 : (s - acc[lo]) / seg; // // نسبت درون سگمنت
//        return ((double) lo + ratio) / (double) S;      // // t متناظر با s
//    }
//
//    public Point getPositionAt(double positionInLane) { // // مرکز خودرو روی این لِین در فاصلهٔ داده‌شده
//        double L = getLength();                         // // طول لِین
//        if (L < 1e-6) {                                 // // محافظت از تقسیم بر صفر
//            Point A = parentRoad.getStartIntersection().getPosition(); // // گرفتن نقطهٔ شروع
//            return new Point(A.getX(), A.getY());       // // بازگرداندن همان نقطه
//        }
//
//        int sideSign = (direction == Direction.EAST || direction == Direction.SOUTH) ? (+1) : (-1); // // علامت سمت
//        double s = positionInLane;                      // // فاصلهٔ روی مسیر
//        if (sideSign < 0) s = L - s;                    // // معکوس کردن برای جهت مخالف
//
//        double t = sToT(s);                             // // نگاشت s به t
//        Point2D c = parentRoad.curvePoint(t);           // // نقطهٔ مرکزی مسیر
//        Point2D tan = parentRoad.curveTangent(t);       // // بردار مماس مسیر
//        double lenT = Math.hypot(tan.getX(), tan.getY()); // // طول بردار مماس
//        if (lenT < 1e-6) lenT = 1.0;                    // // جلوگیری از صفر
//        double nx = -tan.getY() / lenT;                 // // مؤلفهٔ X نرمال چپ مسیر
//        double ny =  tan.getX() / lenT;                 // // مؤلفهٔ Y نرمال چپ مسیر
//
//        int lanesF = parentRoad.getForwardLanes().size(); // // تعداد لِین‌های جهت رفت
//        int lanesB = parentRoad.getBackwardLanes().size(); // // تعداد لِین‌های جهت برگشت
//        int perSide = Math.max(lanesF, lanesB);           // // بیشینهٔ لِین برای هر سمت
//        double groupW = perSide * UIConstants.LANE_WIDTH
//                + Math.max(0, perSide - 1) * UIConstants.LANE_GAP; // // پهنای گروه یک سمت
//        double sideCenter = UIConstants.LANE_GAP * 0.5 + groupW * 0.5; // // فاصلهٔ مرکز گروه از مرکز راه
//        double perLaneOffset = (UIConstants.LANE_WIDTH * 0.5) + (UIConstants.LANE_GAP * 0.5); // // افست بین لِین‌ها
//
//        double lateral = sideSign * sideCenter + (-offsetIndex * perLaneOffset * sideSign); // // افست جانبی نهایی
//
//        int x = (int) Math.round(c.getX() + nx * lateral); // // مختصات X مرکز خودرو
//        int y = (int) Math.round(c.getY() + ny * lateral); // // مختصات Y مرکز خودرو
//        return new Point(x, y);                             // // بازگرداندن نقطهٔ نهایی
//    }
//
//    public double getAngleRadians() {                     // // زاویهٔ حرکت خودرو روی این لِین (رادیان)
//        Point2D tan = parentRoad.curveTangent(0.5);       // // مماس در میانهٔ مسیر
//        double a = Math.atan2(tan.getY(), tan.getX());    // // زاویهٔ A→B
//        if (direction == Direction.WEST || direction == Direction.NORTH) a += Math.PI; // // تصحیح برای جهت معکوس
//        return a;                                         // // بازگرداندن زاویه
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
////package infrastructure; // // پکیج زیرساخت
////
////import core.Direction;         // // جهت
////import core.Identifiable;      // // اینترفیس شناسه
////import core.Point;             // // نقطه
////import ui.UIConstants;         // // ثوابت UI
////import java.awt.geom.Point2D;  // // نقطهٔ اعشاری
////
////public class Lane implements Identifiable { // // لِین یک‌طرفه
////    private final String id;           // // شناسه
////    private final Direction direction; // // جهت حرکت
////    private final Road parentRoad;     // // جادهٔ والد
////
////    private Lane leftAdjacentLane;     // // همسایهٔ چپ
////    private Lane rightAdjacentLane;    // // همسایهٔ راست
////    private int offsetIndex = 0;       // // جایگاه در گروه همان جهت (۰،۱،۲…)
////
////    public Lane(String id, Direction direction, Road parentRoad){ // // سازنده
////        this.id=id; this.direction=direction; this.parentRoad=parentRoad;        // // ست‌ها
////    }
////
////    @Override public String getId(){ return id; }                                // // گتر ID
////    public Direction getDirection(){ return direction; }                         // // گتر جهت
////    public Road getParentRoad(){ return parentRoad; }                            // // گتر جاده
////    public void setLeftAdjacentLane(Lane l){ this.leftAdjacentLane=l; }          // // ست چپ
////    public void setRightAdjacentLane(Lane l){ this.rightAdjacentLane=l; }        // // ست راست
////    public void setOffsetIndex(int index){ this.offsetIndex=index; }             // // ست ایندکس
////
////    // --- طول تقریبی مسیر لِین از روی مسیر مرکزی جاده ---
////    public double getLength(){                                                   // // طول
////        final int S = 24;                                                        // // نمونه
////        double L=0;                                                              // // جمع
////        Point2D prev = parentRoad.curvePoint(0.0);                               // // t=0
////        for(int i=1;i<=S;i++){                                                   // // حلقه
////            Point2D p = parentRoad.curvePoint((double)i/S);                      // // نقطه
////            L += prev.distance(p);                                               // // افزایش
////            prev = p;                                                            // // آپدیت
////        }
////        return L;                                                                // // خروجی
////    }
////
////    // --- نگاشت فاصله s به پارامتر t (برای حرکت یکنواخت روی منحنی) ---
////    private double sToT(double s){                                               // // نگاشت
////        final int S=60;                                                          // // نمونه زیاد
////        double[] acc = new double[S+1];                                          // // طول تجمعی
////        acc[0]=0;                                                                // // شروع
////        Point2D prev = parentRoad.curvePoint(0.0);                               // // t=0
////        for(int i=1;i<=S;i++){                                                   // // حلقه
////            Point2D p = parentRoad.curvePoint((double)i/S);                      // // نقطه
////            acc[i] = acc[i-1] + prev.distance(p);                                // // تجمعی
////            prev = p;                                                            // // آپدیت
////        }
////        double total = acc[S];                                                   // // کل
////        if (s<=0) return 0.0; if (s>=total) return 1.0;                          // // کلیپ
////        int lo=0,hi=S;                                                           // // باینری سرچ
////        while(hi-lo>1){ int mid=(lo+hi)>>>1; if (acc[mid]<s) lo=mid; else hi=mid; } // // جستجو
////        double seg=acc[hi]-acc[lo]; double ratio=(seg<=1e-6)?0.0:(s-acc[lo])/seg; // // نسبت
////        return ((double)lo+ratio)/(double)S;                                     // // t
////    }
////
////    // --- مرکز خودرو روی لِین با رعایت جداسازی لِین‌ها ---
////    public Point getPositionAt(double positionInLane){                            // // نقطه روی لِین
////        double L = getLength();                                                   // // طول لِین
////        if (L < 1e-6){ Point A = parentRoad.getStartIntersection().getPosition(); return new Point(A.getX(),A.getY()); } // // محافظت
////        int sideSign = (direction==Direction.EAST || direction==Direction.SOUTH) ? (+1) : (-1); // // علامت سمت
////        double s = positionInLane; if (sideSign<0) s = L - s;                     // // فاصله روی مسیر
////        double t = sToT(s);                                                       // // t متناظر
////        Point2D c = parentRoad.curvePoint(t);                                     // // نقطهٔ مرکزی
////        Point2D tan = parentRoad.curveTangent(t);                                 // // بردار مماس
////        double lenT = Math.hypot(tan.getX(), tan.getY()); if (lenT<1e-6) lenT=1;  // // طول مماس
////        double nx = -tan.getY()/lenT, ny = tan.getX()/lenT;                       // // نرمال چپ مسیر
////
////        int lanesF = parentRoad.getForwardLanes().size();                         // // تعداد رفت
////        int lanesB = parentRoad.getBackwardLanes().size();                        // // تعداد برگشت
////        int perSide = Math.max(lanesF, lanesB);                                   // // بیشینه هر سمت
////        double groupW = perSide*UIConstants.LANE_WIDTH + Math.max(0,perSide-1)*UIConstants.LANE_GAP; // // پهنای گروه
////        double sideCenter = UIConstants.LANE_GAP*0.5 + groupW*0.5;                // // فاصله مرکز گروه
////        double perLaneOffset = (UIConstants.LANE_WIDTH*0.5) + (UIConstants.LANE_GAP*0.5); // // آفست درون گروه
////        double lateral = sideSign*sideCenter + (-offsetIndex*perLaneOffset*sideSign);      // // آفست کل
////
////        int x=(int)Math.round(c.getX()+nx*lateral);                                // // X
////        int y=(int)Math.round(c.getY()+ny*lateral);                                // // Y
////        return new Point(x,y);                                                     // // خروجی
////    }
////
////    public double getAngleRadians(){                                              // // زاویهٔ کلی لِین
////        Point2D tan = parentRoad.curveTangent(0.5);                               // // مماس وسط مسیر
////        double a = Math.atan2(tan.getY(), tan.getX());                            // // زاویه A→B
////        if (direction==Direction.WEST || direction==Direction.NORTH) a += Math.PI;// // ۱۸۰ درجه برای جهت معکوس
////        return a;                                                                  // // خروجی
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
//////package infrastructure; // // پکیج زیرساخت
//////
//////import core.Direction; // // جهت
//////import core.Identifiable; // // اینترفیس شناسه
//////import core.Point; // // نقطه
//////import ui.UIConstants; // // ثوابت UI
//////import java.awt.geom.Point2D; // // نقطه اعشاری
//////
//////public class Lane implements Identifiable { // // لاین یک‌طرفه
//////    private final String id; // // شناسه
//////    private final Direction direction; // // جهت
//////    private final Road parentRoad; // // جادهٔ والد
//////
//////    private Lane leftAdjacentLane; // // همسایه چپ
//////    private Lane rightAdjacentLane; // // همسایه راست
//////    private int offsetIndex = 0; // // ایندکس در گروه همان‌جهت
//////
//////    public Lane(String id, Direction direction, Road parentRoad) { // // سازنده
//////        this.id = id; // // ست
//////        this.direction = direction; // // ست
//////        this.parentRoad = parentRoad; // // ست
//////    }
//////
//////    @Override public String getId(){ return id; } // // گتر ID
//////    public Direction getDirection(){ return direction; } // // گتر جهت
//////    public Road getParentRoad(){ return parentRoad; } // // گتر جاده
//////    public Lane getLeftAdjacentLane(){ return leftAdjacentLane; } // // گتر چپ
//////    public Lane getRightAdjacentLane(){ return rightAdjacentLane; } // // گتر راست
//////    public void setLeftAdjacentLane(Lane l){ this.leftAdjacentLane = l; } // // ست چپ
//////    public void setRightAdjacentLane(Lane l){ this.rightAdjacentLane = l; } // // ست راست
//////    public void setOffsetIndex(int index){ this.offsetIndex = index; } // // ست ایندکس
//////
//////    public double getLength(){ // // طول تقریبی مسیر مرکزی
//////        final int SAMPLES = 24; // // تعداد نمونه
//////        double length = 0.0; // // مجموع
//////        Point2D prev = parentRoad.curvePoint(0.0); // // نقطه اول
//////        for(int i=1;i<=SAMPLES;i++){ // // حلقه
//////            double t = (double)i / (double)SAMPLES; // // t
//////            Point2D p = parentRoad.curvePoint(t); // // نقطه
//////            length += prev.distance(p); // // جمع طول
//////            prev = p; // // به‌روزرسانی
//////        }
//////        return length; // // خروجی
//////    }
//////
//////    private double sToT(double s){ // // نگاشت فاصله به پارامتر t
//////        final int SAMPLES = 60; // // نمونه زیاد
//////        double[] cum = new double[SAMPLES+1]; // // جدول تجمعی
//////        cum[0] = 0; // // اول صفر
//////        Point2D prev = parentRoad.curvePoint(0.0); // // نقطه 0
//////        for(int i=1;i<=SAMPLES;i++){ // // حلقه
//////            double t = (double)i / (double)SAMPLES; // // t
//////            Point2D p = parentRoad.curvePoint(t); // // نقطه
//////            cum[i] = cum[i-1] + prev.distance(p); // // تجمعی
//////            prev = p; // // آپدیت
//////        }
//////        double total = cum[SAMPLES]; // // کل طول
//////        if (s <= 0) return 0.0; // // کلیپ
//////        if (s >= total) return 1.0; // // کلیپ
//////        int lo = 0, hi = SAMPLES; // // مرزها
//////        while(hi - lo > 1){ // // باینری سرچ
//////            int mid = (lo + hi) >>> 1; // // وسط
//////            if (cum[mid] < s) lo = mid; else hi = mid; // // انتخاب نیمه
//////        }
//////        double seg = cum[hi] - cum[lo]; // // طول سگمنت
//////        double ratio = (seg<=1e-6)?0.0: (s - cum[lo]) / seg; // // نسبت
//////        return ((double)lo + ratio) / (double)SAMPLES; // // t
//////    }
//////
//////    public Point getPositionAt(double positionInLane){ // // مرکز خودرو روی لاین
//////        double L = getLength(); // // طول
//////        if (L < 1e-6) { // // محافظت
//////            Point P = parentRoad.getStartIntersection().getPosition(); // // نقطه A
//////            return new Point(P.getX(), P.getY()); // // بازگشت
//////        }
//////        int sideSign = (direction==Direction.EAST || direction==Direction.SOUTH) ? (+1) : (-1); // // علامت سمت
//////        double s = positionInLane; // // فاصله روی مسیر
//////        if (sideSign < 0) s = L - s; // // معکوس برای جهت مخالف
//////        double t = sToT(s); // // نگاشت s→t
//////        Point2D center = parentRoad.curvePoint(t); // // نقطه مرکزی
//////        Point2D tan = parentRoad.curveTangent(t); // // بردار مماس
//////        double lenT = Math.hypot(tan.getX(), tan.getY()); // // طول مماس
//////        if (lenT < 1e-6) lenT = 1; // // جلوگیری از صفر
//////        double nx = -tan.getY() / lenT; // // نرمال x
//////        double ny =  tan.getX() / lenT; // // نرمال y
//////
//////        int lanesF = parentRoad.getForwardLanes().size(); // // تعداد رفت
//////        int lanesB = parentRoad.getBackwardLanes().size(); // // تعداد برگشت
//////        int perSide = Math.max(lanesF, lanesB); // // بیشینه هر سمت
//////        double groupWidth = perSide * UIConstants.LANE_WIDTH
//////                + Math.max(0, perSide - 1) * UIConstants.LANE_GAP; // // پهنای گروه
//////        double sideCenter = UIConstants.LANE_GAP * 0.5 + groupWidth * 0.5; // // فاصله مرکز گروه
//////        double perLaneOffset = (UIConstants.LANE_WIDTH * 0.5) + (UIConstants.LANE_GAP * 0.5); // // آفست درون گروه
//////
//////        double lateral = sideSign * sideCenter + (-offsetIndex * perLaneOffset * sideSign); // // آفست کل
//////        int x = (int)Math.round(center.getX() + nx * lateral); // // X
//////        int y = (int)Math.round(center.getY() + ny * lateral); // // Y
//////        return new Point(x, y); // // خروجی
//////    }
//////
//////    public double getAngleRadians(){ // // زاویهٔ کلی لاین
//////        Point2D tan = parentRoad.curveTangent(0.5); // // مماس میان مسیر
//////        double angle = Math.atan2(tan.getY(), tan.getX()); // // زاویه A→B
//////        if (direction==Direction.WEST || direction==Direction.NORTH) angle += Math.PI; // // ۱۸۰ درجه برای معکوس
//////        return angle; // // خروجی
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
//////
//////
//////
//////
//////
//////
////////package infrastructure; // // پکیج زیرساخت
////////
////////import core.Direction;         // // جهت
////////import core.Identifiable;      // // برای ID
////////import core.Point;             // // مختصات صحیح پروژه
////////import ui.UIConstants;         // // ثوابت UI
////////
////////import java.awt.geom.Point2D;  // // برای محاسبات اعشاری
////////
////////public class Lane implements Identifiable { // // مسیر یک‌طرفه
////////    private final String id;               // // شناسه
////////    private final Direction direction;     // // جهت حرکت لِین
////////    private final Road parentRoad;         // // جادهٔ والد
////////
////////    private Lane leftAdjacentLane;         // // همسایهٔ چپ
////////    private Lane rightAdjacentLane;        // // همسایهٔ راست
////////    private int offsetIndex = 0;           // // ایندکس درون گروه همان جهت
////////
////////    public Lane(String id, Direction direction, Road parentRoad) { // // سازنده
////////        this.id = id; this.direction = direction; this.parentRoad = parentRoad; // // ست‌ها
////////    }
////////
////////    @Override public String getId(){ return id; }       // // گتر ID
////////    public Direction getDirection(){ return direction; } // // گتر جهت
////////    public Road getParentRoad(){ return parentRoad; }    // // گتر جاده
////////    public Lane getLeftAdjacentLane(){ return leftAdjacentLane; }   // // چپ
////////    public Lane getRightAdjacentLane(){ return rightAdjacentLane; } // // راست
////////    public void setLeftAdjacentLane(Lane l){ this.leftAdjacentLane = l; } // // ست چپ
////////    public void setRightAdjacentLane(Lane l){ this.rightAdjacentLane = l; } // // ست راست
////////    public void setOffsetIndex(int index){ this.offsetIndex = index; } // // ست ایندکس
////////
////////    // ====== طول مسیر مرکزی (تقریبی برای منحنی) ======
////////    public double getLength(){ // // طول تقریبی (نمونه‌برداری)
////////        final int SAMPLES = 24;                                   // // تعداد نمونه برای تقریب
////////        double length = 0.0;                                      // // جمع طول
////////        Point2D prev = parentRoad.curvePoint(0.0);                // // نقطهٔ t=0
////////        for(int i=1;i<=SAMPLES;i++){                              // // حلقهٔ نمونه‌ها
////////            double t = (double)i / (double)SAMPLES;               // // t
////////            Point2D p = parentRoad.curvePoint(t);                 // // نقطه
////////            double dx = p.getX() - prev.getX();                   // // Δx
////////            double dy = p.getY() - prev.getY();                   // // Δy
////////            length += Math.hypot(dx, dy);                         // // افزایش طول
////////            prev = p;                                             // // به‌روزرسانی قبلی
////////        }
////////        return length;                                            // // خروجی
////////    }
////////
////////    // نگاشت از «فاصله s روی لِین» به «t روی منحنی» //
////////    private double sToT(double s){ // // تقریب معکوس با جدول تجمعی
////////        final int SAMPLES = 60;                                 // // نمونهٔ زیادتر برای دقت
////////        double[] cum = new double[SAMPLES+1];                   // // آرایهٔ طول تجمعی
////////        cum[0] = 0;                                            // // شروع صفر
////////        Point2D prev = parentRoad.curvePoint(0.0);              // // t=0
////////        for(int i=1;i<=SAMPLES;i++){                            // // پرکردن جدول
////////            double t = (double)i / (double)SAMPLES;             // // t
////////            Point2D p = parentRoad.curvePoint(t);               // // نقطه
////////            cum[i] = cum[i-1] + prev.distance(p);               // // طول تجمعی
////////            prev = p;                                           // // قبلی
////////        }
////////        double total = cum[SAMPLES];                            // // کل طول
////////        if (s <= 0) return 0.0; if (s >= total) return 1.0;     // // کلیپ
////////        // باینری سرچ ساده //
////////        int lo = 0, hi = SAMPLES;                               // // بازه
////////        while(hi - lo > 1){                                     // // تا رسیدن به خانه نزدیک
////////            int mid = (lo + hi) >>> 1;                          // // وسط
////////            if (cum[mid] < s) lo = mid; else hi = mid;          // // انتخاب نیمه
////////        }
////////        // درون‌یابی خطی بین lo..hi //
////////        double seg = cum[hi] - cum[lo];                         // // طول سگمنت
////////        double ratio = (seg<=1e-6)?0.0: (s - cum[lo]) / seg;    // // نسبت
////////        return ((double)lo + ratio) / (double)SAMPLES;          // // t تقریبی
////////    }
////////
////////    public Point getPositionAt(double positionInLane){ // // مرکز خودرو روی لِین
////////        double L = getLength();                                  // // طول مسیر
////////        if (L < 1e-6) { Point P = parentRoad.getStartIntersection().getPosition(); return new Point(P.getX(), P.getY()); } // // محافظت
////////
////////        // جهت واقعی حرکت: EAST/SOUTH = +1 ، WEST/NORTH = -1 //
////////        int sideSign = (direction==Direction.EAST || direction==Direction.SOUTH) ? (+1) : (-1); // // علامت
////////
////////        // s فیزیکی روی مسیر مرکزی //
////////        double s = positionInLane;                                // // فاصلهٔ موردنظر
////////        if (sideSign < 0) s = L - s;                              // // معکوس برای جهت برعکس
////////
////////        // نگاشت s→t //
////////        double t = sToT(s);                                       // // t متناظر
////////
////////        // نقطه و مماس مسیر مرکزی در t //
////////        Point2D center = parentRoad.curvePoint(t);                // // نقطهٔ مرکزی
////////        Point2D tan    = parentRoad.curveTangent(t);              // // بردار مماس
////////
////////        // نرمالِ چپِ مسیر مرکزی (nx,ny) //
////////        double lenT = Math.hypot(tan.getX(), tan.getY());         // // طول مماس
////////        if (lenT < 1e-6) lenT = 1;                                // // جلوگیری از صفر
////////        double nx = -tan.getY() / lenT;                           // // نرمال x
////////        double ny =  tan.getX() / lenT;                           // // نرمال y
////////
////////        // محاسبهٔ آفست جانبی برای گروه جهت‌ها و جایگاه لِین در گروه //
////////        int lanesF = parentRoad.getForwardLanes().size();         // // تعداد رفت
////////        int lanesB = parentRoad.getBackwardLanes().size();        // // تعداد برگشت
////////        int perSide = Math.max(lanesF, lanesB);                   // // بیشینهٔ هر سمت
////////        double groupWidth = perSide * UIConstants.LANE_WIDTH
////////                + Math.max(0, perSide - 1) * UIConstants.LANE_GAP; // // پهنای گروه
////////        double sideCenter = UIConstants.LANE_GAP * 0.5 + groupWidth * 0.5; // // فاصلهٔ مرکز گروه از مرکز راه
////////        double perLaneOffset = (UIConstants.LANE_WIDTH * 0.5) + (UIConstants.LANE_GAP * 0.5); // // آفست درون‌گروه
////////
////////        double lateral = sideSign * sideCenter + (-offsetIndex * perLaneOffset * sideSign); // // آفست کل جانبی
////////
////////        int x = (int)Math.round(center.getX() + nx * lateral);   // // X نهایی
////////        int y = (int)Math.round(center.getY() + ny * lateral);   // // Y نهایی
////////        return new Point(x, y);                                   // // خروجی
////////    }
////////
////////    public double getAngleRadians(){ // // زاویهٔ رندر در موضع فعلی لِین
////////        // از مشتق مسیر مرکزی استفاده می‌کنیم؛ جهت مخالف ۱۸۰ درجه می‌چرخد.
////////        Point2D tan = parentRoad.curveTangent(0.5);               // // مماس تقریبی (میان مسیر)
////////        double angle = Math.atan2(tan.getY(), tan.getX());        // // زاویهٔ A→B
////////        if (direction==Direction.WEST || direction==Direction.NORTH) angle += Math.PI; // // چرخش برای جهت معکوس
////////        return angle;                                             // // خروجی
////////    }
////////}
//////////
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
//////////9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
//////////package infrastructure; // // پکیج زیرساخت
//////////
//////////import core.Direction;   // // جهت حرکت لِین
//////////import core.Identifiable; // // برای شناسه
//////////import core.Point;       // // مختصات جهان
//////////import ui.UIConstants;   // // ثوابت UI
//////////
//////////public class Lane implements Identifiable { // // مسیر یک‌طرفه روی یک Road
//////////    private final String id;           // // شناسه یکتا
//////////    private final Direction direction; // // جهت لِین
//////////    private final Road parentRoad;     // // جادهٔ والد
//////////
//////////    private Lane leftAdjacentLane;     // // همسایهٔ چپ
//////////    private Lane rightAdjacentLane;    // // همسایهٔ راست
//////////
//////////    private int offsetIndex = 0;       // // ایندکس درون «گروهِ همان جهت» (-1 چپ، +1 راست)
//////////
//////////    public Lane(String id, Direction direction, Road parentRoad) { // // سازنده
//////////        this.id = id;                 // // ست ID
//////////        this.direction = direction;   // // ست جهت
//////////        this.parentRoad = parentRoad; // // ست جاده
//////////    }
//////////
//////////    @Override
//////////    public String getId() { // // گتر ID
//////////        return id;
//////////    }
//////////
//////////    public Direction getDirection() { // // گتر جهت
//////////        return direction;
//////////    }
//////////
//////////    public Road getParentRoad() { // // گتر جادهٔ والد
//////////        return parentRoad;
//////////    }
//////////
//////////    public Lane getLeftAdjacentLane() { return leftAdjacentLane; }   // // همسایهٔ چپ
//////////    public Lane getRightAdjacentLane(){ return rightAdjacentLane; }  // // همسایهٔ راست
//////////    public void setLeftAdjacentLane(Lane l) { this.leftAdjacentLane = l; }   // // ست چپ
//////////    public void setRightAdjacentLane(Lane l){ this.rightAdjacentLane = l; }  // // ست راست
//////////    public void setOffsetIndex(int index){ this.offsetIndex = index; }       // // ست ایندکس
//////////
//////////    // --------------------- هندسه: طول/مکان/زاویه ---------------------
//////////
//////////    public double getLength() { // // طول لِین
//////////        Point A = parentRoad.getStartIntersection().getPosition(); // // نقطهٔ شروع
//////////        Point B = parentRoad.getEndIntersection().getPosition();   // // نقطهٔ پایان
//////////        double dx = B.getX() - A.getX(); // // Δx
//////////        double dy = B.getY() - A.getY(); // // Δy
//////////        return Math.sqrt(dx*dx + dy*dy); // // طول
//////////    }
//////////
//////////    public Point getPositionAt(double positionInLane) {
//////////        // // مختصات «مرکز خودرو» روی این لِین با احتساب جداسازی جهت‌ها و چندلاینه
//////////
//////////        Point A = parentRoad.getStartIntersection().getPosition(); // // A
//////////        Point B = parentRoad.getEndIntersection().getPosition();   // // B
//////////        double dx = B.getX() - A.getX(); // // Δx
//////////        double dy = B.getY() - A.getY(); // // Δy
//////////        double len = Math.sqrt(dx*dx + dy*dy); // // طول
//////////
//////////        if (len < 1e-6) return new Point(A.getX(), A.getY()); // // محافظت از تقسیم بر صفر
//////////
//////////        // واحد جهت مسیرِ Road (A→B) //
//////////        double ux = dx / len; // // بردار واحد طولی x
//////////        double uy = dy / len; // // بردار واحد طولی y
//////////
//////////        // بردار عمودِ «چپِ A→B» (برای جابه‌جایی جانبی) //
//////////        double nx = -uy; // // نرمال x
//////////        double ny =  ux; // // نرمال y
//////////
//////////        // نسبت پیشروی روی راه (0..1) //
//////////        double t = positionInLane / len; // // t
//////////        // برای پایداری، کلیپ //
//////////        if (t < 0) t = 0; if (t > 1) t = 1; // // کلیپ
//////////
//////////        // نقطهٔ مرکزی روی خطِ مرکزی راه //
//////////        double cx = A.getX() + ux * (t * len); // // X مرکزی
//////////        double cy = A.getY() + uy * (t * len); // // Y مرکزی
//////////
//////////        // --- محاسبهٔ عرض بصری راه بر اساس تعداد لِین‌ها ---
//////////        int lanesF = parentRoad.getForwardLanes().size(); // // تعداد لِین‌های جهت رفت
//////////        int lanesB = parentRoad.getBackwardLanes().size();// // تعداد لِین‌های جهت برگشت
//////////        int perSide = Math.max(lanesF, lanesB);           // // حداکثر لِین در هر سمت
//////////
//////////        // پهنای گروه یک سمت (فقط همان جهت) //
//////////        double groupWidth = perSide * UIConstants.LANE_WIDTH
//////////                + Math.max(0, perSide - 1) * UIConstants.LANE_GAP; // // پهنا گروه
//////////
//////////        // فاصلهٔ مرکز هر گروه از خط وسط راه (نیمِ شکاف وسط + نیمِ پهنای گروه) //
//////////        double sideCenter = UIConstants.LANE_GAP * 0.5 + groupWidth * 0.5; // // مرکز گروه
//////////
//////////        // جابه‌جایی درون گروه برای این لِین (نصف پهنای یک لِین + نصف فاصلهٔ بین‌لاین) //
//////////        double perLaneOffset = (UIConstants.LANE_WIDTH * 0.5) + (UIConstants.LANE_GAP * 0.5); // // آفست درون‌گروه
//////////
//////////        // علامت سمت: +1 برای جهت «رو به جلو» (E/S) ، -1 برای جهت «برگشتی» (W/N) //
//////////        int sideSign = (direction == Direction.EAST || direction == Direction.SOUTH) ? (+1) : (-1); // // علامت سمت
//////////
//////////        // آفست کل جانبی: مرکزِ گروه ± آفست درون‌گروه با توجه به چپ/راستِ همان جهت //
//////////        double lateral = sideSign * sideCenter                             // // انتقال گروه از مرکز راه
//////////                + (-offsetIndex * perLaneOffset * sideSign);        // // جابه‌جایی درون گروه (چپ/راست)
//////////
//////////        // مختصات نهایی با انتقال روی نرمال //
//////////        int x = (int)Math.round(cx + nx * lateral); // // X نهایی
//////////        int y = (int)Math.round(cy + ny * lateral); // // Y نهایی
//////////        return new Point(x, y); // // خروجی
//////////    }
//////////
//////////    public double getAngleRadians() { // // زاویهٔ A→B برای رندر
//////////        Point A = parentRoad.getStartIntersection().getPosition(); // // A
//////////        Point B = parentRoad.getEndIntersection().getPosition();   // // B
//////////        double dx = B.getX() - A.getX(); // // Δx
//////////        double dy = B.getY() - A.getY(); // // Δy
//////////        return Math.atan2(dy, dx); // // زاویه
//////////    }
//////////}
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
////////////777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777
////////////
////////////package infrastructure; // // پکیج زیرساخت
////////////
////////////import core.Direction;   // // جهت حرکت لاین (N/E/S/W)
////////////import core.Identifiable; // // برای داشتن شناسه یکتا
////////////import core.Point;       // // نوع نقطه (مختصات جهان) — توجه: این core.Point است، نه java.awt.Point
////////////
////////////public class Lane implements Identifiable { // // مسیر یک‌طرفه درون یک Road
////////////    private final String id;           // // شناسه یکتا برای لاین
////////////    private final Direction direction; // // جهت حرکت این لاین
////////////    private final Road parentRoad;     // // جادهٔ والد که این لاین روی آن قرار دارد
////////////
////////////    private Lane leftAdjacentLane;     // // لاین کناری در سمت چپ (اگر وجود داشته باشد)
////////////    private Lane rightAdjacentLane;    // // لاین کناری در سمت راست (اگر وجود داشته باشد)
////////////
////////////    // --- آفست جانبی برای جدا کردن مسیر هندسی هر لاین از خط مرکزی راه ---
////////////    // مقدار -1 یعنی کمی به چپ مسیر اصلی، +1 یعنی کمی به راست مسیر اصلی (نسبت به جهت همین لاین)
////////////    private int offsetIndex = 0;       // // پیش‌فرض 0 (روی خط مرکزی راه)
////////////
////////////    public Lane(String id, Direction direction, Road parentRoad) { // // سازندهٔ لاین
////////////        this.id = id;                 // // ذخیره شناسه
////////////        this.direction = direction;   // // ذخیره جهت
////////////        this.parentRoad = parentRoad; // // ذخیره جاده والد
////////////        this.leftAdjacentLane = null; // // در ابتدا همسایه ندارد
////////////        this.rightAdjacentLane = null;// // در ابتدا همسایه ندارد
////////////    }
////////////
////////////    @Override
////////////    public String getId() { // // گتر شناسه
////////////        return id; // // برگرداندن ID
////////////    }
////////////
////////////    public Direction getDirection() { // // گتر جهت
////////////        return direction; // // برگرداندن جهت
////////////    }
////////////
////////////    public Road getParentRoad() { // // گتر جادهٔ والد
////////////        return parentRoad; // // برگرداندن Road
////////////    }
////////////
////////////    // --------------------- همسایه‌های کناری (برای تغییر لاین/سبقت) ---------------------
////////////
////////////    public Lane getLeftAdjacentLane() { // // لاین سمت چپ
////////////        return leftAdjacentLane; // // ممکن است null باشد
////////////    }
////////////
////////////    public Lane getRightAdjacentLane() { // // لاین سمت راست
////////////        return rightAdjacentLane; // // ممکن است null باشد
////////////    }
////////////
////////////    public void setLeftAdjacentLane(Lane lane) { // // ست کردن همسایهٔ چپ
////////////        this.leftAdjacentLane = lane; // // ذخیرهٔ ارجاع
////////////    }
////////////
////////////    public void setRightAdjacentLane(Lane lane) { // // ست کردن همسایهٔ راست
////////////        this.rightAdjacentLane = lane; // // ذخیرهٔ ارجاع
////////////    }
////////////
////////////    // --------------------- آفست جانبی (برای عریض کردن بصری خیابان) ---------------------
////////////
////////////    public void setOffsetIndex(int index) { // // تعیین ایندکس آفست جانبی این لاین
////////////        this.offsetIndex = index; // // -1 = چپ، +1 = راست (نسبت به جهت همین لاین)
////////////    }
////////////
////////////    // --------------------- هندسهٔ لاین: طول، موقعیت، زاویه ---------------------
////////////
////////////    public double getLength() { // // طول هندسی لاین (فاصلهٔ دو تقاطع Road)
////////////        Point A = parentRoad.getStartIntersection().getPosition(); // // مختصات تقاطع شروع
////////////        Point B = parentRoad.getEndIntersection().getPosition();   // // مختصات تقاطع پایان
////////////        double dx = B.getX() - A.getX(); // // اختلاف X
////////////        double dy = B.getY() - A.getY(); // // اختلاف Y
////////////        return Math.sqrt(dx * dx + dy * dy); // // طول با قضیهٔ فیثاغورس
////////////    }
////////////
////////////    public Point getPositionAt(double positionInLane) {
////////////        // // خروجی: «مختصات جهان» نقطه‌ای روی این لاین، با احتساب «آفست جانبی»
////////////        // // ورودی positionInLane فاصلهٔ طی‌شده از ابتدای لاین است (0..طول لاین)
////////////
////////////        Point A = parentRoad.getStartIntersection().getPosition(); // // نقطهٔ شروع راه
////////////        Point B = parentRoad.getEndIntersection().getPosition();   // // نقطهٔ پایان راه
////////////
////////////        double dx = B.getX() - A.getX(); // // مؤلفهٔ X بردار راه
////////////        double dy = B.getY() - A.getY(); // // مؤلفهٔ Y بردار راه
////////////        double len = Math.sqrt(dx * dx + dy * dy); // // طول راه
////////////
////////////        if (len <= 0.0001) { // // اگر طول تقریباً صفر بود (ایراد داده)
////////////            return new Point(A.getX(), A.getY()); // // همان نقطهٔ شروع را برگردان
////////////        }
////////////
////////////        // t = نسبت پیشروی روی خط مرکزی راه (0..1) //
////////////        double t = positionInLane / len; // // تبدیل فاصله به نسبت
////////////        if (t < 0) t = 0; // // کلیپ پایین
////////////        if (t > 1) t = 1; // // کلیپ بالا
////////////
////////////        // مختصات نقطهٔ روی خط مرکزی (بدون آفست جانبی) //
////////////        double cx = A.getX() + dx * t; // // X مرکزی
////////////        double cy = A.getY() + dy * t; // // Y مرکزی
////////////
////////////        // بردار عمود واحد روی مسیر: برای (dx,dy)، عمود = (-dy, +dx) / len //
////////////        double nx = -dy / len; // // X عمود واحد
////////////        double ny =  dx / len; // // Y عمود واحد
////////////
////////////        // فاصلهٔ جانبی هر لاین نسبت به خط مرکزی:
////////////        // perLaneOffset = نصف پهنای یک لاین + نصف فاصلهٔ میان لاین‌ها
////////////        double halfLane = ui.UIConstants.LANE_WIDTH * 0.5; // // نصف پهنای یک لاین (از UIConstants)
////////////        double halfGap  = ui.UIConstants.LANE_GAP  * 0.5;  // // نصف فاصلهٔ بین لاین‌ها
////////////        double perLaneOffset = halfLane + halfGap; // // مجموع آفست جانبی برای هر لاین از مرکز
////////////
////////////        // آفست نهایی این لاین (بر حسب ایندکس سمت چپ/راست)
////////////        double lateral = offsetIndex * perLaneOffset; // // -1=چپ، +1=راست
////////////
////////////        // انتقال نقطهٔ مرکزی به سمت عمود (چپ/راست) //
////////////        int x = (int) Math.round(cx + nx * lateral); // // X نهایی
////////////        int y = (int) Math.round(cy + ny * lateral); // // Y نهایی
////////////
////////////        return new Point(x, y); // // مختصات جهانِ نهاییِ این لاین
////////////    }
////////////
////////////    public double getAngleRadians() { // // زاویهٔ حرکت نسبت به محور X (برای rotate تصویر خودرو)
////////////        Point A = parentRoad.getStartIntersection().getPosition(); // // شروع
////////////        Point B = parentRoad.getEndIntersection().getPosition();   // // پایان
////////////        double dx = B.getX() - A.getX(); // // ΔX
////////////        double dy = B.getY() - A.getY(); // // ΔY
////////////        return Math.atan2(dy, dx); // // زاویه بر حسب رادیان
////////////    }
////////////}
