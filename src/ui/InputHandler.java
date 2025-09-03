package ui; // // پکیج UI

import java.awt.event.KeyListener; // // لیسنر کیبورد
import java.awt.event.KeyEvent; // // رویداد کلید
import javax.swing.JComponent; // // برای تمرکز

public class InputHandler implements KeyListener { // // ورودی کیبورد
    private final UIController controller; // // کنترلر
    private final JComponent focusOwner; // // کامپوننت هدف

    public InputHandler(UIController c, JComponent owner) { // // سازنده
        this.controller = c; // // ذخیره کنترلر
        this.focusOwner = owner; // // ذخیره کامپوننت
        owner.setFocusable(true); // // قابل فوکوس
        owner.requestFocusInWindow(); // // درخواست فوکوس
    }

    @Override public void keyTyped(KeyEvent e) {} // // استفاده نمی‌کنیم

    @Override
    public void keyPressed(KeyEvent e) { // // فشردن کلید
        int code = e.getKeyCode(); // // کد کلید
        if (code == KeyEvent.VK_PLUS || code == KeyEvent.VK_EQUALS) controller.zoomIn(); // // +
        if (code == KeyEvent.VK_MINUS) controller.zoomOut(); // // -
        if (code == KeyEvent.VK_LEFT)  controller.pan(20, 0); // // ←
        if (code == KeyEvent.VK_RIGHT) controller.pan(-20, 0); // // →
        if (code == KeyEvent.VK_UP)    controller.pan(0, 20); // // ↑
        if (code == KeyEvent.VK_DOWN)  controller.pan(0, -20); // // ↓
    }

    @Override public void keyReleased(KeyEvent e) {} // // استفاده نمی‌کنیم
}



































