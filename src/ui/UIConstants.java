
package ui; // // پکیج UI

import java.awt.Color; // // رنگ‌ها

public final class UIConstants { // // ثابت‌های واسط کاربری
    private UIConstants() {} // // جلوگیری از نمونه‌سازی

    public static final int LANE_WIDTH = 20; // // پهنای یک لِین (پیکسل)
    public static final int LANE_GAP   = 6;  // // فاصله بین لِین‌ها

    public static final Color BACKGROUND = new Color(170, 204, 130); // // سبز فضای شهری
    public static final Color ROAD_FILL  = new Color(60, 60, 60);    // // خاکستری تیره آسفالت
    public static final Color ROAD_EDGE  = new Color(40, 40, 40);    // // حاشیهٔ تیره‌تر
    public static final Color DASH       = new Color(230, 230, 230); // // خط‌چین سفید

    public static final int VEHICLE_LENGTH = 24; // // طول خودرو برای رندر
    public static final int VEHICLE_WIDTH  = 14; // // عرض خودرو برای رندر

    public static final double MIN_ZOOM = 0.4; // // کمترین زوم
    public static final double MAX_ZOOM = 2.2; // // بیشترین زوم
}






























//package ui; // // پکیج رابط کاربری
//
//import java.awt.Color; // // برای رنگ‌ها
//
//public final class UIConstants { // // کلاس ثابت‌های UI (final برای جلوگیری از ارث‌بری)
//    private UIConstants() {} // // جلوگیری از نمونه‌سازی
//
//    public static final int LANE_WIDTH = 20; // // پهنای هر لِین (پیکسل)
//    public static final int LANE_GAP   = 6;  // // فاصله بین لِین‌ها (پیکسل)
//
//    public static final Color BACKGROUND = new Color(170, 204, 130); // // رنگ پس‌زمینه نقشه
//    public static final Color ROAD_FILL  = new Color(60, 60, 60);    // // رنگ پرِ جاده
//    public static final Color ROAD_EDGE  = new Color(40, 40, 40);    // // رنگ حاشیه جاده
//    public static final Color DASH       = new Color(230, 230, 230); // // رنگ خط‌چین وسط
//
//    public static final Color CAR_COLOR       = new Color(0, 102, 204); // // رنگ خودروها
//    public static final Color BUS_COLOR       = new Color(204, 102, 0); // // رنگ اتوبوس
//    public static final Color TRUCK_COLOR     = new Color(102, 51, 0);  // // رنگ کامیون
//    public static final Color BIKE_COLOR      = new Color(0, 153, 0);   // // رنگ دوچرخه
//    public static final Color PEDESTRIAN_COLOR= new Color(255, 255, 255); // // رنگ عابر
//
//    public static final int VEHICLE_LENGTH = 24; // // طول نمایش خودرو (پیکسل)
//    public static final int VEHICLE_WIDTH  = 14; // // عرض نمایش خودرو (پیکسل)
//
//    public static final double MIN_ZOOM = 0.4; // // حداقل زوم
//    public static final double MAX_ZOOM = 2.2; // // حداکثر زوم
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
////package ui; // // پکیج UI
////
////import java.awt.Color; // // رنگ‌ها
////
////public final class UIConstants { // // ثابت‌های واسط کاربری
////    private UIConstants() {} // // جلوگیری از نمونه‌سازی
////
////    public static final int LANE_WIDTH = 20; // // پهنای یک لِین (پیکسل)
////    public static final int LANE_GAP   = 6;  // // فاصله بین لِین‌ها
////
////    public static final Color BACKGROUND = new Color(170, 204, 130); // // سبز فضای شهری
////    public static final Color ROAD_FILL  = new Color(60, 60, 60);    // // خاکستری تیره آسفالت
////    public static final Color ROAD_EDGE  = new Color(40, 40, 40);    // // حاشیهٔ تیره‌تر
////    public static final Color DASH       = new Color(230, 230, 230); // // خط‌چین سفید
////
////    public static final int VEHICLE_LENGTH = 24; // // طول خودرو برای رندر
////    public static final int VEHICLE_WIDTH  = 14; // // عرض خودرو برای رندر
////
////    public static final double MIN_ZOOM = 0.4; // // کمترین زوم
////    public static final double MAX_ZOOM = 2.2; // // بیشترین زوم
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
////package ui; // ثابت‌های ظاهر و ابعاد
////
////import java.awt.Color;
////
////public class UIConstants {
////    // اندازه پنل
////    public static final int PANEL_WIDTH  = 1400;
////    public static final int PANEL_HEIGHT = 900;
////
////    // ابعاد لاین‌ها
////    public static final int LANE_WIDTH = 30;  // پهنای هر لاین
////    public static final int LANE_GAP   = 8;   // فاصله بین لاین‌های هم‌جهت
////
////    // فاصله وسط دو جهت خیابان (برای Double Yellow وسط)
////    public static final int CARRIAGE_CENTER_GAP = 10;
////
////    // ابعاد خودرو برای رندر
////    public static final int VEHICLE_WIDTH  = 24;
////    public static final int VEHICLE_HEIGHT = 14;
////
////    // رنگ‌ها
////    public static final Color ROAD_COLOR           = new Color(58, 58, 58);   // آسفالت
////    public static final Color EDGE_LINE_COLOR      = new Color(235, 235, 235);// خط کناری سفید
////    public static final Color LANE_DASH_COLOR      = new Color(205, 205, 205);// خط‌چین داخلی
////    public static final Color CENTER_LINE_COLOR    = new Color(245, 210, 0);  // خط وسط زرد
////    public static final Color CAR_COLOR            = new Color(66, 140, 240); // رنگ پیشفرض خودرو
////    public static final Color PEDESTRIAN_COLOR     = new Color(245, 245, 245);// عابر
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
////// ui/UIConstants.java
////package ui;
////
////import java.awt.Color;
////
////public class UIConstants {
////    public static final int PANEL_WIDTH  = 1400;
////    public static final int PANEL_HEIGHT = 900;
////
////    // پهنای یک لاین و فاصله بین لاین‌ها
////    public static final int LANE_WIDTH = 30;   // کمی جمع‌وجورتر از قبل
////    public static final int LANE_GAP   = 8;
////
////    // فاصلهٔ بین دو جهت (عرض جداکنندهٔ وسط)
////    public static final int CARRIAGE_CENTER_GAP = 10; // فاصله اضافی بین دو جهت
////
////    // خودرو
////    public static final int VEHICLE_WIDTH  = 20;
////    public static final int VEHICLE_HEIGHT = 12;
////
////    // رنگ‌ها
////    public static final Color ROAD_COLOR          = new Color(58, 58, 58);   // آسفالت
////    public static final Color EDGE_LINE_COLOR     = new Color(235, 235, 235);// خط کناری سفید
////    public static final Color LANE_DASH_COLOR     = new Color(205, 205, 205);// خط‌چین بین لاین‌ها
////    public static final Color CENTER_LINE_COLOR   = new Color(245, 210, 0);  // زرد وسط (دو خط)
////    public static final Color CAR_COLOR           = new Color(66, 140, 240);
////    public static final Color PEDESTRIAN_COLOR    = new Color(245,245,245);
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
////package ui;
////
////import java.awt.Color;
////
////public class UIConstants {
////    public static final int PANEL_WIDTH  = 1400;
////    public static final int PANEL_HEIGHT = 900;
////
////    // پهنای یک لاین (پیکسل) — از 20 به 36 افزایش یافت
////    public static final int LANE_WIDTH = 36;
////
////    // فاصلهٔ بین لاین‌های هم‌جهت (گپ باریک بین دو لاین موازی)
////    public static final int LANE_GAP = 8;
////
////    // ابعاد خودرو را کمی جمع‌وجورتر می‌گیریم تا داخل لاین جا شود
////    public static final int VEHICLE_WIDTH  = 22;
////    public static final int VEHICLE_HEIGHT = 12;
////
////    public static final Color ROAD_COLOR       = new Color(60, 60, 60);
////    public static final Color LANE_LINE_COLOR  = new Color(200, 200, 200);
////    public static final Color CENTER_LINE_COLOR= new Color(240, 210, 0); // خط میانی زرد
////    public static final Color CAR_COLOR        = Color.BLUE;
////    public static final Color PEDESTRIAN_COLOR = new Color(245,245,245);
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
////
////package ui; // // پکیج UI
////
////import java.awt.Color; // // برای رنگ‌ها
////
////public class UIConstants { // // ثابت‌های گرافیکی و ابعادی
////    public static final int PANEL_WIDTH  = 1400; // // عرض پنل اصلی
////    public static final int PANEL_HEIGHT = 900;  // // ارتفاع پنل اصلی
////
////    public static final int LANE_WIDTH = 20;          // // ضخامت رسم راه
////    public static final int VEHICLE_WIDTH  = 26;      // // عرض رسم خودرو
////    public static final int VEHICLE_HEIGHT = 14;      // // ارتفاع رسم خودرو
////
////    public static final Color ROAD_COLOR      = new Color(60, 60, 60);     // // رنگ بدنه راه (خاکستری تیره)
////    public static final Color LANE_LINE_COLOR = new Color(170, 170, 170);  // // رنگ خط میانی راه
////    public static final Color CAR_COLOR       = Color.BLUE;                // // رنگ بکاپ خودرو (وقتی PNG نداریم)
////    public static final Color PEDESTRIAN_COLOR = new Color(245,245,245);   // // رنگ بکاپ عابر (دایره سفید)
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
////
////package ui;
////
////import java.awt.Color;
////
////public class UIConstants {
////    public static final int PANEL_WIDTH = 1000;
////    public static final int PANEL_HEIGHT = 700;
////
////    public static final int LANE_WIDTH = 20;
////    public static final Color ROAD_COLOR = new Color(60, 60, 60);
////    public static final Color LANE_LINE_COLOR = new Color(200, 200, 200);
////
////    public static final Color CAR_COLOR = Color.BLUE;
////    public static final Color PEDESTRIAN_COLOR = Color.DARK_GRAY;
////    public static final int VEHICLE_WIDTH = 20;
////    public static final int VEHICLE_HEIGHT = 12;
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
////package ui;
////
////import java.awt.Color;
////
////public class UIConstants {
////    public static final int LANE_WIDTH = 20;
////    public static final int VEHICLE_SIZE = 10;
////
////    public static final Color BG_COLOR = new Color(235, 239, 245);
////    public static final Color ROAD_COLOR = new Color(120, 120, 120);
////    public static final Color LANE_MARK_COLOR = new Color(220, 220, 220);
////    public static final Color INTERSECTION_COLOR = new Color(90, 90, 90);
////    public static final Color CAR_COLOR = new Color(60, 120, 255);
////}
