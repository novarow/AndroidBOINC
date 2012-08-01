#ifdef ANDROID
//setup logcat for debugging android application via DDMS
#include "android/log.h"
#define LOG_TAG "BOINC"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
//setup cwd
#define CWD "/data/data/edu.berkeley.boinc/client"
#endif

