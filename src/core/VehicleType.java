package core; // // پکیج هسته

import java.util.Random; // // تصادفی

/**
 * نوع وسیله نقلیه + بازهٔ سرعت مجاز (به «واحد/ثانیه» یا هر واحدی که در شبیه‌ساز استفاده می‌کنی).
 * متدهای جدید:
 *   - getMinSpeed(), getMaxSpeed() ← بازگشت حدود مجاز
 *   - randomSpeed(Random)          ← انتخاب سرعت تصادفی «در محدودهٔ مجاز» برای این نوع
 *
 * توجه: نام‌ها/ثابت‌ها حفظ شده و فقط قابلیت سرعت اضافه شده تا با کدهای قبلی ناسازگار نشود.
 */
public enum VehicleType { // // انواع وسیله
    CAR        (12.0, 22.0), // // خودرو سواری: 12..22 واحد/ثانیه
    BUS        ( 8.0, 14.0), // // اتوبوس:      8..14
    TRUCK      ( 7.0, 12.0), // // کامیون:      7..12
    MOTORCYCLE (14.0, 25.0); // // موتورسیکلت: 14..25

    private final double minSpeed; // // حداقل سرعت مجاز
    private final double maxSpeed; // // حداکثر سرعت مجاز

    VehicleType(double minSpeed, double maxSpeed){ // // سازنده enum
        this.minSpeed = minSpeed;                  // // ذخیره حداقل
        this.maxSpeed = maxSpeed;                  // // ذخیره حداکثر
    }

    public double getMinSpeed(){ return minSpeed; } // // گتر حداقل
    public double getMaxSpeed(){ return maxSpeed; } // // گتر حداکثر

    public double randomSpeed(Random rnd){          // // سرعت تصادفی در بازه
        if (rnd == null) rnd = new Random();        // // محافظت
        double span = maxSpeed - minSpeed;          // // پهنای بازه
        if (span <= 0.0) return minSpeed;           // // اگر بازه صفر شد
        return minSpeed + rnd.nextDouble() * span;  // // خروجی تصادفی
    }
}





















//package core;
//
//public enum VehicleType {
//    CAR,
//    BUS,
//    TRUCK,
//    BIKE
//}
