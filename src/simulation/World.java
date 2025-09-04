package simulation; // // پکیج شبیه‌سازی

import core.*;                       // // Vehicle/State/Direction/VehicleType/Point
import infrastructure.*;             // // CityMap/Lane/Road/Intersection
import trafficcontrol.*;             // // TrafficLight/LightState/TrafficControlDevice
import pedestrian.*;                 // // Pedestrian/Crossing
import java.util.*;                  // // لیست/مپ/کالکشن

public class World implements Updatable { // // دنیای شبیه‌سازی

    // --- لیست‌های امن --- //
    private final List<Vehicle> vehicles;                 // // خودروها
    private final List<TrafficLight> trafficLights;       // // چراغ‌ها
    private final List<Pedestrian> pedestrians;           // // عابرها
    private final List<PedestrianCrossing> crossings;     // // گذرگاه‌ها

    private final CityMap map;                            // // نقشه
    private double dtSeconds = SimulationConfig.TICK_DT_SEC; // // dt پیش‌فرض

    public World(CityMap map) {                           // // سازنده
        this.map = map;                                   // // ست نقشه
        this.vehicles       = Collections.synchronizedList(new ArrayList<Vehicle>());      // // لیست ایمن
        this.trafficLights  = Collections.synchronizedList(new ArrayList<TrafficLight>());  // // لیست ایمن
        this.pedestrians    = Collections.synchronizedList(new ArrayList<Pedestrian>());    // // لیست ایمن
        this.crossings      = Collections.synchronizedList(new ArrayList<PedestrianCrossing>()); // // لیست ایمن
    }

    // --- dt --- //
    public void setDtSeconds(double dt) {                 // // تنظیم dt
        if (dt <= 0) dt = SimulationConfig.TICK_DT_SEC;   // // حداقل
        this.dtSeconds = dt;                               // // ذخیره
    }

    // --- افزودن‌ها (سازگار) --- //
    public void addVehicle(Vehicle v) {                   // // افزودن خودرو (قدیمی)
        if (v != null) {                                  // // محافظت
            v.setDtSeconds(dtSeconds);                    // // همانگام‌سازی dt
            vehicles.add(v);                              // // افزودن
            Lane l = v.getCurrentLane();                  // // لِین فعلی
            if (l != null) l.registerVehicle(v);          // // ثبت در لِین
        }
    }

    // افزونه: افزودن مستقیم در یک لِین با s اولیه //
    public void addVehicle(Lane lane, double s, Vehicle v) { // // افزودن در لِین
        if (v == null || lane == null) return;            // // محافظت
        v.setCurrentLane(lane);                            // // ست لِین
        v.setPositionInLane(s);                            // // ست s
        addVehicle(v);                                     // // افزودن به دنیا
    }

    public void addTrafficLight(TrafficLight tl){ if (tl != null) trafficLights.add(tl); } // // چراغ
    public void addPedestrian(Pedestrian p){ if (p != null) pedestrians.add(p); }         // // عابر
    public void addCrossing(PedestrianCrossing c){ if (c != null) crossings.add(c); }     // // گذرگاه

    // --- snapshot getters --- //
    public CityMap getMap(){ return map; }                          // // نقشه
    public List<Vehicle> getVehicles(){ synchronized (vehicles){ return Collections.unmodifiableList(new ArrayList<Vehicle>(vehicles)); } } // // کپی
    public List<TrafficLight> getTrafficLights(){ synchronized (trafficLights){ return Collections.unmodifiableList(new ArrayList<TrafficLight>(trafficLights)); } } // // کپی
    public List<Pedestrian> getPedestrians(){ synchronized (pedestrians){ return Collections.unmodifiableList(new ArrayList<Pedestrian>(pedestrians)); } } // // کپی
    public List<PedestrianCrossing> getCrossings(){ synchronized (crossings){ return Collections.unmodifiableList(new ArrayList<PedestrianCrossing>(crossings)); } } // // کپی

    // --- حذف ایمن (در صورت نیاز) --- //
    public void removeVehicle(Vehicle v){ if (v == null) return; vehicles.remove(v); Lane l=v.getCurrentLane(); if(l!=null) l.unregisterVehicle(v); } // // حذف خودرو

    @Override
    public void update() {                                // // تیک شبیه‌سازی (بدون پارامتر)
        // ۱) snapshot برای چراغ‌ها //
        List<TrafficLight> tls; synchronized (trafficLights){ tls = new ArrayList<TrafficLight>(trafficLights); } // // کپی
        for (int i = 0; i < tls.size(); i++) tls.get(i).update(); // // آپدیت چراغ

        // ۲) snapshot خودروها //
        List<Vehicle> vs; synchronized (vehicles){ vs = new ArrayList<Vehicle>(vehicles); } // // کپی

        // ۳) تزریق dt و آپدیت هر خودرو //
        for (int i = 0; i < vs.size(); i++) {            // // حلقه
            Vehicle v = vs.get(i);                       // // خودرو
            v.setDtSeconds(dtSeconds);                   // // همگام dt
            v.update();                                  // // آپدیت (IDM داخل Vehicle)
        }

        // ۴) snapshot عابرها و آپدیت //
        List<Pedestrian> ps; synchronized (pedestrians){ ps = new ArrayList<Pedestrian>(pedestrians); } // // کپی
        for (int i = 0; i < ps.size(); i++) ps.get(i).update(); // // آپدیت عابر
    }
}


























