#!/bin/bash
set -e

# ============================================================================
# WebTrackDB - MSI 安装包构建脚本
# ============================================================================

# ==================== 应用信息 ====================
APP_NAME="WebTrackDB"
APP_VERSION="1.0"
APP_DESC="WebTrackDB 网络监控系统"
MAIN_CLASS="Main"

# ==================== 目录结构 ====================
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/manage/src"
LIB_DIR="$PROJECT_DIR/manage/lib"
WEB_DIR="$PROJECT_DIR/web"
DB_DIR="$PROJECT_DIR/manage/database"

# 输出目录
DIST_DIR="$PROJECT_DIR/dist"
BUILD_DIR="$PROJECT_DIR/build"
CLASSES_DIR="$BUILD_DIR/classes"
PACK_DIR="$BUILD_DIR/pack"

# ==================== 工具路径（可通过环境变量覆盖）====================
JAVA_HOME="${JAVA_HOME:-/d/JDK-21}"
JAVAFX_JMODS="${JAVAFX_JMODS:-$HOME/javafx-jmods-21.0.2}"
WIX_HOME="${WIX_HOME:-/c/Program Files (x86)/WiX Toolset v3.14}"

export PATH="$JAVA_HOME/bin:$WIX_HOME/bin:$PATH"

# ==================== 颜色输出 ====================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

info()    { echo -e "${CYAN}[信息]${NC} $1"; }
success() { echo -e "${GREEN}[成功]${NC} $1"; }
warn()    { echo -e "${YELLOW}[警告]${NC} $1"; }
error()   { echo -e "${RED}[错误]${NC} $1"; }

