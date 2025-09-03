


package core; // // پکیج core

import infrastructure.Lane; // // برای استفاده از Lane
import java.util.ArrayList; // // لیست پویا
import java.util.List; // // اینترفیس لیست

public class Route { // // مسیر: لیستی از لِین‌ها به ترتیب
    private Lane startLane; // // لِین شروع
    private Lane endLane;   // // لِین پایان
    private List<Lane> lanesInOrder; // // لِین‌ها به ترتیب عبور

    public Route(Lane startLane, Lane endLane, List<Lane> lanes) { // // سازنده مسیر
        this.startLane = startLane; // // ست لِین شروع
        this.endLane = endLane;     // // ست لِین پایان
        // اگر null بود، لیست خالی بگذار //
        this.lanesInOrder = (lanes == null) ? new ArrayList<Lane>() : new ArrayList<Lane>(lanes); // // کپی از ورودی
    }

    public Lane getStartLane() { // // گتر لِین شروع
        return startLane; // // خروجی
    }

    public Lane getEndLane() { // // گتر لِین پایان
        return endLane; // // خروجی
    }

    public List<Lane> getLanes() { // // گرفتن کل لیست لِین‌ها
        return new ArrayList<Lane>(lanesInOrder); // // برگرداندن کپی برای ایمنی
    }

    public Lane getNextLane(Lane current) { // // گرفتن لِین بعدی نسبت به لِین فعلی
        for (int i = 0; i < lanesInOrder.size() - 1; i++) { // // پیمایش تا یکی مانده به آخر
            if (lanesInOrder.get(i) == current) { // // اگر لِین پیدا شد
                return lanesInOrder.get(i + 1); // // لِین بعدی را بده
            }
        }
        return null; // // اگر آخر مسیر بودیم یا پیدا نشد
    }

    public boolean isFinished(Lane current) { // // آیا به آخر مسیر رسیدیم؟
        if (lanesInOrder.isEmpty()) return true; // // اگر مسیر تهیست، تمام
        return current == lanesInOrder.get(lanesInOrder.size() - 1); // // آخرین لِین؟
    }

    public int size() { // // تعداد لِین‌های مسیر
        return lanesInOrder.size(); // // خروجی
    }
}


































//package core; // // پکیج هسته
//
//import infrastructure.Lane; // // لِین
//import java.util.ArrayList; // // لیست
//import java.util.List; // // اینترفیس
//
//public class Route { // // مسیر حرکت
//    private final ArrayList<Lane> lanes; // // توالی لِین‌ها به ترتیب
//
//    public Route(ArrayList<Lane> lanes) { // // سازنده با لیست
//        this.lanes = (lanes != null) ? lanes : new ArrayList<Lane>(); // // نگهداری امن
//    }
//
//    public List<Lane> getLanes() { // // گتر لیست
//        return this.lanes; // // خروجی
//    }
//
//    public boolean isEmpty() { // // خالی بودن مسیر
//        return this.lanes.isEmpty(); // // چک
//    }
//
//    public Lane getFirstLane() { // // اولین لِین مسیر
//        if (this.lanes.isEmpty()) { return null; } // // اگر خالی
//        return this.lanes.get(0); // // بازگرداندن
//    }
//
//    public Lane getNextLane(Lane current) { // // لِین بعدی نسبت به لِین فعلی
//        if (current == null) { // // اگر ورودی تهی
//            return getFirstLane(); // // اولین را بده
//        }
//        int idx = indexOf(current); // // ایندکس لِین فعلی
//        if (idx == -1) { // // اگر در لیست نبود
//            return getFirstLane(); // // از اول شروع کن
//        }
//        if (idx + 1 < this.lanes.size()) { // // اگر بعدی وجود دارد
//            return this.lanes.get(idx + 1); // // لِین بعدی
//        }
//        return null; // // به انتهای مسیر رسیدیم
//    }
//
//    private int indexOf(Lane ln) { // // کمک: یافتن ایندکس لِین
//        for (int i = 0; i < this.lanes.size(); i++) { // // پیمایش
//            if (this.lanes.get(i) == ln) { return i; } // // تطابق ارجاعی
//        }
//        return -1; // // نبود
//    }
//}




































