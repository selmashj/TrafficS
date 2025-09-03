package infrastructure; // // پکیج زیرساخت

import core.Direction; // // جهت
import core.Identifiable; // // اینترفیس شناسه
import core.Point; // // نقطه
import ui.UIConstants; // // ثوابت UI
import java.awt.geom.Point2D; // // نقطه اعشاری

public class Lane implements Identifiable { // // لاین یک‌طرفه
    private final String id; // // شناسه
    private final Direction direction; // // جهت
    private final Road parentRoad; // // جادهٔ والد

    private Lane leftAdjacentLane; // // همسایه چپ
    private Lane rightAdjacentLane; // // همسایه راست
    private int offsetIndex = 0; // // ایندکس در گروه همان‌جهت

    public Lane(String id, Direction direction, Road parentRoad) { // // سازنده
        this.id = id; // // ست
        this.direction = direction; // // ست
        this.parentRoad = parentRoad; // // ست
    }

    @Override public String getId(){ return id; } // // گتر ID
    public Direction getDirection(){ return direction; } // // گتر جهت
    public Road getParentRoad(){ return parentRoad; } // // گتر جاده
    public Lane getLeftAdjacentLane(){ return leftAdjacentLane; } // // گتر چپ
    public Lane getRightAdjacentLane(){ return rightAdjacentLane; } // // گتر راست
    public void setLeftAdjacentLane(Lane l){ this.leftAdjacentLane = l; } // // ست چپ
    public void setRightAdjacentLane(Lane l){ this.rightAdjacentLane = l; } // // ست راست
    public void setOffsetIndex(int index){ this.offsetIndex = index; } // // ست ایندکس

    public double getLength(){ // // طول تقریبی مسیر مرکزی
        final int SAMPLES = 24; // // تعداد نمونه
        double length = 0.0; // // مجموع
        Point2D prev = parentRoad.curvePoint(0.0); // // نقطه اول
        for(int i=1;i<=SAMPLES;i++){ // // حلقه
            double t = (double)i / (double)SAMPLES; // // t
            Point2D p = parentRoad.curvePoint(t); // // نقطه
            length += prev.distance(p); // // جمع طول
            prev = p; // // به‌روزرسانی
        }
        return length; // // خروجی
    }

    private double sToT(double s){ // // نگاشت فاصله به پارامتر t
        final int SAMPLES = 60; // // نمونه زیاد
        double[] cum = new double[SAMPLES+1]; // // جدول تجمعی
        cum[0] = 0; // // اول صفر
        Point2D prev = parentRoad.curvePoint(0.0); // // نقطه 0
        for(int i=1;i<=SAMPLES;i++){ // // حلقه
            double t = (double)i / (double)SAMPLES; // // t
            Point2D p = parentRoad.curvePoint(t); // // نقطه
            cum[i] = cum[i-1] + prev.distance(p); // // تجمعی
            prev = p; // // آپدیت
        }
        double total = cum[SAMPLES]; // // کل طول
        if (s <= 0) return 0.0; // // کلیپ
        if (s >= total) return 1.0; // // کلیپ
        int lo = 0, hi = SAMPLES; // // مرزها
        while(hi - lo > 1){ // // باینری سرچ
            int mid = (lo + hi) >>> 1; // // وسط
            if (cum[mid] < s) lo = mid; else hi = mid; // // انتخاب نیمه
        }
        double seg = cum[hi] - cum[lo]; // // طول سگمنت
        double ratio = (seg<=1e-6)?0.0: (s - cum[lo]) / seg; // // نسبت
        return ((double)lo + ratio) / (double)SAMPLES; // // t
    }

    public Point getPositionAt(double positionInLane){ // // مرکز خودرو روی لاین
        double L = getLength(); // // طول
        if (L < 1e-6) { // // محافظت
            Point P = parentRoad.getStartIntersection().getPosition(); // // نقطه A
            return new Point(P.getX(), P.getY()); // // بازگشت
        }
        int sideSign = (direction==Direction.EAST || direction==Direction.SOUTH) ? (+1) : (-1); // // علامت سمت
        double s = positionInLane; // // فاصله روی مسیر
        if (sideSign < 0) s = L - s; // // معکوس برای جهت مخالف
        double t = sToT(s); // // نگاشت s→t
        Point2D center = parentRoad.curvePoint(t); // // نقطه مرکزی
        Point2D tan = parentRoad.curveTangent(t); // // بردار مماس
        double lenT = Math.hypot(tan.getX(), tan.getY()); // // طول مماس
        if (lenT < 1e-6) lenT = 1; // // جلوگیری از صفر
        double nx = -tan.getY() / lenT; // // نرمال x
        double ny =  tan.getX() / lenT; // // نرمال y

        int lanesF = parentRoad.getForwardLanes().size(); // // تعداد رفت
        int lanesB = parentRoad.getBackwardLanes().size(); // // تعداد برگشت
        int perSide = Math.max(lanesF, lanesB); // // بیشینه هر سمت
        double groupWidth = perSide * UIConstants.LANE_WIDTH
                + Math.max(0, perSide - 1) * UIConstants.LANE_GAP; // // پهنای گروه
        double sideCenter = UIConstants.LANE_GAP * 0.5 + groupWidth * 0.5; // // فاصله مرکز گروه
        double perLaneOffset = (UIConstants.LANE_WIDTH * 0.5) + (UIConstants.LANE_GAP * 0.5); // // آفست درون گروه

        double lateral = sideSign * sideCenter + (-offsetIndex * perLaneOffset * sideSign); // // آفست کل
        int x = (int)Math.round(center.getX() + nx * lateral); // // X
        int y = (int)Math.round(center.getY() + ny * lateral); // // Y
        return new Point(x, y); // // خروجی
    }

    public double getAngleRadians(){ // // زاویهٔ کلی لاین
        Point2D tan = parentRoad.curveTangent(0.5); // // مماس میان مسیر
        double angle = Math.atan2(tan.getY(), tan.getX()); // // زاویه A→B
        if (direction==Direction.WEST || direction==Direction.NORTH) angle += Math.PI; // // ۱۸۰ درجه برای معکوس
        return angle; // // خروجی
    }
}





























