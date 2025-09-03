package core;

public class Point {
    private int x, y;

    // سازنده برای تعیین مختصات اولیه
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // دریافت مختصات x
    public int getX() {
        return x;
    }

    // دریافت مختصات y
    public int getY() {
        return y;
    }

    // نمایش رشته‌ای برای چاپ نقطه به صورت (x, y)
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    // بررسی برابری دو نقطه بر اساس مختصات
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Point)) return false;
        Point other = (Point) obj;
        return this.x == other.x && this.y == other.y;
    }

    // تولید کد هش برای استفاده در کالکشن‌ها
    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}








//package core;
//
//public class Point {
//    private final int x, y;
//    public Point(int x, int y) { this.x = x; this.y = y; }
//    public int getX(){ return x; }
//    public int getY(){ return y; }
//}
//





























//package core;
//
//public class Point {
//    private int x, y;
//
//    // سازنده برای تعیین مختصات اولیه


//    public Point(int x, int y) {
//        this.x = x;
//        this.y = y;
//    }
//
//    // دریافت مختصات x
//    public int getX() {
//        return x;
//    }
//
//    // دریافت مختصات y
//    public int getY() {
//        return y;
//    }
//
//    // نمایش رشته‌ای برای چاپ نقطه به صورت (x, y)
//    @Override
//    public String toString() {
//        return "(" + x + ", " + y + ")";
//    }
//
//    // بررسی برابری دو نقطه بر اساس مختصات
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (!(obj instanceof Point)) return false;
//        Point other = (Point) obj;
//        return this.x == other.x && this.y == other.y;
//    }
//
//    // تولید کد هش برای استفاده در کالکشن‌ها
//    @Override
//    public int hashCode() {
//        return 31 * x + y;
//    }
//}
