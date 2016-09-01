package cn.jianke.jkstepsensor.module.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import cn.jianke.jkstepsensor.R;
import cn.jianke.jkstepsensor.module.service.StepService;
/**
 * @className: SwitchActivity
 * @classDescription: 切换启动关闭服务
 * @author: leibing
 * @createTime: 2016/09/01
 */
public class SwitchActivity extends AppCompatActivity implements View.OnClickListener{
    // 是否停止服务Key
    public final static String IS_SERVICE_STOP_KEY = "is_service_stop";
    // Button
    private Button startServiceBtn,stopServiceBtn;
    // 是否停止服务
    private boolean isStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
        // findView
        startServiceBtn = (Button) findViewById(R.id.btn_start_service);
        stopServiceBtn = (Button) findViewById(R.id.btn_stop_service);
        // 按钮是否可用
        btnIsEnable();
        // onclick
        findViewById(R.id.btn_start_service).setOnClickListener(this);
        findViewById(R.id.btn_stop_service).setOnClickListener(this);
    }

    /**
     * 按钮是否可用
     * @author leibing
     * @createTime 2016/09/01
     * @lastModify 2016/09/01
     * @param
     * @return
     */
    private void btnIsEnable() {
        // 获取Intent传值
        isStop = getIntent().getBooleanExtra(IS_SERVICE_STOP_KEY, false);
        if (isStop){
            startServiceBtn.setEnabled(true);
            stopServiceBtn.setEnabled(false);
        }else {
            startServiceBtn.setEnabled(false);
            stopServiceBtn.setEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_start_service:
                // 启动服务
                startService();
                startServiceBtn.setEnabled(false);
                stopServiceBtn.setEnabled(true);
                break;
            case R.id.btn_stop_service:
                // 停止服务
                stopService();
                startServiceBtn.setEnabled(true);
                stopServiceBtn.setEnabled(false);
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
        Intent intent = new Intent(this, StepService.class);
        startService(intent);
        isStop = false;
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
        isStop = true;
        // 发送停止计步服务广播
        Intent intent = new Intent();
        intent.setAction(StepService.ACTION_STOP_SERVICE);
        sendBroadcast(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(IS_SERVICE_STOP_KEY, isStop);
        setResult(RESULT_OK, intent);
        finish();
    }
}
