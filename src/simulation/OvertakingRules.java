package simulation;                           // پکیج شبیه‌سازی

import core.Vehicle;                          // استفاده از کلاس Vehicle
import infrastructure.Lane;                   // دسترسی به Lane برای لاین کناری
import java.lang.reflect.Method;              // Reflection برای فراخوانی امنِ متدهای احتمالی
// (بدون وابستگی سخت به امضای Vehicle)

/**
 * قوانین سادهٔ سبقت — فقط بررسی می‌کند آیا لاین چپ موجود است و
 * آیا سرعت فعلی خودرو نسبت به سقف/هدف سرعتش به اندازه کافی پایین است.
 * نکتهٔ مهم: برای سازگاری با نسخه‌های مختلف Vehicle، ما سقف سرعت را
 * با Reflection از getMaxSpeed() یا getTargetSpeed() می‌خوانیم.
 */
public class OvertakingRules {                // کلاس قوانین سبقت

    /** آیا سبقت مجاز است؟ */
    public boolean canOvertake(Vehicle v) {   // متد اصلی بررسی
        if (v == null) return false;          // محافظت از null

        Lane currentLane = v.getCurrentLane();// لاین فعلی خودرو
        if (currentLane == null) return false;// اگر لاین نداریم، سبقت بی‌معنی است

        Lane leftLane = currentLane.getLeftAdjacentLane(); // یافتن لاین سمت چپ
        if (leftLane == null) return false;   // اگر لاین چپ موجود نیست، مجاز نیست

        double vmax = resolveMaxOrTargetSpeed(v); // استخراج سقف/هدف سرعت به‌صورت سازگار
        if (vmax <= 0) vmax = 1.0;            // محافظت از تقسیم بر صفر/مقادیر غیرمنطقی

        // شرط نمونه: وقتی سرعت فعلی کمتر از ۸۰٪ سرعت سقف/هدف باشد اجازهٔ سبقت بده
        // (یعنی راننده انگیزهٔ افزایش سرعت دارد.)
        return v.getSpeed() < (0.8 * vmax);   // تصمیم نهایی
    }

    // --------- کمک‌متد: استخراج «سقف/هدف سرعت» به‌صورت سازگار ---------

    private double resolveMaxOrTargetSpeed(Vehicle v) { // استخراج سقف/هدف سرعت
        // ۱) ابتدا تلاش برای getMaxSpeed()
        Double d = tryInvokeDoubleGetter(v, "getMaxSpeed"); // تلاش برای گرفتن مقدار
        if (d != null) return d.doubleValue();              // اگر موفق شد همان را برگردان

        // ۲) اگر نبود، تلاش برای getTargetSpeed() (در نسخه‌هایی که IDM پیاده شده)
        d = tryInvokeDoubleGetter(v, "getTargetSpeed");     // تلاش دوم
        if (d != null) return d.doubleValue();              // اگر موفق شد همان را برگردان

        // ۳) در نهایت یک پیش‌فرض معقول؛ اگر SimulationConfig مقدار مطلوب دارد از آن استفاده کن
        try {                                               // تلاش برای خواندن ثابت پیکربندی
            Class<?> cfg = Class.forName("simulation.SimulationConfig"); // کلاس پیکربندی
            try {
                // اگر ثابت DESIRED_SPEED_PXPS تعریف شده باشد از آن استفاده کن
                return cfg.getField("DESIRED_SPEED_PXPS").getDouble(null); // مقدار ثابت
            } catch (Throwable ignored) {
                // اگر آن ثابت نبود، تلاش برای DEFAULT_SPEED_LIMIT (در پروژه‌های قدیمی‌تر)
                try {
                    return cfg.getField("DEFAULT_SPEED_LIMIT").getInt(null); // مقدار پیش‌فرض قدیمی
                } catch (Throwable ignoredToo) {
                    // اگر هیچ‌کدام نبود، به یک پیش‌فرض fallback کن
                    return 120.0;                             // fallback منطقی به پیکسل‌برثانیه
                }
            }
        } catch (Throwable ignored) {
            // اگر کلاس پیکربندی هم نبود، همان fallback
            return 120.0;                                     // fallback نهایی
        }
    }

    /** فراخوانی امن یک getter دوبل با Reflection؛ اگر نبود یا خطا داد، null برمی‌گرداند. */
    private Double tryInvokeDoubleGetter(Vehicle v, String getterName) { // کمک‌متد Reflection
        try {
            Method m = v.getClass().getMethod(getterName);   // گرفتن متد به‌نام داده‌شده
            Object val = m.invoke(v);                        // فراخوانی متد روی شیء
            if (val instanceof Number) {                     // اگر خروجی عددی بود
                return Double.valueOf(((Number) val).doubleValue()); // تبدیل به Double
            }
        } catch (Throwable ignored) {
            // هیچ: اگر متد نبود/خطا داشت، null برمی‌گردانیم
        }
        return null;                                         // عدم موفقیت
    }
}
















//// simulation/OvertakingRules.java  (نال‌چک کوچک برای ایمنی)
//package simulation;                  // // پکیج
//
//import core.Vehicle;                // // خودرو
//import infrastructure.Lane;         // // لِین
//
//public class OvertakingRules {      // // قوانین سبقت
//    public boolean canOvertake(Vehicle v) {       // // آیا مجاز به سبقت؟
//        Lane currentLane = v.getCurrentLane();    // // لِین فعلی
//        if (currentLane == null) return false;    // // ✅ محافظت در برابر null
//
//        Lane leftLane = currentLane.getLeftAdjacentLane(); // // لِین چپ
//        if (leftLane == null) return false;                // // نبود یعنی ممنوع
//
//        return v.getSpeed() < v.getMaxSpeed() * 0.8;       // // شرط سرعت
//    }
//}
