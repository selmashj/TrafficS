package simulation; // // پکیج شبیه‌سازی

import core.*; // // Vehicle/State/Direction
import infrastructure.*; // // CityMap/Lane/Road
import trafficcontrol.*; // // چراغ/وضعیت
import pedestrian.*; // // عابر/گذرگاه

import java.util.*; // // لیست‌ها

public class World implements Updatable { // // دنیای شبیه‌سازی
    private final LinkedList<Vehicle> vehicles; // // خودروها
    private final LinkedList<TrafficLight> trafficLights; // // چراغ‌ها
    private final LinkedList<Pedestrian> pedestrians; // // عابرها
    private final LinkedList<PedestrianCrossing> crossings; // // گذرگاه‌ها
    private final CityMap map; // // نقشه
    private double dtSeconds = 0.1; // // گام زمانی

    public World(CityMap map) { // // سازنده
        this.map = map; // // ست نقشه
        this.vehicles = new LinkedList<Vehicle>(); // // لیست خودرو
        this.trafficLights = new LinkedList<TrafficLight>(); // // لیست چراغ
        this.pedestrians = new LinkedList<Pedestrian>(); // // لیست عابر
        this.crossings = new LinkedList<PedestrianCrossing>(); // // لیست گذرگاه
    }

    public void setDtSeconds(double dt) { // // ست dt
        if (dt <= 0) dt = 0.1; // // حداقل
        this.dtSeconds = dt; // // ذخیره
        for (int i = 0; i < vehicles.size(); i++) vehicles.get(i).setDtSeconds(dt); // // همگام‌سازی
    }

    public void addVehicle(Vehicle v){ if (v != null){ v.setDtSeconds(dtSeconds); vehicles.add(v);} } // // افزودن خودرو
    public void addTrafficLight(TrafficLight tl){ if (tl != null) trafficLights.add(tl); } // // افزودن چراغ
    public void addPedestrian(Pedestrian p){ if (p != null) pedestrians.add(p); } // // افزودن عابر
    public void addCrossing(PedestrianCrossing c){ if (c != null) crossings.add(c); } // // افزودن گذرگاه

    public CityMap getMap(){ return map; } // // گتر نقشه
    public List<Vehicle> getVehicles(){ return vehicles; } // // گتر خودروها
    public List<TrafficLight> getTrafficLights(){ return trafficLights; } // // گتر چراغ‌ها
    public List<Pedestrian> getPedestrians(){ return pedestrians; } // // گتر عابرها
    public List<PedestrianCrossing> getCrossings(){ return crossings; } // // گتر گذرگاه‌ها

    @Override
    public void update() { // // تیک شبیه‌سازی
        // ۱) آپدیت چراغ‌ها //
        for (int i = 0; i < trafficLights.size(); i++) trafficLights.get(i).update(); // // آپدیت چراغ

        // ۲) تعیین سرعت هدف بر اساس چراغِ انتهای لِین و خودروی جلویی //
        // ساخت نگاشت «لِین → خودروها به ترتیب حرکت» //
        HashMap<Lane, ArrayList<Vehicle>> byLane = new HashMap<Lane, ArrayList<Vehicle>>(); // // مپ لِین به لیست
        for (int i = 0; i < vehicles.size(); i++) { // // حلقه خودروها
            Vehicle v = vehicles.get(i); // // خودرو
            Lane   l = v.getCurrentLane(); // // لِین
            if (l == null) continue; // // اگر لِین ندارد
            ArrayList<Vehicle> list = byLane.get(l); // // لیست لِین
            if (list == null){ list = new ArrayList<Vehicle>(); byLane.put(l, list);} // // ایجاد
            list.add(v); // // افزودن
        }
        // مرتب‌سازی بر اساس «ترتیب حرکت واقعی» //
        for (Map.Entry<Lane, ArrayList<Vehicle>> e : byLane.entrySet()) { // // پیمایش لِین‌ها
            final Lane lane = e.getKey(); // // لِین
            final int sign = (lane.getDirection()==Direction.EAST || lane.getDirection()==Direction.SOUTH) ? (+1):(-1); // // علامت
            Collections.sort(e.getValue(), new Comparator<Vehicle>() { // // مرتب‌سازی
                @Override public int compare(Vehicle a, Vehicle b){ // // مقایسه
                    double pa = a.getPositionInLane(); double pb = b.getPositionInLane(); // // موضع
                    return sign>0 ? Double.compare(pa, pb) : Double.compare(pb, pa); // // جلو→عقب
                }
            });
        }

        // محاسبهٔ targetSpeed با توجه به چراغ و خودروی جلویی //
        for (Map.Entry<Lane, ArrayList<Vehicle>> e : byLane.entrySet()) { // // برای هر لِین
            Lane lane = e.getKey(); // // لِین
            ArrayList<Vehicle> list = e.getValue(); // // خودروهای همان لِین
            double L = lane.getLength(); // // طول لِین
            boolean forward = (lane.getDirection()==Direction.EAST || lane.getDirection()==Direction.SOUTH); // // جهت
            for (int idx = 0; idx < list.size(); idx++) { // // حلقه خودروهای لِین
                Vehicle v = list.get(idx); // // خودرو
                double target = 42.0; // // سرعت پایه

                // فاصله تا تقاطع انتهاییِ همین جهت //
                double distToEnd = forward ? (L - v.getPositionInLane()) : (v.getPositionInLane()); // // فاصله تا انتها
                Intersection endInter = forward ? lane.getParentRoad().getEndIntersection() : lane.getParentRoad().getStartIntersection(); // // تقاطع انتها
                TrafficControlDevice dev = endInter.getControl(lane.getDirection()); // // کنترل همین جهت
                if (dev instanceof TrafficLight) { // // چراغ؟
                    LightState st = ((TrafficLight) dev).getState(); // // وضعیت
                    if (distToEnd < 55) { // // ناحیهٔ ترمز
                        if (st == LightState.RED) target = 0; // // قرمز = توقف
                        else if (st == LightState.YELLOW) target = Math.min(target, 18); // // زرد = کاهش
                    }
                }

                // رعایت فاصله از خودروی جلویی //
                if (idx < list.size() - 1) { // // اگر جلویی وجود دارد
                    Vehicle front = list.get(idx + 1); // // خودروی جلو
                    double gap = forward ? (front.getPositionInLane() - v.getPositionInLane())
                            : (v.getPositionInLane() - front.getPositionInLane()); // // فاصلهٔ طولی
                    double minGap = 20 + v.getSpeed() * 0.25; // // حداقل فاصلهٔ ایمن
                    if (gap < minGap) { // // خیلی نزدیک
                        target = Math.min(target, Math.max(0, (gap - 8))); // // کاهش هدف
                    }
                }

                v.setTargetSpeed(target); // // اعمال هدف
            }
        }

        // ۳) آپدیت حرکت خودروها //
        for (int i = 0; i < vehicles.size(); i++) vehicles.get(i).update(); // // آپدیت

        // ۴) آپدیت سادهٔ عابرها (دمو) //
        for (int i = 0; i < pedestrians.size(); i++) pedestrians.get(i).update(); // // آپدیت عابر
    }
}









































