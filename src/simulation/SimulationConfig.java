package simulation; // // پکیج شبیه‌سازی

public class SimulationConfig { // // کلاس ثابت‌ها

    // ===== موجودِ قبلی ===== //
    public static final int MAX_VEHICLE_COUNT = 100; // // حداکثر خودرو
    public static final int DEFAULT_SPEED_LIMIT = 50; // // سرعت پیش‌فرض (نمادین)
    public static final int TICK_INTERVAL = 100; // // میلی‌ثانیه بین هر تیک (برای Timer قدیمی)

    // ======= ثابت‌های جدید برای اندازه نقشه و حداقل طول بلوک =======
    public static final int MAP_SCALE = 2;          // // ضریب مقیاس نقشه (۲ یعنی ۲×)
    public static final int MIN_BLOCK_PX = 140;     // // حداقل طول هر بلوک/خیابان به پیکسل (قبلاً 60 بود)




    // ===== افزوده برای IDM / Jerk ===== //
    public static final double TICK_DT_SEC = 1.0 / 60.0;         // // گام زمانی هدف (ثانیه)
    public static final double DESIRED_HEADWAY_S = 1.3;          // // فاصله‌ی زمانی مطلوب T
    public static final double MIN_GAP_PX = 18.0;                 // // حداقل فاصله ساکن s0
    public static final double MAX_ACCEL_PXPS2 = 160.0;           // // aMax (px/s^2)
    public static final double COMFORT_DECEL_PXPS2 = 220.0;       // // b (px/s^2)
    public static final double JERK_LIMIT_PXPS3 = 3000.0;         // // محدودیت jerk (px/s^3)
    public static final double DESIRED_SPEED_PXPS = 120.0;        // // v0 سرعت آزاد (px/s)
    public static final double VEHICLE_LENGTH_PX = 14.0;          // // طول نموداری خودرو (px)
}











//
//
//package simulation;
//
//public class SimulationConfig {
//    public static final int MAX_VEHICLE_COUNT = 100;
//    public static final int DEFAULT_SPEED_LIMIT = 50;
//    public static final int TICK_INTERVAL = 100; // میلی‌ثانیه بین هر تیک
//}
//




