package infrastructure; // // پکیج زیرساخت

import core.Direction; // // جهت‌ها
import core.Point;     // // مختصات
import core.Identifiable; // // داشتن ID
import trafficcontrol.TrafficControlDevice; // // چراغ/تابلو

import java.util.HashMap; // // مپ ساده
import java.util.Map;     // // اینترفیس مپ

public class Intersection implements Identifiable { // // تقاطع
    private final String id;     // // شناسه یکتا
    private final Point position; // // مختصات مرکز تقاطع
    private Map<Direction, TrafficControlDevice> controls; // // کنترل‌های ترافیکی به ازای هر جهت

    public Intersection(String id, Point position) { // // سازنده
        this.id = id; // // ذخیره ID
        this.position = position; // // ذخیره مختصات
        this.controls = new HashMap<Direction, TrafficControlDevice>(); // // مپ خالی برای کنترل‌ها
    }

    @Override
    public String getId() { // // برگرداندن شناسه
        return id;
    }

    public Point getPosition() { // // برگرداندن مختصات
        return position;
    }

    // ---------- کنترل‌های ترافیکی (برای استفاده در SimulatorPanel و World) ----------
    public Map<Direction, TrafficControlDevice> getControls() { // // گرفتن کل مپ کنترل‌ها
        return controls; // // ممکنه خالی باشه ولی null نیست
    }

    public TrafficControlDevice getControl(Direction d) { // // کنترل مربوط به یک جهت خاص
        return controls.get(d); // // اگر نباشه null برمی‌گرده
    }

    public void setControl(Direction d, TrafficControlDevice device) { // // ثبت چراغ/تابلو برای یک جهت
        controls.put(d, device); // // قرار دادن در مپ
    }
}


























//package infrastructure;
//
//import core.Identifiable;
//import core.Locatable;
//import core.Point;
//import trafficcontrol.TrafficControlDevice;
//import core.Direction;
//
//import java.util.EnumMap;
//import java.util.Map;
//
//public class Intersection implements Identifiable, Locatable {
//    private final String id;
//    private final Point position;
//    private final Map<Direction, TrafficControlDevice> controls = new EnumMap<>(Direction.class);
//    private boolean nearRoundabout = false; // برای Rule فلکه
//
//    public Intersection(String id, Point position) {
//        this.id = id;
//        this.position = position;
//    }
//
//    @Override public String getId() { return id; }
//    @Override public Point getPosition() { return position; }
//
//    public void setControl(Direction dir, TrafficControlDevice dev) { controls.put(dir, dev); }
//    public TrafficControlDevice getControl(Direction dir) { return controls.get(dir); }
//
//    public void setNearRoundabout(boolean v) { this.nearRoundabout = v; }
//    public boolean isNearRoundabout() { return nearRoundabout; }
//}
//





















//
//package infrastructure;
//
//import java.util.*;
//import core.*;
//import trafficcontrol.*;
//
//public class Intersection implements Identifiable, Locatable {
//
//    private final String id;
//    private final Point position;
//
//    private final Map<Direction, TrafficControlDevice> controls = new EnumMap<>(Direction.class);
//
//    // فلکه؟
//    private boolean roundabout = false;
//
//    // داده‌های کمکی برای حق‌تقدم فلکه (اختیاری اما مفید برای World)
//    private List<Lane> ringLanes; // دو لاین راست‌گرد
//    private final Map<Direction, Double> ringMergeS = new EnumMap<>(Direction.class);
//
//    // برای Yield معمولی
//    private final Map<Direction, Lane> priorityLane = new EnumMap<>(Direction.class);
//    private final Map<Lane, Double>   priorityCheckS = new HashMap<>();
//
//    public Intersection(String id, Point pos){ this.id=id; this.position=pos; }
//
//    public String getId(){ return id; }
//    public Point getPosition(){ return position; }
//
//    public void setControl(Direction dir, TrafficControlDevice dev){ controls.put(dir, dev); }
//    public TrafficControlDevice getControl(Direction dir){ return controls.get(dir); }
//
//    public boolean isRoundabout(){ return roundabout; }
//    public void markAsRoundabout(boolean value){ this.roundabout = value; }
//
//    public void setRingLanes(List<Lane> lanes){ this.ringLanes = lanes; }
//    public List<Lane> getRingLanes(){ return ringLanes; }
//    public void setRingMergeS(Direction d, double s){ ringMergeS.put(d, s); }
//    public double getRingMergeS(Direction d){ return ringMergeS.getOrDefault(d, 0.0); }
//
//    public void setPriorityLane(Direction entering, Lane lane, double checkS){
//        priorityLane.put(entering, lane);
//        priorityCheckS.put(lane, checkS);
//    }
//    public Lane getPriorityLaneAgainst(Direction entering){ return priorityLane.get(entering); }
//    public double getPriorityCheckS(Lane ln){ return priorityCheckS.getOrDefault(ln, 0.0); }
//
//    // هندسهٔ جاده‌های وصل‌شده (لازم به‌جای خودتان)
//    public Road getConnected(Direction d){ /* ... */ return null; }
//}
//