//package infrastructure; // // پکیج زیرساخت
//
//import core.Direction;         // // جهت
//import core.Identifiable;      // // برای ID
//import core.Point;             // // مختصات صحیح پروژه
//import ui.UIConstants;         // // ثوابت UI
//
//import java.awt.geom.Point2D;  // // برای محاسبات اعشاری
//
//public class Lane implements Identifiable { // // مسیر یک‌طرفه
//    private final String id;               // // شناسه
//    private final Direction direction;     // // جهت حرکت لِین
//    private final Road parentRoad;         // // جادهٔ والد
//
//    private Lane leftAdjacentLane;         // // همسایهٔ چپ
//    private Lane rightAdjacentLane;        // // همسایهٔ راست
//    private int offsetIndex = 0;           // // ایندکس درون گروه همان جهت
//
//    public Lane(String id, Direction direction, Road parentRoad) { // // سازنده
//        this.id = id; this.direction = direction; this.parentRoad = parentRoad; // // ست‌ها
//    }
//
//    @Override public String getId(){ return id; }       // // گتر ID
//    public Direction getDirection(){ return direction; } // // گتر جهت
//    public Road getParentRoad(){ return parentRoad; }    // // گتر جاده
//    public Lane getLeftAdjacentLane(){ return leftAdjacentLane; }   // // چپ
//    public Lane getRightAdjacentLane(){ return rightAdjacentLane; } // // راست
//    public void setLeftAdjacentLane(Lane l){ this.leftAdjacentLane = l; } // // ست چپ
//    public void setRightAdjacentLane(Lane l){ this.rightAdjacentLane = l; } // // ست راست
//    public void setOffsetIndex(int index){ this.offsetIndex = index; } // // ست ایندکس
//
//    // ====== طول مسیر مرکزی (تقریبی برای منحنی) ======
//    public double getLength(){ // // طول تقریبی (نمونه‌برداری)
//        final int SAMPLES = 24;                                   // // تعداد نمونه برای تقریب
//        double length = 0.0;                                      // // جمع طول
//        Point2D prev = parentRoad.curvePoint(0.0);                // // نقطهٔ t=0
//        for(int i=1;i<=SAMPLES;i++){                              // // حلقهٔ نمونه‌ها
//            double t = (double)i / (double)SAMPLES;               // // t
//            Point2D p = parentRoad.curvePoint(t);                 // // نقطه
//            double dx = p.getX() - prev.getX();                   // // Δx
//            double dy = p.getY() - prev.getY();                   // // Δy
//            length += Math.hypot(dx, dy);                         // // افزایش طول
//            prev = p;                                             // // به‌روزرسانی قبلی
//        }
//        return length;                                            // // خروجی
//    }
//
//    // نگاشت از «فاصله s روی لِین» به «t روی منحنی» //
//    private double sToT(double s){ // // تقریب معکوس با جدول تجمعی
//        final int SAMPLES = 60;                                 // // نمونهٔ زیادتر برای دقت
//        double[] cum = new double[SAMPLES+1];                   // // آرایهٔ طول تجمعی
//        cum[0] = 0;                                            // // شروع صفر
//        Point2D prev = parentRoad.curvePoint(0.0);              // // t=0
//        for(int i=1;i<=SAMPLES;i++){                            // // پرکردن جدول
//            double t = (double)i / (double)SAMPLES;             // // t
//            Point2D p = parentRoad.curvePoint(t);               // // نقطه
//            cum[i] = cum[i-1] + prev.distance(p);               // // طول تجمعی
//            prev = p;                                           // // قبلی
//        }
//        double total = cum[SAMPLES];                            // // کل طول
//        if (s <= 0) return 0.0; if (s >= total) return 1.0;     // // کلیپ
//        // باینری سرچ ساده //
//        int lo = 0, hi = SAMPLES;                               // // بازه
//        while(hi - lo > 1){                                     // // تا رسیدن به خانه نزدیک
//            int mid = (lo + hi) >>> 1;                          // // وسط
//            if (cum[mid] < s) lo = mid; else hi = mid;          // // انتخاب نیمه
//        }
//        // درون‌یابی خطی بین lo..hi //
//        double seg = cum[hi] - cum[lo];                         // // طول سگمنت
//        double ratio = (seg<=1e-6)?0.0: (s - cum[lo]) / seg;    // // نسبت
//        return ((double)lo + ratio) / (double)SAMPLES;          // // t تقریبی
//    }
//
//    public Point getPositionAt(double positionInLane){ // // مرکز خودرو روی لِین
//        double L = getLength();                                  // // طول مسیر
//        if (L < 1e-6) { Point P = parentRoad.getStartIntersection().getPosition(); return new Point(P.getX(), P.getY()); } // // محافظت
//
//        // جهت واقعی حرکت: EAST/SOUTH = +1 ، WEST/NORTH = -1 //
//        int sideSign = (direction==Direction.EAST || direction==Direction.SOUTH) ? (+1) : (-1); // // علامت
//
//        // s فیزیکی روی مسیر مرکزی //
//        double s = positionInLane;                                // // فاصلهٔ موردنظر
//        if (sideSign < 0) s = L - s;                              // // معکوس برای جهت برعکس
//
//        // نگاشت s→t //
//        double t = sToT(s);                                       // // t متناظر
//
//        // نقطه و مماس مسیر مرکزی در t //
//        Point2D center = parentRoad.curvePoint(t);                // // نقطهٔ مرکزی
//        Point2D tan    = parentRoad.curveTangent(t);              // // بردار مماس
//
//        // نرمالِ چپِ مسیر مرکزی (nx,ny) //
//        double lenT = Math.hypot(tan.getX(), tan.getY());         // // طول مماس
//        if (lenT < 1e-6) lenT = 1;                                // // جلوگیری از صفر
//        double nx = -tan.getY() / lenT;                           // // نرمال x
//        double ny =  tan.getX() / lenT;                           // // نرمال y
//
//        // محاسبهٔ آفست جانبی برای گروه جهت‌ها و جایگاه لِین در گروه //
//        int lanesF = parentRoad.getForwardLanes().size();         // // تعداد رفت
//        int lanesB = parentRoad.getBackwardLanes().size();        // // تعداد برگشت
//        int perSide = Math.max(lanesF, lanesB);                   // // بیشینهٔ هر سمت
//        double groupWidth = perSide * UIConstants.LANE_WIDTH
//                + Math.max(0, perSide - 1) * UIConstants.LANE_GAP; // // پهنای گروه
//        double sideCenter = UIConstants.LANE_GAP * 0.5 + groupWidth * 0.5; // // فاصلهٔ مرکز گروه از مرکز راه
//        double perLaneOffset = (UIConstants.LANE_WIDTH * 0.5) + (UIConstants.LANE_GAP * 0.5); // // آفست درون‌گروه
//
//        double lateral = sideSign * sideCenter + (-offsetIndex * perLaneOffset * sideSign); // // آفست کل جانبی
//
//        int x = (int)Math.round(center.getX() + nx * lateral);   // // X نهایی
//        int y = (int)Math.round(center.getY() + ny * lateral);   // // Y نهایی
//        return new Point(x, y);                                   // // خروجی
//    }
//
//    public double getAngleRadians(){ // // زاویهٔ رندر در موضع فعلی لِین
//        // از مشتق مسیر مرکزی استفاده می‌کنیم؛ جهت مخالف ۱۸۰ درجه می‌چرخد.
//        Point2D tan = parentRoad.curveTangent(0.5);               // // مماس تقریبی (میان مسیر)
//        double angle = Math.atan2(tan.getY(), tan.getX());        // // زاویهٔ A→B
//        if (direction==Direction.WEST || direction==Direction.NORTH) angle += Math.PI; // // چرخش برای جهت معکوس
//        return angle;                                             // // خروجی
//    }
//}
////
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
////9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
////package infrastructure; // // پکیج زیرساخت
////
////import core.Direction;   // // جهت حرکت لِین
////import core.Identifiable; // // برای شناسه
////import core.Point;       // // مختصات جهان
////import ui.UIConstants;   // // ثوابت UI
////
////public class Lane implements Identifiable { // // مسیر یک‌طرفه روی یک Road
////    private final String id;           // // شناسه یکتا
////    private final Direction direction; // // جهت لِین
////    private final Road parentRoad;     // // جادهٔ والد
////
////    private Lane leftAdjacentLane;     // // همسایهٔ چپ
////    private Lane rightAdjacentLane;    // // همسایهٔ راست
////
////    private int offsetIndex = 0;       // // ایندکس درون «گروهِ همان جهت» (-1 چپ، +1 راست)
////
////    public Lane(String id, Direction direction, Road parentRoad) { // // سازنده
////        this.id = id;                 // // ست ID
////        this.direction = direction;   // // ست جهت
////        this.parentRoad = parentRoad; // // ست جاده
////    }
////
////    @Override
////    public String getId() { // // گتر ID
////        return id;
////    }
////
////    public Direction getDirection() { // // گتر جهت
////        return direction;
////    }
////
////    public Road getParentRoad() { // // گتر جادهٔ والد
////        return parentRoad;
////    }
////
////    public Lane getLeftAdjacentLane() { return leftAdjacentLane; }   // // همسایهٔ چپ
////    public Lane getRightAdjacentLane(){ return rightAdjacentLane; }  // // همسایهٔ راست
////    public void setLeftAdjacentLane(Lane l) { this.leftAdjacentLane = l; }   // // ست چپ
////    public void setRightAdjacentLane(Lane l){ this.rightAdjacentLane = l; }  // // ست راست
////    public void setOffsetIndex(int index){ this.offsetIndex = index; }       // // ست ایندکس
////
////    // --------------------- هندسه: طول/مکان/زاویه ---------------------
////
////    public double getLength() { // // طول لِین
////        Point A = parentRoad.getStartIntersection().getPosition(); // // نقطهٔ شروع
////        Point B = parentRoad.getEndIntersection().getPosition();   // // نقطهٔ پایان
////        double dx = B.getX() - A.getX(); // // Δx
////        double dy = B.getY() - A.getY(); // // Δy
////        return Math.sqrt(dx*dx + dy*dy); // // طول
////    }
////
////    public Point getPositionAt(double positionInLane) {
////        // // مختصات «مرکز خودرو» روی این لِین با احتساب جداسازی جهت‌ها و چندلاینه
////
////        Point A = parentRoad.getStartIntersection().getPosition(); // // A
////        Point B = parentRoad.getEndIntersection().getPosition();   // // B
////        double dx = B.getX() - A.getX(); // // Δx
////        double dy = B.getY() - A.getY(); // // Δy
////        double len = Math.sqrt(dx*dx + dy*dy); // // طول
////
////        if (len < 1e-6) return new Point(A.getX(), A.getY()); // // محافظت از تقسیم بر صفر
////
////        // واحد جهت مسیرِ Road (A→B) //
////        double ux = dx / len; // // بردار واحد طولی x
////        double uy = dy / len; // // بردار واحد طولی y
////
////        // بردار عمودِ «چپِ A→B» (برای جابه‌جایی جانبی) //
////        double nx = -uy; // // نرمال x
////        double ny =  ux; // // نرمال y
////
////        // نسبت پیشروی روی راه (0..1) //
////        double t = positionInLane / len; // // t
////        // برای پایداری، کلیپ //
////        if (t < 0) t = 0; if (t > 1) t = 1; // // کلیپ
////
////        // نقطهٔ مرکزی روی خطِ مرکزی راه //
////        double cx = A.getX() + ux * (t * len); // // X مرکزی
////        double cy = A.getY() + uy * (t * len); // // Y مرکزی
////
////        // --- محاسبهٔ عرض بصری راه بر اساس تعداد لِین‌ها ---
////        int lanesF = parentRoad.getForwardLanes().size(); // // تعداد لِین‌های جهت رفت
////        int lanesB = parentRoad.getBackwardLanes().size();// // تعداد لِین‌های جهت برگشت
////        int perSide = Math.max(lanesF, lanesB);           // // حداکثر لِین در هر سمت
////
////        // پهنای گروه یک سمت (فقط همان جهت) //
////        double groupWidth = perSide * UIConstants.LANE_WIDTH
////                + Math.max(0, perSide - 1) * UIConstants.LANE_GAP; // // پهنا گروه
////
////        // فاصلهٔ مرکز هر گروه از خط وسط راه (نیمِ شکاف وسط + نیمِ پهنای گروه) //
////        double sideCenter = UIConstants.LANE_GAP * 0.5 + groupWidth * 0.5; // // مرکز گروه
////
////        // جابه‌جایی درون گروه برای این لِین (نصف پهنای یک لِین + نصف فاصلهٔ بین‌لاین) //
////        double perLaneOffset = (UIConstants.LANE_WIDTH * 0.5) + (UIConstants.LANE_GAP * 0.5); // // آفست درون‌گروه
////
////        // علامت سمت: +1 برای جهت «رو به جلو» (E/S) ، -1 برای جهت «برگشتی» (W/N) //
////        int sideSign = (direction == Direction.EAST || direction == Direction.SOUTH) ? (+1) : (-1); // // علامت سمت
////
////        // آفست کل جانبی: مرکزِ گروه ± آفست درون‌گروه با توجه به چپ/راستِ همان جهت //
////        double lateral = sideSign * sideCenter                             // // انتقال گروه از مرکز راه
////                + (-offsetIndex * perLaneOffset * sideSign);        // // جابه‌جایی درون گروه (چپ/راست)
////
////        // مختصات نهایی با انتقال روی نرمال //
////        int x = (int)Math.round(cx + nx * lateral); // // X نهایی
////        int y = (int)Math.round(cy + ny * lateral); // // Y نهایی
////        return new Point(x, y); // // خروجی
////    }
////
////    public double getAngleRadians() { // // زاویهٔ A→B برای رندر
////        Point A = parentRoad.getStartIntersection().getPosition(); // // A
////        Point B = parentRoad.getEndIntersection().getPosition();   // // B
////        double dx = B.getX() - A.getX(); // // Δx
////        double dy = B.getY() - A.getY(); // // Δy
////        return Math.atan2(dy, dx); // // زاویه
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
//////777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777
//////
//////package infrastructure; // // پکیج زیرساخت
//////
//////import core.Direction;   // // جهت حرکت لاین (N/E/S/W)
//////import core.Identifiable; // // برای داشتن شناسه یکتا
//////import core.Point;       // // نوع نقطه (مختصات جهان) — توجه: این core.Point است، نه java.awt.Point
//////
//////public class Lane implements Identifiable { // // مسیر یک‌طرفه درون یک Road
//////    private final String id;           // // شناسه یکتا برای لاین
//////    private final Direction direction; // // جهت حرکت این لاین
//////    private final Road parentRoad;     // // جادهٔ والد که این لاین روی آن قرار دارد
//////
//////    private Lane leftAdjacentLane;     // // لاین کناری در سمت چپ (اگر وجود داشته باشد)
//////    private Lane rightAdjacentLane;    // // لاین کناری در سمت راست (اگر وجود داشته باشد)
//////
//////    // --- آفست جانبی برای جدا کردن مسیر هندسی هر لاین از خط مرکزی راه ---
//////    // مقدار -1 یعنی کمی به چپ مسیر اصلی، +1 یعنی کمی به راست مسیر اصلی (نسبت به جهت همین لاین)
//////    private int offsetIndex = 0;       // // پیش‌فرض 0 (روی خط مرکزی راه)
//////
//////    public Lane(String id, Direction direction, Road parentRoad) { // // سازندهٔ لاین
//////        this.id = id;                 // // ذخیره شناسه
//////        this.direction = direction;   // // ذخیره جهت
//////        this.parentRoad = parentRoad; // // ذخیره جاده والد
//////        this.leftAdjacentLane = null; // // در ابتدا همسایه ندارد
//////        this.rightAdjacentLane = null;// // در ابتدا همسایه ندارد
//////    }
//////
//////    @Override
//////    public String getId() { // // گتر شناسه
//////        return id; // // برگرداندن ID
//////    }
//////
//////    public Direction getDirection() { // // گتر جهت
//////        return direction; // // برگرداندن جهت
//////    }
//////
//////    public Road getParentRoad() { // // گتر جادهٔ والد
//////        return parentRoad; // // برگرداندن Road
//////    }
//////
//////    // --------------------- همسایه‌های کناری (برای تغییر لاین/سبقت) ---------------------
//////
//////    public Lane getLeftAdjacentLane() { // // لاین سمت چپ
//////        return leftAdjacentLane; // // ممکن است null باشد
//////    }
//////
//////    public Lane getRightAdjacentLane() { // // لاین سمت راست
//////        return rightAdjacentLane; // // ممکن است null باشد
//////    }
//////
//////    public void setLeftAdjacentLane(Lane lane) { // // ست کردن همسایهٔ چپ
//////        this.leftAdjacentLane = lane; // // ذخیرهٔ ارجاع
//////    }
//////
//////    public void setRightAdjacentLane(Lane lane) { // // ست کردن همسایهٔ راست
//////        this.rightAdjacentLane = lane; // // ذخیرهٔ ارجاع
//////    }
//////
//////    // --------------------- آفست جانبی (برای عریض کردن بصری خیابان) ---------------------
//////
//////    public void setOffsetIndex(int index) { // // تعیین ایندکس آفست جانبی این لاین
//////        this.offsetIndex = index; // // -1 = چپ، +1 = راست (نسبت به جهت همین لاین)
//////    }
//////
//////    // --------------------- هندسهٔ لاین: طول، موقعیت، زاویه ---------------------
//////
//////    public double getLength() { // // طول هندسی لاین (فاصلهٔ دو تقاطع Road)
//////        Point A = parentRoad.getStartIntersection().getPosition(); // // مختصات تقاطع شروع
//////        Point B = parentRoad.getEndIntersection().getPosition();   // // مختصات تقاطع پایان
//////        double dx = B.getX() - A.getX(); // // اختلاف X
//////        double dy = B.getY() - A.getY(); // // اختلاف Y
//////        return Math.sqrt(dx * dx + dy * dy); // // طول با قضیهٔ فیثاغورس
//////    }
//////
//////    public Point getPositionAt(double positionInLane) {
//////        // // خروجی: «مختصات جهان» نقطه‌ای روی این لاین، با احتساب «آفست جانبی»
//////        // // ورودی positionInLane فاصلهٔ طی‌شده از ابتدای لاین است (0..طول لاین)
//////
//////        Point A = parentRoad.getStartIntersection().getPosition(); // // نقطهٔ شروع راه
//////        Point B = parentRoad.getEndIntersection().getPosition();   // // نقطهٔ پایان راه
//////
//////        double dx = B.getX() - A.getX(); // // مؤلفهٔ X بردار راه
//////        double dy = B.getY() - A.getY(); // // مؤلفهٔ Y بردار راه
//////        double len = Math.sqrt(dx * dx + dy * dy); // // طول راه
//////
//////        if (len <= 0.0001) { // // اگر طول تقریباً صفر بود (ایراد داده)
//////            return new Point(A.getX(), A.getY()); // // همان نقطهٔ شروع را برگردان
//////        }
//////
//////        // t = نسبت پیشروی روی خط مرکزی راه (0..1) //
//////        double t = positionInLane / len; // // تبدیل فاصله به نسبت
//////        if (t < 0) t = 0; // // کلیپ پایین
//////        if (t > 1) t = 1; // // کلیپ بالا
//////
//////        // مختصات نقطهٔ روی خط مرکزی (بدون آفست جانبی) //
//////        double cx = A.getX() + dx * t; // // X مرکزی
//////        double cy = A.getY() + dy * t; // // Y مرکزی
//////
//////        // بردار عمود واحد روی مسیر: برای (dx,dy)، عمود = (-dy, +dx) / len //
//////        double nx = -dy / len; // // X عمود واحد
//////        double ny =  dx / len; // // Y عمود واحد
//////
//////        // فاصلهٔ جانبی هر لاین نسبت به خط مرکزی:
//////        // perLaneOffset = نصف پهنای یک لاین + نصف فاصلهٔ میان لاین‌ها
//////        double halfLane = ui.UIConstants.LANE_WIDTH * 0.5; // // نصف پهنای یک لاین (از UIConstants)
//////        double halfGap  = ui.UIConstants.LANE_GAP  * 0.5;  // // نصف فاصلهٔ بین لاین‌ها
//////        double perLaneOffset = halfLane + halfGap; // // مجموع آفست جانبی برای هر لاین از مرکز
//////
//////        // آفست نهایی این لاین (بر حسب ایندکس سمت چپ/راست)
//////        double lateral = offsetIndex * perLaneOffset; // // -1=چپ، +1=راست
//////
//////        // انتقال نقطهٔ مرکزی به سمت عمود (چپ/راست) //
//////        int x = (int) Math.round(cx + nx * lateral); // // X نهایی
//////        int y = (int) Math.round(cy + ny * lateral); // // Y نهایی
//////
//////        return new Point(x, y); // // مختصات جهانِ نهاییِ این لاین
//////    }
//////
//////    public double getAngleRadians() { // // زاویهٔ حرکت نسبت به محور X (برای rotate تصویر خودرو)
//////        Point A = parentRoad.getStartIntersection().getPosition(); // // شروع
//////        Point B = parentRoad.getEndIntersection().getPosition();   // // پایان
//////        double dx = B.getX() - A.getX(); // // ΔX
//////        double dy = B.getY() - A.getY(); // // ΔY
//////        return Math.atan2(dy, dx); // // زاویه بر حسب رادیان
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
////////// infrastructure/Lane.java
////////package infrastructure;
////////
////////import java.util.*;
////////import core.*;
//////
////////public class Lane implements Identifiable {
////////    private String id;
////////    private Direction direction;
////////    private Road parentRoad;
////////    private int offsetIndex;
////////    private static final int LANE_CENTER_OFFSET_PX = 14;
////////
////////    public Lane(String id, Direction dir, Road road, int offset){
////////        this.id=id; this.direction=dir; this.parentRoad=road; this.offsetIndex=offset;
////////    }
////////
////////    @Override public String getId(){ return id; }
////////    public Direction getDirection(){ return direction; }
////////    public Road getParentRoad(){ return parentRoad; }
////////    public int getOffsetIndex(){ return offsetIndex; }
////////    public void setOffsetIndex(int idx){ this.offsetIndex=idx; }
////////
////////    public double getLength(){ return parentRoad!=null ? parentRoad.getLengthMeters() : 0; }
////////
////////    public double getAngleRadians(){
////////        if(parentRoad==null) return 0;
////////        Point a = parentRoad.getStartIntersection().getPosition();
////////        Point b = parentRoad.getEndIntersection().getPosition();
////////        double dx=b.getX()-a.getX(), dy=b.getY()-a.getY();
////////        return Math.atan2(dy,dx);
////////    }
////////
////////    public Point getPositionAt(double s){
////////        if(parentRoad==null) return new Point(0,0);
////////        Point a=parentRoad.getStartIntersection().getPosition();
////////        Point b=parentRoad.getEndIntersection().getPosition();
////////        int dx=b.getX()-a.getX(), dy=b.getY()-a.getY();
////////        double len=Math.sqrt(dx*dx+dy*dy); if(len<1e-6) return a;
////////        double t=s/len; if(t<0) t=0; if(t>1) t=1;
////////        double cx=a.getX()+t*dx, cy=a.getY()+t*dy;
////////        double nx=-dy/len, ny=dx/len;
////////        double off=offsetIndex*LANE_CENTER_OFFSET_PX;
////////        return new Point((int)Math.round(cx+nx*off),(int)Math.round(cy+ny*off));
////////    }
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
//////
////////package infrastructure;
////////
////////import core.Identifiable;
////////import core.Direction;
////////import core.Point;
////////import core.Vehicle;
////////
////////import java.util.ArrayList;
////////import java.util.List;
////////
////////public class Lane implements Identifiable {
////////    private final String id;
////////    private final Direction direction;
////////    private final List<Vehicle> vehicles = new ArrayList<>();
////////    private Road parentRoad;
////////    private final List<SpeedBump> speedBumps = new ArrayList<>();
////////    private int offsetIndex = 0; // برای چند لاینه‌ها
////////
////////    private static final int LANE_CENTER_OFFSET_PX = 14;
////////
////////    public Lane(String id, Direction direction, Road parentRoad) {
////////        this.id = id;
////////        this.direction = direction;
////////        this.parentRoad = parentRoad;
////////    }
////////
////////    @Override public String getId() { return id; }
////////    public Direction getDirection() { return direction; }
////////    public Road getParentRoad() { return parentRoad; }
////////    public void setParentRoad(Road r) { this.parentRoad = r; }
////////
////////    public void setOffsetIndex(int idx) { this.offsetIndex = idx; }
////////    public int getOffsetIndex() { return offsetIndex; }
////////
////////    public List<Vehicle> getVehicles() { return vehicles; }
////////    public List<SpeedBump> getSpeedBumps() { return speedBumps; }
////////
////////    /** طول لِین (برابر طول جاده) */
////////    public double getLength() { return parentRoad != null ? parentRoad.getGeometricLength() : 0.0; }
////////
////////    /** زاویه رندر نسبت به محور x (رادیان) */
////////    public double getAngleRadians() {
////////        if (parentRoad == null) return 0.0;
////////        Point a = parentRoad.getStartIntersection().getPosition();
////////        Point b = parentRoad.getEndIntersection().getPosition();
////////        double dx = b.getX() - a.getX();
////////        double dy = b.getY() - a.getY();
////////        double ang = Math.atan2(dy, dx);
////////        // هم‌جهت‌سازی با حرکت لِین
////////        switch (direction) {
////////            case WEST: case NORTH: ang += Math.PI; break;
////////        }
////////        return ang;
////////    }
////////    // برای سازگاری با کدهایی که getAngle می‌خوانند
////////    public double getAngle() { return getAngleRadians(); }
////////
////////    /** نقطه‌ی روی لِین با درنظرگرفتن آفست مرکز لِین‌ها */
////////    public Point getPositionAt(double s) {
////////        if (parentRoad == null) return new Point(0, 0);
////////        Point a = parentRoad.getStartIntersection().getPosition();
////////        Point b = parentRoad.getEndIntersection().getPosition();
////////
////////        int dx = b.getX() - a.getX();
////////        int dy = b.getY() - a.getY();
////////        double len = Math.sqrt(dx * dx + dy * dy);
////////        if (len < 1e-6) return new Point(a.getX(), a.getY());
////////
////////        double t = Math.max(0.0, Math.min(1.0, s / len));
////////        double cx = a.getX() + t * dx;
////////        double cy = a.getY() + t * dy;
////////
////////        double nx = -dy / len;
////////        double ny =  dx / len;
////////        double off = offsetIndex * LANE_CENTER_OFFSET_PX;
////////
////////        int x = (int)Math.round(cx + nx * off);
////////        int y = (int)Math.round(cy + ny * off);
////////        return new Point(x, y);
////////    }
////////
////////    // لِین مجاور چپ/راست نسبت به جهت حرکت — در صورت نیاز بعداً کامل‌تر می‌کنیم
////////    public Lane getLeftAdjacentLane()  { return null; }
////////    public Lane getRightAdjacentLane() { return null; }
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
////////
////////package infrastructure;
////////
////////import core.Direction;
////////import core.Identifiable;
////////import core.Point;
////////import core.Vehicle;
////////
////////import java.util.ArrayList;
////////import java.util.List;
////////
////////public class Lane implements Identifiable {
////////    private final String id;
////////    private final Direction direction;
////////    private final List<Vehicle> vehicles = new ArrayList<>();
////////    private Road parentRoad;
////////    private final List<SpeedBump> speedBumps = new ArrayList<>();
////////    private int offsetIndex = 0; // برای فاصله‌ی جانبی لاین‌ها
////////
////////    private static final int LANE_CENTER_OFFSET_PX = 14;
////////
////////    public Lane(String id, Direction dir, Road parent) {
////////        this.id = id;
////////        this.direction = dir;
////////        this.parentRoad = parent;
////////    }
////////
////////    @Override public String getId() { return id; }
////////    public Direction getDirection()  { return direction; }
////////    public Road getParentRoad()      { return parentRoad; }
////////    public void setParentRoad(Road r){ this.parentRoad = r; }
////////
////////    public void setOffsetIndex(int idx) { this.offsetIndex = idx; }
////////    public int  getOffsetIndex()        { return offsetIndex; }
////////
////////    public List<Vehicle> getVehicles() { return vehicles; }
////////    public void addSpeedBump(SpeedBump sb){ if (sb!=null) speedBumps.add(sb); }
////////    public List<SpeedBump> getSpeedBumps(){ return speedBumps; }
////////
////////    /** طول لاین = طول هندسی جاده */
////////    public double getLength() {
////////        return (parentRoad == null) ? 0.0 : parentRoad.getGeometricLength();
////////    }
////////
////////    /** زاویه‌ی لاین نسبت به محور x (برای رندر) */
////////    public double getAngleRadians() {
////////        if (parentRoad == null) return 0.0;
////////        Point a = parentRoad.getStart().getPosition();
////////        Point b = parentRoad.getEnd().getPosition();
////////        double dx = b.getX() - a.getX();
////////        double dy = b.getY() - a.getY();
////////        double ang = Math.atan2(dy, dx);
////////        // اگر جهت حرکتی مخالف بردار هندسی است، 180 درجه جابه‌جا
////////        if (direction == Direction.WEST || direction == Direction.NORTH) ang += Math.PI;
////////        return ang;
////////    }
////////    // آلیاس برای جاهایی که getAngle() صدا می‌زنند
////////    public double getAngle() { return getAngleRadians(); }
////////
////////    /** نقطه‌ی “مرکز لاین” در فاصله‌ی s (۰..طول) با اعمال offset جانبی. */
////////    public Point getPositionAt(double s) {
////////        if (parentRoad == null) return new Point(0,0);
////////        Point a = parentRoad.getStart().getPosition();
////////        Point b = parentRoad.getEnd().getPosition();
////////        int dx = b.getX() - a.getX();
////////        int dy = b.getY() - a.getY();
////////        double len = Math.sqrt((double)dx*dx + (double)dy*dy);
////////        if (len <= 1e-9) return new Point(a.getX(), a.getY());
////////
////////        double t = Math.max(0.0, Math.min(1.0, s / len));
////////        double cx = a.getX() + t * dx;
////////        double cy = a.getY() + t * dy;
////////
////////        double nx = -dy / len;
////////        double ny =  dx / len;
////////        double off = offsetIndex * LANE_CENTER_OFFSET_PX;
////////
////////        int x = (int)Math.round(cx + nx * off);
////////        int y = (int)Math.round(cy + ny * off);
////////        return new Point(x, y);
////////    }
////////
////////    // لاین کناری (در صورت نیاز بعداً کامل می‌کنیم)
////////    public Lane getLeftAdjacentLane()  { return null; }
////////    public Lane getRightAdjacentLane() { return null; }
////////}
////////
////////
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
////////
/////////**
//////// * Lane with side-offset rendering helpers and adjacent-lane queries.
//////// * شامل alias ها برای جلوگیری از ارورهای «cannot find symbol».
//////// */
////////public class Lane implements Identifiable {
////////    private final String id;
////////    private final Direction direction;
////////    private final List<core.Vehicle> vehicles = new ArrayList<>();
////////    private final List<SpeedBump> speedBumps = new ArrayList<>();
////////    private Road parentRoad;
////////
////////    // برای رندر، فاصلهٔ مرکزی هر لاین از محور جاده
////////    private int offsetIndex = 0;
////////    private static final int LANE_CENTER_OFFSET_PX = 14;
////////
////////    public Lane(String id, Direction direction, Road parentRoad) {
////////        this.id = id;
////////        this.direction = direction;
////////        this.parentRoad = parentRoad;
////////    }
////////
////////    @Override public String getId(){ return id; }
////////    public Direction getDirection(){ return direction; }
////////    public Road getParentRoad(){ return parentRoad; }
////////    public void setParentRoad(Road r){ this.parentRoad = r; }
////////
////////    public void setOffsetIndex(int idx){ this.offsetIndex = idx; }
////////    public int  getOffsetIndex(){ return offsetIndex; }
////////
////////    public List<core.Vehicle> getVehicles(){ return vehicles; }
////////    public void addSpeedBump(SpeedBump sb){ if(sb!=null) speedBumps.add(sb); }
////////    public List<SpeedBump> getSpeedBumps(){ return speedBumps; }
////////
////////    /** طول لاین = طول جادهٔ والد. (سازگار با کدهای قبلی) */
////////    public double getLength(){
////////        return (parentRoad == null) ? 0.0 : parentRoad.getGeometricLength();
////////    }
////////
////////    // ---------- Adjacent lanes helpers ----------
////////    public Lane getLeftAdjacentLane(){
////////        if(parentRoad == null) return null;
////////        List<Lane> same = (direction==Direction.EAST || direction==Direction.SOUTH)
////////                ? parentRoad.getForwardLanes() : parentRoad.getBackwardLanes();
////////        int target = offsetIndex - ((direction==Direction.EAST || direction==Direction.SOUTH)?1:-1);
////////        return findByOffset(same, target);
////////    }
////////
////////    public Lane getRightAdjacentLane(){
////////        if(parentRoad == null) return null;
////////        List<Lane> same = (direction==Direction.EAST || direction==Direction.SOUTH)
////////                ? parentRoad.getForwardLanes() : parentRoad.getBackwardLanes();
////////        int target = offsetIndex + ((direction==Direction.EAST || direction==Direction.SOUTH)?1:-1);
////////        return findByOffset(same, target);
////////    }
////////
////////    private Lane findByOffset(List<Lane> lanes, int offset){
////////        if(lanes==null) return null;
////////        for(Lane ln: lanes){ if(ln!=null && ln.getOffsetIndex()==offset) return ln; }
////////        return null;
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
//////////
//////////package infrastructure;
//////////
//////////import java.util.ArrayList;
//////////import java.util.List;
//////////
//////////import core.*;
//////////
///////////**
////////// * لِین با آفست جانبی نسبت به محور جاده.
////////// */
//////////public class Lane implements Identifiable {
//////////    private final String id;
//////////    private final Direction direction;
//////////    private final List<Vehicle> vehicles = new ArrayList<>();
//////////    private final Road parentRoad;
//////////    private final List<SpeedBump> speedBumps = new ArrayList<>();
//////////    private int offsetIndex = 0; // 0 مرکز محور؛ ±1، ±2 ... لِین‌های کنار
//////////
//////////    private static final int LANE_CENTER_OFFSET_PX = 14;
//////////
//////////    public Lane(String id, Direction direction, Road parentRoad) {
//////////        this.id = id;
//////////        this.direction = direction;
//////////        this.parentRoad = parentRoad;
//////////    }
//////////
//////////    @Override public String getId() { return id; }
//////////    public Direction getDirection() { return direction; }
//////////    public Road getParentRoad() { return parentRoad; }
//////////
//////////    public List<Vehicle> getVehicles() { return vehicles; }
//////////
//////////    public void addSpeedBump(SpeedBump sb) { if (sb != null) speedBumps.add(sb); }
//////////    public List<SpeedBump> getSpeedBumps() { return speedBumps; }
//////////
//////////    public void setOffsetIndex(int idx) { this.offsetIndex = idx; }
//////////    public int getOffsetIndex() { return offsetIndex; }
//////////
//////////    /** طول لِین = طول هندسی جاده‌ی والد. */
//////////    public double getLength() {
//////////        return parentRoad != null ? parentRoad.getGeometricLength() : 0.0;
//////////    }
//////////
//////////    /** زاویه‌ی رندر بر حسب رادیان. */
//////////    public double getAngleRadians() {
//////////        if (parentRoad == null) return 0.0;
//////////        Point a = parentRoad.getStartIntersection().getPosition();
//////////        Point b = parentRoad.getEndIntersection().getPosition();
//////////        double dx = (double)(b.getX() - a.getX());
//////////        double dy = (double)(b.getY() - a.getY());
//////////        double ang = Math.atan2(dy, dx);
//////////        if (direction == Direction.WEST || direction == Direction.NORTH) ang += Math.PI;
//////////        return ang;
//////////    }
//////////
//////////    /** نقطه‌ی روی لِین با درنظر گرفتن آفست جانبی. s به پیکسل. */
//////////    public Point getPositionAt(double s) {
//////////        if (parentRoad == null) return new Point(0, 0);
//////////        Point a = parentRoad.getStartIntersection().getPosition();
//////////        Point b = parentRoad.getEndIntersection().getPosition();
//////////
//////////        int dx = b.getX() - a.getX();
//////////        int dy = b.getY() - a.getY();
//////////        double len = Math.sqrt((double)dx * dx + (double)dy * dy);
//////////        if (len <= 1e-9) return new Point(a.getX(), a.getY());
//////////
//////////        double t = s / len;
//////////        if (t < 0.0) t = 0.0;
//////////        if (t > 1.0) t = 1.0;
//////////
//////////        double cx = a.getX() + t * dx;
//////////        double cy = a.getY() + t * dy;
//////////
//////////        // نرمال واحد برای آفست جانبی
//////////        double nx = -dy / len;
//////////        double ny =  dx / len;
//////////        double off = this.offsetIndex * LANE_CENTER_OFFSET_PX;
//////////
//////////        int x = (int)Math.round(cx + nx * off);
//////////        int y = (int)Math.round(cy + ny * off);
//////////        return new Point(x, y);
//////////    }
//////////
//////////    // ========= لِین‌های چپ/راست نسبت به جهت حرکت =========
//////////
//////////    public Lane getLeftAdjacentLane() {
//////////        if (parentRoad == null) return null;
//////////        int target = (direction == Direction.EAST || direction == Direction.SOUTH)
//////////                ? offsetIndex - 1 : offsetIndex + 1;
//////////        List<Lane> same = (direction == Direction.EAST || direction == Direction.SOUTH)
//////////                ? parentRoad.getForwardLanes() : parentRoad.getBackwardLanes();
//////////        return findByOffset(same, target);
//////////    }
//////////
//////////    public Lane getRightAdjacentLane() {
//////////        if (parentRoad == null) return null;
//////////        int target = (direction == Direction.EAST || direction == Direction.SOUTH)
//////////                ? offsetIndex + 1 : offsetIndex - 1;
//////////        List<Lane> same = (direction == Direction.EAST || direction == Direction.SOUTH)
//////////                ? parentRoad.getForwardLanes() : parentRoad.getBackwardLanes();
//////////        return findByOffset(same, target);
//////////    }
//////////
//////////    private Lane findByOffset(List<Lane> lanes, int offset) {
//////////        if (lanes == null) return null;
//////////        for (Lane ln : lanes) {
//////////            if (ln != null && ln.getOffsetIndex() == offset) return ln;
//////////        }
//////////        return null;
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
////////////import java.util.ArrayList;
////////////import java.util.List;
////////////
////////////import core.Direction;
////////////import core.Identifiable;
////////////import core.Point;
////////////import core.Vehicle;
////////////
////////////public class Lane implements Identifiable {
////////////
////////////    // ---------------- fields ----------------
////////////    private final String id;
////////////    private final Direction direction;
////////////    private final List<Vehicle> vehicles = new ArrayList<>();
////////////    private Road parentRoad;
////////////
////////////    private final List<SpeedBump> speedBumps = new ArrayList<>();
////////////
////////////    /** اندیس آفست جانبی نسبت به خط میانی جاده: 0، ±1، ±2، ... */
////////////    private int offsetIndex = 0;
////////////
////////////    /** فاصله‌ی مرکز هر لاین از خط میانی (پیکسل رندر) */
////////////    private static final int LANE_CENTER_OFFSET_PX = 14;
////////////
////////////    // ---------------- ctor ----------------
////////////    public Lane(String id, Direction direction, Road parent) {
////////////        this.id = id;
////////////        this.direction = direction;
////////////        this.parentRoad = parent;
////////////    }
////////////
////////////    // ---------------- identity ----------------
////////////    @Override public String getId() { return id; }
////////////
////////////    // ---------------- basic getters/setters ----------------
////////////    public Direction getDirection() { return direction; }
////////////
////////////    public Road getParentRoad() { return parentRoad; }
////////////    public void setParentRoad(Road parent) { this.parentRoad = parent; }
////////////
////////////    public List<Vehicle> getVehicles() { return vehicles; }
////////////
////////////    public void addSpeedBump(SpeedBump sb) { if (sb != null) speedBumps.add(sb); }
////////////    public List<SpeedBump> getSpeedBumps() { return speedBumps; }
////////////
////////////    public void setOffsetIndex(int idx) { this.offsetIndex = idx; }
////////////    public int getOffsetIndex() { return offsetIndex; }
////////////
////////////    // ---------------- geometry helpers ----------------
////////////    /** طول لاین (متر) — معادل طول جاده‌ی والد. */
////////////    public double getLength() {
////////////        if (parentRoad == null) return 0.0;
////////////        // دقت کن: این باید با امضای واقعی Road هم‌خوان باشد.
////////////        // اگر در Road متد دیگری داری، همین‌جا جایگزینش کن.
////////////        return parentRoad.getLengthMeters();
////////////    }
////////////
////////////    /** زاویه‌ی حرکت (رادیان) برای رندر نسبی به جهت حرکت. */
////////////    public double getAngleRadians() {
////////////        if (parentRoad == null) return 0.0;
////////////        Point a = parentRoad.getStartIntersection().getPosition();
////////////        Point b = parentRoad.getEndIntersection().getPosition();
////////////        double dx = b.getX() - a.getX();
////////////        double dy = b.getY() - a.getY();
////////////        double ang = Math.atan2(dy, dx);
////////////        // اگر لاین به سمت غرب/شمال حرکت می‌کند، بردار را برعکس کن
////////////        if (direction == Direction.WEST || direction == Direction.NORTH) {
////////////            ang += Math.PI;
////////////        }
////////////        return ang;
////////////    }
////////////
////////////    /**
////////////     * مختصات نقطه‌ای روی لاین برای فاصله‌ی s (پیکسل روی نقشه‌ی رندر)
////////////     * با اعمال آفست جانبی لاین نسبت به خط میانی جاده.
////////////     */
////////////    public Point getPositionAt(double s) {
////////////        if (parentRoad == null) return new Point(0, 0);
////////////        Point a = parentRoad.getStartIntersection().getPosition();
////////////        Point b = parentRoad.getEndIntersection().getPosition();
////////////
////////////        int dx = b.getX() - a.getX();
////////////        int dy = b.getY() - a.getY();
////////////        double len = Math.sqrt((double) dx * dx + (double) dy * dy);
////////////        if (len <= 1e-9) return new Point(a.getX(), a.getY());
////////////
////////////        // نسبت 0..1 روی پاره‌خط
////////////        double t = s / len;
////////////        if (t < 0) t = 0;
////////////        if (t > 1) t = 1;
////////////
////////////        double cx = a.getX() + t * dx;
////////////        double cy = a.getY() + t * dy;
////////////
////////////        // نرمال واحد (عمود بر بردار جاده) برای آفست جانبی لاین
////////////        double nx = -dy / len;
////////////        double ny =  dx / len;
////////////        double off = offsetIndex * LANE_CENTER_OFFSET_PX;
////////////
////////////        int x = (int) Math.round(cx + nx * off);
////////////        int y = (int) Math.round(cy + ny * off);
////////////        return new Point(x, y);
////////////    }
////////////
////////////    // ---------------- adjacent lanes (for lane change / overtake) ----------------
////////////    public Lane getLeftAdjacentLane() {
////////////        if (parentRoad == null) return null;
////////////
////////////        // برای EAST/SOUTH: چپ یعنی آفست کوچک‌تر (منفی‌تر)
////////////        // برای WEST/NORTH: چپ یعنی آفست بزرگ‌تر (مثبت‌تر)
////////////        if (direction == Direction.EAST || direction == Direction.SOUTH) {
////////////            return findByOffset(parentRoad.getForwardLanes(), offsetIndex - 1);
////////////        } else {
////////////            return findByOffset(parentRoad.getBackwardLanes(), offsetIndex + 1);
////////////        }
////////////    }
////////////
////////////    public Lane getRightAdjacentLane() {
////////////        if (parentRoad == null) return null;
////////////
////////////        if (direction == Direction.EAST || direction == Direction.SOUTH) {
////////////            return findByOffset(parentRoad.getForwardLanes(), offsetIndex + 1);
////////////        } else {
////////////            return findByOffset(parentRoad.getBackwardLanes(), offsetIndex - 1);
////////////        }
////////////    }
////////////
////////////    private Lane findByOffset(List<Lane> lanes, int targetOffset) {
////////////        if (lanes == null) return null;
////////////        for (int i = 0; i < lanes.size(); i++) {
////////////            Lane ln = lanes.get(i);
////////////            if (ln != null && ln.getOffsetIndex() == targetOffset) return ln;
////////////        }
////////////        return null;
////////////    }
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
////////////
////////////// در infrastructure/Lane.java
////////////package infrastructure;
////////////
////////////import core.Direction;
////////////import core.Point;
////////////import java.util.*;
////////////
////////////public class Lane implements core.Identifiable {
////////////    private final String id;
////////////    private final Direction direction;
////////////    private final Road parentRoad;
////////////    private final List<core.Vehicle> vehicles = new ArrayList<>();
////////////
////////////    // افست عرضی لِین نسبت به خط مرکزی جاده: -2, -1, 0, +1, +2 ...
////////////    private int offsetIndex = 0;
////////////
////////////    // برای رندر و محاسبه موقعیت خودرو روی لِین (با عرض لِین و گپ)
////////////    public static final int LANE_WIDTH = ui.UIConstants.LANE_WIDTH;
////////////    public static final int LANE_GAP   = 2;
////////////
////////////    // طول هندسی لِین (طول جاده)
////////////    public double getLength() { return parentRoad.getGeometricLength(); }
////////////
////////////    public Lane(String id, Direction dir, Road parent) {
////////////        this.id = id; this.direction = dir; this.parentRoad = parent;
////////////    }
////////////    public String getId() { return id; }
////////////    public Direction getDirection() { return direction; }
////////////    public Road getParentRoad() { return parentRoad; }
////////////
////////////    public void setOffsetIndex(int idx) { this.offsetIndex = idx; }
////////////    public int  getOffsetIndex() { return offsetIndex; }
////////////
////////////    /** مرکز لِین در فاصله s (۰..طول) بر حسب مختصات نقشه */
////////////    public Point getCenterAt(double s) {
////////////        Point A = parentRoad.getStartIntersection().getPosition();
////////////        Point B = parentRoad.getEndIntersection().getPosition();
////////////        double L = Math.max(1.0, getLength());
////////////        double t = Math.max(0, Math.min(1, s / L));
////////////
////////////        double cx = A.getX() + (B.getX() - A.getX()) * t;
////////////        double cy = A.getY() + (B.getY() - A.getY()) * t;
////////////
////////////        // بردار عمود واحد برای جابجایی عرضی لِین
////////////        double dx = B.getX() - A.getX(), dy = B.getY() - A.getY();
////////////        double nlen = Math.hypot(dx, dy); if (nlen < 1e-6) nlen = 1;
////////////        double nx = -dy / nlen, ny = dx / nlen;
////////////
////////////        double off = offsetIndex * (LANE_WIDTH + LANE_GAP);
////////////        int x = (int) Math.round(cx + nx * off);
////////////        int y = (int) Math.round(cy + ny * off);
////////////        return new Point(x, y);
////////////    }
////////////
////////////    // همسایه‌ها برای سبقت/تغییر لِین (اگر داری نگه دار، وگرنه placeholder)
////////////    public Lane getLeftAdjacentLane()  { return parentRoad.getAdjacent(this, -1); }
////////////    public Lane getRightAdjacentLane() { return parentRoad.getAdjacent(this, +1); }
////////////}
////////////
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
////////////package infrastructure; // // پکیج زیرساخت
////////////
////////////import java.util.ArrayList; // // لیست
////////////import java.util.List; // // اینترفیس لیست
////////////import core.Identifiable; // // اینترفیس شناسه
////////////import core.Direction; // // جهت
////////////import core.Vehicle; // // خودرو
////////////import core.Point; // // مختصات
////////////
////////////public class Lane implements Identifiable { // // کلاس لِین
////////////    private String id; // // شناسه یکتا
////////////    private Direction direction; // // جهت حرکت
////////////    private List<Vehicle> vehicles; // // وسایل داخل لِین
////////////    private Road parentRoad; // // جادهٔ والد
////////////    private List<SpeedBump> speedBumps; // // دست‌اندازها
////////////    private int offsetIndex = 0; // // شاخص آفست جانبی (±۱، ±۲، ...)
////////////
////////////    private static final int LANE_CENTER_OFFSET_PX = 14; // // فاصلهٔ مرکز هر لِین از خط مرکزی
////////////
////////////    public Lane(String id, Direction direction, Road parentRoad) { // // سازنده
////////////        this.id = id; // // id
////////////        this.direction = direction; // // جهت
////////////        this.vehicles = new ArrayList<Vehicle>(); // // ساخت لیست
////////////        this.parentRoad = parentRoad; // // والد
////////////        this.speedBumps = new ArrayList<SpeedBump>(); // // دست‌انداز
////////////    }
////////////
////////////    @Override public String getId() { return this.id; } // // گتر id
////////////    public Direction getDirection() { return this.direction; } // // گتر جهت
////////////    public Road getParentRoad() { return this.parentRoad; } // // گتر والد
////////////    public void setParentRoad(Road road) { this.parentRoad = road; } // // ست والد
////////////    public void setOffsetIndex(int idx) { this.offsetIndex = idx; } // // ست آفست
////////////    public int getOffsetIndex() { return this.offsetIndex; } // // گتر آفست
////////////
////////////    public List<Vehicle> getVehicles() { return this.vehicles; } // // لیست وسایل
////////////    public void addSpeedBump(SpeedBump sb) { if (sb != null) { this.speedBumps.add(sb); } } // // افزودن دست‌انداز
////////////    public List<SpeedBump> getSpeedBumps() { return this.speedBumps; } // // گتر دست‌انداز
////////////
////////////    public double getLength() { // // طول لِین = طول جاده
////////////        if (this.parentRoad == null) { return 0.0; } // // نال‌چک
////////////        return this.parentRoad.getLengthMeters(); // // استفاده از طول جاده
////////////    }
////////////
////////////    public double getAngleRadians() { // // زاویهٔ رندر
////////////        if (this.parentRoad == null) { return 0.0; } // // بدون والد
////////////        Point a = this.parentRoad.getStartIntersection().getPosition(); // // A
////////////        Point b = this.parentRoad.getEndIntersection().getPosition(); // // B
////////////        double dx = (double)(b.getX() - a.getX()); // // Δx
////////////        double dy = (double)(b.getY() - a.getY()); // // Δy
////////////        double ang = Math.atan2(dy, dx); // // زاویه محور جاده
////////////        if (this.direction == Direction.WEST || this.direction == Direction.NORTH) { ang += Math.PI; } // // معکوس جهت
////////////        return ang; // // خروجی
////////////    }
////////////
////////////    public Point getPositionAt(double s) { // // نقطه روی لِین (با آفست جانبی)
////////////        if (this.parentRoad == null) { return new Point(0, 0); } // // نال‌چک
////////////        Point a = this.parentRoad.getStartIntersection().getPosition(); // // A
////////////        Point b = this.parentRoad.getEndIntersection().getPosition(); // // B
////////////
////////////        int dx = b.getX() - a.getX(); // // Δx
////////////        int dy = b.getY() - a.getY(); // // Δy
////////////        double len = Math.sqrt((double)dx * dx + (double)dy * dy); // // طول
////////////        if (len <= 1e-9) { return new Point(a.getX(), a.getY()); } // // محافظه‌کاری
////////////
////////////        double t = s / len; if (t < 0.0) t = 0.0; if (t > 1.0) t = 1.0; // // نسبت ۰..۱ و کلمپ
////////////        double cx = a.getX() + t * dx; // // X مرکزی
////////////        double cy = a.getY() + t * dy; // // Y مرکزی
////////////
////////////        // نرمال واحد برای آفست جانبی
////////////        double nx = -dy / len; // // نرمال X
////////////        double ny =  dx / len; // // نرمال Y
////////////        double off = this.offsetIndex * LANE_CENTER_OFFSET_PX; // // مقدار آفست
////////////
////////////        int x = (int)Math.round(cx + nx * off); // // X نهایی
////////////        int y = (int)Math.round(cy + ny * off); // // Y نهایی
////////////        return new Point(x, y); // // خروجی
////////////    }
////////////
////////////    // ================== لِین‌های کناری برای سبقت/تغییر لِین ==================
////////////
////////////    public Lane getLeftAdjacentLane() { // // لِین سمت چپ نسبت به جهت حرکت
////////////        if (this.parentRoad == null) { return null; } // // نال‌چک
////////////        // جهت چپ: برای EAST/SOUTH یعنی offsetIndex - 1 ، برای WEST/NORTH یعنی +1
////////////        int targetOffset = this.offsetIndex; // // شروع از فعلی
////////////        if (this.direction == Direction.EAST || this.direction == Direction.SOUTH) { // // رو به جلو
////////////            targetOffset = this.offsetIndex - 1; // // چپ = آفست منفی‌تر
////////////            List<Lane> same = this.parentRoad.getForwardLanes(); // // لِین‌های هم‌جهت
////////////            return findByOffset(same, targetOffset); // // جست‌وجو
////////////        } else { // // WEST/NORTH (برگشت)
////////////            targetOffset = this.offsetIndex + 1; // // چپ = آفست مثبت‌تر
////////////            List<Lane> same = this.parentRoad.getBackwardLanes(); // // لِین‌های هم‌جهت
////////////            return findByOffset(same, targetOffset); // // جست‌وجو
////////////        }
////////////    }
////////////
////////////    public Lane getRightAdjacentLane() { // // لِین سمت راست نسبت به جهت حرکت
////////////        if (this.parentRoad == null) { return null; } // // نال‌چک
////////////        // جهت راست: برای EAST/SOUTH یعنی offsetIndex + 1 ، برای WEST/NORTH یعنی -1
////////////        int targetOffset = this.offsetIndex; // // شروع از فعلی
////////////        if (this.direction == Direction.EAST || this.direction == Direction.SOUTH) { // // رو به جلو
////////////            targetOffset = this.offsetIndex + 1; // // راست = آفست مثبت‌تر
////////////            List<Lane> same = this.parentRoad.getForwardLanes(); // // لِین‌های هم‌جهت
////////////            return findByOffset(same, targetOffset); // // جست‌وجو
////////////        } else { // // WEST/NORTH
////////////            targetOffset = this.offsetIndex - 1; // // راست = آفست منفی‌تر
////////////            List<Lane> same = this.parentRoad.getBackwardLanes(); // // لِین‌های هم‌جهت
////////////            return findByOffset(same, targetOffset); // // جست‌وجو
////////////        }
////////////    }
////////////
////////////    private Lane findByOffset(List<Lane> lanes, int offset) { // // کمک: پیدا کردن لِین با آفست مشخص
////////////        if (lanes == null) { return null; } // // نال‌چک
////////////        for (int i = 0; i < lanes.size(); i++) { // // حلقه
////////////            Lane ln = lanes.get(i); // // کاندید
////////////            if (ln != null && ln.getOffsetIndex() == offset) { return ln; } // // تطابق آفست
////////////        }
////////////        return null; // // پیدا نشد
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
////////////package infrastructure; // // پکیج زیرساخت
////////////
////////////import java.util.ArrayList; // // لیست
////////////import java.util.List; // // اینترفیس لیست
////////////import core.Identifiable; // // اینترفیس شناسه
////////////import core.Direction; // // جهت
////////////import core.Vehicle; // // خودرو
////////////import core.Point; // // مختصات
////////////
////////////public class Lane implements Identifiable { // // کلاس لِین
////////////    private String id; // // شناسه یکتا
////////////    private Direction direction; // // جهت حرکت
////////////    private List<Vehicle> vehicles; // // وسایل داخل لِین
////////////    private Road parentRoad; // // جادهٔ والد
////////////    private List<SpeedBump> speedBumps; // // دست‌اندازها
////////////    private int offsetIndex = 0; // // شاخص آفست جانبی (علامت سمت، قدرمطلق شمارهٔ لِین از مرکز)
////////////    // نکته: offsetIndex > 0 یعنی سمت «مثبت» خط مرکزی، offsetIndex < 0 سمت «منفی»
////////////    // هر واحد offsetIndex یک فاصلهٔ ثابت از مرکز جاده است.
////////////
////////////    // فاصلهٔ جانبی هر «واحد لِین» نسبت به مرکز (px) — باید بزرگ‌تر از نیم‌عرض خودرو باشد
////////////    private static final int LANE_CENTER_OFFSET_PX = 14; // // ۱۴px → از هم جدا دیده شوند
////////////
////////////    public Lane(String id, Direction direction, Road parentRoad) { // // سازنده
////////////        this.id = id; // // مقداردهی id
////////////        this.direction = direction; // // مقداردهی جهت
////////////        this.vehicles = new ArrayList<Vehicle>(); // // ساخت لیست وسایل
////////////        this.parentRoad = parentRoad; // // ست والد
////////////        this.speedBumps = new ArrayList<SpeedBump>(); // // ساخت لیست دست‌انداز
////////////    }
////////////
////////////    @Override public String getId() { return this.id; } // // گتر id
////////////    public Direction getDirection() { return this.direction; } // // گتر جهت
////////////    public Road getParentRoad() { return this.parentRoad; } // // گتر جاده والد
////////////    public void setParentRoad(Road road) { this.parentRoad = road; } // // ست جاده والد
////////////
////////////    public void setOffsetIndex(int idx) { this.offsetIndex = idx; } // // تعیین آفست جانبی
////////////    public int getOffsetIndex() { return this.offsetIndex; } // // خواندن آفست جانبی
////////////
////////////    public List<Vehicle> getVehicles() { return this.vehicles; } // // گتر وسایل
////////////    public void addSpeedBump(SpeedBump sb) { if (sb != null) { this.speedBumps.add(sb); } } // // افزودن دست‌انداز
////////////    public List<SpeedBump> getSpeedBumps() { return this.speedBumps; } // // گتر دست‌انداز
////////////
////////////    public double getLength() { // // طول لِین = طول جاده
////////////        if (this.parentRoad == null) { return 0.0; } // // نال‌چک
////////////        return this.parentRoad.getLengthMeters(); // // استفاده از طول جاده
////////////    }
////////////
////////////    public double getAngleRadians() { // // زاویهٔ محور جاده برای رندر
////////////        if (this.parentRoad == null) { return 0.0; } // // بدون والد
////////////        Point a = this.parentRoad.getStartIntersection().getPosition(); // // نقطهٔ A
////////////        Point b = this.parentRoad.getEndIntersection().getPosition(); // // نقطهٔ B
////////////        double dx = (double)(b.getX() - a.getX()); // // Δx
////////////        double dy = (double)(b.getY() - a.getY()); // // Δy
////////////        double ang = Math.atan2(dy, dx); // // زاویه خط جاده
////////////        if (this.direction == Direction.WEST || this.direction == Direction.NORTH) { ang += Math.PI; } // // معکوس جهت
////////////        return ang; // // خروجی
////////////    }
////////////
////////////    public Point getPositionAt(double s) { // // محاسبه نقطه روی لِین با آفست جانبی
////////////        if (this.parentRoad == null) { return new Point(0, 0); } // // نال‌چک
////////////        Point a = this.parentRoad.getStartIntersection().getPosition(); // // A
////////////        Point b = this.parentRoad.getEndIntersection().getPosition(); // // B
////////////
////////////        int dx = b.getX() - a.getX(); // // Δx
////////////        int dy = b.getY() - a.getY(); // // Δy
////////////        double len = Math.sqrt((double)dx * dx + (double)dy * dy); // // طول جاده
////////////        if (len <= 1e-9) { return new Point(a.getX(), a.getY()); } // // جلوگیری از تقسیم بر صفر
////////////
////////////        // نسبت طولی روی خط مرکزی
////////////        double t = s / len; // // نسبت 0..1
////////////        if (t < 0.0) t = 0.0; if (t > 1.0) t = 1.0; // // کلمپ
////////////
////////////        // نقطهٔ وسطِ خط مرکزی در نسبت t
////////////        double cx = a.getX() + t * dx; // // X مرکزی
////////////        double cy = a.getY() + t * dy; // // Y مرکزی
////////////
////////////        // نرمالِ واحد به خط (چرخش ۹۰ درجه): برای آفست جانبی
////////////        double nx = -dy / len; // // نرمال X
////////////        double ny =  dx / len; // // نرمال Y
////////////
////////////        // مقدار آفست: شاخص * فاصلهٔ پایه
////////////        double off = this.offsetIndex * LANE_CENTER_OFFSET_PX; // // آفست جانبی
////////////
////////////        int x = (int)Math.round(cx + nx * off); // // اعمال آفست روی X
////////////        int y = (int)Math.round(cy + ny * off); // // اعمال آفست روی Y
////////////        return new Point(x, y); // // خروجی
////////////    }
////////////}
