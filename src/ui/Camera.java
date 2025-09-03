package ui; // // پکیج UI

import core.Point; // // نقطهٔ جهان

public class Camera { // // دوربین/نمایش
    private int offsetX = 0; // // جابه‌جایی افقی
    private int offsetY = 0; // // جابه‌جایی عمودی
    private double scale = 1.0; // // زوم

    public void pan(int dx, int dy) { // // حرکت دادن دوربین
        offsetX += dx; // // به‌روز کردن X
        offsetY += dy; // // به‌روز کردن Y
    }

    public void setScale(double s) { // // تنظیم زوم
        if (s < UIConstants.MIN_ZOOM) s = UIConstants.MIN_ZOOM; // // حداقل
        if (s > UIConstants.MAX_ZOOM) s = UIConstants.MAX_ZOOM; // // حداکثر
        scale = s; // // ذخیره
    }

    public double getScale() { // // گرفتن زوم
        return scale; // // خروجی
    }

    public int getOffsetX() { return offsetX; } // // گتر X
    public int getOffsetY() { return offsetY; } // // گتر Y

    public java.awt.Point worldToScreen(Point p) { // // تبدیل مختصات جهان به صفحه
        int sx = (int)Math.round((p.getX() + offsetX) * scale); // // X صفحه
        int sy = (int)Math.round((p.getY() + offsetY) * scale); // // Y صفحه
        return new java.awt.Point(sx, sy); // // نقطهٔ صفحه
    }
}



























//package ui; // // پکیج UI
//
//import core.Point; // // نقطهٔ جهان
//
//public class Camera { // // دوربین/نمایش
//    private int offsetX = 0; // // جابه‌جایی افقی
//    private int offsetY = 0; // // جابه‌جایی عمودی
//    private double scale = 1.0; // // زوم
//
//    public void pan(int dx, int dy) { // // حرکت دادن دوربین
//        offsetX += dx; // // به‌روز کردن X
//        offsetY += dy; // // به‌روز کردن Y
//    }
//
//    public void setScale(double s) { // // تنظیم زوم
//        if (s < UIConstants.MIN_ZOOM) s = UIConstants.MIN_ZOOM; // // حداقل
//        if (s > UIConstants.MAX_ZOOM) s = UIConstants.MAX_ZOOM; // // حداکثر
//        scale = s; // // ذخیره
//    }
//
//    public double getScale() { // // گرفتن زوم
//        return scale; // // خروجی
//    }
//
//    public int getOffsetX() { return offsetX; } // // گتر X
//    public int getOffsetY() { return offsetY; } // // گتر Y
//
//    public java.awt.Point worldToScreen(Point p) { // // تبدیل مختصات جهان به صفحه
//        int sx = (int)Math.round((p.getX() + offsetX) * scale); // // X صفحه
//        int sy = (int)Math.round((p.getY() + offsetY) * scale); // // Y صفحه
//        return new java.awt.Point(sx, sy); // // نقطهٔ صفحه
//    }
//}

































//package ui; // // پکیج ui
//
//import core.Point; // // استفاده از Point پروژه
//
//public class Camera { // // کلاس دوربین
//    private double scale = 1.0; // // مقدار زوم (۱ یعنی ۱:۱)
//    private int offsetX = 0; // // جابه‌جایی افقی نقشه روی صفحه
//    private int offsetY = 0; // // جابه‌جایی عمودی نقشه روی صفحه
//
//    public Point transform(Point world) { // // تبدیل نقطه «دنیا» به «صفحه»
//        int sx = (int) Math.round(world.getX() * scale) + offsetX; // // اعمال زوم و آفست X
//        int sy = (int) Math.round(world.getY() * scale) + offsetY; // // اعمال زوم و آفست Y
//        return new Point(sx, sy); // // بازگشت نقطه صفحه
//    }
//
//    public Point inverseTransform(Point screen) { // // تبدیل نقطه «صفحه» به «دنیا»
//        int wx = (int) Math.round((screen.getX() - offsetX) / scale); // // محاسبه X دنیا
//        int wy = (int) Math.round((screen.getY() - offsetY) / scale); // // محاسبه Y دنیا
//        return new Point(wx, wy); // // بازگشت نقطه دنیا
//    }
//
//    public void zoomAt(double factor, int anchorX, int anchorY) { // // زوم حول یک نقطه صفحه
//        double old = scale; // // زوم قبلی
//        scale = Math.max(0.25, Math.min(4.0, scale * factor)); // // محدودسازی زوم بین ۰.۲۵ تا ۴
//        double r = scale / old; // // نسبت تغییر زوم
//        offsetX = (int) Math.round(anchorX - (anchorX - offsetX) * r); // // اصلاح آفست X برای ثابت ماندن نقطه لنگر
//        offsetY = (int) Math.round(anchorY - (anchorY - offsetY) * r); // // اصلاح آفست Y
//    }
//
//    public void zoomIn(int centerX, int centerY) { zoomAt(1.25, centerX, centerY); } // // زوم به داخل
//    public void zoomOut(int centerX, int centerY) { zoomAt(0.8, centerX, centerY); } // // زوم به بیرون
//
//    public void panBy(int dx, int dy) { // // جابه‌جایی نقشه
//        offsetX += dx; // // تغییر X
//        offsetY += dy; // // تغییر Y
//    }
//
//    public double getScale() { return scale; } // // گرفتن زوم
//    public int getOffsetX() { return offsetX; } // // گرفتن آفست X
//    public int getOffsetY() { return offsetY; } // // گرفتن آفست Y
//}
//




































