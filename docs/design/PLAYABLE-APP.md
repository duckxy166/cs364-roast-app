# ต้นแบบกดเล่น · วางเดี๋ยวนี้!

แบบที่เจ้าของงานอนุมัติวันที่ 16 พฤศจิกายน 2026: ใช้หน้าหลักแบบเดิม (กล่องโฟกัสสีน้ำเงิน ปุ่มคอรัล ตามด้วยทางเข้าประวัติ) และหน้าที่เหลือแบบใหม่ สไตล์การเตือนอยู่ในตั้งค่า

เปิด [playable-app.html](playable-app.html) เพื่อทดลอง Home → Apps → Picker → Daily limit; Focus เลือกเวลา/พิมพ์เอง/จับเวลา/ยืนยันจบ; Settings ภาษา/ธีม/สาธิต; Persona เลือกบุคลิก/เพิ่ม/ลบข้อความ; History ดูรายวัน/ลบ/ยกเลิก/ล้างทั้งหมด

ข้อมูลจำลองอยู่ใน localStorage แผงทดลองนอกกรอบแอปมีกรณีข้อมูลสถิติครบ/บางวัน/ไม่ครบ/อ่านไม่ได้ และการเริ่มตรวจจับสำเร็จ/ล้มเหลว ต้นแบบไม่ติดตามเครื่อง ไม่ส่ง notification ระบบ และไม่เล่นเสียงจริง

Android ใช้ Java/XML/resources ใน app/src/main ไม่ได้ใช้ HTML รันเป็นแอป ภาพรุ่นที่ส่งอยู่ใน [final-approved.html](final-approved.html) และผลตรวจใน [FINAL-UI-CHECKLIST.md](FINAL-UI-CHECKLIST.md) ระบบ status bar/คีย์บอร์ด/notification shade ขึ้นกับ Android

ภาพ final-ui-review.html และ review-fixes.html เก็บประวัติการตรวจรอบก่อนการอนุมัติ ไม่ใช่แบบปัจจุบัน โดยเฉพาะกล่องสรุปวันนี้และโฟกัสแบบ tonal ที่ถูกยกเลิกแล้ว

ใช้ฟอนต์ท้องถิ่น Noto Sans Thai/OFL, ไอคอน Lucide/ISC และชุดสีเดิม ไม่มี dependency ใหม่ ใช้ AI/Codex ช่วยตาม feedback และการเลือกแบบของ Danny