//package infrastructure; // // پکیج زیرساخت
//
//import core.Direction; // // جهت‌ها
//import core.Point;     // // مختصات
//import core.Identifiable; // // داشتن ID
//import trafficcontrol.TrafficControlDevice; // // چراغ/تابلو
//
//import java.util.HashMap; // // مپ ساده
//import java.util.Map;     // // اینترفیس مپ
//
//public class Intersection implements Identifiable { // // تقاطع
//    private final String id;     // // شناسه یکتا
//    private final Point position; // // مختصات مرکز تقاطع
//    private Map<Direction, TrafficControlDevice> controls; // // کنترل‌های ترافیکی به ازای هر جهت
//
//    public Intersection(String id, Point position) { // // سازنده
//        this.id = id; // // ذخیره ID
//        this.position = position; // // ذخیره مختصات
//        this.controls = new HashMap<Direction, TrafficControlDevice>(); // // مپ خالی برای کنترل‌ها
//    }
//
//    @Override
//    public String getId() { // // برگرداندن شناسه
//        return id;
//    }
//
//    public Point getPosition() { // // برگرداندن مختصات
//        return position;
//    }
//
//    // ---------- کنترل‌های ترافیکی (برای استفاده در SimulatorPanel و World) ----------
//    public Map<Direction, TrafficControlDevice> getControls() { // // گرفتن کل مپ کنترل‌ها
//        return controls; // // ممکنه خالی باشه ولی null نیست
//    }
//
//    public TrafficControlDevice getControl(Direction d) { // // کنترل مربوط به یک جهت خاص
//        return controls.get(d); // // اگر نباشه null برمی‌گرده
//    }
//
//    public void setControl(Direction d, TrafficControlDevice device) { // // ثبت چراغ/تابلو برای یک جهت
//        controls.put(d, device); // // قرار دادن در مپ
//    }
//}



















//package infrastructure;
//
//import core.*;
//import trafficcontrol.TrafficControlDevice;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class Intersection implements Identifiable {
//    private String id;
//    private Point position;
//    private Map<Direction, TrafficControlDevice> controls;
//
//    public Intersection(String id, Point position) {
//        this.id = id;
//        this.position = position;
//        this.controls = new HashMap<>();
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
//    public void setControl(Direction direction, TrafficControlDevice device) {
//        controls.put(direction, device);
//    }
//
//    public TrafficControlDevice getControl(Direction direction) {
//        return controls.get(direction);
//    }
//
//    public boolean canProceed(Vehicle v) {
//        Direction dir = v.getCurrentLane() != null ? v.getCurrentLane().getDirection() : null;
//        if (dir != null && controls.containsKey(dir)) {
//            return controls.get(dir).canProceed(v);
//        }
//        return true; // اگر کنترل خاصی نبود، عبور مجاز است
//    }
//}