//
//package core; // // پکیج core
//
//import infrastructure.Lane; // // برای استفاده از Lane
//import java.util.ArrayList; // // لیست پویا
//import java.util.List; // // اینترفیس لیست
//
//public class Route { // // مسیر: لیستی از لِین‌ها به ترتیب
//    private Lane startLane; // // لِین شروع
//    private Lane endLane;   // // لِین پایان
//    private List<Lane> lanesInOrder; // // لِین‌ها به ترتیب عبور
//
//    public Route(Lane startLane, Lane endLane, List<Lane> lanes) { // // سازنده مسیر
//        this.startLane = startLane; // // ست لِین شروع
//        this.endLane = endLane;     // // ست لِین پایان
//        // اگر null بود، لیست خالی بگذار //
//        this.lanesInOrder = (lanes == null) ? new ArrayList<Lane>() : new ArrayList<Lane>(lanes); // // کپی از ورودی
//    }
//
//    public Lane getStartLane() { // // گتر لِین شروع
//        return startLane; // // خروجی
//    }
//
//    public Lane getEndLane() { // // گتر لِین پایان
//        return endLane; // // خروجی
//    }
//
//    public List<Lane> getLanes() { // // گرفتن کل لیست لِین‌ها
//        return new ArrayList<Lane>(lanesInOrder); // // برگرداندن کپی برای ایمنی
//    }
//
//    public Lane getNextLane(Lane current) { // // گرفتن لِین بعدی نسبت به لِین فعلی
//        for (int i = 0; i < lanesInOrder.size() - 1; i++) { // // پیمایش تا یکی مانده به آخر
//            if (lanesInOrder.get(i) == current) { // // اگر لِین پیدا شد
//                return lanesInOrder.get(i + 1); // // لِین بعدی را بده
//            }
//        }
//        return null; // // اگر آخر مسیر بودیم یا پیدا نشد
//    }
//
//    public boolean isFinished(Lane current) { // // آیا به آخر مسیر رسیدیم؟
//        if (lanesInOrder.isEmpty()) return true; // // اگر مسیر تهیست، تمام
//        return current == lanesInOrder.get(lanesInOrder.size() - 1); // // آخرین لِین؟
//    }
//
//    public int size() { // // تعداد لِین‌های مسیر
//        return lanesInOrder.size(); // // خروجی
//    }
//}





























//package core;
//
//
//import infrastructure.Lane;
//import java.util.List;
//
//public class Route {
//    private Lane startLane;
//    private Lane endLane;
//    private List<Lane> lanesInOrder;
//
//    // سازنده
//    public Route(Lane startLane, Lane endLane, List<Lane> lanesInOrder) {
//        this.startLane = startLane;
//        this.endLane = endLane;
//        this.lanesInOrder = lanesInOrder;
//    }
//
//    // گرفتن مسیر بعدی در لیست بر اساس current
//    public Lane getNextLane(Lane current) {
//        int index = lanesInOrder.indexOf(current);
//        if (index != -1 && index + 1 < lanesInOrder.size()) {
//            return lanesInOrder.get(index + 1);
//        }
//        return null; // مسیر به پایان رسیده یا current یافت نشد
//    }
//
//    // بررسی اینکه مسیر تمام شده یا نه
//    public boolean isFinished(Lane current) {
//        return current != null && current.equals(endLane);
//    }
//
//    // گرفتن لیست کل مسیر
//    public List<Lane> getLanesInOrder() {
//        return lanesInOrder;
//    }
//
//    public Lane getStartLane() {
//        return startLane;
//    }
//
//    public Lane getEndLane() {
//        return endLane;
//    }
//}
