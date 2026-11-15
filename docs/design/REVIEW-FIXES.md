# แก้ผลรีวิวทั้ง 5 ประเด็น · 15 พฤศจิกายน 2026

ฐาน: main e0c798a บน branch `codex/final-ui-checklist` งานรอบนี้แก้ **Java/XML/resources Android จริง** พร้อมต้นแบบ HTML ไม่ใช่การส่ง HTML แทนหน้าจอ Android เตรียมส่งงานผ่าน Pull Request สำหรับ #13

- [ต้นแบบกดเล่น](playable-app.html)
- [ภาพจาก Android หลังแก้](review-fixes.html)
- [APK Debug สำหรับติดตั้งตรวจ](../../app/build/outputs/apk/debug/app-debug.apk)
- ภาพและผลรัน: `../evidence/review-fixes/`

| ประเด็น | การแก้ Android จริง |
|---|---|
| P0 Home แท็บเล็ตเปิดไม่ได้ | มี btn_nav_history ทั้งมือถือและ sw600dp ใช้ ID เดิม เปิด Home และไป History ได้ |
| P1 ความหมายกราฟ | อ่าน daily buckets บน worker; ใช้ null เมื่อข้อมูลไม่ครบทุกแอปที่เลือก แยก known zero; แสดงช่วงวันที่จริง จำนวนวันที่ครบ และรวมเฉพาะวันที่ครบ ไม่มี comparison ที่ข้อมูลไม่พอ |
| P1 Restart ไม่มี feedback | STARTING ปิดปุ่มกดซ้ำ ตรวจสิทธิ์และสถานะ service จริง มีช่วงรอยืนยัน start แบบจำกัดเวลา เปลี่ยน READY หรือ FAILED พร้อม retry/ตรวจสิทธิ์ ป้องกัน callback เก่าแก้ UI หลังออกจากหน้า |
| P2 Home ผิดน้ำหนัก | แอป/เวลาคงเหลือมาก่อน เจ้าของงานเลือกกลับเป็นกล่อง Focus เดิมและทางเข้าประวัติด้านล่าง ไม่เพิ่มสรุปวันนี้; ย้าย Persona เข้า Settings พร้อมค่าปัจจุบัน |
| P2 อ่าน/เข้าถึงกราฟยาก | ไม่มี gray track เต็มที่ดูเหมือนมีข้อมูล ใช้ 13sp จาก dimen ให้แกนกราฟ และมี Show/Hide daily values เป็นข้อความ 14sp ขยาย/ตัดบรรทัดได้ แยก unknown จาก 0 มีชื่อปุ่มและ live status; ปุ่มลบประวัติ 48dp |

## การตีความข้อมูล

นับเฉพาะ **แอปที่เลือกคุมอยู่ตอนเปิดดู** ตามสัญญาของฟีเจอร์เดิม ไม่ได้อ้างว่าเป็น screen time ทั้งเครื่อง

`WeeklyUsageData` จะรวมวันหนึ่งได้ต่อเมื่อ Android ส่ง bucket ของแอปที่เลือกครบทุกแอป วันไม่มี bucket หรือขาดบางแอปแสดงข้อมูลไม่ครบ แม้บางแอปมีเวลาบวกอยู่ เพื่อไม่แสดงยอดบางส่วนเป็นยอดเต็ม ถ้า Android ให้ข้อมูลครบและรวมได้ 0 จึงแสดง 0 จริง ไม่มีการเติม 0 หรือแต่งย้อนหลัง

นี่เป็นการอ่านแบบระมัดระวัง ข้อมูลจริงบางเครื่องจึงอาจแสดงจำนวนวันที่ครบได้น้อยกว่าจำนวนวันที่ใช้งาน การมี historical completeness ที่รับรองได้กว่านี้ต้องเพิ่มสัญญาข้อมูลของเจ้าของ tracking ไม่ได้สร้าง schema หรือข้อมูลปลอมในรอบ UI นี้

ยอดรวมและรายละเอียดแปลงตั้งแต่ 60 นาทีเป็น ชม./นาที; เวลาบวกที่ต่ำกว่านาทีมีข้อความน้อยกว่า 1 นาที ช่วงวันใช้ timezone ของเครื่องและ 7 วันล่าสุดแบบรวมวันนี้ ไม่ใช้คำว่าสัปดาห์นี้

## Native / prototype

