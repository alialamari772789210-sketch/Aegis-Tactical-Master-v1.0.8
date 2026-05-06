#!/usr/bin/env sh

APP_HOME=$(cd "$(dirname "$0")"; pwd -P)

CLASSPATH=$APP_HOME/gradle-wrapper.jar
WRAPPER_PROPERTIES=$APP_HOME/gradle/wrapper/gradle-wrapper.properties

if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" -Dorg.gradle.wrapper.properties="$WRAPPER_PROPERTIES" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
