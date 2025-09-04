package simulation; // // پکیج شبیه‌سازی

import infrastructure.CityMap;            // // نقشهٔ شهر
import infrastructure.Intersection;       // // تقاطع
import infrastructure.Road;               // // جاده
import infrastructure.Lane;               // // لِین
import core.Direction;                    // // جهات
import trafficcontrol.TrafficControlDevice; // // دستگاه کنترلی
import trafficcontrol.TrafficLight;       // // چراغ راهنما
import trafficcontrol.LightState;         // // وضعیت چراغ
import core.VehicleType;                  // // نوع وسیله

import java.util.ArrayList;               // // لیست
import java.util.List;                    // // لیست
import java.util.Random;                  // // رندوم
import java.lang.reflect.*;               // // Reflection برای سازگاری امضاءها

/**
 * ابزار سناریو: نصب چراغ‌ها و افزودن خودروی تصادفی. //
 * 👇 افزودنی جدید طبق خواسته: seedVehicles(...) بدون حذف هیچ‌چیز //
 */
public final class DemoTraffic { // // کلاس نهایی ابزار
    private static final Random RNG = new Random();     // // رندوم مشترک
    private DemoTraffic() {}                             // // جلوگیری از نمونه‌سازی

    // ------------------------- نصب چراغ‌ها (بدون تغییر) ------------------------- //
    public static void installLights(final World world, final CityMap map,
                                     final int greenMs, final int yellowMs, final int redMs) { // // نصب چراغ‌ها
        int i; for (i = 0; i < map.getIntersections().size(); i++) { // // پیمایش تقاطع‌ها
            final Intersection it = map.getIntersections().get(i);            // // تقاطع
            attachIfMissing(world, it, Direction.NORTH, greenMs, yellowMs, redMs); // // رویکرد شمال
            attachIfMissing(world, it, Direction.EAST,  greenMs, yellowMs, redMs); // // رویکرد شرق
            attachIfMissing(world, it, Direction.SOUTH, greenMs, yellowMs, redMs); // // رویکرد جنوب
            attachIfMissing(world, it, Direction.WEST,  greenMs, yellowMs, redMs); // // رویکرد غرب
        }
    }

    private static void attachIfMissing(final World world, final Intersection it, final Direction d,
                                        final int g, final int y, final int r) { // // اگر نبود چراغ نصب کن
        final trafficcontrol.TrafficControlDevice dev = it.getControl(d);     // // کنترل فعلی
        if (dev == null) {                                                    // // اگر چیزی نصب نیست
            final TrafficLight tl = new TrafficLight(                         // // ساخت چراغ
                    "TL-" + it.getId() + "-" + d,                             // // شناسهٔ یکتا
                    d, g, y, r,                                               // // زمان‌های G/Y/R
                    LightState.GREEN                                          // // وضعیت اولیه
            );
            it.setControl(d, tl);                                             // // قرار دادن روی تقاطع
            tryRegisterTrafficLightInWorld(world, tl);                        // // ثبت در دنیا (Reflection)
        }
    }

    private static void tryRegisterTrafficLightInWorld(final World world, final TrafficLight tl) { // // ثبت چراغ در دنیا
        try {
            Method m = world.getClass().getMethod("addTrafficLight", TrafficLight.class); // // امضای رایج
            m.invoke(world, tl);                                                          // // صدا
            return;                                                                       // // موفق
        } catch (Throwable ignored) {}                                                    // // بی‌اهمیت

        try {
            Method m = world.getClass().getMethod("registerDevice", trafficcontrol.TrafficControlDevice.class); // // امضای جایگزین
            m.invoke(world, tl);                                                                                 // // صدا
            return;                                                                                              // // موفق
        } catch (Throwable ignored) {}                                                                           // // بی‌اهمیت
        // اگر نبود، همان setControl روی Intersection کافی است //
    }

    // ---------------------- افزودن تصادفی خودرو (بدون تغییر) ---------------------- //
    public static void addRandomVehicle(final World world, final CityMap map) { // // افزودن یک خودرو تصادفی
        final Lane spawnLane = pickRandomLane(map);                              // // انتخاب لِین تصادفی
        if (spawnLane == null) return;                                           // // اگر لِینی نبود، هیچ

        final VehicleType type = randomVehicleType();                            // // نوع وسیله
        final double speed = randomSpeedForType(type);                           // // سرعت رندوم در بازهٔ مجاز
        final double pos   = 0.0;                                               // // شروع از ابتدای لِین
        final String id = "VH-" + System.currentTimeMillis() + "-" + Math.abs(RNG.nextInt()); // // شناسهٔ یکتا

        final Object vehicle = reflectMakeVehicle(id, type, spawnLane, pos, speed); // // ساخت Vehicle با امضاهای مختلف
        if (vehicle == null) return;                                                 // // اگر نشد، خروج

        reflectRegisterVehicleInWorld(world, vehicle);                                // // ثبت در دنیا با Reflection
    }

    private static Lane pickRandomLane(final CityMap map) {                 // // انتخاب تصادفی لِین
        final List<Lane> lanes = new ArrayList<Lane>();                     // // تجمیع لِین‌ها
        int i; for (i = 0; i < map.getRoads().size(); i++) {                // // پیمایش راه‌ها
            final Road r = map.getRoads().get(i);                           // // راه
            lanes.addAll(r.getForwardLanes());                              // // لِین‌های رفت
            lanes.addAll(r.getBackwardLanes());                             // // لِین‌های برگشت
        }
        if (lanes.isEmpty()) return null;                                   // // اگر خالیست
        return lanes.get(RNG.nextInt(lanes.size()));                        // // یکی تصادفی
    }

