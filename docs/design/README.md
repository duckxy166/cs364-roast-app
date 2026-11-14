# วางเดี๋ยวนี้ · Roast Club

ชุดดีไซน์ส้มคอรัล–น้ำเงินสดสำหรับงาน #3, #4 และ #13 เลือกชื่อ วางเดี๋ยวนี้! / Put It Down! และไอคอนแบบที่ 2 เครื่องหมายตกใจสีส้มบนพื้นน้ำเงิน

## Android design system · #3

ใช้ `Theme.RoastApp` ซึ่งเชื่อมสีเข้ากับ Material 3 roles และเปลี่ยน Day/Night ด้วยชื่อ resource เดิม ฟอนต์ Noto Sans Thai ถูก bundle ใน `res/font/` จึงไม่ต้องดาวน์โหลดตอนเปิดแอป ใบอนุญาตอยู่ใน `licenses/NotoSansThai-OFL.txt`

| Style | ใช้เมื่อ |
|---|---|
| `TextAppearance.PutItDown.Heading` | หัวข้อหน้าจอ |
| `TextAppearance.PutItDown.Section` | หัวข้อส่วน |
| `TextAppearance.PutItDown.Body` | เนื้อหาหลัก |
| `TextAppearance.PutItDown.Label` | ป้ายและตัวเลือก |
| `TextAppearance.PutItDown.Supporting` | คำอธิบายรอง |
| `TextAppearance.PutItDown.Timer` | เวลาถอยหลัง |
| `Widget.PutItDown.Button.Primary` | การกระทำหลัก สีส้ม |
| `Widget.PutItDown.Button.Secondary` | การกระทำรอง |
| `Widget.PutItDown.Button.Destructive` | ยืนยันลบ ใช้สี error |
| `Widget.PutItDown.Button.Back` | กลับแบบไม่มีกรอบ พร้อมลูกศร |
| `Widget.PutItDown.Card` | การ์ดรายการ |
| `Widget.PutItDown.ListRow` | แถวกดเลือกรายการ |
| `Widget.PutItDown.TextField` | TextInputLayout แบบมีกรอบ |
| `Widget.PutItDown.ThemeChoice` | ตัวเลือกธีม พร้อม selected state |

ระยะทั้งหมดใช้ `pid_space_*`, `pid_page_inset`, `pid_touch_target` และ `pid_content_width` ใน `dimens.xml`; จอ sw600dp มีระยะและขนาด preview แยก สี `pid_primary_container` คือพื้นปุ่มส้ม ส่วน `pid_primary` เป็นสีเข้มที่อ่านได้บนพื้นหลัก ใช้ on-color คู่กันเสมอ

ไอคอนเมนู: `pid_ic_apps`, `pid_ic_focus`, `pid_ic_settings`; โลโก้ในแอป `pid_ic_bang`; launcher ใช้ adaptive icon `ic_launcher` / `ic_launcher_round` และ monochrome สำหรับ Android 13+ การเปลี่ยน manifest ในงานนี้จำกัดอยู่ที่ icon/roundIcon ไม่มี permission หรือ Activity ใหม่

## เปิดใช้งาน

เปิด `roast-club.html` ด้วย Chrome หรือ Edge ได้ทันที แก้ไขได้ด้วยโปรแกรมแก้ไขข้อความ เช่น VS Code เลือกหน้าจอ สลับภาษา และ Day/Night จากแถบด้านบน

ไฟล์นี้เป็นต้นแบบที่กดทดลองได้ ไม่ใช่ XML สำหรับรันใน Android Studio ข้อมูลและสิทธิ์เป็นข้อมูลจำลอง ไม่มีการเล่นเสียงจริง ฟอนต์ Google Fonts ใช้อินเทอร์เน็ต หากออฟไลน์จะใช้ฟอนต์สำรอง ไอคอนรวมอยู่ในไฟล์แล้ว

## ไฟล์ในชุดนี้

- `roast-club.html`: ต้นฉบับแบบพร้อมการโต้ตอบ
- `screens/`: ภาพ PNG 49 ภาพ ทั้ง Day/Night และสถานะเพิ่มเติม
- `assets/app-icon.svg`: ไอคอนเวกเตอร์
- `assets/mascot.svg`: มาสคอตเวกเตอร์
- `assets/empty-state.svg`: ภาพประกอบรายการว่าง

ภาพ PNG เป็นภาพแบบ ไม่ใช่หลักฐานทดสอบบน Android จริง SVG ต้องแปลงเป็น VectorDrawable หรือส่งออกเป็นรูปก่อนใช้ในแอป

## สีและรูปแบบ

| ส่วน | Day | Night |
|---|---|---|
| พื้นหลัง | #FFF5EE | #171725 |
| พื้นการ์ด | #FFFDF9 | #252538 |
| ข้อความหลัก | #252033 | #FFF3EB |
| ข้อความรอง | #6B5D6A | #C3B8CB |
| ปุ่มหลัก | #FF795C | #FF795C |
| การ์ดโฟกัส | #3448F0 | #3448F0 |
| ข้อความบนการ์ดโฟกัส | #FFF8F0 | #FFF8F0 |
| พื้นสถานะ | #E9E9FF | #323052 |
| ข้อความสถานะ | #3442AA | #C2C5FF |
| เส้นแบ่ง | #D8CBD0 | #51445E |
| ข้อความเตือน | #A12D37 | #FFACB2 |

