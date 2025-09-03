package infrastructure; // // پکیج زیرساخت

import core.Point;                 // // کلاس نقطه (X,Y)
import java.awt.geom.Path2D;       // // مسیر برای رسم خیابان
import java.awt.geom.Point2D;      // // نقطهٔ اعشاری برای محاسبات
import java.util.ArrayList;         // // لیست پویا
import java.util.List;              // // اینترفیس لیست

public class Road { // // جاده بین دو تقاطع (پشتیبانی خط مستقیم و Bezier درجه۲)
    // ---------- فیلدهای اصلی ----------
    private final String id;                // // شناسه یکتا
    private final Intersection start;       // // تقاطع شروع (A)
    private final Intersection end;         // // تقاطع پایان (B)
    private final List<Lane> forwardLanes;  // // لِین‌های جهت رفت
    private final List<Lane> backwardLanes; // // لِین‌های جهت برگشت

    // ---------- خمیدگی (اختیاری) ----------
    private boolean curved = false;         // // آیا مسیر خمیده است؟
    private Point control;                  // // نقطهٔ کنترل Bezier درجه۲

    // ---------- نکته سازگاری ----------
    private boolean twoWayHint = true;      // // فقط جهت سازگاری با سازندهٔ قدیمی

    // ===== سازنده‌های سازگار با نسخه‌های قبلی =====
    public Road(String id, Intersection start, Intersection end,
                List<Lane> forward, List<Lane> backward) { // // سازندهٔ کامل (داخلی)
        this.id = id;                   // // ست ID
        this.start = start;             // // ست شروع
        this.end = end;                 // // ست پایان
        this.forwardLanes = (forward != null) ? forward : new ArrayList<Lane>();   // // لیست رفت
        this.backwardLanes = (backward != null) ? backward : new ArrayList<Lane>(); // // لیست برگشت
    }

    public Road(String id, Intersection start, Intersection end) { // // سازندهٔ قدیمیِ ساده
        this(id, start, end, new ArrayList<Lane>(), new ArrayList<Lane>()); // // تفویض
    }

    public Road(String id, Intersection start, Intersection end, boolean twoWay) { // // سازندهٔ قدیمی با twoWay
        this(id, start, end, new ArrayList<Lane>(), new ArrayList<Lane>()); // // تفویض
        this.twoWayHint = twoWay; // // نگه‌داشتن فقط برای سازگاری
    }

    // ===== متدهای افزایشی (سازگاری با DemoMaps) =====
    public void addForwardLane(Lane lane) { // // افزودن لِین جهت رفت
        if (lane != null) this.forwardLanes.add(lane); // // اضافه به لیست
    }

    public void addBackwardLane(Lane lane) { // // افزودن لِین جهت برگشت
        if (lane != null) this.backwardLanes.add(lane); // // اضافه به لیست
    }

    // ===== گترهای عمومی =====
    public String getId() { return id; }                                  // // گتر ID
    public Intersection getStartIntersection() { return start; }          // // گتر شروع
    public Intersection getEndIntersection() { return end; }              // // گتر پایان
    public List<Lane> getForwardLanes() { return forwardLanes; }          // // گتر لِین‌های رفت
    public List<Lane> getBackwardLanes() { return backwardLanes; }        // // گتر لِین‌های برگشت

    public boolean isTwoWay() { // // آیا جاده دوطرفه است؟
        // // اگر هر دو لیست لِین خالی نباشند، دوطرفه تلقی می‌شود. در غیراین‌صورت از twoWayHint کمک می‌گیریم.
        if (!forwardLanes.isEmpty() && !backwardLanes.isEmpty()) return true;   // // هر دو سمت داریم
        if (!forwardLanes.isEmpty() && backwardLanes.isEmpty()) return twoWayHint; // // به‌طور پیش‌فرض
        if (forwardLanes.isEmpty() && !backwardLanes.isEmpty()) return twoWayHint; // // به‌طور پیش‌فرض
        return twoWayHint; // // اگر لِینی هنوز اضافه نشده باشد
    }

    // ===== API خم =====
    public void setQuadraticControl(Point c) { // // تعیین نقطهٔ کنترل Bezier
        this.control = c;              // // ذخیره
        this.curved = (c != null);     // // اگر null نباشد خمیده
    }

    public boolean isCurved() { return curved; } // // آیا خمیده؟
    public Point getControl() { return control; } // // گتر نقطهٔ کنترل

    // ===== کمکی: نقطه و مماس روی مسیر مرکزی (t∈[0,1]) =====
    public Point2D.Double curvePoint(double t) { // // نقطه روی مسیر (خط یا Bezier)
        Point A = start.getPosition(); // // نقطهٔ A
        Point B = end.getPosition();   // // نقطهٔ B
        if (!curved || control == null) { // // حالت خطی
            double x = A.getX() + (B.getX() - A.getX()) * t; // // X خطی
            double y = A.getY() + (B.getY() - A.getY()) * t; // // Y خطی
            return new Point2D.Double(x, y); // // خروجی
        }
        double one = 1.0 - t; // // (1-t)
        double x = one*one*A.getX() + 2*one*t*control.getX() + t*t*B.getX(); // // X بزیه
        double y = one*one*A.getY() + 2*one*t*control.getY() + t*t*B.getY(); // // Y بزیه
        return new Point2D.Double(x, y); // // خروجی
    }

    public Point2D.Double curveTangent(double t) { // // مشتق مسیر مرکزی
        Point A = start.getPosition(); // // A
        Point B = end.getPosition();   // // B
        if (!curved || control == null) { // // حالت خطی
            return new Point2D.Double(B.getX() - A.getX(), B.getY() - A.getY()); // // Δ بردار
        }
        double dx = 2*(1-t)*(control.getX() - A.getX()) + 2*t*(B.getX() - control.getX()); // // dX/dt
        double dy = 2*(1-t)*(control.getY() - A.getY()) + 2*t*(B.getY() - control.getY()); // // dY/dt
        return new Point2D.Double(dx, dy); // // خروجی
    }

    // ===== مسیر برای رندر =====
    public Path2D buildPath() { // // ساخت Path2D مسیر مرکزی
        Path2D path = new Path2D.Double();        // // مسیر خالی
        Point A = start.getPosition();            // // A
        Point B = end.getPosition();              // // B
        path.moveTo(A.getX(), A.getY());          // // شروع از A
        if (curved && control != null) {          // // اگر خمیده
            path.quadTo(control.getX(), control.getY(), B.getX(), B.getY()); // // بزیه Q
        } else {
            path.lineTo(B.getX(), B.getY());      // // خط مستقیم
        }
        return path;                               // // خروجی
    }

    // ===== متدهای کمکی که بقیه کلاس‌ها استفاده می‌کنند =====
    public Intersection getOtherEnd(Intersection oneSide) { // // برگرداندن سر دیگر جاده
        // // اگر ورودی برابر start باشد، end را بده؛ در غیر این صورت start را برگردان.
        if (oneSide == null) return null;                         // // محافظت
        if (oneSide == start) return end;                         // // دیگرسر
        if (oneSide == end) return start;                         // // دیگرسر
        // // اگر آبجکت متفاوت ولی هم‌مختصات بود هم چک می‌کنیم.
        Point p = oneSide.getPosition();                          // // مختصات ورودی
        if (p != null) {
            Point ps = start.getPosition(); Point pe = end.getPosition(); // // مختصات دو سر
            if (ps != null && p.getX()==ps.getX() && p.getY()==ps.getY()) return end; // // مقایسه
            if (pe != null && p.getX()==pe.getX() && p.getY()==pe.getY()) return start;// // مقایسه
        }
        return null;                                              // // اگر هیچ‌کدام نبود
    }

    public double getLength() { // // طول تقریبی مسیر مرکزی (برای PathFinder و غیره)
        final int SAMPLES = 32;                                   // // تعداد نمونه برای تقریب
        double length = 0.0;                                      // // جمع طول
        Point2D prev = curvePoint(0.0);                           // // نقطه‌ی شروع
        for (int i = 1; i <= SAMPLES; i++) {                      // // حلقه روی نمونه‌ها
            double t = (double) i / (double) SAMPLES;             // // t
            Point2D p = curvePoint(t);                            // // نقطه جدید
            length += prev.distance(p);                           // // جمع طول قطعه
            prev = p;                                             // // آپدیت قبلی
        }
        return length;                                            // // خروجی
    }
}





























