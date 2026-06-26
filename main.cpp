#include <Arduino.h>
#include <WiFi.h>
#include <time.h>

// --- Configuration ---
const char* ssid       = "YOUR_WIFI_NAME";
const char* password   = "YOUR_WIFI_PASSWORD";

// --- Pin Definitions ---
const int STEPPER_PIN = 12; // Pin to rotate the medicine tray
const int BUZZER_PIN = 14;  // Pin for the alarm alert

// --- NTP Server Settings (For Real Time) ---
const char* ntpServer = "pool.ntp.org";
const long  gmtOffset_sec = 19800; // Offset for IST (5.5 hours * 3600). Change this for your timezone.
const int   daylightOffset_sec = 0;

// --- Schedule ---
const int DISPENSE_HOUR = 8;  // Dispense at 8:00 AM
const int DISPENSE_MINUTE = 30; // Dispense at 8:30 AM
bool hasDispensedToday = false;

void setup() {
  Serial.begin(115200);
  
  // Initialize hardware pins
  pinMode(STEPPER_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);

  // Connect to WiFi
  Serial.print("Connecting to WiFi");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi connected!");

  // Initialize Network Time Protocol (NTP)
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  Serial.println("Time configured via NTP.");
}

void dispenseMedicine() {
  Serial.println("--- DISPENSING MEDICINE ---");
  
  // Sound the alarm
  for(int i=0; i<3; i++) {
    digitalWrite(BUZZER_PIN, HIGH);
    delay(500);
    digitalWrite(BUZZER_PIN, LOW);
    delay(500);
  }
  
  // Trigger motor to drop medicine
  digitalWrite(STEPPER_PIN, HIGH);
  delay(1000); // Simulate motor turning 
  digitalWrite(STEPPER_PIN, LOW);
  
  hasDispensedToday = true;
}

void loop() {
  struct tm timeinfo;
  if(!getLocalTime(&timeinfo)){
    Serial.println("Failed to obtain time");
    delay(5000);
    return;
  }

  int currentHour = timeinfo.tm_hour;
  int currentMinute = timeinfo.tm_min;

  // Reset the dispense flag at midnight
  if (currentHour == 0 && currentMinute == 0) {
    hasDispensedToday = false;
  }

  // Check if it is time to dispense
  if (currentHour == DISPENSE_HOUR && currentMinute == DISPENSE_MINUTE && !hasDispensedToday) {
    dispenseMedicine();
  }

  // Print current time every 10 seconds for debugging
  Serial.println(&timeinfo, "%A, %B %d %Y %H:%M:%S");
  delay(10000); 
}
