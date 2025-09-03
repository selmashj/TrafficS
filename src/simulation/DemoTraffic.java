package simulation; // // پکیج simulation

import core.Direction; // // جهت
import core.Vehicle; // // خودرو
import core.VehicleType; // // نوع خودرو
import core.DriverProfile; // // پروفایل راننده
import infrastructure.CityMap; // // نقشه
import infrastructure.Intersection; // // تقاطع
import infrastructure.Road; // // جاده
import infrastructure.Lane; // // لِین
import trafficcontrol.TrafficLight; // // چراغ
import trafficcontrol.LightState; // // حالت چراغ
import trafficcontrol.TrafficControlDevice; // // اینترفیس کنترل

import java.util.ArrayList; // // لیست کمکی
import java.util.List; // // اینترفیس لیست
import java.util.Random; // // رندوم

public final class DemoTraffic { // // کلاس کمکی ترافیک دمو
    private DemoTraffic() {} // // جلوگیری از نمونه‌سازی
    private static final Random rnd = new Random(); // // رندوم مشترک

    // ---------- نصب چراغ روی همهٔ جهت‌های هر تقاطع ----------
    public static void installLights(World world, CityMap map, int greenMs, int yellowMs, int redMs) { // // نصب چراغ‌ها
        List<Intersection> xs = map.getIntersections(); // // همه تقاطع‌ها
        for (int i = 0; i < xs.size(); i++) { // // حلقه روی تقاطع‌ها
            Intersection it = xs.get(i); // // تقاطع
            attachIfMissing(world, it, Direction.NORTH, greenMs, yellowMs, redMs); // // شمال
            attachIfMissing(world, it, Direction.SOUTH, greenMs, yellowMs, redMs); // // جنوب
            attachIfMissing(world, it, Direction.EAST,  greenMs, yellowMs, redMs); // // شرق
            attachIfMissing(world, it, Direction.WEST,  greenMs, yellowMs, redMs); // // غرب
        }
    }

    private static void attachIfMissing(World world, Intersection it, Direction d, int g, int y, int r) { // // وصل کردن چراغ
        TrafficControlDevice dev = it.getControl(d); // // کنترل فعلی
        if (dev == null) { // // اگر چیزی وصل نیست
            TrafficLight tl = new TrafficLight(
                    "TL-" + it.getId() + "-" + d, // // ID یکتا
                    d,                            // // جهت کنترل‌شونده
                    g, y, r,                      // // مدت‌ها (ms)
                    LightState.GREEN              // // حالت شروع (رفع خطا: بجای int)
            );
            it.setControl(d, tl);       // // وصل به تقاطع
            world.addTrafficLight(tl);  // // ثبت در World برای آپدیت دوره‌ای
        }
    }

    // ---------- ریختن چند خودرو تستی روی لِین‌های تصادفی ----------
    public static void seedVehicles(World world, CityMap map, SimulationClock clock, int count) { // // افزودن خودرو
        ArrayList<Lane> lanes = new ArrayList<Lane>(); // // لیست همه لِین‌ها
        List<Road> roads = map.getRoads(); // // همه جاده‌ها
        for (int i = 0; i < roads.size(); i++) { // // حلقه روی جاده‌ها
            Road r = roads.get(i); // // جاده
            lanes.addAll(r.getForwardLanes()); // // لِین‌های رفت
            lanes.addAll(r.getBackwardLanes()); // // لِین‌های برگشت
        }
        if (lanes.isEmpty()) return; // // اگر هیچ لِینی نداریم خروج

        for (int n = 0; n < count; n++) { // // به تعداد خواسته
            Lane lane = lanes.get(rnd.nextInt(lanes.size())); // // یک لِین رندوم
            Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING); // // خودرو
            v.setCurrentLane(lane); // // قرار دادن روی لِین
            // // جای شروع درست با توجه به جهت لِین:
            double L = lane.getLength(); // // طول لِین
            if (lane.getDirection() == Direction.EAST || lane.getDirection() == Direction.SOUTH) v.setPositionInLane(Math.min(40, L*0.25)); // // شروع نزدیک A
            else v.setPositionInLane(Math.max(0, L - 40)); // // شروع نزدیک B
            v.setTargetSpeed(38 + rnd.nextInt(15)); // // سرعت هدف اولیه
            world.addVehicle(v); // // افزودن به دنیا
        }
    }

    // ---------- یوتیلیتی: افزودن یک خودرو کاملاً تصادفی ----------
    public static Vehicle addRandomVehicle(World world, CityMap map) { // // افزودن تک خودرو
        ArrayList<Lane> lanes = new ArrayList<Lane>(); // // جمع‌کردن لِین‌ها
        List<Road> roads = map.getRoads(); // // جاده‌ها
        for (int i = 0; i < roads.size(); i++) { // // حلقه
            Road r = roads.get(i); // // جاده
            lanes.addAll(r.getForwardLanes()); // // رفت
            lanes.addAll(r.getBackwardLanes()); // // برگشت
        }
        if (lanes.isEmpty()) return null; // // بدون لِین

        Lane lane = lanes.get(rnd.nextInt(lanes.size())); // // انتخاب لِین
        Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING); // // خودرو
        double L = lane.getLength(); // // طول
        if (lane.getDirection() == Direction.EAST || lane.getDirection() == Direction.SOUTH) v.setPositionInLane(Math.min(30, L*0.2)); else v.setPositionInLane(Math.max(0, L-30)); // // موضع اولیه
        v.setCurrentLane(lane); // // ست لِین
        v.setTargetSpeed(36 + rnd.nextInt(18)); // // هدف سرعت
        world.addVehicle(v); // // افزودن
        return v; // // بازگشت
    }

    private static core.VehicleType randomType() { // // انتخاب نوع خودرو تصادفی
        core.VehicleType[] vals = core.VehicleType.values(); // // آرایه انواع
        return vals[rnd.nextInt(vals.length)]; // // یکی تصادفی
    }
}