    private static VehicleType randomVehicleType() {                        // // انتخاب نوع وسیله
        final VehicleType[] all = VehicleType.values();                     // // همهٔ انواع
        return all[RNG.nextInt(all.length)];                                // // تصادفی
    }

    private static double randomSpeedForType(final VehicleType t) {         // // سرعت رندوم برای نوع
        final String name = t.name().toUpperCase();                         // // نام نوع
        double min, max;                                                    // // کران‌ها
        if (name.contains("CAR") || name.contains("SEDAN") || name.contains("AUTO")) { min = 30; max = 90; } // // سواری
        else if (name.contains("BUS") || name.contains("COACH")) { min = 20; max = 70; }                      // // اتوبوس
        else if (name.contains("TRUCK") || name.contains("LORRY") || name.contains("HGV")) { min = 20; max = 60; } // // کامیون
        else if (name.contains("MOTOR") || name.contains("BIKE") || name.contains("SCOOT")) { min = 30; max = 80; } // // موتور/اسکوتر
        else { min = 25; max = 60; }                                        // // پیش‌فرض
        return min + RNG.nextDouble() * (max - min);                        // // خروجی یکنواخت
    }

    private static Object reflectMakeVehicle(final String id, final VehicleType type,
                                             final Lane lane, final double pos, final double speed) { // // ساخت Vehicle با امضاهای مختلف
        try {
            Class<?> vehicleCls = Class.forName("core.Vehicle");                                  // // کلاس Vehicle

            Constructor<?> c1 = safeCtor(vehicleCls,
                    new Class[]{String.class, VehicleType.class, Lane.class, double.class, double.class}); // // امضاء ۱
            if (c1 != null) {
                Object v = c1.newInstance(new Object[]{id, type, lane, Double.valueOf(pos), Double.valueOf(speed)}); // // ساخت
                return v;                                                                                             // // موفق
            }

            Constructor<?> c2 = safeCtor(vehicleCls, new Class[]{String.class, VehicleType.class, Lane.class}); // // امضاء ۲
            if (c2 != null) {
                Object v = c2.newInstance(new Object[]{id, type, lane}); // // ساخت
                trySetDouble(v, "setPositionInLane", pos);               // // ست موقعیت
                trySetDouble(v, "setSpeed", speed);                       // // ست سرعت
                trySetDouble(v, "setCurrentSpeed", speed);                // // ست سرعت جایگزین
                return v;                                                 // // موفق
            }

            Constructor<?> c3 = safeCtor(vehicleCls, new Class[]{String.class, VehicleType.class}); // // امضاء ۳
            if (c3 != null) {
                Object v = c3.newInstance(new Object[]{id, type});       // // ساخت
                trySetLane(v, lane);                                      // // ست لِین
                trySetDouble(v, "setPositionInLane", pos);                // // ست موضع
                trySetDouble(v, "setSpeed", speed);                       // // ست سرعت
                trySetDouble(v, "setCurrentSpeed", speed);                // // ست سرعت جایگزین
                return v;                                                 // // موفق
            }

            Constructor<?> c4 = safeCtor(vehicleCls, new Class[]{String.class}); // // امضاء ۴
            if (c4 != null) {
                Object v = c4.newInstance(new Object[]{id});             // // ساخت
                trySetEnum(v, "setType", VehicleType.class, type);       // // ست نوع
                trySetLane(v, lane);                                      // // ست لِین
                trySetDouble(v, "setPositionInLane", pos);                // // ست موضع
                trySetDouble(v, "setSpeed", speed);                       // // ست سرعت
                trySetDouble(v, "setCurrentSpeed", speed);                // // ست سرعت جایگزین
                return v;                                                 // // موفق
            }

        } catch (Throwable ignored) { /* اگر Vehicle نبود یا reflection خطا داد، ادامه */ } // // بی‌اهمیت
        return null; // // نتوانست بسازد
    }

    private static void reflectRegisterVehicleInWorld(final World world, final Object vehicle) { // // ثبت خودرو در دنیا
        try {
            Method m = world.getClass().getMethod("addVehicle", Class.forName("core.Vehicle")); // // امضاء ۱
            m.invoke(world, vehicle);                                                           // // ثبت
            return;                                                                             // // موفق
        } catch (Throwable ignored) {}

        try {
            Method m = world.getClass().getMethod("registerVehicle", Class.forName("core.Vehicle")); // // امضاء ۲
            m.invoke(world, vehicle);                                                                // // ثبت
            return;                                                                                  // // موفق
        } catch (Throwable ignored) {}

        try {
            Method m = world.getClass().getMethod("addEntity", Object.class); // // امضاء ۳
            m.invoke(world, vehicle);                                         // // ثبت
        } catch (Throwable ignored) {}
    }

    private static Constructor<?> safeCtor(final Class<?> cls, final Class<?>[] sig) { // // گرفتن سازنده امن
        try { return cls.getConstructor(sig); } catch (Throwable t) { return null; }    // // اگر نبود، null
    }

    private static void trySetDouble(final Object target, final String setter, final double value) { // // صدا زدن setter(double)
        try {
            Method m = target.getClass().getMethod(setter, double.class);       // // متد
            m.invoke(target, new Object[]{ Double.valueOf(value) });            // // صدا
        } catch (Throwable ignored) {}
    }

    private static void trySetEnum(final Object target, final String setter,
                                   final Class<?> enumCls, final Object enumValue) { // // صدا زدن setter(enum)
        try {
            Method m = target.getClass().getMethod(setter, enumCls); // // متد
            m.invoke(target, enumValue);                              // // صدا
        } catch (Throwable ignored) {}
    }

