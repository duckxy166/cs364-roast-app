# cs364-roast-app

Term Project วิชา CS364 การพัฒนาโปรแกรมประยุกต์สำหรับอุปกรณ์พกพา — แอป Android ที่ด่าผู้ใช้เมื่อเล่นมือถือนานเกินเวลาที่ตั้งไว้ (ยังไม่ได้ตั้งชื่อแอป)

ตอนนี้เป็นโครงโปรเจกต์เปล่า: มี `MainActivity` หน้าเดียว ยังไม่มีฟังก์ชันของแอป

## เครื่องมือ

- Android Studio (ใช้ JDK ที่มากับโปรแกรม)
- Java 17 · XML layout · AppCompat + Material 3 + ConstraintLayout
- Android Gradle Plugin 9.3.2 · Gradle 9.5.0 · compileSdk 35 · minSdk 26

## เปิดโปรเจกต์

1. Clone repo นี้
2. Android Studio → **Open** → เลือกโฟลเดอร์ `cs364-roast-app`
3. รอ Gradle sync เสร็จ แล้วกด **Run**

Build จาก command line:

```bash
./gradlew assembleDebug
```

## โครงสร้าง

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/cs364/roastapp/MainActivity.java
└── res/
    ├── layout/activity_main.xml
    └── values/  (strings.xml, themes.xml)
```
