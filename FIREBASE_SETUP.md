# Hướng dẫn thiết lập Firebase

## Bước 1: Tạo Firebase Project

1. Truy cập https://console.firebase.google.com/
2. Click "Add project" hoặc chọn project có sẵn
3. Đặt tên project và làm theo hướng dẫn

## Bước 2: Thêm Android App

1. Trong Firebase Console, click biểu tượng Android
2. Nhập package name: `com.noteapp.gk2025`
3. Tải file `google-services.json`
4. Đặt file vào thư mục `app/` (thay thế file placeholder hiện có)

## Bước 3: Thiết lập Authentication

1. Vào **Authentication** > **Sign-in method**
2. Bật **Email/Password**
3. Click **Save**

## Bước 4: Thiết lập Realtime Database

1. Vào **Realtime Database** (không phải Firestore)
2. Click **Create database**
3. Chọn chế độ:
   - **Test mode** (cho development) - cho phép đọc/ghi trong 30 ngày
   - Hoặc **Production mode** (cho production) - cần cấu hình rules
4. Chọn location gần nhất
5. Click **Enable**

### Cấu trúc dữ liệu trong Realtime Database

Database sẽ có cấu trúc như sau:
```
{
  "products": {
    "-Nxxxxx": {
      "title": "Tên sản phẩm",
      "description": "Gia: 300000; Loai: Thoi trang nu",
      "file": "base64_string_here"
    }
  },
  "users": {
    "user_uid_here": {
      "role": "admin"
    }
  }
}
```

### Tạo node `users` trong Realtime Database

1. Vào **Realtime Database** > **Data**
2. Click **Add node** hoặc sử dụng console
3. Node name: `users`
4. Tạo child node với key = User UID từ Authentication
5. Thêm field:
   - Field: `role`
   - Type: `string`
   - Value: `admin` hoặc `user`

### Node `products` sẽ được tạo tự động khi app chạy

## Bước 5: Tạo User Admin

1. Vào **Authentication** > **Users**
2. Click **Add user**
3. Nhập email và password
4. Click **Add user**
5. Copy **User UID**
6. Vào **Realtime Database** > **Data**
7. Tạo node trong `users` với:
   - Key = User UID vừa copy
   - Field `role` = `admin`

## Bước 6: Cấu hình Security Rules (Production)

### Realtime Database Rules:
```
{
  "rules": {
    "products": {
      ".read": "auth != null",
      ".write": "auth != null && root.child('users').child(auth.uid).child('role').val() == 'admin'"
    },
    "users": {
      "$uid": {
        ".read": "auth != null && ($uid == auth.uid || root.child('users').child(auth.uid).child('role').val() == 'admin')",
        ".write": "false"
      }
    }
  }
}
```

**Lưu ý:** 
- File/hình ảnh được lưu dưới dạng **base64 string** trong field `file` của mỗi product
- Không cần thiết lập Firebase Storage
- Base64 string có thể rất dài, nên cân nhắc giới hạn kích thước file

## Kiểm tra

1. Sync project với Gradle
2. Build project
3. Chạy app trên emulator/device
4. Đăng nhập với user admin đã tạo
5. Kiểm tra các chức năng CRUD
6. Thử upload hình ảnh và kiểm tra xem có được lưu dưới dạng base64 trong Realtime Database
