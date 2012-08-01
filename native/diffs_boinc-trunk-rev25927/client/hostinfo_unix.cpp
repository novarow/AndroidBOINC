156a157,158
> #include "android_log.h"
> 
214a217,251
> #elif ANDROID
> 	// using /sys/class/power_supply/*/online
> 	// power supplies are both ac and usb!
> 	char acpath[1024];
> 	snprintf(acpath,sizeof(acpath),"/sys/class/power_supply/ac/online");
> 	char usbpath[1024];
> 	snprintf(usbpath,sizeof(usbpath),"/sys/class/power_supply/usb/online");
> 
> 	FILE *fsysac = fopen(acpath, "r");
> 	FILE *fsysusb = fopen(usbpath, "r");
> 	int aconline = 0;
> 	int usbonline = 0;
> 	bool power_supply_online = false;
> 
> 	if(fsysac) {
> 		(void) fscanf(fsysac, "%d", &aconline);
> 		fclose(fsysac);
> 	}
> 
> 	if(fsysusb) {
> 		(void) fscanf(fsysusb, "%d", &usbonline);
> 		fclose(fsysusb);
> 	}
> 
> 	if((aconline == 1) || (usbonline == 1)){
> 		power_supply_online = true;
> 		char msg[1024];
> 		snprintf(msg,sizeof(msg),"power supply online! status for usb: %d and ac: %d",usbonline,aconline);
> 		LOGD(msg);
> 	} else {
> 		LOGD("running on batteries");
> 	}
> 
> 	return !power_supply_online;
> 
1411a1449,1451
> #ifdef ANDROID
>     safe_strcpy(os_name, "Android");
> #else
1412a1453
> #endif //ANDROID
