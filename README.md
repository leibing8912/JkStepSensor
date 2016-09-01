#JkStepSensor
一个基于传感器开发的计步器,内置缓存模块,可以根据日期查询目标日行走步数.根据开源计步算法,做了相关优化
具体如下:

*  连续运动一段时间才开始计步,屏蔽细微移动或者驾车时震动所带来的干扰.
*  停止运动一段时间后,需要连续运动一段时间才会计步.
*  调整计步算法以及计步精度.
*  添加缓存机制.
*  将Service置于一个独立的进程进行计步并通过messenger进行进程间传输.


### License
Copyright 2016 leibing

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.