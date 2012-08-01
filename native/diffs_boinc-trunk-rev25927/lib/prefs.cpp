81a82
>     network_wifi_only = true;
116a118
>     if (network_wifi_only) return true;
237a240
>     network_wifi_only = false;
253a257
>     network_wifi_only = true;
613c617,618
<         "   <override_file_present>%d</override_file_present>\n",
---
>         "   <override_file_present>%d</override_file_present>\n"
> 	"   <network_wifi_only>%d</network_wifi_only>\n",
646c651,652
<         override_file_present?1:0
---
>         override_file_present?1:0,
> 	network_wifi_only?1:0
807a814,816
>     }
>     if (mask.network_wifi_only) {
>         f.printf("   <network_wifi_only>%d</network_wifi_only>\n", network_wifi_only?1:0 );
