# เสียงพากย์คำด่าระดับ 3 (Roast Voice Assets — Issue #16)

เอกสารสรุปที่มา รายละเอียดการบันทึกเสียง และรายการไฟล์เสียงคำด่าระดับ 3 ทั้ง 16 ไฟล์ (ภาษาไทย 8 ประโยค และภาษาอังกฤษ 8 ประโยค) สำหรับใช้งานในฟีเจอร์การเตือนระดับ 3 (`app/src/main/res/raw/roast_*`)

---

## 1. ที่มาของสื่อและรายละเอียดการอัดเสียง (Production & Attribution)

- **ผู้พากย์เสียง (Voice Actor):** สุทธิพจน์ (Suttipoch) — สมาชิกในทีมผู้รับผิดชอบ Issue #16 พากย์เองทั้งหมด 16 ประโยคด้วยคนเดียว เพื่อให้โทนเสียงเป็นเสียงเดียวกันทั้งแอป
- **รูปแบบน้ำเสียง:** ดุ จริงจัง กระชับ เหมือนจับได้คาหนังคาเขา
- **อุปกรณ์บันทึกเสียง:** ไมโครโฟนโทรศัพท์มือถือ / ไมโครโฟนคอมพิวเตอร์ บันทึกในห้องเงียบ ระยะห่างประมาณ 1 คืบ
- **โปรแกรมตัดต่อและปรับแต่งเสียง:** ตัดความเงียบช่วงต้นและท้ายไฟล์ (เริ่มพูดภายใน 0.2 วินาทีแรก), ปรับระดับความดัง (Normalize) ให้ใกล้เคียงกันทุกไฟล์โดยไม่ให้เสียงแตก (Clipping) และแปลงไฟล์เป็นรูปแบบ `.mp3`
- **ความยาวแต่ละไฟล์:** ประมาณ 1.5 – 3.5 วินาที (ทุกไฟล์ยาวไม่เกิน 5 วินาทีตามข้อกำหนด)
- **ตำแหน่งจัดเก็บในโปรเจกต์:** `app/src/main/res/raw/`

---

## 2. รายการไฟล์เสียงและบทพากย์ (Voice Script & File Mapping)

### ภาษาไทย (Thai — 8 ไฟล์)

| ชื่อไฟล์ใน `res/raw/` | Resource ID | บทพากย์ |
|---|---|---|
| `roast_th_01.mp3` | `R.raw.roast_th_01` | เกินเวลาแล้วนะ วางมือถือเดี๋ยวนี้ |
| `roast_th_02.mp3` | `R.raw.roast_th_02` | ยังไม่เลิกอีกเหรอ งานเสร็จแล้วหรือไง |
| `roast_th_03.mp3` | `R.raw.roast_th_03` | นิ้วโป้งเธอขยันกว่าตัวเธออีกนะ |
| `roast_th_04.mp3` | `R.raw.roast_th_04` | เลื่อนต่อไปก็ไม่มีอะไรใหม่หรอก มีแต่เวลาที่หายไป |
| `roast_th_05.mp3` | `R.raw.roast_th_05` | แบตเหลือเยอะ แต่อนาคตเหลือน้อยลงทุกนาที |
| `roast_th_06.mp3` | `R.raw.roast_th_06` | บอกว่าอีกห้านาที นี่ผ่านมากี่รอบแล้ว |
| `roast_th_07.mp3` | `R.raw.roast_th_07` | จอมันไม่ได้รักเธอ วางมันลงเถอะ |
| `roast_th_08.mp3` | `R.raw.roast_th_08` | พอได้แล้ว ไปทำอะไรที่มีประโยชน์เดี๋ยวนี้ |

### ภาษาอังกฤษ (English — 8 ไฟล์)

| ชื่อไฟล์ใน `res/raw/` | Resource ID | บทพากย์ |
|---|---|---|
| `roast_en_01.mp3` | `R.raw.roast_en_01` | Time's up. Put the phone down. Now. |
| `roast_en_02.mp3` | `R.raw.roast_en_02` | Still scrolling? Is your work done yet? |
| `roast_en_03.mp3` | `R.raw.roast_en_03` | Your thumb works harder than you do. |
| `roast_en_04.mp3` | `R.raw.roast_en_04` | Nothing new down there. Just your time disappearing. |
| `roast_en_05.mp3` | `R.raw.roast_en_05` | Battery's full. Your future isn't. |
| `roast_en_06.mp3` | `R.raw.roast_en_06` | You said five more minutes. How many times now? |
| `roast_en_07.mp3` | `R.raw.roast_en_07` | The screen doesn't love you back. Let it go. |
| `roast_en_08.mp3` | `R.raw.roast_en_08` | That's enough. Go do something useful. |