    private static void trySetLane(final Object target, final Lane lane) { // // تلاش برای setLane یا setCurrentLane
        try {
            Method m = target.getClass().getMethod("setLane", Lane.class); // // setLane
            m.invoke(target, lane);
            return;
        } catch (Throwable ignored) {}

        try {
            Method m = target.getClass().getMethod("setCurrentLane", Lane.class); // // setCurrentLane
            m.invoke(target, lane);
        } catch (Throwable ignored) {}
    }

    // ======================== ⭐️ ویژگی افزوده‌شده (فقط اضافه؛ بدون حذف) ======================== //

    /** کاشت N خودروی تصادفی با استفاده از همان addRandomVehicle موجود. */
    public static void seedVehicles(final World world, final CityMap map, final int count) { // // متد جدید
        int n = (count < 0) ? 0 : count;                        // // دفاع در برابر مقدار منفی
        int i; for (i = 0; i < n; i++) {                        // // تکرار
            addRandomVehicle(world, map);                       // // همان منطق افزودن تصادفی
        }
    }

    /** کاشت پیش‌فرض (۲۰ خودرو) برای راحتی سناریوهای سریع. */
    public static void seedVehicles(final World world, final CityMap map) { // // متد کمکی جدید
        seedVehicles(world, map, 20);                                       // // فراخوانی نسخهٔ اصلی
    }
}


























