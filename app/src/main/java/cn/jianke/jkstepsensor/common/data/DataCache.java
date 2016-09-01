package cn.jianke.jkstepsensor.common.data;

import android.content.Context;
import java.util.ArrayList;
import java.util.Date;
import cn.jianke.customcache.data.ListCache;
import cn.jianke.customcache.data.SpLocalCache;
import cn.jianke.jkstepsensor.common.data.bean.StepModel;
import cn.jianke.jkstepsensor.common.utils.DateUtils;

/**
 * @className: DataCache
 * @classDescription: 本地缓存（用于缓存日期、当天步数等数据）
 * @author: leibing
 * @createTime: 2016/08/31
 */
public class DataCache {
    // sington
    private static DataCache instance;
    // 本地缓存
    private SpLocalCache<ListCache> mSpLocalCache;
    // 缓存列表
    private ArrayList<StepModel> mCacheList;
    // 列表缓存
    private ListCache<StepModel> mListCache;

    /**
     * Constructor
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param
     * @return
     */
    private DataCache(){
        mListCache = new ListCache<>();
        mCacheList = new ArrayList<>();
        mSpLocalCache = new SpLocalCache<>(ListCache.class, StepModel.class);
    }

    /**
     * sington
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param
     * @return
     */
    public synchronized static DataCache getInstance(){
        if (instance == null)
            instance = new DataCache();

        return instance;
    }

    /**
     * 添加计步数据
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param context 上下文
     * @param mStepModel 计步数据
     * @return
     */
    public void addStepCache(Context context, final StepModel mStepModel){
        if (mSpLocalCache != null){
            // 读取缓存
            mSpLocalCache.read(context, new SpLocalCache.LocalCacheCallBack() {
                @Override
                public void readCacheComplete(Object obj) {
                    if (obj != null){
                        mListCache = (ListCache<StepModel>) obj;
                        if (mListCache != null) {
                            mCacheList = mListCache.getObjList();
                            if (mCacheList == null || mCacheList.size() == 0){
                                mCacheList.add(mStepModel);
                            }
                            for (StepModel stepModel : mCacheList) {
                                if (mStepModel.getDate().equals(stepModel.getDate()) ) {
                                    // 新步数与旧步数差值不能为负数
                                    int cha = Integer.parseInt(mStepModel.getStep())
                                            - Integer.parseInt(stepModel.getStep());
                                    if (cha >= 0) {
                                        mCacheList.remove(stepModel);
                                        mCacheList.add(mStepModel);
                                    }
                                    break;
                                }
                            }
                        }
                    }else {
                        mCacheList.add(mStepModel);
                    }
                }
            });
            // 保存缓存
            mListCache.setObjList(mCacheList);
            mSpLocalCache.save(context, mListCache);
        }
    }

    /**
     * 获取当天计步缓存
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param context 上下文
     * @param mDataCacheListener 缓存数据监听
     * @return
     */
    public void getTodayCache(Context context,DataCacheListener mDataCacheListener){
        getCacheByDate(context, new Date(), mDataCacheListener);
    }

    /**
     * 通过日期拿取计步缓存
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param context 上下文
     * @param date 日期
     * @param mDataCacheListener 缓存数据监听
     * @return
     */
    public void getCacheByDate(Context context, Date date, final DataCacheListener mDataCacheListener){
        // 日期 格式如2016/08/31
        final String dateStr = DateUtils.simpleDateFormat(date);
        if (mSpLocalCache != null){
            // 读取缓存
            mSpLocalCache.read(context, new SpLocalCache.LocalCacheCallBack() {
                @Override
                public void readCacheComplete(Object obj) {
                    if (obj != null){
                        mListCache = (ListCache<StepModel>) obj;
                        if (mListCache != null && mDataCacheListener != null){
                            mCacheList = mListCache.getObjList();
                            for (StepModel stepModel : mCacheList) {
                                if (dateStr.equals(stepModel.getDate())) {
                                    mDataCacheListener.readListCache(stepModel);
                                    return;
                                }
                            }
                        }
                    }
                    // 当缓存无数据时,默认缓存步数为0步
                    StepModel model = new StepModel();
                    model.setDate(dateStr);
                    model.setStep(0 + "");
                    mDataCacheListener.readListCache(model);
                }
            });
        }
    }

    /**
     * 清除所有计步缓存数据
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param context 上下文
     * @return
     */
    public void clearAllCache(Context context){
        if (mSpLocalCache != null){
            mSpLocalCache.clear(context);
        }
    }

    /**
     * 清除当天计步数据
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param context 上下文
     * @return
     */
    public void clearTodayData(Context context){
        clearCacheByDate(context, new Date());
    }

    /**
     * 根据日期清除计步数据
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param context 上下文
     * @return
     */
    public void clearCacheByDate(Context context, Date date){
        // 日期 格式如2016/08/31
        final String dateStr = DateUtils.simpleDateFormat(date);
        if (mSpLocalCache != null){
            // 读取缓存
            mSpLocalCache.read(context, new SpLocalCache.LocalCacheCallBack() {
                @Override
                public void readCacheComplete(Object obj) {
                    if (obj != null){
                        mListCache = (ListCache<StepModel>) obj;
                        if (mListCache != null) {
                            mCacheList = mListCache.getObjList();
                            for (StepModel stepModel : mCacheList) {
                                if (dateStr.equals(stepModel.getDate())) {
                                    mCacheList.remove(stepModel);
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * @interfaceName: DataCacheListener
     * @interfaceDescription: 本地缓存监听
     * @author: leibing
     * @createTime: 2016/08/31
     */
    public interface DataCacheListener{
        void readListCache(StepModel stepModel);
    }
}