# 构建 classpath（Windows Git Bash 下用 : 代替 ;，避免被解释为命令分隔符）
build_classpath() {
    local jars=()
    for jar in "$LIB_DIR"/*.jar; do
        [ -f "$jar" ] || continue
        jars+=("$jar")
    done
    # 使用 IFS 拼接，: 作为分隔符（JDK 在 Windows 上也支持）
    local IFS=":"
    echo "${jars[*]}"
}

# ==================== 函数：前置检查 ====================
check_prerequisites() {
    echo ""
    echo "============================================"
    echo "  $APP_NAME - MSI 打包脚本"
    echo "============================================"
    echo ""

    # 检查 JDK
    if [ ! -f "$JAVA_HOME/bin/javac" ]; then
        error "找不到 javac: $JAVA_HOME/bin/javac"
        error "请设置 JAVA_HOME 环境变量指向 JDK 21 安装目录"
        exit 1
    fi
    info "JDK: $("$JAVA_HOME/bin/java" --version 2>&1 | head -1)"

    # 检查 JavaFX jmods
    if [ ! -d "$JAVAFX_JMODS" ]; then
        error "找不到 JavaFX jmods: $JAVAFX_JMODS"
        echo ""
        echo "请按以下步骤操作："
        echo "  1. 访问 https://gluonhq.com/products/javafx/"
        echo "  2. 下载 \"JavaFX Windows x64 SDK\" (版本 21.0.x)"
        echo "  3. 解压后确保 jmods 目录在: $JAVAFX_JMODS"
        echo "  4. 或通过环境变量指定路径: export JAVAFX_JMODS=/your/path"
        exit 1
    fi
    info "JavaFX jmods: $JAVAFX_JMODS"

    # 检查 WiX Toolset
    if [ ! -d "$WIX_HOME/bin" ]; then
        error "找不到 WiX Toolset: $WIX_HOME/bin"
        echo ""
        echo "请按以下步骤操作："
        echo "  1. 访问 https://wixtoolset.org/releases/"
        echo "  2. 下载并安装 WiX Toolset v3.14"
        echo "  3. 或通过环境变量指定路径: export WIX_HOME=/your/wix/path"
        exit 1
    fi
    info "WiX Toolset: $WIX_HOME"

    # 检查源码
    if [ ! -f "$SRC_DIR/Main.java" ]; then
        error "找不到源码目录: $SRC_DIR"
        exit 1
    fi

    # 检查依赖
    local jar_count
    jar_count=$(find "$LIB_DIR" -maxdepth 1 -name "*.jar" | wc -l)
    if [ "$jar_count" -eq 0 ]; then
        error "找不到依赖 JAR 文件: $LIB_DIR"
        exit 1
    fi
    info "依赖 JAR: $jar_count 个"

    # 检查 web 资源
    if [ ! -d "$WEB_DIR" ]; then
        error "找不到 web 资源目录: $WEB_DIR"
        exit 1
    fi
    info "Web 资源: $WEB_DIR"

    # 检查数据库脚本
    if [ -f "$DB_DIR/init.sql" ]; then
        info "数据库脚本: $DB_DIR/init.sql"
    else
        warn "未找到数据库初始化脚本"
    fi

    success "前置检查全部通过"
}

# ==================== 函数：清理 ====================
clean() {
    echo ""
    info "清理旧的构建产物..."
    rm -rf "$DIST_DIR" "$BUILD_DIR"
    mkdir -p "$DIST_DIR" "$CLASSES_DIR"
    success "清理完成"
}

# ==================== 函数：编译源码 ====================
compile() {
    echo ""
    info "编译 Java 源码..."

    # 收集所有 .java 文件
    local src_files
    src_files=$(find "$SRC_DIR" -name "*.java" -type f)
    local file_count
    file_count=$(echo "$src_files" | wc -l)

    info "源码文件: $file_count 个"
    info "编码: GBK"

    # 编译（: 作为 classpath 分隔符）
    local classpath
    classpath=$(build_classpath)

    "$JAVA_HOME/bin/javac" \
        -encoding GBK \
        -cp "$classpath" \
        -d "$CLASSES_DIR" \
        $src_files

    success "编译完成 → $CLASSES_DIR"
}

# ==================== 函数：打包 JAR ====================
create_jar() {
    echo ""
    info "创建应用 JAR..."

    # 生成 MANIFEST.MF
    local manifest="$BUILD_DIR/MANIFEST.MF"
    echo "Manifest-Version: 1.0" > "$manifest"
    echo "Main-Class: $MAIN_CLASS" >> "$manifest"
    echo "Created-By: WebTrackDB Build Script" >> "$manifest"

    cd "$CLASSES_DIR"
    "$JAVA_HOME/bin/jar" cfm "$PACK_DIR/${APP_NAME}.jar" "$manifest" .
    cd "$PROJECT_DIR"

    success "${APP_NAME}.jar 创建完成"
}

# ==================== 函数：准备 web 资源 ====================
prepare_web() {
    echo ""
    info "准备 web 资源..."

    # 复制整个 web 目录
    cp -r "$WEB_DIR" "$PACK_DIR/web"

    # 将编译后的 class 文件同步到 WEB-INF/classes（Tomcat WebappClassLoader 需要）
    local web_classes="$PACK_DIR/web/WEB-INF/classes"
    rm -rf "$web_classes"
    mkdir -p "$web_classes"
    cp -r "$CLASSES_DIR"/* "$web_classes/"

    success "web 资源已准备（含 WEB-INF/classes）"
}

# ==================== 函数：复制依赖 ====================
copy_dependencies() {
    echo ""
    info "复制依赖 JAR..."

    local copied=0
    local skipped=0

    for jar in "$LIB_DIR"/*.jar; do
        [ -f "$jar" ] || continue
        local name
        name=$(basename "$jar")

        # JavaFX 模块已由 jlink 运行时提供，不需要复制 JAR
        case "$name" in
            javafx-*.jar|javafx.*.jar)
                skipped=$((skipped + 1))
                ;;
            *)
                cp "$jar" "$PACK_DIR/"
                copied=$((copied + 1))
                ;;
        esac
    done

    success "依赖 JAR: 复制 $copied 个, 跳过 JavaFX $skipped 个"

    # 复制数据库初始化脚本
    if [ -f "$DB_DIR/init.sql" ]; then
        mkdir -p "$PACK_DIR/database"
        cp "$DB_DIR/init.sql" "$PACK_DIR/database/"
        success "数据库脚本已复制"
    fi
}

# ==================== 函数：jlink 裁剪 JRE ====================
create_runtime() {
    echo ""
    info "jlink: 生成精简 JRE（含 JavaFX）..."

    local runtime_dir="$BUILD_DIR/runtime"
    rm -rf "$runtime_dir"

    "$JAVA_HOME/bin/jlink" \
        --module-path "$JAVAFX_JMODS:$JAVA_HOME/jmods" \
        --add-modules java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.naming,java.scripting,java.sql,java.xml,java.net.http,java.security.jgss,jdk.unsupported,jdk.crypto.ec,jdk.management,jdk.xml.dom,javafx.base,javafx.controls,javafx.graphics,javafx.media,javafx.web \
        --output "$runtime_dir" \
        --strip-debug \
        --compress zip-6 \
        --no-header-files \
        --no-man-pages

    echo ""
    info "运行时包含的模块:"
    "$runtime_dir/bin/java" --list-modules

    success "运行时镜像生成完成"
}

# ==================== 函数：jpackage 生成 MSI ====================
create_msi() {
    echo ""
    info "jpackage: 生成 MSI 安装包..."

    "$JAVA_HOME/bin/jpackage" \
        --type msi \
        --name "$APP_NAME" \
        --app-version "$APP_VERSION" \
        --input "$PACK_DIR" \
        --main-jar "${APP_NAME}.jar" \
        --main-class "$MAIN_CLASS" \
        --dest "$DIST_DIR" \
        --runtime-image "$BUILD_DIR/runtime" \
        --win-shortcut \
        --win-menu \
        --win-dir-chooser \
        --description "$APP_DESC" \
        --vendor "WebTrackDB"

    echo ""
    success "MSI 安装包生成完成"
}

# ==================== 函数：输出结果 ====================
print_summary() {
    echo ""
    echo "============================================"
    echo "  构建完成！"
    echo "============================================"
    echo ""

    if [ -f "$DIST_DIR/${APP_NAME}-${APP_VERSION}.msi" ]; then
        local size
        size=$(du -h "$DIST_DIR/${APP_NAME}-${APP_VERSION}.msi" | cut -f1)
        success "安装包: $DIST_DIR/${APP_NAME}-${APP_VERSION}.msi ($size)"
    else
        # 某些版本的 jpackage 命名格式不同
        ls -lh "$DIST_DIR"/*.msi 2>/dev/null && success "安装包已生成至 $DIST_DIR" || error "MSI 文件未生成，请检查上方日志"
    fi

    echo ""
    echo "构建目录: $BUILD_DIR"
    echo ""
}

# ==================== 主流程 ====================
main() {
    check_prerequisites
    clean
    compile
    create_jar
    prepare_web
    copy_dependencies
    create_runtime
    create_msi
    print_summary
}

main
