# MOBILE PROGRAMMING PROJECT
## Smart Finance AI

## Features

### 1. Transaction System
ระบบธุรกรรมที่แสดงภาพรวมการเงินของผู้ใช้ผ่านกราฟ โดยประกอบไปด้วย:
- รายรับ (Income) ที่ผู้ใช้กรอกเอง
- รายจ่าย (Expense)
- ยอดเงินคงเหลือ (Balance)

ผู้ใช้สามารถดูแนวโน้มการใช้เงินและภาพรวมทางการเงินได้ในหน้า Dashboard

---

### 2. Add Transaction
ระบบเพิ่มธุรกรรมใหม่ รองรับทั้งรายรับและรายจ่าย โดยสามารถระบุข้อมูลได้ดังนี้:
- ชื่อรายการ
- จำนวนเงิน
- หมวดหมู่ (Category) เช่น ค่าอาหาร ค่าเดินทาง ดูหนัง เงินเดือน หรือ Freelance
- Note สำหรับรายละเอียดเพิ่มเติม

มีหน้าสำหรับแสดงรายการธุรกรรมทั้งหมดของผู้ใช้

---

### 3. Group Expense System

#### Create Group
- ผู้ใช้สามารถสร้างกลุ่ม เช่น ทริปท่องเที่ยว หรือค่าหอพัก
- สามารถเพิ่มสมาชิกเข้ากลุ่มได้

#### Group Transactions
- บันทึกรายการค่าใช้จ่ายภายในกลุ่ม
- ระบุว่าใครเป็นผู้จ่าย

#### Balance
- แสดงยอดรวมว่าแต่ละคนใช้จ่ายไปเท่าไหร่

#### Settlement
- คำนวณว่าใครต้องจ่ายเงินให้ใคร
- มีระบบลดจำนวนการโอนเงิน เช่น:
  - A จ่ายให้ B และ B จ่ายให้ C
  - ระบบจะสรุปเป็น A จ่ายให้ C

#### Summary
- แสดงสรุปค่าใช้จ่ายของกลุ่ม
- จำนวนสมาชิก
- ภาพรวมการใช้เงิน

#### AI Analysis (Group)
- วิเคราะห์พฤติกรรมการใช้เงินของกลุ่ม
- แนะนำแนวทางการประหยัด

---

### 4. AI Chat System
ระบบแชทกับ AI เพื่อให้คำแนะนำด้านการเงิน เช่น:
- วิธีการประหยัดเงิน
- ตรวจสอบความผิดปกติของการใช้เงิน
- วิเคราะห์ว่าผู้ใช้ใช้เงินไปกับอะไรเป็นหลัก

---

### 5. Widget and AI Insight
ระบบ Widget สำหรับแสดงข้อมูลสำคัญของผู้ใช้บนหน้าจอหลัก ได้แก่:
- ยอดเงินคงเหลือ (Balance)
- ยอดใช้จ่ายรวม (Total Spend)

รวมถึง AI Insight ที่สรุปพฤติกรรมการเงินแบบสั้น เช่น:
- การใช้จ่ายในหมวดที่สูงผิดปกติ
- การแจ้งเตือนเมื่อใช้เงินเกิน

---

## Tech Stack
- Android (Kotlin)
- Firebase (Authentication, Firestore)
- OpenAI API (AI Analysis and Chat)

---

## WIREFRAME
![alt text](https://github.com/WATDAHELL888/MyMobileProject/blob/master/Wireframe%20for%20mobile%20project.jpg?raw=true)  

## Presentation video :
https://drive.google.com/drive/folders/12sivb5QRcVPpbY3aKHknDuLZr2Pw9BXK?usp=drive_link

