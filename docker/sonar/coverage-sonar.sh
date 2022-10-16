#!/usr/bin/env bash

SONAR_HOST=http://localhost:9000
SONAR_LOGIN=admin
SONAR_PASSWORD=admin
JACOCO_DIRECTORY_XML_PATH=build/reports/jacoco/jacoco.xml
TEST_DIRECTORY_PATH=src/test
APP_PATH=$1

cd $APP_PATH
bash ./gradlew test sonarqube \
  -Dsonar.host.url="${SONAR_HOST}" \
  -Dsonar.login="${SONAR_LOGIN}" \
  -Dsonar.password="${SONAR_PASSWORD}" \
  -Dsonar.coverage.jacoco.xmlReportPaths="${JACOCO_DIRECTORY_XML_PATH}" \
  -Dsonar.tests="${TEST_DIRECTORY_PATH}" \
  -Dsonar.scm.disabled=True \
  -Dsonar.verbose=true