//package ui; // // پکیج UI
//
//import core.Point; // // مختصات
//import core.Vehicle; // // خودرو
//
//public class Camera { // // دوربین ساده
//    private int offsetX; // // شیفت افقی
//    private int offsetY; // // شیفت عمودی
//    private Vehicle followed; // // خودروی در حال دنبال شدن
//
//    public void follow(Vehicle v) { // // دنبال‌کردن خودرو
//        this.followed = v; // // تنظیم خودرو
//    }
//
//    public void centerOn(Point p) { // // مرکز کردن روی نقطه
//        offsetX = p.getX() - UIConstants.PANEL_WIDTH / 2; // // محاسبه شیفت X
//        offsetY = p.getY() - UIConstants.PANEL_HEIGHT / 2; // // محاسبه شیفت Y
//    }
//
//    public Point transform(Point worldPoint) { // // تبدیل مختصات جهان به صفحه
//        return new Point(worldPoint.getX() - offsetX, worldPoint.getY() - offsetY); // // برگرداندن نقطه جدید
//    }
//
//    public void updateAutoFollow() { // // به‌روزرسانی خودکار دنبال‌کردن
//        if (followed == null) return; // // اگر خودرویی انتخاب نشده، خروج
//        if (followed.getCurrentLane() == null) return; // // اگر لِین ندارد، خروج
//        Point wp = followed.getCurrentLane().getPositionAt(followed.getPositionInLane()); // // نقطه جهانی خودرو
//        centerOn(wp); // // مرکز کردن دوربین
//    }
//}
//













//package ui;
//
//import core.Point;
//import core.Vehicle;
//
//public class Camera {
//    private int offsetX, offsetY;
//    private Vehicle followed;
//
//    public void follow(Vehicle v) {
//        this.followed = v;
//    }
//
//    public void toggleFollow() {
//        this.followed = (this.followed == null) ? this.followed : null;
//    }
//
//    public void centerOn(Point p) {
//        // با فرض اینکه می‌خواهیم نقطه وسط پنل باشد
//        offsetX = p.getX() - UIConstants.PANEL_WIDTH / 2;
//        offsetY = p.getY() - UIConstants.PANEL_HEIGHT / 2;
//    }
//
//    public Point transform(Point worldPoint) {
//        return new Point(worldPoint.getX() - offsetX, worldPoint.getY() - offsetY);
//    }
//
//    public void updateAutoFollow() {
//        if (followed == null) return;
//        if (followed.getCurrentLane() == null) return;
//        var wp = followed.getCurrentLane().getPositionAt(followed.getPositionInLane());
//        centerOn(wp);
//    }
//}


















//package ui;
//
//import java.awt.Point;
//
//public class Camera {
//    private int offsetX = 0, offsetY = 0;
//
//    public void pan(int dx, int dy) {
//        offsetX += dx;
//        offsetY += dy;
//    }
//
//    // تبدیل مختصات دنیای بازی به مختصات صفحه
//    public Point transform(int worldX, int worldY) {
//        return new Point(worldX - offsetX, worldY - offsetY);
//    }
//
//    // شورتکات‌ها
//    public int tx(int worldX) { return worldX - offsetX; }
//    public int ty(int worldY) { return worldY - offsetY; }
//}