ฟอนต์ Noto Sans Thai + Manrope ระยะขอบหลัก 24dp การ์ดมน 16dp ปุ่มหลักสูงอย่างน้อย 52dp ใน Android ใช้ sp สำหรับข้อความ และ dp สำหรับระยะ/ขนาด พื้นที่สัมผัสอย่างน้อย 48dp

## การปรับ UX/UI ล่าสุด

- หน้าแรกนำข้อมูลเวลาของแอปขึ้นก่อนการ์ดโฟกัส โดยคงส้มคอรัลและน้ำเงินสด
- ซ่อนการ์ดสถานะเมื่อพร้อมทำงาน แสดงเฉพาะสิทธิ์ไม่ครบหรือหยุดตรวจจับ พร้อมปุ่มแก้ไข
- กรอบแท็บเล็ตใช้ navigation rail ส่วนกรอบแคบกลับเป็น bottom navigation
- ลดคำโปรยอังกฤษ ตัดช่องทางตั้งค่าที่ซ้ำ และใช้ปุ่ม Back ที่มีข้อความ
- ตัวจับเวลาไม่เอียง การยกเลิก dialog คืน focus และค่าที่กรอกไม่หายเมื่อสลับธีม
- ภาพใน screens เป็นแบบ UX/UI จากเบราว์เซอร์ ไม่ใช่หลักฐาน Android จริง

### การปรับรอบล่าสุด

- หน้าโฟกัสก่อนเริ่มเน้นเลือกเวลาและปุ่มเริ่ม วงกลมใหญ่แสดงเมื่อกำลังจับเวลา
- แยกครบเวลาพอดีออกจากเกินเวลา พร้อมแสดงจำนวนนาทีที่เกิน
- หน้าแรกบอกจำนวนแอปทั้งหมดในปุ่มดูทั้งหมด
- ตั้งค่าภาษาเป็นแถวกดเลือก ธีมมีภาพตัวอย่าง และเกณฑ์เตือนกางดูได้

## หน้าจอ

หน้าหลัก, Onboarding ขอสิทธิ์สองอย่าง, รายการแอปที่คุม, เลือกแอป, ตั้งเวลาต่อวัน, ยืนยันลบ, รายการว่าง, โฟกัสพร้อมจับเวลาและยืนยันก่อนออก, ตั้งค่า และตัวอย่างการแจ้งเตือนสามระดับ

ปุ่มแก้เวลาและนำออกมีไอคอนพร้อมข้อความ พื้นที่กดอย่างน้อย 48px ในต้นแบบ เมนูหลักมีหน้าหลัก แอปที่คุม โฟกัส และตั้งค่า พร้อมแสดงหน้าปัจจุบัน

## ข้อกำหนด Android ที่ต้องรักษา

- Java + XML ของโปรเจกต์เดิม รักษา view ID ที่ Java เรียกใช้อยู่
- รวมสี สไตล์ และระยะเป็น resources ในงาน #3 ข้อความทุกคำอยู่ใน strings ของฟีเจอร์ทั้งไทยและอังกฤษ
- Usage Access เปิดหน้าตั้งค่า Android และตรวจสถานะเมื่อกลับมา ขอ Notifications ผ่านระบบจริง
- เวลาใช้งานอ่านผ่าน UsageSource รายการแอปเก็บใน tracked_app ตั้งเวลาต่ำสุด 1 นาที
- ลบข้อมูลต้องยืนยันผ่าน ConfirmDialog
- โฟกัสหยุดเมื่อสลับแอปหรือปิดจอ กลับมาแล้วทำต่อ กด Back ระหว่างจับเวลาต้องยืนยันก่อนทิ้ง
- เตือนระดับ 1 เมื่อครบเวลา ระดับ 2 เมื่อเกิน 5 นาที ระดับ 3 เมื่อเกิน 10 นาที เสียงซ้ำทุก 5 นาที
- โหมดสาธิต: ระดับ 2 ที่เกิน 10 วินาที ระดับ 3 ที่เกิน 20 วินาที เสียงซ้ำทุก 20 วินาที
- อ่านเกณฑ์จาก AlertThresholds ไม่เขียนตัวเลขซ้ำในฟีเจอร์
- การแจ้งเตือนใช้รูปแบบ Android จริง หน้าตาต่างได้ตามเครื่อง เสียงหยุดตาม #17 และเคารพ silent / Do Not Disturb
- ตรวจทั้งสองธีม บนมือถือและแท็บเล็ตจริง รองรับตัวอักษรขยายและเนื้อหาเลื่อนได้
- #13 ยังต้องนำแบบไปทำ layout หลังฟีเจอร์ merge และทดสอบจริงก่อนปิด issue

## ที่มา

แบบและกราฟิกสร้างขึ้นสำหรับโปรเจกต์นี้โดยใช้ AI ช่วย ไม่มีภาพ stock ไอคอน Lucide ใช้ ISC license ฟอนต์ทั้งสองใช้ SIL Open Font License หาก bundle ฟอนต์ต้องแนบ license ด้วย

- [Lucide license](https://lucide.dev/license)
- [Noto Sans Thai](https://fonts.google.com/noto/specimen/Noto+Sans+Thai)
- [Manrope](https://fonts.google.com/specimen/Manrope)
- [Android accessibility](https://developer.android.com/guide/topics/ui/accessibility/apps)
