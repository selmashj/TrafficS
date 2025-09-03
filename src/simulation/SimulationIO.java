package simulation;

import java.io.*;

import core.Vehicle;
import infrastructure.CityMap;

public class SimulationIO {

    // ذخیره وضعیت فعلی شبیه‌سازی (صرفاً به صورت نمایشی)
    public void saveState(World world) {
        System.out.println("Saving world state...");
        // در حالت واقعی، می‌توان به‌صورت JSON/XML/Serialized ذخیره کرد
    }

    // بارگذاری نقشه از فایل (برای الان فقط چاپ تستی)
    public void loadMap(String filePath) {
        System.out.println("Loading city map from: " + filePath);
        // در حالت واقعی می‌توان فایل را خواند و CityMap ساخت
    }

    // بارگذاری وسایل نقلیه از فایل (برای الان فقط چاپ تستی)
    public void loadVehicles(String filePath) {
        System.out.println("Loading vehicles from: " + filePath);
        // در حالت واقعی می‌توان لیست وسایل را از فایل خواند و به World اضافه کرد
    }
}























//package simulation;
//
//import java.io.*;
//
//import core.Vehicle;
//import infrastructure.CityMap;
//
//public class SimulationIO {
//
//    // ذخیره وضعیت فعلی شبیه‌سازی (صرفاً به صورت نمایشی)
//    public void saveState(World world) {
//        System.out.println("Saving world state...");
//        // در حالت واقعی، می‌توان به‌صورت JSON/XML/Serialized ذخیره کرد
//    }
//
//    // بارگذاری نقشه از فایل (برای الان فقط چاپ تستی)
//    public void loadMap(String filePath) {
//        System.out.println("Loading city map from: " + filePath);
//        // در حالت واقعی می‌توان فایل را خواند و CityMap ساخت
//    }
//
//    // بارگذاری وسایل نقلیه از فایل (برای الان فقط چاپ تستی)
//    public void loadVehicles(String filePath) {
//        System.out.println("Loading vehicles from: " + filePath);
//        // در حالت واقعی می‌توان لیست وسایل را از فایل خواند و به World اضافه کرد
//    }
//}
