package ui; // // پکیج UI

import java.awt.Color; // // رنگ

public final class UIConstants {                // // ثابت‌های UI
    private UIConstants() {}                    // // جلوگیری از نمونه‌سازی

    // پس‌زمینه
    public static final Color BACKGROUND   = new Color(18,18,22);        // // پس‌زمینهٔ تیره
    public static final Color GRAD_TOP     = new Color(18,18,22);        // // گرادیان بالا
    public static final Color GRAD_BOTTOM  = new Color(14,14,18);        // // گرادیان پایین

    // خیابان
    public static final int   LANE_WIDTH   = 14;                         // // پهنای هر لِین
    public static final int   LANE_GAP     = 4;                          // // فاصله بین لِین‌ها
    public static final Color ROAD_FILL    = new Color(44,46,54);        // // رنگ آسفالت
    public static final Color ROAD_HALO    = new Color(52,55,63,120);    // // هالهٔ بیرونی
    public static final Color DASH         = new Color(240,240,240);     // // خط‌چین

    // خودرو
    public static final int   VEHICLE_LENGTH = 22;                       // // طول خودرو
    public static final int   VEHICLE_WIDTH  = 12;                       // // عرض خودرو
    public static final Color CAR_BODY    = new Color(50,160,255);       // // رنگ بدنه
    public static final Color CAR_TRIM    = new Color(230,230,230);      // // تریم روشن
    public static final Color CAR_LINE    = Color.BLACK;                 // // خط دور
    public static final Color CAR_SHADOW  = new Color(0,0,0,90);         // // سایه

    // گذرگاه
    public static final Color CROSSING    = new Color(255,255,255,210);  // // سفید روشن

    // عابر
    public static final Color PEDESTRIAN       = new Color(255,255,255,230); // // سفید
    public static final Color PEDESTRIAN_RING  = new Color(0,0,0,160);       // // دورخط

    // چراغ
    public static final Color LIGHT_OFF   = new Color(90,90,90);         // // خاموش/خاکستری
    public static final Color LIGHT_RED   = new Color(230,60,60);        // // قرمز
    public static final Color LIGHT_YEL   = new Color(255,170,40);       // // زرد
    public static final Color LIGHT_GRN   = new Color(66,210,80);        // // سبز
    public static final Color LIGHT_RING  = new Color(0,0,0,200);        // // دور چراغ
    public static final Color LIGHT_SHADOW= new Color(0,0,0,160);        // // سایه
    public static final Color LIGHT_TAIL  = new Color(235,235,235,220);  // // خط‌راهنما
}





















//
//package ui; // // پکیج UI
//
//import java.awt.Color; // // رنگ‌ها
//
//public final class UIConstants { // // ثابت‌های واسط کاربری
//    private UIConstants() {} // // جلوگیری از نمونه‌سازی
//
//    public static final int LANE_WIDTH = 20; // // پهنای یک لِین (پیکسل)
//    public static final int LANE_GAP   = 6;  // // فاصله بین لِین‌ها
//
//    public static final Color BACKGROUND = new Color(170, 204, 130); // // سبز فضای شهری
//    public static final Color ROAD_FILL  = new Color(60, 60, 60);    // // خاکستری تیره آسفالت
//    public static final Color ROAD_EDGE  = new Color(40, 40, 40);    // // حاشیهٔ تیره‌تر
//    public static final Color DASH       = new Color(230, 230, 230); // // خط‌چین سفید
//
//    public static final int VEHICLE_LENGTH = 24; // // طول خودرو برای رندر
//    public static final int VEHICLE_WIDTH  = 14; // // عرض خودرو برای رندر
//
//    public static final double MIN_ZOOM = 0.4; // // کمترین زوم
//    public static final double MAX_ZOOM = 2.2; // // بیشترین زوم
//}
//