//
//package ui;
//
//import core.*;
//import simulation.*;
//import java.awt.event.*;
//
///**
// * برای جلوگیری از خطای cast، علاوه بر KeyListener، MouseListener/Motion/Wheel را هم پیاده‌سازی می‌کنیم.
// * می‌تونی فقط متدهای لازم رو در آینده پر کنی.
// */
//public class InputHandler implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
//
//    private final UIController controller;
//    private final SimulatorPanel panel;
//
//    public InputHandler(UIController controller, SimulatorPanel panel) {
//        this.controller = controller;
//        this.panel = panel;
//    }
//
//    // ===== Key =====
//    @Override public void keyTyped(KeyEvent e) {}
//    @Override public void keyPressed(KeyEvent e) {}
//    @Override public void keyReleased(KeyEvent e) {}
//
//    // ===== Mouse =====
//    @Override public void mouseClicked(MouseEvent e) {}
//    @Override public void mousePressed(MouseEvent e) {}
//    @Override public void mouseReleased(MouseEvent e) {}
//    @Override public void mouseEntered(MouseEvent e) {}
//    @Override public void mouseExited(MouseEvent e) {}
//
//    // ===== Mouse Motion =====
//    @Override public void mouseDragged(MouseEvent e) {}
//    @Override public void mouseMoved(MouseEvent e) {}
//
//    // ===== Mouse Wheel =====
//    @Override public void mouseWheelMoved(MouseWheelEvent e) {}
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
////import java.awt.event.KeyListener; // // لیسنر کیبورد
////import java.awt.event.KeyEvent; // // رویداد کلید
////import javax.swing.JComponent; // // برای تمرکز
////
////public class InputHandler implements KeyListener { // // ورودی کیبورد
////    private final UIController controller; // // کنترلر
////    private final JComponent focusOwner; // // کامپوننت هدف
////
////    public InputHandler(UIController c, JComponent owner) { // // سازنده
////        this.controller = c; // // ذخیره کنترلر
////        this.focusOwner = owner; // // ذخیره کامپوننت
////        owner.setFocusable(true); // // قابل فوکوس
////        owner.requestFocusInWindow(); // // درخواست فوکوس
////    }
////
////    @Override public void keyTyped(KeyEvent e) {} // // استفاده نمی‌کنیم
////
////    @Override
////    public void keyPressed(KeyEvent e) { // // فشردن کلید
////        int code = e.getKeyCode(); // // کد کلید
////        if (code == KeyEvent.VK_PLUS || code == KeyEvent.VK_EQUALS) controller.zoomIn(); // // +
////        if (code == KeyEvent.VK_MINUS) controller.zoomOut(); // // -
////        if (code == KeyEvent.VK_LEFT)  controller.pan(20, 0); // // ←
////        if (code == KeyEvent.VK_RIGHT) controller.pan(-20, 0); // // →
////        if (code == KeyEvent.VK_UP)    controller.pan(0, 20); // // ↑
////        if (code == KeyEvent.VK_DOWN)  controller.pan(0, -20); // // ↓
////    }
////
////    @Override public void keyReleased(KeyEvent e) {} // // استفاده نمی‌کنیم
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
////import java.awt.event.KeyEvent; // // رویداد کلید
////import java.awt.event.KeyListener; // // لیسنر کلید
////
////public class InputHandler implements KeyListener { // // مدیریت ورودی کیبورد
////    private final UIController controller; // // مرجع کنترلر
////    private final SimulatorPanel panel; // // برای درخواست فوکوس
////
////    public InputHandler(UIController controller, SimulatorPanel panel) { // // سازنده
////        this.controller = controller; // // مقداردهی کنترلر
////        this.panel = panel; // // مقداردهی پنل
////    }
////
////    @Override
////    public void keyPressed(KeyEvent e) { // // فشرده شدن کلید
////        int code = e.getKeyCode(); // // کد کلید
////        if (code == KeyEvent.VK_SPACE) { // // اگر Space
////            if (controller.isPaused()) controller.resume(); else controller.pause(); // // توقف/ادامه
////        } else if (code == KeyEvent.VK_PLUS || code == KeyEvent.VK_EQUALS) { // // افزایش سرعت
////            controller.changeSpeed(Math.max(10, controller.getTickInterval() - 10)); // // کاهش فاصله تیک
////        } else if (code == KeyEvent.VK_MINUS) { // // کاهش سرعت
////            controller.changeSpeed(controller.getTickInterval() + 10); // // افزایش فاصله تیک
////        }
////    }
////
////    @Override public void keyTyped(KeyEvent e) {} // // استفاده نمی‌شود
////    @Override public void keyReleased(KeyEvent e) {} // // استفاده نمی‌شود
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
////package ui;
////
////import java.awt.event.KeyEvent;
////import java.awt.event.KeyListener;
////
////public class InputHandler implements KeyListener {
////    private final UIController controller;
////
////    public InputHandler(UIController controller) {
////        this.controller = controller;
////    }
////
////    @Override
////    public void keyPressed(KeyEvent e) {
////        switch (e.getKeyCode()) {
////            case KeyEvent.VK_SPACE -> {
////                if (controller.isPaused()) controller.resume(); else controller.pause();
////            }
////            case KeyEvent.VK_PLUS, KeyEvent.VK_EQUALS -> controller.changeSpeed(Math.max(10, controller.getTickInterval() - 10));
////            case KeyEvent.VK_MINUS -> controller.changeSpeed(controller.getTickInterval() + 10);
////            case KeyEvent.VK_F -> controller.toggleFollow();
////            case KeyEvent.VK_R -> controller.resetSimulation();
////            default -> {}
////        }
////    }
////
////    @Override public void keyTyped(KeyEvent e) {}
////    @Override public void keyReleased(KeyEvent e) {}
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
////import java.awt.event.KeyAdapter;
////import java.awt.event.KeyEvent;
////
////public class InputHandler extends KeyAdapter {
////    private final Camera camera;
////    private final Runnable onViewChanged;
////
////    public InputHandler(Camera camera, Runnable onViewChanged) {
////        this.camera = camera;
////        this.onViewChanged = onViewChanged;
////    }
////
////    @Override
////    public void keyPressed(KeyEvent e) {
////        int step = 30;
////        switch (e.getKeyCode()) {
////            case KeyEvent.VK_LEFT  -> camera.pan(-step, 0);
////            case KeyEvent.VK_RIGHT -> camera.pan(step, 0);
////            case KeyEvent.VK_UP    -> camera.pan(0, -step);
////            case KeyEvent.VK_DOWN  -> camera.pan(0, step);
////        }
////        onViewChanged.run(); // معمولاً repaint()
////    }
////}
