
package pedestrian;

import core.Identifiable;
import core.Point;

/**
 * Pedestrian: عابر پیاده
 */
public class Pedestrian implements Identifiable {
    private final String id;
    private Point position;
    private final PedestrianCrossing crossing;

    // مسیر حرکت
    private final Point start;
    private final Point end;

    private boolean finished = false;
    private double speed = 1.5; // سرعت حرکت px در هر tick

    // --- سازنده اصلی (۴ پارامتر) ---
    public Pedestrian(String id, Point start, Point end, PedestrianCrossing crossing) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.crossing = crossing;
        this.position = new Point(start.getX(), start.getY());
    }

    // --- سازنده ساده (۳ پارامتر) ---
    public Pedestrian(String id, Point start, PedestrianCrossing crossing) {
        this.id = id;
        this.start = start;
        this.crossing = crossing;

        // به طور پیش‌فرض end را بر اساس crossing بسازیم
        // فرض: اگر start سمت چپ گذرگاه باشه → end سمت راست میشه
        if (crossing != null && crossing.getIntersection() != null) {
            Point center = crossing.getIntersection().getPosition();
            if (start.getX() < center.getX()) {
                this.end = new Point(center.getX() + 20, center.getY());
            } else {
                this.end = new Point(center.getX() - 20, center.getY());
            }
        } else {
            // fallback
            this.end = new Point(start.getX() + 40, start.getY());
        }

        this.position = new Point(start.getX(), start.getY());
    }

    @Override
    public String getId() { return id; }

    public Point getPosition() { return position; }

    public boolean isFinished() { return finished; }

    public PedestrianCrossing getCrossing() { return crossing; }

    // بروزرسانی موقعیت عابر
    public void update() {
        if (finished) return;

        double dx = end.getX() - position.getX();
        double dy = end.getY() - position.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < speed) {
            // رسید به مقصد
            position = new Point(end.getX(), end.getY());
            finished = true;
        } else {
            // حرکت تدریجی
            double stepX = (dx / dist) * speed;
            double stepY = (dy / dist) * speed;
            position = new Point((int)(position.getX() + stepX), (int)(position.getY() + stepY));
        }
    }
}
































//package pedestrian;
//
//import core.Identifiable;
//import core.Point;
//
///**
// * Pedestrian: عابر پیاده
// */
//public class Pedestrian implements Identifiable {
//    private final String id;
//    private Point position;
//    private final PedestrianCrossing crossing;
//
//    // مسیر حرکت
//    private final Point start;
//    private final Point end;
//
//    private boolean finished = false;
//    private double speed = 1.5; // سرعت حرکت px در هر tick
//
//    public Pedestrian(String id, Point start, Point end, PedestrianCrossing crossing) {
//        this.id = id;
//        this.start = start;
//        this.end = end;
//        this.crossing = crossing;
//        this.position = new Point(start.getX(), start.getY());
//    }
//
//    @Override
//    public String getId() { return id; }
//
//    public Point getPosition() { return position; }
//
//    public boolean isFinished() { return finished; }
//
//    public PedestrianCrossing getCrossing() { return crossing; }
//
//    // بروزرسانی موقعیت عابر
//    public void update() {
//        if (finished) return;
//
//        double dx = end.getX() - position.getX();
//        double dy = end.getY() - position.getY();
//        double dist = Math.sqrt(dx * dx + dy * dy);
//
//        if (dist < speed) {
//            // رسید به مقصد
//            position = new Point(end.getX(), end.getY());
//            finished = true;
//        } else {
//            // حرکت تدریجی
//            double stepX = (dx / dist) * speed;
//            double stepY = (dy / dist) * speed;
//            position = new Point((int)(position.getX() + stepX), (int)(position.getY() + stepY));
//        }
//    }
//}



























