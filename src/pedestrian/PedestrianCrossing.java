




package pedestrian;

import core.*;
import infrastructure.Intersection;

public class PedestrianCrossing implements Identifiable {
    private String id;
    private Intersection intersection;
    private Direction direction;
    private boolean hasSignal;

    public PedestrianCrossing(String id, Intersection intersection, Direction direction, boolean hasSignal) {
        this.id = id;
        this.intersection = intersection;
        this.direction = direction;
        this.hasSignal = hasSignal;
    }

    @Override
    public String getId() {
        return id;
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean hasSignal() {
        return hasSignal;
    }
}








































//
//
//package pedestrian;
//
//import core.Identifiable;
//import core.Point;
//import core.Direction;
//import infrastructure.Intersection;
//
//public class PedestrianCrossing implements Identifiable {
//    private final String id;
//    private final Intersection intersection;
//    private final Direction direction;
//    private final boolean hasSignal;
//
//    public PedestrianCrossing(String id, Intersection at, Direction dir, boolean hasSignal) {
//        this.id = id;
//        this.intersection = at;
//        this.direction = dir;
//        this.hasSignal = hasSignal;
//    }
//
//    @Override public String getId() { return id; }
//
//    public Intersection getIntersection() { return intersection; }
//    public Direction getDirection()       { return direction; }
//    public boolean hasSignal()            { return hasSignal; }
//
//    /** محل شروع عبور عابر (برای spawn کردن) */
//    public Point startPoint() {
//        Point c = intersection.getPosition();
//        // یک افست کوچک از مرکز تقاطع به سمت خارج
//        int d = 24;
//        switch (direction) {
//            case NORTH: return new Point(c.getX(), c.getY() - d);
//            case SOUTH: return new Point(c.getX(), c.getY() + d);
//            case EAST:  return new Point(c.getX() + d, c.getY());
//            default:    return new Point(c.getX() - d, c.getY());
//        }
//    }
//    /** نقطه‌ی انتهایی عبور (سمت مقابل) */
//    public Point endPoint() {
//        Point s = startPoint();
//        // 2*d آن طرف‌تر
//        Point c = intersection.getPosition();
//        int dx = c.getX() - s.getX();
//        int dy = c.getY() - s.getY();
//        return new Point(c.getX() + dx, c.getY() + dy);
//    }
//
//    public void setPlanned(boolean grouped, int i, double v) {
//    }
//
//    public boolean isNearRoundabout() {
//        return nearRoundabout;
//    }
//
//    public void setNearRoundabout(boolean nearRoundabout) {
//        this.nearRoundabout = nearRoundabout;
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
////
////
////package pedestrian;
////
////import core.*;
////import infrastructure.*;
////
/////**
//// * PedestrianCrossing with helpers:
//// *  - startPoint()  : برای spawn عابر (سازگاری با کد قبلی)
//// *  - isPeopleOnCrossing() : برای چک کردن عبور عابر
//// *  - setPlanned(...) : برای ارورهای setPlanned در World (noop)
//// */
////public class PedestrianCrossing implements Identifiable {
////    private final String id;
////    private final Intersection intersection;
////    private final Direction direction;
////    private final boolean hasSignal;
////
////    private boolean peopleOn = false;
////
////    public PedestrianCrossing(String id, Intersection inter, Direction dir, boolean hasSignal){
////        this.id = id;
////        this.intersection = inter;
////        this.direction = dir;
////        this.hasSignal = hasSignal;
////    }
////
////    @Override public String getId(){ return id; }
////    public Intersection getIntersection(){ return intersection; }
////    public Direction getDirection(){ return direction; }
////    public boolean hasSignal(){ return hasSignal; }
////
////    /** نقطهٔ شروع تقریبی برای اسپاون عابر (سازگاری با کدی که pc.startPoint() صدا می‌زند). */
////    public Point startPoint(){
////        // از مختصات تقاطع استفاده می‌کنیم؛ می‌توان کمی افست داد
////        Point p = intersection.getPosition();
////        int off = 12;
////        int x = p.getX(), y = p.getY();
////        switch (direction){
////            case NORTH: y -= off; break;
////            case SOUTH: y += off; break;
////            case EAST : x += off; break;
////            case WEST : x -= off; break;
////        }
////        return new Point(x, y);
////    }
////
////    /** آیا عابری در حال عبور است؟ */
////    public boolean isPeopleOnCrossing(){ return peopleOn; }
////    public void setPeopleOnCrossing(boolean b){ peopleOn = b; }
////
////    /** سازگاری با متدهای برنامه‌ریزی‌شده در World (در حال حاضر noop). */
////    public void setPlanned(boolean enabled, int everyMs, double prob){ /* no-op for compatibility */ }
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
///
////
////
////
//////
//////
//////package pedestrian;
//////
//////import core.*;
//////import infrastructure.Intersection;
//////
///////** عابر گذر ساده */
//////public class PedestrianCrossing implements Identifiable {
//////
//////    private final String id;
//////    private final Intersection intersection;
//////    private final Direction direction;
//////    private final boolean hasSignal;
//////
//////    // حالت‌های مورد نیاز World/Rules
//////    private boolean planned;
//////    private long    planTickStart;
//////    private double  planDurationSec;
//////    private boolean peopleOnCrossing; // برای isPeopleOnCrossing()
//////
//////    public PedestrianCrossing(String id, Intersection inter, Direction dir, boolean hasSignal) {
//////        this.id = id;
//////        this.intersection = inter;
//////        this.direction = dir;
//////        this.hasSignal = hasSignal;
//////    }
//////
//////    @Override public String getId() { return id; }
//////    public Intersection getIntersection() { return intersection; }
//////    public Direction getDirection() { return direction; }
//////    public boolean hasSignal() { return hasSignal; }
//////
//////    // قبلاً در کد از pc.startPoint() استفاده شده بود
//////    public Point startPoint() { return intersection.getPosition(); }
//////
//////    // برای برنامه‌ریزی عبور گروهی/تکی
//////    public void setPlanned(boolean planned, int tickStart, double durationSec) {
//////        this.planned = planned;
//////        this.planTickStart = tickStart;
//////        this.planDurationSec = durationSec;
//////    }
//////
//////    public boolean isPlanned() { return planned; }
//////
//////    public boolean isPeopleOnCrossing() { return peopleOnCrossing; }
//////    public void setPeopleOnCrossing(boolean v) { this.peopleOnCrossing = v; }
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
////////
////////package pedestrian;
////////
////////import core.*;
////////import infrastructure.Intersection;
////////
/////////**
//////// * یک گذرگاه عابر که روی یک تقاطع قرار می‌گیرد و جهت عبور آن (در کدام لبه‌ی تقاطع) مشخص است.
//////// */
////////public class PedestrianCrossing implements Identifiable {
////////    private final String id;
////////    private final Intersection at;        // تقاطعی که گذرگاه روی آن است
////////    private final Direction direction;    // لبه‌ی تقاطع (N/S/E/W) که گذرگاه روی آن قرار دارد
////////    private final boolean hasSignal;      // آیا چراغ عابر دارد؟
////////
////////    // آفست‌های رندر برای اینکه نقطه شروع عابر کمی از مرکز تقاطع فاصله بگیرد
////////    private static final int START_OFFSET_PX = 20;
////////
////////    public PedestrianCrossing(String id, Intersection at, Direction direction, boolean hasSignal) {
////////        this.id = id;
////////        this.at = at;
////////        this.direction = direction;
////////        this.hasSignal = hasSignal;
////////    }
////////
////////    @Override
////////    public String getId() { return id; }
////////
////////    public Intersection getIntersection() { return at; }
////////
////////    public Direction getDirection() { return direction; }
////////
////////    public boolean hasSignal() { return hasSignal; }
////////
////////    /**
////////     * نقطه‌ی شروع عابر پیاده در این گذرگاه، نسبت به موقعیت هندسی تقاطع و جهت گذرگاه.
////////     */
////////    public Point startPoint() {
////////        Point p = at.getPosition();
////////        int x = p.getX();
////////        int y = p.getY();
////////
////////        switch (direction) {
////////            case NORTH: y -= START_OFFSET_PX; break;
////////            case SOUTH: y += START_OFFSET_PX; break;
////////            case EAST:  x += START_OFFSET_PX; break;
////////            case WEST:  x -= START_OFFSET_PX; break;
////////        }
////////        return new Point(x, y);
////////    }
////////
////////    /**
////////     * نقطه‌ی خروجی عابر از گذرگاه (سمت مقابل startPoint).
////////     */
////////    public Point endPoint() {
////////        Point p = at.getPosition();
////////        int x = p.getX();
////////        int y = p.getY();
////////
////////        switch (direction) {
////////            case NORTH: y += START_OFFSET_PX; break; // سمت مقابل NORTH می‌شود نزدیک SOUTH
////////            case SOUTH: y -= START_OFFSET_PX; break;
////////            case EAST:  x -= START_OFFSET_PX; break;
////////            case WEST:  x += START_OFFSET_PX; break;
////////        }
////////        return new Point(x, y);
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
//////////package pedestrian;
//////////
//////////import core.*;
//////////import infrastructure.Intersection;
//////////
//////////import java.util.ArrayList;
//////////
//////////public class PedestrianCrossing implements Identifiable {
//////////    private String id;
//////////    private Intersection intersection;
//////////    private Direction direction;
//////////    private boolean hasSignal;
//////////
//////////    public PedestrianCrossing(String id, Intersection intersection, Direction direction, boolean hasSignal) {
//////////        this.id = id;
//////////        this.intersection = intersection;
//////////        this.direction = direction;
//////////        this.hasSignal = hasSignal;
//////////    }
//////////
//////////    @Override
//////////    public String getId() {
//////////        return id;
//////////    }
//////////
//////////    public Intersection getIntersection() {
//////////        return intersection;
//////////    }
//////////
//////////    public Direction getDirection() {
//////////        return direction;
//////////    }
//////////
//////////    public boolean hasSignal() {
//////////        return hasSignal;
//////////    }
//////////
//////////
//////////    // PedestrianCrossing.java (اضافه‌ها)
//////////    private boolean nearRoundabout = false;
//////////    private boolean planned;
//////////    private boolean grouped;
//////////    private int batchSize = 1;
//////////    private double intervalSec = 5.0;
//////////    private double timer;
//////////
//////////    public void setNearRoundabout(boolean v){ nearRoundabout=v; }
//////////    public boolean isNearRoundabout(){ return nearRoundabout; }
//////////
//////////    public void setPlanned(boolean grouped, int batchSize, double intervalSec){
//////////        this.planned = true; this.grouped = grouped;
//////////        this.batchSize = Math.max(1, batchSize);
//////////        this.intervalSec = Math.max(1.0, intervalSec);
//////////        this.timer = 0.0;
//////////    }
//////////
//////////    // فراخوانی از World: هر crossing می‌تواند People را وارد کند
//////////    public ArrayList<Pedestrian> updatePlan(double dt){
//////////        ArrayList<Pedestrian> out = new ArrayList<>();
//////////        if (!planned) return out;
//////////        timer += dt;
//////////        if (timer >= intervalSec){
//////////            timer = 0.0;
//////////            int count = grouped? batchSize : 1;
//////////            for (int i=0;i<count;i++){
//////////                Pedestrian p = Pedestrian.spawnAt(this);
//////////                out.add(p);
//////////            }
//////////        }
//////////        return out;
//////////    }
//////////
//////////
//////////
//////////
//////////}