//7777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777
//
//package simulation; // پکیج شبیه‌ساز //
//
//import core.Vehicle; // خودرو //
//import core.Point; // نقطه //
//import infrastructure.CityMap; // نقشه //
//import infrastructure.Intersection; // تقاطع //
//import trafficcontrol.*; // چراغ و LightState و ... //
//import pedestrian.Pedestrian; // عابر //
//
//import java.util.*; // Collections //
//
//public class World implements Updatable { // دنیا //
//    private final CityMap map; // نقشه //
//    private final List<Vehicle> vehicles = new ArrayList<Vehicle>(); // خودروها //
//    private final List<Pedestrian> pedestrians = new ArrayList<Pedestrian>(); // عابرها //
//    private final List<TrafficLight> trafficLights = new ArrayList<TrafficLight>(); // همه چراغ‌ها //
//
//    // گروه‌های چراغ هماهنگ برای هر تقاطع: [N,S,E,W] //
//    private final List<TrafficLight[]> syncedLights = new ArrayList<TrafficLight[]>(); // //
//
//    private double dtSeconds = 0.1; // گام زمانی //
//
//    // نمایش برچسب تصادف //
//    public static class Accident { // کلاس دیتای تصادف //
//        public final Point position; // محل //
//        public int ticksLeft; // مدت باقی‌مانده نمایش //
//        public Accident(Point pos, int durationTicks){ this.position = pos; this.ticksLeft = durationTicks; } // سازنده //
//    }
//    private final LinkedList<Accident> activeAccidents = new LinkedList<Accident>(); // لیست برچسب‌ها //
//
//    public World(CityMap map) { // سازنده //
//        this.map = map; // ذخیره //
//    }
//
//    // --- getters / setters --- //
//    public CityMap getMap(){ return map; } // گتر نقشه //
//    public List<Vehicle> getVehicles(){ return vehicles; } // گتر خودروها //
//    public List<Pedestrian> getPedestrians(){ return pedestrians; } // گتر عابرها //
//    public List<TrafficLight> getTrafficLights(){ return trafficLights; } // گتر چراغ‌ها //
//    public void setDtSeconds(double dt){ if (dt > 0) this.dtSeconds = dt; } // تنظیم dt //
//
//    // --- add methods --- //
//    public void addVehicle(Vehicle v){ if (v != null) vehicles.add(v); } // افزودن خودرو //
//    public void addPedestrian(Pedestrian p){ if (p != null) pedestrians.add(p); } // ✅ فیکس: افزودن عابر //
//    public void addTrafficLight(TrafficLight t){ if (t != null) trafficLights.add(t); } // افزودن چراغ //
//    public void addSynchronizedLights(TrafficLight n, TrafficLight s, TrafficLight e, TrafficLight w){ // ✅ فیکس: ثبت گروه هماهنگ //
//        syncedLights.add(new TrafficLight[]{n, s, e, w}); // //
//    }
//
//    // خروجی برای رندر برچسب‌ها //
//    public List<Accident> getActiveAccidents(){ return new ArrayList<Accident>(activeAccidents); } // کپی ایمن //
//
//    @Override
//    public void update() { // تیک //
//        // ۱) آپدیت چراغ‌های هماهنگ //
//        for (int gi = 0; gi < syncedLights.size(); gi++) {
//            TrafficLight[] group = syncedLights.get(gi); // گروه //
//            TrafficLight n = group[0], s = group[1], e = group[2], w = group[3]; // اعضا //
//            n.update(); // فقط north جلو می‌رود //
//            s.setState(n.getState()); // south مثل north //
//
//            if (n.getState() == LightState.GREEN || n.getState() == LightState.YELLOW) { // اگر NS سبز/زرد //
//                e.setState(LightState.RED); // EW قرمز //
//                w.setState(LightState.RED); // //
//            } else { // اگر NS قرمز //
//                e.update(); // E جلو برود //
//                w.setState(e.getState()); // W مثل E //
//            }
//        }
//
//        // ۲) چراغ‌هایی که عضو هیچ گروهی نیستند، مستقل آپدیت شوند //
//        for (int i = 0; i < trafficLights.size(); i++) {
//            TrafficLight tl = trafficLights.get(i);
//            if (!isInSyncedGroup(tl)) tl.update();
//        }
//
//        // ۳) منطق ساده تعامل خودرو با چراغ انتهای لاین (Red=Stop / Yellow=Slow) //
//        for (int i = 0; i < vehicles.size(); i++) {
//            Vehicle v = vehicles.get(i);
//            double target = 42.0; // سرعت پایه //
//            if (v.getCurrentLane() != null) {
//                double len = v.getCurrentLane().getLength();
//                double dist = len - v.getPositionInLane();
//                if (dist < 45) { // نزدیک انتهای لاین //
//                    Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection();
//                    TrafficControlDevice dev = end.getControl(v.getCurrentLane().getDirection());
//                    if (dev instanceof TrafficLight) {
//                        LightState st = ((TrafficLight)dev).getState();
//                        if (st == LightState.RED) target = 0;        // قرمز: توقف //
//                        else if (st == LightState.YELLOW) target = Math.min(target, 18); // زرد: کند //
//                    }
//                }
//            }
//            v.setTargetSpeed(target); // اعمال //
//        }
//
//        // ۴) آپدیت حرکت خودروها و عابرها //
//        for (int i = 0; i < vehicles.size(); i++) vehicles.get(i).update(); // خودرو //
//        for (int i = 0; i < pedestrians.size(); i++) pedestrians.get(i).update(); // عابر //
//
//        // ۵) مدیریت ثبت/حذف برچسب تصادف (کوتاه) //
//        detectAndRecordAccidents(); // تشخیص //
//        decayAccidents();           // کاهش تایمر //
//    }
//
//    private boolean isInSyncedGroup(TrafficLight tl){ // بررسی عضویت چراغ //
//        for (int gi = 0; gi < syncedLights.size(); gi++) {
//            TrafficLight[] g = syncedLights.get(gi);
//            for (int k = 0; k < g.length; k++) if (g[k] == tl) return true;
//        }
//        return false;
//    }
//
//    private void detectAndRecordAccidents(){ // تشخیص خیلی ساده //
//        final double THRESH = 5.0; // آستانه نزدیکی //
//        final int DURATION = Math.max(4, (int)Math.round(0.8 / Math.max(dtSeconds, 0.01))); // ~0.8s نمایش //
//
//        for (int i = 0; i < vehicles.size(); i++) {
//            Vehicle a = vehicles.get(i);
//            if (a.getCurrentLane() == null) continue;
//            for (int j = i+1; j < vehicles.size(); j++) {
//                Vehicle b = vehicles.get(j);
//                if (b.getCurrentLane() != a.getCurrentLane()) continue;
//                if (Math.abs(a.getPositionInLane() - b.getPositionInLane()) <= THRESH) {
//                    Point p = a.getCurrentLane().getPositionAt((a.getPositionInLane() + b.getPositionInLane()) * 0.5);
//                    boolean dup = false;
//                    for (int k = 0; k < activeAccidents.size(); k++) {
//                        Point q = activeAccidents.get(k).position;
//                        int dx = q.getX()-p.getX(), dy = q.getY()-p.getY();
//                        if (dx*dx + dy*dy < 16*16) { dup = true; break; }
//                    }
//                    if (!dup) activeAccidents.add(new Accident(p, DURATION));
//                }
//            }
//        }
//    }
//
//    private void decayAccidents(){ // کم‌کردن تایمر نمایش //
//        for (int i = activeAccidents.size()-1; i >= 0; i--) {
//            Accident a = activeAccidents.get(i);
//            a.ticksLeft--;
//            if (a.ticksLeft <= 0) activeAccidents.remove(i);
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
////package simulation; // پکیج شبیه‌سازی //
////
////import core.Vehicle; // خودرو //
////import core.Point; // نقطه //
////import infrastructure.CityMap; // نقشه //
////import trafficcontrol.*; // چراغ‌ها //
////
////import java.util.LinkedList; // لیست پیوندی //
////import java.util.List; // اینترفیس لیست //
////import java.util.ArrayList; // آرایه‌لیست //
////
////public class World implements Updatable { // دنیای شبیه‌سازی //
////    private final LinkedList<Vehicle> vehicles; // فهرست خودروها //
////    private final LinkedList<TrafficLight> trafficLights; // فهرست چراغ‌ها //
////    private final LinkedList<pedestrian.Pedestrian> pedestrians; // فهرست عابرها //
////    private final CityMap map; // نقشه //
////    private double dtSeconds = 0.1; // dt پیش‌فرض //
////
////    // --- ثبت “accident”های فعال برای رندر --- //
////    public static class Accident { // کلاس عمومی برای دسترسی پنل //
////        public final Point position; // محل //
////        public int ticksLeft; // تعداد تیک باقی‌مانده //
////        public Accident(Point p, int durationTicks) { this.position = p; this.ticksLeft = durationTicks; } // سازنده //
////    }
////    private final LinkedList<Accident> activeAccidents = new LinkedList<Accident>(); // لیست تصادف‌ها //
////
////    public World(CityMap map) { // سازنده //
////        this.map = map; // ذخیره نقشه //
////        this.vehicles = new LinkedList<Vehicle>(); // لیست خودرو //
////        this.trafficLights = new LinkedList<TrafficLight>(); // لیست چراغ //
////        this.pedestrians = new LinkedList<pedestrian.Pedestrian>(); // لیست عابر //
////    }
////
////    public void setDtSeconds(double dt) { // ست dt از سمت Clock //
////        if (dt <= 0) dt = 0.1; // ایمنی //
////        this.dtSeconds = dt; // ذخیره //
////        for (int i = 0; i < vehicles.size(); i++) { // همگام‌سازی dt خودروها //
////            vehicles.get(i).setDtSeconds(dt); // تنظیم dt //
////        }
////    }
////
////    public CityMap getMap() { return map; } // گتر نقشه //
////    public List<Vehicle> getVehicles() { return vehicles; } // گتر خودروها //
////    public List<TrafficLight> getTrafficLights() { return trafficLights; } // گتر چراغ‌ها //
////    public void addVehicle(Vehicle v) { if (v != null) { v.setDtSeconds(dtSeconds); vehicles.add(v); } } // افزودن خودرو //
////    public void addTrafficLight(TrafficLight tl) { if (tl != null) trafficLights.add(tl); } // افزودن چراغ //
////
////    // ✅ برای SimulatorPanel: لیست تصادف‌های فعال را بده (کپی ایمن) //
////    public List<Accident> getActiveAccidents() { return new ArrayList<Accident>(activeAccidents); } // خروجی //
////
////    @Override
////    public void update() { // تیک شبیه‌سازی //
////        // ۱) آپدیت چراغ‌ها //
////        for (int i = 0; i < trafficLights.size(); i++) { trafficLights.get(i).update(); }
////
////        // ۲) منطق ساده سرعت نسبت به چراغ انتهای لِین //
////        for (int i = 0; i < vehicles.size(); i++) {
////            Vehicle v = vehicles.get(i);
////            double target = 42.0; // سرعت پایه //
////            if (v.getCurrentLane() != null) { // اگر روی لِین است //
////                double laneLen = v.getCurrentLane().getLength(); // طول لِین //
////                double distToEnd = laneLen - v.getPositionInLane(); // فاصله تا انتها //
////                if (distToEnd < 45) { // نزدیک انتها //
////                    infrastructure.Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection(); // تقاطع //
////                    TrafficControlDevice dev = end.getControl(v.getCurrentLane().getDirection()); // کنترل //
////                    if (dev instanceof TrafficLight) {
////                        LightState st = ((TrafficLight) dev).getState();
////                        if (st == LightState.RED) target = 0; // قرمز: توقف //
////                        else if (st == LightState.YELLOW) target = Math.min(target, 18); // زرد: کند //
////                    }
////                }
////            }
////            v.setTargetSpeed(target); // اعمال //
////        }
////
////        // ۳) حرکت خودروها //
////        for (int i = 0; i < vehicles.size(); i++) { vehicles.get(i).update(); }
////
////        // ۴) تشخیص ساده‌ی تصادف و ثبت “accident” (در همان محل) فقط به‌ندرت //
////        detectAndRecordAccidents(); // ثبت/افزودن //
////        decayAccidents(); // کم‌کردن تایمر و حذف //
////    }
////
////    // تشخیص خیلی ساده: دو خودرو روی یک لِین و فاصلهٔ طولی خیلی کم → یکبار “accident” ثبت کن //
////    private void detectAndRecordAccidents() {
////        final double THRESH = 5.0; // آستانه نزدیکی (پیکسل) //
////        final int DURATION = Math.max(4, (int)Math.round(0.8 / Math.max(dtSeconds, 0.01))); // ~0.8s نمایش //
////
////        for (int i = 0; i < vehicles.size(); i++) {
////            Vehicle a = vehicles.get(i);
////            if (a.getCurrentLane() == null) continue;
////            for (int j = i + 1; j < vehicles.size(); j++) {
////                Vehicle b = vehicles.get(j);
////                if (b.getCurrentLane() != a.getCurrentLane()) continue; // فقط لِین مشترک //
////                if (Math.abs(a.getPositionInLane() - b.getPositionInLane()) <= THRESH) { // خیلی نزدیک //
////                    // نقطهٔ میانگین برای برچسب //
////                    Point p = a.getCurrentLane().getPositionAt((a.getPositionInLane() + b.getPositionInLane()) * 0.5);
////                    // اگر قبلاً نزدیک همین نقطه ثبت شده، دوباره ثبت نکن //
////                    boolean duplicate = false;
////                    for (int k = 0; k < activeAccidents.size(); k++) {
////                        Point q = activeAccidents.get(k).position;
////                        int dx = q.getX() - p.getX();
////                        int dy = q.getY() - p.getY();
////                        if (dx*dx + dy*dy < 16*16) { duplicate = true; break; } // نزدیکی مکانی //
////                    }
////                    if (!duplicate) {
////                        activeAccidents.add(new Accident(p, DURATION)); // افزودن //
////                    }
////                }
////            }
////        }
////    }
////
////    // کاهش زمان باقی‌ماندهٔ برچسب‌ها و حذف موارد تمام‌شده //
////    private void decayAccidents() {
////        for (int i = activeAccidents.size() - 1; i >= 0; i--) {
////            Accident a = activeAccidents.get(i);
////            a.ticksLeft--;
////            if (a.ticksLeft <= 0) activeAccidents.remove(i);
////        }
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
////package simulation; // پکیج شبیه‌سازی //
////
////import core.Vehicle; // خودرو //
////import pedestrian.Pedestrian; // عابر //
////import trafficcontrol.TrafficLight; // چراغ //
////import trafficcontrol.LightState; // وضعیت چراغ //
////
////import java.util.ArrayList; //
////import java.util.List; //
////
////public class World implements Updatable { // دنیای شبیه‌سازی //
////    private final List<Vehicle> vehicles = new ArrayList<Vehicle>(); // لیست خودروها //
////    private final List<Pedestrian> pedestrians = new ArrayList<Pedestrian>(); // لیست عابرها //
////    private final List<TrafficLight> trafficLights = new ArrayList<TrafficLight>(); // همه چراغ‌ها //
////    private final List<TrafficLight[]> syncedLights = new ArrayList<TrafficLight[]>(); // گروه‌های چراغ هماهنگ //
////
////    private double dtSeconds = 0.016; // ✅ گام زمانی (برای سازگاری با setDtSeconds) //
////
////    public World() { /* سازنده پیش‌فرض */ } // سازنده بدون پارامتر //
////
////    public void setDtSeconds(double dt) { this.dtSeconds = dt; } // ✅ ست کردن dt //
////    public double getDtSeconds() { return dtSeconds; } // گرفتن dt در صورت نیاز //
////
////    public void addVehicle(Vehicle v) { vehicles.add(v); } // افزودن خودرو //
////    public void addPedestrian(Pedestrian p) { pedestrians.add(p); } // افزودن عابر //
////    public void addTrafficLight(TrafficLight t) { trafficLights.add(t); } // افزودن چراغ //
////
////    public List<Vehicle> getVehicles() { return vehicles; } // دسترسی خودروها //
////    public List<Pedestrian> getPedestrians() { return pedestrians; } // دسترسی عابرها //
////    public List<TrafficLight> getTrafficLights() { return trafficLights; } // دسترسی چراغ‌ها //
////
////    public void addSynchronizedLights(TrafficLight n, TrafficLight s, TrafficLight e, TrafficLight w) { // ثبت گروه //
////        syncedLights.add(new TrafficLight[]{n, s, e, w}); // ذخیره گروه //
////    }
////
////    @Override
////    public void update() { // تیک شبیه‌ساز //
////        // آپدیت خودروها //
////        for (int i = 0; i < vehicles.size(); i++) { vehicles.get(i).update(); } // بروزرسانی تمام خودروها //
////
////        // آپدیت عابرها //
////        for (int i = 0; i < pedestrians.size(); i++) { pedestrians.get(i).update(); } // بروزرسانی عابرها //
////
////        // آپدیت گروه‌های هماهنگ چراغ‌ها //
////        for (int gi = 0; gi < syncedLights.size(); gi++) { // پیمایش گروه‌ها //
////            TrafficLight[] g = syncedLights.get(gi); // گرفتن گروه //
////            TrafficLight n = g[0]; TrafficLight s = g[1]; TrafficLight e = g[2]; TrafficLight w = g[3]; // اعضای گروه //
////
////            n.update(); // فقط north را جلو می‌بریم //
////            s.setState(n.getState()); // south همسان north //
////
////            if (n.getState() == LightState.GREEN || n.getState() == LightState.YELLOW) { // اگر NS سبز/زرد //
////                e.setState(LightState.RED); // EW قرمز //
////                w.setState(LightState.RED); // //
////            } else { // اگر NS قرمز //
////                e.update(); // E چرخه خودش //
////                w.setState(e.getState()); // W همسان E //
////            }
////        }
////
////        // چراغ‌های غیرعضو گروه‌ها (در صورت وجود) //
////        for (int i = 0; i < trafficLights.size(); i++) { // پیمایش همه چراغ‌ها //
////            TrafficLight tl = trafficLights.get(i); // چراغ فعلی //
////            if (!isInSyncedGroup(tl)) { tl.update(); } // اگر در گروه نیست، مستقل آپدیت شود //
////        }
////    }
////
////    private boolean isInSyncedGroup(TrafficLight tl) { // بررسی عضویت چراغ در گروه //
////        for (int gi = 0; gi < syncedLights.size(); gi++) { // پیمایش گروه‌ها //
////            TrafficLight[] g = syncedLights.get(gi); // //
////            for (int k = 0; k < g.length; k++) { if (g[k] == tl) return true; } // تطبیق مرجع //
////        }
////        return false; // عضو هیچ گروهی نیست //
////    }
////}
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
//
//
//
//
//
//
//
////
////
////package simulation;
////
////import core.Vehicle;
////import core.Pedestrian;
////import trafficcontrol.TrafficLight;
////import trafficcontrol.LightState;
////
////import java.util.*;
////
////public class World implements Updatable {
////    private final List<Vehicle> vehicles = new ArrayList<>();
////    private final List<Pedestrian> pedestrians = new ArrayList<>();
////    private final List<TrafficLight> trafficLights = new ArrayList<>();
////
////    // 🔹 گروه‌های چراغ هماهنگ (N/S , E/W)
////    private final List<TrafficLight[]> syncedLights = new ArrayList<>();
////
////    public void addVehicle(Vehicle v) {
////        vehicles.add(v);
////    }
////
////    public void addPedestrian(Pedestrian p) {
////        pedestrians.add(p);
////    }
////
////    public void addTrafficLight(TrafficLight t) {
////        trafficLights.add(t);
////    }
////
////    public List<Vehicle> getVehicles() { return vehicles; }
////    public List<Pedestrian> getPedestrians() { return pedestrians; }
////    public List<TrafficLight> getTrafficLights() { return trafficLights; }
////
////    // متد جدید برای اضافه کردن گروه چراغ‌های هماهنگ
////    public void addSynchronizedLights(TrafficLight n, TrafficLight s, TrafficLight e, TrafficLight w) {
////        syncedLights.add(new TrafficLight[]{n, s, e, w});
////    }
////
////    // ---------------- آپدیت ----------------
////    @Override
////    public void update() {
////        // 🚗 آپدیت خودروها
////        for (Vehicle v : vehicles) {
////            v.update();
////        }
////
////        // 🚶 آپدیت عابرها
////        for (Pedestrian p : pedestrians) {
////            p.update();
////        }
////
////        // 🚦 آپدیت گروه‌های چراغ هماهنگ
////        for (TrafficLight[] group : syncedLights) {
////            TrafficLight n = group[0];
////            TrafficLight s = group[1];
////            TrafficLight e = group[2];
////            TrafficLight w = group[3];
////
////            // فقط north رو آپدیت می‌کنیم
////            n.update();
////            s.setState(n.getState()); // south مثل north میشه
////
////            // وقتی north/south سبز یا زرد بودن → east/west قرمز
////            if (n.getState() == LightState.GREEN || n.getState() == LightState.YELLOW) {
////                e.setState(LightState.RED);
////                w.setState(LightState.RED);
////            }
////            // وقتی north قرمز شد → east/west وارد چرخه میشن
////            else if (n.getState() == LightState.RED) {
////                e.update();
////                w.setState(e.getState());
////            }
////        }
////
////        // چراغ‌های دیگه که جزو sync group نیستن (مثلا تستی)
////        for (TrafficLight tl : trafficLights) {
////            if (!isInSyncedGroup(tl)) {
////                tl.update();
////            }
////        }
////    }
////
////    // بررسی اینکه چراغ جزو گروه هماهنگ هست یا نه
////    private boolean isInSyncedGroup(TrafficLight tl) {
////        for (TrafficLight[] group : syncedLights) {
////            for (TrafficLight t : group) {
////                if (t == tl) return true;
////            }
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
////
////
////
////
////
//////package simulation;
//////
//////import core.Vehicle;
//////import core.Point;
//////import infrastructure.CityMap;
//////import trafficcontrol.TrafficLight;
//////import trafficcontrol.LightState;
//////import infrastructure.Lane;
//////import pedestrian.Pedestrian;
//////import pedestrian.PedestrianCrossing;
//////
//////import java.util.*;
//////
//////public class World implements Updatable {
//////    private final LinkedList<Vehicle> vehicles;
//////    private final LinkedList<TrafficLight> trafficLights;
//////    private final LinkedList<Pedestrian> pedestrians;
//////    private final CityMap map;
//////    private double dtSeconds = 0.1;
//////
//////    // ---------- مدیریت تصادف ----------
//////    public static class Accident {
//////        public double x, y;
//////        public long endTimeMs;
//////        public Accident(double x, double y, long endTimeMs) {
//////            this.x = x; this.y = y; this.endTimeMs = endTimeMs;
//////        }
//////    }
//////    private final List<Accident> activeAccidents = new ArrayList<>();
//////
//////    // ---------- مدیریت عابر ----------
//////    private final List<PedestrianCrossing> crossings = new ArrayList<>();
//////    private long lastPedestrianSpawnTime = 0;
//////    private static final long PEDESTRIAN_INTERVAL_MS = 17000; // هر ۱۷ ثانیه
//////
//////    public World(CityMap map) {
//////        this.map = map;
//////        this.vehicles = new LinkedList<>();
//////        this.trafficLights = new LinkedList<>();
//////        this.pedestrians = new LinkedList<>();
//////    }
//////
//////    public void setDtSeconds(double dt) {
//////        if (dt <= 0) dt = 0.1;
//////        this.dtSeconds = dt;
//////        for (Vehicle v : vehicles) {
//////            v.setDtSeconds(dt);
//////        }
//////    }
//////
//////    public void addVehicle(Vehicle v) {
//////        if (v != null) {
//////            v.setDtSeconds(dtSeconds);
//////            vehicles.add(v);
//////        }
//////    }
//////
//////    public void addTrafficLight(TrafficLight tl) {
//////        if (tl != null) trafficLights.add(tl);
//////    }
//////
//////    public void addPedestrian(Pedestrian p) {
//////        if (p != null) pedestrians.add(p);
//////    }
//////
//////    public void addCrossing(PedestrianCrossing c) {
//////        if (c != null) crossings.add(c);
//////    }
//////
//////    public CityMap getMap() { return map; }
//////    public List<Vehicle> getVehicles() { return vehicles; }
//////    public List<TrafficLight> getTrafficLights() { return trafficLights; }
//////    public List<Pedestrian> getPedestrians() { return pedestrians; }
//////    public List<Accident> getActiveAccidents() { return activeAccidents; }
//////
//////    @Override
//////    public void update() {
//////        // ۱) آپدیت چراغ‌ها
//////        for (TrafficLight tl : trafficLights) tl.update();
//////
//////        // ۲) تعیین سرعت هدف خودروها
//////        for (Vehicle v : vehicles) {
//////            double target = 42.0;
//////
//////            double laneLen = (v.getCurrentLane() != null) ? v.getCurrentLane().getLength() : 0;
//////            double distToEnd = laneLen - v.getPositionInLane();
//////
//////            if (v.getCurrentLane() != null && distToEnd < 45) {
//////                infrastructure.Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection();
//////                trafficcontrol.TrafficControlDevice dev = end.getControl(v.getCurrentLane().getDirection());
//////                if (dev instanceof TrafficLight) {
//////                    LightState st = ((TrafficLight) dev).getState();
//////                    if (st == LightState.RED) target = 0;
//////                    else if (st == LightState.YELLOW) target = Math.min(target, 18);
//////                }
//////            }
//////            v.setTargetSpeed(target);
//////        }
//////
//////        // ۳) منطق سبقت
//////        checkOvertaking();
//////
//////        // ۴) آپدیت خودروها
//////        for (Vehicle v : vehicles) v.update();
//////
//////        // ۵) بررسی تصادف‌ها
//////        checkCollisions();
//////
//////        // ۶) مدیریت عابرها
//////        spawnPedestrians();
//////        updatePedestrians();
//////        handleVehiclesNearPedestrians();
//////
//////        // ۷) پاک کردن تصادف‌های قدیمی + آزاد کردن ماشین‌ها
//////        handleAccidentRecovery();
//////    }
//////
//////    // ---------- تولید عابر جدید ----------
//////    private void spawnPedestrians() {
//////        long now = System.currentTimeMillis();
//////        if (now - lastPedestrianSpawnTime < PEDESTRIAN_INTERVAL_MS) return;
//////        lastPedestrianSpawnTime = now;
//////
//////        if (crossings.isEmpty()) return;
//////        Random rnd = new Random();
//////        PedestrianCrossing c = crossings.get(rnd.nextInt(crossings.size()));
//////
//////        Point start = new Point(c.getIntersection().getPosition().getX() - 20, c.getIntersection().getPosition().getY());
//////        Point end   = new Point(c.getIntersection().getPosition().getX() + 20, c.getIntersection().getPosition().getY());
//////
//////        Pedestrian p = new Pedestrian("P-" + System.nanoTime(), start, end, c);
//////        pedestrians.add(p);
//////    }
//////
//////    // ---------- آپدیت عابر ----------
//////    private void updatePedestrians() {
//////        pedestrians.removeIf(Pedestrian::isFinished);
//////        for (Pedestrian p : pedestrians) {
//////            p.update();
//////        }
//////    }
//////
//////    // ---------- توقف ماشین‌ها جلوی عابر ----------
//////    private void handleVehiclesNearPedestrians() {
//////        for (Pedestrian ped : pedestrians) {
//////            Point pos = ped.getPosition();
//////            for (Vehicle v : vehicles) {
//////                if (v.getCurrentLane() == null) continue;
//////
//////                Point vp = v.getCurrentLane().getPositionAt(v.getPositionInLane());
//////                double dist = Math.hypot(vp.getX() - pos.getX(), vp.getY() - pos.getY());
//////
//////                if (dist < 30) { // 🚦 توقف ماشین جلوی عابر
//////                    v.setTargetSpeed(0);
//////                }
//////            }
//////        }
//////    }
//////
//////    // ---------- منطق سبقت ----------
//////    private void checkOvertaking() {
//////        for (Vehicle v : vehicles) {
//////            if (v.isOvertaking()) {
//////                boolean clear = true;
//////                for (Vehicle other : vehicles) {
//////                    if (other == v) continue;
//////                    if (other.getCurrentLane() == v.getCurrentLane()) {
//////                        double dist = other.getPositionInLane() - v.getPositionInLane();
//////                        if (dist > 0 && dist < 20) { clear = false; break; }
//////                    }
//////                }
//////                if (clear) v.finishOvertaking();
//////                continue;
//////            }
//////
//////            Vehicle front = findFrontVehicle(v);
//////            if (front != null) {
//////                double gap = front.getPositionInLane() - v.getPositionInLane();
//////                if (gap > 0 && gap < 20) {
//////                    Lane left = v.getCurrentLane().getLeftAdjacentLane();
//////                    if (left != null) v.startOvertaking(left);
//////                }
//////            }
//////        }
//////    }
//////
//////    private Vehicle findFrontVehicle(Vehicle v) {
//////        Vehicle closest = null;
//////        double minDist = Double.MAX_VALUE;
//////        for (Vehicle other : vehicles) {
//////            if (other == v) continue;
//////            if (other.getCurrentLane() == v.getCurrentLane()) {
//////                double dist = other.getPositionInLane() - v.getPositionInLane();
//////                if (dist > 0 && dist < minDist) {
//////                    minDist = dist;
//////                    closest = other;
//////                }
//////            }
//////        }
//////        return closest;
//////    }
//////
//////    // ---------- منطق تصادف ----------
//////    private void checkCollisions() {
//////        int carLength = ui.UIConstants.VEHICLE_LENGTH;
//////        Random rnd = new Random();
//////
//////        for (int i = 0; i < vehicles.size(); i++) {
//////            Vehicle v1 = vehicles.get(i);
//////            for (int j = i + 1; j < vehicles.size(); j++) {
//////                Vehicle v2 = vehicles.get(j);
//////
//////                if (v1.getCurrentLane() == null || v2.getCurrentLane() == null) continue;
//////                if (v1.getCurrentLane() != v2.getCurrentLane()) continue;
//////
//////                double dist = Math.abs(v1.getPositionInLane() - v2.getPositionInLane());
//////
//////                if (dist < carLength * 0.8) {
//////                    // 🚨 فقط ۳٪ احتمال تصادف واقعی
//////                    if (rnd.nextDouble() < 0.03) {
//////                        v1.setTargetSpeed(0); v1.setSpeed(0);
//////                        v2.setTargetSpeed(0); v2.setSpeed(0);
//////
//////                        Point p = v1.getCurrentLane().getPositionAt(
//////                                (v1.getPositionInLane() + v2.getPositionInLane()) / 2.0
//////                        );
//////                        long endTime = System.currentTimeMillis() + 7000;
//////                        activeAccidents.add(new Accident(p.getX(), p.getY(), endTime));
//////                    } else {
//////                        // بقیه فقط ترمز می‌کنن
//////                        v1.setTargetSpeed(0);
//////                        v2.setTargetSpeed(0);
//////                    }
//////                }
//////            }
//////        }
//////    }
//////
//////    // ---------- آزادسازی ماشین‌ها بعد از پایان تصادف ----------
//////    private void handleAccidentRecovery() {
//////        long now = System.currentTimeMillis();
//////        Iterator<Accident> it = activeAccidents.iterator();
//////        while (it.hasNext()) {
//////            Accident a = it.next();
//////            if (a.endTimeMs < now) {
//////                // آزادسازی ماشین‌های نزدیک به محل تصادف
//////                for (Vehicle v : vehicles) {
//////                    if (v.getCurrentLane() == null) continue;
//////                    Point vp = v.getCurrentLane().getPositionAt(v.getPositionInLane());
//////                    double dist = Math.hypot(vp.getX() - a.x, vp.getY() - a.y);
//////                    if (dist < 15) {
//////                        v.setTargetSpeed(42); // دوباره حرکت کنه
//////                    }
//////                }
//////                it.remove();
//////            }
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
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
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
////////package simulation;
////////
////////import core.Vehicle;
////////import core.Point;
////////import infrastructure.CityMap;
////////import trafficcontrol.TrafficLight;
////////import trafficcontrol.LightState;
////////import infrastructure.Lane;
////////import pedestrian.Pedestrian;
////////import pedestrian.PedestrianCrossing;
////////
////////import java.util.*;
////////
////////public class World implements Updatable {
////////    private final LinkedList<Vehicle> vehicles;
////////    private final LinkedList<TrafficLight> trafficLights;
////////    private final LinkedList<Pedestrian> pedestrians;
////////    private final CityMap map;
////////    private double dtSeconds = 0.1;
////////
////////    // ---------- مدیریت تصادف ----------
////////    public static class Accident {
////////        public double x, y;
////////        public long endTimeMs;
////////        public Accident(double x, double y, long endTimeMs) {
////////            this.x = x; this.y = y; this.endTimeMs = endTimeMs;
////////        }
////////    }
////////    private final List<Accident> activeAccidents = new ArrayList<>();
////////
////////    // ---------- مدیریت عابر ----------
////////    private final List<PedestrianCrossing> crossings = new ArrayList<>();
////////    private long lastPedestrianSpawnTime = 0;
////////    private static final long PEDESTRIAN_INTERVAL_MS = 17000; // هر ۱۷ ثانیه
////////
////////    public World(CityMap map) {
////////        this.map = map;
////////        this.vehicles = new LinkedList<>();
////////        this.trafficLights = new LinkedList<>();
////////        this.pedestrians = new LinkedList<>();
////////    }
////////
////////    public void setDtSeconds(double dt) {
////////        if (dt <= 0) dt = 0.1;
////////        this.dtSeconds = dt;
////////        for (Vehicle v : vehicles) {
////////            v.setDtSeconds(dt);
////////        }
////////    }
////////
////////    public void addVehicle(Vehicle v) {
////////        if (v != null) {
////////            v.setDtSeconds(dtSeconds);
////////            vehicles.add(v);
////////        }
////////    }
////////
////////    public void addTrafficLight(TrafficLight tl) {
////////        if (tl != null) trafficLights.add(tl);
////////    }
////////
////////    public void addPedestrian(Pedestrian p) {
////////        if (p != null) pedestrians.add(p);
////////    }
////////
////////    public void addCrossing(PedestrianCrossing c) {
////////        if (c != null) crossings.add(c);
////////    }
////////
////////    public CityMap getMap() { return map; }
////////    public List<Vehicle> getVehicles() { return vehicles; }
////////    public List<TrafficLight> getTrafficLights() { return trafficLights; }
////////    public List<Pedestrian> getPedestrians() { return pedestrians; }
////////    public List<Accident> getActiveAccidents() { return activeAccidents; }
////////
////////    @Override
////////    public void update() {
////////        // ۱) آپدیت چراغ‌ها
////////        for (TrafficLight tl : trafficLights) tl.update();
////////
////////        // ۲) تعیین سرعت هدف خودروها
////////        for (Vehicle v : vehicles) {
////////            double target = 42.0;
////////
////////            double laneLen = (v.getCurrentLane() != null) ? v.getCurrentLane().getLength() : 0;
////////            double distToEnd = laneLen - v.getPositionInLane();
////////
////////            if (v.getCurrentLane() != null && distToEnd < 45) {
////////                infrastructure.Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection();
////////                trafficcontrol.TrafficControlDevice dev = end.getControl(v.getCurrentLane().getDirection());
////////                if (dev instanceof TrafficLight) {
////////                    LightState st = ((TrafficLight) dev).getState();
////////                    if (st == LightState.RED) target = 0;
////////                    else if (st == LightState.YELLOW) target = Math.min(target, 18);
////////                }
////////            }
////////            v.setTargetSpeed(target);
////////        }
////////
////////        // ۳) منطق سبقت
////////        checkOvertaking();
////////
////////        // ۴) آپدیت خودروها
////////        for (Vehicle v : vehicles) v.update();
////////
////////        // ۵) بررسی تصادف‌ها
////////        checkCollisions();
////////
////////        // ۶) مدیریت عابرها
////////        spawnPedestrians();
////////        updatePedestrians();
////////        handleVehiclesNearPedestrians();
////////
////////        // ۷) پاک کردن تصادف‌های قدیمی
////////        long now = System.currentTimeMillis();
////////        activeAccidents.removeIf(a -> a.endTimeMs < now);
////////    }
////////
////////    // ---------- تولید عابر جدید ----------
////////    private void spawnPedestrians() {
////////        long now = System.currentTimeMillis();
////////        if (now - lastPedestrianSpawnTime < PEDESTRIAN_INTERVAL_MS) return;
////////        lastPedestrianSpawnTime = now;
////////
////////        if (crossings.isEmpty()) return;
////////        Random rnd = new Random();
////////        PedestrianCrossing c = crossings.get(rnd.nextInt(crossings.size()));
////////
////////        Point start = new Point(c.getIntersection().getPosition().getX() - 20, c.getIntersection().getPosition().getY());
////////        Point end   = new Point(c.getIntersection().getPosition().getX() + 20, c.getIntersection().getPosition().getY());
////////
////////        Pedestrian p = new Pedestrian("P-" + System.nanoTime(), start, end, c);
////////        pedestrians.add(p);
////////    }
////////
////////    // ---------- آپدیت عابر ----------
////////    private void updatePedestrians() {
////////        pedestrians.removeIf(Pedestrian::isFinished);
////////        for (Pedestrian p : pedestrians) {
////////            p.update();
////////        }
////////    }
////////
////////    // ---------- توقف ماشین‌ها جلوی عابر ----------
////////    private void handleVehiclesNearPedestrians() {
////////        for (Pedestrian ped : pedestrians) {
////////            Point pos = ped.getPosition();
////////            for (Vehicle v : vehicles) {
////////                if (v.getCurrentLane() == null) continue;
////////
////////                Point vp = v.getCurrentLane().getPositionAt(v.getPositionInLane());
////////                double dist = Math.hypot(vp.getX() - pos.getX(), vp.getY() - pos.getY());
////////
////////                if (dist < 30) { // 🚦 توقف ماشین جلوی عابر
////////                    v.setTargetSpeed(0);
////////                }
////////            }
////////        }
////////    }
////////
////////    // ---------- منطق سبقت ----------
////////    private void checkOvertaking() {
////////        for (Vehicle v : vehicles) {
////////            if (v.isOvertaking()) {
////////                boolean clear = true;
////////                for (Vehicle other : vehicles) {
////////                    if (other == v) continue;
////////                    if (other.getCurrentLane() == v.getCurrentLane()) {
////////                        double dist = other.getPositionInLane() - v.getPositionInLane();
////////                        if (dist > 0 && dist < 20) { clear = false; break; }
////////                    }
////////                }
////////                if (clear) v.finishOvertaking();
////////                continue;
////////            }
////////
////////            Vehicle front = findFrontVehicle(v);
////////            if (front != null) {
////////                double gap = front.getPositionInLane() - v.getPositionInLane();
////////                if (gap > 0 && gap < 20) {
////////                    Lane left = v.getCurrentLane().getLeftAdjacentLane();
////////                    if (left != null) v.startOvertaking(left);
////////                }
////////            }
////////        }
////////    }
////////
////////    private Vehicle findFrontVehicle(Vehicle v) {
////////        Vehicle closest = null;
////////        double minDist = Double.MAX_VALUE;
////////        for (Vehicle other : vehicles) {
////////            if (other == v) continue;
////////            if (other.getCurrentLane() == v.getCurrentLane()) {
////////                double dist = other.getPositionInLane() - v.getPositionInLane();
////////                if (dist > 0 && dist < minDist) {
////////                    minDist = dist;
////////                    closest = other;
////////                }
////////            }
////////        }
////////        return closest;
////////    }
////////
////////    // ---------- منطق تصادف ----------
////////    private void checkCollisions() {
////////        int carLength = ui.UIConstants.VEHICLE_LENGTH;
////////        Random rnd = new Random();
////////
////////        for (int i = 0; i < vehicles.size(); i++) {
////////            Vehicle v1 = vehicles.get(i);
////////            for (int j = i + 1; j < vehicles.size(); j++) {
////////                Vehicle v2 = vehicles.get(j);
////////
////////                if (v1.getCurrentLane() == null || v2.getCurrentLane() == null) continue;
////////                if (v1.getCurrentLane() != v2.getCurrentLane()) continue;
////////
////////                double dist = Math.abs(v1.getPositionInLane() - v2.getPositionInLane());
////////
////////                if (dist < carLength * 0.8) {
////////                    // 🚨 فقط ۳٪ احتمال تصادف واقعی
////////                    if (rnd.nextDouble() < 0.03) {
////////                        v1.setTargetSpeed(0); v1.setSpeed(0);
////////                        v2.setTargetSpeed(0); v2.setSpeed(0);
////////
////////                        Point p = v1.getCurrentLane().getPositionAt(
////////                                (v1.getPositionInLane() + v2.getPositionInLane()) / 2.0
////////                        );
////////                        long endTime = System.currentTimeMillis() + 7000;
////////                        activeAccidents.add(new Accident(p.getX(), p.getY(), endTime));
////////                    } else {
////////                        // بقیه فقط ترمز می‌کنن
////////                        v1.setTargetSpeed(0);
////////                        v2.setTargetSpeed(0);
////////                    }
////////                }
////////            }
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
//////
//////
//////
//////
////////package simulation;
////////
////////import core.Vehicle;
////////import core.Point;
////////import infrastructure.CityMap;
////////import trafficcontrol.TrafficLight;
////////import trafficcontrol.LightState;
////////import infrastructure.Lane;
////////import pedestrian.Pedestrian;
////////import pedestrian.PedestrianCrossing;
////////
////////import java.util.*;
////////
////////public class World implements Updatable {
////////    private final LinkedList<Vehicle> vehicles;
////////    private final LinkedList<TrafficLight> trafficLights;
////////    private final LinkedList<Pedestrian> pedestrians;
////////    private final CityMap map;
////////    private double dtSeconds = 0.1;
////////
////////    // ---------- مدیریت تصادف ----------
////////    public static class Accident {
////////        public double x, y;
////////        public long endTimeMs;
////////        public Accident(double x, double y, long endTimeMs) {
////////            this.x = x; this.y = y; this.endTimeMs = endTimeMs;
////////        }
////////    }
////////    private final List<Accident> activeAccidents = new ArrayList<>();
////////
////////    // ---------- مدیریت عابر ----------
////////    private final List<PedestrianCrossing> crossings = new ArrayList<>();
////////    private long lastPedestrianSpawnTime = 0;
////////    private static final long PEDESTRIAN_INTERVAL_MS = 17000; // هر ۱۷ ثانیه
////////
////////    public World(CityMap map) {
////////        this.map = map;
////////        this.vehicles = new LinkedList<>();
////////        this.trafficLights = new LinkedList<>();
////////        this.pedestrians = new LinkedList<>();
////////    }
////////
////////    public void setDtSeconds(double dt) {
////////        if (dt <= 0) dt = 0.1;
////////        this.dtSeconds = dt;
////////        for (Vehicle v : vehicles) {
////////            v.setDtSeconds(dt);
////////        }
////////    }
////////
////////    public void addVehicle(Vehicle v) {
////////        if (v != null) {
////////            v.setDtSeconds(dtSeconds);
////////            vehicles.add(v);
////////        }
////////    }
////////
////////    public void addTrafficLight(TrafficLight tl) {
////////        if (tl != null) trafficLights.add(tl);
////////    }
////////
////////    public void addPedestrian(Pedestrian p) {
////////        if (p != null) pedestrians.add(p);
////////    }
////////
////////    public void addCrossing(PedestrianCrossing c) {
////////        if (c != null) crossings.add(c);
////////    }
////////
////////    public CityMap getMap() { return map; }
////////    public List<Vehicle> getVehicles() { return vehicles; }
////////    public List<TrafficLight> getTrafficLights() { return trafficLights; }
////////    public List<Pedestrian> getPedestrians() { return pedestrians; }
////////    public List<Accident> getActiveAccidents() { return activeAccidents; }
////////
////////    @Override
////////    public void update() {
////////        // ۱) آپدیت چراغ‌ها
////////        for (TrafficLight tl : trafficLights) tl.update();
////////
////////        // ۲) تعیین سرعت هدف خودروها
////////        for (Vehicle v : vehicles) {
////////            double target = 42.0;
////////
////////            double laneLen = (v.getCurrentLane() != null) ? v.getCurrentLane().getLength() : 0;
////////            double distToEnd = laneLen - v.getPositionInLane();
////////
////////            if (v.getCurrentLane() != null && distToEnd < 45) {
////////                infrastructure.Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection();
////////                trafficcontrol.TrafficControlDevice dev = end.getControl(v.getCurrentLane().getDirection());
////////                if (dev instanceof TrafficLight) {
////////                    LightState st = ((TrafficLight) dev).getState();
////////                    if (st == LightState.RED) target = 0;
////////                    else if (st == LightState.YELLOW) target = Math.min(target, 18);
////////                }
////////            }
////////            v.setTargetSpeed(target);
////////        }
////////
////////        // ۳) منطق سبقت
////////        checkOvertaking();
////////
////////        // ۴) آپدیت خودروها
////////        for (Vehicle v : vehicles) v.update();
////////
////////        // ۵) بررسی تصادف‌ها
////////        checkCollisions();
////////
////////        // ۶) مدیریت عابرها
////////        spawnPedestrians();
////////        updatePedestrians();
////////        handleVehiclesNearPedestrians();
////////
////////        // ۷) پاک کردن تصادف‌های قدیمی
////////        long now = System.currentTimeMillis();
////////        activeAccidents.removeIf(a -> a.endTimeMs < now);
////////    }
////////
////////    // ---------- تولید عابر جدید ----------
////////    private void spawnPedestrians() {
////////        long now = System.currentTimeMillis();
////////        if (now - lastPedestrianSpawnTime < PEDESTRIAN_INTERVAL_MS) return;
////////        lastPedestrianSpawnTime = now;
////////
////////        if (crossings.isEmpty()) return;
////////        Random rnd = new Random();
////////        PedestrianCrossing c = crossings.get(rnd.nextInt(crossings.size()));
////////
////////        // شروع و پایان روی گذرگاه (یک سمت به سمت دیگر)
////////        Point start = new Point(c.getIntersection().getPosition().getX() - 20, c.getIntersection().getPosition().getY());
////////        Point end   = new Point(c.getIntersection().getPosition().getX() + 20, c.getIntersection().getPosition().getY());
////////
////////        Pedestrian p = new Pedestrian("P-" + System.nanoTime(), start, end, c);
////////        pedestrians.add(p);
////////    }
////////
////////    // ---------- آپدیت عابر ----------
////////    private void updatePedestrians() {
////////        pedestrians.removeIf(Pedestrian::isFinished); // حذف عابرهایی که رسیدن
////////        for (Pedestrian p : pedestrians) {
////////            p.update();
////////        }
////////    }
////////
////////    // ---------- توقف ماشین‌ها جلوی عابر ----------
////////    private void handleVehiclesNearPedestrians() {
////////        for (Pedestrian ped : pedestrians) {
////////            Point pos = ped.getPosition();
////////            for (Vehicle v : vehicles) {
////////                if (v.getCurrentLane() == null) continue;
////////
////////                Point vp = v.getCurrentLane().getPositionAt(v.getPositionInLane());
////////                double dist = Math.hypot(vp.getX() - pos.getX(), vp.getY() - pos.getY());
////////
////////                if (dist < 30) { // 🚦 توقف ماشین جلوی عابر
////////                    v.setTargetSpeed(0);
////////                }
////////            }
////////        }
////////    }
////////
////////    // ---------- منطق سبقت ----------
////////    private void checkOvertaking() {
////////        for (Vehicle v : vehicles) {
////////            if (v.isOvertaking()) {
////////                boolean clear = true;
////////                for (Vehicle other : vehicles) {
////////                    if (other == v) continue;
////////                    if (other.getCurrentLane() == v.getCurrentLane()) {
////////                        double dist = other.getPositionInLane() - v.getPositionInLane();
////////                        if (dist > 0 && dist < 20) { clear = false; break; }
////////                    }
////////                }
////////                if (clear) v.finishOvertaking();
////////                continue;
////////            }
////////
////////            Vehicle front = findFrontVehicle(v);
////////            if (front != null) {
////////                double gap = front.getPositionInLane() - v.getPositionInLane();
////////                if (gap > 0 && gap < 20) {
////////                    Lane left = v.getCurrentLane().getLeftAdjacentLane();
////////                    if (left != null) v.startOvertaking(left);
////////                }
////////            }
////////        }
////////    }
////////
////////    private Vehicle findFrontVehicle(Vehicle v) {
////////        Vehicle closest = null;
////////        double minDist = Double.MAX_VALUE;
////////        for (Vehicle other : vehicles) {
////////            if (other == v) continue;
////////            if (other.getCurrentLane() == v.getCurrentLane()) {
////////                double dist = other.getPositionInLane() - v.getPositionInLane();
////////                if (dist > 0 && dist < minDist) {
////////                    minDist = dist;
////////                    closest = other;
////////                }
////////            }
////////        }
////////        return closest;
////////    }
////////
////////
////////
////////    Random rnd = new Random();
////////if (rnd.nextDouble() < 0.03) {
////////        // تصادف واقعی (3 درصد احتمال)
////////    } else {
////////        // فقط ترمز کن
////////        v1.setTargetSpeed(0);
////////        v2.setTargetSpeed(0);
////////    }
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
//////
////////    // ---------- منطق تصادف ----------
////////    private void checkCollisions() {
////////        int carLength = ui.UIConstants.VEHICLE_LENGTH;
////////
////////        for (int i = 0; i < vehicles.size(); i++) {
////////            Vehicle v1 = vehicles.get(i);
////////            for (int j = i + 1; j < vehicles.size(); j++) {
////////                Vehicle v2 = vehicles.get(j);
////////
////////                if (v1.getCurrentLane() == null || v2.getCurrentLane() == null) continue;
////////                if (v1.getCurrentLane() != v2.getCurrentLane()) continue;
////////
////////                double dist = Math.abs(v1.getPositionInLane() - v2.getPositionInLane());
////////                if (dist < carLength * 0.8) {
////////                    v1.setTargetSpeed(0); v1.setSpeed(0);
////////                    v2.setTargetSpeed(0); v2.setSpeed(0);
////////
////////                    Point p = v1.getCurrentLane().getPositionAt(
////////                            (v1.getPositionInLane() + v2.getPositionInLane()) / 2.0
////////                    );
////////                    long endTime = System.currentTimeMillis() + 7000;
////////                    activeAccidents.add(new Accident(p.getX(), p.getY(), endTime));
////////                }
////////            }
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
//////
//////
//////
//////
//////
//////
////////
////////package simulation;
////////
////////import core.Vehicle;
////////import core.Point; // ✅ درست شد
////////import infrastructure.CityMap;
////////import trafficcontrol.TrafficLight;
////////import trafficcontrol.LightState;
////////import infrastructure.Lane;
////////
////////import java.util.*;
////////
/////////**
//////// * World: دنیای شبیه‌سازی
//////// */
////////public class World implements Updatable {
////////    private final LinkedList<Vehicle> vehicles;
////////    private final LinkedList<TrafficLight> trafficLights;
////////    private final LinkedList<pedestrian.Pedestrian> pedestrians;
////////    private final CityMap map;
////////    private double dtSeconds = 0.1;
////////
////////    // ---------- مدیریت تصادف ----------
////////    public static class Accident {   // ✅ قبلاً private بود
////////        public double x, y;
////////        public long endTimeMs;
////////        public Accident(double x, double y, long endTimeMs) {
////////            this.x = x; this.y = y; this.endTimeMs = endTimeMs;
////////        }
////////    }
////////    private final List<Accident> activeAccidents = new ArrayList<Accident>();
////////
////////    public World(CityMap map) {
////////        this.map = map;
////////        this.vehicles = new LinkedList<Vehicle>();
////////        this.trafficLights = new LinkedList<TrafficLight>();
////////        this.pedestrians = new LinkedList<pedestrian.Pedestrian>();
////////    }
////////
////////    public void setDtSeconds(double dt) {
////////        if (dt <= 0) dt = 0.1;
////////        this.dtSeconds = dt;
////////        for (int i = 0; i < vehicles.size(); i++) {
////////            vehicles.get(i).setDtSeconds(dt);
////////        }
////////    }
////////
////////    public void addVehicle(Vehicle v) {
////////        if (v != null) {
////////            v.setDtSeconds(dtSeconds);
////////            vehicles.add(v);
////////        }
////////    }
////////
////////    public void addTrafficLight(TrafficLight tl) {
////////        if (tl != null) trafficLights.add(tl);
////////    }
////////
////////    public void addPedestrian(pedestrian.Pedestrian p) {
////////        if (p != null) pedestrians.add(p);
////////    }
////////
////////    public CityMap getMap() { return map; }
////////    public List<Vehicle> getVehicles() { return vehicles; }
////////    public List<TrafficLight> getTrafficLights() { return trafficLights; }
////////    public List<pedestrian.Pedestrian> getPedestrians() { return pedestrians; }
////////    public List<Accident> getActiveAccidents() { return activeAccidents; }
////////
////////    @Override
////////    public void update() {
////////        // ۱) آپدیت چراغ‌ها
////////        for (int i = 0; i < trafficLights.size(); i++) {
////////            trafficLights.get(i).update();
////////        }
////////
////////        // ۲) تعیین سرعت هدف برای خودروها (بر اساس چراغ)
////////        for (int i = 0; i < vehicles.size(); i++) {
////////            Vehicle v = vehicles.get(i);
////////            double target = 42.0;
////////
////////            double laneLen = (v.getCurrentLane() != null) ? v.getCurrentLane().getLength() : 0;
////////            double distToEnd = laneLen - v.getPositionInLane();
////////
////////            if (v.getCurrentLane() != null && distToEnd < 45) {
////////                infrastructure.Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection();
////////                trafficcontrol.TrafficControlDevice dev = end.getControl(v.getCurrentLane().getDirection());
////////                if (dev instanceof TrafficLight) {
////////                    LightState st = ((TrafficLight) dev).getState();
////////                    if (st == LightState.RED) target = 0;
////////                    else if (st == LightState.YELLOW) target = Math.min(target, 18);
////////                }
////////            }
////////            v.setTargetSpeed(target);
////////        }
////////
////////        // ۳) منطق سبقت
////////        checkOvertaking();
////////
////////        // ۴) آپدیت خودروها
////////        for (int i = 0; i < vehicles.size(); i++) {
////////            vehicles.get(i).update();
////////        }
////////
////////        // ۵) بررسی تصادف‌ها
////////        checkCollisions();
////////
////////        // ۶) آپدیت عابران
////////        for (int i = 0; i < pedestrians.size(); i++) {
////////            pedestrians.get(i).update();
////////        }
////////
////////        // ۷) پاک کردن تصادف‌های قدیمی
////////        long now = System.currentTimeMillis();
////////        activeAccidents.removeIf(a -> a.endTimeMs < now);
////////    }
////////
////////    // ---------- منطق سبقت ----------
////////    private void checkOvertaking() {
////////        for (Vehicle v : vehicles) {
////////            if (v.isOvertaking()) {
////////                // وقتی جلوش خالی شد برگرده
////////                boolean clear = true;
////////                for (Vehicle other : vehicles) {
////////                    if (other == v) continue;
////////                    if (other.getCurrentLane() == v.getCurrentLane()) {
////////                        double dist = other.getPositionInLane() - v.getPositionInLane();
////////                        if (dist > 0 && dist < 20) { clear = false; break; }
////////                    }
////////                }
////////                if (clear) {
////////                    v.finishOvertaking();
////////                }
////////                continue;
////////            }
////////
////////            // اگر نزدیک به ماشین جلویی باشه → شروع سبقت
////////            Vehicle front = findFrontVehicle(v);
////////            if (front != null) {
////////                double gap = front.getPositionInLane() - v.getPositionInLane();
////////                if (gap > 0 && gap < 20) {
////////                    Lane left = v.getCurrentLane().getLeftAdjacentLane();
////////                    if (left != null) {
////////                        v.startOvertaking(left);
////////                    }
////////                }
////////            }
////////        }
////////    }
////////
////////    // پیدا کردن ماشین جلویی در همان لاین
////////    private Vehicle findFrontVehicle(Vehicle v) {
////////        Vehicle closest = null;
////////        double minDist = Double.MAX_VALUE;
////////        for (Vehicle other : vehicles) {
////////            if (other == v) continue;
////////            if (other.getCurrentLane() == v.getCurrentLane()) {
////////                double dist = other.getPositionInLane() - v.getPositionInLane();
////////                if (dist > 0 && dist < minDist) {
////////                    minDist = dist;
////////                    closest = other;
////////                }
////////            }
////////        }
////////        return closest;
////////    }
////////
////////
////////
////////    // ---------- منطق تصادف ----------
////////    private void checkCollisions() {
////////        int carLength = ui.UIConstants.VEHICLE_LENGTH; // ✅ طول واقعی ماشین‌ها
////////
////////        for (int i = 0; i < vehicles.size(); i++) {
////////            Vehicle v1 = vehicles.get(i);
////////            for (int j = i + 1; j < vehicles.size(); j++) {
////////                Vehicle v2 = vehicles.get(j);
////////
////////                if (v1.getCurrentLane() == null || v2.getCurrentLane() == null) continue;
////////                if (v1.getCurrentLane() != v2.getCurrentLane()) continue;
////////
////////                // فاصله جلو و عقب دو ماشین روی لاین
////////                double dist = Math.abs(v1.getPositionInLane() - v2.getPositionInLane());
////////
////////                // ✅ برخورد فقط وقتی که طول ماشین‌ها روی هم بیفته
////////                if (dist < carLength * 0.8) { // مثلا اگر کمتر از ۸۰٪ طول ماشین باشه → برخورد
////////                    // توقف خودروها
////////                    v1.setTargetSpeed(0); v1.setSpeed(0);
////////                    v2.setTargetSpeed(0); v2.setSpeed(0);
////////
////////                    // ثبت یکبار محل تصادف
////////                    Point p = v1.getCurrentLane().getPositionAt(
////////                            (v1.getPositionInLane() + v2.getPositionInLane()) / 2.0
////////                    );
////////                    long endTime = System.currentTimeMillis() + 7000;
////////                    activeAccidents.add(new Accident(p.getX(), p.getY(), endTime));
////////                }
////////            }
////////        }
////////    }
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
//////////    // ---------- منطق تصادف ----------
//////////    private void checkCollisions() {
//////////        for (int i = 0; i < vehicles.size(); i++) {
//////////            Vehicle v1 = vehicles.get(i);
//////////            for (int j = i + 1; j < vehicles.size(); j++) {
//////////                Vehicle v2 = vehicles.get(j);
//////////
//////////                if (v1.getCurrentLane() == null || v2.getCurrentLane() == null) continue;
//////////                if (v1.getCurrentLane() != v2.getCurrentLane()) continue;
//////////
//////////                double dist = Math.abs(v1.getPositionInLane() - v2.getPositionInLane());
//////////                if (dist < 5) {
//////////                    // تصادف
//////////                    v1.setTargetSpeed(0);
//////////                    v1.setSpeed(0);
//////////                    v2.setTargetSpeed(0);
//////////                    v2.setSpeed(0);
//////////
//////////                    // ثبت محل تصادف
//////////                    Point p = v1.getCurrentLane().getPositionAt(v1.getPositionInLane());
//////////                    long endTime = System.currentTimeMillis() + 7000; // ۷ ثانیه
//////////                    activeAccidents.add(new Accident(p.getX(), p.getY(), endTime));
//////////                }
//////////            }
//////////        }
//////////    }
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
//////////package simulation;
//////////
//////////
//////////import core.Vehicle;
//////////import core.Point; // ✅ ایمپورت درست
//////////import infrastructure.CityMap;
//////////import trafficcontrol.TrafficLight;
//////////import trafficcontrol.LightState;
//////////import infrastructure.Lane;
//////////
//////////import java.util.*;
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
//////////
////////////import core.Vehicle;
////////////import infrastructure.CityMap;
////////////import trafficcontrol.TrafficLight;
////////////import trafficcontrol.LightState;
////////////import infrastructure.Lane;
////////////import core.Point;
////////////
////////////import java.util.*;
//////////
///////////**
////////// * World: دنیای شبیه‌سازی
////////// */
//////////public class World implements Updatable {
//////////    private final LinkedList<Vehicle> vehicles;
//////////    private final LinkedList<TrafficLight> trafficLights;
//////////    private final LinkedList<pedestrian.Pedestrian> pedestrians;
//////////    private final CityMap map;
//////////    private double dtSeconds = 0.1;
//////////
//////////    // ---------- مدیریت تصادف ----------
//////////    private static class Accident {
//////////        double x, y;
//////////        long endTimeMs;
//////////        Accident(double x, double y, long endTimeMs) {
//////////            this.x = x; this.y = y; this.endTimeMs = endTimeMs;
//////////        }
//////////    }
//////////    private final List<Accident> activeAccidents = new ArrayList<Accident>();
//////////
//////////    public World(CityMap map) {
//////////        this.map = map;
//////////        this.vehicles = new LinkedList<Vehicle>();
//////////        this.trafficLights = new LinkedList<TrafficLight>();
//////////        this.pedestrians = new LinkedList<pedestrian.Pedestrian>();
//////////    }
//////////
//////////    public void setDtSeconds(double dt) {
//////////        if (dt <= 0) dt = 0.1;
//////////        this.dtSeconds = dt;
//////////        for (int i = 0; i < vehicles.size(); i++) {
//////////            vehicles.get(i).setDtSeconds(dt);
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
//////////    public void addPedestrian(pedestrian.Pedestrian p) {
//////////        if (p != null) pedestrians.add(p);
//////////    }
//////////
//////////    public CityMap getMap() { return map; }
//////////    public List<Vehicle> getVehicles() { return vehicles; }
//////////    public List<TrafficLight> getTrafficLights() { return trafficLights; }
//////////    public List<pedestrian.Pedestrian> getPedestrians() { return pedestrians; }
//////////    public List<Accident> getActiveAccidents() { return activeAccidents; }
//////////
//////////    @Override
//////////    public void update() {
//////////        // ۱) آپدیت چراغ‌ها
//////////        for (int i = 0; i < trafficLights.size(); i++) {
//////////            trafficLights.get(i).update();
//////////        }
//////////
//////////        // ۲) تعیین سرعت هدف برای خودروها (بر اساس چراغ)
//////////        for (int i = 0; i < vehicles.size(); i++) {
//////////            Vehicle v = vehicles.get(i);
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
//////////        for (int i = 0; i < vehicles.size(); i++) {
//////////            vehicles.get(i).update();
//////////        }
//////////
//////////        // ۵) بررسی تصادف‌ها
//////////        checkCollisions();
//////////
//////////        // ۶) آپدیت عابران
//////////        for (int i = 0; i < pedestrians.size(); i++) {
//////////            pedestrians.get(i).update();
//////////        }
//////////
//////////        // ۷) پاک کردن تصادف‌های قدیمی
//////////        long now = System.currentTimeMillis();
//////////        activeAccidents.removeIf(a -> a.endTimeMs < now);
//////////    }
//////////
//////////    // ---------- منطق سبقت ----------
//////////    private void checkOvertaking() {
//////////        for (Vehicle v : vehicles) {
//////////            if (v.isOvertaking()) {
//////////                // وقتی جلوش خالی شد برگرده
//////////                boolean clear = true;
//////////                for (Vehicle other : vehicles) {
//////////                    if (other == v) continue;
//////////                    if (other.getCurrentLane() == v.getCurrentLane()) {
//////////                        double dist = other.getPositionInLane() - v.getPositionInLane();
//////////                        if (dist > 0 && dist < 20) { clear = false; break; }
//////////                    }
//////////                }
//////////                if (clear) {
//////////                    v.finishOvertaking();
//////////                }
//////////                continue;
//////////            }
//////////
//////////            // اگر نزدیک به ماشین جلویی باشه → شروع سبقت
//////////            Vehicle front = findFrontVehicle(v);
//////////            if (front != null) {
//////////                double gap = front.getPositionInLane() - v.getPositionInLane();
//////////                if (gap > 0 && gap < 20) {
//////////                    Lane left = v.getCurrentLane().getLeftAdjacentLane();
//////////                    if (left != null) {
//////////                        v.startOvertaking(left);
//////////                    }
//////////                }
//////////            }
//////////        }
//////////    }
//////////
//////////    // پیدا کردن ماشین جلویی در همان لاین
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
//////////        for (int i = 0; i < vehicles.size(); i++) {
//////////            Vehicle v1 = vehicles.get(i);
//////////            for (int j = i + 1; j < vehicles.size(); j++) {
//////////                Vehicle v2 = vehicles.get(j);
//////////
//////////                if (v1.getCurrentLane() == null || v2.getCurrentLane() == null) continue;
//////////                if (v1.getCurrentLane() != v2.getCurrentLane()) continue;
//////////
//////////                double dist = Math.abs(v1.getPositionInLane() - v2.getPositionInLane());
//////////                if (dist < 5) {
//////////                    // تصادف
//////////                    v1.setTargetSpeed(0);
//////////                    v1.setSpeed(0);
//////////                    v2.setTargetSpeed(0);
//////////                    v2.setSpeed(0);
//////////
//////////                    // ثبت محل تصادف
//////////                    infrastructure.Point p = v1.getCurrentLane().getPositionAt(v1.getPositionInLane());
//////////                    long endTime = System.currentTimeMillis() + 7000; // ۷ ثانیه
//////////                    activeAccidents.add(new Accident(p.getX(), p.getY(), endTime));
//////////                }
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
////////////
////////////package simulation; // // پکیج شبیه‌سازی
////////////
////////////import core.Vehicle; // // وسیله نقلیه
////////////import core.VehicleState; // // وضعیت خودرو
////////////import infrastructure.CityMap; // // نقشه
////////////import trafficcontrol.TrafficLight; // // چراغ راهنما
////////////import trafficcontrol.LightState; // // وضعیت چراغ
////////////
////////////import java.util.LinkedList; // // لیست پیوندی برای خودروها
////////////import java.util.List; // // اینترفیس لیست
////////////
////////////public class World implements Updatable { // // دنیای شبیه‌سازی
////////////    private final LinkedList<Vehicle> vehicles; // // فهرست خودروها
////////////    private final LinkedList<TrafficLight> trafficLights; // // فهرست چراغ‌ها
////////////    private final LinkedList<pedestrian.Pedestrian> pedestrians; // // فهرست عابرها
////////////    private final CityMap map; // // نقشه شهر
////////////    private double dtSeconds = 0.1; // // dt پیش‌فرض (ثانیه)
////////////
////////////    public World(CityMap map) { // // سازنده
////////////        this.map = map; // // ذخیره نقشه
////////////        this.vehicles = new LinkedList<Vehicle>(); // // ساخت لیست خودرو
////////////        this.trafficLights = new LinkedList<TrafficLight>(); // // ساخت لیست چراغ
////////////        this.pedestrians = new LinkedList<pedestrian.Pedestrian>(); // // ساخت لیست عابر
////////////    }
////////////
////////////    public void setDtSeconds(double dt) { // // ست‌کردن dt از سمت Clock
////////////        if (dt <= 0) dt = 0.1; // // ایمنی
////////////        this.dtSeconds = dt; // // ذخیره
////////////        for (int i = 0; i < vehicles.size(); i++) { // // روی خودروها
////////////            vehicles.get(i).setDtSeconds(dt); // // همگام‌سازی dt
////////////        }
////////////    }
////////////
////////////    public void addVehicle(Vehicle v) { // // افزودن خودرو
////////////        if (v != null) { // // بررسی null
////////////            v.setDtSeconds(dtSeconds); // // تنظیم dt
////////////            vehicles.add(v); // // افزودن به لیست
////////////        }
////////////    }
////////////
////////////    public void addTrafficLight(TrafficLight tl) { // // افزودن چراغ
////////////        if (tl != null) trafficLights.add(tl); // // افزودن
////////////    }
////////////
////////////    public CityMap getMap() { // // گتر نقشه
////////////        return map; // // خروجی
////////////    }
////////////
////////////    public List<Vehicle> getVehicles() { // // گتر لیست خودروها
////////////        return vehicles; // // خروجی
////////////    }
////////////
////////////    public List<TrafficLight> getTrafficLights() { // // گتر لیست چراغ‌ها
////////////        return trafficLights; // // خروجی
////////////    }
////////////
////////////    @Override
////////////    public void update() { // // تیک شبیه‌سازی
////////////        // ۱) آپدیت چراغ‌ها
////////////        for (int i = 0; i < trafficLights.size(); i++) { // // حلقه چراغ‌ها
////////////            trafficLights.get(i).update(); // // آپدیت چراغ
////////////        }
////////////
////////////        // ۲) تعیین targetSpeed ساده (براساس چراغ نزدیکِ انتهای لِین)
////////////        for (int i = 0; i < vehicles.size(); i++) { // // حلقه خودروها
////////////            Vehicle v = vehicles.get(i); // // خودرو
////////////            double target = 42.0; // // سرعت هدف پایه (قابل تنظیم)
////////////            // بازه ترمز قبل از انتهای لِین:
////////////            double laneLen = (v.getCurrentLane() != null) ? v.getCurrentLane().getLength() : 0; // // طول لِین
////////////            double distToEnd = laneLen - v.getPositionInLane(); // // فاصله تا انتها
////////////            if (v.getCurrentLane() != null && distToEnd < 45) { // // اگر نزدیک انتهای لِینیم
////////////                infrastructure.Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection(); // // تقاطع انتها
////////////                trafficcontrol.TrafficControlDevice dev =
////////////                        end.getControl(v.getCurrentLane().getDirection()); // // کنترلِ جهتِ لِین
////////////                if (dev instanceof TrafficLight) { // // اگر چراغ است
////////////                    LightState st = ((TrafficLight) dev).getState(); // // وضعیت چراغ
////////////                    if (st == LightState.RED) target = 0; // // قرمز = توقف
////////////                    else if (st == LightState.YELLOW) target = Math.min(target, 18); // // زرد = کند
////////////                }
////////////            }
////////////            v.setTargetSpeed(target); // // اعمال سرعت هدف
////////////        }
////////////
////////////        // ۳) آپدیت حرکت خودروها
////////////        for (int i = 0; i < vehicles.size(); i++) { // // حلقه خودروها
////////////            vehicles.get(i).update(); // // محاسبه حرکت
////////////        }
////////////
////////////        // ۴) TODO: تصادف/تداخل و مسیر بعدی را در آینده کامل می‌کنیم
////////////    }
////////////}
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
////////////
//////////////
//////////////
//////////////package simulation;
//////////////
//////////////import java.util.*;
//////////////import core.*;
//////////////import infrastructure.*;
//////////////import pedestrian.*;
//////////////import trafficcontrol.*;
//////////////
//////////////public class World implements Updatable {
//////////////
//////////////    private CityMap map;
//////////////    private final ArrayList<Vehicle> vehicles = new ArrayList<>();
//////////////    private final ArrayList<TrafficLight> lights = new ArrayList<>();
//////////////    private final ArrayList<Pedestrian> pedestrians = new ArrayList<>();
//////////////    private final Random rng = new Random();
//////////////
//////////////    // تایم‌استپ فیزیکی (ثابت و کوچک → حرکت نرم)
//////////////    private double dtSeconds = 0.016; // ~16ms
//////////////
//////////////    // پارامترهای ایمنی/رفتار
//////////////    private static final double STOP_DIST      = 18.0; // فاصلهٔ شروع قضاوت قبل از انتهای لاین
//////////////    private static final double SAFE_GAP_MIN   = 22.0; // حداقل فاصلهٔ ایمن بین خودروها
//////////////    private static final double YIELD_GAP      = 24.0; // حداقل فاصله برای ورود به فلکه
//////////////    private static final double PED_ZONE       = 18.0; // ناحیهٔ ترمز برای عابر
//////////////    private static final double BUMP_MARGIN    = 6.0;  // حاشیهٔ قبل از سرعت‌گیر
//////////////    private static final double SPEED_SMOOTH   = 0.10; // ضریب هموارسازی هدف‌سرعت
//////////////
//////////////    public World() {}
//////////////    public World(CityMap m){ this.map = m; }
//////////////
//////////////    public void setCityMap(CityMap m){ this.map = m; }
//////////////    public CityMap getCityMap(){ return this.map; }
//////////////    public CityMap getMap(){ return this.map; } // alias
//////////////
//////////////    public List<Vehicle> getVehicles(){ return vehicles; }
//////////////    public void addVehicle(Vehicle v){
//////////////        if(v!=null){
//////////////            v.setDtSeconds(dtSeconds);
//////////////            vehicles.add(v);
//////////////        }
//////////////    }
//////////////
//////////////    public void addTrafficLight(TrafficLight tl){ if(tl!=null) lights.add(tl); }
//////////////    public List<TrafficLight> getTrafficLights(){ return lights; }
//////////////
//////////////    public void addPedestrian(Pedestrian p){ if(p!=null) pedestrians.add(p); }
//////////////    public List<Pedestrian> getPedestrians(){ return pedestrians; }
//////////////
//////////////    public void setDtSeconds(double dt){
//////////////        if(dt>0){
//////////////            dtSeconds = dt;
//////////////            for (int i=0;i<vehicles.size();i++) vehicles.get(i).setDtSeconds(dt);
//////////////        }
//////////////    }
//////////////
//////////////    // ---------- تزریق تدریجی خودرو/عابر (می‌توان از بیرون هم فراخوانی کرد)
//////////////    private int tick;
//////////////    public void spawnVehiclesGradually(int perSecond, DriverProfile profile,
//////////////                                       double minSpeed, double maxSpeed){
//////////////        // هر ثانیه perSecond خودرو در ورودی‌های تصادفی
//////////////        int intervalTicks = Math.max(1, (int)Math.round(1.0 / dtSeconds / perSecond));
//////////////        if (tick % intervalTicks == 0 && map!=null){
//////////////            Lane spawn = map.pickRandomEntryLane();
//////////////            if (spawn != null){
//////////////                Vehicle v = new Vehicle("V"+System.nanoTime(), VehicleType.CAR, profile);
//////////////                v.setCurrentLane(spawn);
//////////////                v.setPositionInLane(rng.nextDouble()*2.0 + 1.0); // کمی بعد از ابتدا
//////////////                v.setCruiseSpeed(minSpeed + rng.nextDouble()*(maxSpeed-minSpeed));
//////////////                v.setTargetSpeed(v.getCruiseSpeed());
//////////////                addVehicle(v);
//////////////            }
//////////////        }
//////////////    }
//////////////
//////////////    public void spawnPedestriansPlan(int totalCrossings, int groupedCountPerWave){
//////////////        // نمونه ساده: هر چند ثانیه چند عابر با الگوی گروهی/تکی وارد شوند
//////////////        if (tick == 0 && map != null){
//////////////            List<PedestrianCrossing> pcs = map.pickRandomCrossings(totalCrossings, /*avoidRoundabout=*/true);
//////////////            // دو گذرگاه → گروهی، دو گذرگاه → تکی
//////////////            for (int i=0;i<pcs.size();i++){
//////////////                PedestrianCrossing pc = pcs.get(i);
//////////////                boolean grouped = (i<2);
//////////////                pc.setPlanned(grouped, grouped? groupedCountPerWave : 1, 5.0); // فاصله 5s
//////////////            }
//////////////        }
//////////////    }
//////////////
//////////////    // ---------- منطق شبیه‌سازی
//////////////    @Override
//////////////    public void update() {
//////////////        tick++;
//////////////
//////////////        // چراغ‌ها (خارج از فلکه‌ها)
//////////////        for (int i=0;i<lights.size();i++) lights.get(i).update();
//////////////
//////////////        // عابرها (حرکت/ورود موجی)
//////////////        for (int i=0;i<pedestrians.size();i++) pedestrians.get(i).update();
//////////////
//////////////        // تزریق نمونه (پارامترهای شما از پیام قبلی)
//////////////        spawnVehiclesGradually(4, DriverProfile.LAW_ABIDING, 5.0, 13.0);
//////////////
//////////////        // بدنهٔ اصلی خودروها
//////////////        final int n = vehicles.size();
//////////////        for (int i=0;i<n;i++){
//////////////            Vehicle v = vehicles.get(i);
//////////////            Lane ln = v.getCurrentLane();
//////////////            if (ln == null){ v.update(); continue; }
//////////////
//////////////            Road rd = ln.getParentRoad();
//////////////            if (rd == null){ v.update(); continue; }
//////////////
//////////////            // 1) چراغ/حق‌تقدم نزدیک انتها
//////////////            double laneLen   = ln.getLength();
//////////////            double s         = v.getPositionInLane();
//////////////            double distToEnd = laneLen - s;
//////////////
//////////////            if (distToEnd <= STOP_DIST){
//////////////                Intersection endGeom = rd.getEnd(); // انتهای هندسی جاده
//////////////                if (ln.getDirection() == Direction.WEST || ln.getDirection() == Direction.NORTH)
//////////////                    endGeom = rd.getStart();
//////////////
//////////////                // اگر فلکه: چراغ نادیده گرفته می‌شود، صرفاً فاصلهٔ خودروهای در حلقه بررسی شود
//////////////                if (endGeom.isRoundabout()){
//////////////                    boolean canEnter = hasRoundaboutGap(endGeom, ln.getDirection(), YIELD_GAP);
//////////////                    if (!canEnter) v.setTargetSpeed(0.0);
//////////////                    else relaxToCruise(v);
//////////////                } else {
//////////////                    // غیر فلکه: اگر چراغ کنترل دارد و قرمز/زرد بود، توقف
//////////////                    TrafficControlDevice dev = endGeom.getControl(ln.getDirection());
//////////////                    if (dev instanceof TrafficLight tl){
//////////////                        LightState st = tl.getState();
//////////////                        if (st==LightState.RED || st==LightState.YELLOW) v.setTargetSpeed(0.0);
//////////////                        else relaxToCruise(v);
//////////////                    } else if (dev instanceof YieldSign){
//////////////                        boolean allowed = hasPriorityGap(endGeom, ln.getDirection(), YIELD_GAP);
//////////////                        if (!allowed) v.setTargetSpeed(0.0); else relaxToCruise(v);
//////////////                    } else relaxToCruise(v);
//////////////                }
//////////////            }
//////////////
//////////////            // 2) Car-Following : فقط در «همان لاین»
//////////////            Vehicle lead = nearestLeadInSameLane(v, ln);
//////////////            if (lead != null){
//////////////                double gap = lead.getPositionInLane() - s;
//////////////                double desiredGap = Math.max(SAFE_GAP_MIN, v.getSpeed()*0.7); // کمی وابسته به سرعت
//////////////                if (gap < desiredGap){
//////////////                    // هدف‌سرعت را نرم کاهش می‌دهیم تا هم‌سرعت جلو شویم
//////////////                    double followSpeed = Math.max(0.0, lead.getSpeed()*0.9);
//////////////                    v.setTargetSpeed(Math.min(v.getTargetSpeed(), followSpeed));
//////////////                }
//////////////            }
//////////////
//////////////            // 3) عابر پیاده: اگر گذرگاه در چند متر جلوی خودرو فعال است، ترمز
//////////////            if (isPedCrossingAhead(ln, s, PED_ZONE)) v.setTargetSpeed(0.0);
//////////////
//////////////            // 4) سرعت‌گیر/محدودیت محلی: اگر داخل بازه، هدف‌سرعت را محدود کن
//////////////            SpeedBump bump = insideSpeedBump(ln, s, BUMP_MARGIN);
//////////////            if (bump != null){
//////////////                v.setTargetSpeed(Math.min(v.getTargetSpeed(), bump.getMaxAllowedSpeed()));
//////////////            }
//////////////            double localLimit = rd.getLocalSpeedLimit();
//////////////            if (localLimit > 0) v.setTargetSpeed(Math.min(v.getTargetSpeed(), localLimit));
//////////////
//////////////            // 5) هموارسازی هدف‌سرعت برای حذف «مکث-مکث»
//////////////            double tgt = v.getTargetSpeed();
//////////////            double smooth = v.getSpeed() + (tgt - v.getSpeed())*SPEED_SMOOTH;
//////////////            v.setTargetSpeed(smooth);
//////////////
//////////////            // 6) به‌روزرسانی داخلی وسیله
//////////////            v.update();
//////////////        }
//////////////    }
//////////////
//////////////    // ---------- کمکی‌ها
//////////////
//////////////    private void relaxToCruise(Vehicle v){
//////////////        if (v.getTargetSpeed() < 1e-6) v.setTargetSpeed(v.getCruiseSpeed());
//////////////    }
//////////////
//////////////    private Vehicle nearestLeadInSameLane(Vehicle me, Lane ln){
//////////////        Vehicle lead = null;
//////////////        double bestDelta = Double.POSITIVE_INFINITY;
//////////////        double myS = me.getPositionInLane();
//////////////        for (int j=0;j<vehicles.size();j++){
//////////////            Vehicle u = vehicles.get(j);
//////////////            if (u==me) continue;
//////////////            if (u.getCurrentLane() != ln) continue;
//////////////            double d = u.getPositionInLane() - myS;
//////////////            if (d>0 && d<bestDelta){ bestDelta=d; lead=u; }
//////////////        }
//////////////        return lead;
//////////////    }
//////////////
//////////////    private boolean isPedCrossingAhead(Lane ln, double s, double zone){
//////////////        List<PedestrianCrossing> pcs = ln.getParentRoad().getCrossings();
//////////////        if (pcs==null) return false;
//////////////        for (int i=0;i<pcs.size();i++){
//////////////            PedestrianCrossing pc = pcs.get(i);
//////////////            if (pc==null || pc.isNearRoundabout()) continue;
//////////////            double pos = pc.getLanePosition(ln);
//////////////            if (pos>=0){
//////////////                double d = pos - s;
//////////////                if (d>=0 && d<=zone && pc.isPeopleOnCrossing()) return true;
//////////////            }
//////////////        }
//////////////        return false;
//////////////    }
//////////////
//////////////    private SpeedBump insideSpeedBump(Lane ln, double s, double margin){
//////////////        List<SpeedBump> bumps = ln.getSpeedBumps();
//////////////        if (bumps==null) return null;
//////////////        for (int i=0;i<bumps.size();i++){
//////////////            SpeedBump b = bumps.get(i);
//////////////            if (b.isInside(s+margin)) return b;
//////////////        }
//////////////        return null;
//////////////    }
//////////////
//////////////    // درگاه سادهٔ بررسی حق‌تقدم در فلکه (بر اساس لاین‌های حلقه با همان جهت)
//////////////    private boolean hasRoundaboutGap(Intersection rbt, Direction entering, double minGap){
//////////////        List<Lane> ring = rbt.getRingLanes(); // لاین‌های حلقه راست‌گرد
//////////////        if (ring==null) return true;
//////////////        for (int i=0;i<ring.size();i++){
//////////////            Lane ln = ring.get(i);
//////////////            // نزدیک‌ترین وسیله جلو مسیر ورود؟
//////////////            Vehicle nearest = nearestOnLaneFrom(ring.get(i), rbt.getRingMergeS(entering));
//////////////            if (nearest!=null){
//////////////                double delta = nearest.getPositionInLane() - rbt.getRingMergeS(entering);
//////////////                if (delta >=0 && delta < minGap) return false;
//////////////            }
//////////////        }
//////////////        return true;
//////////////    }
//////////////
//////////////    // برای Yield معمولی (غیرفلکه)، اگر وسیله‌ای با حق‌تقدم نزدیک است، اجازه نده
//////////////    private boolean hasPriorityGap(Intersection is, Direction entering, double minGap){
//////////////        Lane pri = is.getPriorityLaneAgainst(entering);
//////////////        if (pri==null) return true;
//////////////        Vehicle near = nearestOnLaneFrom(pri, is.getPriorityCheckS(pri));
//////////////        if (near==null) return true;
//////////////        double d = near.getPositionInLane() - is.getPriorityCheckS(pri);
//////////////        return !(d>=0 && d<minGap);
//////////////    }
//////////////
//////////////    private Vehicle nearestOnLaneFrom(Lane ln, double fromS){
//////////////        Vehicle best=null; double bestD=Double.POSITIVE_INFINITY;
//////////////        for (int i=0;i<vehicles.size();i++){
//////////////            Vehicle v = vehicles.get(i);
//////////////            if (v.getCurrentLane()!=ln) continue;
//////////////            double d = v.getPositionInLane() - fromS;
//////////////            if (d>=0 && d<bestD){ bestD=d; best=v; }
//////////////        }
//////////////        return best;
//////////////    }
//////////////}
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
////////////////package simulation;
//////////////
////////////////import java.util.ArrayList;
////////////////import java.util.Collections;
////////////////import java.util.List;
////////////////
////////////////import core.*;                 // Vehicle, Direction, Updatable
////////////////import infrastructure.*;       // Lane, Road, Intersection, CityMap
////////////////import trafficcontrol.*;       // TrafficLight, LightState, TrafficControlDevice
////////////////
/////////////////**
//////////////// * World: ظرف مرکزی شبیه‌سازی.
//////////////// * - نگهداری نقشه، وسایل نقلیه و چراغ‌ها
//////////////// * - اجرای یک تیک شبیه‌سازی در update()
//////////////// * - فراهم‌کردن clock tick و زمان شبیه‌سازی برای rule/engineها
//////////////// */
////////////////public class World implements Updatable {
////////////////
////////////////    // --------- State ---------
////////////////    private CityMap map;
////////////////    private final ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
////////////////    private final ArrayList<TrafficLight> lights = new ArrayList<TrafficLight>();
////////////////
////////////////    private double dtSeconds = 0.1;           // گام زمانی (ثانیه)
////////////////    private long tickCount = 0L;              // شمارنده‌ی تیک‌های شبیه‌سازی
////////////////    private long simMillis = 0L;              // زمان شبیه‌سازی بر حسب میلی‌ثانیه
////////////////
////////////////    // --------- Ctors ---------
////////////////    public World() {}
////////////////    public World(CityMap m) { this.map = m; }
////////////////
////////////////    // --------- Map / Time API ---------
////////////////    public void setCityMap(CityMap m) { this.map = m; }
////////////////    public CityMap getCityMap() { return this.map; }
////////////////    /** آلیاس با سازگاری عقب‌رو */
////////////////    public CityMap getMap() { return this.map; }
////////////////
////////////////    /** زمان هر تیک (ثانیه) */
////////////////    public double getDtSeconds() { return dtSeconds; }
////////////////
////////////////    /** تنظیم dt و همگام‌سازی روی وسایل */
////////////////    public void setDtSeconds(double dt) {
////////////////        if (dt > 0) {
////////////////            this.dtSeconds = dt;
////////////////            for (int i = 0; i < this.vehicles.size(); i++) {
////////////////                this.vehicles.get(i).setDtSeconds(dt);
////////////////            }
////////////////        }
////////////////    }
////////////////
////////////////    /** شمارنده‌ی تیک (برای RuleEngine و …) */
////////////////    public long getClockTick() { return tickCount; }
////////////////
////////////////    /** زمان شبیه‌سازی انباشته بر حسب میلی‌ثانیه */
////////////////    public long getTickMillis() { return simMillis; }
////////////////
////////////////    // --------- Entities API ---------
////////////////    public List<Vehicle> getVehicles() { return this.vehicles; }
////////////////    public List<TrafficLight> getTrafficLights() { return this.lights; }
////////////////
////////////////    public List<Vehicle> getVehiclesReadOnly() { return Collections.unmodifiableList(vehicles); }
////////////////    public List<TrafficLight> getTrafficLightsReadOnly() { return Collections.unmodifiableList(lights); }
////////////////
////////////////    public void addVehicle(Vehicle v) {
////////////////        if (v != null) {
////////////////            v.setDtSeconds(this.dtSeconds);
////////////////            this.vehicles.add(v);
////////////////        }
////////////////    }
////////////////
////////////////    public boolean removeVehicleById(String id) {
////////////////        if (id == null) return false;
////////////////        for (int i = 0; i < vehicles.size(); i++) {
////////////////            if (id.equals(vehicles.get(i).getId())) {
////////////////                vehicles.remove(i);
////////////////                return true;
////////////////            }
////////////////        }
////////////////        return false;
////////////////    }
////////////////
////////////////    public void addTrafficLight(TrafficLight tl) {
////////////////        if (tl != null) { this.lights.add(tl); }
////////////////    }
////////////////
////////////////    /** پاک‌کردن دنیا (برای ریست سناریوها) */
////////////////    public void clear() {
////////////////        vehicles.clear();
////////////////        lights.clear();
////////////////        tickCount = 0L;
////////////////        simMillis = 0L;
////////////////    }
////////////////
////////////////    // --------- Tick / Update ---------
////////////////    @Override
////////////////    public void update() {
////////////////        // 1) چراغ‌ها
////////////////        for (int i = 0; i < this.lights.size(); i++) {
////////////////            this.lights.get(i).update();
////////////////        }
////////////////
////////////////        // پارامترهای ساده‌ی تعامل
////////////////        final double STOP_DIST = 22.0;        // فاصله‌ی شروع تصمیم‌گیری در انتهای لِین
////////////////        final double SAFE_GAP_MIN = 24.0;     // حداقل فاصله‌ی ایمن طولی
////////////////
////////////////        // 2) وسایل نقلیه
////////////////        for (int i = 0; i < this.vehicles.size(); i++) {
////////////////            Vehicle v = this.vehicles.get(i);
////////////////
////////////////            Lane ln = v.getCurrentLane();
////////////////            if (ln == null) { v.update(); continue; }
////////////////
////////////////            Road rd = ln.getParentRoad();
////////////////            if (rd == null) { v.update(); continue; }
////////////////
////////////////            // --- کنترل تقاطع نزدیک انتهای لِین ---
////////////////            double laneLen = ln.getLength();
////////////////            double distToEnd = laneLen - v.getPositionInLane();
////////////////
////////////////            if (distToEnd <= STOP_DIST) {
////////////////                // انتخاب تقاطع "پایان" نسبت به جهت حرکت لِین
////////////////                Intersection end = rd.getEnd();
////////////////                if (ln.getDirection() == Direction.WEST || ln.getDirection() == Direction.NORTH) {
////////////////                    end = rd.getStart();
////////////////                }
////////////////
////////////////                if (end != null) {
////////////////                    TrafficControlDevice dev = end.getControl(ln.getDirection());
////////////////                    if (dev instanceof TrafficLight) {
////////////////                        LightState st = ((TrafficLight) dev).getState();
////////////////                        if (st == LightState.RED || st == LightState.YELLOW) {
////////////////                            v.setTargetSpeed(0.0); // توقف
////////////////                        } else { // GREEN
////////////////                            if (v.getTargetSpeed() < 1e-6) {
////////////////                                v.setTargetSpeed(v.getCruiseSpeed()); // برگشت به سرعت کروز
////////////////                            }
////////////////                        }
////////////////                    }
////////////////                }
////////////////            }
////////////////
////////////////            // --- car-following ساده در همان لِین ---
////////////////            double myPos = v.getPositionInLane();
////////////////            Vehicle lead = null; double leadDelta = Double.POSITIVE_INFINITY;
////////////////
////////////////            for (int j = 0; j < this.vehicles.size(); j++) {
////////////////                if (i == j) continue;
////////////////                Vehicle u = this.vehicles.get(j);
////////////////                if (u.getCurrentLane() != ln) continue;
////////////////
////////////////                double d = u.getPositionInLane() - myPos;
////////////////                if (d > 0 && d < leadDelta) { leadDelta = d; lead = u; }
////////////////            }
////////////////
////////////////            if (lead != null) {
////////////////                double desiredGap = Math.max(SAFE_GAP_MIN, v.getSpeed() * 0.6);
////////////////                if (leadDelta < desiredGap) {
////////////////                    double newTarget = Math.min(v.getTargetSpeed(),
////////////////                            Math.max(0.0, lead.getSpeed() * 0.9));
////////////////                    v.setTargetSpeed(newTarget);
////////////////                }
////////////////            }
////////////////
////////////////            // منطق داخلی حرکت
////////////////            v.update();
////////////////        }
////////////////
////////////////        // 3) پیش‌برد زمان شبیه‌سازی
////////////////        tickCount++;
////////////////        simMillis += Math.round(dtSeconds * 1000.0);
////////////////    }
////////////////}
////////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
////////////////package simulation; // // پکیج simulation
////////////////
////////////////import java.util.ArrayList; // // لیست
////////////////import java.util.List; // // اینترفیس
////////////////import core.*; // // Vehicle/Direction/Updatable
////////////////import infrastructure.*; // // Lane/Road/Intersection/CityMap
////////////////import trafficcontrol.*; // // چراغ راهنمایی
////////////////
////////////////public class World implements Updatable { // // دنیای شبیه‌سازی
////////////////    private CityMap map; // // نقشه
////////////////    private ArrayList<Vehicle> vehicles; // // وسایل
////////////////    private ArrayList<TrafficLight> lights; // // چراغ‌ها
////////////////    private double dtSeconds = 0.1; // // گام زمانی
////////////////
////////////////    public World() { this.vehicles = new ArrayList<Vehicle>(); this.lights = new ArrayList<TrafficLight>(); } // // سازنده
////////////////    public World(CityMap m) { this(); this.map = m; } // // سازنده با نقشه
////////////////
////////////////    public void setCityMap(CityMap m) { this.map = m; } // // ست نقشه
////////////////    public CityMap getCityMap() { return this.map; } // // گتر نقشه
////////////////    public CityMap getMap() { return this.map; } // // آلیاس
////////////////    public List<Vehicle> getVehicles() { return this.vehicles; } // // گتر وسایل
////////////////    public void addVehicle(Vehicle v) { if (v != null) { v.setDtSeconds(this.dtSeconds); this.vehicles.add(v); } } // // افزودن
////////////////    public void addTrafficLight(TrafficLight tl) { if (tl != null) { this.lights.add(tl); } } // // افزودن چراغ
////////////////    public List<TrafficLight> getTrafficLights() { return this.lights; } // // گتر چراغ‌ها
////////////////    public void setDtSeconds(double dt){ if(dt>0){ this.dtSeconds=dt; for(int i=0;i<this.vehicles.size();i++){ this.vehicles.get(i).setDtSeconds(dt);} } } // // ست dt
////////////////
////////////////    @Override
////////////////    public void update() { // // به‌روزرسانی
////////////////        for (int i = 0; i < this.lights.size(); i++) { this.lights.get(i).update(); } // // آپدیت چراغ‌ها
////////////////
////////////////        final double STOP_DIST = 22.0; // // فاصلهٔ توقف
////////////////        final double SAFE_GAP_MIN = 24.0; // // فاصله ایمن مینیمم
////////////////
////////////////        for (int i = 0; i < this.vehicles.size(); i++) { // // حلقه وسایل
////////////////            Vehicle v = this.vehicles.get(i); // // خودرو
////////////////            Lane ln = v.getCurrentLane(); // // لِین
////////////////            if (ln == null) { v.update(); continue; } // // بدون لِین
////////////////            Road rd = ln.getParentRoad(); if (rd == null) { v.update(); continue; } // // بدون جاده
////////////////
////////////////            double laneLen = ln.getLength(); // // طول
////////////////            double distToEnd = laneLen - v.getPositionInLane(); // // فاصله تا انتها
////////////////            if (distToEnd <= STOP_DIST) { // // نزدیک تقاطع
////////////////                Intersection end = rd.getEnd(); // // انتهای هندسی
////////////////                if (ln.getDirection() == Direction.WEST || ln.getDirection() == Direction.NORTH) { end = rd.getStart(); } // // معکوس
////////////////                TrafficControlDevice dev = end.getControl(ln.getDirection()); // // کنترل
////////////////                if (dev instanceof TrafficLight) { // // اگر چراغ است
////////////////                    LightState st = ((TrafficLight) dev).getState(); // // وضعیت
////////////////                    if (st == LightState.RED || st == LightState.YELLOW) { // // قرمز/زرد
////////////////                        v.setTargetSpeed(0.0); // // توقف
////////////////                    } else { // // GREEN
////////////////                        // اگر قبلاً به خاطر چراغ ایست کرده بود، هدف‌سرعت را به سرعت کروز برگردان
////////////////                        if (v.getTargetSpeed() < 1e-6) { v.setTargetSpeed(v.getCruiseSpeed()); } // // بازگشت
////////////////                    }
////////////////                }
////////////////            }
////////////////
////////////////            // Car-Following ساده
////////////////            double myPos = v.getPositionInLane(); // // موقعیت خود
////////////////            Vehicle lead = null; double leadDelta = Double.POSITIVE_INFINITY; // // جلویی
////////////////            for (int j = 0; j < this.vehicles.size(); j++) { // // جست‌وجو
////////////////                if (i == j) continue; Vehicle u = this.vehicles.get(j); // // کاندید
////////////////                if (u.getCurrentLane() != ln) continue; // // باید همان لِین باشد
////////////////                double d = u.getPositionInLane() - myPos; // // فاصلهٔ طولی
////////////////                if (d > 0 && d < leadDelta) { leadDelta = d; lead = u; } // // نزدیک‌ترین جلویی
////////////////            }
////////////////            if (lead != null) { // // اگر جلویی داریم
////////////////                double desiredGap = Math.max(SAFE_GAP_MIN, v.getSpeed() * 0.6); // // فاصلهٔ ایمن
////////////////                if (leadDelta < desiredGap) { // // نزدیک شدیم
////////////////                    double newTarget = Math.min(v.getTargetSpeed(), Math.max(0.0, lead.getSpeed() * 0.9)); // // هم‌سرعت
////////////////                    v.setTargetSpeed(newTarget); // // اعمال
////////////////                }
////////////////            }
////////////////
////////////////            v.update(); // // منطق حرکت داخلی
////////////////        }
////////////////    }
////////////////}
////////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
////////////////package simulation; // // پکیج simulation
////////////////
////////////////import java.util.ArrayList; // // لیست پویا
////////////////import java.util.List; // // اینترفیس لیست
////////////////import core.*; // // Vehicle/Direction/Updatable و ...
////////////////import infrastructure.*; // // CityMap/Road/Lane/Intersection
////////////////import trafficcontrol.*; // // TrafficLight/LightState/TrafficControlDevice
////////////////
////////////////public class World implements Updatable { // // دنیای شبیه‌سازی
////////////////    private CityMap map; // // نقشهٔ شهر
////////////////    private ArrayList<Vehicle> vehicles; // // لیست وسایل نقلیه
////////////////    private ArrayList<TrafficLight> lights; // // لیست چراغ‌ها
////////////////    private double dtSeconds = 0.1; // // گام زمانی (ثانیه)
////////////////
////////////////    public World() { // // سازندهٔ پیش‌فرض
////////////////        this.vehicles = new ArrayList<Vehicle>(); // // ساخت لیست وسایل
////////////////        this.lights = new ArrayList<TrafficLight>(); // // ساخت لیست چراغ‌ها
////////////////    }
////////////////
////////////////    public World(CityMap m) { // // سازنده با نقشه
////////////////        this(); // // صدا زدن سازندهٔ پیش‌فرض
////////////////        this.map = m; // // ست کردن نقشه
////////////////    }
////////////////
////////////////    public void setCityMap(CityMap m) { this.map = m; } // // ست نقشه
////////////////    public CityMap getCityMap() { return this.map; } // // گتر نقشه (نام اصلی پیشنهادی)
////////////////
////////////////    // --- آلیاس برای سازگاری با کدهای قدیمی/دیگر فایل‌ها ---
////////////////    public CityMap getMap() { return this.map; } // // آلیاس: همان getCityMap()
////////////////
////////////////    public List<Vehicle> getVehicles() { return this.vehicles; } // // گتر وسایل
////////////////    public void addVehicle(Vehicle v) { // // افزودن وسیله
////////////////        if (v != null) { // // نال‌چک
////////////////            v.setDtSeconds(this.dtSeconds); // // هماهنگ‌سازی گام زمانی
////////////////            this.vehicles.add(v); // // افزودن به لیست
////////////////        }
////////////////    }
////////////////
////////////////    public void addTrafficLight(TrafficLight tl) { if (tl != null) { this.lights.add(tl); } } // // افزودن چراغ
////////////////    public List<TrafficLight> getTrafficLights() { return this.lights; } // // گتر چراغ‌ها
////////////////
////////////////    public void setDtSeconds(double dt) { // // ست گام زمانی
////////////////        if (dt > 0) { // // اعتبارسنجی
////////////////            this.dtSeconds = dt; // // ذخیره
////////////////            for (int i = 0; i < this.vehicles.size(); i++) { // // برای همهٔ وسایل
////////////////                this.vehicles.get(i).setDtSeconds(dt); // // همگام‌سازی dt
////////////////            }
////////////////        }
////////////////    }
////////////////
////////////////    @Override
////////////////    public void update() { // // حلقهٔ به‌روزرسانی دنیا
////////////////        // 1) آپدیت چراغ‌ها (اگر TrafficLight خودش Updatable است)
////////////////        for (int i = 0; i < this.lights.size(); i++) { // // حلقه چراغ‌ها
////////////////            this.lights.get(i).update(); // // آپدیت چراغ
////////////////        }
////////////////
////////////////        // 2) قوانین: ایست پشت چراغ + فاصلهٔ ایمن در هر لِین
////////////////        final double STOP_DIST = 22.0; // // فاصلهٔ توقف از انتهای لِین (px)
////////////////        final double SAFE_GAP_MIN = 24.0; // // حداقل فاصلهٔ ایمن طولی در یک لِین (px)
////////////////
////////////////        for (int i = 0; i < this.vehicles.size(); i++) { // // حلقهٔ وسایل
////////////////            Vehicle v = this.vehicles.get(i); // // وسیلهٔ فعلی
////////////////            Lane ln = v.getCurrentLane(); // // لِین فعلی
////////////////            if (ln == null) { v.update(); continue; } // // اگر لِین ندارد
////////////////
////////////////            Road rd = ln.getParentRoad(); // // جادهٔ والد
////////////////            if (rd == null) { v.update(); continue; } // // اگر جاده ندارد
////////////////
////////////////            // --- ایست پشت چراغ قرمز/زرد ---
////////////////            double laneLen = ln.getLength(); // // طول لِین
////////////////            double distToEnd = laneLen - v.getPositionInLane(); // // فاصله تا انتها
////////////////            if (distToEnd <= STOP_DIST) { // // نزدیک انتها
////////////////                Intersection end = rd.getEnd(); // // انتهای هندسی
////////////////                if (ln.getDirection() == Direction.WEST || ln.getDirection() == Direction.NORTH) { // // اگر لِین معکوس محور است
////////////////                    end = rd.getStart(); // // انتها = start
////////////////                }
////////////////                TrafficControlDevice dev = end.getControl(ln.getDirection()); // // کنترل جهت
////////////////                if (dev instanceof TrafficLight) { // // اگر چراغ است
////////////////                    LightState st = ((TrafficLight) dev).getState(); // // وضعیت چراغ
////////////////                    if (st == LightState.RED || st == LightState.YELLOW) { // // قرمز یا زرد
////////////////                        v.setTargetSpeed(0.0); // // توقف
////////////////                    }
////////////////                }
////////////////            }
////////////////
////////////////            // --- فاصلهٔ ایمن ساده (Car-Following) در همان لِین ---
////////////////            double myPos = v.getPositionInLane(); // // موقعیت طولی خودروی فعلی
////////////////            Vehicle lead = null; // // خودروی جلویی
////////////////            double leadDelta = Double.POSITIVE_INFINITY; // // فاصله تا جلویی
////////////////
////////////////            for (int j = 0; j < this.vehicles.size(); j++) { // // جست‌وجوی جلویی
////////////////                if (i == j) continue; // // خودِ v نیست
////////////////                Vehicle u = this.vehicles.get(j); // // کاندید
////////////////                if (u.getCurrentLane() != ln) continue; // // باید در همان لِین باشد
////////////////                double d = u.getPositionInLane() - myPos; // // فاصلهٔ طولی
////////////////                if (d > 0 && d < leadDelta) { // // اگر جلو و نزدیک‌تر است
////////////////                    leadDelta = d; // // ثبت فاصله
////////////////                    lead = u; // // ثبت جلویی
////////////////                }
////////////////            }
////////////////
////////////////            if (lead != null) { // // اگر جلویی داریم
////////////////                double desiredGap = Math.max(SAFE_GAP_MIN, v.getSpeed() * 0.6); // // فاصله ایمن دینامیک
////////////////                if (leadDelta < desiredGap) { // // اگر خیلی نزدیک شدیم
////////////////                    double newTarget = Math.min(v.getTargetSpeed(), Math.max(0.0, lead.getSpeed() * 0.9)); // // هم‌سرعت‌سازی ملایم
////////////////                    v.setTargetSpeed(newTarget); // // اعمال سرعت هدف
////////////////                }
////////////////            }
////////////////
////////////////            v.update(); // // اجرای منطق حرکت خود وسیله
////////////////        }
////////////////    }
////////////////}
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
////////////////package simulation; // // پکیج simulation
////////////////
////////////////import java.util.ArrayList; // // لیست
////////////////import java.util.List; // // اینترفیس
////////////////import core.*; // // Vehicle و ...
////////////////import infrastructure.*; // // Lane/Road/Intersection
////////////////import trafficcontrol.*; // // چراغ‌ها
////////////////
////////////////public class World implements Updatable { // // دنیای شبیه‌سازی
////////////////    private CityMap map; // // نقشه
////////////////    private ArrayList<Vehicle> vehicles; // // وسایل
////////////////    private ArrayList<TrafficLight> lights; // // چراغ‌ها
////////////////    private double dtSeconds = 0.1; // // گام زمانی
////////////////
////////////////    public World() { // // سازنده
////////////////        this.vehicles = new ArrayList<Vehicle>(); // // لیست وسایل
////////////////        this.lights = new ArrayList<TrafficLight>(); // // لیست چراغ‌ها
////////////////    }
////////////////
////////////////    public World(CityMap m) { this(); this.map = m; } // // سازنده با نقشه
////////////////    public void setCityMap(CityMap m) { this.map = m; } // // ست نقشه
////////////////    public CityMap getCityMap() { return this.map; } // // گتر نقشه
////////////////    public List<Vehicle> getVehicles() { return this.vehicles; } // // گتر وسایل
////////////////    public void addVehicle(Vehicle v) { if (v != null) { v.setDtSeconds(this.dtSeconds); this.vehicles.add(v); } } // // افزودن وسیله
////////////////    public void addTrafficLight(TrafficLight tl) { if (tl != null) { this.lights.add(tl); } } // // افزودن چراغ
////////////////    public List<TrafficLight> getTrafficLights() { return this.lights; } // // گتر چراغ‌ها
////////////////    public void setDtSeconds(double dt) { if (dt > 0) { this.dtSeconds = dt; for (int i = 0; i < this.vehicles.size(); i++) { this.vehicles.get(i).setDtSeconds(dt); } } } // // ست dt
////////////////
////////////////    @Override
////////////////    public void update() { // // بروزرسانی دنیا
////////////////        // 1) آپدیت چراغ‌ها
////////////////        for (int i = 0; i < this.lights.size(); i++) { this.lights.get(i).update(); } // // آپدیت چراغ
////////////////
////////////////        // 2) قوانین: ایست پشت چراغ + فاصلهٔ ایمن داخل هر لِین
////////////////        final double STOP_DIST = 22.0; // // فاصلهٔ توقف از انتهای جاده (px)
////////////////        final double SAFE_GAP_MIN = 24.0; // // حداقل فاصلهٔ ایمن بین دو خودرو در یک لِین (px)
////////////////
////////////////        for (int i = 0; i < this.vehicles.size(); i++) { // // حلقه وسایل
////////////////            Vehicle v = this.vehicles.get(i); // // خودرو
////////////////            Lane ln = v.getCurrentLane(); // // لِین فعلی
////////////////            if (ln == null) { v.update(); continue; } // // بدون لِین
////////////////
////////////////            Road rd = ln.getParentRoad(); // // جاده
////////////////            if (rd == null) { v.update(); continue; } // // بدون جاده
////////////////
////////////////            // --- ایست پشت چراغ قرمز/زرد ---
////////////////            double laneLen = ln.getLength(); // // طول لِین
////////////////            double distToEnd = laneLen - v.getPositionInLane(); // // فاصله تا انتها
////////////////            if (distToEnd <= STOP_DIST) { // // نزدیک انتهای لِین
////////////////                Intersection end = rd.getEnd(); // // انتهای هندسی
////////////////                if (ln.getDirection() == Direction.WEST || ln.getDirection() == Direction.NORTH) { end = rd.getStart(); } // // اگر جهت معکوس
////////////////                TrafficControlDevice dev = end.getControl(ln.getDirection()); // // کنترل جهت
////////////////                if (dev instanceof TrafficLight) { // // اگر چراغ
////////////////                    LightState st = ((TrafficLight) dev).getState(); // // وضعیت
////////////////                    if (st == LightState.RED || st == LightState.YELLOW) { // // قرمز/زرد
////////////////                        v.setTargetSpeed(0.0); // // توقف
////////////////                    }
////////////////                }
////////////////            }
////////////////
////////////////            // --- فاصلهٔ ایمن داخل همان لِین (Car-Following ساده) ---
////////////////            double myPos = v.getPositionInLane(); // // موقعیت طولی خودرو
////////////////            Vehicle lead = null; // // خودرو جلویی
////////////////            double leadDelta = Double.POSITIVE_INFINITY; // // فاصله تا جلویی
////////////////
////////////////            for (int j = 0; j < this.vehicles.size(); j++) { // // حلقه برای پیدا کردن جلویی
////////////////                if (i == j) continue; // // خودِ خودرو نباشد
////////////////                Vehicle u = this.vehicles.get(j); // // کاندید
////////////////                if (u.getCurrentLane() != ln) continue; // // باید در همان لِین باشد
////////////////                double d = u.getPositionInLane() - myPos; // // فاصلهٔ طولی u از v
////////////////                if (d > 0 && d < leadDelta) { // // فقط جلویی‌ها (فاصله مثبتِ کمتر)
////////////////                    leadDelta = d; // // ثبت فاصله بهتر
////////////////                    lead = u; // // ثبت جلویی
////////////////                }
////////////////            }
////////////////
////////////////            if (lead != null) { // // اگر جلویی داریم
////////////////                double desiredGap = Math.max(SAFE_GAP_MIN, v.getSpeed() * 0.6); // // فاصلهٔ ایمن دینامیک (px)
////////////////                if (leadDelta < desiredGap) { // // اگر نزدیک شدیم
////////////////                    // کاهش سرعت هدف تا برخورد نشود (هم‌سرعت با جلویی، یا توقف ملایم)
////////////////                    double newTarget = Math.min(v.getTargetSpeed(), Math.max(0.0, lead.getSpeed() * 0.9)); // // هم‌گرایی به سرعت جلویی
////////////////                    v.setTargetSpeed(newTarget); // // اعمال هدف
////////////////                }
////////////////            }
////////////////
////////////////            v.update(); // // در پایان منطق حرکت داخلی را اجرا کن
////////////////        }
////////////////    }
////////////////}
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
////////////////package simulation; // // پکیج شبیه‌سازی
////////////////
////////////////import core.Vehicle; // // وسیله نقلیه
////////////////import core.VehicleState; // // وضعیت خودرو
////////////////import infrastructure.CityMap; // // نقشه
////////////////import trafficcontrol.TrafficLight; // // چراغ راهنما
////////////////import trafficcontrol.LightState; // // وضعیت چراغ
////////////////
////////////////import java.util.LinkedList; // // لیست پیوندی برای خودروها
////////////////import java.util.List; // // اینترفیس لیست
////////////////
////////////////public class World implements Updatable { // // دنیای شبیه‌سازی
////////////////    private final LinkedList<Vehicle> vehicles; // // فهرست خودروها
////////////////    private final LinkedList<TrafficLight> trafficLights; // // فهرست چراغ‌ها
////////////////    private final LinkedList<pedestrian.Pedestrian> pedestrians; // // فهرست عابرها
////////////////    private final CityMap map; // // نقشه شهر
////////////////    private double dtSeconds = 0.1; // // dt پیش‌فرض (ثانیه)
////////////////
////////////////    public World(CityMap map) { // // سازنده
////////////////        this.map = map; // // ذخیره نقشه
////////////////        this.vehicles = new LinkedList<Vehicle>(); // // ساخت لیست خودرو
////////////////        this.trafficLights = new LinkedList<TrafficLight>(); // // ساخت لیست چراغ
////////////////        this.pedestrians = new LinkedList<pedestrian.Pedestrian>(); // // ساخت لیست عابر
////////////////    }
////////////////
////////////////    public void setDtSeconds(double dt) { // // ست‌کردن dt از سمت Clock
////////////////        if (dt <= 0) dt = 0.1; // // ایمنی
////////////////        this.dtSeconds = dt; // // ذخیره
////////////////        for (int i = 0; i < vehicles.size(); i++) { // // روی خودروها
////////////////            vehicles.get(i).setDtSeconds(dt); // // همگام‌سازی dt
////////////////        }
////////////////    }
////////////////
////////////////    public void addVehicle(Vehicle v) { // // افزودن خودرو
////////////////        if (v != null) { // // بررسی null
////////////////            v.setDtSeconds(dtSeconds); // // تنظیم dt
////////////////            vehicles.add(v); // // افزودن به لیست
////////////////        }
////////////////    }
////////////////
////////////////    public void addTrafficLight(TrafficLight tl) { // // افزودن چراغ
////////////////        if (tl != null) trafficLights.add(tl); // // افزودن
////////////////    }
////////////////
////////////////    public CityMap getMap() { // // گتر نقشه
////////////////        return map; // // خروجی
////////////////    }
////////////////
////////////////    public List<Vehicle> getVehicles() { // // گتر لیست خودروها
////////////////        return vehicles; // // خروجی
////////////////    }
////////////////
////////////////    public List<TrafficLight> getTrafficLights() { // // گتر لیست چراغ‌ها
////////////////        return trafficLights; // // خروجی
////////////////    }
////////////////
////////////////    @Override
////////////////    public void update() { // // تیک شبیه‌سازی
////////////////        // ۱) آپدیت چراغ‌ها
////////////////        for (int i = 0; i < trafficLights.size(); i++) { // // حلقه چراغ‌ها
////////////////            trafficLights.get(i).update(); // // آپدیت چراغ
////////////////        }
////////////////
////////////////        // ۲) تعیین targetSpeed ساده (براساس چراغ نزدیکِ انتهای لِین)
////////////////        for (int i = 0; i < vehicles.size(); i++) { // // حلقه خودروها
////////////////            Vehicle v = vehicles.get(i); // // خودرو
////////////////            double target = 42.0; // // سرعت هدف پایه (قابل تنظیم)
////////////////            // بازه ترمز قبل از انتهای لِین:
////////////////            double laneLen = (v.getCurrentLane() != null) ? v.getCurrentLane().getLength() : 0; // // طول لِین
////////////////            double distToEnd = laneLen - v.getPositionInLane(); // // فاصله تا انتها
////////////////            if (v.getCurrentLane() != null && distToEnd < 45) { // // اگر نزدیک انتهای لِینیم
////////////////                infrastructure.Intersection end = v.getCurrentLane().getParentRoad().getEndIntersection(); // // تقاطع انتها
////////////////                trafficcontrol.TrafficControlDevice dev =
////////////////                        end.getControl(v.getCurrentLane().getDirection()); // // کنترلِ جهتِ لِین
////////////////                if (dev instanceof TrafficLight) { // // اگر چراغ است
////////////////                    LightState st = ((TrafficLight) dev).getState(); // // وضعیت چراغ
////////////////                    if (st == LightState.RED) target = 0; // // قرمز = توقف
////////////////                    else if (st == LightState.YELLOW) target = Math.min(target, 18); // // زرد = کند
////////////////                }
////////////////            }
////////////////            v.setTargetSpeed(target); // // اعمال سرعت هدف
////////////////        }
////////////////
////////////////        // ۳) آپدیت حرکت خودروها
////////////////        for (int i = 0; i < vehicles.size(); i++) { // // حلقه خودروها
////////////////            vehicles.get(i).update(); // // محاسبه حرکت
////////////////        }
////////////////
////////////////        // ۴) TODO: تصادف/تداخل و مسیر بعدی را در آینده کامل می‌کنیم
////////////////    }
////////////////}
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
////////////////package simulation; // // پکیج شبیه‌سازی
////////////////
////////////////import core.Vehicle; // // لیست خودروها
////////////////import core.Direction; // // جهت لِین برای یافتن تقاطع انتهایی
////////////////import infrastructure.CityMap; // // نقشه
////////////////import infrastructure.Intersection; // // تقاطع
////////////////import infrastructure.Lane; // // لِین
////////////////import pedestrian.Pedestrian; // // عابر
////////////////import trafficcontrol.TrafficControlDevice; // // دستگاه کنترل (چراغ/تابلو)
////////////////import trafficcontrol.TrafficLight; // // چراغ راهنما
////////////////import trafficcontrol.LightState; // // وضعیت چراغ
////////////////
////////////////import java.util.ArrayList; // // ساخت لیست‌ها
////////////////import java.util.List; // // لیست‌ها
////////////////
////////////////public class World implements Updatable { // // قلب شبیه‌سازی
////////////////    private final List<Vehicle> vehicles; // // همه خودروها
////////////////    private final List<TrafficLight> trafficLights; // // چراغ‌ها
////////////////    private final List<Pedestrian> pedestrians; // // عابرها
////////////////    private final CityMap cityMap; // // نقشه
////////////////    private double dtSeconds = 0.1; // // مدت هر تیک شبیه‌سازی بر حسب ثانیه (با Clock هماهنگ می‌شود)
////////////////
////////////////    public World(CityMap cityMap) {
////////////////        this.cityMap = cityMap; // // نگه‌داری نقشه
////////////////        this.vehicles = new ArrayList<>(); // // لیست خودرو
////////////////        this.trafficLights = new ArrayList<>(); // // لیست چراغ‌ها
////////////////        this.pedestrians = new ArrayList<>(); // // لیست عابر
////////////////    }
////////////////
////////////////    // -------- ثبت موجودیت‌ها --------
////////////////    public void addVehicle(Vehicle v) { vehicles.add(v); }
////////////////    public void addTrafficLight(TrafficLight light) { trafficLights.add(light); }
////////////////    public void addPedestrian(Pedestrian p) { pedestrians.add(p); }
////////////////
////////////////    // -------- دسترسی‌ها --------
////////////////    public List<Vehicle> getVehicles() { return vehicles; }
////////////////    public List<TrafficLight> getTrafficLights() { return trafficLights; }
////////////////    public List<Pedestrian> getPedestrians() { return pedestrians; }
////////////////    public CityMap getCityMap() { return cityMap; }
////////////////
////////////////    // -------- تنظیم dt از بیرون (Clock/UI) --------
////////////////    public void setDtSeconds(double dtSeconds) {
////////////////        if (dtSeconds <= 0) dtSeconds = 0.1; // // جلوگیری از مقدار نامعتبر
////////////////        this.dtSeconds = dtSeconds;
////////////////    }
////////////////
////////////////    @Override
////////////////    public void update() {
////////////////        // 1) آپدیت چراغ‌ها (تعویض RED/YELLOW/GREEN با تایمر داخلی خودشان)
////////////////        for (TrafficLight light : trafficLights) {
////////////////            light.update();
////////////////        }
////////////////
////////////////        // 2) تصمیم‌گیری برای سرعت هدف هر خودرو طبق قوانین چراغ/فاصله ترمز
////////////////        for (Vehicle v : vehicles) {
////////////////            v.setDtSeconds(dtSeconds); // // زمان این تیک را به خودرو بده تا حرکت نرم باشد
////////////////            applyTrafficRules(v);      // // قوانین: قرمز/زرد/سبز + نزدیک تقاطع
////////////////            v.update();                // // حرکت خودرو با شتاب/ترمز به سمت targetSpeed
////////////////        }
////////////////
////////////////        // 3) آپدیت ساده عابرها (فعلاً نمایشی)
////////////////        for (Pedestrian p : pedestrians) {
////////////////            p.update();
////////////////        }
////////////////    }
////////////////
////////////////    // ================= قوانین ترافیک برای یک Vehicle =================
////////////////    private void applyTrafficRules(Vehicle v) {
////////////////        // اگر لِین ندارد، هدف سرعت صفر
////////////////        Lane lane = v.getCurrentLane();
////////////////        if (lane == null) {
////////////////            v.setTargetSpeed(0);
////////////////            return;
////////////////        }
////////////////
////////////////        // محدودیت پیش‌فرض
////////////////        double limit = SimulationConfig.DEFAULT_SPEED_LIMIT; // // سرعت مجاز پایه
////////////////        double cruising = Math.min(limit, v.getMaxSpeed()); // // سرعت کروز عادی
////////////////
////////////////        // فاصله تا انتهای همین لِین
////////////////        double distToEnd = distanceToLaneEnd(v);
////////////////
////////////////        // فاصله ترمزگیری تقریبی + حاشیه اطمینان
////////////////        double brakingDistance = (v.getSpeed() * v.getSpeed()) / (2.0 * Math.max(0.1, v.getDeceleration())) + 10.0;
////////////////
////////////////        // اگر نزدیک انتهای لِین هستیم، وضعیت چراغ تقاطع انتهایی را بررسی کن
////////////////        if (distToEnd <= Math.max(5.0, brakingDistance)) {
////////////////            TrafficControlDevice control = getControlAtLaneEnd(lane); // // چراغ/تابلو سمت همین جهت
////////////////            if (control instanceof TrafficLight) {
////////////////                LightState state = ((TrafficLight) control).getState();
////////////////                if (state == LightState.RED) {
////////////////                    v.setTargetSpeed(0); // // قرمز ⇒ توقف
////////////////                    return;
////////////////                } else if (state == LightState.YELLOW) {
////////////////                    v.setTargetSpeed(cruising * 0.5); // // زرد ⇒ کاهش نرم سرعت
////////////////                    return;
////////////////                } else {
////////////////                    v.setTargetSpeed(cruising); // // سبز ⇒ حرکت عادی
////////////////                    return;
////////////////                }
////////////////            } else if (control != null) {
////////////////                // اگر تابلوی Stop/Yield بود، می‌شود اینجا منطقش را اضافه کرد (فاز بعد)
////////////////                v.setTargetSpeed(cruising * 0.6); // // احتیاطی
////////////////                return;
////////////////            } else {
////////////////                // بدون کنترل ⇒ حرکت عادی
////////////////                v.setTargetSpeed(cruising);
////////////////                return;
////////////////            }
////////////////        }
////////////////
////////////////        // دور از تقاطع ⇒ حرکت عادی با سرعت کروز
////////////////        v.setTargetSpeed(cruising);
////////////////    }
////////////////
////////////////    // فاصله باقی‌مانده تا انتهای لِین فعلی
////////////////    private double distanceToLaneEnd(Vehicle v) {
////////////////        Lane lane = v.getCurrentLane();
////////////////        if (lane == null) return 0;
////////////////
////////////////        // طول لِین = فاصله بین دو تقاطع Road
////////////////        var A = lane.getParentRoad().getStartIntersection().getPosition();
////////////////        var B = lane.getParentRoad().getEndIntersection().getPosition();
////////////////
////////////////        double dx = B.getX() - A.getX();
////////////////        double dy = B.getY() - A.getY();
////////////////        double laneLength = Math.sqrt(dx * dx + dy * dy);
////////////////
////////////////        double remaining = Math.max(0, laneLength - v.getPositionInLane());
////////////////        return remaining;
////////////////    }
////////////////
////////////////    // دستگاه کنترل در انتهای لِین (چراغ/تابلو) مطابق جهت لِین
////////////////    private TrafficControlDevice getControlAtLaneEnd(Lane lane) {
////////////////        Intersection end = getLaneEndIntersection(lane);
////////////////        if (end == null) return null;
////////////////
////////////////        // طبق طراحی Intersection یک Map<Direction, TrafficControlDevice> دارد
////////////////        // اینجا فرض می‌کنیم متدی برای گرفتن کنترلِ یک جهت داریم:
////////////////        return end.getControl(lane.getDirection()); // // اگر وجود داشته باشد همان را برمی‌گرداند، وگرنه null
////////////////    }
////////////////
////////////////    // تعیین اینکه انتهای لِین کدام تقاطع است (با توجه به جهت)
////////////////    private Intersection getLaneEndIntersection(Lane lane) {
////////////////        Direction d = lane.getDirection();
////////////////        if (d == Direction.EAST || d == Direction.SOUTH) {
////////////////            return lane.getParentRoad().getEndIntersection(); // // لِین‌های EAST/SOUTH را به سمت end می‌گیریم
////////////////        } else {
////////////////            return lane.getParentRoad().getStartIntersection(); // // لِین‌های WEST/NORTH را به سمت start می‌گیریم
////////////////        }
////////////////    }
////////////////}
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
//////////////
////////////////package simulation;
////////////////
////////////////import core.Vehicle;
////////////////import infrastructure.CityMap;
////////////////import trafficcontrol.TrafficLight;
////////////////import pedestrian.Pedestrian;
////////////////
////////////////import java.util.ArrayList;
////////////////import java.util.List;
////////////////
////////////////public class World implements Updatable {
////////////////    private List<Vehicle> vehicles;
////////////////    private List<TrafficLight> trafficLights;
////////////////    private List<Pedestrian> pedestrians;
////////////////    private CityMap cityMap;
////////////////
////////////////    public World(CityMap cityMap) {
////////////////        this.cityMap = cityMap;
////////////////        this.vehicles = new ArrayList<>();
////////////////        this.trafficLights = new ArrayList<>();
////////////////        this.pedestrians = new ArrayList<>();
////////////////    }
////////////////
////////////////    public void addVehicle(Vehicle v) {
////////////////        vehicles.add(v);
////////////////    }
////////////////
////////////////    public void addTrafficLight(TrafficLight light) {
////////////////        trafficLights.add(light);
////////////////    }
////////////////
////////////////    public void addPedestrian(Pedestrian p) {
////////////////        pedestrians.add(p);
////////////////    }
////////////////
////////////////    public List<Vehicle> getVehicles() {
////////////////        return vehicles;
////////////////    }
////////////////
////////////////    public List<TrafficLight> getTrafficLights() {
////////////////        return trafficLights;
////////////////    }
////////////////
////////////////    public List<Pedestrian> getPedestrians() {
////////////////        return pedestrians;
////////////////    }
////////////////
////////////////    public CityMap getCityMap() {
////////////////        return cityMap;
////////////////    }
////////////////
////////////////    @Override
////////////////    public void update() {
////////////////        for (TrafficLight light : trafficLights) {
////////////////            light.update();
////////////////        }
////////////////        for (Vehicle v : vehicles) {
////////////////            v.update();
////////////////        }
////////////////        for (Pedestrian p : pedestrians) {
////////////////            p.update();
////////////////        }
////////////////    }
////////////////}