//77777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777
//
//package simulation; // پکیج //
//
//import core.*; // Direction, Vehicle, VehicleType, DriverProfile, Point //
//import infrastructure.*; // CityMap, Intersection, Road, Lane //
//import pedestrian.*; // Pedestrian, PedestrianCrossing //
//import trafficcontrol.*; // TrafficLight, LightState //
//
//import java.util.*; // لیست/رندوم //
//
//public final class DemoTraffic { // کلاس کمکی //
//    private DemoTraffic() {} // جلوگیری از نمونه‌سازی //
//    private static final Random rnd = new Random(); // رندوم //
//
//    // نصب چراغ‌های راهنمایی هماهنگ در هر تقاطع //
//    public static void installLights(World world, CityMap map, int green, int yellow, int red) { // //
//        List<Intersection> xs = map.getIntersections(); // تقاطع‌ها //
//        for (int i = 0; i < xs.size(); i++) { // هر تقاطع //
//            Intersection it = xs.get(i); // تقاطع //
//
//            TrafficLight north = new TrafficLight("TL-" + it.getId() + "-N", Direction.NORTH, green, yellow, red, LightState.GREEN); // NS سبز //
//            TrafficLight south = new TrafficLight("TL-" + it.getId() + "-S", Direction.SOUTH, green, yellow, red, LightState.GREEN); // //
//            TrafficLight east  = new TrafficLight("TL-" + it.getId() + "-E", Direction.EAST,  green, yellow, red, LightState.RED);   // EW قرمز //
//            TrafficLight west  = new TrafficLight("TL-" + it.getId() + "-W", Direction.WEST,  green, yellow, red, LightState.RED);   // //
//
//            it.setControl(Direction.NORTH, north); // ثبت در تقاطع //
//            it.setControl(Direction.SOUTH, south); // //
//            it.setControl(Direction.EAST,  east);  // //
//            it.setControl(Direction.WEST,  west);  // //
//
//            world.addTrafficLight(north); // افزودن به دنیا //
//            world.addTrafficLight(south); // //
//            world.addTrafficLight(east);  // //
//            world.addTrafficLight(west);  // //
//
//            world.addSynchronizedLights(north, south, east, west); // ✅ فیکس: ثبت گروه هماهنگ //
//        }
//    }
//
//    // ریختن چند خودروی اولیه //
//    public static void seedVehicles(World world, CityMap map, SimulationClock clock, int count) { // //
//        ArrayList<Lane> lanes = new ArrayList<Lane>(); // جمع‌آوری لاین‌ها //
//        List<Road> roads = map.getRoads(); // //
//        for (int i = 0; i < roads.size(); i++) {
//            Road r = roads.get(i);
//            lanes.addAll(r.getForwardLanes());
//            lanes.addAll(r.getBackwardLanes());
//        }
//        if (lanes.isEmpty()) return; // محافظه‌کار //
//
//        for (int n = 0; n < count; n++) { // ساخت خودرو //
//            Lane lane = lanes.get(rnd.nextInt(lanes.size())); // لاین رندوم //
//            Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING); // //
//            v.setCurrentLane(lane); // لاین //
//            v.setPositionInLane(rnd.nextInt(40)); // موقعیت //
//            v.setTargetSpeed(38 + rnd.nextInt(15)); // سرعت هدف //
//            world.addVehicle(v); // افزودن //
//        }
//    }
//
//    // افزودن یک خودرو رندوم //
//    public static Vehicle addRandomVehicle(World world, CityMap map) { // //
//        ArrayList<Lane> lanes = new ArrayList<Lane>(); // //
//        List<Road> roads = map.getRoads(); // //
//        for (int i = 0; i < roads.size(); i++) {
//            Road r = roads.get(i);
//            lanes.addAll(r.getForwardLanes());
//            lanes.addAll(r.getBackwardLanes());
//        }
//        if (lanes.isEmpty()) return null; // //
//
//        Lane lane = lanes.get(rnd.nextInt(lanes.size())); // //
//        Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING); // //
//        v.setCurrentLane(lane); // //
//        v.setPositionInLane(rnd.nextInt(30)); // //
//        v.setTargetSpeed(36 + rnd.nextInt(18)); // //
//        world.addVehicle(v); // //
//        return v; // //
//    }
//
//    private static VehicleType randomType() { // //
//        VehicleType[] vals = VehicleType.values(); // //
//        return vals[rnd.nextInt(vals.length)]; // //
//    }
//
//    // افزودن گذرگاه و عابر (۴ عدد به صورت رندوم) //
//    public static void addPedestrians(World world, CityMap map) { // //
//        List<Intersection> xs = map.getIntersections(); // //
//        if (xs.size() < 4) return; // //
//
//        for (int i = 0; i < 4; i++) { // چهار بار //
//            Intersection it = xs.get(rnd.nextInt(xs.size())); // تقاطع //
//            Direction d = Direction.values()[rnd.nextInt(Direction.values().length)]; // جهت //
//            PedestrianCrossing crossing = new PedestrianCrossing("PC-" + it.getId() + "-" + d, it, d, true); // //
//            Pedestrian p = new Pedestrian("P-" + System.nanoTime(),
//                    new Point(it.getPosition().getX(), it.getPosition().getY()), crossing); // //
//            world.addPedestrian(p); // ✅ فیکس: متد addPedestrian در World وجود دارد //
//        }
//    }
//
//    // راه‌اندازی اولیه //
//    public static void setup(World world, CityMap map, SimulationClock clock) { // //
//        installLights(world, map, 35, 5, 30); // چراغ‌ها //
//        seedVehicles(world, map, clock, 70);  // ۷۰ خودرو //
//        addPedestrians(world, map);           // عابرها //
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
////package simulation;
////
////import core.Direction; // جهت //
////import core.Vehicle; // وسیله نقلیه //
////import core.VehicleType; // نوع خودرو //
////import core.DriverProfile; // پروفایل راننده //
////import core.Point; // نقطه //
////import infrastructure.CityMap; // نقشه //
////import infrastructure.Intersection; // تقاطع //
////import infrastructure.Road; // جاده //
////import infrastructure.Lane; // لاین //
////import pedestrian.Pedestrian; // عابر //
////import pedestrian.PedestrianCrossing; // گذرگاه //
////import trafficcontrol.TrafficLight; // چراغ //
////import trafficcontrol.LightState; // وضعیت چراغ //
////
////import java.util.ArrayList; //
////import java.util.List; //
////import java.util.Random; //
////
////// پیکربندی و آماده‌سازی سناریو دموی ترافیک //
////public final class DemoTraffic { //
////    private DemoTraffic() {} // جلوگیری از ساخت //
////    private static final Random rnd = new Random(); // رندوم //
////
////    // نصب چراغ‌ها و ثبت گروه‌های هماهنگ در World //
////    public static void installLights(World world, CityMap map, int green, int yellow, int red) { //
////        List<Intersection> xs = map.getIntersections(); // لیست تقاطع‌ها //
////        for (int i = 0; i < xs.size(); i++) { // حلقه روی تقاطع‌ها //
////            Intersection it = xs.get(i); // گرفتن تقاطع //
////
////            // ساخت چراغ‌ها با وضعیت اولیه هماهنگ //
////            TrafficLight north = new TrafficLight("TL-" + it.getId() + "-N", Direction.NORTH, green, yellow, red, LightState.GREEN); // N سبز //
////            TrafficLight south = new TrafficLight("TL-" + it.getId() + "-S", Direction.SOUTH, green, yellow, red, LightState.GREEN); // S سبز //
////            TrafficLight east  = new TrafficLight("TL-" + it.getId() + "-E", Direction.EAST,  green, yellow, red, LightState.RED);   // E قرمز //
////            TrafficLight west  = new TrafficLight("TL-" + it.getId() + "-W", Direction.WEST,  green, yellow, red, LightState.RED);   // W قرمز //
////
////            // ثبت کنترل در تقاطع //
////            it.setControl(Direction.NORTH, north); //
////            it.setControl(Direction.SOUTH, south); //
////            it.setControl(Direction.EAST, east); //
////            it.setControl(Direction.WEST, west); //
////
////            // افزودن به world //
////            world.addTrafficLight(north); //
////            world.addTrafficLight(south); //
////            world.addTrafficLight(east); //
////            world.addTrafficLight(west); //
////
////            // ثبت گروه هماهنگ (N/S و E/W) //
////            world.addSynchronizedLights(north, south, east, west); //
////        }
////    }
////
////    // ریختن چند خودروی اولیه //
////    public static void seedVehicles(World world, CityMap map, SimulationClock clock, int count) { //
////        ArrayList<Lane> lanes = new ArrayList<Lane>(); // جمع‌آوری لاین‌ها //
////        List<Road> roads = map.getRoads(); //
////        for (int i = 0; i < roads.size(); i++) { //
////            Road r = roads.get(i); //
////            lanes.addAll(r.getForwardLanes()); //
////            lanes.addAll(r.getBackwardLanes()); //
////        }
////        if (lanes.isEmpty()) return; // اگر لاینی نبود //
////
////        for (int n = 0; n < count; n++) { // ایجاد خودروها //
////            Lane lane = lanes.get(rnd.nextInt(lanes.size())); // لاین تصادفی //
////            Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING); // ساخت //
////            v.setCurrentLane(lane); // ست لاین //
////            v.setPositionInLane(rnd.nextInt(40)); // موقعیت اولیه //
////            v.setTargetSpeed(38 + rnd.nextInt(15)); // سرعت هدف //
////            world.addVehicle(v); // افزودن //
////        }
////    }
////
////    // افزودن یک خودرو رندوم //
////    public static Vehicle addRandomVehicle(World world, CityMap map) { //
////        ArrayList<Lane> lanes = new ArrayList<Lane>(); //
////        List<Road> roads = map.getRoads(); //
////        for (int i = 0; i < roads.size(); i++) { //
////            Road r = roads.get(i); //
////            lanes.addAll(r.getForwardLanes()); //
////            lanes.addAll(r.getBackwardLanes()); //
////        }
////        if (lanes.isEmpty()) return null; //
////
////        Lane lane = lanes.get(rnd.nextInt(lanes.size())); //
////        Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING); //
////        v.setCurrentLane(lane); //
////        v.setPositionInLane(rnd.nextInt(30)); //
////        v.setTargetSpeed(36 + rnd.nextInt(18)); //
////        world.addVehicle(v); //
////        return v; //
////    }
////
////    private static VehicleType randomType() { // انتخاب نوع //
////        VehicleType[] vals = VehicleType.values(); //
////        return vals[rnd.nextInt(vals.length)]; //
////    }
////
////    // افزودن ۴ گذرگاه و عابر //
////    public static void addPedestrians(World world, CityMap map) { //
////        List<Intersection> xs = map.getIntersections(); //
////        if (xs.size() < 4) return; //
////
////        for (int i = 0; i < 4; i++) { // چهار گذرگاه //
////            Intersection it = xs.get(rnd.nextInt(xs.size())); //
////            Direction d = Direction.values()[rnd.nextInt(Direction.values().length)]; //
////            PedestrianCrossing crossing = new PedestrianCrossing("PC-" + it.getId() + "-" + d, it, d, true); //
////            Pedestrian p = new Pedestrian("P-" + System.nanoTime(), new Point(it.getPosition().getX(), it.getPosition().getY()), crossing); //
////            world.addPedestrian(p); //
////        }
////    }
////
////    // راه‌اندازی اولیه //
////    public static void setup(World world, CityMap map, SimulationClock clock) { //
////        installLights(world, map, 35, 5, 30); // نصب چراغ //
////        seedVehicles(world, map, clock, 70); // ۷۰ خودرو اولیه //
////        addPedestrians(world, map); // عابر + گذرگاه //
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
////
////package simulation;
////
////import core.Direction; // جهت //
////import core.Vehicle; // وسیله نقلیه //
////import core.VehicleType; // نوع خودرو //
////import core.DriverProfile; // پروفایل راننده //
////import core.Point; // نقطه //
////import infrastructure.CityMap; // نقشه //
////import infrastructure.Intersection; // تقاطع //
////import infrastructure.Road; // جاده //
////import infrastructure.Lane; // لاین //
////import pedestrian.Pedestrian; // عابر //
////import pedestrian.PedestrianCrossing; // گذرگاه //
////import trafficcontrol.TrafficLight; // چراغ //
////import trafficcontrol.LightState; // وضعیت چراغ //
////
////import java.util.ArrayList; //
////import java.util.List; //
////import java.util.Random; //
////
////// پیکربندی و آماده‌سازی سناریو دموی ترافیک //
////public final class DemoTraffic { //
////    private DemoTraffic() {} // جلوگیری از ساخت //
////    private static final Random rnd = new Random(); // رندوم //
////
////    // نصب چراغ‌ها و ثبت گروه‌های هماهنگ در World //
////    public static void installLights(World world, CityMap map, int green, int yellow, int red) { //
////        List<Intersection> xs = map.getIntersections(); // لیست تقاطع‌ها //
////        for (int i = 0; i < xs.size(); i++) { // حلقه روی تقاطع‌ها //
////            Intersection it = xs.get(i); // گرفتن تقاطع //
////
////            // ساخت چراغ‌ها با وضعیت اولیه هماهنگ //
////            TrafficLight north = new TrafficLight("TL-" + it.getId() + "-N", Direction.NORTH, green, yellow, red, LightState.GREEN); // N سبز //
////            TrafficLight south = new TrafficLight("TL-" + it.getId() + "-S", Direction.SOUTH, green, yellow, red, LightState.GREEN); // S سبز //
////            TrafficLight east  = new TrafficLight("TL-" + it.getId() + "-E", Direction.EAST,  green, yellow, red, LightState.RED);   // E قرمز //
////            TrafficLight west  = new TrafficLight("TL-" + it.getId() + "-W", Direction.WEST,  green, yellow, red, LightState.RED);   // W قرمز //
////
////            // ثبت کنترل در تقاطع //
////            it.setControl(Direction.NORTH, north); //
////            it.setControl(Direction.SOUTH, south); //
////            it.setControl(Direction.EAST, east); //
////            it.setControl(Direction.WEST, west); //
////
////            // افزودن به world //
////            world.addTrafficLight(north); //
////            world.addTrafficLight(south); //
////            world.addTrafficLight(east); //
////            world.addTrafficLight(west); //
////
////            // ثبت گروه هماهنگ (N/S و E/W) //
////            world.addSynchronizedLights(north, south, east, west); //
////        }
////    }
////
////    // ریختن چند خودروی اولیه //
////    public static void seedVehicles(World world, CityMap map, SimulationClock clock, int count) { //
////        ArrayList<Lane> lanes = new ArrayList<Lane>(); // جمع‌آوری لاین‌ها //
////        List<Road> roads = map.getRoads(); //
////        for (int i = 0; i < roads.size(); i++) { //
////            Road r = roads.get(i); //
////            lanes.addAll(r.getForwardLanes()); //
////            lanes.addAll(r.getBackwardLanes()); //
////        }
////        if (lanes.isEmpty()) return; // اگر لاینی نبود //
////
////        for (int n = 0; n < count; n++) { // ایجاد خودروها //
////            Lane lane = lanes.get(rnd.nextInt(lanes.size())); // لاین تصادفی //
////            Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING); // ساخت //
////            v.setCurrentLane(lane); // ست لاین //
////            v.setPositionInLane(rnd.nextInt(40)); // موقعیت اولیه //
////            v.setTargetSpeed(38 + rnd.nextInt(15)); // سرعت هدف //
////            world.addVehicle(v); // افزودن //
////        }
////    }
////
////    // افزودن یک خودرو رندوم //
////    public static Vehicle addRandomVehicle(World world, CityMap map) { //
////        ArrayList<Lane> lanes = new ArrayList<Lane>(); //
////        List<Road> roads = map.getRoads(); //
////        for (int i = 0; i < roads.size(); i++) { //
////            Road r = roads.get(i); //
////            lanes.addAll(r.getForwardLanes()); //
////            lanes.addAll(r.getBackwardLanes()); //
////        }
////        if (lanes.isEmpty()) return null; //
////
////        Lane lane = lanes.get(rnd.nextInt(lanes.size())); //
////        Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING); //
////        v.setCurrentLane(lane); //
////        v.setPositionInLane(rnd.nextInt(30)); //
////        v.setTargetSpeed(36 + rnd.nextInt(18)); //
////        world.addVehicle(v); //
////        return v; //
////    }
////
////    private static VehicleType randomType() { // انتخاب نوع //
////        VehicleType[] vals = VehicleType.values(); //
////        return vals[rnd.nextInt(vals.length)]; //
////    }
////
////    // افزودن ۴ گذرگاه و عابر //
////    public static void addPedestrians(World world, CityMap map) { //
////        List<Intersection> xs = map.getIntersections(); //
////        if (xs.size() < 4) return; //
////
////        for (int i = 0; i < 4; i++) { // چهار گذرگاه //
////            Intersection it = xs.get(rnd.nextInt(xs.size())); //
////            Direction d = Direction.values()[rnd.nextInt(Direction.values().length)]; //
////            PedestrianCrossing crossing = new PedestrianCrossing("PC-" + it.getId() + "-" + d, it, d, true); //
////            Pedestrian p = new Pedestrian("P-" + System.nanoTime(), new Point(it.getPosition().getX(), it.getPosition().getY()), crossing); //
////            world.addPedestrian(p); //
////        }
////    }
////
////    // راه‌اندازی اولیه //
////    public static void setup(World world, CityMap map, SimulationClock clock) { //
////        installLights(world, map, 35, 5, 30); // نصب چراغ //
////        seedVehicles(world, map, clock, 70); // ۷۰ خودرو اولیه //
////        addPedestrians(world, map); // عابر + گذرگاه //
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
//////
//////package simulation;
//////
//////import core.Direction;
//////import core.Vehicle;
//////import core.VehicleType;
//////import core.DriverProfile;
//////import core.Point;
//////import infrastructure.CityMap;
//////import infrastructure.Intersection;
//////import infrastructure.Road;
//////import infrastructure.Lane;
//////import pedestrian.Pedestrian;
//////import pedestrian.PedestrianCrossing;
//////import trafficcontrol.TrafficLight;
//////import trafficcontrol.LightState;
//////
//////import java.util.ArrayList;
//////import java.util.List;
//////import java.util.Random;
//////
//////public final class DemoTraffic {
//////    private DemoTraffic() {}
//////    private static final Random rnd = new Random();
//////
//////    // ---------- نصب چراغ واقعی در تقاطع‌ها ----------
//////    public static void installLights(World world, CityMap map, int green, int yellow, int red) {
//////        List<Intersection> xs = map.getIntersections();
//////        for (int i = 0; i < xs.size(); i++) {
//////            Intersection it = xs.get(i);
//////
//////            // چراغ شمال و جنوب
//////            TrafficLight north = new TrafficLight("TL-" + it.getId() + "-N", Direction.NORTH, green, yellow, red, LightState.GREEN);
//////            TrafficLight south = new TrafficLight("TL-" + it.getId() + "-S", Direction.SOUTH, green, yellow, red, LightState.GREEN);
//////
//////            // چراغ شرق و غرب
//////            TrafficLight east  = new TrafficLight("TL-" + it.getId() + "-E", Direction.EAST,  green, yellow, red, LightState.RED);
//////            TrafficLight west  = new TrafficLight("TL-" + it.getId() + "-W", Direction.WEST,  green, yellow, red, LightState.RED);
//////
//////            // ثبت کنترل در تقاطع
//////            it.setControl(Direction.NORTH, north);
//////            it.setControl(Direction.SOUTH, south);
//////            it.setControl(Direction.EAST, east);
//////            it.setControl(Direction.WEST, west);
//////
//////            // افزودن به جهان
//////            world.addTrafficLight(north);
//////            world.addTrafficLight(south);
//////            world.addTrafficLight(east);
//////            world.addTrafficLight(west);
//////        }
//////    }
//////
//////    // ---------- ریختن چند خودرو تستی ----------
//////    public static void seedVehicles(World world, CityMap map, SimulationClock clock, int count) {
//////        ArrayList<Lane> lanes = new ArrayList<>();
//////        List<Road> roads = map.getRoads();
//////        for (Road r : roads) {
//////            lanes.addAll(r.getForwardLanes());
//////            lanes.addAll(r.getBackwardLanes());
//////        }
//////        if (lanes.isEmpty()) return;
//////
//////        for (int n = 0; n < count; n++) {
//////            Lane lane = lanes.get(rnd.nextInt(lanes.size()));
//////            Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING);
//////            v.setCurrentLane(lane);
//////            v.setPositionInLane(rnd.nextInt(40));
//////            v.setTargetSpeed(38 + rnd.nextInt(15));
//////            world.addVehicle(v);
//////        }
//////    }
//////
//////    // ---------- افزودن یک خودرو رندوم ----------
//////    public static Vehicle addRandomVehicle(World world, CityMap map) {
//////        ArrayList<Lane> lanes = new ArrayList<>();
//////        List<Road> roads = map.getRoads();
//////        for (Road r : roads) {
//////            lanes.addAll(r.getForwardLanes());
//////            lanes.addAll(r.getBackwardLanes());
//////        }
//////        if (lanes.isEmpty()) return null;
//////
//////        Lane lane = lanes.get(rnd.nextInt(lanes.size()));
//////        Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING);
//////        v.setCurrentLane(lane);
//////        v.setPositionInLane(rnd.nextInt(30));
//////        v.setTargetSpeed(36 + rnd.nextInt(18));
//////        world.addVehicle(v);
//////        return v;
//////    }
//////
//////    private static VehicleType randomType() {
//////        VehicleType[] vals = VehicleType.values();
//////        return vals[rnd.nextInt(vals.length)];
//////    }
//////
//////    // ---------- افزودن عابر پیاده + گذرگاه ----------
//////    public static void addPedestrians(World world, CityMap map) {
//////        List<Intersection> xs = map.getIntersections();
//////        if (xs.size() < 4) return;
//////
//////        // ۴ گذرگاه پیاده روی ۴ تقاطع مختلف
//////        for (int i = 0; i < 4; i++) {
//////            Intersection it = xs.get(rnd.nextInt(xs.size()));
//////            Direction d = Direction.values()[rnd.nextInt(Direction.values().length)];
//////            PedestrianCrossing crossing = new PedestrianCrossing("PC-" + it.getId() + "-" + d, it, d, true);
//////
//////            Pedestrian p = new Pedestrian("P-" + System.nanoTime(),
//////                    new Point(it.getPosition().getX(), it.getPosition().getY()), crossing);
//////            world.addPedestrian(p);
//////        }
//////    }
//////
//////    // ---------- راه‌اندازی اولیه ----------
//////    public static void setup(World world, CityMap map, SimulationClock clock) {
//////        installLights(world, map, 35, 5, 30);      // چراغ‌ها
//////        seedVehicles(world, map, clock, 70);       // 🚗 تعداد اولیه ۷۰ ماشین
//////        addPedestrians(world, map);                // 🚶 اضافه کردن عابر و گذرگاه
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
//////
//////
//////
//////
//////
//////
////////
////////package simulation;
////////
////////import core.Direction;
////////import core.Vehicle;
////////import core.VehicleType;
////////import core.DriverProfile;
////////import core.Point;
////////import infrastructure.CityMap;
////////import infrastructure.Intersection;
////////import infrastructure.Road;
////////import infrastructure.Lane;
////////import pedestrian.Pedestrian;
////////import pedestrian.PedestrianCrossing;
////////import trafficcontrol.TrafficLight;
////////import trafficcontrol.TrafficControlDevice;
////////import trafficcontrol.LightState;
////////
////////import java.util.ArrayList;
////////import java.util.List;
////////import java.util.Random;
////////
////////public final class DemoTraffic {
////////    private DemoTraffic() {}
////////    private static final Random rnd = new Random();
////////
////////    // ---------- نصب چراغ واقعی در تقاطع‌ها ----------
////////    public static void installLights(World world, CityMap map, int green, int yellow, int red) {
////////        List<Intersection> xs = map.getIntersections();
////////        for (int i = 0; i < xs.size(); i++) {
////////            Intersection it = xs.get(i);
////////
////////            // چراغ شمال و جنوب
////////            TrafficLight north = new TrafficLight("TL-" + it.getId() + "-N", Direction.NORTH, green, yellow, red, SimulationConfig.TICK_INTERVAL);
////////            TrafficLight south = new TrafficLight("TL-" + it.getId() + "-S", Direction.SOUTH, green, yellow, red, SimulationConfig.TICK_INTERVAL);
////////            // چراغ شرق و غرب
////////            TrafficLight east  = new TrafficLight("TL-" + it.getId() + "-E", Direction.EAST,  green, yellow, red, SimulationConfig.TICK_INTERVAL);
////////            TrafficLight west  = new TrafficLight("TL-" + it.getId() + "-W", Direction.WEST,  green, yellow, red, SimulationConfig.TICK_INTERVAL);
////////
////////            // در شروع: شمال/جنوب سبز، شرق/غرب قرمز
////////            while (north.getState() != LightState.GREEN) north.update();
////////            while (south.getState() != LightState.GREEN) south.update();
////////            while (east.getState()  != LightState.RED)   east.update();
////////            while (west.getState()  != LightState.RED)   west.update();
////////
////////            it.setControl(Direction.NORTH, north);
////////            it.setControl(Direction.SOUTH, south);
////////            it.setControl(Direction.EAST, east);
////////            it.setControl(Direction.WEST, west);
////////
////////            world.addTrafficLight(north);
////////            world.addTrafficLight(south);
////////            world.addTrafficLight(east);
////////            world.addTrafficLight(west);
////////        }
////////    }
////////
////////    // ---------- ریختن چند خودرو تستی ----------
////////    public static void seedVehicles(World world, CityMap map, SimulationClock clock, int count) {
////////        ArrayList<Lane> lanes = new ArrayList<Lane>();
////////        List<Road> roads = map.getRoads();
////////        for (int i = 0; i < roads.size(); i++) {
////////            Road r = roads.get(i);
////////            lanes.addAll(r.getForwardLanes());
////////            lanes.addAll(r.getBackwardLanes());
////////        }
////////        if (lanes.isEmpty()) return;
////////
////////        for (int n = 0; n < count; n++) {
////////            Lane lane = lanes.get(rnd.nextInt(lanes.size()));
////////            Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING);
////////            v.setCurrentLane(lane);
////////            v.setPositionInLane(rnd.nextInt(40));
////////            v.setTargetSpeed(38 + rnd.nextInt(15));
////////            world.addVehicle(v);
////////        }
////////    }
////////
////////    // ---------- افزودن یک خودرو رندوم ----------
////////    public static Vehicle addRandomVehicle(World world, CityMap map) {
////////        ArrayList<Lane> lanes = new ArrayList<Lane>();
////////        List<Road> roads = map.getRoads();
////////        for (int i = 0; i < roads.size(); i++) {
////////            Road r = roads.get(i);
////////            lanes.addAll(r.getForwardLanes());
////////            lanes.addAll(r.getBackwardLanes());
////////        }
////////        if (lanes.isEmpty()) return null;
////////
////////        Lane lane = lanes.get(rnd.nextInt(lanes.size()));
////////        Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING);
////////        v.setCurrentLane(lane);
////////        v.setPositionInLane(rnd.nextInt(30));
////////        v.setTargetSpeed(36 + rnd.nextInt(18));
////////        world.addVehicle(v);
////////        return v;
////////    }
////////
////////    private static VehicleType randomType() {
////////        VehicleType[] vals = VehicleType.values();
////////        return vals[rnd.nextInt(vals.length)];
////////    }
////////
////////    // ---------- افزودن عابر پیاده + گذرگاه ----------
////////    public static void addPedestrians(World world, CityMap map) {
////////        List<Intersection> xs = map.getIntersections();
////////        if (xs.size() < 4) return;
////////
////////        // ۴ گذرگاه پیاده روی ۴ تقاطع مختلف
////////        for (int i = 0; i < 4; i++) {
////////            Intersection it = xs.get(rnd.nextInt(xs.size()));
////////            Direction d = Direction.values()[rnd.nextInt(Direction.values().length)];
////////            PedestrianCrossing crossing = new PedestrianCrossing("PC-" + it.getId() + "-" + d, it, d, true);
////////
////////            Pedestrian p = new Pedestrian("P-" + System.nanoTime(), new Point(it.getPosition().getX(), it.getPosition().getY()), crossing);
////////            world.addPedestrian(p);
////////        }
////////    }
////////
////////    // ---------- راه‌اندازی اولیه ----------
////////    public static void setup(World world, CityMap map, SimulationClock clock) {
////////        installLights(world, map, 35, 5, 30);      // چراغ‌ها
////////        seedVehicles(world, map, clock, 70);       // 🚗 تعداد اولیه ۷۰ ماشین
////////        addPedestrians(world, map);                // 🚶 اضافه کردن عابر و گذرگاه
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
////////
////////package simulation;
////////
////////import core.Direction;
////////import core.Vehicle;
////////import core.VehicleType;
////////import core.DriverProfile;
////////import core.Point;
////////import infrastructure.CityMap;
////////import infrastructure.Intersection;
////////import infrastructure.Road;
////////import infrastructure.Lane;
////////import pedestrian.Pedestrian;
////////import pedestrian.PedestrianCrossing;
////////import trafficcontrol.TrafficLight;
////////import trafficcontrol.TrafficControlDevice;
////////import trafficcontrol.LightState;
////////
////////import java.util.ArrayList;
////////import java.util.List;
////////import java.util.Random;
////////
////////public final class DemoTraffic {
////////    private DemoTraffic() {}
////////    private static final Random rnd = new Random();
////////
////////    // ---------- نصب چراغ واقعی در تقاطع‌ها ----------
////////    public static void installLights(World world, CityMap map, int green, int yellow, int red) {
////////        List<Intersection> xs = map.getIntersections();
////////        for (int i = 0; i < xs.size(); i++) {
////////            Intersection it = xs.get(i);
////////
////////            // چراغ شمال و جنوب
////////            TrafficLight north = new TrafficLight("TL-" + it.getId() + "-N", Direction.NORTH, green, yellow, red, SimulationConfig.TICK_INTERVAL);
////////            TrafficLight south = new TrafficLight("TL-" + it.getId() + "-S", Direction.SOUTH, green, yellow, red, SimulationConfig.TICK_INTERVAL);
////////            // چراغ شرق و غرب
////////            TrafficLight east  = new TrafficLight("TL-" + it.getId() + "-E", Direction.EAST,  green, yellow, red, SimulationConfig.TICK_INTERVAL);
////////            TrafficLight west  = new TrafficLight("TL-" + it.getId() + "-W", Direction.WEST,  green, yellow, red, SimulationConfig.TICK_INTERVAL);
////////
////////            // در شروع: شمال/جنوب سبز باشن، شرق/غرب قرمز
////////            while (north.getState() != LightState.GREEN) north.update();
////////            while (south.getState() != LightState.GREEN) south.update();
////////            while (east.getState() != LightState.RED) east.update();
////////            while (west.getState() != LightState.RED) west.update();
////////
////////            it.setControl(Direction.NORTH, north);
////////            it.setControl(Direction.SOUTH, south);
////////            it.setControl(Direction.EAST, east);
////////            it.setControl(Direction.WEST, west);
////////
////////            world.addTrafficLight(north);
////////            world.addTrafficLight(south);
////////            world.addTrafficLight(east);
////////            world.addTrafficLight(west);
////////        }
////////    }
////////
////////    // ---------- ریختن چند خودرو تستی ----------
////////    public static void seedVehicles(World world, CityMap map, SimulationClock clock, int count) {
////////        ArrayList<Lane> lanes = new ArrayList<Lane>();
////////        List<Road> roads = map.getRoads();
////////        for (int i = 0; i < roads.size(); i++) {
////////            Road r = roads.get(i);
////////            lanes.addAll(r.getForwardLanes());
////////            lanes.addAll(r.getBackwardLanes());
////////        }
////////        if (lanes.isEmpty()) return;
////////
////////        for (int n = 0; n < count; n++) {
////////            Lane lane = lanes.get(rnd.nextInt(lanes.size()));
////////            Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING);
////////            v.setCurrentLane(lane);
////////            v.setPositionInLane(rnd.nextInt(40));
////////            v.setTargetSpeed(38 + rnd.nextInt(15));
////////            world.addVehicle(v);
////////        }
////////    }
////////
////////    // ---------- افزودن یک خودرو رندوم ----------
////////    public static Vehicle addRandomVehicle(World world, CityMap map) {
////////        ArrayList<Lane> lanes = new ArrayList<Lane>();
////////        List<Road> roads = map.getRoads();
////////        for (int i = 0; i < roads.size(); i++) {
////////            Road r = roads.get(i);
////////            lanes.addAll(r.getForwardLanes());
////////            lanes.addAll(r.getBackwardLanes());
////////        }
////////        if (lanes.isEmpty()) return null;
////////
////////        Lane lane = lanes.get(rnd.nextInt(lanes.size()));
////////        Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING);
////////        v.setCurrentLane(lane);
////////        v.setPositionInLane(rnd.nextInt(30));
////////        v.setTargetSpeed(36 + rnd.nextInt(18));
////////        world.addVehicle(v);
////////        return v;
////////    }
////////
////////    private static VehicleType randomType() {
////////        VehicleType[] vals = VehicleType.values();
////////        return vals[rnd.nextInt(vals.length)];
////////    }
////////
////////    // ---------- افزودن عابر پیاده + گذرگاه ----------
////////    public static void addPedestrians(World world, CityMap map) {
////////        List<Intersection> xs = map.getIntersections();
////////        if (xs.size() < 4) return;
////////
////////        // ۴ گذرگاه پیاده روی ۴ تقاطع مختلف
////////        for (int i = 0; i < 4; i++) {
////////            Intersection it = xs.get(rnd.nextInt(xs.size()));
////////            Direction d = Direction.values()[rnd.nextInt(Direction.values().length)];
////////            PedestrianCrossing crossing = new PedestrianCrossing("PC-" + it.getId() + "-" + d, it, d, true);
////////
////////            Pedestrian p = new Pedestrian("P-" + System.nanoTime(), new Point(it.getPosition().getX(), it.getPosition().getY()), crossing);
////////            world.addPedestrian(p);
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
////////
////////package simulation; // // پکیج simulation
////////
////////import core.Direction;
////////import core.Vehicle;
////////import core.VehicleType;
////////import core.DriverProfile;
////////import infrastructure.CityMap;
////////import infrastructure.Intersection;
////////import infrastructure.Road;
////////import infrastructure.Lane;
////////import trafficcontrol.TrafficLight;
////////import trafficcontrol.TrafficControlDevice;
////////
////////import java.util.ArrayList;
////////import java.util.List;
////////import java.util.Random;
////////
////////public final class DemoTraffic { // // کلاس کمکی ترافیک دمو
////////    private DemoTraffic() {}
////////    private static final Random rnd = new Random();
////////
////////    // ---------- نصب چراغ روی همهٔ جهت‌های هر تقاطع ----------
////////    public static void installLights(World world, CityMap map, int green, int yellow, int red) {
////////        List<Intersection> xs = map.getIntersections();
////////        for (int i = 0; i < xs.size(); i++) {
////////            Intersection it = xs.get(i);
////////            attachIfMissing(world, it, Direction.NORTH, green, yellow, red);
////////            attachIfMissing(world, it, Direction.SOUTH, green, yellow, red);
////////            attachIfMissing(world, it, Direction.EAST,  green, yellow, red);
////////            attachIfMissing(world, it, Direction.WEST,  green, yellow, red);
////////        }
////////    }
////////
////////    private static void attachIfMissing(World world, Intersection it, Direction d, int g, int y, int r) {
////////        TrafficControlDevice dev = it.getControl(d);
////////        if (dev == null) {
////////            // ✅ اینجا اصلاح شد: به جای LightState.GREEN → SimulationConfig.TICK_INTERVAL
////////            TrafficLight tl = new TrafficLight("TL-" + it.getId() + "-" + d, d, g, y, r, SimulationConfig.TICK_INTERVAL);
////////            it.setControl(d, tl);
////////            world.addTrafficLight(tl);
////////        }
////////    }
////////
////////    // ---------- ریختن چند خودرو تستی ----------
////////    public static void seedVehicles(World world, CityMap map, SimulationClock clock, int count) {
////////        ArrayList<Lane> lanes = new ArrayList<Lane>();
////////        List<Road> roads = map.getRoads();
////////        for (int i = 0; i < roads.size(); i++) {
////////            Road r = roads.get(i);
////////            lanes.addAll(r.getForwardLanes());
////////            lanes.addAll(r.getBackwardLanes());
////////        }
////////        if (lanes.isEmpty()) return;
////////
////////        for (int n = 0; n < count; n++) {
////////            Lane lane = lanes.get(rnd.nextInt(lanes.size()));
////////            Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING);
////////            v.setCurrentLane(lane);
////////            v.setPositionInLane(rnd.nextInt(40));
////////            v.setTargetSpeed(38 + rnd.nextInt(15));
////////            world.addVehicle(v);
////////        }
////////    }
////////
////////    // ---------- افزودن یک خودرو رندوم ----------
////////    public static Vehicle addRandomVehicle(World world, CityMap map) {
////////        ArrayList<Lane> lanes = new ArrayList<Lane>();
////////        List<Road> roads = map.getRoads();
////////        for (int i = 0; i < roads.size(); i++) {
////////            Road r = roads.get(i);
////////            lanes.addAll(r.getForwardLanes());
////////            lanes.addAll(r.getBackwardLanes());
////////        }
////////        if (lanes.isEmpty()) return null;
////////
////////        Lane lane = lanes.get(rnd.nextInt(lanes.size()));
////////        Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING);
////////        v.setCurrentLane(lane);
////////        v.setPositionInLane(rnd.nextInt(30));
////////        v.setTargetSpeed(36 + rnd.nextInt(18));
////////        world.addVehicle(v);
////////        return v;
////////    }
////////
////////    private static VehicleType randomType() {
////////        VehicleType[] vals = VehicleType.values();
////////        return vals[rnd.nextInt(vals.length)];
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
//////////package simulation; // // پکیج simulation
//////////
//////////import core.Direction; // // جهت
//////////import core.Vehicle; // // خودرو
//////////import core.VehicleType; // // نوع خودرو
//////////import core.DriverProfile; // // پروفایل راننده
//////////import infrastructure.CityMap; // // نقشه
//////////import infrastructure.Intersection; // // تقاطع
//////////import infrastructure.Road; // // جاده
//////////import infrastructure.Lane; // // لِین
//////////import trafficcontrol.TrafficLight; // // چراغ
//////////import trafficcontrol.LightState; // // حالت چراغ
//////////import trafficcontrol.TrafficControlDevice; // // اینترفیس کنترل
//////////
//////////import java.util.ArrayList; // // لیست کمکی
//////////import java.util.List; // // اینترفیس لیست
//////////import java.util.Random; // // رندوم
//////////
//////////public final class DemoTraffic { // // کلاس کمکی ترافیک دمو
//////////    private DemoTraffic() {} // // جلوگیری از نمونه‌سازی
//////////    private static final Random rnd = new Random(); // // رندوم مشترک
//////////
//////////    // ---------- نصب چراغ روی همهٔ جهت‌های هر تقاطع (اگر کنترل ندارد) ----------
//////////    public static void installLights(World world, CityMap map, int green, int yellow, int red) { // // نصب چراغ‌ها
//////////        List<Intersection> xs = map.getIntersections(); // // همه تقاطع‌ها
//////////        for (int i = 0; i < xs.size(); i++) { // // حلقه روی تقاطع‌ها
//////////            Intersection it = xs.get(i); // // تقاطع
//////////            attachIfMissing(world, it, Direction.NORTH, green, yellow, red); // // شمال
//////////            attachIfMissing(world, it, Direction.SOUTH, green, yellow, red); // // جنوب
//////////            attachIfMissing(world, it, Direction.EAST,  green, yellow, red); // // شرق
//////////            attachIfMissing(world, it, Direction.WEST,  green, yellow, red); // // غرب
//////////        }
//////////    }
//////////
//////////    private static void attachIfMissing(World world, Intersection it, Direction d, int g, int y, int r) { // // وصل کردن چراغ
//////////        TrafficControlDevice dev = it.getControl(d); // // کنترل فعلی
//////////        if (dev == null) { // // اگر چیزی وصل نیست
//////////            TrafficLight tl = new TrafficLight("TL-" + it.getId() + "-" + d, d, g, y, r, LightState.GREEN); // // ساخت چراغ
//////////            it.setControl(d, tl); // // وصل به تقاطع (نیاز به setControl داری که داری)
//////////            world.addTrafficLight(tl); // // ثبت در World برای آپدیت دوره‌ای
//////////        }
//////////    }
//////////
//////////    // ---------- ریختن چند خودرو تستی روی لِین‌های تصادفی ----------
//////////    public static void seedVehicles(World world, CityMap map, SimulationClock clock, int count) { // // افزودن خودرو
//////////        ArrayList<Lane> lanes = new ArrayList<Lane>(); // // لیست همه لِین‌ها
//////////        List<Road> roads = map.getRoads(); // // همه جاده‌ها
//////////        for (int i = 0; i < roads.size(); i++) { // // حلقه روی جاده‌ها
//////////            Road r = roads.get(i); // // جاده
//////////            lanes.addAll(r.getForwardLanes()); // // لِین‌های رفت
//////////            lanes.addAll(r.getBackwardLanes()); // // لِین‌های برگشت
//////////        }
//////////        if (lanes.isEmpty()) return; // // اگر هیچ لِینی نداریم خروج
//////////
//////////        for (int n = 0; n < count; n++) { // // به تعداد خواسته
//////////            Lane lane = lanes.get(rnd.nextInt(lanes.size())); // // یک لِین رندوم
//////////            Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING); // // خودرو
//////////            v.setCurrentLane(lane); // // قرار دادن روی لِین
//////////            v.setPositionInLane(rnd.nextInt(40)); // // کمی جلوتر از ابتدا
//////////            v.setTargetSpeed(38 + rnd.nextInt(15)); // // سرعت هدف اولیه
//////////            world.addVehicle(v); // // افزودن به دنیا
//////////        }
//////////    }
//////////
//////////    // ---------- یوتیلیتی: افزودن یک خودرو کاملاً تصادفی ----------
//////////    public static Vehicle addRandomVehicle(World world, CityMap map) { // // افزودن تک خودرو
//////////        ArrayList<Lane> lanes = new ArrayList<Lane>(); // // جمع‌کردن لِین‌ها
//////////        List<Road> roads = map.getRoads(); // // جاده‌ها
//////////        for (int i = 0; i < roads.size(); i++) { // // حلقه
//////////            Road r = roads.get(i); // // جاده
//////////            lanes.addAll(r.getForwardLanes()); // // رفت
//////////            lanes.addAll(r.getBackwardLanes()); // // برگشت
//////////        }
//////////        if (lanes.isEmpty()) return null; // // بدون لِین
//////////
//////////        Lane lane = lanes.get(rnd.nextInt(lanes.size())); // // انتخاب لِین
//////////        Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING); // // خودرو
//////////        v.setCurrentLane(lane); // // ست لِین
//////////        v.setPositionInLane(rnd.nextInt(30)); // // موقعیت اولیه
//////////        v.setTargetSpeed(36 + rnd.nextInt(18)); // // هدف سرعت
//////////        world.addVehicle(v); // // افزودن
//////////        return v; // // بازگشت
//////////    }
//////////
//////////    private static core.VehicleType randomType() { // // انتخاب نوع خودرو تصادفی
//////////        core.VehicleType[] vals = core.VehicleType.values(); // // آرایه انواع
//////////        return vals[rnd.nextInt(vals.length)]; // // یکی تصادفی
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
////////////package simulation; // // پکیج simulation
////////////
////////////import core.Vehicle; // // کلاس خودرو
////////////import core.VehicleType; // // نوع خودرو
////////////import core.DriverProfile; // // پروفایل راننده
////////////import core.Direction; // // جهت‌ها
////////////import infrastructure.CityMap; // // نقشه
////////////import infrastructure.Intersection; // // تقاطع
////////////import infrastructure.Road; // // جاده
////////////import infrastructure.Lane; // // لِین
////////////import trafficcontrol.TrafficLight; // // چراغ
////////////import trafficcontrol.LightState; // // حالت چراغ
////////////import trafficcontrol.TrafficControlDevice; // // اینترفیس کنترل
////////////
////////////import java.util.ArrayList; // // لیست قابل تغییر
////////////import java.util.HashMap;  // // مپ
////////////import java.util.List;     // // اینترفیس لیست
////////////import java.util.Map;      // // اینترفیس مپ
////////////import java.util.Random;  // // رندوم
////////////
////////////public final class DemoTraffic { // // کلاس کمکی برای دمو
////////////    private DemoTraffic() {} // // جلوگیری از نمونه‌سازی
////////////
////////////    private static final Random rnd = new Random(); // // رندوم مشترک
////////////
////////////    // --------------------------------------------------------------------
////////////    // ۱) نصب چراغ‌ها: سه‌راهی‌ها و فلکه‌ها (id شروع‌شون با "RND-") چراغ نگیرند
////////////    // --------------------------------------------------------------------
////////////    public static void installLights(World world, CityMap map, int green, int yellow, int red) { // // نصب چراغ
////////////        if (map == null) return; // // نال‌چک
////////////
////////////        Map<String, Integer> degree = new HashMap<String, Integer>(); // // درجه هر تقاطع
////////////        List<Intersection> xs = map.getIntersections(); // // همه تقاطع‌ها
////////////        for (int i = 0; i < xs.size(); i++) { degree.put(xs.get(i).getId(), 0); } // // مقداردهی اولیه
////////////
////////////        List<Road> rs = map.getRoads(); // // همه جاده‌ها
////////////        for (int i = 0; i < rs.size(); i++) { // // حلقه جاده‌ها
////////////            Road r = rs.get(i); // // جاده
////////////            String a = r.getStart().getId(); // // سر جاده
////////////            String b = r.getEnd().getId();   // // ته جاده
////////////            degree.put(a, degree.get(a) + 1); // // افزایش درجه
////////////            degree.put(b, degree.get(b) + 1); // // افزایش درجه
////////////        }
////////////
////////////        for (int i = 0; i < xs.size(); i++) { // // حلقه تقاطع‌ها
////////////            Intersection it = xs.get(i); // // تقاطع
////////////            Integer deg = degree.get(it.getId()); if (deg == null) deg = 0; // // درجه
////////////            if (deg.intValue() == 3) continue;            // // سه‌راه: چراغ نصب نشود
////////////            if (it.getId().startsWith("RND-")) continue;  // // فلکه: چراغ نصب نشود
////////////
////////////            attachIfMissing(world, it, Direction.NORTH, green, yellow, red); // // شمال
////////////            attachIfMissing(world, it, Direction.SOUTH, green, yellow, red); // // جنوب
////////////            attachIfMissing(world, it, Direction.EAST,  green, yellow, red); // // شرق
////////////            attachIfMissing(world, it, Direction.WEST,  green, yellow, red); // // غرب
////////////        }
////////////    }
////////////
////////////    private static void attachIfMissing(World world, Intersection it, Direction d, int g, int y, int r) { // // وصل چراغ
////////////        TrafficControlDevice dev = it.getControl(d); // // کنترل فعلی
////////////        if (dev == null) { // // اگر چیزی وصل نیست
////////////            TrafficLight tl = new TrafficLight("TL-" + it.getId() + "-" + d, d, g, y, r, 0); // // چراغ با فاز تصادفی داخلی
////////////            it.setControl(d, tl); // // اتصال به تقاطع
////////////            world.addTrafficLight(tl); // // ثبت در دنیا
////////////        }
////////////    }
////////////
////////////    // --------------------------------------------------------------------
////////////    // ۲) ریختن چند خودرو تستی (اختیاری برای دمو)
////////////    // --------------------------------------------------------------------
////////////    public static void seedVehicles(World world, CityMap map, SimulationClock clock, int count) { // // افزودن تعدادی خودرو
////////////        ArrayList<Lane> lanes = collectAllLanes(map); // // جمع آوری همه لِین‌ها
////////////        if (lanes.isEmpty()) return; // // اگر لِینی نداریم خروج
////////////
////////////        for (int n = 0; n < count; n++) { // // تکرار به تعداد خواسته
////////////            Lane lane = lanes.get(rnd.nextInt(lanes.size())); // // انتخاب لِین تصادفی
////////////            Vehicle v = makeRandomVehicle(); // // ساخت خودرو با ویژگی‌های رندوم
////////////            v.setCurrentLane(lane); // // قرار دادن روی لِین
////////////            v.setPositionInLane(rnd.nextInt(40)); // // قدری جلوتر از ابتدا
////////////            v.setTargetSpeed(36 + rnd.nextInt(18)); // // هدف سرعت اولیه
////////////            world.addVehicle(v); // // افزودن به دنیا
////////////        }
////////////    }
////////////
////////////    // --------------------------------------------------------------------
////////////    // ۳) افزودن ۱ خودرو تصادفی (متدی که UIController صدا می‌زند)
////////////    // --------------------------------------------------------------------
////////////    public static Vehicle addRandomVehicle(World world, CityMap map) { // // افزودن تک‌خودرو
////////////        ArrayList<Lane> lanes = collectAllLanes(map); // // گرفتن همه لِین‌ها
////////////        if (lanes.isEmpty()) return null; // // بدون لِین
////////////        Lane lane = lanes.get(rnd.nextInt(lanes.size())); // // انتخاب لِین تصادفی
////////////
////////////        Vehicle v = makeRandomVehicle(); // // ساخت خودرو
////////////        v.setCurrentLane(lane); // // قرار دادن روی لِین
////////////        v.setPositionInLane(rnd.nextInt(30)); // // موقعیت اولیه
////////////        v.setTargetSpeed(34 + rnd.nextInt(22)); // // سرعت هدف (px/s)
////////////        world.addVehicle(v); // // افزودن به دنیا
////////////        return v; // // برگرداندن برای UI
////////////    }
////////////
////////////    // --------------------------------------------------------------------
////////////    // ابزارهای داخلی
////////////    // --------------------------------------------------------------------
////////////    private static ArrayList<Lane> collectAllLanes(CityMap map) { // // جمع کردن همه لِین‌ها
////////////        ArrayList<Lane> lanes = new ArrayList<Lane>(); // // لیست خروجی
////////////        if (map == null) return lanes; // // نال‌چک
////////////        List<Road> roads = map.getRoads(); // // همه جاده‌ها
////////////        for (int i = 0; i < roads.size(); i++) { // // حلقه
////////////            Road r = roads.get(i); // // جاده
////////////            lanes.addAll(r.getForwardLanes());   // // لِین‌های رفت
////////////            lanes.addAll(r.getBackwardLanes());  // // لِین‌های برگشت
////////////        }
////////////        return lanes; // // خروجی
////////////    }
////////////
////////////    private static Vehicle makeRandomVehicle() { // // ساخت خودرو رندوم
////////////        String id = "V-" + System.nanoTime(); // // id یکتا
////////////        VehicleType type = randomType(); // // نوع تصادفی
////////////        double vmax = 60 + rnd.nextInt(50); // // سقف سرعت px/s
////////////        Vehicle v = new Vehicle(id, type, vmax, DriverProfile.LAW_ABIDING); // // ساخت
////////////        v.setAcceleration(60.0); // // شتاب ملایم
////////////        v.setDeceleration(120.0); // // ترمز قوی‌تر
////////////        return v; // // خروجی
////////////    }
////////////
////////////    private static VehicleType randomType() { // // انتخاب نوع خودرو
////////////        VehicleType[] vals = VehicleType.values(); // // آرایه انواع
////////////        return vals[rnd.nextInt(vals.length)];     // // انتخاب تصادفی
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
////////////
////////////
////////////
////////////
////////////
////////////
//////////////package simulation; // // پکیج simulation
//////////////
//////////////import core.Direction; // // جهت
//////////////import core.Vehicle; // // خودرو
//////////////import core.VehicleType; // // نوع خودرو
//////////////import core.DriverProfile; // // پروفایل راننده
//////////////import core.Route; // // مسیر
//////////////import infrastructure.CityMap; // // نقشه
//////////////import infrastructure.Intersection; // // تقاطع
//////////////import infrastructure.Road; // // جاده
//////////////import infrastructure.Lane; // // لِین
//////////////import trafficcontrol.*; // // چراغ‌ها
//////////////
//////////////import java.util.ArrayList; // // لیست
//////////////import java.util.List; // // اینترفیس
//////////////import java.util.Random; // // رندوم
//////////////
//////////////public final class DemoTraffic { // // کلاس دمو
//////////////    private DemoTraffic() {} // // جلوگیری از نمونه‌سازی
//////////////    private static final Random rnd = new Random(); // // رندوم
//////////////
//////////////    // ---------- نصب چراغ ----------
//////////////    public static void installLights(World world, CityMap map, int green, int yellow, int red) { // // نصب چراغ‌ها
//////////////        List<Intersection> xs = map.getIntersections(); // // تقاطع‌ها
//////////////        for (int i = 0; i < xs.size(); i++) { // // حلقه
//////////////            Intersection it = xs.get(i); // // تقاطع
//////////////            attachIfMissing(world, it, Direction.NORTH, green, yellow, red); // // شمال
//////////////            attachIfMissing(world, it, Direction.SOUTH, green, yellow, red); // // جنوب
//////////////            attachIfMissing(world, it, Direction.EAST,  green, yellow, red); // // شرق
//////////////            attachIfMissing(world, it, Direction.WEST,  green, yellow, red); // // غرب
//////////////        }
//////////////    }
//////////////
//////////////    private static void attachIfMissing(World world, Intersection it, Direction d, int g, int y, int r) { // // وصل کردن چراغ
//////////////        TrafficControlDevice dev = it.getControl(d); // // کنترل فعلی
//////////////        if (dev == null) { // // اگر خالی
//////////////            TrafficLight tl = new TrafficLight("TL-" + it.getId() + "-" + d, d, g, y, r, LightState.GREEN.ordinal()); // // ساخت چراغ
//////////////            it.setControl(d, tl); // // نصب روی تقاطع
//////////////            world.addTrafficLight(tl); // // ثبت برای آپدیت
//////////////        }
//////////////    }
//////////////
//////////////    // ---------- خودروهای تستی با مسیر ----------
//////////////    public static void seedVehicles(World world, CityMap map, SimulationClock clock, int count) { // // افزودن چند خودرو
//////////////        ArrayList<Lane> lanes = collectAllLanes(map); // // همه لِین‌ها
//////////////        List<Intersection> xs = map.getIntersections(); // // همه تقاطع‌ها
//////////////        if (lanes.isEmpty() || xs.isEmpty()) return; // // اگر تهی
//////////////
//////////////        for (int n = 0; n < count; n++) { // // تکرار
//////////////            Lane lane = lanes.get(rnd.nextInt(lanes.size())); // // لِین تصادفی
//////////////            double vmax = 80 + rnd.nextInt(50); // // Vmax ~ 80..129 px/s
//////////////            Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), vmax, DriverProfile.LAW_ABIDING); // // ساخت خودرو
//////////////            v.setCurrentLane(lane); // // قرار دادن روی لِین
//////////////            v.setPositionInLane(rnd.nextInt(40)); // // کمی جلوتر از ابتدا
//////////////            double target = 50 + rnd.nextInt(40); // // هدف سرعت 50..89
//////////////            v.setTargetSpeed(target); // // ست سرعت هدف
//////////////
//////////////            // انتخاب مقصد و محاسبهٔ مسیر
//////////////            Intersection goal = xs.get(rnd.nextInt(xs.size())); // // مقصد تصادفی
//////////////            Route rt = PathFinder.shortestRoute(map, lane, goal); // // مسیر کوتاه
//////////////            v.setRoute(rt); // // ثبت مسیر
//////////////            v.setDestination(goal); // // ثبت مقصد
//////////////
//////////////            world.addVehicle(v); // // افزودن به دنیا
//////////////        }
//////////////    }
//////////////
//////////////    // ---------- افزودن تک خودرو با مسیر ----------
//////////////    public static Vehicle addRandomVehicle(World world, CityMap map) { // // افزودن یک خودرو
//////////////        ArrayList<Lane> lanes = collectAllLanes(map); // // همه لِین‌ها
//////////////        List<Intersection> xs = map.getIntersections(); // // تقاطع‌ها
//////////////        if (lanes.isEmpty() || xs.isEmpty()) return null; // // اگر تهی
//////////////
//////////////        Lane lane = lanes.get(rnd.nextInt(lanes.size())); // // لِین تصادفی
//////////////        double vmax = 80 + rnd.nextInt(50); // // Vmax
//////////////        Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), vmax, DriverProfile.LAW_ABIDING); // // خودرو
//////////////        v.setCurrentLane(lane); // // لِین
//////////////        v.setPositionInLane(rnd.nextInt(30)); // // مکان
//////////////        v.setTargetSpeed(50 + rnd.nextInt(40)); // // هدف سرعت
//////////////
//////////////        Intersection goal = xs.get(rnd.nextInt(xs.size())); // // مقصد
//////////////        Route rt = PathFinder.shortestRoute(map, lane, goal); // // مسیر
//////////////        v.setRoute(rt); // // ثبت
//////////////        v.setDestination(goal); // // مقصد
//////////////
//////////////        world.addVehicle(v); // // افزودن
//////////////        return v; // // خروجی
//////////////    }
//////////////
//////////////    // ---------- کمک‌ها ----------
//////////////    private static ArrayList<Lane> collectAllLanes(CityMap map) { // // جمع‌آوری همهٔ لِین‌ها
//////////////        ArrayList<Lane> lanes = new ArrayList<Lane>(); // // خروجی
//////////////        List<Road> roads = map.getRoads(); // // جاده‌ها
//////////////        for (int i = 0; i < roads.size(); i++) { // // حلقه
//////////////            Road r = roads.get(i); // // جاده
//////////////            lanes.addAll(r.getForwardLanes()); // // لِین‌های forward
//////////////            lanes.addAll(r.getBackwardLanes()); // // لِین‌های backward
//////////////        }
//////////////        return lanes; // // خروجی
//////////////    }
//////////////
//////////////    private static VehicleType randomType() { // // نوع خودرو تصادفی
//////////////        VehicleType[] vals = VehicleType.values(); // // همه انواع
//////////////        return vals[rnd.nextInt(vals.length)]; // // یکی تصادفی
//////////////    }
//////////////}
//////////////
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
//////////////package simulation; // // پکیج simulation
//////////////
//////////////import core.Direction; // // جهت
//////////////import core.Vehicle; // // خودرو
//////////////import core.VehicleType; // // نوع خودرو
//////////////import core.DriverProfile; // // پروفایل راننده
//////////////import infrastructure.CityMap; // // نقشه
//////////////import infrastructure.Intersection; // // تقاطع
//////////////import infrastructure.Road; // // جاده
//////////////import infrastructure.Lane; // // لِین
//////////////import trafficcontrol.TrafficLight; // // چراغ
//////////////import trafficcontrol.LightState; // // حالت چراغ
//////////////import trafficcontrol.TrafficControlDevice; // // اینترفیس کنترل
//////////////
//////////////import java.util.ArrayList; // // لیست کمکی
//////////////import java.util.List; // // اینترفیس لیست
//////////////import java.util.Random; // // رندوم
//////////////
//////////////public final class DemoTraffic { // // کلاس کمکی ترافیک دمو
//////////////    private DemoTraffic() {} // // جلوگیری از نمونه‌سازی
//////////////    private static final Random rnd = new Random(); // // رندوم مشترک
//////////////
//////////////    // ---------- نصب چراغ روی همهٔ جهت‌های هر تقاطع (اگر کنترل ندارد) ----------
//////////////    public static void installLights(World world, CityMap map, int green, int yellow, int red) { // // نصب چراغ‌ها
//////////////        List<Intersection> xs = map.getIntersections(); // // همه تقاطع‌ها
//////////////        for (int i = 0; i < xs.size(); i++) { // // حلقه روی تقاطع‌ها
//////////////            Intersection it = xs.get(i); // // تقاطع
//////////////            attachIfMissing(world, it, Direction.NORTH, green, yellow, red); // // شمال
//////////////            attachIfMissing(world, it, Direction.SOUTH, green, yellow, red); // // جنوب
//////////////            attachIfMissing(world, it, Direction.EAST,  green, yellow, red); // // شرق
//////////////            attachIfMissing(world, it, Direction.WEST,  green, yellow, red); // // غرب
//////////////        }
//////////////    }
//////////////
//////////////    private static void attachIfMissing(World world, Intersection it, Direction d, int g, int y, int r) { // // وصل کردن چراغ
//////////////        TrafficControlDevice dev = it.getControl(d); // // کنترل فعلی
//////////////        if (dev == null) { // // اگر چیزی وصل نیست
//////////////            TrafficLight tl = new TrafficLight("TL-" + it.getId() + "-" + d, d, g, y, r, LightState.GREEN.ordinal()); // // ساخت چراغ
//////////////            it.setControl(d, tl); // // وصل به تقاطع (نیاز به setControl داری که داری)
//////////////            world.addTrafficLight(tl); // // ثبت در World برای آپدیت دوره‌ای
//////////////        }
//////////////    }
//////////////
//////////////    // ---------- ریختن چند خودرو تستی روی لِین‌های تصادفی ----------
//////////////    public static void seedVehicles(World world, CityMap map, SimulationClock clock, int count) { // // افزودن خودرو
//////////////        ArrayList<Lane> lanes = new ArrayList<Lane>(); // // لیست همه لِین‌ها
//////////////        List<Road> roads = map.getRoads(); // // همه جاده‌ها
//////////////        for (int i = 0; i < roads.size(); i++) { // // حلقه روی جاده‌ها
//////////////            Road r = roads.get(i); // // جاده
//////////////            lanes.addAll(r.getForwardLanes()); // // لِین‌های رفت
//////////////            lanes.addAll(r.getBackwardLanes()); // // لِین‌های برگشت
//////////////        }
//////////////        if (lanes.isEmpty()) return; // // اگر هیچ لِینی نداریم خروج
//////////////
//////////////        for (int n = 0; n < count; n++) { // // به تعداد خواسته
//////////////            Lane lane = lanes.get(rnd.nextInt(lanes.size())); // // یک لِین رندوم
//////////////            Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING); // // خودرو
//////////////            v.setCurrentLane(lane); // // قرار دادن روی لِین
//////////////            v.setPositionInLane(rnd.nextInt(40)); // // کمی جلوتر از ابتدا
//////////////            v.setTargetSpeed(38 + rnd.nextInt(15)); // // سرعت هدف اولیه
//////////////            world.addVehicle(v); // // افزودن به دنیا
//////////////        }
//////////////    }
//////////////
//////////////    // ---------- یوتیلیتی: افزودن یک خودرو کاملاً تصادفی ----------
//////////////    public static Vehicle addRandomVehicle(World world, CityMap map) { // // افزودن تک خودرو
//////////////        ArrayList<Lane> lanes = new ArrayList<Lane>(); // // جمع‌کردن لِین‌ها
//////////////        List<Road> roads = map.getRoads(); // // جاده‌ها
//////////////        for (int i = 0; i < roads.size(); i++) { // // حلقه
//////////////            Road r = roads.get(i); // // جاده
//////////////            lanes.addAll(r.getForwardLanes()); // // رفت
//////////////            lanes.addAll(r.getBackwardLanes()); // // برگشت
//////////////        }
//////////////        if (lanes.isEmpty()) return null; // // بدون لِین
//////////////
//////////////        Lane lane = lanes.get(rnd.nextInt(lanes.size())); // // انتخاب لِین
//////////////        Vehicle v = new Vehicle("V-" + System.nanoTime(), randomType(), 60 + rnd.nextInt(30), DriverProfile.LAW_ABIDING); // // خودرو
//////////////        v.setCurrentLane(lane); // // ست لِین
//////////////        v.setPositionInLane(rnd.nextInt(30)); // // موقعیت اولیه
//////////////        v.setTargetSpeed(36 + rnd.nextInt(18)); // // هدف سرعت
//////////////        world.addVehicle(v); // // افزودن
//////////////        return v; // // بازگشت
//////////////    }
//////////////
//////////////    private static core.VehicleType randomType() { // // انتخاب نوع خودرو تصادفی
//////////////        core.VehicleType[] vals = core.VehicleType.values(); // // آرایه انواع
//////////////        return vals[rnd.nextInt(vals.length)]; // // یکی تصادفی
//////////////    }
//////////////}
