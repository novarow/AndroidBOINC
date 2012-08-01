57a58,86
> #include "android_log.h"
> 
> #if defined ANDROID
> // Returns TRUE if host is currently using a wifi connection
> // used on Android devices to prevent usage of data plans.
> // if value cant be read, default return false
> bool HOST_INFO::host_wifi_online() {
>         char wifipath[1024];
>         snprintf(wifipath,sizeof(wifipath),"/sys/class/net/eth0/operstate");
> 
>         FILE *fsyswifi = fopen(wifipath, "r");
>         char wifi_state[64];
> 
>         bool wifi_online = false;
> 
>         if(fsyswifi) {
>                 (void) fscanf(fsyswifi, "%s", &wifi_state);
>                 fclose(fsyswifi);
>         }
> 
>         if((strcmp(wifi_state,"up")) == 0) { //operstate = up
> 	LOGD("wifi is online");
>                 wifi_online = true;
>         }
> 
>         return wifi_online;
> }
> #endif //ANDROID
> 
96,97c125,126
<     sprintf(buf, "%d%.15e%s%s%f%s",
<         getpid(), dtime(), domain_name, ip_addr, d_free, salt
---
>     sprintf(buf, "%f%s%s%f%s",
>         dtime(), domain_name, ip_addr, d_free, salt
