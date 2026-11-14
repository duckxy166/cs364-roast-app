# ตรวจงาน UI รอบที่เหลือ · 14 พฤศจิกายน 2026

งานรอบนี้ส่งต่อบน branch `feature/13-ui-polish` ตามที่ผู้ใช้อนุญาตวันที่ 14 พฤศจิกายน 2026 โดยไม่ merge เข้า main และไม่มีการโพสต์คอมเมนต์ประสาน

เปิด `review-ui-completion.html` เพื่อเลือกหน้า สถานะ ธีม และขนาดจอ ภาพ Android อยู่ใน `../evidence/ui-completion/`; แบบสำหรับทดลองและส่งต่ออยู่ใน `remaining-screens.html` และ `screens/remaining/`

## สิ่งที่ทำใน Android

- **บุคลิกและคำด่า:** หัวข้อและปุ่มกลับเข้าชุดเดิม ตัวเลือกแบบมี selected state คำอธิบายแต่ละบุคลิก ภาพตัวอย่างข้อความ แยกช่องเพิ่มกับรายการที่บันทึก ปุ่มนำออกมีทั้งไอคอนและคำ ภาพ empty/error/add/delete ทั้งสองธีม ใช้ radio IDs, การเลือกบุคลิก, DAO และ ConfirmDialog เดิม
- **โฟกัส:** ข้อผิดพลาดอยู่ติดช่องกรอก แสดงแถบสำเร็จเมื่อจบรอบ แยกจากประวัติ และบันทึกสถานะแถบสำเร็จเมื่อสร้างหน้าจอใหม่
- **เวลาแบบชั่วโมง:** ต่ำกว่า 60 นาทีแสดง MM:SS; ตั้งแต่ 60 นาทีแสดง H:MM:SS พร้อมป้ายหน่วย การทดสอบครอบคลุม 59:59, 1:00:00, 8:19:53 และ 16:39:59 ปรับเฉพาะการแสดงผล ไม่เปลี่ยน clock, pause/resume หรือข้อมูลที่บันทึก
- **สถานะตรวจจับ:** READY ซ่อนแถบเตือน; MISSING_PERMISSIONS และ STOPPED แสดงเหตุผลกับปุ่มแก้ไข โดยรับสถานะและ recovery callback จากเจ้าของฟีเจอร์ ไม่เพิ่มการตรวจสิทธิ์หรือสั่ง Service เอง
- **การแจ้งเตือน:** ใช้ไอคอน ! แบบ monochrome และ accent สีน้ำเงินใน notification builder ทั้งสามระดับ หน้าต่าง preview แสดง hierarchy ตาม notification จริง คง channels, ข้อความฟีเจอร์, vibration/audio และ dismiss behavior เดิม

## ประวัติและสถิติ (#10)

ทำเป็นแบบส่งต่อของฟีเจอร์เสริม: กราฟใช้งาน 7 วัน รายการแจ้งเตือน ลบรายการเดียว ล้างทั้งหมด ยืนยัน/ยกเลิก และสถานะว่าง ไม่มีการเพิ่มตาราง DB, Activity หรือฟีเจอร์บันทึกประวัติใน production

กราฟเวลาใช้งานยังอยู่เมื่อผู้ใช้ล้างประวัติการเตือน เพราะเป็นข้อมูลคนละประเภท แบบนี้ใช้ข้อมูลตัวอย่างและมีป้ายกำกับในต้นแบบ

## จุดเชื่อมสำหรับทีม

- `HomeActivity.renderPermissionWarning(boolean)` ยังคงใช้ได้
- `HomeActivity.renderMonitoringState(MonitoringStatusUi.State, Runnable recovery)` รับ READY / MISSING_PERMISSIONS / STOPPED โดย callback เป็นของเจ้าของฟีเจอร์
- Persona ใช้ `radio_group_persona`, `radio_persona_*`, `input_layout_custom_roast`, `edit_custom_roast`, `btn_add_custom_roast`, `text_custom_roast_empty`, `recycler_custom_roasts` และ `btn_delete_custom_roast` เดิม ตัวลบเปลี่ยนชนิด view จาก ImageButton เป็น MaterialButton และแก้ชนิดตัวแปรใน adapter ให้ตรงกัน
- Focus ใช้ field และปุ่มเดิม เพิ่มเฉพาะแถบ feedback กับ presentation state

## การตรวจ

ต้นแบบทดสอบไทย/อังกฤษที่ 320, 360, 736px รวม add/error/remove/cancel/Escape, ข้อความค้างระหว่างสลับธีม, preset synchronization, monitoring recovery และยืนยันล้างประวัติ ผลอยู่ใน `screens/remaining/qa-results.json`

ภาพ native เก็บจาก debug instrumentation บน Android Emulator API 36 เฉพาะเครื่องทดสอบ 390dp Day/Night, 800dp Day/Night และ 360dp fontScale 1.3 ผลแต่ละรอบอยู่ใน `../evidence/ui-completion/capture-results.json`

เครื่องมือทดสอบเพิ่มข้อความชั่วคราวผ่านปุ่มจริง ทดสอบยกเลิก/ยืนยันการลบ แล้วลบเฉพาะ record ที่ตัวเองเพิ่ม พร้อมคืนค่าบุคลิก ภาษา ธีม ขนาดจอ ฟอนต์และสิทธิ์แจ้งเตือน ตัวอย่างสถานะโฟกัสสำเร็จเป็น presentation fixture ไม่ได้เขียนรอบโฟกัสจำลองลงฐานข้อมูล

ภาพใน `screens/remaining/` เป็น browser design references ไม่ใช่หลักฐาน Android ภาพใน `evidence/ui-completion/` เป็น native captures ส่วน notification shade ใช้ Android layout จริง จึงมีรูปแบบต่างจากการ์ดต้นแบบได้ตามระบบ

Build debug/release และ lint ผ่านก่อนส่งตรวจ แต่ไม่ได้อ้างว่า warnings, TalkBack, อุปกรณ์จริง หรือการเชื่อม tracking ทั้งหมดผ่านครบแล้ว

## ที่มา

ต่อยอดชุดสี Noto Sans Thai, รูปแบบ Material และไอคอน Lucide ของงานเดิม โดยใช้ AI/Codex ช่วยตาม feedback ของผู้ใช้ ฟอนต์ใช้ OFL และ icon system ใช้ ISC ตามเอกสารของชุดดีไซน์เดิม ไม่มีภาพ stock หรือไลบรารีใหม่