//package simulation; // // پکیج شبیه‌سازی
//
//import core.Direction; // // جهت‌ها
//import core.VehicleType; // // نوع وسیله
//import infrastructure.CityMap; // // نقشه شهر
//import infrastructure.Intersection; // // تقاطع
//import infrastructure.Lane; // // لِین
//import infrastructure.Road; // // جاده
//import trafficcontrol.LightState; // // وضعیت چراغ
//import trafficcontrol.TrafficControlDevice; // // اینترفیس کنترل ترافیک
//import trafficcontrol.TrafficLight; // // کلاس چراغ راهنما
//
//import java.lang.reflect.Constructor; // // سازنده بازتابی
//import java.lang.reflect.Method; // // متد بازتابی
//import java.util.ArrayList; // // لیست پویا
//import java.util.List; // // اینترفیس لیست
//import java.util.Random; // // تصادفی
//
///**
// * ابزار سناریو: نصب چراغ‌ها و افزودن خودروی تصادفی //
// * این نسخه امضاهای متفاوت TrafficLight را به‌صورت ایمن پشتیبانی می‌کند. //
// */
//public final class DemoTraffic { // // کلاس نهایی ابزار
//    private static final Random RNG = new Random(); // // مولد تصادفی مشترک
//
//    private DemoTraffic() { /* // جلوگیری از نمونه‌سازی */ } // // سازنده خصوصی
//
//    // ------------------------- نصب چراغ‌ها -------------------------
//
//    /** نصب چراغ برای هر چهار رویکرد در تمام تقاطع‌ها (N,E,S,W) با زمان‌های داده‌شده (ms). */
//    public static void installLights(final World world, final CityMap map,
//                                     final int greenMs, final int yellowMs, final int redMs) {
//        // // پیمایش تمام تقاطع‌ها
//        int i; for (i = 0; i < map.getIntersections().size(); i++) { // // حلقه تقاطع‌ها
//            final Intersection it = map.getIntersections().get(i); // // تقاطع جاری
//            attachIfMissing(world, it, Direction.NORTH, greenMs, yellowMs, redMs); // // رویکرد شمال
//            attachIfMissing(world, it, Direction.EAST,  greenMs, yellowMs, redMs); // // رویکرد شرق
//            attachIfMissing(world, it, Direction.SOUTH, greenMs, yellowMs, redMs); // // رویکرد جنوب
//            attachIfMissing(world, it, Direction.WEST,  greenMs, yellowMs, redMs); // // رویکرد غرب
//        }
//    }
//
//    /** اگر رویکردی کنترل نداشت، یک TrafficLight با امضای سازگار بساز و ثبت کن. */
//    private static void attachIfMissing(final World world, final Intersection it, final Direction d,
//                                        final int g, final int y, final int r) {
//        // // کنترل فعلی آن رویکرد
//        final TrafficControlDevice dev = it.getControl(d); // // دریافت کنترل
//        if (dev != null) return; // // اگر هست، نیازی نیست
//
//        // // ساخت شناسه یکتا برای چراغ
//        final String id = "TL-" + it.getId() + "-" + d; // // ID چراغ
//
//        // // تلاش برای ساخت چراغ با امضاهای مختلف
//        final TrafficLight tl = makeTrafficLightFlexible(id, it, d, g, y, r); // // ساخت چراغ ایمن
//        if (tl == null) { // // اگر ساخت ناموفق بود
//            System.err.println("Failed to create TrafficLight for " + id + " direction " + d); // // گزارش خطا
//            return; // // خروج
//        }
//
//        // // ثبت چراغ روی همان تقاطع
//        it.setControl(d, tl); // // ست کنترل رویکرد
//
//        // // تلاش برای ثبت چراغ داخل World (برای رندر/مدیریت)
//        tryRegisterTrafficLightInWorld(world, tl); // // ثبت در دنیا
//    }
//
//    /** ساخت TrafficLight با جستجوی چند امضا (۶پارامتری و ۵پارامتری و امضاهای قدیمی). */
//    private static TrafficLight makeTrafficLightFlexible(final String id,
//                                                         final Intersection at,
//                                                         final Direction dir,
//                                                         final int greenMs,
//                                                         final int yellowMs,
//                                                         final int redMs) {
//        try {
//            // // ۱) تلاش امضای مدرن ۶ پارامتری: (String, Direction, int, int, int, int tickIntervalMs)
//            Constructor<TrafficLight> c6 = getCtorTL(
//                    new Class[]{String.class, Direction.class, int.class, int.class, int.class, int.class}); // // امضای ۶تایی
//            if (c6 != null) { // // اگر یافت شد
//                int tick = SimulationConfig.TICK_INTERVAL; // // فاصله تیک از کانفیگ
//                return c6.newInstance(new Object[]{id, dir, Integer.valueOf(greenMs), Integer.valueOf(yellowMs), Integer.valueOf(redMs), Integer.valueOf(tick)}); // // ساخت
//            }
//
//            // // ۲) تلاش امضای ۶ پارامتری قدیمی: (String, Direction, int, int, int, LightState)
//            Constructor<TrafficLight> c6b = getCtorTL(
//                    new Class[]{String.class, Direction.class, int.class, int.class, int.class, LightState.class}); // // امضای ۶تایی با LightState
//            if (c6b != null) { // // اگر یافت شد
//                return c6b.newInstance(new Object[]{id, dir, Integer.valueOf(greenMs), Integer.valueOf(yellowMs), Integer.valueOf(redMs), LightState.GREEN}); // // ساخت
//            }
//
//            // // ۳) تلاش امضای ۵ پارامتری: (String, Direction, int, int, int)
//            Constructor<TrafficLight> c5 = getCtorTL(
//                    new Class[]{String.class, Direction.class, int.class, int.class, int.class}); // // امضای ۵تایی
//            if (c5 != null) { // // اگر یافت شد
//                return c5.newInstance(new Object[]{id, dir, Integer.valueOf(greenMs), Integer.valueOf(yellowMs), Integer.valueOf(redMs)}); // // ساخت
//            }
//
//            // // ۴) تلاش امضای مبتنی بر Intersection (قدیمی): (Intersection, Direction, long, long, long)
//            Constructor<TrafficLight> cOld1 = getCtorTL(
//                    new Class[]{infrastructure.Intersection.class, Direction.class, long.class, long.class, long.class}); // // امضای قدیمی
//            if (cOld1 != null) { // // اگر یافت شد
//                return cOld1.newInstance(new Object[]{at, dir, Long.valueOf(greenMs), Long.valueOf(yellowMs), Long.valueOf(redMs)}); // // ساخت
//            }
//
//            // // ۵) تلاش امضای (String, Direction, long, long, long)
//            Constructor<TrafficLight> cOld2 = getCtorTL(
//                    new Class[]{String.class, Direction.class, long.class, long.class, long.class}); // // امضای قدیمی با long
//            if (cOld2 != null) { // // اگر یافت شد
//                return cOld2.newInstance(new Object[]{id, dir, Long.valueOf(greenMs), Long.valueOf(yellowMs), Long.valueOf(redMs)}); // // ساخت
//            }
//
//        } catch (Throwable t) { // // گرفتن هر خطا
//            t.printStackTrace(); // // چاپ استک برای اشکال‌زدایی
//        }
//        return null; // // نتوانستیم بسازیم
//    }
//
//    /** کمک‌متد برای گرفتن سازنده‌ی TrafficLight با امضای دلخواه، یا null اگر نبود. */
//    @SuppressWarnings("unchecked") // // حذف هشدار جنریک
//    private static Constructor<TrafficLight> getCtorTL(Class<?>[] sig) {
//        try { // // تلاش
//            return (Constructor<TrafficLight>) TrafficLight.class.getConstructor(sig); // // گرفتن سازنده
//        } catch (Throwable ignored) { // // اگر نبود
//            return null; // // null
//        }
//    }
//
//    /** تلاش برای ثبت چراغ در World با امضاهای رایج (addTrafficLight / registerDevice). */
//    private static void tryRegisterTrafficLightInWorld(final World world, final TrafficLight tl) {
//        try { // // تلاش امضای addTrafficLight(TrafficLight)
//            Method m = world.getClass().getMethod("addTrafficLight", TrafficLight.class); // // متد
//            m.invoke(world, tl); // // فراخوانی
//            return; // // موفق
//        } catch (Throwable ignored) { /* // ادامه */ }
//
//        try { // // تلاش امضای registerDevice(TrafficControlDevice)
//            Method m = world.getClass().getMethod("registerDevice", TrafficControlDevice.class); // // متد
//            m.invoke(world, tl); // // فراخوانی
//            return; // // موفق
//        } catch (Throwable ignored) { /* // ادامه */ }
//
//        // // اگر هیچ‌کدام نبود، همان setControl روی Intersection کافی است. //
//    }
//
//    // ---------------------- افزودن تصادفی خودرو ----------------------
//
//    /** افزودن یک خودرو به‌شکل ساده و تصادفی روی یکی از لِین‌ها (حداقل نسخه برای تست). */
//    public static void addRandomVehicle(final World world, final CityMap map) {
//        // // انتخاب لِین تصادفی
//        final Lane spawn = pickRandomLane(map); // // لِین برای اسپاون
//        if (spawn == null) return; // // اگر لِینی نبود
//
//        // // ساخت یک Vehicle مینیمال با Reflection (برای هماهنگی با امضاهای مختلف پروژه‌ها)
//        final Object vehicle = reflectMakeVehicleBasic(spawn); // // ساخت خودرو
//        if (vehicle == null) return; // // اگر نشد
//
//        // // تلاش برای ثبت خودرو داخل World
//        reflectRegisterVehicleInWorld(world, vehicle); // // افزودن به دنیا
//    }
//
//    /** انتخاب تصادفی یک لِین از کل نقشه. */
//    private static Lane pickRandomLane(final CityMap map) {
//        final List<Lane> lanes = new ArrayList<Lane>(); // // لیست تجمیعی
//        int i; for (i = 0; i < map.getRoads().size(); i++) { // // حلقه راه‌ها
//            final Road r = map.getRoads().get(i); // // راه جاری
//            lanes.addAll(r.getForwardLanes()); // // افزودن رفت
//            lanes.addAll(r.getBackwardLanes()); // // افزودن برگشت
//        }
//        if (lanes.isEmpty()) return null; // // خالی بود
//        return lanes.get(RNG.nextInt(lanes.size())); // // یکی تصادفی
//    }
//
//    /** ساخت یک Vehicle با امضاهای رایج، حداقلی برای اجرا. */
//    private static Object reflectMakeVehicleBasic(final Lane lane) {
//        try { // // تلاش
//            Class<?> vehicleCls = Class.forName("core.Vehicle"); // // کلاس Vehicle
//            // // امضای رایج: () بدون پارامتر
//            try {
//                Object v = vehicleCls.getConstructor().newInstance(); // // ساخت
//                trySetLane(v, lane); // // ست لِین
//                return v; // // خروجی
//            } catch (Throwable ignored) { /* // ادامه */ }
//
//            // // امضای (String)
//            try {
//                Object v = vehicleCls.getConstructor(String.class).newInstance("V-" + System.currentTimeMillis()); // // ساخت
//                trySetLane(v, lane); // // ست لِین
//                return v; // // خروجی
//            } catch (Throwable ignored) { /* // ادامه */ }
//
//            // // امضای (String, core.VehicleType, infrastructure.Lane)
//            try {
//                Class<?> vt = Class.forName("core.VehicleType"); // // کلاس VehicleType
//                Object anyType = vt.getEnumConstants()[0]; // // یک مقدار دلخواه از enum
//                Object v = vehicleCls.getConstructor(String.class, vt, Lane.class)
//                        .newInstance("V-" + System.currentTimeMillis(), anyType, lane); // // ساخت
//                return v; // // خروجی
//            } catch (Throwable ignored) { /* // ادامه */ }
//
//        } catch (Throwable t) { // // خطای کلی
//            t.printStackTrace(); // // چاپ برای دیباگ
//        }
//        return null; // // ساخت ناموفق
//    }
//
//    /** تلاش برای setLane یا setCurrentLane با پارامتر Lane. */
//    private static void trySetLane(final Object target, final Lane lane) {
//        try { // // setLane(Lane)
//            Method m = target.getClass().getMethod("setLane", Lane.class); // // متد
//            m.invoke(target, lane); // // فراخوانی
//            return; // // موفق
//        } catch (Throwable ignored) { /* // ادامه */ }
//
//        try { // // setCurrentLane(Lane)
//            Method m = target.getClass().getMethod("setCurrentLane", Lane.class); // // متد
//            m.invoke(target, lane); // // فراخوانی
//        } catch (Throwable ignored) { /* // ادامه */ }
//    }
//
//    /** ثبت خودرو داخل World با امضاهای رایج. */
//    private static void reflectRegisterVehicleInWorld(final World world, final Object vehicle) {
//        try { // // addVehicle(core.Vehicle)
//            Method m = world.getClass().getMethod("addVehicle", Class.forName("core.Vehicle")); // // متد
//            m.invoke(world, vehicle); // // فراخوانی
//            return; // // موفق
//        } catch (Throwable ignored) { /* // ادامه */ }
//
//        try { // // registerVehicle(core.Vehicle)
//            Method m = world.getClass().getMethod("registerVehicle", Class.forName("core.Vehicle")); // // متد
//            m.invoke(world, vehicle); // // فراخوانی
//            return; // // موفق
//        } catch (Throwable ignored) { /* // ادامه */ }
//
//        try { // // addEntity(Object)
//            Method m = world.getClass().getMethod("addEntity", Object.class); // // متد
//            m.invoke(world, vehicle); // // فراخوانی
//        } catch (Throwable ignored) { /* // ادامه */ }
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
////
////// simulation/DemoTraffic.java
////package simulation;                               // // پکیج
////
////import infrastructure.*;                          // // CityMap/Intersection/Road/Lane
////import core.VehicleType;                          // // نوع وسیله
////import trafficcontrol.*;                          // // تجهیزات کنترل (چراغ/وضعیت)
////import java.util.*;                               // // کالکشن‌ها
////import java.lang.reflect.*;                       // // Reflection ساخت Vehicle
////
////public final class DemoTraffic {                  // // ابزار سناریو
////    private static final Random RNG = new Random(); // // رندوم مشترک
////    private DemoTraffic() {}                      // // جلوگیری از نمونه‌سازی
////
////    // ---------------- نصب چراغ برای همهٔ رویکردها ----------------
////    public static void installLights(final World world, final CityMap map,
////                                     final int greenMs, final int yellowMs, final int redMs) {
////        final long g = greenMs, y = yellowMs, r = redMs;     // // به long برای سازندهٔ TrafficLight
////        for (int i = 0; i < map.getIntersections().size(); i++) { // // پیمایش تقاطع‌ها
////            final Intersection it = map.getIntersections().get(i); // // تقاطع
////            attachIfMissing(world, it, core.Direction.NORTH, g, y, r); // // N
////            attachIfMissing(world, it, core.Direction.EAST , g, y, r); // // E
////            attachIfMissing(world, it, core.Direction.SOUTH, g, y, r); // // S
////            attachIfMissing(world, it, core.Direction.WEST , g, y, r); // // W
////        }
////    }
////
////    private static void attachIfMissing(final World world, final Intersection it,
////                                        final core.Direction d, final long g, final long y, final long r) {
////        final TrafficControlDevice dev = it.getControl(d);   // // کنترل فعلی
////        if (dev == null) {                                   // // اگر چیزی نیست
////            final TrafficLight tl = new TrafficLight(it, d, g, y, r); // // ساخت چراغ
////            it.setControl(d, tl);                            // // ثبت روی تقاطع
////            world.addTrafficLight(tl);                       // // ثبت برای رندر/آپدیت
////        }
////    }
////
////    // ---------------- افزودن تصادفی یک Vehicle ----------------
////    public static void addRandomVehicle(final World world, final CityMap map) { // // افزودن خودرو
////        final Lane spawnLane = pickRandomLane(map);          // // لِین تصادفی
////        if (spawnLane == null) return;                       // // محافظت
////
////        final VehicleType type = randomVehicleType();        // // نوع
////        final double startSpeed = type.getMinSpeed() +
////                RNG.nextDouble() * Math.max(0.1, type.getMaxSpeed() - type.getMinSpeed()); // // سرعت رندوم
////        final double pos = 0.0;                              // // موضع شروع
////        final String id = "VH-" + System.currentTimeMillis() + "-" + Math.abs(RNG.nextInt()); // // شناسه
////
////        final Object vehicle = reflectMakeVehicle(id, type, spawnLane, pos, startSpeed); // // ساخت
////        if (vehicle == null) return;                        // // اگر نشد، خروج
////        reflectRegisterVehicleInWorld(world, vehicle);      // // ثبت در دنیا
////    }
////
////    private static Lane pickRandomLane(final CityMap map) { // // انتخاب لِین
////        final ArrayList<Lane> lanes = new ArrayList<Lane>(); // // تجمیع
////        for (int i=0;i<map.getRoads().size();i++){          // // حلقه راه‌ها
////            Road r = map.getRoads().get(i);                 // // راه
////            lanes.addAll(r.getForwardLanes());              // // رفت
////            lanes.addAll(r.getBackwardLanes());             // // برگشت
////        }
////        if (lanes.isEmpty()) return null;                   // // محافظت
////        return lanes.get(RNG.nextInt(lanes.size()));        // // یک لِین تصادفی
////    }
////
////    private static VehicleType randomVehicleType() {        // // انتخاب نوع
////        VehicleType[] all = VehicleType.values();           // // همه انواع
////        return all[RNG.nextInt(all.length)];                // // تصادفی
////    }
////
////    // ---------------- Reflection helpers برای Vehicle ----------------
////    private static Object reflectMakeVehicle(final String id, final VehicleType type,
////                                             final Lane lane, final double pos, final double speed) {
////        try {
////            Class<?> vehicleCls = Class.forName("core.Vehicle"); // // کلاس Vehicle
////
////            // 1) (String, VehicleType, Lane, double position, double speed)
////            try {
////                Constructor<?> c1 = vehicleCls.getConstructor(String.class, VehicleType.class, Lane.class, double.class, double.class); // // امضاء
////                return c1.newInstance(new Object[]{id, type, lane, Double.valueOf(pos), Double.valueOf(speed)}); // // ساخت
////            } catch (Throwable ignored) {}
////
////            // 2) (String, VehicleType, Lane)
////            try {
////                Constructor<?> c2 = vehicleCls.getConstructor(String.class, VehicleType.class, Lane.class); // // امضاء
////                Object v = c2.newInstance(new Object[]{id, type, lane}); // // ساخت
////                trySetDouble(v, "setPositionInLane", pos);              // // ست موقعیت
////                trySetDouble(v, "setSpeed", speed);                      // // ست سرعت
////                trySetDouble(v, "setTargetSpeed", speed);                // // ست هدف
////                return v;
////            } catch (Throwable ignored) {}
////
////            // 3) (String, VehicleType)
////            try {
////                Constructor<?> c3 = vehicleCls.getConstructor(String.class, VehicleType.class); // // امضاء
////                Object v = c3.newInstance(new Object[]{id, type});  // // ساخت
////                trySetLane(v, lane);                                 // // ست لِین
////                trySetDouble(v, "setPositionInLane", pos);           // // ست موقعیت
////                trySetDouble(v, "setSpeed", speed);                  // // ست سرعت
////                trySetDouble(v, "setTargetSpeed", speed);            // // ست هدف
////                return v;
////            } catch (Throwable ignored) {}
////
////            // 4) (String)
////            try {
////                Constructor<?> c4 = vehicleCls.getConstructor(String.class); // // امضاء
////                Object v = c4.newInstance(new Object[]{id});                 // // ساخت
////                trySetEnum(v, "setType", VehicleType.class, type);           // // نوع
////                trySetLane(v, lane);                                         // // لِین
////                trySetDouble(v, "setPositionInLane", pos);                   // // موقعیت
////                trySetDouble(v, "setSpeed", speed);                          // // سرعت
////                trySetDouble(v, "setTargetSpeed", speed);                    // // هدف
////                return v;
////            } catch (Throwable ignored) {}
////
////        } catch (Throwable ignored) {}
////        return null; // // نشد ساخت
////    }
////
////    private static void reflectRegisterVehicleInWorld(final World world, final Object vehicle) { // // ثبت
////        try {
////            Method m = world.getClass().getMethod("addVehicle", Class.forName("core.Vehicle")); // // امضاء ۱
////            m.invoke(world, vehicle);                                                           // // صدا
////            return;
////        } catch (Throwable ignored) {}
////        try {
////            Method m = world.getClass().getMethod("registerVehicle", Class.forName("core.Vehicle")); // // امضاء ۲
////            m.invoke(world, vehicle);                                                                  // // صدا
////            return;
////        } catch (Throwable ignored) {}
////        try {
////            Method m = world.getClass().getMethod("addEntity", Object.class); // // امضاء ۳
////            m.invoke(world, vehicle);                                         // // صدا
////        } catch (Throwable ignored) {}
////    }
////
////    private static void trySetDouble(final Object target, final String setter, final double value) { // // ست double
////        try {
////            Method m = target.getClass().getMethod(setter, double.class); // // متد
////            m.invoke(target, new Object[]{ Double.valueOf(value) });      // // صدا
////        } catch (Throwable ignored) {}
////    }
////
////    private static void trySetEnum(final Object target, final String setter,
////                                   final Class<?> enumCls, final Object enumValue) { // // ست enum
////        try {
////            Method m = target.getClass().getMethod(setter, enumCls); // // متد
////            m.invoke(target, enumValue);                             // // صدا
////        } catch (Throwable ignored) {}
////    }
////
////    private static void trySetLane(final Object target, final Lane lane) { // // ست Lane
////        try {
////            Method m = target.getClass().getMethod("setLane", Lane.class); // // setLane
////            m.invoke(target, lane);
////            return;
////        } catch (Throwable ignored) {}
////        try {
////            Method m = target.getClass().getMethod("setCurrentLane", Lane.class); // // setCurrentLane
////            m.invoke(target, lane);
////        } catch (Throwable ignored) {}
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
//////// simulation/DemoTraffic.java
//////package simulation; // // پکیج شبیه‌سازی
//////
//////import infrastructure.CityMap;         // // نقشه
//////import infrastructure.Intersection;    // // تقاطع
//////import infrastructure.Road;            // // جاده
//////import infrastructure.Lane;            // // لِین
//////import core.VehicleType;               // // نوع وسیله
//////import trafficcontrol.*;               // // چراغ/وضعیت
//////import java.util.*;                    // // کالکشن‌ها
//////import java.lang.reflect.*;            // // Reflection برای ساخت Vehicle
//////
//////public final class DemoTraffic { // // ابزار سناریو
//////    private static final Random RNG = new Random(); // // رندوم
//////    private DemoTraffic() {} // // جلوگیری از نمونه‌سازی
//////
//////    // ------------------------- نصب چراغ‌ها -------------------------
//////
//////    /** نصب چراغ در همهٔ تقاطع‌ها برای چهار رویکرد (N,E,S,W). */
//////    public static void installLights(final World world, final CityMap map,
//////                                     final int greenMs, final int yellowMs, final int redMs) {
//////        // امضا را به long تبدیل می‌کنیم چون TrafficLight(long...) می‌خواهد.
//////        final long g = greenMs, y = yellowMs, r = redMs; // // تبدیل
//////        for (int i = 0; i < map.getIntersections().size(); i++) { // // پیمایش
//////            final Intersection it = map.getIntersections().get(i); // // تقاطع
//////            attachIfMissing(world, it, core.Direction.NORTH, g, y, r); // // شمال
//////            attachIfMissing(world, it, core.Direction.EAST , g, y, r); // // شرق
//////            attachIfMissing(world, it, core.Direction.SOUTH, g, y, r); // // جنوب
//////            attachIfMissing(world, it, core.Direction.WEST , g, y, r); // // غرب
//////        }
//////    }
//////
//////    /** اگر کنترلی برای رویکرد نبود، چراغ راهنما نصب می‌کند. */
//////    private static void attachIfMissing(final World world, final Intersection it,
//////                                        final core.Direction d, final long g, final long y, final long r) {
//////        final TrafficControlDevice dev = it.getControl(d); // // کنترل فعلی
//////        if (dev == null) { // // اگر چیزی نصب نیست
//////            final TrafficLight tl = new TrafficLight(it, d, g, y, r); // // سازندهٔ هماهنگ
//////            it.setControl(d, tl);            // // ثبت روی تقاطع (World از این طریق می‌بیند)
//////            world.addTrafficLight(tl);       // // برای رندر/مدیریت در World
//////        }
//////    }
//////
//////    // ---------------------- افزودن تصادفی خودرو ----------------------
//////
//////    public static void addRandomVehicle(final World world, final CityMap map) { // // افزودن خودرو
//////        final Lane spawnLane = pickRandomLane(map);      // // لِین تصادفی
//////        if (spawnLane == null) return;                   // // محافظت
//////
//////        final VehicleType type = randomVehicleType();    // // نوع
//////        final double startSpeed = type.getMinSpeed() +
//////                RNG.nextDouble() * (Math.max(0.1, type.getMaxSpeed() - type.getMinSpeed())); // // سرعت رندوم
//////        final double pos = 0.0;                          // // موضع شروع
//////        final String id = "VH-" + System.currentTimeMillis() + "-" + Math.abs(RNG.nextInt()); // // شناسه
//////
//////        final Object vehicle = reflectMakeVehicle(id, type, spawnLane, pos, startSpeed); // // ساخت Vehicle
//////        if (vehicle == null) return;                       // // اگر نشد، خروج
//////        reflectRegisterVehicleInWorld(world, vehicle);     // // ثبت در دنیا
//////    }
//////
//////    private static Lane pickRandomLane(final CityMap map) { // // انتخاب لِین
//////        final ArrayList<Lane> lanes = new ArrayList<Lane>(); // // تجمیع
//////        for (int i=0;i<map.getRoads().size();i++){ // // حلقه راه‌ها
//////            Road r = map.getRoads().get(i);        // // راه
//////            lanes.addAll(r.getForwardLanes());     // // رفت
//////            lanes.addAll(r.getBackwardLanes());    // // برگشت
//////        }
//////        if (lanes.isEmpty()) return null;          // // محافظت
//////        return lanes.get(RNG.nextInt(lanes.size())); // // تصادفی
//////    }
//////
//////    private static VehicleType randomVehicleType() { // // انتخاب نوع
//////        VehicleType[] all = VehicleType.values();    // // همه
//////        return all[RNG.nextInt(all.length)];         // // تصادفی
//////    }
//////
//////    // ---------- Reflection helpers ----------
//////
//////    /** تلاش برای ساخت Vehicle با امضاهای رایج پروژهٔ تو. */
//////    private static Object reflectMakeVehicle(final String id, final VehicleType type,
//////                                             final Lane lane, final double pos, final double speed) {
//////        try {
//////            Class<?> vehicleCls = Class.forName("core.Vehicle"); // // کلاس Vehicle
//////
//////            // 1) (String, VehicleType, infrastructure.Lane, double, double)
//////            try {
//////                Constructor<?> c1 = vehicleCls.getConstructor(String.class, VehicleType.class, Lane.class, double.class, double.class); // // امضاء
//////                return c1.newInstance(new Object[]{id, type, lane, Double.valueOf(pos), Double.valueOf(speed)}); // // ساخت
//////            } catch (Throwable ignored) {}
//////
//////            // 2) (String, VehicleType, infrastructure.Lane)
//////            try {
//////                Constructor<?> c2 = vehicleCls.getConstructor(String.class, VehicleType.class, Lane.class); // // امضاء
//////                Object v = c2.newInstance(new Object[]{id, type, lane}); // // ساخت
//////                trySetDouble(v, "setPositionInLane", pos);              // // ست موضع
//////                trySetDouble(v, "setSpeed", speed);                      // // ست سرعت
//////                trySetDouble(v, "setTargetSpeed", speed);                // // ست هدف
//////                return v;
//////            } catch (Throwable ignored) {}
//////
//////            // 3) (String, VehicleType)
//////            try {
//////                Constructor<?> c3 = vehicleCls.getConstructor(String.class, VehicleType.class); // // امضاء
//////                Object v = c3.newInstance(new Object[]{id, type});  // // ساخت
//////                trySetLane(v, lane);                                 // // ست لِین
//////                trySetDouble(v, "setPositionInLane", pos);           // // ست موضع
//////                trySetDouble(v, "setSpeed", speed);                   // // ست سرعت
//////                trySetDouble(v, "setTargetSpeed", speed);             // // ست هدف
//////                return v;
//////            } catch (Throwable ignored) {}
//////
//////            // 4) (String)
//////            try {
//////                Constructor<?> c4 = vehicleCls.getConstructor(String.class); // // امضاء
//////                Object v = c4.newInstance(new Object[]{id}); // // ساخت
//////                trySetEnum(v, "setType", VehicleType.class, type); // // نوع
//////                trySetLane(v, lane);                                // // لِین
//////                trySetDouble(v, "setPositionInLane", pos);          // // موضع
//////                trySetDouble(v, "setSpeed", speed);                  // // سرعت
//////                trySetDouble(v, "setTargetSpeed", speed);            // // هدف
//////                return v;
//////            } catch (Throwable ignored) {}
//////
//////        } catch (Throwable ignored) {}
//////        return null; // // نشد
//////    }
//////
//////    private static void reflectRegisterVehicleInWorld(final World world, final Object vehicle) { // // ثبت خودرو
//////        try {
//////            Method m = world.getClass().getMethod("addVehicle", Class.forName("core.Vehicle")); // // امضاء ۱
//////            m.invoke(world, vehicle); // // صدا
//////            return;
//////        } catch (Throwable ignored) {}
//////        try {
//////            Method m = world.getClass().getMethod("registerVehicle", Class.forName("core.Vehicle")); // // امضاء ۲
//////            m.invoke(world, vehicle); // // صدا
//////            return;
//////        } catch (Throwable ignored) {}
//////        try {
//////            Method m = world.getClass().getMethod("addEntity", Object.class); // // امضاء ۳
//////            m.invoke(world, vehicle); // // صدا
//////        } catch (Throwable ignored) {}
//////    }
//////
//////    private static void trySetDouble(final Object target, final String setter, final double value) { // // ست double
//////        try {
//////            Method m = target.getClass().getMethod(setter, double.class); // // متد
//////            m.invoke(target, new Object[]{ Double.valueOf(value) });      // // صدا
//////        } catch (Throwable ignored) {}
//////    }
//////
//////    private static void trySetEnum(final Object target, final String setter,
//////                                   final Class<?> enumCls, final Object enumValue) { // // ست enum
//////        try {
//////            Method m = target.getClass().getMethod(setter, enumCls); // // متد
//////            m.invoke(target, enumValue); // // صدا
//////        } catch (Throwable ignored) {}
//////    }
//////
//////    private static void trySetLane(final Object target, final Lane lane) { // // ست Lane
//////        try {
//////            Method m = target.getClass().getMethod("setLane", Lane.class); // // setLane
//////            m.invoke(target, lane);
//////            return;
//////        } catch (Throwable ignored) {}
//////        try {
//////            Method m = target.getClass().getMethod("setCurrentLane", Lane.class); // // setCurrentLane
//////            m.invoke(target, lane);
//////        } catch (Throwable ignored) {}
//////    }
//////}
