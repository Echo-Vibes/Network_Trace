#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
DIST_DIR="$PROJECT_DIR/dist"
PACK_DIR="$DIST_DIR/pack"
CLASSES_DIR="$PROJECT_DIR/manage/out/production/Network_Trace"
LIB_DIR="$PROJECT_DIR/manage/lib"
WEB_DIR="$PROJECT_DIR/web"
WIX_HOME="/c/Program Files (x86)/WiX Toolset v3.14"

# ==================== 配置路径 ====================
JAVA_HOME="/c/Program Files/Java/jdk-21.0.2"
# JavaFX jmods (需从 https://gluonhq.com/products/javafx/ 下载 "JavaFX Windows x64 SDK (jmods)")
JAVAFX_JMODS="$HOME/javafx-jmods-21.0.2"

export PATH="$WIX_HOME/bin:$JAVA_HOME/bin:$PATH"

# ==================== 前置检查 ====================
if [ ! -d "$JAVAFX_JMODS" ]; then
  echo "[错误] 找不到 JavaFX jmods: $JAVAFX_JMODS"
  echo ""
  echo "请按以下步骤操作:"
  echo "  1. 访问 https://gluonhq.com/products/javafx/"
  echo "  2. 下载 \"JavaFX Windows x64 SDK\" (版本 21.0.x)"
  echo "  3. 解压后确保目录为: $JAVAFX_JMODS"
  echo "  4. 或修改脚本中的 JAVAFX_JMODS 变量"
  exit 1
fi

if [ ! -f "$CLASSES_DIR/Main.class" ]; then
  echo "[错误] 找不到编译产物，请先在 IDE 中执行 Build → Rebuild Project"
  exit 1
fi

# ==================== 清理 ====================
echo "=== 清理 ==="
rm -rf "$DIST_DIR"
mkdir -p "$PACK_DIR"

# ==================== 打包 ====================
echo "=== 创建 MANIFEST.MF ==="
echo "Main-Class: Main" > "$DIST_DIR/MANIFEST.MF"

echo "=== 创建应用 JAR ==="
cd "$CLASSES_DIR"
jar cfm "$PACK_DIR/WebTrackDB.jar" "$DIST_DIR/MANIFEST.MF" .
cd "$PROJECT_DIR"

echo "=== 复制依赖 JAR（排除 JavaFX，已由 jlink 提供） ==="
for jar in "$LIB_DIR"/*.jar; do
  name=$(basename "$jar")
  case "$name" in
    javafx-*.jar|javafx.*.jar) ;;  # JavaFX 已在运行时镜像中
    *) cp "$jar" "$PACK_DIR/" ;;
  esac
done

echo "=== 复制 web 资源 ==="
cp -r "$WEB_DIR" "$PACK_DIR/web"

# ==================== jlink: 生成精简 JRE ====================
echo "=== jlink: 生成精简 JRE（含 JavaFX） ==="
JLINK_OUTPUT="$DIST_DIR/runtime"
rm -rf "$JLINK_OUTPUT"

"$JAVA_HOME/bin/jlink" \
  --module-path "$JAVAFX_JMODS:$JAVA_HOME/jmods" \
  --add-modules java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.scripting,java.sql,java.xml,java.net.http,java.security.jgss,jdk.unsupported,jdk.crypto.ec,jdk.jsobject,jdk.management,jdk.xml.dom,javafx.base,javafx.controls,javafx.graphics,javafx.media,javafx.web \
  --output "$JLINK_OUTPUT" \
  --strip-debug \
  --compress zip-6 \
  --no-header-files \
  --no-man-pages

echo "运行时模块:"
"$JLINK_OUTPUT/bin/java" --list-modules

# ==================== jpackage: 生成安装包 ====================
echo "=== jpackage: 生成 MSI 安装包 ==="
jpackage \
  --type msi \
  --name WebTrackDB \
  --app-version 1.0 \
  --input "$PACK_DIR" \
  --main-jar WebTrackDB.jar \
  --main-class Main \
  --dest "$DIST_DIR" \
  --runtime-image "$JLINK_OUTPUT" \
  --win-shortcut \
  --win-menu \
  --win-dir-chooser \
  --description "WebTrackDB 网络监控系统"

echo "=== 完成 ==="
ls -lh "$DIST_DIR"/*.msi 2>/dev/null || ls -lh "$DIST_DIR"