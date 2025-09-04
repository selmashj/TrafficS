package ui;                                   // پکیج UI

public class Camera {                         // کلاس دوربین
    private double scale = 1.0;               // مقیاس زوم
    private int offsetX = 0;                  // آفست X (پن)
    private int offsetY = 0;                  // آفست Y (پن)

    public double getScale(){                 // گتر زوم
        return scale;                         // بازگرداندن مقدار
    }

    public void setScale(double s){           // ستر زوم با محدودسازی
        if (s < 0.1) s = 0.1;                 // حداقل زوم
        if (s > 4.0) s = 4.0;                 // حداکثر زوم
        this.scale = s;                       // اعمال مقدار
    }

    public int getOffsetX(){                  // گتر آفست X
        return offsetX;                       // خروجی
    }

    public int getOffsetY(){                  // گتر آفست Y
        return offsetY;                       // خروجی
    }

    public void pan(int dx, int dy){          // جابه‌جایی نسبی دوربین
        this.offsetX += dx;                   // افزایش X
        this.offsetY += dy;                   // افزایش Y
    }

    public void setOffset(int x, int y){      // ✅ تنظیم مطلق آفست (برای فیت‌کردن نقشه)
        this.offsetX = x;                     // ست مستقیم X
        this.offsetY = y;                     // ست مستقیم Y
    }
}




