// trafficcontrol/TrafficSign.java
package trafficcontrol; // // پکیج

import core.Direction;  // // جهت

/**
 * پایهٔ علائم (STOP/YIELD/...) — update() پیش‌فرض پیاده‌سازی شده
 * تا ارور «must override abstract method update()» رخ نده.
 */
public abstract class TrafficSign implements TrafficControlDevice { // // پایه علامت
    protected final String id;                     // // شناسه یکتا
    protected final Direction directionControlled; // // جهت کنترل‌شده

    public TrafficSign(String id, Direction directionControlled) { // // سازنده
        if (id == null || directionControlled == null)             // // چک ورودی
            throw new IllegalArgumentException("TrafficSign params cannot be null"); // // پیام
        this.id = id;                          // // ست ID
        this.directionControlled = directionControlled; // // ست جهت
    }

    @Override public String getId() { return id; }                    // // گتر ID
    @Override public Direction getDirectionControlled() { return directionControlled; } // // گتر جهت

    @Override
    public void update() { /* // // علائم ثابت رفتارِ داینامیک ندارند */ } // // نَو-اُپ
}






























////package trafficcontrol; // // پکیج کنترل ترافیک
////
////import simulation.Updatable; // // اینترفیس آپدیت بدون پارامتر
////
/////**
//// * پایهٔ تمام علائم/کنترلرهای ترافیکی //
//// */
////public abstract class TrafficSign implements Updatable { // // کلاس پایه علائم
////    private boolean enabled = true; // // فعال/غیرفعال بودن علامت
////
////    public final boolean isEnabled() { // // خواندن وضعیت
////        return enabled; // // خروجی
////    }
////
////    public final void setEnabled(boolean en) { // // تغییر وضعیت
////        this.enabled = en; // // اعمال
////    }
////
////    @Override
////    public final void update() { // // امضای درست مطابق Updatable (بدون ورودی)
////        if (!enabled) return; // // اگر غیرفعال، هیچ
////        onUpdate(); // // اجرای منطق کلاس مشتق
////    }
////
////    protected abstract void onUpdate(); // // قلاب مخصوص کلاس‌های مشتق (بدون ورودی)
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
//////package trafficcontrol; // // پکیج کنترل ترافیک
//////
//////import simulation.Updatable; // //‌ اینترفیس آپدیت بدون پارامتر
//////
///////**
////// * پایهٔ تمام علائم/کنترلرهای ترافیکی
////// * بدون لامبدا و با توضیح خطی //
////// */
//////public abstract class TrafficSign implements Updatable { // // کلاس پایه علائم
//////    private boolean enabled = true; // // فعال/غیرفعال بودن علامت
//////
//////    public final boolean isEnabled() { // // خواندن وضعیت
//////        return enabled; // // خروجی
//////    }
//////
//////    public final void setEnabled(boolean en) { // // تغییر وضعیت
//////        this.enabled = en; // // اعمال
//////    }
//////
//////    @Override
//////    public final void update() { // // امضای درست مطابق Updatable
//////        if (!enabled) return; // // اگر غیرفعال، هیچ کاری نکن
//////        onUpdate(); // // قلاب مخصوص کلاس‌های مشتق
//////    }
//////
//////    /**
//////     * قلابی که کلاس‌های مشتق باید پیاده‌سازی کنند. //
//////     * توجه: بدون پارامتر است تا با Updatable پروژه سازگار بماند. //
//////     */
//////    protected abstract void onUpdate(); // // قلاب
//////}
//////
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
//////package trafficcontrol;
//////
//////import core.*;
//////
//////public abstract class TrafficSign implements Identifiable {
//////    protected String id;
//////    protected Direction directionControlled;
//////
//////    public TrafficSign(String id, Direction directionControlled) {
//////        this.id = id;
//////        this.directionControlled = directionControlled;
//////    }
//////
//////    @Override
//////    public String getId() {
//////        return id;
//////    }
//////
//////    public Direction getDirectionControlled() {
//////        return directionControlled;
//////    }
//////}
//////
//////
////
////
////
////
//////package trafficcontrol;
//////
//////import core.Direction;
//////import core.Identifiable;
////////
/////////**
//////// * کلاس انتزاعی برای علائم راهنمایی (مثل STOP, YIELD)
//////// * پایه‌ای برای سایر علائم ثابت
//////// */
////////public abstract class TrafficSign implements TrafficControlDevice {
////////    protected final String id;                  // شناسه یکتا
////////    protected final Direction directionControlled; // جهتی که علامت کنترل می‌کند
////////
////////    // --- سازنده ---
////////    public TrafficSign(String id, Direction directionControlled) {
////////        if (id == null || directionControlled == null) {
////////            throw new IllegalArgumentException("TrafficSign parameters cannot be null");
////////        }
////////        this.id = id;
////////        this.directionControlled = directionControlled;
////////    }
////////
////////    // --- از Identifiable ---
////////    @Override
////////    public String getId() {
////////        return id;
////////    }
////////
////////    // --- از TrafficControlDevice ---
////////    @Override
////////    public Direction getDirectionControlled() {
////////        return directionControlled;
////////    }
////////
////////    @Override
////////    public void update() {
////////        // علائم ثابت (STOP, YIELD) نیاز به آپدیت ندارند
////////    }
////////}
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
//////package trafficcontrol; // // پکیج کنترل ترافیک
//////
//////import core.Direction; // // جهت کنترل‌شده
//////import core.Identifiable; // // اینترفیس شناسه‌دار
//////
//////public abstract class TrafficSign implements Identifiable { // // کلاس انتزاعی علائم راهنمایی
//////    protected String id; // // شناسه یکتا برای علامت
//////    protected Direction directionControlled; // // جهتی که این علامت/چراغ کنترل می‌کند
//////
//////    protected TrafficSign() { // // سازندهٔ بدون پارامتر برای سازگاری با زیرکلاس‌ها
//////        this.id = null; // // مقدار اولیه خنثی برای id
//////        this.directionControlled = null; // // مقدار اولیه خنثی برای جهت
//////    }
//////
//////    protected TrafficSign(String id, Direction direction) { // // سازندهٔ کامل با پارامتر
//////        this.id = id; // // مقداردهی شناسه
//////        this.directionControlled = direction; // // مقداردهی جهت کنترل‌شده
//////    }
//////
//////    @Override
//////    public String getId() { // // برگرداندن شناسه
//////        return this.id; // // مقدار id
//////    }
//////
//////    public Direction getDirectionControlled() { // // گرفتن جهت کنترل‌شده
//////        return this.directionControlled; // // مقدار جهت
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
////////package trafficcontrol; // // پکیج کنترل ترافیک
////////
////////import core.Direction; // // جهت کنترل‌شده
////////import core.Identifiable; // // اینترفیس شناسه‌دار
////////
////////public abstract class TrafficSign implements Identifiable { // // کلاس انتزاعی علائم راهنمایی
////////    protected String id; // // شناسه یکتا برای علامت
////////    protected Direction directionControlled; // // جهتی که این علامت/چراغ کنترل می‌کند
////////
////////    protected TrafficSign() { // // سازندهٔ بدون پارامتر برای سازگاری با زیرکلاس‌ها
////////        this.id = null; // // مقدار اولیه خنثی برای id
////////        this.directionControlled = null; // // مقدار اولیه خنثی برای جهت
////////    }
////////
////////    protected TrafficSign(String id, Direction direction) { // // سازندهٔ کامل با پارامتر
////////        this.id = id; // // مقداردهی شناسه
////////        this.directionControlled = direction; // // مقداردهی جهت کنترل‌شده
////////    }
////////
////////    @Override
////////    public String getId() { // // برگرداندن شناسه
////////        return this.id; // // مقدار id
////////    }
////////
////////    public Direction getDirectionControlled() { // // گرفتن جهت کنترل‌شده
////////        return this.directionControlled; // // مقدار جهت
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
//////////package trafficcontrol;
//////////
//////////import core.*;
//////////
//////////public abstract class TrafficSign implements Identifiable {
//////////    protected String id;
//////////    protected Direction directionControlled;
//////////
//////////    public TrafficSign(String id, Direction directionControlled) {
//////////        this.id = id;
//////////        this.directionControlled = directionControlled;
//////////    }
//////////
//////////    @Override
//////////    public String getId() {
//////////        return id;
//////////    }
//////////
//////////    public Direction getDirectionControlled() {
//////////        return directionControlled;
//////////    }
//////////}