//
//
//package pedestrian;
//
//import core.*;
//import simulation.Updatable;
//
//public class Pedestrian implements Identifiable, Updatable {
//    private String id;
//    private Point position;
//    private PedestrianCrossing targetCrossing;
//    private boolean isCrossing;
//
//    public Pedestrian(String id, Point startPosition, PedestrianCrossing crossing) {
//        this.id = id;
//        this.position = startPosition;
//        this.targetCrossing = crossing;
//        this.isCrossing = false;
//    }
//
//    @Override
//    public String getId() {
//        return id;
//    }
//
//    public Point getPosition() {
//        return position;
//    }
//
//    public boolean isCrossing() {
//        return isCrossing;
//    }
//
//    public PedestrianCrossing getTargetCrossing() {
//        return targetCrossing;
//    }
//
//    @Override
//    public void update() {
//        // منطق ساده برای عبور از خیابان
//        if (!isCrossing) {
//            isCrossing = true; // شروع به عبور
//        } else {
//            isCrossing = false; // فرض می‌کنیم عبور تمام شد
//        }
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
//
//
//
//
//
//
////
////// pedestrian/Pedestrian.java
////package pedestrian; // // پکیج عابر
////
////import core.*; // // Point/Identifiable/Locatable/Updatable
////import simulation.Updatable; // // آپدیت
////import java.util.UUID; // // ساخت id یکتا
////
////public class Pedestrian implements Identifiable, Locatable, Updatable { // // کلاس عابر پیاده
////    private final String id; // // شناسه
////    private Point position; // // موقعیت فعلی
////    private final PedestrianCrossing crossing; // // گذرگاه هدف
////    private final double speed; // // سرعت px/s
////    private double dtSeconds = 0.016; // // گام زمانی
////    private boolean finished = false; // // پایان عبور
////
////    public Pedestrian(PedestrianCrossing crossing, double speedPxPerSec) { // // سازنده
////        this.id = "P-" + UUID.randomUUID().toString(); // // تولید id
////        this.crossing = crossing; // // ست گذرگاه
////        this.speed = Math.max(10.0, speedPxPerSec); // // محدودیت حداقل
////        this.position = crossing.startPoint(); // // شروع از نقطه شروع
////    }
////
////    @Override public String getId(){ return id; } // // گتر id
////    @Override public Point getPosition(){ return position; } // // گتر موقعیت
////    public boolean isFinished(){ return finished; } // // اتمام؟
////
////    public void setDtSeconds(double dt){ if(dt>0) this.dtSeconds=dt; } // // ست dt
////
////    @Override
////    public void update(){ // // به‌روزرسانی حرکت
////        if (finished) return; // // اگر تمام شده هیچ
////        if (crossing.hasSignal() && crossing.getSignal() == PedestrianSignal.DONT_WALK) { // // اگر چراغ نرو
////            return; // // توقف
////        }
////        Point goal = crossing.endPoint(); // // مقصد
////        double dx = goal.getX() - position.getX(); // // Δx
////        double dy = goal.getY() - position.getY(); // // Δy
////        double dist = Math.sqrt(dx*dx + dy*dy); // // فاصله
////        if (dist < 1.0) { // // اگر خیلی نزدیک
////            position = goal; // // چفت روی مقصد
////            finished = true; // // پایان
////            return; // // خروج
////        }
////        double vx = (dx / dist) * speed; // // بردار سرعت X
////        double vy = (dy / dist) * speed; // // بردار سرعت Y
////        int nx = (int)Math.round(position.getX() + vx * dtSeconds); // // X جدید
////        int ny = (int)Math.round(position.getY() + vy * dtSeconds); // // Y جدید
////        position = new Point(nx, ny); // // ست موقعیت
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
////////package pedestrian; // پکیج عابر
////////
////////import core.*;                 // Identifiable, Locatable, Point
////////import simulation.Updatable;   // اینترفیس آپدیت
////////
/////////**
//////// * عابر پیاده ساده: از startPoint گذرگاه به سمت endPoint حرکت می‌کند.
//////// */
////////public class Pedestrian implements Identifiable, Locatable, Updatable {
////////
////////    private final String id;                 // شناسه
////////    private Point position;                  // موقعیت فعلی (Immutable => هر بار نمونه جدید می‌سازیم)
////////    private final PedestrianCrossing target; // گذرگاه هدف
////////    private boolean isCrossing;              // آیا در حال عبور است؟
////////
////////    // سرعت پیکسل بر تیک (بعداً می‌تونیم با dt دقیق‌سازی کنیم)
////////    private int pixelsPerTick = 2;
////////
////////    public Pedestrian(String id, Point startPos, PedestrianCrossing crossing) {
////////        this.id = id;
////////        this.position = startPos;
////////        this.target = crossing;
////////        this.isCrossing = true;
////////    }
////////
////////    // ====== کارخانه: همان امضایی که در کدت صدا می‌زدی ======
////////    public static Pedestrian spawnAt(PedestrianCrossing pc) {
////////        String pid = "p-" + System.nanoTime();
////////        Point pos = pc.startPoint();     // ← ارور اینجا بود؛ الان متد وجود دارد
////////        return new Pedestrian(pid, pos, pc);
////////    }
////////
////////    // ====== Identifiable ======
////////    @Override
////////    public String getId() { return id; }
////////
////////    // ====== Locatable ======
////////    @Override
////////    public Point getPosition() { return position; }
////////
////////    // ====== Logic ======
////////    public boolean isCrossing() { return isCrossing; }
////////
////////    public void setPixelsPerTick(int v) { if (v > 0) pixelsPerTick = v; }
////////
////////    @Override
////////    public void update() {
////////        if (!isCrossing || target == null) return;
////////
////////        // حرکت خطی از موقعیت فعلی به سمت endPoint گذرگاه
////////        Point dest = target.endPoint();
////////
////////        int x = position.getX();
////////        int y = position.getY();
////////        int dx = dest.getX() - x;
////////        int dy = dest.getY() - y;
////////
////////        int absDx = Math.abs(dx);
////////        int absDy = Math.abs(dy);
////////
////////        if (absDx == 0 && absDy == 0) {
////////            // رسیدیم
////////            isCrossing = false;
////////            return;
////////        }
////////
////////        // نرمال‌سازی بردار و گام‌برداری
////////        double len = Math.max(1.0, Math.hypot(dx, dy));
////////        double stepX = pixelsPerTick * (dx / len);
////////        double stepY = pixelsPerTick * (dy / len);
////////
////////        int nx = x + (int)Math.round(stepX);
////////        int ny = y + (int)Math.round(stepY);
////////
////////        // اگر گام از مقصد عبور کرد، clamp به مقصد
////////        if (Math.signum(dest.getX() - nx) != Math.signum(dx)) nx = dest.getX();
////////        if (Math.signum(dest.getY() - ny) != Math.signum(dy)) ny = dest.getY();
////////
////////        position = new Point(nx, ny);
////////
////////        if (nx == dest.getX() && ny == dest.getY()) {
////////            isCrossing = false; // عبور تمام شد
////////        }
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
////////package pedestrian;
////////
////////import core.*;
////////import simulation.Updatable;
////////
////////public class Pedestrian implements Identifiable, Updatable {
////////    private String id;
////////    private Point position;
////////    private PedestrianCrossing targetCrossing;
////////    private boolean isCrossing;
////////
////////    public Pedestrian(String id, Point startPosition, PedestrianCrossing crossing) {
////////        this.id = id;
////////        this.position = startPosition;
////////        this.targetCrossing = crossing;
////////        this.isCrossing = false;
////////    }
////////
////////    @Override
////////    public String getId() {
////////        return id;
////////    }
////////
////////    public Point getPosition() {
////////        return position;
////////    }
////////
////////    public boolean isCrossing() {
////////        return isCrossing;
////////    }
////////
////////    public PedestrianCrossing getTargetCrossing() {
////////        return targetCrossing;
////////    }
////////
////////    @Override
////////    public void update() {
////////        // منطق ساده برای عبور از خیابان
////////        if (!isCrossing) {
////////            isCrossing = true; // شروع به عبور
////////        } else {
////////            isCrossing = false; // فرض می‌کنیم عبور تمام شد
////////        }
////////    }
////////
////////
////////    // Pedestrian.java (کمکی اسپاون)
////////    public static Pedestrian spawnAt(PedestrianCrossing pc){
////////        String id = "P"+System.nanoTime();
////////        Point pos = pc.startPoint();
////////        return new Pedestrian(id, pos, pc);
////////    }
////////
////////
////////
////////
////////}
