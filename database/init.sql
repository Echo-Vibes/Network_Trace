-- =====================================================
-- 上网记录跟踪软件 - 数据库初始化脚本
-- 适用数据库：SQL Server 2012+
-- 执行方式：VS Code MSSQL 扩展 或 SSMS
-- =====================================================

-- 第1步：创建数据库（如果不存在）
IF NOT EXISTS (SELECT *
FROM sys.databases
WHERE name = 'WebTrackDB')
BEGIN
    CREATE DATABASE WebTrackDB;
    PRINT '数据库 WebTrackDB 创建成功';
END
ELSE
BEGIN
    PRINT '数据库 WebTrackDB 已存在';
END
GO

-- 第2步：切换到目标数据库
USE WebTrackDB;
GO

-- 第3步：创建历史记录表
IF NOT EXISTS (SELECT *
FROM sys.tables
WHERE name = 'history')
BEGIN
    CREATE TABLE history
    (
        id INT IDENTITY(1,1) PRIMARY KEY,
        user_name NVARCHAR(50) NOT NULL,
        url NVARCHAR(500) NOT NULL,
        visit_time DATETIME NOT NULL
    );
    PRINT '表 history 创建成功';
END
ELSE
BEGIN
    PRINT '表 history 已存在';
END
GO

-- 第4步：创建黑名单表
IF NOT EXISTS (SELECT *
FROM sys.tables
WHERE name = 'Blacklist')
BEGIN
    CREATE TABLE Blacklist
    (
        id INT IDENTITY(1,1) PRIMARY KEY,
        url NVARCHAR(200) NOT NULL,
        add_time DATETIME DEFAULT GETDATE()
    );
    PRINT '表 Blacklist 创建成功';
END
ELSE
BEGIN
    PRINT '表 Blacklist 已存在';
END
GO

-- 第5步：验证表创建成功
PRINT '';
PRINT '=== 表结构验证 ===';
SELECT TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE';
GO

PRINT '';
PRINT '========================================';
PRINT '数据库初始化完成！';
PRINT '黑名单表为空，请根据需要自行添加黑名单域名';
PRINT '========================================';