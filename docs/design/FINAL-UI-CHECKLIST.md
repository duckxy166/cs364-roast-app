# Final UI checklist · 16 พฤศจิกายน 2026

ส่งมอบงาน #13 จาก main `e0c798a` บน branch `codex/final-ui-checklist` งาน #3/#4 รวมเข้า main และปิดแล้ว เจ้าของงานอนุมัติหน้าอื่นทั้งหมด และให้กลับเฉพาะ Home เป็นแบบเก่า ก่อนอนุมัติ push รอบนี้

- [ต้นแบบกดเล่นทั้งระบบ](playable-app.html?screen=home)
- [ภาพ Android รุ่นส่งมอบทุกหน้า](final-approved.html)
- [ผลรันทดสอบ](../evidence/final-approved/results.json)
- [รายละเอียดการแก้](REVIEW-FIXES.md)

## Checklist งาน UI

- [x] หน้าหลักแบบเดิม: กล่องโฟกัส cobalt / ปุ่ม coral ตามด้วยทางเข้าประวัติ; นำสรุปวันนี้ที่ไม่รับแบบออก
- [x] Onboarding, รายการแอป/เลือกแอป/ตั้งเวลา, โฟกัส, ตั้งค่า, บุคลิก/คำด่า, แจ้งเตือน, ประวัติและสถิติ ใช้ธีมและรูปแบบชุดเดิม
- [x] สไตล์การเตือนอยู่ Settings พร้อมชื่อที่เลือก ไม่อยู่ Home; Back กลับ Settings
- [x] ซ่อนแถบสถานะเมื่อพร้อม; สิทธิ์ไม่ครบจึงเตือน; เริ่มใหม่มี pending/failed/success
- [x] แก้ Home แท็บเล็ต crash เพราะขาด btn_nav_history; ทดสอบ Home → History จริง
- [x] กราฟแยกข้อมูลขาดจากศูนย์จริง ระบุวันและขอบเขตแอป มีรายละเอียดรายวันอ่านได้และเลื่อนได้
- [x] Custom focus ไม่ค้าง preset, เวลา 60 นาทีขึ้นไปแสดงชั่วโมง, ยืนยันก่อนจบ/ลบ
- [x] ทุกหน้าตรวจทั้ง Day/Night มือถือ 390dp และแท็บเล็ต 800dp เพิ่มจอเล็ก 360dp fontScale 1.3; ภาษาไทย/อังกฤษตาม matrix
- [x] ตรวจภาพหน้าหลักและหน้ารองใน matrix ไม่พบข้อความ/ปุ่มซ้อนกันหรือถูกตัดใน layout ที่ทดสอบ เนื้อหายาวอยู่นอก viewport สามารถเลื่อนต่อได้
- [x] เจ้าของงานตรวจรับแบบและอนุมัติส่ง
- [x] assembleDebug assembleRelease lintDebug --offline ผ่าน มี warnings ของโครงการ
- [x] release DEX ไม่มี capture runners ทั้งสาม; production Manifest/dependencies/permissions ไม่เปลี่ยน
- [x] ไฟล์ skill/บริบทเครื่องมือไม่อยู่ในรายการส่ง

## หลักฐานรุ่นที่จะส่ง

API 36, 53 สถานะ × 5 configurations = 265 ภาพ พร้อมผลผ่าน 15 runs จาก FinalSystemCapture, UiReferenceCapture และ RemainingUiCapture:

| รูปแบบ | ภาษา | ฟอนต์ |
|---|---|---|
| มือถือ 390dp Day / Night | ไทย | 100% |
| แท็บเล็ต 800dp Day | ไทย | 100% |
| แท็บเล็ต 800dp Night | อังกฤษ | 100% |
| จอเล็ก 360dp Day | อังกฤษ | 130% |

ภาพ/ผลรุ่นสุดท้ายอยู่ docs/evidence/final-approved/ และ SHA-256 ของ APK อยู่ apk-sha256.txt ภาพใน final-ui-review.html / review-fixes.html / compare-old-new.html เป็นประวัติการออกแบบก่อนการอนุมัติ Home รอบสุดท้าย ระบุไว้บนหน้าแล้ว

Native instrumentation ตรวจ known zero/unknown, 5/7 วันรวม 3 ชม. 5 นาที, ข้อความรายวัน 7 รายการ, retry pending/failure และ retry จริงเมื่อไม่มี tracked apps กลับ READY, navigation, notification/channel/ไอคอน ! จริง 3 ระดับ, history และยกเลิกล้างไม่ลบข้อมูล

ภาพ app usage, focus success, partial chart และ failure บางสถานะเป็น presentation fixtures เฉพาะ debug ไม่ถูกโหลดในแอปปกติ Notification และการบันทึก history เรียกของจริงบน emulator เครื่องทดสอบลบเฉพาะ events ที่สร้างและคืนค่าภาษา/ธีม/ขนาดจอ/ฟอนต์

Browser ผ่านเส้นทาง add/invalid limit, focus/preset/hours/cancel, persona add/error, simulate→history/clear/cancel, ภาษา/ธีม, persistence และหลายขนาดโดยไม่พบ runtime error หรือ overflow ต้นแบบไม่อ่าน usage เครื่องและไม่ส่งเสียง

## งานรวมระบบ #14

การตรวจ UI นี้ไม่แทนการทดสอบใช้แอปจริงจนเกินเวลา เสียง/สั่น/หยุดเสียงและการทำงานเบื้องหลังระยะยาว หรือ TalkBack บนอุปกรณ์จริง งานเหล่านี้เป็นการตรวจระบบของ #14 ไม่ใช่งานที่ติ๊กว่าทำแล้วใน #13

งานส่งผ่าน branch/PR ไม่ push ตรง main และไม่ merge อัตโนมัติ

ที่มา: AI/Codex ช่วยตามแบบและ feedback ของ Danny; Noto Sans Thai/OFL, Lucide/ISC และ Material จากโครงการเดิม ไม่มี dependency ใหม่หรือ skill แนบ
