#!/usr/bin/env sh

APP_HOME=$(cd "$(dirname "$0")"; pwd -P)
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# تصحيح علامات التنصيص ورفع كفاءة الذاكرة لبناء مكتبات C++
DEFAULT_JVM_OPTS="-Xmx2048m -Xms512m -XX:MaxMetaspaceSize=512m"

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# التنفيذ النظيف بدون تداخل العلامات
exec "$JAVACMD" $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