Home, History และ Settings ถูกแก้ใน native แล้ว รวมทางเข้า Persona สไตล์ที่เลือกและทางกลับ Settings ส่วนต้นแบบใช้ model ตัวอย่าง: ปกติรู้ข้อมูลเพียงวันนี้ วันที่เก่าเป็น unknown; กรณีข้อมูลครบ 7 วัน/บางวัน/ไม่มีข้อมูล/อ่านไม่ได้เลือกจากแผงทดลองนอกกรอบแอป ไม่ได้อ่านข้อมูลเครื่องหรือส่งเสียงจริง

ฟอนต์ สี ไอคอน shared resources และลำดับหน้าตรงกันตามแนวทางเดียวกัน ระบบ status bar, คีย์บอร์ด และ notification shade ขึ้นกับ Android ไม่เทียบ pixel กับ browser chrome

## การยืนยัน

`assembleDebug assembleRelease lintDebug --offline` ผ่าน มี warnings เดิมของโครงการ ไม่ได้อ้างว่าไม่มี warning

ตรวจบน API 36: มือถือ 390dp Day/Night ไทย, แท็บเล็ต 800dp Day ไทย/Night อังกฤษ, จอเล็ก 360dp Day อังกฤษ fontScale 1.3 ผลสุดท้ายบันทึกใน `../evidence/review-fixes/results.json` และภาพ 19 สถานะต่อ configuration รวม 95 ภาพ

Debug instrumentation ตรวจว่าข้อมูลครบ 0 ยังคงเป็น 0, ขาดบางแอป/ขาดทั้งวันยังเป็น null, ชุดตัวอย่าง 5/7 วันรวมได้ 3 ชม. 5 นาที, ข้อความรายวันครบ 7 รายการ, STARTING ปิดปุ่ม, FAILED แสดง recovery และ real retry เมื่อไม่มี tracked apps กลับ READY ได้ พร้อมตรวจทางเข้าประวัติและ notification 3 ระดับเดิม

บางภาพเป็น presentation fixtures ผ่าน reflection เฉพาะ debug เพื่อให้ตรวจช่วงข้อมูลและ failure state ได้ ไม่ใช่ข้อมูล production ภาพ Home live/ประวัติ notification จริงยังแยกไว้ ตัวทดสอบเก็บ/คืนภาษาและธีม ลบเฉพาะ event ที่สร้างระหว่างรัน และคืนขนาดจอ/ฟอนต์ เครื่องจำลองนี้เป็น dedicated emulator

Browser prototype ตรวจ known/unknown/coverage, ทุก data state, retry pending/failure/success, hierarchy และ overflow ที่ 320/390/1000px ผลอยู่ `prototype-checks.json`

ยังไม่ใช่การยืนยัน TalkBack บนอุปกรณ์จริง หรือการทดสอบเวลาเตือน/เสียง/สั่นต่อเนื่องทั้งระบบใน #14 ไม่แก้ AlertThresholds, service loop, database schema, notification/audio behavior หรือ dependencies

## ไฟล์หลัก

- `core/HomeActivity.java`, `ui/MonitoringStatusUi.java`
- `history/HistoryActivity.java`, `history/UsageHistorySnapshot.java`, `history/WeeklyUsageData.java`, `history/WeeklyChartView.java`
- `settings/SettingsActivity.java`, layout phone/sw600dp, strings_pid_review.xml ทั้งสองภาษา
- helper `ui/UsageTimeText.java`; test instrumentation อยู่ debug และไม่รวมใน release

ที่มา: ใช้ AI/Codex ช่วยตาม feedback ของ Danny, Noto Sans Thai/OFL, Lucide/ISC และ Material จากชุดเดิม ไม่มี skill หรือบริบทเครื่องมือที่ต้องส่งกับงาน ไม่มี network/dependency/permission ใหม่

## ผลตรวจของเจ้าของงาน

เจ้าของงานรับหน้าที่เหลือ และให้กลับเฉพาะหน้าหลักเป็นแบบเก่า: กล่องโฟกัสสีน้ำเงินและปุ่มสีคอรัล ตามด้วยทางเข้าประวัติและสถิติ นำสรุปวันนี้ออก คงสไตล์การเตือนในตั้งค่า รวมทั้งการแก้แท็บเล็ตและสถานะเริ่มตรวจจับไว้ โดยอนุมัติ push แล้ววันที่ 16 พ.ย. 2026

## รุ่นส่งมอบที่อนุมัติ · 16 พ.ย. 2026

ดู final-approved.html และ FINAL-UI-CHECKLIST.md สำหรับภาพและผลของรุ่นที่กลับ Home แล้ว ภาพใน review-fixes.html เป็นประวัติก่อนกลับ Home ใช้ประกอบการเปรียบเทียบ ไม่ใช่แบบปัจจุบัน
