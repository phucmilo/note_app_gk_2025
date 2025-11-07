# Note App GK 2025

Ứng dụng quản lý sản phẩm (Note) với Firebase Authentication và Realtime Database.

## Yêu cầu

- Android Studio
- JDK 8+
- Firebase project đã được thiết lập

## Cài đặt

1. Tạo project trên Firebase Console: https://console.firebase.google.com/

2. Thêm Android app vào Firebase project với package name: `com.noteapp.gk2025`

3. Tải file `google-services.json` từ Firebase Console và đặt vào thư mục `app/`

4. Trong Firebase Console, thiết lập:
   - **Authentication**: Bật Email/Password
   - **Realtime Database**: Tạo database ở chế độ test hoặc production

5. Tạo node `users` trong Realtime Database với cấu trúc:
   ```
   users/
     {userId}/
       role: "admin" hoặc "user"
   ```

6. Sync project với Gradle files

## Cấu trúc dự án

- `data/model/`: Data models (Product)
- `data/repository/`: Repository classes cho Firebase operations
- `ui/login/`: Màn hình đăng nhập
- `ui/admin/`: Màn hình quản lý sản phẩm (admin)
- `ui/user/`: Màn hình xem sản phẩm (user)
- `ui/adapter/`: RecyclerView adapters
- `util/`: Utility classes (FileHelper cho base64 conversion)

## Chức năng

### Admin
- Đăng nhập/đăng xuất
- Thêm sản phẩm (title, description, file)
- Sửa sản phẩm
- Xóa sản phẩm
- Upload file/hình ảnh (chuyển đổi sang base64 và lưu vào Realtime Database)
- Hiển thị danh sách sản phẩm real-time

### User
- Đăng nhập/đăng xuất
- Xem danh sách sản phẩm (chỉ đọc)

## Cấu trúc dữ liệu

### Product (Realtime Database node: `products`)
```json
{
  "title": "Tên sản phẩm",
  "description": "Gia: 300000; Loai: Thoi trang nu",
  "file": "base64_encoded_string_here"
}
```

### User (Realtime Database node: `users`)
```json
{
  "role": "admin" hoặc "user"
}
```

## Lưu ý

- File `google-services.json` cần được thay thế bằng file thực từ Firebase Console
- Đảm bảo Firebase project đã được cấu hình đúng với package name
- Cần tạo user trong Firebase Authentication và set role trong Realtime Database
- **File/hình ảnh được lưu dưới dạng base64 string** trong Realtime Database (không dùng Storage)
- Base64 string có thể rất dài, nên cân nhắc giới hạn kích thước file khi upload

## Công nghệ sử dụng

- **Firebase Authentication**: Đăng nhập/đăng xuất
- **Firebase Realtime Database**: Lưu trữ dữ liệu sản phẩm và user roles
- **Base64 Encoding**: Chuyển đổi hình ảnh thành string để lưu trong database
