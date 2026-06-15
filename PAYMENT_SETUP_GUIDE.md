# Hướng dẫn thiết lập thanh toán PRO bằng SePay

Tài liệu này mô tả luồng thanh toán PRO đã được tích hợp giữa:

- Frontend: `FE-learn-english`
- Backend: `BE-learn-english`
- Thanh toán chuyển khoản: SePay + VietQR
- Các gói hiện tại:
  - 1 tháng: `69.000 VND`
  - 3 tháng: `169.000 VND`
  - 1 năm: `499.000 VND`
  - Trọn đời: `1.849.000 VND`

## 1. Luồng thanh toán

1. Người dùng nhấn nút **PRO** trên frontend.
2. Popup giới thiệu quyền lợi PRO xuất hiện.
3. Người dùng nhấn **Mở khóa PRO**.
4. Frontend gọi:

   ```http
   POST /api/v1/payments/pro/orders
   ```

   Body ví dụ:

   ```json
   {
     "planCode": "YEARLY"
   }
   ```

   Giá trị hợp lệ: `MONTHLY`, `QUARTERLY`, `YEARLY`, `LIFETIME`.

5. Backend tạo một đơn `PENDING`, mã chuyển khoản riêng dạng `PRO...` và URL VietQR.
6. Frontend hiển thị QR, số tiền, tài khoản nhận và nội dung chuyển khoản.
7. Frontend gọi API kiểm tra đơn mỗi 3 giây:

   ```http
   GET /api/v1/payments/orders/{orderId}
   ```

8. Người dùng quét QR và chuyển khoản.
9. SePay nhận biến động số dư từ ngân hàng rồi gọi webhook:

   ```http
   POST /api/v1/payments/sepay/webhook
   ```

10. Backend xác thực chữ ký HMAC-SHA256 và kiểm tra:

    - Giao dịch là tiền vào.
    - Đúng tài khoản nhận.
    - Đúng mã thanh toán.
    - Đúng số tiền.
    - Giao dịch chưa được xử lý trước đó.

11. Backend chuyển đơn thành `PAID` và cộng 365 ngày PRO cho người dùng.
12. Lần polling tiếp theo, frontend nhận trạng thái `PAID` và hiển thị thanh toán thành công.

## 2. Chuẩn bị tài khoản ngân hàng

Bạn cần một tài khoản ngân hàng dùng để nhận thanh toán.

Ghi lại ba thông tin:

```text
Ngân hàng: Ví dụ MBBank
Số tài khoản: Ví dụ 0123456789
Tên chủ tài khoản: Ví dụ NGUYEN VAN A
```

Không dùng các thông tin mẫu trên khi triển khai thật.

Tên ngân hàng phải là mã SePay/VietQR hỗ trợ, ví dụ:

- `MBBank`
- `Vietcombank`
- `BIDV`
- `VietinBank`
- `ACB`
- `TPBank`

Danh sách mã ngân hàng:

https://qr.sepay.vn/banks.json

## 3. Tạo và kết nối tài khoản SePay

### Bước 1: Đăng ký SePay

Truy cập:

https://my.sepay.vn

Đăng ký hoặc đăng nhập tài khoản SePay.

### Bước 2: Liên kết tài khoản ngân hàng

Trong trang quản trị SePay:

1. Vào phần **Tài khoản ngân hàng**.
2. Chọn **Thêm tài khoản ngân hàng**.
3. Chọn ngân hàng bạn dùng để nhận thanh toán.
4. Làm theo hướng dẫn kết nối của SePay.
5. Kiểm tra tài khoản đã hoạt động và SePay đọc được biến động số dư.

Thông tin tài khoản trong backend phải trùng với tài khoản đã liên kết tại SePay.

## 4. Cấu hình mã thanh toán

Backend tạo mã đơn dạng:

```text
PROA1B2C3D4E5
```

Trong trang quản trị SePay:

1. Vào **Công ty**.
2. Chọn **Cấu hình chung**.
3. Tìm phần **Cấu trúc mã thanh toán**.
4. Thêm tiền tố:

   ```text
   PRO
   ```

5. Lưu cấu hình.

Việc này giúp trường `code` trong webhook chứa đúng mã đơn. Backend vẫn có thể tìm mã trong nội dung chuyển khoản, nhưng cấu hình tiền tố vẫn được khuyến nghị.

## 5. Tạo Webhook SePay

### Bước 1: Chuẩn bị URL backend

Backend production phải:

- Có domain public.
- Dùng HTTPS hợp lệ.
- Có thể được SePay truy cập từ Internet.

Webhook URL:

```text
https://API_DOMAIN/api/v1/payments/sepay/webhook
```

Ví dụ:

```text
https://api.english.xhroudev.com/api/v1/payments/sepay/webhook
```

Không dùng `localhost` làm webhook production.

### Bước 2: Tạo secret

Tạo một chuỗi bí mật dài và ngẫu nhiên. Ví dụ có thể dùng PowerShell:

