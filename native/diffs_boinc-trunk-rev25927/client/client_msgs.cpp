41a42,43
> #include "android_log.h"
> 
119a122,126
> #ifdef ANDROID // print message to Logcat
>     char amessage[2048];
>     snprintf(amessage,sizeof(amessage),"client_msgs: %s",message);
>     LOGD(amessage);
> #endif //ANDROID
