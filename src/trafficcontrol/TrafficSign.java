
//package trafficcontrol;
//
//import core.*;
//
//public abstract class TrafficSign implements Identifiable {
//    protected String id;
//    protected Direction directionControlled;
//
//    public TrafficSign(String id, Direction directionControlled) {
//        this.id = id;
//        this.directionControlled = directionControlled;
//    }
//
//    @Override
//    public String getId() {
//        return id;
//    }
//
//    public Direction getDirectionControlled() {
//        return directionControlled;
//    }
//}
//
//




package trafficcontrol;

import core.Direction;
import core.Identifiable;

/**
 * کلاس انتزاعی برای علائم راهنمایی (مثل STOP, YIELD)
 * پایه‌ای برای سایر علائم ثابت
 */
public abstract class TrafficSign implements TrafficControlDevice {
    protected final String id;                  // شناسه یکتا
    protected final Direction directionControlled; // جهتی که علامت کنترل می‌کند

    // --- سازنده ---
    public TrafficSign(String id, Direction directionControlled) {
        if (id == null || directionControlled == null) {
            throw new IllegalArgumentException("TrafficSign parameters cannot be null");
        }
        this.id = id;
        this.directionControlled = directionControlled;
    }

    // --- از Identifiable ---
    @Override
    public String getId() {
        return id;
    }

    // --- از TrafficControlDevice ---
    @Override
    public Direction getDirectionControlled() {
        return directionControlled;
    }

    @Override
    public void update() {
        // علائم ثابت (STOP, YIELD) نیاز به آپدیت ندارند
    }
}





























//package trafficcontrol; // // پکیج کنترل ترافیک
//
//import core.Direction; // // جهت کنترل‌شده
//import core.Identifiable; // // اینترفیس شناسه‌دار
//
//public abstract class TrafficSign implements Identifiable { // // کلاس انتزاعی علائم راهنمایی
//    protected String id; // // شناسه یکتا برای علامت
//    protected Direction directionControlled; // // جهتی که این علامت/چراغ کنترل می‌کند
//
//    protected TrafficSign() { // // سازندهٔ بدون پارامتر برای سازگاری با زیرکلاس‌ها
//        this.id = null; // // مقدار اولیه خنثی برای id
//        this.directionControlled = null; // // مقدار اولیه خنثی برای جهت
//    }
//
//    protected TrafficSign(String id, Direction direction) { // // سازندهٔ کامل با پارامتر
//        this.id = id; // // مقداردهی شناسه
//        this.directionControlled = direction; // // مقداردهی جهت کنترل‌شده
//    }
//
//    @Override
//    public String getId() { // // برگرداندن شناسه
//        return this.id; // // مقدار id
//    }
//
//    public Direction getDirectionControlled() { // // گرفتن جهت کنترل‌شده
//        return this.directionControlled; // // مقدار جهت
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
////package trafficcontrol; // // پکیج کنترل ترافیک
////
////import core.Direction; // // جهت کنترل‌شده
////import core.Identifiable; // // اینترفیس شناسه‌دار
////
////public abstract class TrafficSign implements Identifiable { // // کلاس انتزاعی علائم راهنمایی
////    protected String id; // // شناسه یکتا برای علامت
////    protected Direction directionControlled; // // جهتی که این علامت/چراغ کنترل می‌کند
////
////    protected TrafficSign() { // // سازندهٔ بدون پارامتر برای سازگاری با زیرکلاس‌ها
////        this.id = null; // // مقدار اولیه خنثی برای id
////        this.directionControlled = null; // // مقدار اولیه خنثی برای جهت
////    }
////
////    protected TrafficSign(String id, Direction direction) { // // سازندهٔ کامل با پارامتر
////        this.id = id; // // مقداردهی شناسه
////        this.directionControlled = direction; // // مقداردهی جهت کنترل‌شده
////    }
////
////    @Override
////    public String getId() { // // برگرداندن شناسه
////        return this.id; // // مقدار id
////    }
////
////    public Direction getDirectionControlled() { // // گرفتن جهت کنترل‌شده
////        return this.directionControlled; // // مقدار جهت
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
