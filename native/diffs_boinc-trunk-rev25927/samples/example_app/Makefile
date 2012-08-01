7,8c7,8
< CXXFLAGS = -g \
< 	-Wall -W -Wshadow -Wpointer-arith -Wcast-qual -Wcast-align -Wwrite-strings -fno-common \
---
> CXXFLAGS = --sysroot=/home/novarow/tools/android-4-tc/sysroot \
>     -DANDROID \
50c50
< 	$(CXX) $(CXXFLAGS) -o uc2 uc2.o libstdc++.a -pthread \
---
> 	$(CXX) $(CXXFLAGS) -o uc2 uc2.o libstdc++.a  \
52c52,53
< 	$(BOINC_LIB_DIR)/libboinc.a
---
> 	$(BOINC_LIB_DIR)/libboinc.a \
> 	-llog