//package infrastructure; // // پکیج زیرساخت
//
//import core.Point; // // کلاس نقطه
//import java.awt.geom.Path2D; // // مسیر برای رسم
//import java.awt.geom.Point2D; // // نقطهٔ اعشاری
//
//public class Road { // // جاده بین دو تقاطع
//    private final String id; // // شناسه
//    private final Intersection start; // // تقاطع شروع A
//    private final Intersection end; // // تقاطع پایان B
//    private final java.util.List<Lane> forwardLanes; // // لاین‌های رفت
//    private final java.util.List<Lane> backwardLanes; // // لاین‌های برگشت
//
//    private boolean curved = false; // // آیا خمیده است
//    private Point control; // // نقطه کنترل Bezier درجه۲
//
//    public Road(String id, Intersection start, Intersection end,
//                java.util.List<Lane> forward, java.util.List<Lane> backward) { // // سازنده
//        this.id = id; // // ست ID
//        this.start = start; // // ست شروع
//        this.end = end; // // ست پایان
//        this.forwardLanes = forward; // // ست لاین‌های رفت
//        this.backwardLanes = backward; // // ست لاین‌های برگشت
//    }
//
//    public String getId(){ return id; } // // گتر ID
//    public Intersection getStartIntersection(){ return start; } // // گتر شروع
//    public Intersection getEndIntersection(){ return end; } // // گتر پایان
//    public java.util.List<Lane> getForwardLanes(){ return forwardLanes; } // // گتر رفت
//    public java.util.List<Lane> getBackwardLanes(){ return backwardLanes; } // // گتر برگشت
//
//    public boolean isTwoWay(){ // // دوطرفه؟
//        return !forwardLanes.isEmpty() && !backwardLanes.isEmpty(); // // هر دو سمت داشته باشیم
//    }
//
//    public void setQuadraticControl(Point c){ // // تنظیم نقطه کنترل
//        this.control = c; // // ذخیره
//        this.curved = (c != null); // // تعیین خمیدگی
//    }
//
//    public boolean isCurved(){ return curved; } // // آیا خمیده
//    public Point getControl(){ return control; } // // گتر کنترل
//
//    public Point2D.Double curvePoint(double t){ // // نقطه روی مسیر مرکزی
//        Point A = start.getPosition(); // // A
//        Point B = end.getPosition(); // // B
//        if (!curved || control == null){ // // اگر خطی
//            double x = A.getX() + (B.getX() - A.getX()) * t; // // X خطی
//            double y = A.getY() + (B.getY() - A.getY()) * t; // // Y خطی
//            return new Point2D.Double(x, y); // // خروجی
//        }
//        double one = 1.0 - t; // // (1-t)
//        double x = one*one*A.getX() + 2*one*t*control.getX() + t*t*B.getX(); // // X بزیه
//        double y = one*one*A.getY() + 2*one*t*control.getY() + t*t*B.getY(); // // Y بزیه
//        return new Point2D.Double(x, y); // // خروجی
//    }
//
//    public Point2D.Double curveTangent(double t){ // // بردار مماس مسیر
//        Point A = start.getPosition(); // // A
//        Point B = end.getPosition(); // // B
//        if (!curved || control == null){ // // اگر خطی
//            return new Point2D.Double(B.getX() - A.getX(), B.getY() - A.getY()); // // Δ
//        }
//        double dx = 2*(1-t)*(control.getX()-A.getX()) + 2*t*(B.getX()-control.getX()); // // dX/dt
//        double dy = 2*(1-t)*(control.getY()-A.getY()) + 2*t*(B.getY()-control.getY()); // // dY/dt
//        return new Point2D.Double(dx, dy); // // خروجی
//    }
//
//    public Path2D buildPath(){ // // مسیر برای رسم
//        Path2D path = new Path2D.Double(); // // ایجاد مسیر
//        Point A = start.getPosition(); // // A
//        Point B = end.getPosition(); // // B
//        path.moveTo(A.getX(), A.getY()); // // حرکت به A
//        if (curved && control != null) path.quadTo(control.getX(), control.getY(), B.getX(), B.getY()); // // بزیه
//        else path.lineTo(B.getX(), B.getY()); // // خط مستقیم
//        return path; // // خروجی
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
////import core.Point;               // // نقطهٔ دوبعدی پروژه
////import java.awt.geom.Path2D;     // // مسیر برای رسم
////import java.awt.geom.Point2D;    // // نقطهٔ اعشاری برای محاسبات
////
//////public class Road { // // کلاس جاده بین دو تقاطع
////    private final String id;                   // // شناسه یکتا
////    private final Intersection start;          // // تقاطع A (ابتدا)
////    private final Intersection end;            // // تقاطع B (انتها)
////    private final java.util.List<Lane> forwardLanes;   // // لِین‌های جهت رفت
////    private final java.util.List<Lane> backwardLanes;  // // لِین‌های جهت برگشت
////
////    // ====== افزوده‌ها برای خم ======
////    private boolean curved = false;            // // آیا این جاده خمیده است؟
////    private Point control;                     // // نقطهٔ کنترل Bezier درجه‌۲ (اختیاری)
////
////    public Road(String id, Intersection start, Intersection end,
////                java.util.List<Lane> forward, java.util.List<Lane> backward) { // // سازنده
////        this.id = id;                              // // ست ID
////        this.start = start;                        // // ست A
////        this.end = end;                            // // ست B
////        this.forwardLanes = forward;               // // ست لِین‌های رفت
////        this.backwardLanes = backward;             // // ست لِین‌های برگشت
////    }
////
////    public String getId(){ return id; }            // // گتر ID
////    public Intersection getStartIntersection(){ return start; } // // گتر A
////    public Intersection getEndIntersection(){ return end; }     // // گتر B
////    public java.util.List<Lane> getForwardLanes(){ return forwardLanes; }   // // گتر رفت
////    public java.util.List<Lane> getBackwardLanes(){ return backwardLanes; } // // گتر برگشت
////
////    public boolean isTwoWay(){ // // آیا دوطرفه است؟
////        return !forwardLanes.isEmpty() && !backwardLanes.isEmpty(); // // اگر هر دو سمت لِین دارند
////    }
////
////    // ====== API خم ======
////    public void setQuadraticControl(Point c){ // // تعیین نقطهٔ کنترل برای خم
////        this.control = c;          // // ذخیره
////        this.curved = (c != null); // // اگر null نباشد خمیده است
////    }
////
////    public boolean isCurved(){ return curved; } // // آیا خمیده؟
////
////    public Point getControl(){ return control; } // // گتر کنترل
////
////    // نقطه روی مسیر مرکزی برای t∈[0..1] //
////    public Point2D.Double curvePoint(double t){ // // نقطهٔ Bezier درجه‌۲ یا خطی
////        Point A = start.getPosition(); // // A
////        Point B = end.getPosition();   // // B
////        if (!curved || control == null){ // // اگر خطی بود
////            double x = A.getX() + (B.getX() - A.getX()) * t; // // درون‌یابی خطی X
////            double y = A.getY() + (B.getY() - A.getY()) * t; // // درون‌یابی خطی Y
////            return new Point2D.Double(x, y); // // خروجی
////        }
////        double one = 1.0 - t;                          // // (1 - t)
////        double x = one*one*A.getX() + 2*one*t*control.getX() + t*t*B.getX(); // // فرمول Bezier X
////        double y = one*one*A.getY() + 2*one*t*control.getY() + t*t*B.getY(); // // فرمول Bezier Y
////        return new Point2D.Double(x, y);               // // خروجی
////    }
////
////    // بردار مماس مسیر برای t∈[0..1] //
////    public Point2D.Double curveTangent(double t){ // // مشتق Bezier یا بردار خطی
////        Point A = start.getPosition(); // // A
////        Point B = end.getPosition();   // // B
////        if (!curved || control == null){ // // اگر خطی بود
////            double x = B.getX() - A.getX(); // // ΔX
////            double y = B.getY() - A.getY(); // // ΔY
////            return new Point2D.Double(x, y); // // بازگشت
////        }
////        // مشتق Bezier درجه‌۲: 2(1-t)(C-A) + 2t(B-C) //
////        double dx = 2*(1-t)*(control.getX()-A.getX()) + 2*t*(B.getX()-control.getX()); // // dX/dt
////        double dy = 2*(1-t)*(control.getY()-A.getY()) + 2*t*(B.getY()-control.getY()); // // dY/dt
////        return new Point2D.Double(dx, dy); // // خروجی
////    }
////
////    // مسیر هندسی برای رسم //
////    public Path2D buildPath(){ // // ساخت Path2D مسیر مرکزی
////        Path2D path = new Path2D.Double();                                 // // مسیر خالی
////        Point A = start.getPosition();                                     // // A
////        Point B = end.getPosition();                                       // // B
////        path.moveTo(A.getX(), A.getY());                                   // // حرکت به A
////        if (curved && control != null) path.quadTo(control.getX(), control.getY(), B.getX(), B.getY()); // // Bezier Q
////        else path.lineTo(B.getX(), B.getY());                               // // خط مستقیم
////        return path;                                                        // // خروجی
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
//////99999999999999999999999999999999999999999999999999
//////
//////package infrastructure; // // پکیج زیرساخت
//////
//////import core.Identifiable; // // برای داشتن شناسه یکتا
//////import core.Direction;   // // برای تشخیص جهت لاین‌ها
//////import core.Point;       // // برای محاسبه طول راه
//////
//////import java.util.ArrayList; // // لیست پویا برای نگهداری لاین‌ها
//////import java.util.List;      // // اینترفیس لیست
//////
//////public class Road implements Identifiable { // // کلاس جاده که بین دو تقاطع قرار می‌گیرد
//////    private final String id; // // شناسه یکتا برای هر جاده
//////    private final Intersection startIntersection; // // تقاطع شروع جاده
//////    private final Intersection endIntersection;   // // تقاطع پایان جاده
//////    private final boolean isTwoWay;               // // آیا جاده دوطرفه است یا یک‌طرفه
//////
//////    private final List<Lane> forwardLanes;  // // لاین‌های جهت رفت (از start به end)
//////    private final List<Lane> backwardLanes; // // لاین‌های جهت برگشت (از end به start)
//////
//////    public Road(String id, Intersection start, Intersection end, boolean twoWay) { // // سازنده
//////        this.id = id;
//////        this.startIntersection = start;
//////        this.endIntersection = end;
//////        this.isTwoWay = twoWay;
//////        this.forwardLanes = new ArrayList<Lane>();
//////        this.backwardLanes = new ArrayList<Lane>();
//////    }
//////
//////    @Override
//////    public String getId() {
//////        return id;
//////    }
//////
//////    public Intersection getStartIntersection() {
//////        return startIntersection;
//////    }
//////
//////    public Intersection getEndIntersection() {
//////        return endIntersection;
//////    }
//////
//////    public boolean isTwoWay() {
//////        return isTwoWay;
//////    }
//////
//////    public List<Lane> getForwardLanes() {
//////        return forwardLanes;
//////    }
//////
//////    public List<Lane> getBackwardLanes() {
//////        return backwardLanes;
//////    }
//////
//////    public List<Lane> getAllLanes() {
//////        List<Lane> all = new ArrayList<Lane>();
//////        all.addAll(forwardLanes);
//////        all.addAll(backwardLanes);
//////        return all;
//////    }
//////
//////    // ===================== افزودن لاین‌ها + اتصال چپ/راست =====================
//////
//////    public void addForwardLane(Lane lane) {
//////        forwardLanes.add(lane);
//////        updateOffsets(forwardLanes); // // ست کردن offsetIndex درست
//////        updateAdjacency(forwardLanes); // // آپدیت همسایه‌ها
//////    }
//////
//////    public void addBackwardLane(Lane lane) {
//////        backwardLanes.add(lane);
//////        updateOffsets(backwardLanes); // // ست کردن offsetIndex درست
//////        updateAdjacency(backwardLanes); // // آپدیت همسایه‌ها
//////    }
//////
//////    // ست کردن offsetIndex برای کل لاین‌ها در یک جهت
//////    private void updateOffsets(List<Lane> lanes) {
//////        int n = lanes.size();
//////        int leftCount = n / 2; // // تعداد سمت چپ
//////        int rightCount = n - leftCount; // // تعداد سمت راست
//////        int idx = 0;
//////        for (int i = 0; i < n; i++) {
//////            Lane l = lanes.get(i);
//////            if (i < leftCount) {
//////                l.setOffsetIndex(-(leftCount - i)); // // ایندکس‌های منفی (چپ)
//////            } else {
//////                l.setOffsetIndex(i - leftCount + 1); // // ایندکس‌های مثبت (راست)
//////            }
//////        }
//////    }
//////
//////    // ست کردن همسایه‌های چپ/راست
//////    private void updateAdjacency(List<Lane> lanes) {
//////        for (int i = 0; i < lanes.size(); i++) {
//////            Lane l = lanes.get(i);
//////            Lane left = (i > 0) ? lanes.get(i - 1) : null;
//////            Lane right = (i < lanes.size() - 1) ? lanes.get(i + 1) : null;
//////            l.setLeftAdjacentLane(left);
//////            l.setRightAdjacentLane(right);
//////        }
//////    }
//////
//////    // ===================== توابع کمکی مفید =====================
//////
//////    public Lane pickAnyLane(Direction dir) {
//////        if (dir == Direction.EAST || dir == Direction.SOUTH) {
//////            return forwardLanes.isEmpty() ? null : forwardLanes.get(0);
//////        } else {
//////            return backwardLanes.isEmpty() ? null : backwardLanes.get(0);
//////        }
//////    }
//////
//////    public Intersection getOtherEnd(Intersection one) {
//////        if (one == startIntersection) return endIntersection;
//////        if (one == endIntersection)   return startIntersection;
//////        return null;
//////    }
//////
//////    public double getLength() {
//////        Point A = startIntersection.getPosition();
//////        Point B = endIntersection.getPosition();
//////        double dx = B.getX() - A.getX();
//////        double dy = B.getY() - A.getY();
//////        return Math.sqrt(dx * dx + dy * dy);
//////    }
//////
//////    @Override
//////    public String toString() {
//////        return "Road{" + id + ", " + startIntersection.getId() + "->" + endIntersection.getId()
//////                + ", twoWay=" + isTwoWay + ", fLanes=" + forwardLanes.size()
//////                + ", bLanes=" + backwardLanes.size() + "}";
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
//////
//////
//////
////////package infrastructure;
////////
////////import core.Identifiable;
////////
////////import java.util.ArrayList;
////////import java.util.List;
////////
////////public class Road implements Identifiable {
////////    private final String id;
////////    private final Intersection startIntersection;
////////    private final Intersection endIntersection;
////////
////////    private final List<Lane> forwardLanes = new ArrayList<>();
////////    private final List<Lane> backwardLanes = new ArrayList<>();
////////
////////    private boolean twoWay = true;
////////    private int localSpeedLimitKmh = 50; // به‌صورت پیش‌فرض
////////    private double geometricLength;      // بر حسب پیکسل/واحد نقشه
////////
////////    public Road(String id, Intersection start, Intersection end, boolean twoWay) {
////////        this.id = id;
////////        this.startIntersection = start;
////////        this.endIntersection = end;
////////        this.twoWay = twoWay;
////////        recomputeLength();
////////    }
////////
////////    private void recomputeLength() {
////////        int dx = endIntersection.getPosition().getX() - startIntersection.getPosition().getX();
////////        int dy = endIntersection.getPosition().getY() - startIntersection.getPosition().getY();
////////        this.geometricLength = Math.sqrt(dx * dx + dy * dy);
////////    }
////////
////////    @Override public String getId() { return id; }
////////
////////    // برای سازگاری با کد موجود
////////    public Intersection getStart() { return startIntersection; }
////////    public Intersection getEnd()   { return endIntersection; }
////////
////////    public Intersection getStartIntersection() { return startIntersection; }
////////    public Intersection getEndIntersection()   { return endIntersection; }
////////
////////    public List<Lane> getForwardLanes() { return forwardLanes; }
////////    public List<Lane> getBackwardLanes(){ return backwardLanes; }
////////
////////    public void addForwardLane(Lane lane) {
////////        if (lane != null && !forwardLanes.contains(lane)) {
////////            lane.setParentRoad(this);
////////            forwardLanes.add(lane);
////////        }
////////    }
////////
////////    public void addBackwardLane(Lane lane) {
////////        if (lane != null && !backwardLanes.contains(lane)) {
////////            lane.setParentRoad(this);
////////            backwardLanes.add(lane);
////////        }
////////    }
////////
////////    public boolean isTwoWay() { return twoWay; }
////////    public void setTwoWay(boolean twoWay) { this.twoWay = twoWay; }
////////
////////    public int getLocalSpeedLimit() { return localSpeedLimitKmh; }
////////    public void setLocalSpeedLimit(int kmh) { this.localSpeedLimitKmh = Math.max(10, kmh); }
////////
////////    /** طول هندسی جاده برای محاسبات/رندر */
////////    public double getGeometricLength() { return geometricLength; }
////////}
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
//////
////////
////////package infrastructure;
////////
////////import java.util.ArrayList;
////////import java.util.List;
////////import core.*;
/////////**
//////// * Road with compatibility helpers (getStart/getEnd/getGeometricLength/...).
//////// */
////////public class Road implements Identifiable {
////////    private final String id;
////////    private final Intersection startIntersection;
////////    private final Intersection endIntersection;
////////
////////    private final List<Lane> forwardLanes = new ArrayList<>();
////////    private final List<Lane> backwardLanes = new ArrayList<>();
////////
////////    private final boolean twoWay;
////////    private int localSpeedLimit = 60; // km/h پیش‌فرض
////////
////////    public Road(String id, Intersection start, Intersection end, boolean twoWay) {
////////        this.id = id;
////////        this.startIntersection = start;
////////        this.endIntersection = end;
////////        this.twoWay = twoWay;
////////    }
////////
////////    @Override public String getId() { return id; }
////////
////////    // API فعلی
////////    public Intersection getStartIntersection() { return startIntersection; }
////////    public Intersection getEndIntersection()   { return endIntersection; }
////////    public List<Lane> getForwardLanes()       { return forwardLanes; }
////////    public List<Lane> getBackwardLanes()      { return backwardLanes; }
////////    public boolean isTwoWay()                 { return twoWay; }
////////
////////    public void addForwardLane(Lane ln){ if(ln!=null){ forwardLanes.add(ln); ln.setParentRoad(this);} }
////////    public void addBackwardLane(Lane ln){ if(ln!=null){ backwardLanes.add(ln); ln.setParentRoad(this);} }
////////
////////    public void setLocalSpeedLimit(int kmh){ this.localSpeedLimit = Math.max(10, kmh); }
////////    public int  getLocalSpeedLimit(){ return localSpeedLimit; }
////////
////////    /** طول هندسی جاده بر حسب پیکسل/واحد نقشه. */
////////    public double getLengthMeters() { return distance(startIntersection.getPosition(), endIntersection.getPosition()); }
////////
////////    // ---------- لایه سازگاری (Alias ها) ----------
////////    /** alias: در بسیاری از فایل‌ها از این اسم استفاده شده. */
////////    public Intersection getStart(){ return getStartIntersection(); }
////////    public Intersection getEnd()  { return getEndIntersection(); }
////////    /** alias: بعضی کلاس‌ها این اسم را صدا می‌زنند. */
////////    public double getGeometricLength(){ return getLengthMeters(); }
////////    /** سازگاری با کدهایی که چک می‌کنند جاده نزدیک فلکه است. (فعلاً نافی؛ بعداً می‌شود واقعی کرد) */
////////    public boolean isNearRoundabout(){ return false; }
////////
////////    // ---------- داخلی ----------
////////    private static double distance(Point a, Point b){
////////        int dx = b.getX() - a.getX();
////////        int dy = b.getY() - a.getY();
////////        return Math.hypot(dx, dy);
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
//////////
//////////package infrastructure;
//////////
//////////import core.*;
//////////import pedestrian.PedestrianCrossing;
//////////
//////////import java.util.ArrayList;
//////////import java.util.Collections;
//////////import java.util.List;
//////////import java.util.Objects;
//////////
///////////** مسیر بین دو تقاطع با چند لِین رفت و برگشت */
//////////public class Road implements Identifiable {
//////////
//////////    private final String id;
//////////    private final Intersection startIntersection;
//////////    private final Intersection endIntersection;
//////////
//////////    private final List<Lane> forwardLanes = new ArrayList<>();
//////////    private final List<Lane> backwardLanes = new ArrayList<>();
//////////
//////////    // اختیاری: عابرها/سرعت/راونداباوت اطراف
//////////    private final List<PedestrianCrossing> crossings = new ArrayList<>();
//////////    private int localSpeedLimit = 50; // km/h
//////////    private boolean nearRoundabout = false;
//////////
//////////    public Road(String id, Intersection start, Intersection end, boolean twoWay, int fwdCount, int backCount) {
//////////        this.id = id;
//////////        this.startIntersection = start;
//////////        this.endIntersection = end;
//////////        // ساخت لِین‌ها
//////////        for (int i = 0; i < fwdCount; i++) {
//////////            Lane ln = new Lane(id + "_f" + i, Direction.EAST, this); // جهت واقعی را UI/Map تعیین می‌کند
//////////            ln.setOffsetIndex(i); // فاصله جانبی برای رندر
//////////            forwardLanes.add(ln);
//////////        }
//////////        if (twoWay) {
//////////            for (int i = 0; i < backCount; i++) {
//////////                Lane ln = new Lane(id + "_b" + i, Direction.WEST, this);
//////////                ln.setOffsetIndex(i);
//////////                backwardLanes.add(ln);
//////////            }
//////////        }
//////////    }
//////////
//////////    @Override public String getId() { return id; }
//////////
//////////    public Intersection getStartIntersection() { return startIntersection; }
//////////    public Intersection getEndIntersection()   { return endIntersection; }
//////////
//////////    // شیم‌های مورد نیاز قوانین/نقشه:
//////////    public Intersection getStart() { return startIntersection; }   // برای کلاس‌هایی که getStart می‌خوانند
//////////    public Intersection getEnd()   { return endIntersection; }     // برای کلاس‌هایی که getEnd می‌خوانند
//////////
//////////    /** وقتی یک طرف تقاطع را داریم، طرف دیگر را بده */
//////////    public Intersection getOtherEnd(Intersection x) {
//////////        if (Objects.equals(x, startIntersection)) return endIntersection;
//////////        if (Objects.equals(x, endIntersection))   return startIntersection;
//////////        return null;
//////////    }
//////////
//////////    public List<Lane> getForwardLanes()  { return Collections.unmodifiableList(forwardLanes); }
//////////    public List<Lane> getBackwardLanes() { return Collections.unmodifiableList(backwardLanes); }
//////////
//////////    // --- Crossing/Speed helpers (برای ارورهای World/Rules) ---
//////////    public List<PedestrianCrossing> getCrossings() { return Collections.unmodifiableList(crossings); }
//////////    public void addCrossing(PedestrianCrossing pc) { if (pc != null) crossings.add(pc); }
//////////
//////////    public int  getLocalSpeedLimit() { return localSpeedLimit; }
//////////    public void setLocalSpeedLimit(int kmh) { this.localSpeedLimit = Math.max(10, kmh); }
//////////
//////////    public boolean isNearRoundabout() { return nearRoundabout; }
//////////    public void   setNearRoundabout(boolean v) { this.nearRoundabout = v; }
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
//////////
//////////
//////////package infrastructure;
//////////
//////////import java.util.ArrayList;
//////////import java.util.List;
//////////
//////////import core.Identifiable;
//////////import core.Point;
//////////
///////////**
////////// * یک جاده بین دو تقاطع.
////////// */
//////////public class Road implements Identifiable {
//////////    private final String id;
//////////    private final Intersection startIntersection;
//////////    private final Intersection endIntersection;
//////////    private final List<Lane> forwardLanes = new ArrayList<>();
//////////    private final List<Lane> backwardLanes = new ArrayList<>();
//////////    private final boolean twoWay;
//////////
//////////    public Road(String id, Intersection a, Intersection b, boolean twoWay) {
//////////        this.id = id;
//////////        this.startIntersection = a;
//////////        this.endIntersection = b;
//////////        this.twoWay = twoWay;
//////////    }
//////////
//////////    @Override public String getId() { return id; }
//////////
//////////    public Intersection getStartIntersection() { return startIntersection; }
//////////    public Intersection getEndIntersection() { return endIntersection; }
//////////    public boolean isTwoWay() { return twoWay; }
//////////
//////////    public List<Lane> getForwardLanes() { return forwardLanes; }
//////////    public List<Lane> getBackwardLanes() { return backwardLanes; }
//////////
//////////    /** کمک به PathFinder: برگرداندن سرِ دیگر جاده نسبت به یک تقاطع. */
//////////    public Intersection getOtherEnd(Intersection x) {
//////////        if (x == null) return null;
//////////        if (x == startIntersection) return endIntersection;
//////////        if (x == endIntersection) return startIntersection;
//////////        return null;
//////////    }
//////////
//////////    /** طول هندسی جاده از روی مختصات تقاطع‌ها. */
//////////    public double getGeometricLength() {
//////////        Point a = startIntersection.getPosition();
//////////        Point b = endIntersection.getPosition();
//////////        int dx = b.getX() - a.getX();
//////////        int dy = b.getY() - a.getY();
//////////        return Math.sqrt((double)dx * dx + (double)dy * dy);
//////////    }
//////////
//////////    /** اگر جایی «متر» می‌خواست، فعلاً همان طول هندسی را می‌دهیم. */
//////////    public double getLengthMeters() { return getGeometricLength(); }
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
////////////package infrastructure;
////////////
////////////import java.util.*;
////////////import core.*;
////////////import pedestrian.PedestrianCrossing; // // ایمپورت کردن کلاس عبور عابر پیاده
////////////
////////////public class Road implements Identifiable {
////////////
////////////    private final String id;
////////////    private final Intersection start, end;
////////////    private final ArrayList<Lane> forward = new ArrayList<>();
////////////    private final ArrayList<Lane> backward = new ArrayList<>();
////////////    private boolean twoWay = true;
////////////
////////////    private double lengthMeters;           // طول متنوع
////////////    private double localSpeedLimit = 0.0;  // 0 یعنی از پیش‌فرض استفاده کن
////////////    private final ArrayList<SpeedBump> bumps = new ArrayList<>();
////////////    private final ArrayList<PedestrianCrossing> crossings = new ArrayList<>();
////////////
////////////    public Road(String id, Intersection a, Intersection b, double lengthMeters){
////////////        this.id=id; this.start=a; this.end=b; this.lengthMeters=lengthMeters;
////////////    }
////////////
////////////    public String getId(){ return id; }
////////////    public Intersection getStart(){ return start; }
////////////    public Intersection getEnd(){ return end; }
////////////
////////////    public List<Lane> getForwardLanes(){ return forward; }
////////////    public List<Lane> getBackwardLanes(){ return backward; }
////////////
////////////    public void setTwoWay(boolean v){ twoWay=v; }
////////////    public boolean isTwoWay(){ return twoWay; }
////////////
////////////    public double getLengthMeters(){ return lengthMeters; }
////////////    public void setLengthMeters(double m){ this.lengthMeters = Math.max(10.0, m); }
////////////
////////////    public double getLocalSpeedLimit(){ return localSpeedLimit; }
////////////    public void setLocalSpeedLimit(double mps){ this.localSpeedLimit = mps; }
////////////
////////////    public void addForwardLane(Lane ln){ forward.add(ln); }
////////////    public void addBackwardLane(Lane ln){ backward.add(ln); }
////////////
////////////    public void addSpeedBump(SpeedBump b){ bumps.add(b); }
////////////    public List<SpeedBump> getSpeedBumps(){ return bumps; }
////////////
////////////    public void addCrossing(PedestrianCrossing pc){ crossings.add(pc); }
////////////    public List<PedestrianCrossing> getCrossings(){ return crossings; }
////////////
////////////    // انتهای هندسی وابسته به جهت
////////////    public Intersection getStartIntersection(){ return start; }
////////////    public Intersection getEndIntersection(){ return end; }
////////////}
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
////////////package infrastructure; // // پکیج زیرساخت
////////////
////////////import java.util.ArrayList; // // برای لیست‌ها
////////////import java.util.List; // // اینترفیس لیست
////////////import core.Identifiable; // // اینترفیس شناسه‌دار
////////////import core.Point; // // برای محاسبه طول از مختصات
////////////import core.Direction; // // جهت لِین‌ها
////////////
////////////public class Road implements Identifiable { // // کلاس جاده
////////////    private String id; // // شناسه یکتا
////////////    private Intersection start; // // تقاطع شروع
////////////    private Intersection end; // // تقاطع پایان
////////////    private boolean twoWay; // // دوطرفه؟
////////////    private List<Lane> forwardLanes; // // لِین‌های جهت رفت
////////////    private List<Lane> backwardLanes; // // لِین‌های جهت برگشت
////////////    private double lengthMeters; // // طول تقریبی (px≈m)
////////////
////////////    public Road(String id, Intersection start, Intersection end, boolean twoWay) { // // سازنده
////////////        this.id = id; // // مقداردهی id
////////////        this.start = start; // // مقداردهی شروع
////////////        this.end = end; // // مقداردهی پایان
////////////        this.twoWay = twoWay; // // مقداردهی دوطرفه
////////////        this.forwardLanes = new ArrayList<Lane>(); // // لیست اولیه forward
////////////        this.backwardLanes = new ArrayList<Lane>(); // // لیست اولیه backward
////////////        this.lengthMeters = computeLengthMeters(); // // محاسبه طول
////////////    }
////////////
////////////    private double computeLengthMeters() { // // محاسبهٔ طول از مختصات تقاطع‌ها
////////////        Point a = this.start.getPosition(); // // مختصات A
////////////        Point b = this.end.getPosition(); // // مختصات B
////////////        int dx = b.getX() - a.getX(); // // Δx
////////////        int dy = b.getY() - a.getY(); // // Δy
////////////        double pixels = Math.sqrt((double)dx * dx + (double)dy * dy); // // فاصله اقلیدسی
////////////        return pixels; // // px≈m
////////////    }
////////////
////////////    @Override
////////////    public String getId() { return this.id; } // // شناسه
////////////
////////////    // --- نقاط اتصال ---
////////////    public Intersection getStart() { return this.start; } // // تقاطع شروع
////////////    public Intersection getEnd()   { return this.end;   } // // تقاطع پایان
////////////    public Intersection getStartIntersection() { return this.start; } // // معادل برای سازگاری
////////////    public Intersection getEndIntersection()   { return this.end;   } // // معادل برای سازگاری
////////////
////////////    // --- طول ---
////////////    public double getLengthMeters() { return this.lengthMeters; } // // طول با نام اصلی
////////////    public double getLength() { return this.lengthMeters; } // // آلیاس برای کدهایی که getLength می‌خوان
////////////
////////////    public boolean isTwoWay() { return this.twoWay; } // // دوطرفه؟
////////////
////////////    // ====== افزودن/ست‌کردن لِین‌ها ======
////////////    public void addForwardLane(Lane lane) { // // افزودن لِین جهت رفت
////////////        if (lane != null) { lane.setParentRoad(this); this.forwardLanes.add(lane); } // // تنظیم والد و افزودن
////////////    }
////////////
////////////    public void addBackwardLane(Lane lane) { // // افزودن لِین جهت برگشت
////////////        if (lane != null) { lane.setParentRoad(this); this.backwardLanes.add(lane); } // // تنظیم والد و افزودن
////////////    }
////////////
////////////    public void setForwardLanes(List<Lane> lanes) { // // ست لِین‌های رفت (برای سازگاری)
////////////        this.forwardLanes = (lanes != null) ? lanes : new ArrayList<Lane>(); // // لیست امن
////////////        for (int i = 0; i < this.forwardLanes.size(); i++) { // // حلقه
////////////            Lane ln = this.forwardLanes.get(i); // // لِین
////////////            if (ln != null) { ln.setParentRoad(this); } // // ست والد
////////////        }
////////////    }
////////////
////////////    public void setBackwardLanes(List<Lane> lanes) { // // ست لِین‌های برگشت (برای سازگاری)
////////////        this.backwardLanes = (lanes != null) ? lanes : new ArrayList<Lane>(); // // لیست امن
////////////        for (int i = 0; i < this.backwardLanes.size(); i++) { // // حلقه
////////////            Lane ln = this.backwardLanes.get(i); // // لِین
////////////            if (ln != null) { ln.setParentRoad(this); } // // ست والد
////////////        }
////////////    }
////////////
////////////    public List<Lane> getForwardLanes() { return this.forwardLanes; } // // لِین‌های رفت
////////////    public List<Lane> getBackwardLanes() { return this.backwardLanes; } // // لِین‌های برگشت
////////////
////////////    public List<Lane> getLanesByDirection(Direction dir) { // // لِین‌های هم‌جهت
////////////        if (dir == Direction.EAST || dir == Direction.SOUTH) { // // تعریف ساده
////////////            return this.forwardLanes; // // forward
////////////        } else { // // NORTH/WEST
////////////            return this.backwardLanes; // // backward
////////////        }
////////////    }
////////////
////////////    public Intersection getOtherEnd(Intersection oneSide) { // // سر دیگر جاده
////////////        if (oneSide == null) { return null; } // // نال‌چک
////////////        if (oneSide == this.start) { return this.end; } // // اگر شروع بود
////////////        if (oneSide == this.end)   { return this.start; } // // اگر پایان بود
////////////        return null; // // غیر از این، ارتباطی ندارد
////////////    }
////////////}
////////////
////////////
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
////////////package infrastructure; // // پکیج زیرساخت
////////////
////////////import java.util.ArrayList; // // برای لیست‌ها
////////////import java.util.List; // // اینترفیس لیست
////////////import core.Identifiable; // // اینترفیس شناسه‌دار
////////////import core.Point; // // برای محاسبه طول از مختصات
//////////////import core.Direction; // // جهت لِین‌ها
////////////
////////////public class Road implements Identifiable { // // کلاس جاده
////////////    private String id; // // شناسه یکتا
////////////    private Intersection start; // // تقاطع شروع
////////////    private Intersection end; // // تقاطع پایان
////////////    private boolean twoWay; // // دوطرفه؟
////////////    private List<Lane> forwardLanes; // // لِین‌های جهت رفت (start→end)
////////////    private List<Lane> backwardLanes; // // لِین‌های جهت برگشت (end→start)
////////////    private double lengthMeters; // // طول تقریبی (px≈m)
////////////
////////////    public Road(String id, Intersection start, Intersection end, boolean twoWay) { // // سازنده
////////////        this.id = id; // // مقداردهی id
////////////        this.start = start; // // مقداردهی شروع
////////////        this.end = end; // // مقداردهی پایان
////////////        this.twoWay = twoWay; // // مقداردهی دوطرفه
////////////        this.forwardLanes = new ArrayList<Lane>(); // // لیست اولیه forward
////////////        this.backwardLanes = new ArrayList<Lane>(); // // لیست اولیه backward
////////////        this.lengthMeters = computeLengthMeters(); // // محاسبه طول
////////////    }
////////////
////////////    private double computeLengthMeters() { // // محاسبهٔ طول از مختصات تقاطع‌ها
////////////        Point a = this.start.getPosition(); // // مختصات A
////////////        Point b = this.end.getPosition(); // // مختصات B
////////////        int dx = b.getX() - a.getX(); // // Δx
////////////        int dy = b.getY() - a.getY(); // // Δy
////////////        double pixels = Math.sqrt((double)dx * dx + (double)dy * dy); // // فاصله اقلیدسی
////////////        return pixels; // // فرض ساده: px≈m
////////////    }
////////////
////////////    @Override
////////////    public String getId() { return this.id; } // // شناسه
////////////
////////////    // --- API اصلی ---
////////////    public Intersection getStart() { return this.start; } // // تقاطع شروع
////////////    public Intersection getEnd()   { return this.end;   } // // تقاطع پایان
////////////
////////////    // --- API سازگاری با کدهای موجود ---
////////////    public Intersection getStartIntersection() { return this.start; } // // معادل getStart
////////////    public Intersection getEndIntersection()   { return this.end;   } // // معادل getEnd
////////////
////////////    public boolean isTwoWay() { return this.twoWay; } // // دوطرفه؟
////////////    public double getLengthMeters() { return this.lengthMeters; } // // طول
////////////
////////////    // ====== افزودن لِین‌ها (برای DemoMaps/DemoTraffic) ======
////////////    public void addForwardLane(Lane lane) { // // افزودن لِین جهت رفت
////////////        if (lane != null) { // // چک نال
////////////            lane.setParentRoad(this); // // ست جادهٔ والد
////////////            this.forwardLanes.add(lane); // // افزودن به لیست
////////////        }
////////////    }
////////////
////////////    public void addBackwardLane(Lane lane) { // // افزودن لِین جهت برگشت
////////////        if (lane != null) { // // چک نال
////////////            lane.setParentRoad(this); // // ست جادهٔ والد
////////////            this.backwardLanes.add(lane); // // افزودن به لیست
////////////        }
////////////    }
////////////
////////////    // ====== سازگاری با نسخهٔ قبلی CityMap.buildGrid ======
////////////    public void setForwardLanes(List<Lane> lanes) { // // ست لِین‌های رفت
////////////        this.forwardLanes = (lanes != null) ? lanes : new ArrayList<Lane>(); // // ذخیره یا خالی
////////////        for (int i = 0; i < this.forwardLanes.size(); i++) { // // حلقه
////////////            Lane ln = this.forwardLanes.get(i); // // لِین
////////////            if (ln != null) { ln.setParentRoad(this); } // // ست والد
////////////        }
////////////    }
////////////
////////////    public void setBackwardLanes(List<Lane> lanes) { // // ست لِین‌های برگشت
////////////        this.backwardLanes = (lanes != null) ? lanes : new ArrayList<Lane>(); // // ذخیره یا خالی
////////////        for (int i = 0; i < this.backwardLanes.size(); i++) { // // حلقه
////////////            Lane ln = this.backwardLanes.get(i); // // لِین
////////////            if (ln != null) { ln.setParentRoad(this); } // // ست والد
////////////        }
////////////    }
////////////
////////////    public List<Lane> getForwardLanes() { return this.forwardLanes; } // // لِین‌های رفت
////////////    public List<Lane> getBackwardLanes() { return this.backwardLanes; } // // لِین‌های برگشت
////////////
////////////    public List<Lane> getLanesByDirection(Direction dir) { // // لِین‌های هم‌جهت
////////////        if (dir == Direction.EAST || dir == Direction.SOUTH) { // // تعریف ساده
////////////            return this.forwardLanes; // // forward
////////////        } else { // // NORTH/WEST
////////////            return this.backwardLanes; // // backward
////////////        }
////////////    }
////////////
////////////    public Intersection getOtherEnd(Intersection oneSide) { // // سر دیگر جاده
////////////        if (oneSide == null) { return null; } // // نال‌چک
////////////        if (oneSide == this.start) { return this.end; } // // اگر شروع بود
////////////        if (oneSide == this.end)   { return this.start; } // // اگر پایان بود
////////////        return null; // // غیر از این، ارتباطی ندارد
////////////    }
////////////}
////////////
////////////
////////////
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
////////////package infrastructure; // // پکیج زیرساخت
////////////
////////////import java.util.ArrayList; // // برای لیست‌ها
////////////import java.util.List; // // اینترفیس لیست
////////////import core.Identifiable; // // اینترفیس شناسه‌دار
////////////import core.Point; // // برای محاسبه طول از مختصات
////////////import core.Direction; // // جهت لِین‌ها
////////////
////////////public class Road implements Identifiable { // // کلاس جاده
////////////    private String id; // // شناسه یکتا
////////////    private Intersection start; // // تقاطع شروع
////////////    private Intersection end; // // تقاطع پایان
////////////    private boolean twoWay; // // دوطرفه؟
////////////    private List<Lane> forwardLanes; // // لِین‌های جهت رفت (start→end)
////////////    private List<Lane> backwardLanes; // // لِین‌های جهت برگشت (end→start)
////////////    private double lengthMeters; // // طول تقریبی (px≈m)
////////////
////////////    public Road(String id, Intersection start, Intersection end, boolean twoWay) { // // سازنده
////////////        this.id = id; // // مقداردهی id
////////////        this.start = start; // // مقداردهی شروع
////////////        this.end = end; // // مقداردهی پایان
////////////        this.twoWay = twoWay; // // مقداردهی دوطرفه
////////////        this.forwardLanes = new ArrayList<Lane>(); // // لیست اولیه forward
////////////        this.backwardLanes = new ArrayList<Lane>(); // // لیست اولیه backward
////////////        this.lengthMeters = computeLengthMeters(); // // محاسبه طول
////////////    }
////////////
////////////    private double computeLengthMeters() { // // محاسبه طول از مختصات تقاطع‌ها
////////////        Point a = this.start.getPosition(); // // مختصات A
////////////        Point b = this.end.getPosition(); // // مختصات B
////////////        int dx = b.getX() - a.getX(); // // Δx
////////////        int dy = b.getY() - a.getY(); // // Δy
////////////        double pixels = Math.sqrt((double)dx * dx + (double)dy * dy); // // فاصله اقلیدسی
////////////        return pixels; // // فرض ساده: px≈m
////////////    }
////////////
////////////    @Override
////////////    public String getId() { return this.id; } // // شناسه
////////////
////////////    // --- API اصلی که قبلاً داشتیم ---
////////////    public Intersection getStart() { return this.start; } // // تقاطع شروع
////////////    public Intersection getEnd()   { return this.end;   } // // تقاطع پایان
////////////
////////////    // --- API سازگاری با Vehicle/SimulatorPanel تو: ---
////////////    public Intersection getStartIntersection() { return this.start; } // // معادل getStart
////////////    public Intersection getEndIntersection()   { return this.end;   } // // معادل getEnd
////////////
////////////    public boolean isTwoWay() { return this.twoWay; } // // دوطرفه؟
////////////    public double getLengthMeters() { return this.lengthMeters; } // // طول
////////////
////////////    public void setForwardLanes(List<Lane> lanes) { // // ست لِین‌های رفت
////////////        this.forwardLanes = (lanes != null) ? lanes : new ArrayList<Lane>(); // // ذخیره یا خالی
////////////        int size = this.forwardLanes.size(); // // اندازه لیست
////////////        for (int i = 0; i < size; i++) { // // حلقه روی لِین‌ها
////////////            Lane ln = this.forwardLanes.get(i); // // لِین iام
////////////            if (ln != null) { ln.setParentRoad(this); } // // ست والد جاده
////////////        }
////////////    }
////////////
////////////    public void setBackwardLanes(List<Lane> lanes) { // // ست لِین‌های برگشت
////////////        this.backwardLanes = (lanes != null) ? lanes : new ArrayList<Lane>(); // // ذخیره یا خالی
////////////        int size = this.backwardLanes.size(); // // اندازه
////////////        for (int i = 0; i < size; i++) { // // حلقه
////////////            Lane ln = this.backwardLanes.get(i); // // لِین iام
////////////            if (ln != null) { ln.setParentRoad(this); } // // ست والد
////////////        }
////////////    }
////////////
////////////    public List<Lane> getForwardLanes() { return this.forwardLanes; } // // لِین‌های رفت
////////////    public List<Lane> getBackwardLanes() { return this.backwardLanes; } // // لِین‌های برگشت
////////////
////////////    public List<Lane> getLanesByDirection(Direction dir) { // // لِین‌های هم‌جهت
////////////        if (dir == Direction.EAST || dir == Direction.SOUTH) { // // تعریف ساده
////////////            return this.forwardLanes; // // forward
////////////        } else { // // NORTH یا WEST
////////////            return this.backwardLanes; // // backward
////////////        }
////////////    }
////////////
////////////    public Intersection getOtherEnd(Intersection oneSide) { // // سر دیگر جاده
////////////        if (oneSide == null) { return null; } // // نال‌چک
////////////        if (oneSide == this.start) { return this.end; } // // اگر شروع بود
////////////        if (oneSide == this.end)   { return this.start; } // // اگر پایان بود
////////////        return null; // // غیر از این، ارتباطی ندارد
////////////    }
////////////}
////////////
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
////////////package infrastructure; // // پکیج زیرساخت
////////////
////////////import java.util.ArrayList; // // برای ساخت لیست
////////////import java.util.List; // // اینترفیس لیست
////////////import core.Identifiable; // // اینترفیس شناسه‌دار
////////////import core.Point; // // برای محاسبه طول از مختصات
////////////import core.Direction; // // جهت‌ها
////////////
////////////public class Road implements Identifiable { // // کلاس جاده
////////////    private String id; // // شناسه یکتا
////////////    private Intersection start; // // تقاطع شروع
////////////    private Intersection end; // // تقاطع پایان
////////////    private boolean twoWay; // // آیا جاده دوطرفه است؟
////////////    private List<Lane> forwardLanes; // // لِین‌های جهت رفت (start→end)
////////////    private List<Lane> backwardLanes; // // لِین‌های جهت برگشت (end→start)
////////////    private double lengthMeters; // // طول تقریبی جاده (px≈m برای سادگی)
////////////
////////////    public Road(String id, Intersection start, Intersection end, boolean twoWay) { // // سازندهٔ کامل
////////////        this.id = id; // // مقداردهی id
////////////        this.start = start; // // تنظیم تقاطع شروع
////////////        this.end = end; // // تنظیم تقاطع پایان
////////////        this.twoWay = twoWay; // // تنظیم دوطرفه بودن
////////////        this.forwardLanes = new ArrayList<Lane>(); // // لیست اولیه لِین‌های رفت
////////////        this.backwardLanes = new ArrayList<Lane>(); // // لیست اولیه لِین‌های برگشت
////////////        this.lengthMeters = computeLengthMeters(); // // محاسبه طول از مختصات تقاطع‌ها
////////////    }
////////////
////////////    private double computeLengthMeters() { // // محاسبهٔ طول جاده
////////////        Point a = this.start.getPosition(); // // گرفتن مختصات شروع
////////////        Point b = this.end.getPosition(); // // گرفتن مختصات پایان
////////////        int dx = b.getX() - a.getX(); // // تفاضل X
////////////        int dy = b.getY() - a.getY(); // // تفاضل Y
////////////        double pixels = Math.sqrt((double)dx * dx + (double)dy * dy); // // فاصله اقلیدسی
////////////        return pixels; // // فرض ساده: 1px≈1m
////////////    }
////////////
////////////    @Override
////////////    public String getId() { return this.id; } // // برگرداندن شناسه
////////////
////////////    public Intersection getStart() { return this.start; } // // گرفتن تقاطع شروع
////////////    public Intersection getEnd() { return this.end; } // // گرفتن تقاطع پایان
////////////    public boolean isTwoWay() { return this.twoWay; } // // آیا دوطرفه است؟
////////////    public double getLengthMeters() { return this.lengthMeters; } // // طول جاده
////////////
////////////    public void setForwardLanes(List<Lane> lanes) { // // ست‌کردن لِین‌های رفت
////////////        this.forwardLanes = (lanes != null) ? lanes : new ArrayList<Lane>(); // // ذخیره یا خالی
////////////        int size = this.forwardLanes.size(); // // اندازه لیست
////////////        for (int i = 0; i < size; i++) { // // حلقه روی لِین‌ها
////////////            Lane ln = this.forwardLanes.get(i); // // لِین iام
////////////            if (ln != null) { ln.setParentRoad(this); } // // تنظیم والد جاده
////////////        }
////////////    }
////////////
////////////    public void setBackwardLanes(List<Lane> lanes) { // // ست‌کردن لِین‌های برگشت
////////////        this.backwardLanes = (lanes != null) ? lanes : new ArrayList<Lane>(); // // ذخیره یا خالی
////////////        int size = this.backwardLanes.size(); // // اندازه لیست
////////////        for (int i = 0; i < size; i++) { // // حلقه روی لِین‌ها
////////////            Lane ln = this.backwardLanes.get(i); // // لِین iام
////////////            if (ln != null) { ln.setParentRoad(this); } // // تنظیم والد جاده
////////////        }
////////////    }
////////////
////////////    public List<Lane> getForwardLanes() { return this.forwardLanes; } // // گرفتن لِین‌های رفت
////////////    public List<Lane> getBackwardLanes() { return this.backwardLanes; } // // گرفتن لِین‌های برگشت
////////////
////////////    public List<Lane> getLanesByDirection(Direction dir) { // // لِین‌های مطابق جهت
////////////        if (dir == Direction.EAST || dir == Direction.SOUTH) { // // تعریف ساده برای forward
////////////            return this.forwardLanes; // // forward
////////////        } else { // // NORTH یا WEST
////////////            return this.backwardLanes; // // backward
////////////        }
////////////    }
////////////
////////////    public Intersection getOtherEnd(Intersection oneSide) { // // گرفتن سر دیگر جاده
////////////        if (oneSide == null) { return null; } // // چک نال
////////////        if (oneSide == this.start) { return this.end; } // // اگر ورودی شروع بود، پایان بده
////////////        if (oneSide == this.end) { return this.start; } // // اگر ورودی پایان بود، شروع بده
////////////        return null; // // در غیر این صورت ارتباطی ندارد
////////////    }
////////////}
////////////
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
//////////
//////////
//////////
//////////
////////////package infrastructure; // // پکیج زیرساخت
////////////
////////////import java.util.ArrayList; // // برای ساخت لیست‌ها
////////////import java.util.List; // // اینترفیس لیست
////////////import core.Identifiable; // // برای داشتن شناسه یکتا
////////////import core.Point; // // برای محاسبه طول از مختصات
////////////import core.Direction; // // جهت حرکت لِین‌ها
////////////
////////////public class Road implements Identifiable { // // کلاس جاده
////////////    private String id; // // شناسه یکتا
////////////    private Intersection start; // // تقاطع شروع
////////////    private Intersection end; // // تقاطع پایان
////////////    private boolean twoWay; // // آیا دوطرفه است؟
////////////    private List<Lane> forwardLanes; // // لِین‌های جهت رفت (از start به end)
////////////    private List<Lane> backwardLanes; // // لِین‌های جهت برگشت (از end به start)
////////////    private double lengthMeters; // // طول جاده بر حسب متر (تقریبی از فاصله نقاط)
////////////
////////////    public Road(String id, Intersection start, Intersection end, boolean twoWay) { // // سازنده کامل
////////////        this.id = id; // // مقداردهی شناسه
////////////        this.start = start; // // مقداردهی تقاطع شروع
////////////        this.end = end; // // مقداردهی تقاطع پایان
////////////        this.twoWay = twoWay; // // مقداردهی دوطرفه بودن
////////////        this.forwardLanes = new ArrayList<Lane>(); // // آماده‌سازی لیست لِین‌های رفت
////////////        this.backwardLanes = new ArrayList<Lane>(); // // آماده‌سازی لیست لِین‌های برگشت
////////////        this.lengthMeters = computeLengthMeters(); // // محاسبه طول تقریبی
////////////    }
////////////
////////////    private double computeLengthMeters() { // // محاسبه طول از مختصات تقاطع‌ها
////////////        Point a = this.start.getPosition(); // // گرفتن مختصات شروع
////////////        Point b = this.end.getPosition(); // // گرفتن مختصات پایان
////////////        int dx = b.getX() - a.getX(); // // تفاضل X
////////////        int dy = b.getY() - a.getY(); // // تفاضل Y
////////////        double pixels = Math.sqrt((double) dx * dx + (double) dy * dy); // // فاصله اقلیدسی پیکسلی
////////////        // اگر در جای دیگری مقیاس پیکسل→متر داری، اینجا اعمال کن. فعلاً 1px≈1m فرض می‌کنیم.
////////////        return pixels; // // برگرداندن طول
////////////    }
////////////
////////////    @Override
////////////    public String getId() { // // پیاده‌سازی getId
////////////        return this.id; // // برگرداندن شناسه
////////////    }
////////////
////////////    public Intersection getStart() { // // گرفتن تقاطع شروع
////////////        return this.start; // // برگرداندن مرجع
////////////    }
////////////
////////////    public Intersection getEnd() { // // گرفتن تقاطع پایان
////////////        return this.end; // // برگرداندن مرجع
////////////    }
////////////
////////////    public boolean isTwoWay() { // // آیا دوطرفه است؟
////////////        return this.twoWay; // // برگرداندن مقدار
////////////    }
////////////
////////////    public double getLengthMeters() { // // گرفتن طول جاده
////////////        return this.lengthMeters; // // برگرداندن طول
////////////    }
////////////
////////////    // ===== API که CityMap لازم داشت =====
////////////    public void setForwardLanes(List<Lane> lanes) { // // ست کردن لِین‌های رفت
////////////        this.forwardLanes = (lanes != null) ? lanes : new ArrayList<Lane>(); // // ذخیره یا ساخت لیست خالی
////////////        int size = this.forwardLanes.size(); // // اندازه لیست
////////////        for (int i = 0; i < size; i++) { // // حلقه روی لِین‌ها
////////////            Lane ln = this.forwardLanes.get(i); // // گرفتن لِین
////////////            if (ln != null) { // // چک نال
////////////                ln.setParentRoad(this); // // اعلام والد بودن این جاده
////////////            } // // پایان if
////////////        } // // پایان for
////////////    }
////////////
////////////    public void setBackwardLanes(List<Lane> lanes) { // // ست کردن لِین‌های برگشت
////////////        this.backwardLanes = (lanes != null) ? lanes : new ArrayList<Lane>(); // // ذخیره یا ساخت لیست خالی
////////////        int size = this.backwardLanes.size(); // // اندازه لیست
////////////        for (int i = 0; i < size; i++) { // // حلقه روی لِین‌ها
////////////            Lane ln = this.backwardLanes.get(i); // // گرفتن لِین
////////////            if (ln != null) { // // چک نال
////////////                ln.setParentRoad(this); // // تنظیم والد جاده
////////////            } // // پایان if
////////////        } // // پایان for
////////////    }
////////////
////////////    public List<Lane> getForwardLanes() { // // گرفتن لِین‌های رفت
////////////        return this.forwardLanes; // // برگرداندن لیست
////////////    }
////////////
////////////    public List<Lane> getBackwardLanes() { // // گرفتن لِین‌های برگشت
////////////        return this.backwardLanes; // // برگرداندن لیست
////////////    }
////////////
////////////    public List<Lane> getLanesByDirection(Direction dir) { // // برگرداندن لِین‌های مطابق جهت
////////////        if (dir == Direction.EAST || dir == Direction.SOUTH) { // // برای سادگی: EAST/SOUTH را forward می‌گیریم
////////////            return this.forwardLanes; // // لیست forward
////////////        } else { // // برای NORTH/WEST
////////////            return this.backwardLanes; // // لیست backward
////////////        }
////////////    }
////////////
////////////    public Intersection getOtherEnd(Intersection oneSide) { // // گرفتن سر دیگر جاده
////////////        if (oneSide == null) { // // اگر ورودی نال بود
////////////            return null; // // مقدار خنثی
////////////        }
////////////        if (oneSide == this.start) { // // اگر ورودی شروع بود
////////////            return this.end; // // برگرداندن پایان
////////////        }
////////////        if (oneSide == this.end) { // // اگر ورودی پایان بود
////////////            return this.start; // // برگرداندن شروع
////////////        }
////////////        return null; // // اگر هیچکدام نبود (جاده ارتباطی با آن ندارد)
////////////    }
////////////}
////////////
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
//////////
//////////
//////////
//////////
//////////
//////////
//////////
////////////package infrastructure; // // پکیج زیرساخت
////////////
////////////import core.Identifiable; // // برای داشتن شناسه یکتا
////////////import core.Direction;   // // برای تشخیص جهت لاین‌ها
////////////import core.Point;       // // برای محاسبه طول راه (اختیاری)
////////////
////////////import java.util.ArrayList; // // لیست پویا برای نگهداری لاین‌ها
////////////import java.util.List;      // // اینترفیس لیست
////////////
////////////public class Road implements Identifiable { // // کلاس جاده که بین دو تقاطع قرار می‌گیرد
////////////    private final String id; // // شناسه یکتا برای هر جاده
////////////    private final Intersection startIntersection; // // تقاطع شروع جاده
////////////    private final Intersection endIntersection;   // // تقاطع پایان جاده
////////////    private final boolean isTwoWay;               // // آیا جاده دوطرفه است یا یک‌طرفه
////////////
////////////    private final List<Lane> forwardLanes;  // // لاین‌های جهت رفت (از start به end)
////////////    private final List<Lane> backwardLanes; // // لاین‌های جهت برگشت (از end به start)
////////////
////////////    public Road(String id, Intersection start, Intersection end, boolean twoWay) { // // سازنده
////////////        this.id = id;                      // // مقداردهی شناسه
////////////        this.startIntersection = start;    // // ثبت تقاطع شروع
////////////        this.endIntersection = end;        // // ثبت تقاطع پایان
////////////        this.isTwoWay = twoWay;            // // ثبت دوطرفه بودن
////////////        this.forwardLanes = new ArrayList<Lane>();   // // ایجاد لیست خالی لاین‌های رفت
////////////        this.backwardLanes = new ArrayList<Lane>();  // // ایجاد لیست خالی لاین‌های برگشت
////////////    }
////////////
////////////    @Override
////////////    public String getId() { // // گتر شناسه
////////////        return id; // // برگرداندن شناسه
////////////    }
////////////
////////////    public Intersection getStartIntersection() { // // گتر تقاطع شروع
////////////        return startIntersection; // // خروجی
////////////    }
////////////
////////////    public Intersection getEndIntersection() { // // گتر تقاطع پایان
////////////        return endIntersection; // // خروجی
////////////    }
////////////
////////////    public boolean isTwoWay() { // // آیا جاده دوطرفه است؟
////////////        return isTwoWay; // // خروجی
////////////    }
////////////
////////////    public List<Lane> getForwardLanes() { // // گتر لیست لاین‌های رفت
////////////        return forwardLanes; // // خروجی
////////////    }
////////////
////////////    public List<Lane> getBackwardLanes() { // // گتر لیست لاین‌های برگشت
////////////        return backwardLanes; // // خروجی
////////////    }
////////////
////////////    public List<Lane> getAllLanes() { // // برگرداندن همه لاین‌ها (رفت + برگشت)
////////////        List<Lane> all = new ArrayList<Lane>(); // // ساخت یک لیست جدید
////////////        all.addAll(forwardLanes); // // افزودن لاین‌های رفت
////////////        all.addAll(backwardLanes); // // افزودن لاین‌های برگشت
////////////        return all; // // خروجی
////////////    }
////////////
////////////    // ===================== افزودن لاین‌ها + اتصال چپ/راست =====================
////////////
////////////    public void addForwardLane(Lane lane) { // // افزودن یک لاین به جهت رفت (start→end)
////////////        forwardLanes.add(lane); // // افزودن به انتهای لیست
////////////        lane.setLeftAdjacentLane(null);  // // پیش‌فرض: همسایه چپ ندارد
////////////        lane.setRightAdjacentLane(null); // // پیش‌فرض: همسایه راست ندارد
////////////
////////////        if (forwardLanes.size() >= 2) { // // اگر حداقل دو لاین در این جهت داریم
////////////            Lane left  = forwardLanes.get(forwardLanes.size() - 2); // // لاین قبلی را «چپ» در نظر می‌گیریم
////////////            Lane right = forwardLanes.get(forwardLanes.size() - 1); // // لاین جدید را «راست» در نظر می‌گیریم
////////////            left.setRightAdjacentLane(right);  // // راستِ لاین چپ = لاین جدید
////////////            right.setLeftAdjacentLane(left);   // // چپِ لاین جدید = لاین قبلی
////////////        }
////////////    }
////////////
////////////    public void addBackwardLane(Lane lane) { // // افزودن یک لاین به جهت برگشت (end→start)
////////////        backwardLanes.add(lane); // // افزودن به انتهای لیست
////////////        lane.setLeftAdjacentLane(null);  // // پیش‌فرض: همسایه چپ ندارد
////////////        lane.setRightAdjacentLane(null); // // پیش‌فرض: همسایه راست ندارد
////////////
////////////        if (backwardLanes.size() >= 2) { // // اگر حداقل دو لاین در این جهت داریم
////////////            Lane left  = backwardLanes.get(backwardLanes.size() - 2); // // لاین قبلی = چپ
////////////            Lane right = backwardLanes.get(backwardLanes.size() - 1); // // لاین جدید = راست
////////////            left.setRightAdjacentLane(right);  // // راستِ لاین چپ = لاین جدید
////////////            right.setLeftAdjacentLane(left);   // // چپِ لاین جدید = لاین قبلی
////////////        }
////////////    }
////////////
////////////    // ===================== توابع کمکی مفید =====================
////////////
////////////    public Lane pickAnyLane(Direction dir) { // // انتخاب یک لاین از یک جهت (برای تست/ورود خودرو)
////////////        if (dir == Direction.EAST || dir == Direction.SOUTH) { // // اگر جهت رفت باشد
////////////            return forwardLanes.isEmpty() ? null : forwardLanes.get(0); // // اولین لاین رفت یا null
////////////        } else { // // در غیر این صورت (WEST/NORTH) یعنی برگشت
////////////            return backwardLanes.isEmpty() ? null : backwardLanes.get(0); // // اولین لاین برگشت یا null
////////////        }
////////////    }
////////////
////////////    public Intersection getOtherEnd(Intersection one) { // // گرفتن تقاطع مقابل (اگر یکی از دو سر را بدهی)
////////////        if (one == startIntersection) return endIntersection; // // اگر ورودی سرِ شروع بود، خروجی سرِ پایان
////////////        if (one == endIntersection)   return startIntersection; // // اگر ورودی سرِ پایان بود، خروجی سرِ شروع
////////////        return null; // // اگر هیچ‌کدام نبود، null (ورودی نامعتبر)
////////////    }
////////////
////////////    public double getLength() { // // طول هندسی جاده (برای نیازهای محاسباتی/تصویری)
////////////        Point A = startIntersection.getPosition(); // // نقطه شروع
////////////        Point B = endIntersection.getPosition();   // // نقطه پایان
////////////        double dx = B.getX() - A.getX(); // // اختلاف X
////////////        double dy = B.getY() - A.getY(); // // اختلاف Y
////////////        return Math.sqrt(dx * dx + dy * dy); // // طول خط (پیثاگورس)
////////////    }
////////////
////////////    @Override
////////////    public String toString() { // // نمایش متنی ساده برای دیباگ
////////////        return "Road{" + id + ", " + startIntersection.getId() + "->" + endIntersection.getId()
////////////                + ", twoWay=" + isTwoWay + ", fLanes=" + forwardLanes.size()
////////////                + ", bLanes=" + backwardLanes.size() + "}"; // // ساخت رشتهٔ توضیحی
////////////    }
////////////}
////////////
////////////
////////////
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
////////////
////////////package infrastructure; // // پکیج زیرساخت
////////////
////////////import core.Identifiable; // // برای داشتن شناسه
////////////import java.util.ArrayList; // // لیست پویا
////////////import java.util.List; // // اینترفیس لیست
////////////
////////////public class Road implements Identifiable { // // کلاس جاده
////////////    private final String id; // // شناسه جاده
////////////    private final Intersection startIntersection; // // تقاطع شروع
////////////    private final Intersection endIntersection; // // تقاطع پایان
////////////    private final boolean isTwoWay; // // آیا دوطرفه است؟
////////////    private final List<Lane> forwardLanes; // // لاین‌های جهت رفت (start->end)
////////////    private final List<Lane> backwardLanes; // // لاین‌های جهت برگشت (end->start)
////////////
////////////    public Road(String id, Intersection start, Intersection end, boolean twoWay) { // // سازنده
////////////        this.id = id; // // مقداردهی شناسه
////////////        this.startIntersection = start; // // ست تقاطع شروع
////////////        this.endIntersection = end; // // ست تقاطع پایان
////////////        this.isTwoWay = twoWay; // // ست دوطرفه بودن
////////////        this.forwardLanes = new ArrayList<Lane>(); // // ایجاد آرایه لاین‌های رفت
////////////        this.backwardLanes = new ArrayList<Lane>(); // // ایجاد آرایه لاین‌های برگشت
////////////    }
////////////
////////////    @Override
////////////    public String getId() { // // برگرداندن شناسه
////////////        return id; // // خروجی
////////////    }
////////////
////////////    public Intersection getStartIntersection() { return startIntersection; } // // گتر شروع
////////////    public Intersection getEndIntersection()   { return endIntersection; }   // // گتر پایان
////////////    public List<Lane> getForwardLanes()       { return forwardLanes; }      // // گتر لیست رفت
////////////    public List<Lane> getBackwardLanes()      { return backwardLanes; }     // // گتر لیست برگشت
////////////    public boolean isTwoWay()                 { return isTwoWay; }          // // گتر دوطرفه بودن
////////////
////////////    public void addForwardLane(Lane lane) { // // افزودن لاین به جهت رفت
////////////        forwardLanes.add(lane); // // اضافه به لیست
////////////        lane.setRightAdjacentLane(null); // // پیش‌فرض بدون همسایه راست
////////////        lane.setLeftAdjacentLane(null);  // // پیش‌فرض بدون همسایه چپ
////////////        if (forwardLanes.size() >= 2) { // // اگر حداقل دو لاین وجود دارد
////////////            Lane left = forwardLanes.get(forwardLanes.size() - 2); // // لاین قبلی را چپ فرض می‌کنیم
////////////            Lane right = forwardLanes.get(forwardLanes.size() - 1); // // این لاین جدید راستِ آن است
////////////            left.setRightAdjacentLane(right); // // اتصال راست لاین چپی
////////////            right.setLeftAdjacentLane(left);  // // اتصال چپ لاین راستی
////////////        }
////////////    }
////////////
////////////    public void addBackwardLane(Lane lane) { // // افزودن لاین به جهت برگشت
////////////        backwardLanes.add(lane); // // اضافه به لیست
////////////        lane.setRightAdjacentLane(null); // // پیش‌فرض بدون همسایه راست
////////////        lane.setLeftAdjacentLane(null);  // // پیش‌فرض بدون همسایه چپ
////////////        if (backwardLanes.size() >= 2) { // // اگر حداقل دو لاین وجود دارد
////////////            Lane left = backwardLanes.get(backwardLanes.size() - 2); // // لاین قبلی را چپ فرض می‌کنیم
////////////            Lane right = backwardLanes.get(backwardLanes.size() - 1); // // این لاین جدید راستِ آن است
////////////            left.setRightAdjacentLane(right); // // اتصال راست لاین چپی
////////////            right.setLeftAdjacentLane(left);  // // اتصال چپ لاین راستی
////////////        }
////////////    }
////////////}
////////////
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
////////////package infrastructure;
////////////
////////////
////////////import core.*;
////////////import java.util.ArrayList;
////////////import java.util.List;
////////////
////////////public class Road implements Identifiable {
////////////    private String id;
////////////    private Intersection startIntersection;
////////////    private Intersection endIntersection;
////////////    private List<Lane> forwardLanes;
////////////    private List<Lane> backwardLanes;
////////////    private boolean isTwoWay;
////////////
////////////    public Road(String id, Intersection start, Intersection end, boolean isTwoWay) {
////////////        this.id = id;
////////////        this.startIntersection = start;
////////////        this.endIntersection = end;
////////////        this.isTwoWay = isTwoWay;
////////////        this.forwardLanes = new ArrayList<>();
////////////        this.backwardLanes = new ArrayList<>();
////////////    }
////////////
////////////    @Override
////////////    public String getId() {
////////////        return id;
////////////    }
////////////
////////////    public Intersection getStartIntersection() {
////////////        return startIntersection;
////////////    }
////////////
////////////    public Intersection getEndIntersection() {
////////////        return endIntersection;
////////////    }
////////////
////////////    public List<Lane> getForwardLanes() {
////////////        return forwardLanes;
////////////    }
////////////
////////////    public List<Lane> getBackwardLanes() {
////////////        return backwardLanes;
////////////    }
////////////
////////////    public boolean isTwoWay() {
////////////        return isTwoWay;
////////////    }
////////////
////////////    public void addForwardLane(Lane lane) {
////////////        forwardLanes.add(lane);
////////////    }
////////////
////////////    public void addBackwardLane(Lane lane) {
////////////        if (isTwoWay) {
////////////            backwardLanes.add(lane);
////////////        }
////////////    }
////////////}
////////////
