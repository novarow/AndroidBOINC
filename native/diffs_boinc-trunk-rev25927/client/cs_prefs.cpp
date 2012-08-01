47a48,49
> #include "android_log.h"
> 
333a336,345
> 
> #ifdef ANDROID
> //verify that device is on wifi before making project transfers.
>     if(global_prefs.network_wifi_only && !host_info.host_wifi_online()) {
> 	file_xfers_suspended = true;
> 	if (!recent_rpc) network_suspended = true;
> 	network_suspend_reason = SUSPEND_REASON_WIFI_STATE;
> 	LOGD("supended due to wifi state");
>     }
> #endif
