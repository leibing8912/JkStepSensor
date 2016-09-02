#JkStepSensor
一个基于传感器开发的计步器,内置缓存模块,可以根据日期查询目标日行走步数.根据开源计步算法,做了相关优化
具体如下:

*  连续运动一段时间才开始计步,屏蔽细微移动或者驾车时震动所带来的干扰.
*  停止运动一段时间后,需要连续运动一段时间才会计步.
*  调整计步算法以及计步精度.
*  添加缓存机制.
*  将Service置于一个独立的进程进行计步并通过messenger进行进程间传输.
*  如非手动停止服务,服务会在销毁时重新启动服务.


### Usage

本人已将该项目放入Maven jcenter仓库,简化了该项目的使用,大家如需集成,请往下看:


* 添加依赖代码,代码如下:

gradle:
```java
dependencies {
    compile 'cn.jianke.jkstepsensor:app:1.0.5'
}
```
maven:
```java
<dependency>
  <groupId>cn.jianke.jkstepsensor</groupId>
  <artifactId>app</artifactId>
  <version>1.0.5</version>
  <type>pom</type>
</dependency>
```

lvy:
```java
<dependency org='cn.jianke.jkstepsensor' name='app' rev='1.0.5'>
  <artifact name='$AID' ext='pom'></artifact>
</dependency>
```

* 调用代码如下:

```java
/**
 * @className: MainActivity
 * @classDescription: 计步首页
 * @author: leibing
 * @createTime: 2016/09/02
 */
public class MainActivity extends AppCompatActivity implements Handler.Callback,View.OnClickListener{
    // 显示计步
    private TextView mStepCountTv,mContentTipTv,mStepTipTv;
    // 开始停止计步按钮
    private Button mStartStepBtn, mStopStepBtn;
    // 用于与计步服务通信
    private Messenger messenger;
    private Messenger replyMessenger = new Messenger(new Handler(this));
    private Handler delayHandler = new Handler(this);
    // 循环取当前时刻步数的间隔时间
    private long TIME_INTERVAL = 1000;
    // 确定是否退出app
    private int i = 0;
    // 服务连接
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                // 向服务端发送消息(Messenger通信)
                messenger = new Messenger(service);
                Message msg = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                msg.replyTo = replyMessenger;
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case Constant.MSG_FROM_SERVER:
                // 收到从服务端发来的计步数
                int stepCount = message.getData().getInt(StepService.STEP_KEY);
                // 更新界面上的步数
                mStepTipTv.setVisibility(View.VISIBLE);
                mContentTipTv.setVisibility(View.VISIBLE);
                mStepCountTv.setTextColor(getResources().getColor(cn.jianke.jkstepsensor.R.color.colorPrimary));
                mStepCountTv.setText(stepCount + "");
                // 循环向服务端请求数据
                delayHandler.sendEmptyMessageDelayed(Constant.REQUEST_SERVER, TIME_INTERVAL);
                break;
            case Constant.REQUEST_SERVER:
                try {
                    // 向服务端发送消息(Messenger通信)
                    Message serverMsg = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                    serverMsg.replyTo = replyMessenger;
                    // bundle添加Notification配置信息（包括内容、标题、是否不可取消等）
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constant.CONTENT_KEY, "今日行走");
                    bundle.putSerializable(Constant.TICKER_KEY, "健客计步");
                    bundle.putSerializable(Constant.CONTENTTITLE_KEY, "健客计步");
                    bundle.putSerializable(Constant.PENDINGCLASS_KEY, MainActivity.class);
                    bundle.putSerializable(Constant.ISONGOING_KEY, true);
                    bundle.putSerializable(Constant.ICON_KEY, cn.jianke.jkstepsensor.R.mipmap.icon);
                    bundle.putSerializable(Constant.NOTIFYID_KEY, cn.jianke.jkstepsensor.R.string.app_name);
                    serverMsg.setData(bundle);
                    messenger.send(serverMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // findView
        mStepCountTv = (TextView) findViewById(R.id.tv_step_count);
        mContentTipTv = (TextView) findViewById(R.id.tv_content_tip);
        mStepTipTv = (TextView) findViewById(R.id.tv_step_tip);
        mStartStepBtn = (Button) findViewById(R.id.btn_start_step);
        mStopStepBtn = (Button) findViewById(R.id.btn_stop_step);
        // onClick
        findViewById(R.id.btn_start_step).setOnClickListener(this);
        findViewById(R.id.btn_stop_step).setOnClickListener(this);
        // 开始计步
        startService();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_start_step:
                // 开始计步
                startService();
                break;
            case R.id.btn_stop_step:
                // 停止计步
                stopService();
                break;
            default:
                break;
        }
    }

    /**
     * 启动服务
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param
     * @return
     */
    private void startService() {
        // 设置开始停止计步按钮是否可用
        mStopStepBtn.setEnabled(true);
        mStartStepBtn.setEnabled(false);
        final Intent intent = new Intent(this, StepService.class);
        // 绑定服务
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        // 启动服务
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        // 解绑服务
        if (conn != null)
            unbindService(conn);
        super.onDestroy();
    }

    /**
     * 停止服务
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param
     * @return
     */
    private void stopService(){
        // 设置开始停止计步按钮是否可用
        mStopStepBtn.setEnabled(false);
        mStartStepBtn.setEnabled(true);
        if (conn != null) {
            // 解绑服务
            unbindService(conn);
        }
        // 发送停止计步服务广播(用于手动停止当前服务)
        Intent intent = new Intent();
        intent.setAction(StepService.ACTION_STOP_SERVICE);
        sendBroadcast(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        // 再按一次退出app
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0){
            if(i==0){
                i++;
                String quit = getApplication().getString(R.string.comm_quit);
                Toast.makeText(MainActivity.this, quit, Toast.LENGTH_SHORT).show();
            }else if(i==1){
                finish();
            }
        }
        return true;
    }
}
```
以上注释已经非常清晰,如有不明白之处请联系.

demo地址:
 [JkStepSensorDemo Link](https://github.com/leibing8912/JkStepSensorDemo/tree/master).


* 邮箱:leibing1989@126.com
* QQ:872721111


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