```powershell
[Convert]::ToHexString((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

Không commit secret vào Git và không gửi công khai.

### Bước 3: Tạo webhook trên SePay

Trong trang quản trị SePay:

1. Vào phần **WebHooks**.
2. Chọn **Thêm WebHook**.
3. Chọn tài khoản ngân hàng nhận thanh toán.
4. Chọn loại giao dịch **Tiền vào**.
5. Nhập webhook URL của backend.
6. Chọn Content-Type:

   ```text
   application/json
   ```

7. Chọn phương thức xác thực:

   ```text
   HMAC-SHA256
   ```

8. Nhập secret đã tạo.
9. Lưu webhook.

Secret nhập trên SePay phải giống hoàn toàn với `SEPAY_WEBHOOK_SECRET` của backend.

## 6. Cấu hình backend

Thiết lập các biến môi trường trên máy chủ chạy `BE-learn-english`:

```env
SEPAY_BANK=MBBank
SEPAY_ACCOUNT_NUMBER=SO_TAI_KHOAN_CUA_BAN
SEPAY_ACCOUNT_HOLDER=TEN_CHU_TAI_KHOAN_KHONG_DAU
SEPAY_WEBHOOK_SECRET=SECRET_DA_NHAP_TREN_SEPAY

PRO_PRICE_VND=499000
PRO_DURATION_DAYS=365
PAYMENT_ORDER_EXPIRY_MINUTES=30
```

Ý nghĩa:

| Biến | Ý nghĩa |
|---|---|
| `SEPAY_BANK` | Mã ngân hàng dùng để tạo VietQR |
| `SEPAY_ACCOUNT_NUMBER` | Số tài khoản nhận tiền |
| `SEPAY_ACCOUNT_HOLDER` | Tên chủ tài khoản hiển thị trên QR |
| `SEPAY_WEBHOOK_SECRET` | Secret xác thực webhook HMAC |
| `PRO_PRICE_VND` | Giá gói PRO, đơn vị VND |
| `PRO_DURATION_DAYS` | Số ngày được cộng sau thanh toán |
| `PAYMENT_ORDER_EXPIRY_MINUTES` | Thời gian hiệu lực của đơn |

Sau khi cập nhật biến môi trường, restart backend.

### Lưu ý

Không thêm số tiền dưới dạng:

```env
PRO_PRICE_VND=499.000
```

Phải dùng số nguyên:

```env
PRO_PRICE_VND=499000
```

## 7. Cấu hình frontend

Trong `FE-learn-english`, kiểm tra `.env.production`:

```env
NEXT_PUBLIC_API_URL=https://API_DOMAIN
```

Ví dụ:

```env
NEXT_PUBLIC_API_URL=https://api.english.xhroudev.com
```

Sau khi thay đổi biến môi trường, build và deploy lại frontend:

```bash
npm run build
```

Frontend không cần lưu:

- Secret SePay.
- API key SePay.
- Thông tin xác thực webhook.

Các thông tin bí mật chỉ được đặt ở backend.

## 8. Cấu hình CORS

Backend cần cho phép domain frontend gọi API.

Thiết lập:

```env
ALLOWED_ORIGINS=https://english.xhroudev.com
```

Nếu có nhiều domain:

```env
ALLOWED_ORIGINS=https://english.xhroudev.com,https://www.english.xhroudev.com
```

Sau đó restart backend.

## 9. Database

Migration đã được thêm:

```text
V19__create_pro_payment_tables.sql
```

Khi backend khởi động, Flyway tạo:

- Cột `users.pro_expires_at`.
- Bảng `payment_orders`.
- Bảng `payment_webhook_events`.

Không cần chạy SQL thủ công nếu Flyway đang bật.

Kiểm tra log backend phải có thông báo migration thành công.

## 10. Kiểm thử trước khi nhận tiền thật

### Kiểm tra tạo đơn

1. Đăng nhập frontend.
2. Nhấn **PRO**.
3. Nhấn **Mở khóa PRO**.
4. Kiểm tra popup hiển thị:

   - QR.
   - Đúng ngân hàng.
   - Đúng số tài khoản.
   - Giá `499.000đ`.
   - Mã chuyển khoản bắt đầu bằng `PRO`.

Nếu frontend báo không thể tạo đơn, kiểm tra backend đã có:

```env
SEPAY_BANK
SEPAY_ACCOUNT_NUMBER
```

### Kiểm tra QR

Quét QR bằng ứng dụng ngân hàng nhưng chưa xác nhận chuyển tiền.

Kiểm tra ứng dụng tự điền:

- Đúng tài khoản nhận.
- Đúng số tiền.
- Đúng nội dung chuyển khoản.

### Kiểm tra Webhook

Trong trang quản trị SePay:

1. Mở webhook vừa tạo.
2. Dùng chức năng gửi thử hoặc Test Mode nếu tài khoản hỗ trợ.
3. Kiểm tra lịch sử webhook.
4. Response hợp lệ phải là HTTP `200` với:

   ```json
   {
     "success": true
   }
   ```

Nếu nhận `401`, kiểm tra:

- Secret trên SePay và backend có giống nhau không.
- SePay đang dùng HMAC-SHA256 hay không.
- Server có đúng giờ không.
- Reverse proxy có làm thay đổi request body không.

### Kiểm tra giao dịch thật

Nên thử với giá thấp trên môi trường test/staging trước.

Nếu thử production:

1. Tạo đơn từ đúng tài khoản người dùng cần nâng cấp.
2. Chuyển đúng số tiền.
3. Giữ nguyên nội dung chuyển khoản.
4. Theo dõi popup.
5. Sau khi webhook tới, popup phải chuyển sang thành công.
6. Kiểm tra database:

   ```sql
   SELECT id, status, payment_code, amount, paid_at
   FROM payment_orders
   ORDER BY created_at DESC;
   ```

7. Kiểm tra hạn PRO:

   ```sql
   SELECT id, email, pro_expires_at
   FROM users
   WHERE email = 'EMAIL_CAN_KIEM_TRA';
   ```

## 11. API đang sử dụng

### Tạo đơn PRO

```http
POST /api/v1/payments/pro/orders
Authorization: Bearer ACCESS_TOKEN
```

### Kiểm tra trạng thái đơn

```http
GET /api/v1/payments/orders/{orderId}
Authorization: Bearer ACCESS_TOKEN
```

Trạng thái:

- `PENDING`: đang chờ thanh toán.
- `PAID`: đã thanh toán và mở khóa PRO.
- `EXPIRED`: đơn đã hết hạn.

### Kiểm tra tài khoản PRO

```http
GET /api/v1/payments/pro/status
Authorization: Bearer ACCESS_TOKEN
```

### Webhook SePay

```http
POST /api/v1/payments/sepay/webhook
X-SePay-Signature: sha256=...
X-SePay-Timestamp: ...
Content-Type: application/json
```

Endpoint webhook không dùng JWT người dùng. Nó dùng chữ ký HMAC từ SePay.

## 12. Xử lý lỗi thường gặp

### Popup không hiện QR

Kiểm tra:

- Backend đang chạy.
- `NEXT_PUBLIC_API_URL` đúng.
- `SEPAY_BANK` và `SEPAY_ACCOUNT_NUMBER` đã được cấu hình.
- CORS cho phép domain frontend.

### Quét QR sai ngân hàng hoặc tài khoản

Kiểm tra:

```env
SEPAY_BANK
SEPAY_ACCOUNT_NUMBER
SEPAY_ACCOUNT_HOLDER
```

Sau đó restart backend và tạo đơn mới.

### Đã nhận tiền nhưng popup vẫn chờ

Kiểm tra:

1. SePay đã nhận được biến động số dư chưa.
2. Webhook có được gọi không.
3. Webhook trả status nào.
4. Secret HMAC có đúng không.
5. Số tiền có đúng hoàn toàn không.
6. Nội dung chuyển khoản có còn nguyên mã `PRO...` không.
7. `accountNumber` trong webhook có trùng `SEPAY_ACCOUNT_NUMBER` không.

### Webhook gửi nhiều lần

Đây là hành vi bình thường khi SePay retry hoặc replay webhook.

Backend lưu `transaction_id` duy nhất trong `payment_webhook_events`, vì vậy một giao dịch không được cộng PRO nhiều lần.

### Người dùng đang có PRO rồi mua thêm

Backend cộng tiếp 365 ngày từ ngày hết hạn hiện tại.

Ví dụ người dùng còn 20 ngày PRO thì sau thanh toán sẽ còn khoảng 385 ngày.

## 13. Checklist trước khi mở thanh toán

- [ ] Tài khoản ngân hàng nhận tiền đã sẵn sàng.
- [ ] Tài khoản ngân hàng đã liên kết với SePay.
- [ ] SePay nhận được biến động số dư.
- [ ] Tiền tố mã thanh toán `PRO` đã cấu hình.
- [ ] Backend production có HTTPS.
- [ ] Webhook URL đã được tạo trên SePay.
- [ ] Webhook dùng HMAC-SHA256.
- [ ] `SEPAY_WEBHOOK_SECRET` trùng với secret trên SePay.
- [ ] `SEPAY_BANK` đúng mã ngân hàng.
- [ ] `SEPAY_ACCOUNT_NUMBER` đúng tài khoản nhận.
- [ ] `NEXT_PUBLIC_API_URL` trỏ đúng backend.
- [ ] `ALLOWED_ORIGINS` chứa domain frontend.
- [ ] Migration V19 chạy thành công.
- [ ] QR hiển thị đúng tiền và nội dung.
- [ ] Webhook test trả `{"success":true}`.
- [ ] Giao dịch thử chuyển đơn sang `PAID`.
- [ ] `users.pro_expires_at` được cập nhật.

## 14. Tài liệu SePay

- Tổng quan Webhook:
  https://developer.sepay.vn/vi/sepay-webhooks
- Tích hợp Webhook:
  https://developer.sepay.vn/vi/sepay-webhooks/tich-hop-webhook
- Xác thực HMAC-SHA256:
  https://developer.sepay.vn/vi/sepay-webhooks/xac-thuc
- Tạo VietQR:
  https://developer.sepay.vn/vi/tien-ich-khac/tao-qr-code
