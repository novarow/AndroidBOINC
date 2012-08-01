68a69,70
> #include "android_log.h"
> 
83a86,87
> #elif defined (ANDROID)
> 	LOGD(evt_msg);
112a117,118
> #elif defined (ANDROID)
> 	LOGD(evt_msg);
132a139,140
> #elif defined (ANDROID)
>         LOGD(evt_msg);
370a379,387
> 
> #ifdef ANDROID
>     chdir(CWD); //CWD defined at lib/android_log.h
>     char ccwd[1024];
>     getcwd(ccwd,sizeof(ccwd));
>     char msg[1024];
>     snprintf(msg,sizeof(msg),"Hello Logcat! cwd at: %s", ccwd);
>     LOGD(msg);
> #endif
