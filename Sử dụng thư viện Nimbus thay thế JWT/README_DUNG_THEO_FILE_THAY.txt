JWT SPRING BOOT 3 - LAM DUNG THEO FILE 04_JWT.pdf
=================================================

PHAN GIU NGUYEN THEO FILE THAY
--------------------------------
- server.port=8005
- JWT JJWT 0.12.6
- Entity User implements UserDetails
- Models: LoginResponse, LoginUserModel, RegisterUserModel
- UserRepository.findByEmail()
- UserService.allUsers()
- AuthenticationService.signup()/authenticate()
- JwtService
- JwtAuthenticationFilter
- SecurityConfiguration
- AuthenticationController
- UserController
- Bước 9: signup -> login -> JWT -> /users/me -> /users
- Bước 10: login.html -> profile.html -> mainjs.js -> AJAX

THAY DOI DUY NHAT
-----------------
File thầy dùng MySQL.
Máy bạn đang dùng SQL Server nên project này đổi phần datasource sang SQL Server.

DATABASE
--------
Chạy trong SSMS:

IF DB_ID('jwt_springboot3') IS NULL
BEGIN
    CREATE DATABASE jwt_springboot3;
END
GO

CẤU HÌNH SQL SERVER
-------------------
Trong application.properties:

spring.datasource.username=sa
spring.datasource.password=123456

Nếu password sa của bạn khác 123456 thì sửa dòng password.

CHAY
----
1. Import project bằng Maven.
2. Maven -> Update Project.
3. Run As -> Spring Boot App.
4. Server: http://localhost:8005

BƯỚC 9 - TEST THEO FILE
-----------------------
1. Tạo tài khoản:
POST http://localhost:8005/auth/signup

Body JSON:
{
  "email": "admin@gmail.com",
  "password": "123456",
  "fullName": "Nguyen Huu Trung"
}

2. Đăng nhập:
POST http://localhost:8005/auth/login

Body JSON:
{
  "email": "admin@gmail.com",
  "password": "123456"
}

Response có:
{
  "token": "...",
  "expiresIn": 3600000
}

3. Dùng token để gọi:
GET http://localhost:8005/users/me

Authorization:
Bearer <token>

4. Gọi:
GET http://localhost:8005/users/

Authorization:
Bearer <token>

BƯỚC 10 - AJAX THEO FILE
------------------------
Mở:
http://localhost:8005/login

Nhập:
Email: admin@gmail.com
Password: 123456

Bấm Login.

JavaScript lưu token vào localStorage và chuyển sang:
http://localhost:8005/user/profile

Trang profile gọi:
GET /users/me
với Authorization: Bearer token

Logout sẽ xóa token và quay lại /login.