//package simulation; // // پکیج شبیه‌سازی
//
//import core.*; // // Vehicle/State/Direction
//import infrastructure.*; // // CityMap/Lane/Road
//import trafficcontrol.*; // // چراغ/وضعیت
//import pedestrian.*; // // عابر/گذرگاه
//import java.util.*; // // کالکشن‌ها
//
///**
// * دنیای شبیه‌سازی ایمن‌شده برای چندنخی: لیست‌های synchronizedList و اسنپ‌شات برای پیمایش. //
// * سازگاری: update() قدیمی حفظ شده؛ update(double dt) اضافه شده و ترجیحاً توسط SimulationClock صدا می‌شود. //
// */
//public class World implements Updatable { // // دنیا
//
//    private final List<Vehicle> vehicles; // // لیست خودروها (thread-safe wrapper)
//    private final List<TrafficLight> trafficLights; // // لیست چراغ‌ها (thread-safe wrapper)
//    private final List<Pedestrian> pedestrians; // // لیست عابرها (thread-safe wrapper)
//    private final List<PedestrianCrossing> crossings; // // لیست گذرگاه‌ها (thread-safe wrapper)
//    private final CityMap map; // // نقشه
//    private double dtSeconds = 0.1; // // گام زمانی پیش‌فرض (ثانیه)
//
//    public World(CityMap map) { // // سازنده
//        this.map = map; // // ذخیره نقشه
//        this.vehicles = Collections.synchronizedList(new ArrayList<Vehicle>()); // // لیست ایمن خودرو
//        this.trafficLights = Collections.synchronizedList(new ArrayList<TrafficLight>()); // // لیست ایمن چراغ
//        this.pedestrians = Collections.synchronizedList(new ArrayList<Pedestrian>()); // // لیست ایمن عابر
//        this.crossings = Collections.synchronizedList(new ArrayList<PedestrianCrossing>()); // // لیست ایمن گذرگاه
//    }
//
//    public void setDtSeconds(double dt) { // // ست dt
//        if (dt <= 0) dt = 0.1; // // حداقل
//        this.dtSeconds = dt; // // ذخیره
//        List<Vehicle> snap = getVehiclesSnapshotInternal(); // // گرفتن اسنپ‌شات
//        for (int i = 0; i < snap.size(); i++) { // // حلقه خودروها
//            try { snap.get(i).setDtSeconds(dt); } catch (Throwable ignored) {} // // همگام‌سازی dt
//        }
//    }
//
//    // ------------------ API افزودن/حذفِ thread-safe ------------------
//    public void addVehicle(Vehicle v){ if (v != null){ v.setDtSeconds(dtSeconds); vehicles.add(v);} } // // افزودن خودرو
//    public void removeVehicle(Vehicle v){ if (v != null){ vehicles.remove(v);} } // // حذف خودرو
//    public void addTrafficLight(TrafficLight tl){ if (tl != null) trafficLights.add(tl); } // // افزودن چراغ
//    public void removeTrafficLight(TrafficLight tl){ if (tl != null) trafficLights.remove(tl); } // // حذف چراغ
//    public void addPedestrian(Pedestrian p){ if (p != null) pedestrians.add(p); } // // افزودن عابر
//    public void removePedestrian(Pedestrian p){ if (p != null) pedestrians.remove(p); } // // حذف عابر
//    public void addCrossing(PedestrianCrossing c){ if (c != null) crossings.add(c); } // // افزودن گذرگاه
//    public void removeCrossing(PedestrianCrossing c){ if (c != null) crossings.remove(c); } // // حذف گذرگاه
//
//    // ------------------ Getterهای snapshot (ایمن) ------------------
//    public CityMap getMap(){ return map; } // // گتر نقشه
//
//    public List<Vehicle> getVehicles(){ // // لیست خواندنی خودروها
//        return Collections.unmodifiableList(getVehiclesSnapshotInternal()); // // برگرداندن کپی غیرقابل‌تغییر
//    }
//    public List<TrafficLight> getTrafficLights(){ // // لیست خواندنی چراغ‌ها
//        return Collections.unmodifiableList(getTrafficLightsSnapshotInternal()); // // کپی غیرقابل‌تغییر
//    }
//    public List<Pedestrian> getPedestrians(){ // // لیست خواندنی عابرین
//        return Collections.unmodifiableList(getPedestriansSnapshotInternal()); // // کپی غیرقابل‌تغییر
//    }
//    public List<PedestrianCrossing> getCrossings(){ // // لیست خواندنی گذرگاه‌ها
//        return Collections.unmodifiableList(getCrossingsSnapshotInternal()); // // کپی غیرقابل‌تغییر
//    }
//
//    private List<Vehicle> getVehiclesSnapshotInternal(){ // // اسنپ‌شات داخلی خودرو
//        synchronized (vehicles){ return new ArrayList<Vehicle>(vehicles); } // // کپی ایمن
//    }
//    private List<TrafficLight> getTrafficLightsSnapshotInternal(){ // // اسنپ‌شات داخلی چراغ
//        synchronized (trafficLights){ return new ArrayList<TrafficLight>(trafficLights); } // // کپی ایمن
//    }
//    private List<Pedestrian> getPedestriansSnapshotInternal(){ // // اسنپ‌شات داخلی عابر
//        synchronized (pedestrians){ return new ArrayList<Pedestrian>(pedestrians); } // // کپی ایمن
//    }
//    private List<PedestrianCrossing> getCrossingsSnapshotInternal(){ // // اسنپ‌شات داخلی گذرگاه
//        synchronized (crossings){ return new ArrayList<PedestrianCrossing>(crossings); } // // کپی ایمن
//    }
//
//    // ------------------ حلقهٔ آپدیت (سازگار + نسخهٔ dt) ------------------
//    @Override
//    public void update() { // // نسخهٔ قدیمی بدون پارامتر
//        update(this.dtSeconds); // // فراخوانی نسخهٔ جدید با dt فعلی
//    }
//
//    public void update(double dt) { // // نسخهٔ جدید با dt
//        setDtSeconds(dt); // // همگام‌سازی dt برای موجودیت‌ها
//
//        // ۱) اسنپ‌شات‌ها را بگیر تا CME رخ ندهد //
//        List<TrafficLight> tlSnap = getTrafficLightsSnapshotInternal(); // // کپی چراغ‌ها
//        List<Vehicle> vSnap = getVehiclesSnapshotInternal(); // // کپی خودروها
//        List<Pedestrian> pSnap = getPedestriansSnapshotInternal(); // // کپی عابرها
//
//        // ۲) آپدیت چراغ‌ها //
//        for (int i = 0; i < tlSnap.size(); i++) { // // حلقه
//            try { tlSnap.get(i).update(); } catch (Throwable ignored) {} // // محافظت
//        }
//
//        // ۳) ساخت نگاشت «لِین → خودروها به ترتیب حرکت» //
//        HashMap<Lane, ArrayList<Vehicle>> byLane = new HashMap<Lane, ArrayList<Vehicle>>(); // // مپ
//        for (int i = 0; i < vSnap.size(); i++) { // // حلقه خودروها
//            Vehicle v = vSnap.get(i); // // خودرو
//            Lane   l = v.getCurrentLane(); // // لِین
//            if (l == null) continue; // // اگر لِین ندارد
//            ArrayList<Vehicle> list = byLane.get(l); // // لیست لِین
//            if (list == null){ list = new ArrayList<Vehicle>(); byLane.put(l, list);} // // ساخت
//            list.add(v); // // افزودن
//        }
//        // مرتب‌سازی بر اساس «ترتیب حرکت واقعی» //
//        for (Map.Entry<Lane, ArrayList<Vehicle>> e : byLane.entrySet()) { // // پیمایش
//            final Lane lane = e.getKey(); // // لِین
//            final int sign = (lane.getDirection()==Direction.EAST || lane.getDirection()==Direction.SOUTH) ? (+1):(-1); // // علامت
//            Collections.sort(e.getValue(), new Comparator<Vehicle>() { // // مقایسه‌گر
//                @Override public int compare(Vehicle a, Vehicle b){ // // مقایسه
//                    double pa = a.getPositionInLane(); double pb = b.getPositionInLane(); // // موضع
//                    return sign>0 ? Double.compare(pa, pb) : Double.compare(pb, pa); // // جلو→عقب
//                }
//            });
//        }
//
//        // ۴) محاسبهٔ targetSpeed با توجه به چراغ و خودروی جلویی //
//        for (Map.Entry<Lane, ArrayList<Vehicle>> e : byLane.entrySet()) { // // پیمایش لِین‌ها
//            Lane lane = e.getKey(); // // لِین
//            ArrayList<Vehicle> list = e.getValue(); // // خودروها
//            double L = lane.getLength(); // // طول لِین
//            boolean forward = (lane.getDirection()==Direction.EAST || lane.getDirection()==Direction.SOUTH); // // جهت
//            for (int idx = 0; idx < list.size(); idx++) { // // حلقه خودروهای لِین
//                Vehicle v = list.get(idx); // // خودرو
//                double target = 42.0; // // سرعت پایه
//
//                // فاصله تا تقاطع انتهاییِ همین جهت //
//                double distToEnd = forward ? (L - v.getPositionInLane()) : (v.getPositionInLane()); // // فاصله
//                Intersection endInter = forward ? lane.getParentRoad().getEndIntersection() : lane.getParentRoad().getStartIntersection(); // // تقاطع انتها
//                TrafficControlDevice dev = endInter.getControl(lane.getDirection()); // // کنترل
//                if (dev instanceof TrafficLight) { // // چراغ؟
//                    LightState st = ((TrafficLight) dev).getState(); // // وضعیت
//                    if (distToEnd < 55) { // // ناحیهٔ ترمز
//                        if (st == LightState.RED) target = 0; // // قرمز = توقف
//                        else if (st == LightState.YELLOW) target = Math.min(target, 18); // // زرد = کاهش
//                    }
//                }
//
//                // رعایت فاصله از خودروی جلویی //
//                if (idx < list.size() - 1) { // // اگر جلویی هست
//                    Vehicle front = list.get(idx + 1); // // خودرو جلو
//                    double gap = forward ? (front.getPositionInLane() - v.getPositionInLane())
//                            : (v.getPositionInLane() - front.getPositionInLane()); // // فاصله
//                    double minGap = 20 + v.getSpeed() * 0.25; // // حداقل فاصلهٔ ایمن
//                    if (gap < minGap) { // // خیلی نزدیک
//                        target = Math.min(target, Math.max(0, (gap - 8))); // // کاهش هدف
//                    }
//                }
//
//                v.setTargetSpeed(target); // // اعمال هدف
//            }
//        }
//
//        // ۵) آپدیت حرکت خودروها //
//        for (int i = 0; i < vSnap.size(); i++) { // // حلقه
//            try { vSnap.get(i).update(); } catch (Throwable ignored) {} // // محافظت
//        }
//
//        // ۶) آپدیت سادهٔ عابرها (دمو) //
//        for (int i = 0; i < pSnap.size(); i++) { // // حلقه
//            try { pSnap.get(i).update(); } catch (Throwable ignored) {} // // محافظت
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
//////گراکککککککککککککککککککککککککککک
////package simulation; // // پکیج شبیه‌سازی
////
////import core.*; // // Vehicle/State/Direction
////import infrastructure.*; // // CityMap/Lane/Road
////import trafficcontrol.*; // // چراغ/وضعیت
////import pedestrian.*; // // عابر/گذرگاه
////
////import java.util.*; // // لیست‌ها
////
////public class World implements Updatable { // // دنیای شبیه‌سازی
////    private final LinkedList<Vehicle> vehicles; // // خودروها
////    private final LinkedList<TrafficLight> trafficLights; // // چراغ‌ها
////    private final LinkedList<Pedestrian> pedestrians; // // عابرها
////    private final LinkedList<PedestrianCrossing> crossings; // // گذرگاه‌ها
////    private final CityMap map; // // نقشه
////    private double dtSeconds = 0.1; // // گام زمانی
////
////    public World(CityMap map) { // // سازنده
////        this.map = map; // // ست نقشه
////        this.vehicles = new LinkedList<Vehicle>(); // // لیست خودرو
////        this.trafficLights = new LinkedList<TrafficLight>(); // // لیست چراغ
////        this.pedestrians = new LinkedList<Pedestrian>(); // // لیست عابر
////        this.crossings = new LinkedList<PedestrianCrossing>(); // // لیست گذرگاه
////    }
////
////    public void setDtSeconds(double dt) { // // ست dt
////        if (dt <= 0) dt = 0.1; // // حداقل
////        this.dtSeconds = dt; // // ذخیره
////        for (int i = 0; i < vehicles.size(); i++) vehicles.get(i).setDtSeconds(dt); // // همگام‌سازی
////    }
////
////    public void addVehicle(Vehicle v){ if (v != null){ v.setDtSeconds(dtSeconds); vehicles.add(v);} } // // افزودن خودرو
////    public void addTrafficLight(TrafficLight tl){ if (tl != null) trafficLights.add(tl); } // // افزودن چراغ
////    public void addPedestrian(Pedestrian p){ if (p != null) pedestrians.add(p); } // // افزودن عابر
////    public void addCrossing(PedestrianCrossing c){ if (c != null) crossings.add(c); } // // افزودن گذرگاه
////
////    public CityMap getMap(){ return map; } // // گتر نقشه
////    public List<Vehicle> getVehicles(){ return vehicles; } // // گتر خودروها
////    public List<TrafficLight> getTrafficLights(){ return trafficLights; } // // گتر چراغ‌ها
////    public List<Pedestrian> getPedestrians(){ return pedestrians; } // // گتر عابرها
////    public List<PedestrianCrossing> getCrossings(){ return crossings; } // // گتر گذرگاه‌ها
////
////    @Override
////    public void update() { // // تیک شبیه‌سازی
////        // ۱) آپدیت چراغ‌ها //
////        for (int i = 0; i < trafficLights.size(); i++) trafficLights.get(i).update(); // // آپدیت چراغ
////
////        // ۲) تعیین سرعت هدف بر اساس چراغِ انتهای لِین و خودروی جلویی //
////        // ساخت نگاشت «لِین → خودروها به ترتیب حرکت» //
////        HashMap<Lane, ArrayList<Vehicle>> byLane = new HashMap<Lane, ArrayList<Vehicle>>(); // // مپ لِین به لیست
////        for (int i = 0; i < vehicles.size(); i++) { // // حلقه خودروها
////            Vehicle v = vehicles.get(i); // // خودرو
////            Lane   l = v.getCurrentLane(); // // لِین
////            if (l == null) continue; // // اگر لِین ندارد
////            ArrayList<Vehicle> list = byLane.get(l); // // لیست لِین
////            if (list == null){ list = new ArrayList<Vehicle>(); byLane.put(l, list);} // // ایجاد
////            list.add(v); // // افزودن
////        }
////        // مرتب‌سازی بر اساس «ترتیب حرکت واقعی» //
////        for (Map.Entry<Lane, ArrayList<Vehicle>> e : byLane.entrySet()) { // // پیمایش لِین‌ها
////            final Lane lane = e.getKey(); // // لِین
////            final int sign = (lane.getDirection()==Direction.EAST || lane.getDirection()==Direction.SOUTH) ? (+1):(-1); // // علامت
////            Collections.sort(e.getValue(), new Comparator<Vehicle>() { // // مرتب‌سازی
////                @Override public int compare(Vehicle a, Vehicle b){ // // مقایسه
////                    double pa = a.getPositionInLane(); double pb = b.getPositionInLane(); // // موضع
////                    return sign>0 ? Double.compare(pa, pb) : Double.compare(pb, pa); // // جلو→عقب
////                }
////            });
////        }
////
////        // محاسبهٔ targetSpeed با توجه به چراغ و خودروی جلویی //
////        for (Map.Entry<Lane, ArrayList<Vehicle>> e : byLane.entrySet()) { // // برای هر لِین
////            Lane lane = e.getKey(); // // لِین
////            ArrayList<Vehicle> list = e.getValue(); // // خودروهای همان لِین
////            double L = lane.getLength(); // // طول لِین
////            boolean forward = (lane.getDirection()==Direction.EAST || lane.getDirection()==Direction.SOUTH); // // جهت
////            for (int idx = 0; idx < list.size(); idx++) { // // حلقه خودروهای لِین
////                Vehicle v = list.get(idx); // // خودرو
////                double target = 42.0; // // سرعت پایه
////
////                // فاصله تا تقاطع انتهاییِ همین جهت //
////                double distToEnd = forward ? (L - v.getPositionInLane()) : (v.getPositionInLane()); // // فاصله تا انتها
////                Intersection endInter = forward ? lane.getParentRoad().getEndIntersection() : lane.getParentRoad().getStartIntersection(); // // تقاطع انتها
////                TrafficControlDevice dev = endInter.getControl(lane.getDirection()); // // کنترل همین جهت
////                if (dev instanceof TrafficLight) { // // چراغ؟
////                    LightState st = ((TrafficLight) dev).getState(); // // وضعیت
////                    if (distToEnd < 55) { // // ناحیهٔ ترمز
////                        if (st == LightState.RED) target = 0; // // قرمز = توقف
////                        else if (st == LightState.YELLOW) target = Math.min(target, 18); // // زرد = کاهش
////                    }
////                }
////
////                // رعایت فاصله از خودروی جلویی //
////                if (idx < list.size() - 1) { // // اگر جلویی وجود دارد
////                    Vehicle front = list.get(idx + 1); // // خودروی جلو
////                    double gap = forward ? (front.getPositionInLane() - v.getPositionInLane())
////                            : (v.getPositionInLane() - front.getPositionInLane()); // // فاصلهٔ طولی
////                    double minGap = 20 + v.getSpeed() * 0.25; // // حداقل فاصلهٔ ایمن
////                    if (gap < minGap) { // // خیلی نزدیک
////                        target = Math.min(target, Math.max(0, (gap - 8))); // // کاهش هدف
////                    }
////                }
////
////                v.setTargetSpeed(target); // // اعمال هدف
////            }
////        }
////
////        // ۳) آپدیت حرکت خودروها //
////        for (int i = 0; i < vehicles.size(); i++) vehicles.get(i).update(); // // آپدیت
////
////        // ۴) آپدیت سادهٔ عابرها (دمو) //
////        for (int i = 0; i < pedestrians.size(); i++) pedestrians.get(i).update(); // // آپدیت عابر
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
////
////
////
//////7777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777
//////
//////package simulation; // پکیج شبیه‌ساز //
//////
//////import core.Vehicle; // خودرو //
//////import core.Point; // نقطه //
//////import infrastructure.CityMap; // نقشه //
//////import infrastructure.Intersection; // تقاطع //
//////import trafficcontrol.*; // چراغ و LightState و ... //
//////import pedestrian.Pedestrian; // عابر //
//////
//////import java.util.*; // Collections //
//////
//////public class World implements Updatable { // دنیا //
//////    private final CityMap map; // نقشه //
//////    private final List<Vehicle> vehicles = new ArrayList<Vehicle>(); // خودروها //
//////    private final List<Pedestrian> pedestrians = new ArrayList<Pedestrian>(); // عابرها //
//////    private final List<TrafficLight> trafficLights = new ArrayList<TrafficLight>(); // همه چراغ‌ها //
//////
//////    // گروه‌های چراغ هماهنگ برای هر تقاطع: [N,S,E,W] //
//////    private final List<TrafficLight[]> syncedLights = new ArrayList<TrafficLight[]>(); // //
//////
//////    private double dtSeconds = 0.1; // گام زمانی //
//////
//////    // نمایش برچسب تصادف //
//////    public static class Accident { // کلاس دیتای تصادف //
//////        public final Point position; // محل //
//////        public int ticksLeft; // مدت باقی‌مانده نمایش //
//////        public Accident(Point pos, int durationTicks){ this.position = pos; this.ticksLeft = durationTicks; } // سازنده //
//////    }
//////    private final LinkedList<Accident> activeAccidents = new LinkedList<Accident>(); // لیست برچسب‌ها //
//////
//////    public World(CityMap map) { // سازنده //
//////        this.map = map; // ذخیره //
//////    }
//////
//////    // --- getters / setters --- //
//////    public CityMap getMap(){ return map; } // گتر نقشه //
//////    public List<Vehicle> getVehicles(){ return vehicles; } // گتر خودروها //
//////    public List<Pedestrian> getPedestrians(){ return pedestrians; } // گتر عابرها //
//////    public List<TrafficLight> getTrafficLights(){ return trafficLights; } // گتر چراغ‌ها //
//////    public void setDtSeconds(double dt){ if (dt > 0) this.dtSeconds = dt; } // تنظیم dt //
//////
//////    // --- add methods --- //
//////    public void addVehicle(Vehicle v){ if (v != null) vehicles.add(v); } // افزودن خودرو //
//////    public void addPedestrian(Pedestrian p){ if (p != null) pedestrians.add(p); } // ✅ فیکس: افزودن عابر //
//////    public void addTrafficLight(TrafficLight t){ if (t != null) trafficLights.add(t); } // افزودن چراغ //
//////    public void addSynchronizedLights(TrafficLight n, TrafficLight s, TrafficLight e, TrafficLight w){ // ✅ فیکس: ثبت گروه هماهنگ //
//////        syncedLights.add(new TrafficLight[]{n, s, e, w}); // //
//////    }
//////
//////    // خروجی برای رندر برچسب‌ها //
//////    public List<Accident> getActiveAccidents(){ return new ArrayList<Accident>(activeAccidents); } // کپی ایمن //
//////
//////    @Override
//////    public void update() { // تیک //
//////        // ۱) آپدیت چراغ‌های هماهنگ //
//////        for (int gi = 0; gi < syncedLights.size(); gi++) {
//////            TrafficLight[] group = syncedLights.get(gi); // گروه //
//////            TrafficLight n = group[0], s = group[1], e = group[2], w = group[3]; // اعضا //
//////            n.update(); // فقط north جلو می‌رود //
//////            s.setState(n.getState()); // south مثل north //
//////
//////            if (n.getState() == LightState.GREEN || n.getState() == LightState.YELLOW) { // اگر NS سبز/زرد //
//////                e.setState(LightState.RED); // EW قرمز //
//////                w.setState(LightState.RED); // //
//////            } else { // اگر NS قرمز //
//////                e.update(); // E جلو برود //
//////                w.setState(e.getState()); // W مثل E //
//////            }
//////        }
//////
//////        // ۲) چراغ‌هایی که عضو هیچ گروهی نیستند، مستقل آپدیت شوند //
//////        for (int i = 0; i < trafficLights.size(); i++) {
//////            TrafficLight tl = trafficLights.get(i);
//////            if (!isInSyncedGroup(tl)) tl.update();
//////        }
//////
//////        // ۳) منطق ساده تعامل خودرو با چراغ انتهای لاین (Red=Stop / Yellow=Slow) //
//////        for (int i = 0; i < vehicles.size(); i++) {
//////            Vehicle v = vehicles.get(i);
//////            double target = 42.0; // سرعت پایه //
//////            if (v.getCurrentLane() != null) {
//////                double len = v.getCurrentLane().getLength();
//////                double dist = len - v.getPositionInLane();
//////                if (dist < 45) { // نزدیک انتهای لاین //
//////                    Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection();
//////                    TrafficControlDevice dev = end.getControl(v.getCurrentLane().getDirection());
//////                    if (dev instanceof TrafficLight) {
//////                        LightState st = ((TrafficLight)dev).getState();
//////                        if (st == LightState.RED) target = 0;        // قرمز: توقف //
//////                        else if (st == LightState.YELLOW) target = Math.min(target, 18); // زرد: کند //
//////                    }
//////                }
//////            }
//////            v.setTargetSpeed(target); // اعمال //
//////        }
//////
//////        // ۴) آپدیت حرکت خودروها و عابرها //
//////        for (int i = 0; i < vehicles.size(); i++) vehicles.get(i).update(); // خودرو //
//////        for (int i = 0; i < pedestrians.size(); i++) pedestrians.get(i).update(); // عابر //
//////
//////        // ۵) مدیریت ثبت/حذف برچسب تصادف (کوتاه) //
//////        detectAndRecordAccidents(); // تشخیص //
//////        decayAccidents();           // کاهش تایمر //
//////    }
//////
//////    private boolean isInSyncedGroup(TrafficLight tl){ // بررسی عضویت چراغ //
//////        for (int gi = 0; gi < syncedLights.size(); gi++) {
//////            TrafficLight[] g = syncedLights.get(gi);
//////            for (int k = 0; k < g.length; k++) if (g[k] == tl) return true;
//////        }
//////        return false;
//////    }
//////
//////    private void detectAndRecordAccidents(){ // تشخیص خیلی ساده //
//////        final double THRESH = 5.0; // آستانه نزدیکی //
//////        final int DURATION = Math.max(4, (int)Math.round(0.8 / Math.max(dtSeconds, 0.01))); // ~0.8s نمایش //
//////
//////        for (int i = 0; i < vehicles.size(); i++) {
//////            Vehicle a = vehicles.get(i);
//////            if (a.getCurrentLane() == null) continue;
//////            for (int j = i+1; j < vehicles.size(); j++) {
//////                Vehicle b = vehicles.get(j);
//////                if (b.getCurrentLane() != a.getCurrentLane()) continue;
//////                if (Math.abs(a.getPositionInLane() - b.getPositionInLane()) <= THRESH) {
//////                    Point p = a.getCurrentLane().getPositionAt((a.getPositionInLane() + b.getPositionInLane()) * 0.5);
//////                    boolean dup = false;
//////                    for (int k = 0; k < activeAccidents.size(); k++) {
//////                        Point q = activeAccidents.get(k).position;
//////                        int dx = q.getX()-p.getX(), dy = q.getY()-p.getY();
//////                        if (dx*dx + dy*dy < 16*16) { dup = true; break; }
//////                    }
//////                    if (!dup) activeAccidents.add(new Accident(p, DURATION));
//////                }
//////            }
//////        }
//////    }
//////
//////    private void decayAccidents(){ // کم‌کردن تایمر نمایش //
//////        for (int i = activeAccidents.size()-1; i >= 0; i--) {
//////            Accident a = activeAccidents.get(i);
//////            a.ticksLeft--;
//////            if (a.ticksLeft <= 0) activeAccidents.remove(i);
//////        }
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
////////package simulation; // پکیج شبیه‌سازی //
////////
////////import core.Vehicle; // خودرو //
////////import core.Point; // نقطه //
////////import infrastructure.CityMap; // نقشه //
////////import trafficcontrol.*; // چراغ‌ها //
////////
////////import java.util.LinkedList; // لیست پیوندی //
////////import java.util.List; // اینترفیس لیست //
////////import java.util.ArrayList; // آرایه‌لیست //
////////
////////public class World implements Updatable { // دنیای شبیه‌سازی //
////////    private final LinkedList<Vehicle> vehicles; // فهرست خودروها //
////////    private final LinkedList<TrafficLight> trafficLights; // فهرست چراغ‌ها //
////////    private final LinkedList<pedestrian.Pedestrian> pedestrians; // فهرست عابرها //
////////    private final CityMap map; // نقشه //
////////    private double dtSeconds = 0.1; // dt پیش‌فرض //
////////
////////    // --- ثبت “accident”های فعال برای رندر --- //
////////    public static class Accident { // کلاس عمومی برای دسترسی پنل //
////////        public final Point position; // محل //
////////        public int ticksLeft; // تعداد تیک باقی‌مانده //
////////        public Accident(Point p, int durationTicks) { this.position = p; this.ticksLeft = durationTicks; } // سازنده //
////////    }
////////    private final LinkedList<Accident> activeAccidents = new LinkedList<Accident>(); // لیست تصادف‌ها //
////////
////////    public World(CityMap map) { // سازنده //
////////        this.map = map; // ذخیره نقشه //
////////        this.vehicles = new LinkedList<Vehicle>(); // لیست خودرو //
////////        this.trafficLights = new LinkedList<TrafficLight>(); // لیست چراغ //
////////        this.pedestrians = new LinkedList<pedestrian.Pedestrian>(); // لیست عابر //
////////    }
////////
////////    public void setDtSeconds(double dt) { // ست dt از سمت Clock //
////////        if (dt <= 0) dt = 0.1; // ایمنی //
////////        this.dtSeconds = dt; // ذخیره //
////////        for (int i = 0; i < vehicles.size(); i++) { // همگام‌سازی dt خودروها //
////////            vehicles.get(i).setDtSeconds(dt); // تنظیم dt //
////////        }
////////    }
////////
////////    public CityMap getMap() { return map; } // گتر نقشه //
////////    public List<Vehicle> getVehicles() { return vehicles; } // گتر خودروها //
////////    public List<TrafficLight> getTrafficLights() { return trafficLights; } // گتر چراغ‌ها //
////////    public void addVehicle(Vehicle v) { if (v != null) { v.setDtSeconds(dtSeconds); vehicles.add(v); } } // افزودن خودرو //
////////    public void addTrafficLight(TrafficLight tl) { if (tl != null) trafficLights.add(tl); } // افزودن چراغ //
////////
////////    // ✅ برای SimulatorPanel: لیست تصادف‌های فعال را بده (کپی ایمن) //
////////    public List<Accident> getActiveAccidents() { return new ArrayList<Accident>(activeAccidents); } // خروجی //
////////
////////    @Override
////////    public void update() { // تیک شبیه‌سازی //
////////        // ۱) آپدیت چراغ‌ها //
////////        for (int i = 0; i < trafficLights.size(); i++) { trafficLights.get(i).update(); }
////////
////////        // ۲) منطق ساده سرعت نسبت به چراغ انتهای لِین //
////////        for (int i = 0; i < vehicles.size(); i++) {
////////            Vehicle v = vehicles.get(i);
////////            double target = 42.0; // سرعت پایه //
////////            if (v.getCurrentLane() != null) { // اگر روی لِین است //
////////                double laneLen = v.getCurrentLane().getLength(); // طول لِین //
////////                double distToEnd = laneLen - v.getPositionInLane(); // فاصله تا انتها //
////////                if (distToEnd < 45) { // نزدیک انتها //
////////                    infrastructure.Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection(); // تقاطع //
////////                    TrafficControlDevice dev = end.getControl(v.getCurrentLane().getDirection()); // کنترل //
////////                    if (dev instanceof TrafficLight) {
////////                        LightState st = ((TrafficLight) dev).getState();
////////                        if (st == LightState.RED) target = 0; // قرمز: توقف //
////////                        else if (st == LightState.YELLOW) target = Math.min(target, 18); // زرد: کند //
////////                    }
////////                }
////////            }
////////            v.setTargetSpeed(target); // اعمال //
////////        }
////////
////////        // ۳) حرکت خودروها //
////////        for (int i = 0; i < vehicles.size(); i++) { vehicles.get(i).update(); }
////////
////////        // ۴) تشخیص ساده‌ی تصادف و ثبت “accident” (در همان محل) فقط به‌ندرت //
////////        detectAndRecordAccidents(); // ثبت/افزودن //
////////        decayAccidents(); // کم‌کردن تایمر و حذف //
////////    }
////////
////////    // تشخیص خیلی ساده: دو خودرو روی یک لِین و فاصلهٔ طولی خیلی کم → یکبار “accident” ثبت کن //
////////    private void detectAndRecordAccidents() {
////////        final double THRESH = 5.0; // آستانه نزدیکی (پیکسل) //
////////        final int DURATION = Math.max(4, (int)Math.round(0.8 / Math.max(dtSeconds, 0.01))); // ~0.8s نمایش //
////////
////////        for (int i = 0; i < vehicles.size(); i++) {
////////            Vehicle a = vehicles.get(i);
////////            if (a.getCurrentLane() == null) continue;
////////            for (int j = i + 1; j < vehicles.size(); j++) {
////////                Vehicle b = vehicles.get(j);
////////                if (b.getCurrentLane() != a.getCurrentLane()) continue; // فقط لِین مشترک //
////////                if (Math.abs(a.getPositionInLane() - b.getPositionInLane()) <= THRESH) { // خیلی نزدیک //
////////                    // نقطهٔ میانگین برای برچسب //
////////                    Point p = a.getCurrentLane().getPositionAt((a.getPositionInLane() + b.getPositionInLane()) * 0.5);
////////                    // اگر قبلاً نزدیک همین نقطه ثبت شده، دوباره ثبت نکن //
////////                    boolean duplicate = false;
////////                    for (int k = 0; k < activeAccidents.size(); k++) {
////////                        Point q = activeAccidents.get(k).position;
////////                        int dx = q.getX() - p.getX();
////////                        int dy = q.getY() - p.getY();
////////                        if (dx*dx + dy*dy < 16*16) { duplicate = true; break; } // نزدیکی مکانی //
////////                    }
////////                    if (!duplicate) {
////////                        activeAccidents.add(new Accident(p, DURATION)); // افزودن //
////////                    }
////////                }
////////            }
////////        }
////////    }
////////
////////    // کاهش زمان باقی‌ماندهٔ برچسب‌ها و حذف موارد تمام‌شده //
////////    private void decayAccidents() {
////////        for (int i = activeAccidents.size() - 1; i >= 0; i--) {
////////            Accident a = activeAccidents.get(i);
////////            a.ticksLeft--;
////////            if (a.ticksLeft <= 0) activeAccidents.remove(i);
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
//////
//////
//////
////////package simulation; // پکیج شبیه‌سازی //
////////
////////import core.Vehicle; // خودرو //
////////import pedestrian.Pedestrian; // عابر //
////////import trafficcontrol.TrafficLight; // چراغ //
////////import trafficcontrol.LightState; // وضعیت چراغ //
////////
////////import java.util.ArrayList; //
////////import java.util.List; //
////////
////////public class World implements Updatable { // دنیای شبیه‌سازی //
////////    private final List<Vehicle> vehicles = new ArrayList<Vehicle>(); // لیست خودروها //
////////    private final List<Pedestrian> pedestrians = new ArrayList<Pedestrian>(); // لیست عابرها //
////////    private final List<TrafficLight> trafficLights = new ArrayList<TrafficLight>(); // همه چراغ‌ها //
////////    private final List<TrafficLight[]> syncedLights = new ArrayList<TrafficLight[]>(); // گروه‌های چراغ هماهنگ //
////////
////////    private double dtSeconds = 0.016; // ✅ گام زمانی (برای سازگاری با setDtSeconds) //
////////
////////    public World() { /* سازنده پیش‌فرض */ } // سازنده بدون پارامتر //
////////
////////    public void setDtSeconds(double dt) { this.dtSeconds = dt; } // ✅ ست کردن dt //
////////    public double getDtSeconds() { return dtSeconds; } // گرفتن dt در صورت نیاز //
////////
////////    public void addVehicle(Vehicle v) { vehicles.add(v); } // افزودن خودرو //
////////    public void addPedestrian(Pedestrian p) { pedestrians.add(p); } // افزودن عابر //
////////    public void addTrafficLight(TrafficLight t) { trafficLights.add(t); } // افزودن چراغ //
////////
////////    public List<Vehicle> getVehicles() { return vehicles; } // دسترسی خودروها //
////////    public List<Pedestrian> getPedestrians() { return pedestrians; } // دسترسی عابرها //
////////    public List<TrafficLight> getTrafficLights() { return trafficLights; } // دسترسی چراغ‌ها //
////////
////////    public void addSynchronizedLights(TrafficLight n, TrafficLight s, TrafficLight e, TrafficLight w) { // ثبت گروه //
////////        syncedLights.add(new TrafficLight[]{n, s, e, w}); // ذخیره گروه //
////////    }
////////
////////    @Override
////////    public void update() { // تیک شبیه‌ساز //
////////        // آپدیت خودروها //
////////        for (int i = 0; i < vehicles.size(); i++) { vehicles.get(i).update(); } // بروزرسانی تمام خودروها //
////////
////////        // آپدیت عابرها //
////////        for (int i = 0; i < pedestrians.size(); i++) { pedestrians.get(i).update(); } // بروزرسانی عابرها //
////////
////////        // آپدیت گروه‌های هماهنگ چراغ‌ها //
////////        for (int gi = 0; gi < syncedLights.size(); gi++) { // پیمایش گروه‌ها //
////////            TrafficLight[] g = syncedLights.get(gi); // گرفتن گروه //
////////            TrafficLight n = g[0]; TrafficLight s = g[1]; TrafficLight e = g[2]; TrafficLight w = g[3]; // اعضای گروه //
////////
////////            n.update(); // فقط north را جلو می‌بریم //
////////            s.setState(n.getState()); // south همسان north //
////////
////////            if (n.getState() == LightState.GREEN || n.getState() == LightState.YELLOW) { // اگر NS سبز/زرد //
////////                e.setState(LightState.RED); // EW قرمز //
////////                w.setState(LightState.RED); // //
////////            } else { // اگر NS قرمز //
////////                e.update(); // E چرخه خودش //
////////                w.setState(e.getState()); // W همسان E //
////////            }
////////        }
////////
////////        // چراغ‌های غیرعضو گروه‌ها (در صورت وجود) //
////////        for (int i = 0; i < trafficLights.size(); i++) { // پیمایش همه چراغ‌ها //
////////            TrafficLight tl = trafficLights.get(i); // چراغ فعلی //
////////            if (!isInSyncedGroup(tl)) { tl.update(); } // اگر در گروه نیست، مستقل آپدیت شود //
////////        }
////////    }
////////
////////    private boolean isInSyncedGroup(TrafficLight tl) { // بررسی عضویت چراغ در گروه //
////////        for (int gi = 0; gi < syncedLights.size(); gi++) { // پیمایش گروه‌ها //
////////            TrafficLight[] g = syncedLights.get(gi); // //
////////            for (int k = 0; k < g.length; k++) { if (g[k] == tl) return true; } // تطبیق مرجع //
////////        }
////////        return false; // عضو هیچ گروهی نیست //
////////    }
////////}
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
////////
////////package simulation;
////////
////////import core.Vehicle;
////////import core.Pedestrian;
////////import trafficcontrol.TrafficLight;
////////import trafficcontrol.LightState;
////////
////////import java.util.*;
////////
////////public class World implements Updatable {
////////    private final List<Vehicle> vehicles = new ArrayList<>();
////////    private final List<Pedestrian> pedestrians = new ArrayList<>();
////////    private final List<TrafficLight> trafficLights = new ArrayList<>();
////////
////////    // 🔹 گروه‌های چراغ هماهنگ (N/S , E/W)
////////    private final List<TrafficLight[]> syncedLights = new ArrayList<>();
////////
////////    public void addVehicle(Vehicle v) {
////////        vehicles.add(v);
////////    }
////////
////////    public void addPedestrian(Pedestrian p) {
////////        pedestrians.add(p);
////////    }
////////
////////    public void addTrafficLight(TrafficLight t) {
////////        trafficLights.add(t);
////////    }
////////
////////    public List<Vehicle> getVehicles() { return vehicles; }
////////    public List<Pedestrian> getPedestrians() { return pedestrians; }
////////    public List<TrafficLight> getTrafficLights() { return trafficLights; }
////////
////////    // متد جدید برای اضافه کردن گروه چراغ‌های هماهنگ
////////    public void addSynchronizedLights(TrafficLight n, TrafficLight s, TrafficLight e, TrafficLight w) {
////////        syncedLights.add(new TrafficLight[]{n, s, e, w});
////////    }
////////
////////    // ---------------- آپدیت ----------------
////////    @Override
////////    public void update() {
////////        // 🚗 آپدیت خودروها
////////        for (Vehicle v : vehicles) {
////////            v.update();
////////        }
////////
////////        // 🚶 آپدیت عابرها
////////        for (Pedestrian p : pedestrians) {
////////            p.update();
////////        }
////////
////////        // 🚦 آپدیت گروه‌های چراغ هماهنگ
////////        for (TrafficLight[] group : syncedLights) {
////////            TrafficLight n = group[0];
////////            TrafficLight s = group[1];
////////            TrafficLight e = group[2];
////////            TrafficLight w = group[3];
////////
////////            // فقط north رو آپدیت می‌کنیم
////////            n.update();
////////            s.setState(n.getState()); // south مثل north میشه
////////
////////            // وقتی north/south سبز یا زرد بودن → east/west قرمز
////////            if (n.getState() == LightState.GREEN || n.getState() == LightState.YELLOW) {
////////                e.setState(LightState.RED);
////////                w.setState(LightState.RED);
////////            }
////////            // وقتی north قرمز شد → east/west وارد چرخه میشن
////////            else if (n.getState() == LightState.RED) {
////////                e.update();
////////                w.setState(e.getState());
////////            }
////////        }
////////
////////        // چراغ‌های دیگه که جزو sync group نیستن (مثلا تستی)
////////        for (TrafficLight tl : trafficLights) {
////////            if (!isInSyncedGroup(tl)) {
////////                tl.update();
////////            }
////////        }
////////    }
////////
////////    // بررسی اینکه چراغ جزو گروه هماهنگ هست یا نه
////////    private boolean isInSyncedGroup(TrafficLight tl) {
////////        for (TrafficLight[] group : syncedLights) {
////////            for (TrafficLight t : group) {
////////                if (t == tl) return true;
////////            }
////////        }
////////        return false;
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
////////
////////
////////
////////
////////
////////
////////
////////
//////////package simulation;
//////////
//////////import core.Vehicle;
//////////import core.Point;
//////////import infrastructure.CityMap;
//////////import trafficcontrol.TrafficLight;
//////////import trafficcontrol.LightState;
//////////import infrastructure.Lane;
//////////import pedestrian.Pedestrian;
//////////import pedestrian.PedestrianCrossing;
//////////
//////////import java.util.*;
//////////
//////////public class World implements Updatable {
//////////    private final LinkedList<Vehicle> vehicles;
//////////    private final LinkedList<TrafficLight> trafficLights;
//////////    private final LinkedList<Pedestrian> pedestrians;
//////////    private final CityMap map;
//////////    private double dtSeconds = 0.1;
//////////
//////////    // ---------- مدیریت تصادف ----------
//////////    public static class Accident {
//////////        public double x, y;
//////////        public long endTimeMs;
//////////        public Accident(double x, double y, long endTimeMs) {
//////////            this.x = x; this.y = y; this.endTimeMs = endTimeMs;
//////////        }
//////////    }
//////////    private final List<Accident> activeAccidents = new ArrayList<>();
//////////
//////////    // ---------- مدیریت عابر ----------
//////////    private final List<PedestrianCrossing> crossings = new ArrayList<>();
//////////    private long lastPedestrianSpawnTime = 0;
//////////    private static final long PEDESTRIAN_INTERVAL_MS = 17000; // هر ۱۷ ثانیه
//////////
//////////    public World(CityMap map) {
//////////        this.map = map;
//////////        this.vehicles = new LinkedList<>();
//////////        this.trafficLights = new LinkedList<>();
//////////        this.pedestrians = new LinkedList<>();
//////////    }
//////////
//////////    public void setDtSeconds(double dt) {
//////////        if (dt <= 0) dt = 0.1;
//////////        this.dtSeconds = dt;
//////////        for (Vehicle v : vehicles) {
//////////            v.setDtSeconds(dt);
//////////        }
//////////    }
//////////
//////////    public void addVehicle(Vehicle v) {
//////////        if (v != null) {
//////////            v.setDtSeconds(dtSeconds);
//////////            vehicles.add(v);
//////////        }
//////////    }
//////////
//////////    public void addTrafficLight(TrafficLight tl) {
//////////        if (tl != null) trafficLights.add(tl);
//////////    }
//////////
//////////    public void addPedestrian(Pedestrian p) {
//////////        if (p != null) pedestrians.add(p);
//////////    }
//////////
//////////    public void addCrossing(PedestrianCrossing c) {
//////////        if (c != null) crossings.add(c);
//////////    }
//////////
//////////    public CityMap getMap() { return map; }
//////////    public List<Vehicle> getVehicles() { return vehicles; }
//////////    public List<TrafficLight> getTrafficLights() { return trafficLights; }
//////////    public List<Pedestrian> getPedestrians() { return pedestrians; }
//////////    public List<Accident> getActiveAccidents() { return activeAccidents; }
//////////
//////////    @Override
//////////    public void update() {
//////////        // ۱) آپدیت چراغ‌ها
//////////        for (TrafficLight tl : trafficLights) tl.update();
//////////
//////////        // ۲) تعیین سرعت هدف خودروها
//////////        for (Vehicle v : vehicles) {
//////////            double target = 42.0;
//////////
//////////            double laneLen = (v.getCurrentLane() != null) ? v.getCurrentLane().getLength() : 0;
//////////            double distToEnd = laneLen - v.getPositionInLane();
//////////
//////////            if (v.getCurrentLane() != null && distToEnd < 45) {
//////////                infrastructure.Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection();
//////////                trafficcontrol.TrafficControlDevice dev = end.getControl(v.getCurrentLane().getDirection());
//////////                if (dev instanceof TrafficLight) {
//////////                    LightState st = ((TrafficLight) dev).getState();
//////////                    if (st == LightState.RED) target = 0;
//////////                    else if (st == LightState.YELLOW) target = Math.min(target, 18);
//////////                }
//////////            }
//////////            v.setTargetSpeed(target);
//////////        }
//////////
//////////        // ۳) منطق سبقت
//////////        checkOvertaking();
//////////
//////////        // ۴) آپدیت خودروها
//////////        for (Vehicle v : vehicles) v.update();
//////////
//////////        // ۵) بررسی تصادف‌ها
//////////        checkCollisions();
//////////
//////////        // ۶) مدیریت عابرها
//////////        spawnPedestrians();
//////////        updatePedestrians();
//////////        handleVehiclesNearPedestrians();
//////////
//////////        // ۷) پاک کردن تصادف‌های قدیمی + آزاد کردن ماشین‌ها
//////////        handleAccidentRecovery();
//////////    }
//////////
//////////    // ---------- تولید عابر جدید ----------
//////////    private void spawnPedestrians() {
//////////        long now = System.currentTimeMillis();
//////////        if (now - lastPedestrianSpawnTime < PEDESTRIAN_INTERVAL_MS) return;
//////////        lastPedestrianSpawnTime = now;
//////////
//////////        if (crossings.isEmpty()) return;
//////////        Random rnd = new Random();
//////////        PedestrianCrossing c = crossings.get(rnd.nextInt(crossings.size()));
//////////
//////////        Point start = new Point(c.getIntersection().getPosition().getX() - 20, c.getIntersection().getPosition().getY());
//////////        Point end   = new Point(c.getIntersection().getPosition().getX() + 20, c.getIntersection().getPosition().getY());
//////////
//////////        Pedestrian p = new Pedestrian("P-" + System.nanoTime(), start, end, c);
//////////        pedestrians.add(p);
//////////    }
//////////
//////////    // ---------- آپدیت عابر ----------
//////////    private void updatePedestrians() {
//////////        pedestrians.removeIf(Pedestrian::isFinished);
//////////        for (Pedestrian p : pedestrians) {
//////////            p.update();
//////////        }
//////////    }
//////////
//////////    // ---------- توقف ماشین‌ها جلوی عابر ----------
//////////    private void handleVehiclesNearPedestrians() {
//////////        for (Pedestrian ped : pedestrians) {
//////////            Point pos = ped.getPosition();
//////////            for (Vehicle v : vehicles) {
//////////                if (v.getCurrentLane() == null) continue;
//////////
//////////                Point vp = v.getCurrentLane().getPositionAt(v.getPositionInLane());
//////////                double dist = Math.hypot(vp.getX() - pos.getX(), vp.getY() - pos.getY());
//////////
//////////                if (dist < 30) { // 🚦 توقف ماشین جلوی عابر
//////////                    v.setTargetSpeed(0);
//////////                }
//////////            }
//////////        }
//////////    }
//////////
//////////    // ---------- منطق سبقت ----------
//////////    private void checkOvertaking() {
//////////        for (Vehicle v : vehicles) {
//////////            if (v.isOvertaking()) {
//////////                boolean clear = true;
//////////                for (Vehicle other : vehicles) {
//////////                    if (other == v) continue;
//////////                    if (other.getCurrentLane() == v.getCurrentLane()) {
//////////                        double dist = other.getPositionInLane() - v.getPositionInLane();
//////////                        if (dist > 0 && dist < 20) { clear = false; break; }
//////////                    }
//////////                }
//////////                if (clear) v.finishOvertaking();
//////////                continue;
//////////            }
//////////
//////////            Vehicle front = findFrontVehicle(v);
//////////            if (front != null) {
//////////                double gap = front.getPositionInLane() - v.getPositionInLane();
//////////                if (gap > 0 && gap < 20) {
//////////                    Lane left = v.getCurrentLane().getLeftAdjacentLane();
//////////                    if (left != null) v.startOvertaking(left);
//////////                }
//////////            }
//////////        }
//////////    }
//////////
//////////    private Vehicle findFrontVehicle(Vehicle v) {
//////////        Vehicle closest = null;
//////////        double minDist = Double.MAX_VALUE;
//////////        for (Vehicle other : vehicles) {
//////////            if (other == v) continue;
//////////            if (other.getCurrentLane() == v.getCurrentLane()) {
//////////                double dist = other.getPositionInLane() - v.getPositionInLane();
//////////                if (dist > 0 && dist < minDist) {
//////////                    minDist = dist;
//////////                    closest = other;
//////////                }
//////////            }
//////////        }
//////////        return closest;
//////////    }
//////////
//////////    // ---------- منطق تصادف ----------
//////////    private void checkCollisions() {
//////////        int carLength = ui.UIConstants.VEHICLE_LENGTH;
//////////        Random rnd = new Random();
//////////
//////////        for (int i = 0; i < vehicles.size(); i++) {
//////////            Vehicle v1 = vehicles.get(i);
//////////            for (int j = i + 1; j < vehicles.size(); j++) {
//////////                Vehicle v2 = vehicles.get(j);
//////////
//////////                if (v1.getCurrentLane() == null || v2.getCurrentLane() == null) continue;
//////////                if (v1.getCurrentLane() != v2.getCurrentLane()) continue;
//////////
//////////                double dist = Math.abs(v1.getPositionInLane() - v2.getPositionInLane());
//////////
//////////                if (dist < carLength * 0.8) {
//////////                    // 🚨 فقط ۳٪ احتمال تصادف واقعی
//////////                    if (rnd.nextDouble() < 0.03) {
//////////                        v1.setTargetSpeed(0); v1.setSpeed(0);
//////////                        v2.setTargetSpeed(0); v2.setSpeed(0);
//////////
//////////                        Point p = v1.getCurrentLane().getPositionAt(
//////////                                (v1.getPositionInLane() + v2.getPositionInLane()) / 2.0
//////////                        );
//////////                        long endTime = System.currentTimeMillis() + 7000;
//////////                        activeAccidents.add(new Accident(p.getX(), p.getY(), endTime));
//////////                    } else {
//////////                        // بقیه فقط ترمز می‌کنن
//////////                        v1.setTargetSpeed(0);
//////////                        v2.setTargetSpeed(0);
//////////                    }
//////////                }
//////////            }
//////////        }
//////////    }
//////////
//////////    // ---------- آزادسازی ماشین‌ها بعد از پایان تصادف ----------
//////////    private void handleAccidentRecovery() {
//////////        long now = System.currentTimeMillis();
//////////        Iterator<Accident> it = activeAccidents.iterator();
//////////        while (it.hasNext()) {
//////////            Accident a = it.next();
//////////            if (a.endTimeMs < now) {
//////////                // آزادسازی ماشین‌های نزدیک به محل تصادف
//////////                for (Vehicle v : vehicles) {
//////////                    if (v.getCurrentLane() == null) continue;
//////////                    Point vp = v.getCurrentLane().getPositionAt(v.getPositionInLane());
//////////                    double dist = Math.hypot(vp.getX() - a.x, vp.getY() - a.y);
//////////                    if (dist < 15) {
//////////                        v.setTargetSpeed(42); // دوباره حرکت کنه
//////////                    }
//////////                }
//////////                it.remove();
//////////            }
//////////        }
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
//////////
//////////
//////////
//////////
//////////
//////////
//////////
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
////////////package simulation;
////////////
////////////import core.Vehicle;
////////////import core.Point;
////////////import infrastructure.CityMap;
////////////import trafficcontrol.TrafficLight;
////////////import trafficcontrol.LightState;
////////////import infrastructure.Lane;
////////////import pedestrian.Pedestrian;
////////////import pedestrian.PedestrianCrossing;
////////////
////////////import java.util.*;
////////////
////////////public class World implements Updatable {
////////////    private final LinkedList<Vehicle> vehicles;
////////////    private final LinkedList<TrafficLight> trafficLights;
////////////    private final LinkedList<Pedestrian> pedestrians;
////////////    private final CityMap map;
////////////    private double dtSeconds = 0.1;
////////////
////////////    // ---------- مدیریت تصادف ----------
////////////    public static class Accident {
////////////        public double x, y;
////////////        public long endTimeMs;
////////////        public Accident(double x, double y, long endTimeMs) {
////////////            this.x = x; this.y = y; this.endTimeMs = endTimeMs;
////////////        }
////////////    }
////////////    private final List<Accident> activeAccidents = new ArrayList<>();
////////////
////////////    // ---------- مدیریت عابر ----------
////////////    private final List<PedestrianCrossing> crossings = new ArrayList<>();
////////////    private long lastPedestrianSpawnTime = 0;
////////////    private static final long PEDESTRIAN_INTERVAL_MS = 17000; // هر ۱۷ ثانیه
////////////
////////////    public World(CityMap map) {
////////////        this.map = map;
////////////        this.vehicles = new LinkedList<>();
////////////        this.trafficLights = new LinkedList<>();
////////////        this.pedestrians = new LinkedList<>();
////////////    }
////////////
////////////    public void setDtSeconds(double dt) {
////////////        if (dt <= 0) dt = 0.1;
////////////        this.dtSeconds = dt;
////////////        for (Vehicle v : vehicles) {
////////////            v.setDtSeconds(dt);
////////////        }
////////////    }
////////////
////////////    public void addVehicle(Vehicle v) {
////////////        if (v != null) {
////////////            v.setDtSeconds(dtSeconds);
////////////            vehicles.add(v);
////////////        }
////////////    }
////////////
////////////    public void addTrafficLight(TrafficLight tl) {
////////////        if (tl != null) trafficLights.add(tl);
////////////    }
////////////
////////////    public void addPedestrian(Pedestrian p) {
////////////        if (p != null) pedestrians.add(p);
////////////    }
////////////
////////////    public void addCrossing(PedestrianCrossing c) {
////////////        if (c != null) crossings.add(c);
////////////    }
////////////
////////////    public CityMap getMap() { return map; }
////////////    public List<Vehicle> getVehicles() { return vehicles; }
////////////    public List<TrafficLight> getTrafficLights() { return trafficLights; }
////////////    public List<Pedestrian> getPedestrians() { return pedestrians; }
////////////    public List<Accident> getActiveAccidents() { return activeAccidents; }
////////////
////////////    @Override
////////////    public void update() {
////////////        // ۱) آپدیت چراغ‌ها
////////////        for (TrafficLight tl : trafficLights) tl.update();
////////////
////////////        // ۲) تعیین سرعت هدف خودروها
////////////        for (Vehicle v : vehicles) {
////////////            double target = 42.0;
////////////
////////////            double laneLen = (v.getCurrentLane() != null) ? v.getCurrentLane().getLength() : 0;
////////////            double distToEnd = laneLen - v.getPositionInLane();
////////////
////////////            if (v.getCurrentLane() != null && distToEnd < 45) {
////////////                infrastructure.Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection();
////////////                trafficcontrol.TrafficControlDevice dev = end.getControl(v.getCurrentLane().getDirection());
////////////                if (dev instanceof TrafficLight) {
////////////                    LightState st = ((TrafficLight) dev).getState();
////////////                    if (st == LightState.RED) target = 0;
////////////                    else if (st == LightState.YELLOW) target = Math.min(target, 18);
////////////                }
////////////            }
////////////            v.setTargetSpeed(target);
////////////        }
////////////
////////////        // ۳) منطق سبقت
////////////        checkOvertaking();
////////////
////////////        // ۴) آپدیت خودروها
////////////        for (Vehicle v : vehicles) v.update();
////////////
////////////        // ۵) بررسی تصادف‌ها
////////////        checkCollisions();
////////////
////////////        // ۶) مدیریت عابرها
////////////        spawnPedestrians();
////////////        updatePedestrians();
////////////        handleVehiclesNearPedestrians();
////////////
////////////        // ۷) پاک کردن تصادف‌های قدیمی
////////////        long now = System.currentTimeMillis();
////////////        activeAccidents.removeIf(a -> a.endTimeMs < now);
////////////    }
////////////
////////////    // ---------- تولید عابر جدید ----------
////////////    private void spawnPedestrians() {
////////////        long now = System.currentTimeMillis();
////////////        if (now - lastPedestrianSpawnTime < PEDESTRIAN_INTERVAL_MS) return;
////////////        lastPedestrianSpawnTime = now;
////////////
////////////        if (crossings.isEmpty()) return;
////////////        Random rnd = new Random();
////////////        PedestrianCrossing c = crossings.get(rnd.nextInt(crossings.size()));
////////////
////////////        Point start = new Point(c.getIntersection().getPosition().getX() - 20, c.getIntersection().getPosition().getY());
////////////        Point end   = new Point(c.getIntersection().getPosition().getX() + 20, c.getIntersection().getPosition().getY());
////////////
////////////        Pedestrian p = new Pedestrian("P-" + System.nanoTime(), start, end, c);
////////////        pedestrians.add(p);
////////////    }
////////////
////////////    // ---------- آپدیت عابر ----------
////////////    private void updatePedestrians() {
////////////        pedestrians.removeIf(Pedestrian::isFinished);
////////////        for (Pedestrian p : pedestrians) {
////////////            p.update();
////////////        }
////////////    }
////////////
////////////    // ---------- توقف ماشین‌ها جلوی عابر ----------
////////////    private void handleVehiclesNearPedestrians() {
////////////        for (Pedestrian ped : pedestrians) {
////////////            Point pos = ped.getPosition();
////////////            for (Vehicle v : vehicles) {
////////////                if (v.getCurrentLane() == null) continue;
////////////
////////////                Point vp = v.getCurrentLane().getPositionAt(v.getPositionInLane());
////////////                double dist = Math.hypot(vp.getX() - pos.getX(), vp.getY() - pos.getY());
////////////
////////////                if (dist < 30) { // 🚦 توقف ماشین جلوی عابر
////////////                    v.setTargetSpeed(0);
////////////                }
////////////            }
////////////        }
////////////    }
////////////
////////////    // ---------- منطق سبقت ----------
////////////    private void checkOvertaking() {
////////////        for (Vehicle v : vehicles) {
////////////            if (v.isOvertaking()) {
////////////                boolean clear = true;
////////////                for (Vehicle other : vehicles) {
////////////                    if (other == v) continue;
////////////                    if (other.getCurrentLane() == v.getCurrentLane()) {
////////////                        double dist = other.getPositionInLane() - v.getPositionInLane();
////////////                        if (dist > 0 && dist < 20) { clear = false; break; }
////////////                    }
////////////                }
////////////                if (clear) v.finishOvertaking();
////////////                continue;
////////////            }
////////////
////////////            Vehicle front = findFrontVehicle(v);
////////////            if (front != null) {
////////////                double gap = front.getPositionInLane() - v.getPositionInLane();
////////////                if (gap > 0 && gap < 20) {
////////////                    Lane left = v.getCurrentLane().getLeftAdjacentLane();
////////////                    if (left != null) v.startOvertaking(left);
////////////                }
////////////            }
////////////        }
////////////    }
////////////
////////////    private Vehicle findFrontVehicle(Vehicle v) {
////////////        Vehicle closest = null;
////////////        double minDist = Double.MAX_VALUE;
////////////        for (Vehicle other : vehicles) {
////////////            if (other == v) continue;
////////////            if (other.getCurrentLane() == v.getCurrentLane()) {
////////////                double dist = other.getPositionInLane() - v.getPositionInLane();
////////////                if (dist > 0 && dist < minDist) {
////////////                    minDist = dist;
////////////                    closest = other;
////////////                }
////////////            }
////////////        }
////////////        return closest;
////////////    }
////////////
////////////    // ---------- منطق تصادف ----------
////////////    private void checkCollisions() {
////////////        int carLength = ui.UIConstants.VEHICLE_LENGTH;
////////////        Random rnd = new Random();
////////////
////////////        for (int i = 0; i < vehicles.size(); i++) {
////////////            Vehicle v1 = vehicles.get(i);
////////////            for (int j = i + 1; j < vehicles.size(); j++) {
////////////                Vehicle v2 = vehicles.get(j);
////////////
////////////                if (v1.getCurrentLane() == null || v2.getCurrentLane() == null) continue;
////////////                if (v1.getCurrentLane() != v2.getCurrentLane()) continue;
////////////
////////////                double dist = Math.abs(v1.getPositionInLane() - v2.getPositionInLane());
////////////
////////////                if (dist < carLength * 0.8) {
////////////                    // 🚨 فقط ۳٪ احتمال تصادف واقعی
////////////                    if (rnd.nextDouble() < 0.03) {
////////////                        v1.setTargetSpeed(0); v1.setSpeed(0);
////////////                        v2.setTargetSpeed(0); v2.setSpeed(0);
////////////
////////////                        Point p = v1.getCurrentLane().getPositionAt(
////////////                                (v1.getPositionInLane() + v2.getPositionInLane()) / 2.0
////////////                        );
////////////                        long endTime = System.currentTimeMillis() + 7000;
////////////                        activeAccidents.add(new Accident(p.getX(), p.getY(), endTime));
////////////                    } else {
////////////                        // بقیه فقط ترمز می‌کنن
////////////                        v1.setTargetSpeed(0);
////////////                        v2.setTargetSpeed(0);
////////////                    }
////////////                }
////////////            }
////////////        }
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
//////////
//////////
//////////
//////////
//////////
////////////package simulation;
////////////
////////////import core.Vehicle;
////////////import core.Point;
////////////import infrastructure.CityMap;
////////////import trafficcontrol.TrafficLight;
////////////import trafficcontrol.LightState;
////////////import infrastructure.Lane;
////////////import pedestrian.Pedestrian;
////////////import pedestrian.PedestrianCrossing;
////////////
////////////import java.util.*;
////////////
////////////public class World implements Updatable {
////////////    private final LinkedList<Vehicle> vehicles;
////////////    private final LinkedList<TrafficLight> trafficLights;
////////////    private final LinkedList<Pedestrian> pedestrians;
////////////    private final CityMap map;
////////////    private double dtSeconds = 0.1;
////////////
////////////    // ---------- مدیریت تصادف ----------
////////////    public static class Accident {
////////////        public double x, y;
////////////        public long endTimeMs;
////////////        public Accident(double x, double y, long endTimeMs) {
////////////            this.x = x; this.y = y; this.endTimeMs = endTimeMs;
////////////        }
////////////    }
////////////    private final List<Accident> activeAccidents = new ArrayList<>();
////////////
////////////    // ---------- مدیریت عابر ----------
////////////    private final List<PedestrianCrossing> crossings = new ArrayList<>();
////////////    private long lastPedestrianSpawnTime = 0;
////////////    private static final long PEDESTRIAN_INTERVAL_MS = 17000; // هر ۱۷ ثانیه
////////////
////////////    public World(CityMap map) {
////////////        this.map = map;
////////////        this.vehicles = new LinkedList<>();
////////////        this.trafficLights = new LinkedList<>();
////////////        this.pedestrians = new LinkedList<>();
////////////    }
////////////
////////////    public void setDtSeconds(double dt) {
////////////        if (dt <= 0) dt = 0.1;
////////////        this.dtSeconds = dt;
////////////        for (Vehicle v : vehicles) {
////////////            v.setDtSeconds(dt);
////////////        }
////////////    }
////////////
////////////    public void addVehicle(Vehicle v) {
////////////        if (v != null) {
////////////            v.setDtSeconds(dtSeconds);
////////////            vehicles.add(v);
////////////        }
////////////    }
////////////
////////////    public void addTrafficLight(TrafficLight tl) {
////////////        if (tl != null) trafficLights.add(tl);
////////////    }
////////////
////////////    public void addPedestrian(Pedestrian p) {
////////////        if (p != null) pedestrians.add(p);
////////////    }
////////////
////////////    public void addCrossing(PedestrianCrossing c) {
////////////        if (c != null) crossings.add(c);
////////////    }
////////////
////////////    public CityMap getMap() { return map; }
////////////    public List<Vehicle> getVehicles() { return vehicles; }
////////////    public List<TrafficLight> getTrafficLights() { return trafficLights; }
////////////    public List<Pedestrian> getPedestrians() { return pedestrians; }
////////////    public List<Accident> getActiveAccidents() { return activeAccidents; }
////////////
////////////    @Override
////////////    public void update() {
////////////        // ۱) آپدیت چراغ‌ها
////////////        for (TrafficLight tl : trafficLights) tl.update();
////////////
////////////        // ۲) تعیین سرعت هدف خودروها
////////////        for (Vehicle v : vehicles) {
////////////            double target = 42.0;
////////////
////////////            double laneLen = (v.getCurrentLane() != null) ? v.getCurrentLane().getLength() : 0;
////////////            double distToEnd = laneLen - v.getPositionInLane();
////////////
////////////            if (v.getCurrentLane() != null && distToEnd < 45) {
////////////                infrastructure.Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection();
////////////                trafficcontrol.TrafficControlDevice dev = end.getControl(v.getCurrentLane().getDirection());
////////////                if (dev instanceof TrafficLight) {
////////////                    LightState st = ((TrafficLight) dev).getState();
////////////                    if (st == LightState.RED) target = 0;
////////////                    else if (st == LightState.YELLOW) target = Math.min(target, 18);
////////////                }
////////////            }
////////////            v.setTargetSpeed(target);
////////////        }
////////////
////////////        // ۳) منطق سبقت
////////////        checkOvertaking();
////////////
////////////        // ۴) آپدیت خودروها
////////////        for (Vehicle v : vehicles) v.update();
////////////
////////////        // ۵) بررسی تصادف‌ها
////////////        checkCollisions();
////////////
////////////        // ۶) مدیریت عابرها
////////////        spawnPedestrians();
////////////        updatePedestrians();
////////////        handleVehiclesNearPedestrians();
////////////
////////////        // ۷) پاک کردن تصادف‌های قدیمی
////////////        long now = System.currentTimeMillis();
////////////        activeAccidents.removeIf(a -> a.endTimeMs < now);
////////////    }
////////////
////////////    // ---------- تولید عابر جدید ----------
////////////    private void spawnPedestrians() {
////////////        long now = System.currentTimeMillis();
////////////        if (now - lastPedestrianSpawnTime < PEDESTRIAN_INTERVAL_MS) return;
////////////        lastPedestrianSpawnTime = now;
////////////
////////////        if (crossings.isEmpty()) return;
////////////        Random rnd = new Random();
////////////        PedestrianCrossing c = crossings.get(rnd.nextInt(crossings.size()));
////////////
////////////        // شروع و پایان روی گذرگاه (یک سمت به سمت دیگر)
////////////        Point start = new Point(c.getIntersection().getPosition().getX() - 20, c.getIntersection().getPosition().getY());
////////////        Point end   = new Point(c.getIntersection().getPosition().getX() + 20, c.getIntersection().getPosition().getY());
////////////
////////////        Pedestrian p = new Pedestrian("P-" + System.nanoTime(), start, end, c);
////////////        pedestrians.add(p);
////////////    }
////////////
////////////    // ---------- آپدیت عابر ----------
////////////    private void updatePedestrians() {
////////////        pedestrians.removeIf(Pedestrian::isFinished); // حذف عابرهایی که رسیدن
////////////        for (Pedestrian p : pedestrians) {
////////////            p.update();
////////////        }
////////////    }
////////////
////////////    // ---------- توقف ماشین‌ها جلوی عابر ----------
////////////    private void handleVehiclesNearPedestrians() {
////////////        for (Pedestrian ped : pedestrians) {
////////////            Point pos = ped.getPosition();
////////////            for (Vehicle v : vehicles) {
////////////                if (v.getCurrentLane() == null) continue;
////////////
////////////                Point vp = v.getCurrentLane().getPositionAt(v.getPositionInLane());
////////////                double dist = Math.hypot(vp.getX() - pos.getX(), vp.getY() - pos.getY());
////////////
////////////                if (dist < 30) { // 🚦 توقف ماشین جلوی عابر
////////////                    v.setTargetSpeed(0);
////////////                }
////////////            }
////////////        }
////////////    }
////////////
////////////    // ---------- منطق سبقت ----------
////////////    private void checkOvertaking() {
////////////        for (Vehicle v : vehicles) {
////////////            if (v.isOvertaking()) {
////////////                boolean clear = true;
////////////                for (Vehicle other : vehicles) {
////////////                    if (other == v) continue;
////////////                    if (other.getCurrentLane() == v.getCurrentLane()) {
////////////                        double dist = other.getPositionInLane() - v.getPositionInLane();
////////////                        if (dist > 0 && dist < 20) { clear = false; break; }
////////////                    }
////////////                }
////////////                if (clear) v.finishOvertaking();
////////////                continue;
////////////            }
////////////
////////////            Vehicle front = findFrontVehicle(v);
////////////            if (front != null) {
////////////                double gap = front.getPositionInLane() - v.getPositionInLane();
////////////                if (gap > 0 && gap < 20) {
////////////                    Lane left = v.getCurrentLane().getLeftAdjacentLane();
////////////                    if (left != null) v.startOvertaking(left);
////////////                }
////////////            }
////////////        }
////////////    }
////////////
////////////    private Vehicle findFrontVehicle(Vehicle v) {
////////////        Vehicle closest = null;
////////////        double minDist = Double.MAX_VALUE;
////////////        for (Vehicle other : vehicles) {
////////////            if (other == v) continue;
////////////            if (other.getCurrentLane() == v.getCurrentLane()) {
////////////                double dist = other.getPositionInLane() - v.getPositionInLane();
////////////                if (dist > 0 && dist < minDist) {
////////////                    minDist = dist;
////////////                    closest = other;
////////////                }
////////////            }
////////////        }
////////////        return closest;
////////////    }
////////////
////////////
////////////
////////////    Random rnd = new Random();
////////////if (rnd.nextDouble() < 0.03) {
////////////        // تصادف واقعی (3 درصد احتمال)
////////////    } else {
////////////        // فقط ترمز کن
////////////        v1.setTargetSpeed(0);
////////////        v2.setTargetSpeed(0);
////////////    }
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
////
