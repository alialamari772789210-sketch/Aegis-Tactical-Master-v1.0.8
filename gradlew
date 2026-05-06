#!/usr/bin/env sh

APP_HOME=$(cd "$(dirname "$0")"; pwd -P)

# تم التعديل هنا ليقرأ من الجذر
CLASSPATH=$APP_HOME/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
